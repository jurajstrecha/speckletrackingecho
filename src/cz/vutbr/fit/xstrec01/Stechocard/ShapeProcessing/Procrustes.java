package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.GUI.Canvas;
import java.awt.Point;
import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 *
 * @author Bc. Juraj Strecha
 */
public final class Procrustes {
    
    public static ArrayList<Mat> analyze(Shapes shapes, Canvas canvas) {
        // reprezentuj tvary maticami Nx2
        ArrayList<Mat> shapeMats = shapesToMats(shapes);
        //printMat(shapeMats.get(1));
        
        // aktualny priemerny tvar
        Mat mean = null;
        // tazisko aktualneho priemerneho tvaru
        double[] meanCentroid;
        // vystredene vektory aktualneho tvaru - pouzite v kovariancnej matici
        Mat meanCenteredForH;        

        
        // priemerny tvar vypocitany v aktualnej iteracii
        Mat newMean = null;
        // struktury pre tvar, ktory sa bude zarovnavat rotaciou na priemerny
        Mat shape;

        // suma Euklidovych vzdialenosti bodov dvoch po sebe nasledujucich tvarov
        // pouziva sa k sledovaniu konvergencie a ukonceniu procesu zarovnavania
        double err;
        // priznak prvej iteracie, kedy sa priemer nepocita, ale zvoli sa prvy
        // tvar z mnoziny tvarov
        boolean start = true;
        // v pripade prveho priemerneho tvaru vynechame zarovnavanie prveho
        // tvaru sady, zacneme od 1, v ostatnych iteraciach uz zaciname od 0
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
            
            // zarovnaj vsetky tvary na sucasny priemerny tvar pomocou SVD dekompozicie
            //System.out.println("p = " + startP);
            for (int p = startP; p < shapeMats.size(); p++) {
                shape = shapeMats.get(p); 

                alignShape(shape, mean, meanCentroid, meanCenteredForH);

                // zapis do pamati platna, aby sa vysledok mohol vykreslit
                shapes.get(p).addSplinePoints(matToSplinePts(shape));
            }
            
            // vypocitaj novy priemerny tvar
            newMean = calculateMeanShape(shapeMats);
            
            // zarovnaj stary a novy priemer na seba
            alignShape(newMean, mean, meanCentroid, meanCenteredForH);
            // spocitaj velkost zmeny priemerneho tvaru po iteracii
            err = calculateErr(mean, newMean);
            //System.out.println("err = " + calculateErr(mean, newMean));
        } while (err > 0.001);
        
