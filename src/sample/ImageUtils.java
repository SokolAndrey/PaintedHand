package sample;

import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {
    /**
     * Some modifications with image
     */

    static public Mat createBackground(Mat frame) {
        return new Mat(frame.size(), CvType.CV_8UC3, new Scalar(0, 0, 0));
    }


    static public Mat createForeground(Mat frame) {
        return new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
    }

    static public Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }


    static public boolean isImageFitted(Mat newImage, Mat sourceImage) {
        if (newImage.cols() <= sourceImage.cols() && newImage.rows() <= sourceImage.rows())
            return true;
        System.out.println("Hand can not be printed, because of size");
        return false;
    }

    static public void addPictureToFrame(Mat frame, Mat pic) {
        if (isImageFitted(pic, frame)) {
            Rect roi = new Rect(0, 0, pic.cols(), pic.rows());
            Mat placeToROI = frame.submat(roi);
            Core.addWeighted(placeToROI, 1.0, pic, 0.8, 0.0, placeToROI);
        } else {
            System.out.println("Huston, something went wrong");
        }
    }


    public static Mat findAndDraw(Mat maskedImage, Mat frame)
    {
        // init
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // find contours
        Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // if any contour exist...
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
        {
            // for each contour, display it in blue
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
            {
                Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
            }
        }

        return frame;
    }


    public static Mat findObject(Mat frame, Scalar minValues, Scalar maxValues) {
        Image imageToShow = null;
        Mat frameROI = null;
        Rect roi = new Rect(50, 50, 400, 400);
        Mat blurredImage = new Mat();
        Mat mask = new Mat();
        Mat hsvImage = new Mat();
        Mat morphOutput = new Mat();

        if (!frame.empty()) {
            frameROI = new Mat(frame, roi);
            Imgproc.blur(frameROI, blurredImage, new Size(7, 7));
            // from rgb to hsv
            Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);
            frameROI = Algorithms.doCanny(frameROI, 128.0);
            Core.inRange(hsvImage, minValues, maxValues, mask);
            Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
            Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

            Imgproc.erode(mask, morphOutput, erodeElement);
            Imgproc.erode(mask, morphOutput, erodeElement);
            Imgproc.erode(mask, morphOutput, erodeElement);

            //Imgproc.dilate(mask, morphOutput, dilateElement);
            //Imgproc.dilate(mask, morphOutput, dilateElement);
            mask.adjustROI(0, 0, 640, 480);
            ImageUtils.findAndDraw(mask, frame);

        }
        return frameROI;
    }

}
