package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import java.awt.Point;
import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Trieda statických metód pre zarovnanie sady tvarov reprezentovaných bodmi v 2D.
 *
 * @author Juraj Strecha, xstrec01
 */
public final class Procrustes {
    
    /**
     * Procrustovou analýzou zarovná tvary na seba tak, aby ich vzdialenosť
     * bola minimálna. Zarovnanie rieši analyticky pomocu SVD.
     * 
     * @param shapes 
     * @return 
     */
    public static ArrayList<Mat> analyze(Shapes shapes) {
        // reprezentuj tvary maticami Nx2
        ArrayList<Mat> shapeMats = shapesToMats(shapes);
        
        // aktuálny priemerný tvar
        Mat mean = null;
        // tažisko aktuálneho priemerného tvaru
        double[] meanCentroid;
        // vystredené vektory aktuálneho tvaru - použité v kovariančnej matici
        Mat meanCenteredForH;        

        
        // priemerný tvar vypočítaný v aktuálnej iterácii
        Mat newMean = null;
        // štruktúry pre tvar, ktorý sa bude zarovnávať rotáciou na priemerný
        Mat shape;

        // suma Euklidovych vzdialeností bodov dvoch po sebe nasledujúcich tvarov
        // použiva sa k sledovaniu konvergencie a ukončeniu procesu zarovnávania
        double err;
        // príznak prvej iterácie, kedy sa priemer nepočíta, ale zvolí sa prvý
        // tvar z množiny tvarov
        boolean start = true;
        // v pripade prvého priemerného tvaru vynecháme zarovnavanie prvého
        // tvaru sady, začneme od 1, v ostatných iteráciách už začíname od 0
        int startP;
        
        do {
            if (start) {
                mean = shapeMats.get(0);
                startP = 1;
                start = false;
            } else {
                mean = newMean;
                startP = 0;
            }
            
            meanCentroid = calculateShapeCentroid(mean);
            meanCenteredForH = centeredPts(mean, meanCentroid);
            
            // zarovnaj všetky tvary na súčasný priemerný tvar pomocou SVD dekompozície
            for (int p = startP; p < shapeMats.size(); p++) {
                shape = shapeMats.get(p); 

                alignShape(shape, mean, meanCentroid, meanCenteredForH);
            }
            
            // vypočíitaj nový priemerný tvar
            newMean = calculateMeanShape(shapeMats);
            
            // zarovnaj starý a nový priemer na seba
            alignShape(newMean, mean, meanCentroid, meanCenteredForH);
            // spočítaj veľkosť zmeny priemerného tvaru po iterácii
            err = calculateErr(mean, newMean);
        } while (err > 0.001);
        
        return shapeMats;
    }
    
    /**
     * Vypočíta rozdiel dvoch tvarov. Hodnota rozdielu je suma Euklidovych vzdialeností
     * bodov tvarov.
     * 
     * @param src tvar 1
     * @param dst tvar 2
     * @return suma vzdialeností tvarov, ich rozdielnosť, čím menšia, tým podobnejšie si tvary sú
     */
    public static double calculateErr(Mat src, Mat dst) {
        if (src == null || dst == null) {
            System.err.println("calculateErr: one of the matrices is null");
            return Double.MAX_VALUE;
        } else if (src.rows() != dst.rows() || src.cols() != dst.cols()) {
            System.err.println("calculateErr: dimensions of the matrices do not match");
            return Double.MAX_VALUE;
        }
        
        double err = 0.0;
        double distance;
        double[] p1 = new double[2];
        double[] p2 = new double[2];
        
        for (int i = 0; i < src.rows(); i++) {
            p1[0] = src.get(i, 0)[0];
            p1[1] = src.get(i, 1)[0];
            p2[0] = dst.get(i, 0)[0];
            p2[1] = dst.get(i, 1)[0];
            err += euclideanDistance(p1, p2);
        }
        
        return err;
    }
    
