package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import java.awt.Point;
import java.util.ArrayList;

/**
 * Data structure holding points representing user annotated shapes.
 * Can select Constants.SPLINE_DIV_INTERVALS + 1 points from shape in spline form.
 * Stores serialized shapes onto the hard drive.
 * 
 * @author Bc. Juraj Strecha, xstrec01@stud.fit.vutbr.cz
 */
public class Shapes {

    private final ArrayList<Point> buffer;
    private final ArrayList<ArrayList<Point>> memory;
    
    public Shapes() {
        super();
        buffer = new ArrayList<Point>();
        memory = new ArrayList<ArrayList<Point>>();
    }
    
    /**
     * Create a set of points from user annotated points using spline
     * representation and spline interval division and point selection.
     * The reduced points are stored as a shape into object memory.
     * 
     * @param shape Points provided by the user
     * @return true if the shape was stored, false otherwise
     */
    public boolean serializeShape(ArrayList<Point> shape) {
        ArrayList<Point> spline;
        ArrayList<Point> mainPoints;
        
        if (memory == null) {
            return false;
        }

        spline = CatmullRom.calculateSpline(shape);
        if (spline == null) {
            return false;
        }

        mainPoints = divideSpline(spline, Constants.SPLINE_DIV_INTERVALS);
        if (mainPoints == null) {
            return false;
        }
        
        memory.add(mainPoints);
        return true;
    }
    
    /**
     * Stores serialized shape as a file on the hard drive, lets the user
     * select file name and destination by displaying a save dialog.
     * 
     * @return true if stored successfully, false otherwise
     */
    public boolean saveShapes() {
        if (memory == null || memory.isEmpty()) {
            return false;
        }
        
        // take all serialized shapes and store them
        // on the disk using user dialog
        
        return true;
    }
    
    /**
     * Selects points from the spline, selects them evenly using
     * intervals parameter.
     * 
     * @param spline Spline shape points
     * @param intervals Number of intervals to divide shape to
     * @return Selected points in count of intervals + 1
     */
    public static ArrayList<Point> divideSpline(ArrayList<Point> spline, int intervals) {
        if (spline != null && spline.size() > 1) {
            ArrayList<Point> mainPoints = new ArrayList<Point>();
            
            // calculate spline length
            double splineLen = 0;            
            for(int i = 1; i < spline.size() - 1; i++) {
                splineLen += getDistance(spline.get(i - 1), spline.get(i));
            }           
            
            // 
            mainPoints.add(spline.get(0));
            double increment = splineLen / Constants.SPLINE_DIV_INTERVALS;
            double border = increment;
            double currLen = 0.0;
            int counter = 1;
            double dist;
            while (border <= splineLen && counter < spline.size()) {
                dist = getDistance(spline.get(counter - 1), spline.get(counter));
                currLen += dist;
                if (currLen >= border) {
                    mainPoints.add(spline.get(counter));
                    border += increment;
                }
                counter++;
            }
            
            // make rounding error correction compating spline length and current length
            if (mainPoints.size() < Constants.SPLINE_DIV_INTERVALS + 1) {
                mainPoints.add(spline.get(spline.size() - 1));
            }
                       
            return mainPoints;
        }
        return null;
    }
    
    public static double getDistance(Point a, Point b) {
        double vecXLen = a.getX() - b.getX();
        double vecYLen = a.getY() - b.getY();
        // Euclidean distance
        return Math.sqrt(vecXLen * vecXLen + vecYLen * vecYLen);
    }
    
    public void addShape(ArrayList<Point> shape) {
        if (memory != null) {
            memory.add(shape);
        }
    }

    /**
     * Insert annotated point into the current shape buffer.
     * 
     * @param p Point to be inserted
     */
    public void addPoint(Point p) {
        if (buffer != null) {
            buffer.add(p);
        }
    }
}
