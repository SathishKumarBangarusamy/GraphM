package com.sathish.bs.graphm.processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.sathish.bs.graphm.MainActivity;

import org.opencv.android.Utils;
import org.opencv.core.Core;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import com.sathish.bs.graphm.graph.Edge;
import com.sathish.bs.graphm.graph.Graph;
import com.sathish.bs.graphm.graph.Position;
import com.sathish.bs.graphm.graph.Vertex;
import com.sathish.bs.graphm.graph.Vertex1;
import com.sathish.bs.graphm.graph.Vertex2;

public class ImageProcessor {

    private static double MIN_CONTOUR_AREA = 120;
    private static double MIN_CONTOUR_AREA_CIRCLE = 5000;
    private static double TANGENT_MIN_DISTANCE = 100;
    private static double MIN_LINE_LENGTH = 25;
    private static double MAX_LINE_GAP = 80;
    private static double CIRCLE_BOUNDARY_ADJUST_FACTOR = 10.0;
    private static double SMALL_LINE_MAX_DIST = 50;
    private static int LINE_THRESHOLD = 10;
    private static int OCR_BOUND_RECT = 25;
    private static double bound = 7;

    private static final double EPSILON_C = 0.01;
    private static final double MINIMUM_DP_THICKNESS = 1;
    private static final int MINIMUM_DISTANCE = 50;
    private static final int CIRCLE_PARAM_1 = 80;
    private static final int CIRCLE_PARAM_2 = 160;
    private static final int MIN_RADIUS = 0;
    private static final int MAX_RADIUS = 0;
    private static final double SQUARE_MIN_RATIO = 0.90;
    private static final double SQUARE_MAX_RATIO = 1.1;
    private static final double MIN_CIRCULARITY_FACTOR = 0.7;
    private static final double MAX_CIRCULARITY_FACTOR = 1.2;
    private static final double THETA_FOR_LINE_DETECTION = Math.PI / 180;
    private static final int RHO_FOR_LINE_DETECTION = 1;

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
    private List<Line> sLines = new ArrayList<>();
    private List<Circle> circles = new ArrayList<>();
    private List<Circle> ords = new ArrayList<>();
    private Mat source;
    private Mat grayScaled;
    private Mat downScaledImage;
    private Mat upScaledImage;
    private Mat dilatedImage;
    private Mat cannyImage;
    private Mat processedImage;
    private MatOfPoint2f approximateCurvePoint;
    private Random random;
    private Mat binary;
    private Mat bilateral;
    private int edges = 0;
    private List<Point> edgePoints = new ArrayList<>();
    private Mat coveredS;
    private List<Line> edgeLines = new ArrayList<>();

    private final Scalar[] scalars = new Scalar[]{new Scalar(255, 255, 0), new Scalar(0, 255, 255), new Scalar(255, 0, 255), new Scalar(255, 0, 0), new Scalar(0, 255, 0), new Scalar(0, 0, 255), new Scalar(255, 128, 128), new Scalar(128, 128, 255), new Scalar(128, 255, 128), new Scalar(128, 128, 128)};

    private int ordinalMean;

    private static class Circle {
        private Rect rect;
        private String label;
        private Point centerPoint;

        private Circle(Rect rect, String label, Point centerPoint) {
            this.rect = rect;
            this.label = label;
            this.centerPoint = centerPoint;
        }

        public Rect getRect() {
            return rect;
        }

        public String getLabel() {
            return label;
        }

        public Point getCenterPoint() {
            return centerPoint;
        }
    }

    public static class Line {
        private Point pt1;
        private Point pt2;
        private String nodeAtP1;
        private String nodeAtP2;
        private String ordinalityAtP1;
        private String ordinalityAtP2;
        private int label1;
        private int label2;

        public Line(Point pt1, Point pt2) {
            this.pt1 = pt1;
            this.pt2 = pt2;
        }

        public Point getPt1() {
            return pt1;
        }

