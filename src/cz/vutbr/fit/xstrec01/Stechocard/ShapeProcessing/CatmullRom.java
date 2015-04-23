package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import java.awt.Point;
import java.util.ArrayList;

/**
 * Set of static methods providing Catmull-Rom interpolation spline points enumeration.
 * Source of inspiration: http://www.booncotter.com/waypoints-catmull-rom-splines/
 * 
 * @author Juraj Strecha, duri.strecha@gmail.com
 * @version 1.0
 */
public class CatmullRom {
    
    /**
     * Enumerates interpolation spline points using a set of control points,
     * samples per span count and Catmull-Rom polynom equations.
     * 
     * @param controlPoints Set of control points that will be interpolated by the spline
     * @return Resulting set of spline points
     */
    public static ArrayList<Point> calculateSpline(ArrayList<Point> controlPoints) {
        if (controlPoints != null && controlPoints.size() > 1) {
            ArrayList<Point> splinePoints = new ArrayList<Point>();            
            int samples = Constants.SPLINE_SAMPLES_PER_SPAN;            
            double deltaT = 1.0 / samples;
            double t;
            
            // copy start and end point at the beginning and the end of the set
            // to include the first and last control point in spline
            extendCtrlPointsSet(controlPoints);
            
            for (int i = 1; i < controlPoints.size() - 2; i++) {
                for (int j = 0; j < Constants.SPLINE_SAMPLES_PER_SPAN; j++) {
                    t = deltaT * j;
                    splinePoints.add(CREquation(controlPoints.get(i - 1),
                                                controlPoints.get(i    ),
                                                controlPoints.get(i + 1),
                                                controlPoints.get(i + 2),
                                                t));
                }
            }
            
            splinePoints.add(getLastPoint(controlPoints));
            
            removeExtendedCtrlPoints(controlPoints);
            
            return splinePoints;
        }
        return null;
    }
    
    /**
     * Enumerates a new point for the interpolation spline according to the
     * t-parameter using polynomial formulas.
     * 
     * @param p1 First control point - affects a shape of the spline
     * @param p2 Second control point - start of the interval
     * @param p3 Third control point - end of the interval
     * @param p4 Fourth control point - affects a shape of the spline
     * @param t Relative t-parameter inside the interval
     * @return Point from the resulting spline
     */
    public static Point CREquation(Point p1, Point p2, Point p3, Point p4, double t) {       
        double t2 = t * t;
        double t3 = t2 * t;
        
        double b1 = 0.5f * (  -t3 + 2*t2 - t);
        double b2 = 0.5f * ( 3*t3 - 5*t2 + 2);
        double b3 = 0.5f * (-3*t3 + 4*t2 + t);
        double b4 = 0.5f * (   t3 -   t2    );
               
        double x = b1*p1.getX() + b2*p2.getX() + b3*p3.getX() + b4*p4.getX();
        double y = b1*p1.getY() + b2*p2.getY() + b3*p3.getY() + b4*p4.getY();
        
        Point result = new Point();
        result.setLocation(x, y);
        
        return result;
    }
    
    public static double getSplineLength(ArrayList<Point> spline) {
        double length = 0.0;
        Point recentPoint = getFirstPoint(spline);
        Point currentPoint;
        double xLen, xLen2, yLen, yLen2;
        for (int i = 1; i < spline.size(); i++) {
            currentPoint = spline.get(i);
            xLen = recentPoint.getX() - currentPoint.getX();
            xLen2 = xLen * xLen;
            yLen = recentPoint.getY() - currentPoint.getY();
            yLen2 = yLen * yLen;
            length += Math.sqrt(xLen2 + yLen2);
            recentPoint = currentPoint;
        }
        
        return length;
    }
    
    /**
     * Copies the first/last point at the start/end of the set.
     * 
     * @param points Set of points to be extended by the first and last item
     */
    public static void extendCtrlPointsSet(ArrayList<Point> points) {
        if (points != null && points.size() > 1) {
            points.add(0, getFirstPoint(points));
            points.add(getLastPoint(points));
        }
    }
    
    public static void removeExtendedCtrlPoints(ArrayList<Point> points) {
        if (points != null) { 
            int pointsCnt = points.size();
            if (pointsCnt > 3) {
                points.remove(0);
                points.remove(pointsCnt - 2);
            }
        }
    }
    
    /**
     * Returns the first point in the set
     * 
     * @param points Set of points
     * @return First point in the set
     */
    public static Point getFirstPoint(ArrayList<Point> points) {
        if (points != null) {
            return points.get(0);
        }
        return null;
    }
    
    /**
     * Returns the last point in the set.
     * 
     * @param points Set of points
     * @return Last point in the set
     */
    public static Point getLastPoint(ArrayList<Point> points) {
        if (points != null) {
            return points.get(points.size() - 1);
        }
        return null;
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
}
