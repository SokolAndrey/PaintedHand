package sample;

import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Controller {

    @FXML
    private ImageView currentFrame;
    @FXML
    private ImageView filteredFrame;
    @FXML
    private ImageView backgroundFrame;

    @FXML
    private Button btnCut;
    @FXML
    private Button buttonToPrintTheHand;
    @FXML
    private Slider threshold;

    @FXML
    private Slider hueStart;
    @FXML
    private Slider hueStop;
    @FXML
    private Slider saturationStart;
    @FXML
    private Slider saturationStop;
    @FXML
    private Slider valueStart;
    @FXML
    private Slider valueStop;

    private Mat image2Print = null;

    private boolean isItPushed = false;
    private boolean isHandPrinted = false;
    private VideoCapture capture = new VideoCapture();
    private ScheduledExecutorService service;
    private Mat hand;

    private ObjectProperty<Image> filteredProperty;
    private static int counter = 0;


    /**
     * Actions with camera
     */

    public void startCamera() {
        // bind an image property with the original frame container
        //final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
        //currentFrame.imageProperty().bind(imageProperty);

        //filteredProperty = new SimpleObjectProperty<>();
        //filteredFrame.imageProperty().bind(filteredProperty);

        currentFrame.setFitWidth(400);
        currentFrame.setPreserveRatio(true);
        filteredFrame.setFitWidth(200);
        filteredFrame.setPreserveRatio(true);
        backgroundFrame.setFitWidth(200);
        backgroundFrame.setPreserveRatio(true);


        // open web cam
        capture.open(0);
        // check is it available
        if (capture.isOpened()) {
            Runnable task = () -> {
                List<Image> frames = grabFrames();
                //Image frame = grabFrame();
                Image frameROI = frames.get(0);
                Image frame = frames.get(1);
                Image frameBackground = frames.get(2);
                currentFrame.setImage(frame);
                filteredFrame.setImage(frameROI);
                backgroundFrame.setImage(frameBackground);

            };
            service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(task, 0, 33, TimeUnit.MILLISECONDS);
        }
    }

    // DO NOT forget to release your camera
    public void stopCamera() {
        capture.release();
        service.shutdown();
    }


    private List<Image> grabFrames() {
        Image imageToShow = null;
        Image imageOfObject = null;
        Image imageWithoutBackground = null;
        Mat frame = new Mat();
        Mat object = new Mat();
        Mat background = new Mat();
        List<Image> tmp = new ArrayList<>();
        Scalar minValues = new Scalar(hueStart.getValue(), saturationStart.getValue(),
                valueStart.getValue());
        Scalar maxValues = new Scalar(hueStop.getValue(), saturationStop.getValue(),
                valueStop.getValue());
        try {
            capture.read(frame);
            if (!frame.empty()) {
                object = ImageUtils.findObject(frame, minValues, maxValues);
                background = Algorithms.removeBackground(frame);
                imageOfObject = ImageUtils.mat2Image(object);
                imageToShow = ImageUtils.mat2Image(frame);
                imageWithoutBackground = ImageUtils.mat2Image(background);
            }

        } catch (Exception e) {
            // log the error
            System.err.println("Huston, something went wrong: " + e);
        }

        tmp.add(imageOfObject);
        tmp.add(imageToShow);
        tmp.add(imageWithoutBackground);
        return tmp;
    }




    private Image grabFrame() {
        Image imageToShow = null;
        Mat frame = new Mat();
        // save a copy of the original frame

        try {
            capture.read(frame);
            Mat blurredImage = new Mat();
            Mat hsvImage = new Mat();

            // filtered
            Imgproc.blur(frame, blurredImage, new Size(7, 7));
            // from rgb to hsv
            Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);


            // if the frame is not empty, process it
            if (!frame.empty()) {
                loadHand();
                if (isHandPrinted)
                    ImageUtils.addPictureToFrame(frame, hand);
                if (isItPushed)
                    //frame = removeBackground(frame);
                    frame = Algorithms.doCanny(frame, threshold.getValue());
                imageToShow = ImageUtils.mat2Image(frame);
            }

        } catch (Exception e) {
            // log the error
            System.err.println("Huston, something went wrong: " + e);
        }
        return imageToShow;
    }


    private void loadHand() {
        hand = Imgcodecs.imread("resources/hand3.png");
    }


    /**
     * Action events
     */


    // Event to cut everything exclude the hand
    // The hand should be in the borders
    @FXML
    public void cutHand(ActionEvent actionEvent) {
        System.out.println("Hey");
        isItPushed = !isItPushed;

    }


    public void printTheHand(ActionEvent actionEvent) {
        isHandPrinted = !isHandPrinted;
        if (isHandPrinted) {
            buttonToPrintTheHand.setText("Delete the hand");
        } else {
            buttonToPrintTheHand.setText("Print the hand");
        }
    }

    @FXML
    public void printTheScreen(ActionEvent actionEvent) {
        Imgcodecs.imwrite("image" + counter++ + ".png", image2Print);
    }
}
