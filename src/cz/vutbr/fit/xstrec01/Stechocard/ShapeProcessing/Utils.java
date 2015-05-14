package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import java.awt.Point;
import java.util.ArrayList;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;

/**
 * Pomocné funkcie pre konverziu matíc.
 *
 * @author Juraj Strecha, xstrec01
 */
public class Utils {
    
    /**
     * Sadu bodov (napríklad anotovaných) prevedie na dvojkanálovú maticu.
     * Výsledná matica sa používa v OpenCV Optical Flow.
     * Opačný prevod získame pomocou matOfPts2fToPts().
     * 
     * @param pts sada N bodov
     * @param newPts výsledná dvojkanálová matica matica 1xN
     */
    public static void ptsToMatOfPts2f(ArrayList<Point> pts, MatOfPoint2f newPts) {
        Point p;
        for (int i = 0; i < pts.size(); i++) {
            p = pts.get(i);
            newPts.put(i, 0, p.x, p.y);
        }
    }
        
    /**
     * Dvojkanálovú maticu reprezentujúcu tvar (získanú napríklad z OpenCV Optical
     * Flow) prevedie na množinu bodov Point.
     * Opačný prevod získame pomocou ptsToMatOfPts2f().
     * 
     * @param newPts dvojkanálová matica bodov tvaru
     * @param pts množina bodov Point
     */
    public static void matOfPts2fToPts(MatOfPoint2f newPts, ArrayList<Point> pts) {
        Point p;
        for (int i = 0; i < newPts.rows(); i++) {
            p = pts.get(i);
            p.x = (int)newPts.get(i, 0)[0];
            p.y = (int)newPts.get(i, 0)[1];
        }
    }
    
    /**
     * Dvojkanálovú maticu prevedie na maticu tvaru pre PCA, kde v riadku sú
     * najskôr postupne všetky hodnoty x a za nimi nasledujú hodnoty y.
     * Opačný prevod získame pomocou pcaMatTomatOfPts2f().
     * 
     * @param pts dvojkanálová matica 1xN
     * @param pcaMat matica 1x2N
     */
    public static void matOfPts2fToPCAMat(MatOfPoint2f pts, Mat pcaMat) {        
        for (int i = 0; i < pts.rows(); i++) {
            pcaMat.put(0, i, pts.get(i, 0)[0]);
            pcaMat.put(0, i + pts.rows(), pts.get(i, 0)[1]);
        }
    }
    
    /**
     * Maticu reprezentujúcu tvar z PCA x1, x2, ..., xN, y1, y2, ..., yN prevedie
     * na dvojkanálovú maticu xi,yi. S takouto maticou pracuje napríklad OpenCV
     * Oprical Flow.
     * Opačný prevod získame pomocou matOfPts2fToPCAMat().
     * 
     * @param pcaMat matica z PCA
     * @param pts dvojkanálová matica
     */
    public static void pcaMatTomatOfPts2f(Mat pcaMat, MatOfPoint2f pts) {
        int cnt = pcaMat.cols() / 2;
        for (int i = 0; i < cnt; i++) {
            pts.put(i, 0, pcaMat.get(0, i)[0], pcaMat.get(0, i + cnt)[0]);
        }
    }
    
    /**
     * Do konzoly vypíše hodnoty matice naformátované do riadkov a stĺpcov.
     * Používa sa pri ladení a debuggovaní.
     * 
     * @param m matica, ktorú chceme vypísať
     */
    public static void printMat(Mat m) {
        if (m == null) {
            System.out.println("mat == null");
        } else if (m.empty()) {
            System.out.println("Empty mat");
        } else {
            System.out.println(m.dump());
        }
    }
}
