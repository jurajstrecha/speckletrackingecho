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
public final class MatUtils {
    private MatUtils(){}
    
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
     * Prevedie množinu bodov na maticu, ktorá sa používa pri projekcii
     * pomocou PCA.
     * 
     * @param pts množina bodov
     * @param pcaMat matica 1x2N, kde N je počet bodov v pararametri pts
     */
    public static void ptsToPCAMat(ArrayList<Point> pts, Mat pcaMat) {
        int ptsSize = pts.size();
        Point p;
        for (int i = 0; i < ptsSize; i++) {
            p = pts.get(i);
            pcaMat.put(0, i, p.x);
            pcaMat.put(0, i + ptsSize, p.y);
        }
    }
    
    /**
     * Zo zadanej sady bodov vytvorí OpenCV maticu Nx2, kde N je počet bodov.
     * 
     * @param pts body
     * @param mat výsledná matica Nx2
     */
    public static void ptsToMat(ArrayList<Point> pts, Mat mat) {
        if (pts != null && pts.size() > 1) {
            if (pts.size() != mat.rows()) {
                return;
            }
            Point p;
            for (int i = 0; i < pts.size(); i++) {
                p = pts.get(i);
                mat.put(i, 0, p.x);
                mat.put(i, 1, p.y);
            }
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
    public static void matOfPoint2fToPts(MatOfPoint2f newPts, ArrayList<Point> pts) {
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
    public static void matOfPoint2fToPCAMat(MatOfPoint2f pts, Mat pcaMat) {        
        for (int i = 0; i < pts.rows(); i++) {
            pcaMat.put(0, i, pts.get(i, 0)[0]);
            pcaMat.put(0, i + pts.rows(), pts.get(i, 0)[1]);
        }
    }
    
    /**
     * Dvojkanálovú maticu tvaru prevedie na maticovú reprezentáciu tvaru s rozmermi
     * Nx2.
     * 
     * @param mat2f dvojkanálová matica, napríklad z metódy optického toku
     * @param mat výsledná maticová reprezentácia jedného tvaru
     */
    public static void matOfPoint2fToMat(MatOfPoint2f mat2f, Mat mat) {
        int ptsCnt = mat2f.rows();
        for (int i = 0; i < ptsCnt; i++) {
            mat.put(i, 0, mat2f.get(i, 0)[0]);
            mat.put(i, 1, mat2f.get(i, 0)[1]);
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
    public static void pcaMatToMatOfPoint2f(Mat pcaMat, MatOfPoint2f pts) {
        int cnt = pcaMat.cols() / 2;
        for (int i = 0; i < cnt; i++) {
            pts.put(i, 0, pcaMat.get(0, i)[0], pcaMat.get(0, i + cnt)[0]);
        }
    }
       
    /**
     * Prevedie tvar reprezentovaný maticou z PCA spätnej projekcie na množinu
     * bodov, ktoré je možné vykresliť ako tvar.
     * 
     * @param pcaMat matica 1xN
     * @param pts množina bodov veľkosti N/2
     */    
    
    public static void pcaMatToPts(Mat pcaMat, ArrayList<Point> pts) {
        int pcaMatSize = pcaMat.cols() / 2;
        Point p;
        for (int i = 0; i < pcaMatSize; i++) {
            if (pts.size() < pcaMatSize) {
                p = new Point();
                pts.add(p);
            } else {
                p = pts.get(i);
            }
            p.x = (int)pcaMat.get(0, i)[0];
            p.y = (int)pcaMat.get(0, i + pcaMatSize)[0];
        }
    }
    
    /**
     * Tvar z reprezentácie pre PCA (1x2N, kde N je počet bodov 2D tvaru) prevedie
     * na maticu Nx2.
     * 
     * @param pcaMat matica z PCA projekcie
     * @param mat výsledná matica
     */
    public static void pcaMatToMat(Mat pcaMat, Mat mat) {
        int ptsCnt = pcaMat.cols() / 2;
        for (int i = 0; i < ptsCnt; i++) {
            mat.put(i, 0, pcaMat.get(0, i)[0]);
            mat.put(i, 1, pcaMat.get(0, i + ptsCnt)[0]);
        }
    }
       
    /**
     * Prevedie maticovú reprezentáciu jedného tvaru na dvojkanálovú maticu
     * použitú napríklad v metóde optického toku.
     * 
     * @param mat maticová reprezentácia jedného tvaru
     * @param mat2f dvojkanálová matica reprezentujúca 2D tvar
     */
    public static void matToMatOfPoint2f(Mat mat, MatOfPoint2f mat2f) {
        int ptsCnt = mat.rows();
        for (int i = 0; i < ptsCnt; i++) {
            mat2f.put(i, 0, mat.get(i, 0)[0], mat.get(i, 1)[0]);
        }
    }
    
    /**
     * Prevedie maticovú reprezentáciu tvaru, rozmerov Nx2, na zobraziteľné body.
     * 
     * @param mat maticová reprezentácia tvaru
     * @param pts výsledný zoznam zobraziteľných bodov
     */
    public static void matToPts(Mat mat, ArrayList<Point> pts) {
        int matSize = mat.rows();
        Point p;
        for (int i = 0; i < matSize; i++) {
            if (pts.size() < matSize) {
                p = new Point();
                pts.add(p);
            } else {
                p = pts.get(i);
            }
            p.x = (int)mat.get(i, 0)[0];
            p.y = (int)mat.get(i, 1)[0];
        }
    }
    
    /**
     * Prevedie maticu tvaru s rozmermi Nx2, kde N je počet bodov tvaru na maticu
     * pre PCA projekciu s rozmermi 1x2N. Pôvodná matica sa používa pri zarovnávaní
     * bodov tvaru na iný tvar.
     * 
     * @param mat matica pôvodného tvaru
     * @param pcaMat výsledná matica pre PCA spracovanie
     */
    public static void matToPCAMat(Mat mat, Mat pcaMat) {
        int ptsCnt = mat.rows();
        for (int i = 0; i < ptsCnt; i++) {
            pcaMat.put(0, i, mat.get(i, 0)[0]);
            pcaMat.put(0, i + ptsCnt, mat.get(i, 1)[0]);
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