    /**
     * Výpočet Euklidovej vzdialenosti dvoch bodov v 2D priestore.
     * 
     * @param p1 bod 1
     * @param p2 bod 2
     * @return vzdialenosť zadaných bodov
     */
    public static double euclideanDistance(double[] p1, double[] p2) {
        double xDiff = p1[0] - p2[0];
        double yDiff = p1[1] - p2[1];
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }
    
    /**
     * Pomocou SVD dekompozície zarovná dva zadané tvary na seba. Jedným z tvarov
     * je priemerný tvar, ale obecne sa môže jednať o ľubovolný tvar. Prvý zadaný
     * tvar bude zmenený na ten, ktorý vznikol zarovnaním na tvar druhý.
     * 
     * @param shape tvar, ktorý má byť zarovnaný
     * @param mean tvar, na ktorý sa má prvý tvar zarovnať
     * @param meanCentroid ťažisko (bod) druhého tvaru
     * @param meanCenteredForH matica súradníc bodov druhého tvaru po odčítaní súradníc ťažiska druhého tvaru
     */
    public static void alignShape(Mat shape, Mat mean, double[] meanCentroid, Mat meanCenteredForH) {
        double[] shapeCentroid;
        
        Mat shapeCenteredForH;
        
        Mat H = new Mat(Constants.SPACE_DIMENSION,
                        Constants.SPACE_DIMENSION,
                        CvType.CV_64F);
        
        Mat w = new Mat(Constants.SPACE_DIMENSION,
                        Constants.SPACE_DIMENSION,
                        CvType.CV_64F);
        Mat u = new Mat(Constants.SPACE_DIMENSION,
                        Constants.SPACE_DIMENSION,
                        CvType.CV_64F);
        Mat vt = new Mat(Constants.SPACE_DIMENSION,
                         Constants.SPACE_DIMENSION,
                         CvType.CV_64F);
        
        Mat R = new Mat(Constants.SPACE_DIMENSION,
                        Constants.SPACE_DIMENSION,
                        CvType.CV_64F);

        Mat t = new Mat(Constants.SPACE_DIMENSION, 1, CvType.CV_64F);
        
        Mat rotatedShape = new Mat(Constants.SPACE_DIMENSION, 1, CvType.CV_64F);
        
        /* -------------------------- VÝPOČET ------------------------------- */
        
        // ťažisko tvaru
        shapeCentroid = calculateShapeCentroid(shape);
        // centrované vektory pre kovariančnú maticu
        shapeCenteredForH = centeredPts(shape, shapeCentroid);
        
        // výpočet kovariančnej matice
        Core.gemm(shapeCenteredForH.t(), meanCenteredForH, 1, new Mat(), 0, H);
        
        // SVD dekompozícia
        Core.SVDecomp(H, w, u, vt);
        
        // výpočet rotačnej matice R
        Core.gemm(vt.t(), u.t(), 1, new Mat(), 0, R);
        // úprava v prípade negatívneho determinantu
        if (Core.determinant(R) < 0) {
            R.put(0, 0, R.get(1, 0)[0] * -1);
            R.put(0, 1, R.get(1, 1)[0] * -1);
        }
        
        // výpočet translačného vektoru t
        t.put(0, 0, meanCentroid[0] - (R.get(0, 0)[0] * shapeCentroid[0] +
                                       R.get(0, 1)[0] * shapeCentroid[1]));
        t.put(1, 0, meanCentroid[1] - (R.get(1, 0)[0] * shapeCentroid[0] +
                                       R.get(1, 1)[0] * shapeCentroid[1]));

        // rotácia + translácia, B = R*A+t, vznikne výsledné zarovnanie
        for (int i = 0; i < shape.rows(); i++) {
            Core.gemm(R, shape.row(i).t(), 1, new Mat(), 0, rotatedShape);
            shape.put(i, 0, rotatedShape.get(0, 0)[0] + t.get(0, 0)[0]);
            shape.put(i, 1, rotatedShape.get(1, 0)[0] + t.get(1, 0)[0]);
        }
    }
    
    
    /**
     * Výpočet priemerného tvaru z množiny tvarov.
     * 
     * @param shapes množina tvarov
     * @return priemerný tvar
     */
    public static Mat calculateMeanShape(ArrayList<Mat> shapes) {
        if (shapes == null || shapes.isEmpty()) {
            return null;
        }
        
        if (shapes.size() == 1) {
            return shapes.get(0);
        }
        
        int shapePtsCnt = shapes.get(0).rows();
        Mat mean = Mat.zeros(shapePtsCnt, Constants.SPACE_DIMENSION, CvType.CV_64F);
        for (Mat m: shapes) {
            Core.add(mean, m, mean);
        }
        
        Core.divide(mean, new Scalar(shapes.size()), mean);
        
        return mean;
    }
    
