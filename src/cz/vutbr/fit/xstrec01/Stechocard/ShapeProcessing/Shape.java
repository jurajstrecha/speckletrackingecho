package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import java.util.ArrayList;
import java.awt.Point;

/**
 * Reprezentuje tvar vyznačený užívateľom. Obsahuje body, ktoré užívateľ naklikal
 * a tiež body, ktoré boli vypočítané zo splajnu. Tie budú použité pre výpočet
 * modelu.
 * 
 * @author Juraj Strecha, xstrec01
 */
public final class Shape {
    ArrayList<Point> annotatedPoints;
    ArrayList<Point> splinePoints;
    
    public Shape() {
        annotatedPoints = new ArrayList<Point>();
        splinePoints = new ArrayList<Point>();
    }
    
    public void addAnnotatedPoints(ArrayList<Point> pts) {
        annotatedPoints.addAll(pts);
    }    
    
    public void addAnnotatedPoint(Point pt) {
        annotatedPoints.add(pt);
    }
    
    public void addSplinePoint(Point p) {
        splinePoints.add(p);
    }
    
    public void addSplinePoints(ArrayList<Point> pt) {
        splinePoints = pt;
    }
    
    public ArrayList<Point> getAnnotatedPoints() {
        return annotatedPoints;
    }
    
    public ArrayList<Point> getSplinePoints() {
        return splinePoints;
    }
}
