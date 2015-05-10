package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import java.awt.Point;
import java.util.ArrayList;

/**
 * Sada statických metód pre výpočet bodov Catmull-Rom interpolačného splajnu.
 * Zdroj inšpirácie: http://www.booncotter.com/waypoints-catmull-rom-splines/
 * 
 * @author Juraj Strecha, xstrec01
 */
public class CatmullRom {
    
    /**
     * Z kontrolných bodov zadaných užívateľom, počtu vzorkov na jeden úsek
     * a rovníc Catmull-Rom polynómu vypočíta splajn krivku.
     * 
     * @param controlPoints kontrolné body, ktorými bude preložený splajn
     * @return výsledná sada bodov, ktoré tvoria kompletný splajn
     */
    public static ArrayList<Point> calculateSpline(ArrayList<Point> controlPoints) {
        if (controlPoints != null && controlPoints.size() > 1) {
            ArrayList<Point> splinePoints = new ArrayList<Point>();            
            int samples = Constants.SPLINE_SAMPLES_PER_SPAN;            
            double deltaT = 1.0 / samples;
            double t;
            
            // kopírovanie prvého a posledného bodu na začiatok a koniec zoznamu
            // pre ich zachovanie v splajne
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
     * Spočíta nový bod interpolačného splajnu na základe parametru t pomocou
     * polynomiálnych rovníc.
     * 
     * @param p1 prvý kontrolný bod - ovplyvňuje tvar splajnu
     * @param p2 druhý kontrolný bod - štart intervalu
     * @param p3 tretí kontrolný bod - koniec intervalu
     * @param p4 štvrtý kontrolný bod - ovplyvňuje tvar splajnu
     * @param t relatívny parameter vnútri intervalu
     * @return body výsledného splajnu
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
    
    /**
     * Vypočíta dĺžku splajnu v pixeloch.
     * 
     * @param spline splajn krivka, ktorej dĺžku chceme spočítať
     * @return dĺžka splajnu v pixeloch
     */
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
     * Kopíruje prvý a posledný bod na začiatok a koniec zoznamu.
     * 
     * @param points zoznam bodov rozšírený o prvý a posledný bod
     */
    public static void extendCtrlPointsSet(ArrayList<Point> points) {
        if (points != null && points.size() > 1) {
            points.add(0, getFirstPoint(points));
            points.add(getLastPoint(points));
        }
    }
    
    /**
     * Odstráni pridaný prvý a posledný bod zo začiatku a konca zoznamu.
     * 
     * @param points výsledný zoznam bodov
     */
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
     * Vráti prvý bod zoznamu.
     * 
     * @param points zoznam bodov
     * @return prvý bod zoznamu
     */
    public static Point getFirstPoint(ArrayList<Point> points) {
        if (points != null) {
            return points.get(0);
        }
        return null;
    }
    
    /**
     * Vráti posledný bod zoznamu.
     * 
     * @param points zoznam bodov
     * @return posledný bod zoznamu
     */
    public static Point getLastPoint(ArrayList<Point> points) {
        if (points != null) {
            return points.get(points.size() - 1);
        }
        return null;
    }
    
    /**
     * Zvolí zadaný počet bodov zo splajnu rovnomerne podľa dĺžky splajnu.
     * Vznikne o jeden bod viac ako je počet intervalov.
     * 
     * @param spline body splajnu
     * @param intervals počet intervalov, na ktoré bude splajn rozdelený
     * @return výsledná reprezentácia splajnu o veľkosti intervals + 1 bodov
     */
    public static ArrayList<Point> divideSpline(ArrayList<Point> spline, int intervals) {
        if (spline != null && spline.size() > 1) {
            ArrayList<Point> mainPoints = new ArrayList<Point>();
            
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
            
            // oprava zaokrúhľovacej chyby
            if (mainPoints.size() < Constants.SPLINE_DIV_INTERVALS + 1) {
                mainPoints.add(spline.get(spline.size() - 1));
            }
                       
            return mainPoints;
        }
        return null;
    }
    
    /**
     * Spočíta Euklidovskú vzdialenosť dvoch bodov.
     * 
     * @param a prvý bod
     * @param b druhý bod
     * @return vzdialenosť v piexloch
     */
    public static double getDistance(Point a, Point b) {
        double vecXLen = a.getX() - b.getX();
        double vecYLen = a.getY() - b.getY();
        // Euclidean distance
        return Math.sqrt(vecXLen * vecXLen + vecYLen * vecYLen);
    }
}
