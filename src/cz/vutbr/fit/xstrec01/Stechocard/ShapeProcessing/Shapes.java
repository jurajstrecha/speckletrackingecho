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
            ArrayList<Point> a = new ArrayList<Point>();
            // get some amount (defined in Constants) of points from spline
            return a;
        }
        return null;
    }
    
    public void addShape() {
        if (memory != null) {
            // TODO
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