        //printMat(newMean);
        //canvas.setSplinePoints(matToSplinePts(mean));
        return shapeMats;
    }
    
    public static Mat matsToPCAMats(ArrayList<Mat> mats) {
        Mat pcaMat = new Mat(((int)Constants.SPLINE_DIV_INTERVALS + 1)*  2,
                             mats.size(),
                             CvType.CV_64F);
        
        Mat m;
        for (int i = 0; i < mats.size(); i++) {
            m = mats.get(i);
            for (int j = 0; j < m.rows(); j++) {
                pcaMat.put(j, i, m.get(j, 0)[0]);
                pcaMat.put(j+11, i, m.get(j, 1)[0]);
            }
        }
        
        return pcaMat;
    }
    
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
    
    public static double euclideanDistance(double[] p1, double[] p2) {
        double xDiff = p1[0] - p2[0];
        double yDiff = p1[1] - p2[1];
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }
    
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
        
        /* -------------------------- VYPOCET ------------------------------- */
        
        // tazisko tvaru
        shapeCentroid = calculateShapeCentroid(shape);
        // centrovane vektory pre kovariancnu maticu
        shapeCenteredForH = centeredPts(shape, shapeCentroid);
        
        // vypocet kovariancnej matice
        Core.gemm(shapeCenteredForH.t(), meanCenteredForH, 1, new Mat(), 0, H);
        
        // SVD dekompozicia
        Core.SVDecomp(H, w, u, vt);
        
        // vypocet rotacnej matice R
        Core.gemm(vt.t(), u.t(), 1, new Mat(), 0, R);
        // uprava v pripade negativneho determinantu
        if (Core.determinant(R) < 0) {
            R.put(0, 0, R.get(1, 0)[0] * -1);
            R.put(0, 1, R.get(1, 1)[0] * -1);
        }
        
        // vypocet translacneho vektoru t
        t.put(0, 0, meanCentroid[0] - (R.get(0, 0)[0] * shapeCentroid[0] +
                                       R.get(0, 1)[0] * shapeCentroid[1]));
        t.put(1, 0, meanCentroid[1] - (R.get(1, 0)[0] * shapeCentroid[0] +
                                       R.get(1, 1)[0] * shapeCentroid[1]));

        // rotacia + translacia, B = R*A+t
        for (int i = 0; i < shape.rows(); i++) {
            Core.gemm(R, shape.row(i).t(), 1, new Mat(), 0, rotatedShape);
            shape.put(i, 0, rotatedShape.get(0, 0)[0] + t.get(0, 0)[0]);
            shape.put(i, 1, rotatedShape.get(1, 0)[0] + t.get(1, 0)[0]);
        }
    }
    
    

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
       
    public static ArrayList<Mat> shapesToMats(Shapes shapes) {
        if (shapes == null || shapes.isEmpty()) {
            return null;
        }

        // buduca mnozina tvarov reprezentovanych maticami
        ArrayList<Mat> shapesMats = new ArrayList<Mat>(shapes.size());
        // matica tvaru
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
     * Zo zadaneho objektu triedy Shape vytvori OpenCV maticu.
     * 
     * @param shape tvar pbsahujuci body reprezentovae objektami triedy Point
     * @param mat prazdna OpenCV matica s rpzmermi Ax2, ked A je pocet bodov tvaru a 2 je dimenzia
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
    
    public static ArrayList<Point> matToSplinePts(Mat mat) {
        if (mat == null || mat.empty()) {
            return null;
        }
        
        ArrayList<Point> pts = new ArrayList<Point>(mat.rows());
        
        for (int i = 0; i < mat.rows(); i++) {
            pts.add(new Point((int)mat.get(i, 0)[0], (int)mat.get(i, 1)[0]));
        }
        
        return pts;
    }
       
    /**
     * Pouzitim merania Euklidovej zvdialenosti tvaru normalizuje tvar.
     * 
     * @param shape Tvar, ktory ma byt normalizovany
     * @return Normalizovany tvar dlzky 1
     */
    public static Mat normalize(Mat shape) {
        if (shape == null) {
            return null;
        }
        Mat normalized = new Mat(shape.rows(),
                                 shape.cols(),
                                 CvType.CV_64F);
        double sum = 0.0;
        double size;
        double val;
        for (int i = 0; i < shape.rows(); i++) {
            val = shape.get(i, 0)[0];
            sum += val * val;
            val = shape.get(i, 1)[0];
            sum += val * val;
        }
        
        size = Math.sqrt(sum);
        
        for (int i = 0; i < normalized.rows(); i++) {
            normalized.put(i, 0, shape.get(i, 0)[0] / size);
            normalized.put(i, 1, shape.get(i, 1)[0] / size);
        }
        
        return normalized;
    }
    
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
    
    public static void printMat(Mat m) {
        if (m == null) {
            System.out.println("mat == null");
        } else if (m.empty()) {
            System.out.println("Empty mat");
        } else {
            System.out.println(m.dump());
        }
    }
    
    /*
    public static void translateToOrigin(Mat shape, double[] centroid) {
        if (shape == null) {
            System.err.println("translateToOrigin: shape is null");
        } else {
            double xdisplacement, ydisplacement;
            double[] shapeCentroid = calculateShapeCentroid(shape);
            xdisplacement = centroid[0] - shapeCentroid[0];
            ydisplacement = centroid[1] - shapeCentroid[1];
            
            for (int i = 0; i < shape.rows(); i++) {
                shape.put(i, 0, shape.get(i, 0)[0] + xdisplacement);
                shape.put(i, 1, shape.get(i, 1)[0] + ydisplacement);
            }             
        }
    }
    */
}