        public Point getPt2() {
            return pt2;
        }

        public int getLabel1() {
            return label1;
        }

        public int getLabel2() {
            return label2;
        }

        public void setLabel1(int label1) {
            this.label1 = label1;
        }

        public void setLabel2(int label2) {
            this.label2 = label2;
        }

        public Line setNodeAtP1(String nodeAtP1) {
            this.nodeAtP1 = nodeAtP1;
            return this;
        }

        public Line setNodeAtP2(String nodeAtP2) {
            this.nodeAtP2 = nodeAtP2;
            return this;
        }

        public String getOrdinalityAtP1() {
            return ordinalityAtP1;
        }

        public String getOrdinalityAtP2() {
            return ordinalityAtP2;
        }

        public void setOrdinalityAtP1(String ordinalityAtP1) {
            this.ordinalityAtP1 = ordinalityAtP1;
        }

        public void setOrdinalityAtP2(String ordinalityAtP2) {
            this.ordinalityAtP2 = ordinalityAtP2;
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
        binary = new Mat();
        bilateral = new Mat();
        ocr = new OCRProcessor(context);
        random = new Random();
        this.context = context;
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
        Imgproc.adaptiveThreshold(upScaledImage, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 25, 5);
        // 3. Detect Edges
        Imgproc.Canny(upScaledImage, cannyImage, 0, THRESHOLD);
        // 4. Morphological Transformations
        Imgproc.dilate(cannyImage, dilatedImage, new Mat(), new Point(-1, -1), 1);
        // 5. Blur to remove noise
        Imgproc.GaussianBlur(dilatedImage, processedImage, new Size(7, 7), 2, 2);

        Imgproc.bilateralFilter(binary, bilateral, 15, 80, 80, Core.BORDER_DEFAULT);


        coveredS = new Mat();

        Mat temp = new Mat(), temp1 = new Mat();
        Imgproc.Canny(grayScaled, temp, 0, THRESHOLD);
        // 4. Morphological Transformations

        Imgproc.dilate(temp, coveredS, new Mat(), new Point(-1, -1), 1);

        setParamConfigurations();
//        private static final int MIN_CONTOUR_AREA = 120; // 166.2337662338
//        private static final int MIN_CONTOUR_AREA_CIRCLE = 5000; //6926.4069264069
//        private static final int TANGENT_MIN_DISTANCE = 100; // 138.5281385281
//        private static int OCR_BOUND_RECT = 25; // 34.632034632
//        private static final int MIN_LINE_LENGTH = 25; // 34.632034632
//        private static final int MAX_LINE_GAP = 80; // 110.8225108225
//        private static final int LINE_THRESHOLD = 10; // 13.8528138528
//        private static final double CIRCLE_BOUNDARY_ADJUST_FACTOR = 10.0; // 13.8528138528
//        private static final int SMALL_LINE_MAX_DIST = 50; // 69.2640692641

        //DEBUG PURPOSE
        saveImage(grayScaled, "1gray");
        saveImage(downScaledImage, "2down");
        saveImage(upScaledImage, "3up");
        saveImage(cannyImage, "4canny");
        saveImage(dilatedImage, "5dil");
        saveImage(processedImage, "6proc");
        saveImage(binary, "9ocr");
    }

    private void setParamConfigurations() {
        double ratio = getSourceRatio();
        MIN_CONTOUR_AREA = ratio * 166.2337662338;
        MIN_CONTOUR_AREA_CIRCLE = ratio * 3926.4069264069;
        TANGENT_MIN_DISTANCE = ratio * 138.5281385281;
        OCR_BOUND_RECT = (int) (ratio * 34.632034632);
        MIN_LINE_LENGTH = ratio * 34.632034632;
        MAX_LINE_GAP = ratio * 80;
        LINE_THRESHOLD = 10;
        CIRCLE_BOUNDARY_ADJUST_FACTOR = ratio * 13.8528138528;
        SMALL_LINE_MAX_DIST = ratio * 69.2640692641 * 3;
        bound = ratio * 9.696969697;
    }