    /**
     * Od každého body tvaru odpočíta hodnotu ťažiska tvaru.
     * 
     * @param mat tvar
     * @param centroid ťažisko tvaru
     * @return výsledná matica bodov, ktorých súradnice sú zmenšené o hodnotu ťažiska
     */
    public static Mat centeredPts(Mat mat, double[] centroid) {
        if (mat == null || mat.empty()) {
            return null;
        }
        
        Mat centered = new Mat(mat.rows(), Constants.SPACE_DIMENSION, CvType.CV_64F);
        
        for (int i = 0; i < mat.rows(); i++) {
            centered.put(i, 0, mat.get(i, 0)[0] - centroid[0]);
            centered.put(i, 1, mat.get(i, 1)[0] - centroid[1]);
        }
        
        return centered;
    }

    /**
     * Množinu tvarov Shape transformuje na sadu matíc, kde každá matica tvaru
     * je vo formáte Nx2, N je počet bodov tvaru.
     * 
     * @param shapes množina tvarov
     * @return sada matíc reprezentujúcich tvary
     */
    public static ArrayList<Mat> shapesToMats(Shapes shapes) {
        if (shapes == null || shapes.isEmpty()) {
            return null;
        }

        // budúca množina tvarov reprezentovaných maticami
        ArrayList<Mat> shapesMats = new ArrayList<Mat>(shapes.size());
        // matica jedného tvaru
        Mat shapeMat;
        ArrayList<Point> pts;        
        for (Shape shape: shapes) {
            pts = shape.getSplinePoints();
            if (pts == null) {
                return null;
            }
            shapeMat = new Mat(pts.size(),
                               Constants.SPACE_DIMENSION,
                               CvType.CV_64F);
            shapeToMat(shape, shapeMat);
            if (shapeMat.empty()) {
                return null;
            }
            shapesMats.add(shapeMat);
        }

        return shapesMats;
    }
    
    /**
     * Zo zadaného objektu triedy Shape vytvorí OpenCV maticu.
     * 
     * @param shape tvar obsahujúci body reprezentované objektami triedy Point
     * @param mat prázdna OpenCV matica s rozmermi Nx2, ked N je počet bodov tvaru a 2 je dimenzia
     */
    public static void shapeToMat(Shape shape, Mat mat) {
        if (shape == null || mat == null || mat.cols() != Constants.SPACE_DIMENSION) {
            return;
        }
        
        ArrayList<Point> pts = shape.getSplinePoints();
        if (pts != null && pts.size() > 1) {
            if (pts.size() != mat.rows()) {
                return;
            }
            Point p;
            for (int i = 0; i < pts.size(); i++) {
                p = pts.get(i);
                mat.put(i, 0, p.getX());
                mat.put(i, 1, p.getY());
            }
        }
    }
    
    /**
     * Vypočíta bod ťažiska zadaného tvaru.
     * 
     * @param shape matica (Nx2) bodov reprezentujúcich tvar
     * @return bod ťažiska
     */
    public static double[] calculateShapeCentroid(Mat shape) {
        if (shape != null) {
            double x = 0.0, y = 0.0, cnt = shape.rows();

            for (int i = 0; i < cnt; i++) {
                x += shape.get(i, 0)[0];
                y += shape.get(i, 1)[0];
            }

            x = x / cnt;
            y = y / cnt;
            
            double[] ret = {x, y};
            return ret;
        }
        return null;
    }
}
