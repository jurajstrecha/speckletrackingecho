package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Juraj Strecha, xstrec01
 */
public final class TrackedShape {
    private ArrayList<Point> controlPoints;
    private ArrayList<Point> splinePoints;
    
    public TrackedShape() {
        controlPoints = new ArrayList<Point>();
    }
       
    public ArrayList<Point> getControlPoints() {
        return controlPoints;
    }
       
    public ArrayList<Point> getSplinePoints() {
        return splinePoints;
    }
    
    public void calcSplinePoints() {
        if (controlPoints.size() > 2) {
            splinePoints = CatmullRom.calculateSpline(controlPoints);
        }
    }
}