    private double getSourceRatio() {
        return source.rows() / source.height();
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
    private void saveImage(Bitmap image, String picName) {
        try {
            File pictureFile = new File("/sdcard/" + picName + ".png");
            FileOutputStream fos = new FileOutputStream(pictureFile);
            asyncTask.onProgressUpdate(image);
            image.setHasAlpha(true);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Object[] detect() {
        convert();
        findNodes();
        findRelationships();
        findSmallLines(coveredS);
        Log.d("Graph Result : ", generateJSON());
        return new Object[]{connectedCircles, source};
    }

    private String generateJSON() {
        Graph graph = new Graph();
        List<Vertex> vertices = graph.getVertices();
        for (Circle circle : circles) {
            vertices.add(new Vertex()
                    .withValue("vertex-" + circle.label)
                    .withDisplayValue(circle.label)
                    .withPosition(new Position().withX(circle.centerPoint.x).withY(circle.centerPoint.y)));
        }
        List<Edge> edges = graph.getEdges();
        for (Line line : edgeLines) {
            edges.add(new Edge()
                    .withVertex1(new Vertex1().withValue("vertex-" + line.nodeAtP1).withName(line.nodeAtP1).withSymbol(line.ordinalityAtP1))
                    .withVertex2(new Vertex2().withValue("vertex-" + line.nodeAtP2).withName(line.nodeAtP2).withSymbol(line.ordinalityAtP2))
                    .withValue("vertex-" + line.nodeAtP1 + " vertex-" + line.nodeAtP2)
            );
        }
        return new GsonBuilder().setPrettyPrinting().create().toJson(graph);
    }
/*
    private void findOrdinality() {

        int nrow = 0;
        double[][] centroids = new double[edgePoints.size()][2];
        for (Point point : edgePoints) {
            centroids[nrow][0] = point.x;
            centroids[nrow][1] = point.y;
            nrow++;
        }
        if (sLines.size() > 0) {
            source = new Mat(source.rows(), source.cols(), source.type());
            KMeans KM = new KMeans(sLines);
            KM.clustering(edges * 2, -1, centroids);
            KMeans.KMeansResult kMeansResult = KM.getResults();
            kMeansResult.getPoints().forEach(new Consumer<KMeans.Point>() {
                @Override
                public void accept(KMeans.Point point) {
                    Imgproc.drawMarker(source, point.getPoint(), scalars[point.getLabel()], Imgproc.MARKER_CROSS, 10, 1, 8);
                }
            });
            kMeansResult.getLines().forEach(new Consumer<Line>() {
                @Override
                public void accept(Line line) {
                    Log.i("KMEAN CLUSTER LABEL : ", line.pt1.toString() + " , " + line.pt2.toString() + " : " + line.getLabel1() + "," + line.getLabel2());
                }
            });
            kMeansResult.getCentroids().forEach(new Consumer<Point>() {
                @Override
                public void accept(Point centroid) {
                    Log.i("KMEAN Centroid : ", centroid.toString());
                }
            });
            kMeansResult.getClusterMap().forEach(new BiConsumer<Integer, KMeans.Cluster>() {
                @Override
                public void accept(Integer integer, KMeans.Cluster cluster) {
                    Log.i("KMEAN CLUSTER : ", "Cluster label " + cluster.getLabel() + " : Points " + cluster.getPoints().size() + " : Centroid " + cluster.getCentroid());
                    Imgproc.putText(source, String.valueOf(cluster.getLabel()), cluster.getCentroid(), Core.FONT_HERSHEY_COMPLEX, 2, scalars[cluster.getLabel()], 4);
                }
            });
        }
    }
*/

    private void findNodes() {
        Mat cIMG = processedImage, dst = source;
        List<MatOfPoint> matOfPoints = new ArrayList<MatOfPoint>();
        Mat hovIMG = new Mat();
        Imgproc.findContours(cIMG, matOfPoints, hovIMG, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        //Imgproc.drawContours(dst, matOfPoints, -1, new Scalar(255,0,0), 3);
        MatOfPoint ldmSurfacePoint = null;
        double surfaceArea = 0, t = 0;
        for(MatOfPoint pt : matOfPoints){
            t = Imgproc.contourArea(pt);
            if(surfaceArea<t){
                surfaceArea = t;
                ldmSurfacePoint = pt;
            }
        }
        Rect ldmSurface = Imgproc.boundingRect(ldmSurfacePoint);
        for (MatOfPoint point : matOfPoints) {
            //      Imgproc.drawContours(dst, Collections.singletonList(point), -1,  new Scalar((random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255), 3);
            MatOfPoint2f curvePoint = new MatOfPoint2f(point.toArray());
            Imgproc.approxPolyDP(curvePoint, approximateCurvePoint, EPSILON_C * Imgproc.arcLength(curvePoint, true), true);
            int numberVertices = (int) approximateCurvePoint.total();
            if (numberVertices < 4)
                continue;

          /*   //Triangle
            if (numberVertices == 3) {
                labelDetectedRegion(dst, point, TRIANGLE_LABEL, new Scalar(255, 255, 0));
            }*/

            // Rectangle, Square, And Polygon. Circle is a polygon with high number of vertices.

            Rect rect = Imgproc.boundingRect(point);

            double contourArea = Imgproc.contourArea(point);

            /*
             * ELIMINATE ALL THE FALSE DETECTIONS AND SMALL SHAPES
             * Value should be changed based on the source image resolution
             */
            Log.d("IP", contourArea + " " + numberVertices);

            if (Math.abs(contourArea) < MIN_CONTOUR_AREA || point == ldmSurfacePoint) {
                continue;
            }

            if (numberVertices == 4) {

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

                if (contourArea > (MIN_CONTOUR_AREA_CIRCLE / 3) && mincos >= -0.3 && maxcos <= 0.3) {
                    if (s >= SQUARE_MIN_RATIO && s <= SQUARE_MAX_RATIO) {
                        labelDetectedRegion(dst, point, "SQ", new Scalar(128, 128, 0),false);
                    } else {
                        labelDetectedRegion(dst, point, RECTANGLE_LABEL, new Scalar(255, 0, 0),false);
                    }
                }
            }
            //Possibly a circle. A circle is a polygon with high number of vertices
            if (numberVertices > 4) {
                double perimeter = Imgproc.arcLength(new MatOfPoint2f(point.toArray()), true);
                double circularity = 4 * 3.14 * (contourArea / (perimeter * perimeter));
                Log.d("IP C", String.valueOf(circularity) + " " + contourArea);
                if (perimeter!=0 && (MIN_CIRCULARITY_FACTOR < circularity && circularity < MAX_CIRCULARITY_FACTOR) && (contourArea > MIN_CONTOUR_AREA_CIRCLE)) {
                    labelDetectedRegion(dst, point, "CIRCLE" + numberVertices + " " + contourArea, new Scalar(255, 0, 0), false);
                }
            }

        }

    }

    public Bitmap getCroppedImage(Bitmap source, double tolerance) {
        // Get our top-left pixel color as our "baseline" for cropping
        int baseColor = Color.BLACK;

        int width = source.getWidth();
        int height = source.getHeight();

        int topY = Integer.MAX_VALUE, topX = Integer.MAX_VALUE;
        int bottomY = -1, bottomX = -1;
        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                if (colorWithinTolerance(baseColor, source.getPixel(x, y), tolerance)) {
                    if (x < topX) topX = x;
                    if (y < topY) topY = y;
                    if (x > bottomX) bottomX = x;
                    if (y > bottomY) bottomY = y;
                }
            }
        }
        topX-=10;
        topY-=10;
        width = (bottomX-topX+1)+10;
        height = (bottomY-topY+1)+10;
        try {
            if (width > 0 && height > 0) {
                return Bitmap.createBitmap(source, topX, topY, width, height);
            }
        }catch (Exception e){
            return source;
        }
        return source;
    }

    private boolean colorWithinTolerance(int a, int b, double tolerance) {
        int aAlpha  = (int)((a & 0xFF000000) >>> 24);   // Alpha level
        int aRed    = (int)((a & 0x00FF0000) >>> 16);   // Red level
        int aGreen  = (int)((a & 0x0000FF00) >>> 8);    // Green level
        int aBlue   = (int)(a & 0x000000FF);            // Blue level

        int bAlpha  = (int)((b & 0xFF000000) >>> 24);   // Alpha level
        int bRed    = (int)((b & 0x00FF0000) >>> 16);   // Red level
        int bGreen  = (int)((b & 0x0000FF00) >>> 8);    // Green level
        int bBlue   = (int)(b & 0x000000FF);            // Blue level

        double distance = Math.sqrt((aAlpha-bAlpha)*(aAlpha-bAlpha) +
                (aRed-bRed)*(aRed-bRed) +
                (aGreen-bGreen)*(aGreen-bGreen) +
                (aBlue-bBlue)*(aBlue-bBlue));

        // 510.0 is the maximum distance between two colors
        // (0,0,0,0 -> 255,255,255,255)
        double percentAway = distance / 510.0d;

        return (percentAway > tolerance);
    }


    int j = 0;

    private void labelDetectedRegion(Mat dst, MatOfPoint cnt, String label, Scalar scalar, boolean skipLabel) {
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 1;
        int thickness = 4;
        int[] baseline = new int[1];
        Rect rect = Imgproc.boundingRect(cnt);
//        Imgproc.drawContours(binary, Collections.singletonList(cnt), -1, new Scalar(0, 0, 0), 5);
        //Crop for OCR
        Mat mat;
        try {
            OCR_BOUND_RECT = (int) (getSourceRatio() * 34.632034632);
            mat = binary.submat(rect.y + OCR_BOUND_RECT, rect.y - OCR_BOUND_RECT + rect.height, rect.x + OCR_BOUND_RECT, rect.x - OCR_BOUND_RECT + rect.width);
        } catch (Exception e) {
            OCR_BOUND_RECT = 0;
            mat = binary.submat(rect.y + OCR_BOUND_RECT, rect.y - OCR_BOUND_RECT + rect.height, rect.x + OCR_BOUND_RECT, rect.x - OCR_BOUND_RECT + rect.width);
        }
        Mat t = new Mat();
        Core.bitwise_not(mat, t);
//        Core.copyMakeBorder(mat, t, (int)0.1*mat.rows(),(int)0.1*mat.rows(), (int)0.1*mat.cols(), (int)0.1* mat.cols(), Core.BORDER_CONSTANT,  new Scalar(0,0,0));
        mat = t;
        if (!skipLabel) {
            Bitmap image = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, image);
            saveImage(mat, "ocr" + j++);
            //Passing to OCR Processor for the label extraction
            //Bitmap b = getCroppedImage(image,0);
            //saveImage(b, "bocr"+j);
            label = ocr.extractText(image);
            if (labels.containsKey(label)) {
                int i = labels.get(label);
                i++;
                labels.put(label, i);
                label += String.valueOf(i);
            } else {
                labels.put(label, 0);
            }
        }

        if (!skipLabel) {
            Size text = Imgproc.getTextSize(label, fontface, scale, thickness, baseline);
            Point pt = new Point(rect.x + ((rect.width - text.width) / 2), rect.y + ((rect.height + text.height) / 2));
            Imgproc.putText(dst, label, pt, fontface, scale, scalar, thickness);
            Imgproc.rectangle(dst, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), scalar, thickness);
            circles.add(new Circle(rect, label, new Point((rect.x + rect.x + rect.width) / 2, (rect.y + rect.y + rect.height) / 2)));
        } else {
            ords.add(new Circle(rect, label, null));
        }
    }


