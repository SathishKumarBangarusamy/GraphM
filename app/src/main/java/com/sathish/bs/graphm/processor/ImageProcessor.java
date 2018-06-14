package com.sathish.bs.graphm.processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.sathish.bs.graphm.MainActivity;

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
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ImageProcessor {

    private static final double EPSILON_C = 0.02;
    private static final double MINIMUM_DP_THICKNESS = 1;
    private static final int MINIMUM_DISTANCE = 50;
    private static final int CIRCLE_PARAM_1 = 80;
    private static final int CIRCLE_PARAM_2 = 160;
    private static final int MIN_RADIUS = 0;
    private static final int MAX_RADIUS = 0;
    private static final int MIN_CONTOUR_AREA = 120;
    private static final double SQUARE_MIN_RATIO = 0.90;
    private static final double SQUARE_MAX_RATIO = 1.1;
    private static final int MIN_CONTOUR_AREA_CIRCLE = 5000;
    private static final double MIN_CIRCULARITY_FACTOR = 0.7;
    private static final double MAX_CIRCULARITY_FACTOR = 1.2;
    private static final double THETA_FOR_LINE_DETECTION = Math.PI / 180;
    private static final int RHO_FOR_LINE_DETECTION = 1;
    private static final int MIN_LINE_LENGTH = 100;
    private static final int MAX_LINE_GAP = 80;
    private static final int LINE_THRESHOLD = 10;
    private static final double CIRCLE_BOUNDARY_ADJUST_FACTOR = 10.0;
    private static final int TANGENT_MIN_DISTANCE = 100;
    private static final int THRESHOLD = 100;
    private static final String CIRCLE_LABEL = "CIR";
    private static final String LINE_LABEL = "LINE";
    private static final String RECTANGLE_LABEL = "RECT";
    private static final String TRIANGLE_LABEL = "TRI";
    private static final String POLYGON_LABEL = "POLYGON";
    private Context context;
    private OCRProcessor ocr;
    private Map<String, String> connectedCircles = new HashMap<>();
    private MainActivity.ProcessAsyncTask asyncTask;
    private Map<String, Integer> labels = new HashMap<>();
    private List<Line> lines = new ArrayList<>();
    private List<Circle> circles = new ArrayList<>();
    private Mat source;
    private Mat grayScaled;
    private Mat downScaledImage;
    private Mat upScaledImage;
    private Mat dilatedImage;
    private Mat cannyImage;
    private Mat processedImage;
    private MatOfPoint2f approximateCurvePoint;
    private Random random;

    private static class Circle {
        private Rect rect;
        private String label;

        private Circle(Rect rect, String label) {
            this.rect = rect;
            this.label = label;
        }

        public Rect getRect() {
            return rect;
        }

        public String getLabel() {
            return label;
        }
    }

    private class Line {
        private Point pt1;
        private Point pt2;

        private Line(Point pt1, Point pt2) {
            this.pt1 = pt1;
            this.pt2 = pt2;
        }

        public Point getPt1() {
            return pt1;
        }

        public Point getPt2() {
            return pt2;
        }
    }

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
        ocr = new OCRProcessor(context);
    }

    public static ImageProcessor getImageProcessor(Context context) {
        return new ImageProcessor(context);
    }

    public ImageProcessor source(Mat src, MainActivity.ProcessAsyncTask asyncTask) {
        this.source = src;
        this.asyncTask = asyncTask;
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
        // 5. Blur to remove noise
        Imgproc.GaussianBlur(dilatedImage, processedImage, new Size(7, 7), 2, 2);

        //DEBUG PURPOSE
        saveImage(grayScaled, "1gray");
        saveImage(downScaledImage, "2down");
        saveImage(upScaledImage, "3up");
        saveImage(cannyImage, "4canny");
        saveImage(dilatedImage, "5dil");
        saveImage(processedImage, "6proc");
    }

    private void saveImage(Mat mat, String picName) {
        try {
            File pictureFile = new File("/sdcard/" + picName + ".png");
            FileOutputStream fos = new FileOutputStream(pictureFile);
            Bitmap image = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, image);
            asyncTask.onProgressUpdate(image);
            image.setHasAlpha(true);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> detect() {
        convert();
        detectObjects();
        detectLines();
        return connectedCircles;
    }

    private void detectObjects() {
        Mat cIMG = processedImage, dst = source;
        List<MatOfPoint> matOfPoints = new ArrayList<MatOfPoint>();
        Mat hovIMG = new Mat();
        Imgproc.findContours(cIMG, matOfPoints, hovIMG, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
//        Imgproc.drawContours(dst, matOfPoints, -1, new Scalar(255,0,0), 3);
        for (MatOfPoint point : matOfPoints) {
            MatOfPoint2f curvePoint = new MatOfPoint2f(point.toArray());
            Imgproc.approxPolyDP(curvePoint, approximateCurvePoint, EPSILON_C * Imgproc.arcLength(curvePoint, true), true);
            int numberVertices = (int) approximateCurvePoint.total();

            if (numberVertices < 3)
                continue;

            /* Triangle
            if (numberVertices == 3) {
                labelDetectedRegion(dst, point, TRIANGLE_LABEL, new Scalar(255, 255, 0));
            }*/

            // Rectangle, Square, And Polygon. Circle is a polygon with high number of vertices.

            if (numberVertices >= 4) {
                Rect rect = Imgproc.boundingRect(point);

                double contourArea = Imgproc.contourArea(point);

                /*
                 * ELIMINATE ALL THE FALSE DETECTIONS AND SMALL SHAPES
                 * Value should be changed based on the source image resolution
                 */
                Log.d("IP", contourArea + " " + numberVertices);
                if (Math.abs(contourArea) < MIN_CONTOUR_AREA) {
                    continue;
                }


                List<Double> cos = new ArrayList<>();

                for (int j = 2; j < numberVertices + 1; j++) {
                    cos.add(angle(approximateCurvePoint.toArray()[j % numberVertices], approximateCurvePoint.toArray()[j - 2], approximateCurvePoint.toArray()[j - 1]));
                }

                Collections.sort(cos);

                double mincos = cos.get(0);
                double maxcos = cos.get(cos.size() - 1);

                float w = rect.width;
                float h = rect.height;
                float s = w / h; // ASPECT RATIO FOR SQUARE DETECTION.

                if (numberVertices == 4 && mincos >= -0.1 && maxcos <= 0.3) {
                    if (s >= SQUARE_MIN_RATIO && s <= SQUARE_MAX_RATIO) {
                        labelDetectedRegion(dst, point, "SQ", new Scalar(128, 128, 0));
                    } else {
                        labelDetectedRegion(dst, point, RECTANGLE_LABEL, new Scalar(255, 0, 0));
                    }
                }
/*
                if (numberVertices == 5) {
                    labelDetectedRegion(dst, point, "PENT", new Scalar(255, 128, 0));
                }
                if (numberVertices == 6) {
                    labelDetectedRegion(dst, point, "HEX", new Scalar(255, 128, 0));
                }
                if (numberVertices == 7) {
                    labelDetectedRegion(dst, point, "HEP", new Scalar(255, 128, 0));
                }
*/
                //Possibly a circle. A circle is a polygon with high number of vertices
                if (numberVertices >= 6) {
                    double perimeter = Imgproc.arcLength(new MatOfPoint2f(point.toArray()), true);
                    if (perimeter == 0)
                        continue;
                    double circularity = 4 * 3.14 * (contourArea / (perimeter * perimeter));
                    Log.d("IP C", String.valueOf(circularity) + " " + contourArea);
                    if ((MIN_CIRCULARITY_FACTOR < circularity && circularity < MAX_CIRCULARITY_FACTOR) && (contourArea > MIN_CONTOUR_AREA_CIRCLE)) {
                        labelDetectedRegion(dst, point, "CIRCLE" + numberVertices + " " + contourArea, new Scalar(255, 0, 0));
                    }

                }
            }

/*
            // Polygon
            if (numberVertices > 4) {
                labelDetectedRegion(dst, point, POLYGON_LABEL, new Scalar(255, 225, 0));
            }
*/

        }
    }

    private void labelDetectedRegion(Mat dst, MatOfPoint cnt, String label, Scalar scalar) {
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 1;
        int thickness = 4;
        int[] baseline = new int[1];
        Rect rect = Imgproc.boundingRect(cnt);
        int boundary = 0;
        //Crop for OCR
        Mat mat = dilatedImage.submat(rect.y + boundary, rect.y + boundary + rect.height, rect.x + boundary, rect.x + boundary + rect.width);
        Bitmap image = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, image);
        //Passing to OCR Processor for the label extraction
        label = ocr.extractText(image);
        if (labels.containsKey(label)) {
            int i = labels.get(label);
            i++;
            labels.put(label, i);
            label += String.valueOf(i);
        } else {
            labels.put(label, 0);
        }
        Size text = Imgproc.getTextSize(label, fontface, scale, thickness, baseline);
        Point pt = new Point(rect.x + ((rect.width - text.width) / 2), rect.y + ((rect.height + text.height) / 2));
        Imgproc.putText(dst, label, pt, fontface, scale, scalar, thickness);
        Imgproc.rectangle(dst, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), scalar, thickness);
        circles.add(new Circle(rect, label));
    }


    private void detectLines() {
        // Line Detection

        random = new Random();
        Mat lines = new Mat();
        Mat covered = processedImage.clone();
        for (Circle circle : circles) {
            Imgproc.rectangle(covered, new Point(circle.rect.x, circle.rect.y), new Point(circle.rect.x + circle.rect.width, circle.rect.y + circle.rect.height), new Scalar(0, 0, 0), -1);
        }
        saveImage(covered, "8covered");

        Mat linesP = new Mat();

        Imgproc.HoughLinesP(covered, linesP, RHO_FOR_LINE_DETECTION, THETA_FOR_LINE_DETECTION, LINE_THRESHOLD, MIN_LINE_LENGTH, MAX_LINE_GAP);

        // Find all lines

        for (int i = 0; i < linesP.rows(); i++) {
            double[] j = linesP.get(i, 0);
            Point pt1 = new Point(j[0], j[1]);
            Point pt2 = new Point(j[2], j[3]);
            this.lines.add(new Line(pt1, pt2));
            findConnectingCircle(i, pt1, pt2);
        }
/*
        // merge
        int thres = 30;
        List<Line> avg = new ArrayList<>();
        for (int i = 0; i < this.lines.size(); i++) {
            Line line = this.lines.get(i);
            Point avgPt1 = new Point(line.pt1.x, line.pt1.y);
            Point avgPt2 = new Point(line.pt2.x, line.pt2.y);
            for (int j = 0; j < this.lines.size(); j++) {
                if (i == j)
                    continue;
                Line line2 = this.lines.get(j);
                double x1 = avgPt2.x, x2 = line2.pt1.x, y1 = avgPt2.y, y2 = line2.pt1.y;
                double dis = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
                if (Math.abs(dis) <= thres) {
                    dis = Math.sqrt((line2.pt2.x - line2.pt1.x) * (line2.pt2.x - line2.pt2.x) + (line2.pt2.y - line2.pt1.y) * (line2.pt2.y - line2.pt1.y));
                    avgPt2.x += dis;
                    avgPt2.y += dis;
                }

                x1 = avgPt1.x;
                x2 = line2.pt2.x;
                y1 = avgPt1.y;
                y2 = line2.pt2.y;
                dis = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
                if (Math.abs(dis) <= thres) {
                    dis = Math.sqrt((line2.pt2.x - line2.pt1.x) * (line2.pt2.x - line2.pt2.x) + (line2.pt2.y - line2.pt1.y) * (line2.pt2.y - line2.pt1.y));
                    avgPt1.x -= dis;
                    avgPt1.y -= dis;
                }

            }
            avg.add(new Line(avgPt1, avgPt2));
        }

        int i=0;
        for (Line line : avg){
            findConnectingCircle(i++, line.pt1, line.pt2);
        }
*/


/*        Imgproc.HoughLines(covered, lines, 1, Math.PI / 180, 150);

        for (int x = 0; x < lines.rows(); x++) {
            Log.d("IP Line", String.valueOf(lines.rows()));
            double rho = lines.get(x, 0)[0], theta = lines.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;
            Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
            Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));
            findConnectingCircle(x, pt1, pt2);
            Log.d("IP Line r,t", String.valueOf(rho + " " + theta + " " + x0 + " " + y0));
        }*/


    }

    private void findConnectingCircle(int i, Point pt1, Point pt2) {
        double x1 = pt1.x;
        double y1 = pt1.y;
        double x2 = pt2.x;
        double y2 = pt2.y;
        double dis = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        boolean start = false, end = false;
        String labelA = "", labelB = "";
        String c1 = "", c2 = "";
        Circle circle1 = null, circle2 = null;
/*
        try {
            Mat matl = new Mat();
            int bound = 25;
            Mat mat = processedImage.submat((int) pt1.y - bound, (int) pt1.y + bound, (int) pt1.x - bound, (int) pt1.x + bound);
            Imgproc.HoughLinesP(mat, matl, 1, Math.PI / 180, 10, 5, 50);
            Log.d("IP L C :", String.valueOf(matl.rows()));
            c1 = (matl.rows() >= 40) ? "\"" : ((matl.rows() >= 30 && matl.rows() < 40) ? "'" : "");
            mat = processedImage.submat((int) pt2.y - bound, (int) pt2.y + bound, (int) pt2.x - bound, (int) pt2.x + bound);
            Imgproc.HoughLinesP(mat, matl, 1, Math.PI / 180, 10, 5, 50);
            Log.d("IP L C :", String.valueOf(matl.rows()));
            c2 = (matl.rows() >= 40) ? "\"" : ((matl.rows() >= 30 && matl.rows() < 40) ? "'" : "");
        } catch (Exception e) {
            e.printStackTrace();
        }
*/

        // We can reduce the iterations by Segmenting the given image and circles. When we find a line point in a segment, analyzing the circles that are in that segment is enough.
        for (Circle circle : circles) {
            double cx1 = circle.rect.x;
            double cx2 = circle.rect.x + circle.rect.width;
            double cy1 = circle.rect.y;
            double cy2 = circle.rect.y + circle.rect.height;
            double thres = CIRCLE_BOUNDARY_ADJUST_FACTOR; // threshold value for finding the line around the given circle.
            // for line point x1,y1
            if ((cx1 - thres <= x1 && x1 <= cx2 + thres) && (cy1 - thres <= y1 && y1 <= cy2 + thres)) {
                start = true;
                labelA = circle.label;
                circle1 = circle;
            }
            // for line point x2,y2
            if ((cx1 - thres <= x2 && x2 <= cx2 + thres) && (cy1 - thres <= y2 && y2 <= cy2 + thres)) {
                end = true;
                labelB = circle.label;
                circle2 = circle;
            }
        }
        //
        if (start && end && dis > TANGENT_MIN_DISTANCE) {
            if (circle1 == circle2)
                return;
            Log.d("IP Line, x1,y1 x2,y2 ", String.format("%d, %f,%f %f,%f D:%f", i, x1, y1, x2, y2, dis) + " " + (start && end) + " L:" + labelA + " " + labelB);
            String A = labelA + c1, B = labelB + c2;
            if (!connectedCircles.containsKey(B + "," + A))
                connectedCircles.put(A + "," + B, labelB);
            Imgproc.line(source, pt1, pt2, new Scalar((random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255), 3, Imgproc.LINE_AA, 0);
        }

       // Imgproc.line(source, pt1, pt2, new Scalar((random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255), 3, Imgproc.LINE_AA, 0);
    }

    // finds a cos angle between vectors
    private double angle(Point point1, Point point2, Point point0) {
        double dx1 = point1.x - point0.x;
        double dy1 = point1.y - point0.y;
        double dx2 = point2.x - point0.x;
        double dy2 = point2.y - point0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

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
            }
        }
    }

}
