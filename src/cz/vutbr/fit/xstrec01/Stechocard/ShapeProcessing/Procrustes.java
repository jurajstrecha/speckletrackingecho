package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Bc. Juraj Strecha
 */
public final class Procrustes {
    public static void analyse(ArrayList<ArrayList<Point>> shapes) {
        translate(shapes);
        scale(shapes);
        rotate(shapes);
    }
    
    static void translate(ArrayList<ArrayList<Point>> shapes) {
        if (shapes != null && shapes.size() > 1) {
            // centroid of the mean shape will become the center of alignment
            Point centroid = calculateShapeCentroid(shapes.get(0));
            System.out.println("Center: " + centroid.x + ", " + centroid.y);
        }        
    }
    
    static void scale(ArrayList<ArrayList<Point>> shapes) {
        
    }
    
    static void rotate(ArrayList<ArrayList<Point>> shapes) {
        
    }
    
    static Point calculateShapeCentroid(ArrayList<Point> shape) {
        int x = 0, y = 0, numOfPts;
        
        numOfPts = shape.size();
        for (Point point: shape) {
            x += point.x;
            y += point.y;
        }
        return new Point((int)(x / numOfPts), (int)(y / numOfPts));
    }
    
    /**
     * Shifts a shape to the 0, 0 centroid.
     * 
     * @param centroid
     * @param shape 
     */
    static void center(Point centroid, ArrayList<Point> shape) {
        
    }
}