    private void findRelationships() {
        // Line Detection̵
        Mat covered = processedImage.clone();
        //coveredS = dilatedImage.clone();
        for (Circle circle : circles) {
            Imgproc.rectangle(covered, new Point(circle.rect.x, circle.rect.y), new Point(circle.rect.x + circle.rect.width, circle.rect.y + circle.rect.height), new Scalar(0, 0, 0), -1);
            Imgproc.rectangle(coveredS, new Point(circle.rect.x - bound, circle.rect.y - bound), new Point(circle.rect.x + circle.rect.width + bound, circle.rect.y + circle.rect.height + bound), new Scalar(0, 0, 0), -1);
        }
        saveImage(covered, "8covered");
        saveImage(coveredS, "8coveredS");
        Mat linesP = new Mat();
        Imgproc.HoughLinesP(covered, linesP, RHO_FOR_LINE_DETECTION, THETA_FOR_LINE_DETECTION, LINE_THRESHOLD, MIN_LINE_LENGTH, MAX_LINE_GAP);
        //Find all lines
        Log.d("IP L", "Lines : " + String.valueOf(linesP.rows()));
        for (int i = 0; i < linesP.rows(); i++) {
            double[] j = linesP.get(i, 0);
            Point pt1 = new Point(j[0], j[1]);
            Point pt2 = new Point(j[2], j[3]);
            this.lines.add(new Line(pt1, pt2));
            findConnectingCircle(i, pt1, pt2);
            //Imgproc.line(source, pt1, pt2, new Scalar((random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255), 3, Imgproc.LINE_AA, 0);
        }
    }

