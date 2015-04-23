package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import java.util.ArrayList;
import java.awt.Point;

/**
 * Reprezentuje tvar vyznaceny uzivatelom. Obsahuje body, ktore uzivatel naklikal
 * a tiez body, ktore boli vypocitane zo splajnu. Tie budu pouzite pre vypocet
 * modelu.
 * 
 * @author Bc. Juraj Strecha
 */
public final class Shape {
    ArrayList<Point> annotatedPoints;
    ArrayList<Point> splinePoints;
    
    public Shape() {
        annotatedPoints = new ArrayList<Point>();
        splinePoints = new ArrayList<Point>();
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
