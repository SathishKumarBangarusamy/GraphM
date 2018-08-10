package com.sathish.bs.graphm.processor;
/** Class for kmeans clustering
 * created by Keke Chen (keke.chen@wright.edu)
 * For Cloud Computing Labs
 * Feb. 2014
 */

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KMeans
{
    // Data members
    private double [][] _data; // Array of all records in dataset
    private int [] _label;  // generated cluster labels
    private int [] _withLabel; // if original labels exist, load them to _withLabel
    // by comparing _label and _withLabel, we can compute accuracy.
    // However, the accuracy function is not defined yet.
    private double [][] _centroids; // centroids: the center of clusters
    private int _nrows, _ndims; // the number of rows and dimensions
    private int _numClusters; // the number of clusters;
    private List<ImageProcessor.Line> lines;
    // Constructor; loads records from file <fileName>.
    // if labels do not exist, set labelname to null
    public KMeans(List<ImageProcessor.Line> lines)
    {
        this.lines = lines;
        // Creates a new KMeans object by reading in all of the records that are stored in a csv file
        try
        {
            // get the number of rows
            _nrows = 2*lines.size();
            _ndims = 2;
            System.out.println(_nrows + " "+_ndims);

            // initialize the _data variable
            _data = new double[_nrows][];
            for (int i=0; i<_nrows; i++)
                _data[i] = new double[_ndims];

            // read records from the csv file
            int nrow=0;
            for(ImageProcessor.Line line : lines){
                double [] dv = new double[_ndims];
                dv[0] = line.getPt1().x;
                dv[1] = line.getPt1().y;
                _data[nrow] = dv;
                nrow ++;
                dv = new double[_ndims];
                dv[0] = line.getPt2().x;
                dv[1] = line.getPt2().y;
                _data[nrow] = dv;
                nrow ++;
            }
            System.out.println("loaded data");

//            if (labelname!=null){
//                // load label file to _withLabel;
//                reader = new BufferedReader(new FileReader(labelname));
//                _withLabel = new int[_nrows];
//                int c=0;
//                while ((values = csv.parseLine(reader))!=null){
//                    _withLabel[c] = Integer.parseInt(values.get(0));
//                }
//                reader.close();
//                System.out.println("loaded labels");
//            }
        }
        catch(Exception e)
        {
            System.out.println( e );
            System.exit( 0 );
        }

    }

    // Perform k-means clustering with the specified number of clusters and
    // Eucliden distance metric.
    // niter is the maximum number of iterations. If it is set to -1, the kmeans iteration is only terminated by the convergence condition.
    // centroids are the initial centroids. It is optional. If set to null, the initial centroids will be generated randomly.
    public void clustering(int numClusters, int niter, double [][] centroids)
    {
        _numClusters = numClusters;
        if (centroids !=null)
            _centroids = centroids;
        else{
            // randomly selected centroids
            _centroids = new double[_numClusters][];

            ArrayList idx= new ArrayList();
            for (int i=0; i<numClusters; i++){
                int c;
                do{
                    c = (int) (Math.random()*_nrows);
                }while(idx.contains(c)); // avoid duplicates
                idx.add(c);

                // copy the value from _data[c]
                _centroids[i] = new double[_ndims];
                for (int j=0; j<_ndims; j++)
                    _centroids[i][j] = _data[c][j];
            }
            System.out.println("selected random centroids");

        }

        double [][] c1 = _centroids;
        double threshold = 0.001;
        int round=0;

        while (true){
            // update _centroids with the last round results
            _centroids = c1;

            //assign record to the closest centroid
            _label = new int[_nrows];
            for (int i=0; i<_nrows; i++){
                _label[i] = closest(_data[i]);
            }

            // recompute centroids based on the assignments
            c1 = updateCentroids();
            round ++;
            if ((niter >0 && round >=niter) || converge(_centroids, c1, threshold))
                break;
        }

        System.out.println("Clustering converges at round " + round);
    }

    // find the closest centroid for the record v
    private int closest(double [] v){
        double mindist = dist(v, _centroids[0]);
        int label =0;
        for (int i=1; i<_numClusters; i++){
            double t = dist(v, _centroids[i]);
            if (mindist>t){
                mindist = t;
                label = i;
            }
        }
        return label;
    }

    // compute Euclidean distance between two vectors v1 and v2
    private double dist(double [] v1, double [] v2){
        double sum=0;
        for (int i=0; i<_ndims; i++){
            double d = v1[i]-v2[i];
            sum += d*d;
        }
        return Math.sqrt(sum);
    }

    // according to the cluster labels, recompute the centroids
    // the centroid is updated by averaging its members in the cluster.
    // this only applies to Euclidean distance as the similarity measure.

    private double [][] updateCentroids(){
        // initialize centroids and set to 0
        double [][] newc = new double [_numClusters][]; //new centroids
        int [] counts = new int[_numClusters]; // sizes of the clusters

        // intialize
        for (int i=0; i<_numClusters; i++){
            counts[i] =0;
            newc[i] = new double [_ndims];
            for (int j=0; j<_ndims; j++)
                newc[i][j] =0;
        }


        for (int i=0; i<_nrows; i++){
            int cn = _label[i]; // the cluster membership id for record i
            for (int j=0; j<_ndims; j++){
                newc[cn][j] += _data[i][j]; // update that centroid by adding the member data record
            }
            counts[cn]++;
        }

        // finally get the average
        for (int i=0; i< _numClusters; i++){
            for (int j=0; j<_ndims; j++){
                newc[i][j]/= counts[i];
            }
        }

        return newc;
    }

    // check convergence condition
    // max{dist(c1[i], c2[i]), i=1..numClusters < threshold
    private boolean converge(double [][] c1, double [][] c2, double threshold){
        // c1 and c2 are two sets of centroids
        double maxv = 0;
        for (int i=0; i< _numClusters; i++){
            double d= dist(c1[i], c2[i]);
            if (maxv<d)
                maxv = d;
        }

        if (maxv <threshold)
            return true;
        else
            return false;

    }
    public double[][] getCentroids()
    {
        return _centroids;
    }

    public int [] getLabel()
    {
        return _label;
    }

    public int nrows(){
        return _nrows;
    }

    public KMeansResult getResults(){

        Map<Integer,Cluster> clusterMap = new HashMap<>();
        List<Point> points = new ArrayList<>();
        for (int i=0; i<_nrows; i+=2){
            lines.get(i/2).setLabel1(_label[i]);
            lines.get(i/2).setLabel2(_label[i+1]);
            org.opencv.core.Point point1 = lines.get(i/2).getPt1();
            org.opencv.core.Point point2 = lines.get(i/2).getPt2();
            points.add(new Point(point1, _label[i]));
            points.add(new Point(point2, _label[i+1]));

            if(!clusterMap.containsKey(_label[i])){
                clusterMap.put(_label[i], new Cluster(new ArrayList<org.opencv.core.Point>(),_label[i]));
            }

            if(!clusterMap.containsKey(_label[i+1])){
                clusterMap.put(_label[i+1], new Cluster(new ArrayList<org.opencv.core.Point>(),_label[i+1]));
            }

            clusterMap.get(_label[i]).getPoints().add(point1);
            clusterMap.get(_label[i+1]).getPoints().add(point2);
        }
        List<org.opencv.core.Point> centroids = new ArrayList<>();

        for (int i=0; i<_numClusters; i++){
            org.opencv.core.Point point = new org.opencv.core.Point(_centroids[i][0],_centroids[i][1]);
            centroids.add(point);
            clusterMap.get(i).setCentroid(point);
        }

        return new KMeansResult(lines,points,centroids,clusterMap);

    }


    public static class Point{
        private org.opencv.core.Point point;
        private int label;

        public Point(org.opencv.core.Point point, int label) {
            this.point = point;
            this.label = label;
        }

        public org.opencv.core.Point getPoint() {
            return point;
        }

        public int getLabel() {
            return label;
        }
    }

    public static class Cluster{
        private List<org.opencv.core.Point> points;
        private org.opencv.core.Point centroid;
        private int label;

        public Cluster(List<org.opencv.core.Point> points, int label) {
            this.points = points;
            this.label = label;
        }

        public List<org.opencv.core.Point> getPoints() {
            return points;
        }

        public int getLabel() {
            return label;
        }

        public org.opencv.core.Point getCentroid() {
            return centroid;
        }

        public void setCentroid(org.opencv.core.Point centroid) {
            this.centroid = centroid;
        }
    }

    public static class KMeansResult{
        private List<ImageProcessor.Line> lines;
        private List<Point> points;
        private List<org.opencv.core.Point> centroids;
        private Map<Integer, Cluster> clusterMap;

        public KMeansResult(List<ImageProcessor.Line> lines, List<Point> points, List<org.opencv.core.Point> centroids, Map<Integer, Cluster> clusterMap) {
            this.lines = lines;
            this.points = points;
            this.centroids = centroids;
            this.clusterMap = clusterMap;
        }

        public List<org.opencv.core.Point> getCentroids() {
            return centroids;
        }

        public List<Point> getPoints() {
            return points;
        }

        public List<ImageProcessor.Line> getLines() {
            return lines;
        }

        public Map<Integer, Cluster> getClusterMap() {
            return clusterMap;
        }
    }

}