    private void findSmallLines(Mat coveredS) {
        //source = new Mat(source.rows(),source.cols(),source.type());
        Mat linesS = new Mat();
        Imgproc.createLineSegmentDetector().detect(coveredS, linesS);
        //Find all lines
        Log.d("IP L", "Lines : " + String.valueOf(linesS.rows()));
        for (int i = 0; i < linesS.rows(); i++) {
            double[] j = linesS.get(i, 0);
            Point pt1 = new Point(j[0], j[1]);
            Point pt2 = new Point(j[2], j[3]);
            double distance = distance(pt1, pt2);
            if (distance <= SMALL_LINE_MAX_DIST) {
                this.sLines.add(new Line(pt1, pt2));
                //     Imgproc.line(source, pt1, pt2, new Scalar((random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255), 2, Imgproc.LINE_AA, 0);
            }
        }
        Map<String, Cluster> clusterMap = new HashMap<>();
        for (Line line : edgeLines) {
            double distance = distance(line.pt1, line.pt2);
            if (!clusterMap.containsKey(line.getPt1().toString())) {
                clusterMap.put(line.getPt1().toString(), new Cluster(new ArrayList<Point>(), line.getPt1().toString()));
                clusterMap.get(line.getPt1().toString()).setCentroid(line.getPt1());
            }
            if (!clusterMap.containsKey(line.getPt2().toString())) {
                clusterMap.put(line.getPt2().toString(), new Cluster(new ArrayList<Point>(), line.getPt2().toString()));
                clusterMap.get(line.getPt2().toString()).setCentroid(line.getPt2());
            }
            Iterator<Line> iterator = sLines.iterator();
            while (iterator.hasNext()) {
                Line sline = iterator.next();
                if ((distance(line.pt1, sline.pt1) <= 0.2 * distance) && distance(line.pt1, sline.pt2) <= 0.2 * distance) {
                    double angle1 = tAngle(sline.pt1, line.pt2, line.pt1);
                    double angle2 = tAngle(sline.pt2, line.pt2, line.pt1);
                    if (checkAngle(angle1, angle2)) {
                        clusterMap.get(line.getPt1().toString()).getPoints().add(sline.pt1);
                        clusterMap.get(line.getPt1().toString()).getPoints().add(sline.pt2);
                        iterator.remove();
                    }
                } else if ((distance(line.pt2, sline.pt1) <= 0.2 * distance) && distance(line.pt2, sline.pt2) <= 0.2 * distance) {
                    double angle1 = tAngle(sline.pt1, line.pt1, line.pt2);
                    double angle2 = tAngle(sline.pt2, line.pt1, line.pt2);
                    if (checkAngle(angle1, angle2)) {
                        clusterMap.get(line.getPt2().toString()).getPoints().add(sline.pt1);
                        clusterMap.get(line.getPt2().toString()).getPoints().add(sline.pt2);
                        iterator.remove();
                    }
                }
            }
        }
        final int[] i = new int[1];
        i[0] = 0;
        final Set<Integer> clusterPoints = new TreeSet<>();
        clusterMap.forEach(new BiConsumer<String, Cluster>() {
            @Override
            public void accept(String s, Cluster cluster) {
                Log.i("CLUSTER : ", "Cluster label " + cluster.getLabel() + " : Points " + cluster.getPoints().size() + " : Centroid " + cluster.getCentroid());
                clusterPoints.add(cluster.getPoints().size());
                Imgproc.putText(source, String.valueOf(cluster.getLabel()), cluster.getCentroid(), Core.FONT_HERSHEY_COMPLEX, 1, scalars[i[0] % 10], 4);
                for (Point point : cluster.getPoints()) {
                    Imgproc.drawMarker(source, point, scalars[i[0] % 10], Imgproc.MARKER_CROSS, 10, 1, 8);
                }
                i[0]++;
            }
        });
        int sum = 0;
        for (Integer integer : clusterPoints) {
            sum += integer;
        }

        if (clusterPoints.size() > 0)
            ordinalMean = sum / clusterPoints.size();
        else {
            ordinalMean = 0;
        }

//        ordinalMean = (int) clusterPoints.toArray()[clusterPoints.size()/2];

        connectedCircles.clear();
        for (Line line : edgeLines) {
            String A = line.nodeAtP1;
            String B = line.nodeAtP2;
            line.setOrdinalityAtP1(getOrdinality(clusterMap.get(line.getPt1().toString()).getPoints().size()));
            line.setOrdinalityAtP2(getOrdinality(clusterMap.get(line.getPt2().toString()).getPoints().size()));
            A += getOrdinalityCharacter(line.getOrdinalityAtP1());
            B += getOrdinalityCharacter(line.getOrdinalityAtP2());
            connectedCircles.put(A + "," + B, line.getPt1().toString() + "," + line.getPt2().toString());
        }
    }

