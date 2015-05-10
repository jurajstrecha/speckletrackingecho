package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import java.awt.Point;
import java.util.ArrayList;
import org.opencv.core.MatOfPoint2f;

/**
 *
 * @author Juraj Strecha, xstrec01
 */
public class Utils {
    public static void ptsToMatOfPts2f(ArrayList<Point> pts, MatOfPoint2f newPts) {
        Point p;
        for (int i = 0; i < pts.size(); i++) {
            p = pts.get(i);
            newPts.put(i, 0, p.x, p.y);
        }
    }
    
    public static void matOfPts2fToPts(MatOfPoint2f newPts, ArrayList<Point> pts) {
        Point p;
        for (int i = 0; i < newPts.rows(); i++) {
            p = pts.get(i);
            p.x = (int)newPts.get(i, 0)[0];
            p.y = (int)newPts.get(i, 0)[1];
        }
    }
}
