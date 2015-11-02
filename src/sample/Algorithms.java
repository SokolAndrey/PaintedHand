package sample;


import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Algorithms {
    /**
     * Algorithms
     */
    static public Mat doCanny(Mat frame, double threshold) {
        // init
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();

        // convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // reduce noise with a 3x3 kernel
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));

        // canny detector, with ratio of lower:upper threshold of 3:1
        // flag is used to more accurate calculation
        Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3, 3, false);

        // using Canny's output as a mask, display the result
        Mat dest = new Mat();
        Core.add(dest, Scalar.all(0), dest);
        frame.copyTo(dest, detectedEdges);

        return dest;
    }

    static public Mat removeBackground(Mat frame) {
        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat thresholdImg = new Mat();

        // threshold the image with the average hue value
        hsvImg.create(frame.size(), CvType.CV_8U);
        Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV);
        Core.split(hsvImg, hsvPlanes);

        // get the average hue value of the image
        double threshValue = getHistAverage(hsvImg, hsvPlanes.get(0));


        Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 180, Imgproc.THRESH_BINARY_INV);
        //Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 180, Imgproc.THRESH_BINARY);

        Imgproc.blur(thresholdImg, thresholdImg, new Size(5, 5));

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, 1), 1);
        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, 1), 3);

        Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 180, Imgproc.THRESH_BINARY);

        // create the new image
        Mat foreground = ImageUtils.createForeground(frame);
        frame.copyTo(foreground, thresholdImg);

        return foreground;
    }

    static private double getHistAverage(Mat hsvImg, Mat hueValues) {
        double average = 0.0;
        Mat hist_hue = new Mat();
        // 0-180: range of Hue values
        int range = 180;
        MatOfInt histSize = new MatOfInt(range);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute the histogram
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, range - 1));

        // get the average Hue value of the image
        // (sum(bin(h)*h))/(image-height*image-width)
        // -----------------
        // equivalent to get the hue of each pixel in the image, add them, and
        // divide for the image size (height and width)
        for (int h = 0; h < range; h++) {
            // for each bin, get its value and multiply it for the corresponding
            // hue
            average += (hist_hue.get(h, 0)[0] * h);
        }

        // return the average hue of the image
        average = average / hsvImg.size().height / hsvImg.size().width;
        return average;
    }
}