    private String getOrdinality(int clusterSize) {
        if (ordinalMean != 0 && clusterSize >= 10 && clusterSize >= ordinalMean) {
            return "many";
        }
        return "one";
    }

    private String getOrdinalityCharacter(String ordinality) {
        return ordinality.equals("many") ? "\"" : "'";
    }

    private boolean checkAngle(double angle1, double angle2) {
        return ((30 <= angle1 && angle1 <= 60) || (330 <= angle1 && angle1 <= 360)) || ((30 <= angle2 && angle2 <= 60) || (330 <= angle2 && angle2 <= 360));
    }

    public static class Cluster {
        private List<org.opencv.core.Point> points;
        private org.opencv.core.Point centroid;
        private String label;
        private String ordinality;

        public Cluster(List<org.opencv.core.Point> points, String label) {
            this.points = points;
            this.label = label;
        }

        public List<org.opencv.core.Point> getPoints() {
            return points;
        }

        public String getLabel() {
            return label;
        }

        public org.opencv.core.Point getCentroid() {
            return centroid;
        }

        public void setCentroid(org.opencv.core.Point centroid) {
            this.centroid = centroid;
        }

        public void setOrdinality(String ordinality) {
            this.ordinality = ordinality;
        }

        public String getOrdinality() {
            return ordinality;
        }
    }

