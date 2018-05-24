package com.sathish.bs.graphm.processor;

import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageProcessor {
    public static final double EPSILON_C = 2.0E-02;
    public static final double MINIMUM_DP_THICKNESS = 1.0E-05;
    public static final int MINIMUM_DISTANCE = 180;
    public static final int CIRCLE_PARAM_1 = 90;
    public static final int CIRCLE_PARAM_2 = 180;
    public static final int MIN_RADIUS = 0;
    public static final int MAX_RADIUS = 0;
    private Mat source;
    private Mat grayScaled;
    private Mat downScaledImage;
    private Mat upScaledImage;
    private Mat dilatedImage;
    private Mat cannyImage;
    private Mat processedImage;
    private MatOfPoint2f approximateCurvePoint;
    private static final int THRESHOLD = 100;
    private static final String CIRCLE_LABEL = "CIR";
    private static final String LINE_LABEL = "LINE";
    private static final String RECTANGLE_LABEL = "RECT";
    private static final String TRIANGLE_LABEL = "TRI";
    private static final String POLYGON_LABEL = "POLYGON";
    public Context context;

    private ImageProcessor() {

    }

    private ImageProcessor(Context context) {
        approximateCurvePoint = new MatOfPoint2f();
        grayScaled = new Mat();
        downScaledImage = new Mat();
        upScaledImage = new Mat();
        cannyImage = new Mat();
        dilatedImage = new Mat();
        processedImage = new Mat();
        this.context = context;
    }

    public static ImageProcessor getImageProcessor(Context context) {
        return new ImageProcessor(context);
    }

    public ImageProcessor source(Mat src) {
        this.source = src;
        return this;
    }

    private void convert() {
        // 1. Convert to GrayScale
        Imgproc.cvtColor(source, grayScaled, Imgproc.COLOR_BGR2GRAY);
        // 2. Image Pyramids https://docs.opencv.org/2.4/doc/tutorials/imgproc/pyramids/pyramids.html
        Imgproc.pyrDown(grayScaled, downScaledImage, new Size(grayScaled.cols() / 2, grayScaled.rows() / 2));
        Imgproc.pyrUp(downScaledImage, upScaledImage, new Size(downScaledImage.cols() * 2, downScaledImage.rows() * 2));
        // 3. Detect Edges
        Imgproc.Canny(upScaledImage, cannyImage, 0, THRESHOLD);
        // 4. Morphological Transformations
        Imgproc.dilate(cannyImage, dilatedImage, new Mat(), new Point(-1, 1), 1);
        Imgproc.GaussianBlur(dilatedImage, processedImage, new Size(5, 5), 2, 2);
    }

    public void detect() {
        convert();
        detectRectangularShapes(processedImage, source);
        detectCircularShape(processedImage, source);
    }

    private void detectRectangularShapes(Mat cIMG, Mat dst) {
        List<MatOfPoint> matOfPoints = new ArrayList<MatOfPoint>();
        Mat hovIMG = new Mat();
        Imgproc.findContours(cIMG, matOfPoints, hovIMG, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (MatOfPoint point : matOfPoints) {
            MatOfPoint2f curvePoint = new MatOfPoint2f(point.toArray());
            Imgproc.approxPolyDP(curvePoint, approximateCurvePoint, EPSILON_C * Imgproc.arcLength(curvePoint, true), true);
            int numberVertices = (int) approximateCurvePoint.total();

            if (numberVertices < 2)
                continue;

            // Line
            if (numberVertices == 2) {
                labelDetectedRegion(dst, point, LINE_LABEL, new Scalar(128,0,255));
            }

            // Triangle
            if (numberVertices == 3) {
                labelDetectedRegion(dst, point, TRIANGLE_LABEL,new Scalar(87,97,128));
            }

            // Rectangle
            if (numberVertices == 4) {
                labelDetectedRegion(dst, point, RECTANGLE_LABEL,new Scalar(90,90,90));
            }

            // Polygon
            if (numberVertices > 4) {
                labelDetectedRegion(dst, point, POLYGON_LABEL,new Scalar(255,22,0));
            }
        }
    }

    private void labelDetectedRegion(Mat dst, MatOfPoint cnt, String label, Scalar scalar) {
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 1;
        int thickness = 1;
        int[] baseline = new int[1];
        Size text = Imgproc.getTextSize(label, fontface, scale, thickness, baseline);
        Rect r = Imgproc.boundingRect(cnt);
        Point pt = new Point(r.x + ((r.width - text.width) / 2), r.y + ((r.height + text.height) / 2));
        Imgproc.putText(dst, label, pt, fontface, scale, scalar, thickness);
        Imgproc.rectangle(dst, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), scalar, thickness);
    }

    /*
        finds a cos angle between vectors
    private double angle(Point point1, Point point2, Point point0) {
        double dx1 = point1.x - point0.x;
        double dy1 = point1.y - point0.y;
        double dx2 = point2.x - point0.x;
        double dy2 = point2.y - point0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }
    */

    private void detectCircularShape(Mat mat, Mat dst) {
        Mat circles = new Mat(mat.width(), mat.height(), CvType.CV_8UC1);
        Imgproc.HoughCircles(mat, circles, Imgproc.CV_HOUGH_GRADIENT, MINIMUM_DP_THICKNESS, MINIMUM_DISTANCE, CIRCLE_PARAM_1, CIRCLE_PARAM_2, MIN_RADIUS, MAX_RADIUS);
        for (int i = 0; i < circles.cols(); i++) {
            /*
             * x = circle[0], y = circle[1], radius = circle[2]
             */
            double[] circleCoordinates = circles.get(0, i);
            if (circleCoordinates != null && circleCoordinates.length == 3) {
                int x = (int) circleCoordinates[0], y = (int) circleCoordinates[1];
                Point center = new Point(x, y);
                int radius = (int) circleCoordinates[2];
                Imgproc.putText(dst, CIRCLE_LABEL, center, Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(255, 255, 0), 2);
                Imgproc.circle(dst, center, radius, new Scalar(255, 255, 0), 4);
                /*

                Passing to OCR Processor for the label extraction

                Bitmap bitmap = Bitmap.createBitmap(source.cols(), source.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(source, bitmap);
                new OCRProcessor(context).extractText(bitmap);

                */
            }
        }
    }

}