    private double distance(Point pt1, Point pt2) {
        double x1 = pt1.x;
        double y1 = pt1.y;
        double x2 = pt2.x;
        double y2 = pt2.y;
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private void findConnectingCircle(int i, Point pt1, Point pt2) {
        double x1 = pt1.x;
        double y1 = pt1.y;
        double x2 = pt2.x;
        double y2 = pt2.y;
        double dis = distance(pt1, pt2);
        boolean start = false, end = false;
        String labelA = "", labelB = "";
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
            if (circle1 == circle2) // Filter false detection
                return;
            Log.d("IP Line, x1,y1 x2,y2 ", String.format("%d, %f,%f %f,%f D:%f", i, x1, y1, x2, y2, dis) + " " + (start && end) + " L:" + labelA + " " + labelB);
            // String A = labelA + findOrdinalityAt(pt1, pt2, dis, circle1), B = labelB + findOrdinalityAt(pt2, pt1, dis, circle2);
            String A = labelA, B = labelB;
            if (!(connectedCircles.containsKey(B + "," + A) || connectedCircles.containsKey(A + "," + B))) { // Filter false detection
                connectedCircles.put(A + "," + B, labelB);
                Imgproc.line(source, pt1, pt2, new Scalar((random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255), 4, Imgproc.LINE_AA, 0);
                edges++;
                edgeLines.add(new Line(pt1, pt2).setNodeAtP1(A).setNodeAtP2(B));
                edgePoints.add(pt1);
                edgePoints.add(pt2);
            }
        }

        // Imgproc.line(source, pt1, pt2, new Scalar((random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255, (random.nextInt() * 100) % 255), 3, Imgproc.LINE_AA, 0);
    }

//
//    private String findOrdinalityAt(Point startPoint, Point endPoint, double distance, Circle lcircle) {
//        for (Circle circle : ords) {
//            if (!((lcircle.rect.x <= circle.rect.x && circle.rect.x <= (lcircle.rect.x + lcircle.rect.width)) &&
//                    (lcircle.rect.y <= circle.rect.y && circle.rect.y <= (lcircle.rect.y + lcircle.rect.height)))) {
//                Point contourPoint = new Point(circle.rect.x, circle.rect.y);
//                if (distance(startPoint, contourPoint) <= 0.3 * distance)
//                    Log.d("ORDIN DIST ", distance(startPoint, contourPoint) + " " + distance(endPoint, contourPoint) + " " + distance);
//                if (distance(startPoint, contourPoint) <= (0.2 * distance)
//                        && distance(contourPoint, endPoint) <= (distance)
//                        && (distance(startPoint, contourPoint) + distance(contourPoint, endPoint) <= (distance + (0.2 * distance)))) {
//                    double angle = tAngle(contourPoint, endPoint, startPoint);
//
//                    if ((0 <= angle && angle <= 60) || (300 <= angle && angle <= 360)) {
//                        Log.d("ORDIN ANG ", String.valueOf(angle) + " TRUE");
//                        return "\"";
//                    } else {
//                        Log.d("ORDIN ANG ", String.valueOf(angle));
//                    }
//                }
//            }
//        }
//        return "'";
//    }


    // finds a cos angle between vectors
    private double angle(Point point1, Point point2, Point point0) {
        double dx1 = point1.x - point0.x;
        double dy1 = point1.y - point0.y;
        double dx2 = point2.x - point0.x;
        double dy2 = point2.y - point0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    private double tAngle(Point point1, Point point2, Point point0) {
        double angle1 = Math.atan2(point1.y - point0.y, point1.x - point0.x);
        double angle2 = Math.atan2(point2.y - point0.y, point2.x - point0.x);
        double theta = Math.toDegrees(angle1 - angle2);

        if (theta < 0.0) {
            theta += 360.0;
        }

        return theta;
    }


}
