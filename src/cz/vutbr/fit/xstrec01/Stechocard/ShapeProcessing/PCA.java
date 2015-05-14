package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;

/**
 * Objekt pre výpočet analýzy hlavných komponent podľa návrhového vzoru Factory.
 *
 * @author Juraj Strecha, xstrec01
 */
public class PCA {
    private static PCA instance = null;
    
    Mat eigenvectors;
    Mat eigenvalues;
    Mat mean;
    Mat bLowerBounds;
    Mat bUpperBounds;
    
    private PCA() {
        eigenvectors = new Mat();
        eigenvalues = new Mat();
        mean = new Mat();
    }
    
    /**
     * Metóda továrne, vyrobí novú inštanciu, ak ešte neexistuje, inak vráti
     * práve jednu existujúcu - v systéme je vždy vytvorená iba jedna.
     * 
     * @return inštancia PCA
     */
    public static PCA getInstance() {
        if (instance == null) {
            instance = new PCA();
        }
        return instance;
    }
    
    /**
     * Inicializuje štruktúry potrebné k výpočtu PCA projekcie a spätnej projekcie.
     * Vypočíta vlastné vektory, vlastné hodnoty a bod priemeru, ktoré sa použijú
     * v metódach OpenCV PCAProject() a PCABackProject().
     * 
     * @param shapeMats 
     */
    public void init(ArrayList<Mat> shapeMats) {
        Mat matForPCA = matsToPCAMats(shapeMats);

        // konštanta Constants.PCA_VARIANCE_TO_RETAIN udáva veľkosť odchýlky,
        // ktorú chceme z množiny tvarov zachovať, implicitne 98%
        Core.PCAComputeVar(matForPCA, mean, eigenvectors, Constants.PCA_VARIANCE_TO_RETAIN);
        
        Mat covar = new Mat();
        Mat someMean = new Mat();
        Core.calcCovarMatrix(matForPCA, covar, someMean, Core.COVAR_SCRAMBLED ^ Core.COVAR_ROWS);
        Core.eigen(covar, false, eigenvalues, new Mat());
        
        // vyber iba eigenvalues, ktoré majú eigenvector spočítaný metódou
        // PCA so zachovanim 98% odchylky
        eigenvalues = eigenvalues.rowRange(0, eigenvectors.rows());
        
        // predpočítanie okrajových hodnôt parametra b z vektoru rozptylu/eigenvalues
        bLowerBounds = new Mat(1, eigenvalues.rows(), CvType.CV_64F);
        bUpperBounds = new Mat(1, eigenvalues.rows(), CvType.CV_64F);        
        double variance, deviation;
        for (int i = 0; i < eigenvalues.rows(); i++) {
            variance = eigenvalues.get(i, 0)[0];
            deviation = Math.sqrt(variance);
            // kvôli rozdielnemu výpočtu eigenvektorov a eigenvalues musí
            // dojsť ku kompenzácii rozptylu, viac v komentári pri definícii
            // konštanty EIGENVALUE_COMPENSATION v súbore Constants.java
            bLowerBounds.put(0, i, deviation * -Constants.EIGENVALUE_COMPENSATION);
            bUpperBounds.put(0, i, deviation * Constants.EIGENVALUE_COMPENSATION);
        }
    }

    /**
     * Tvar reprezentovaný maticou hodnôt súradníc bodov premietne pomocou
     * eigenvektorov z trénovacích dát do parametrickej reprezentácie. Hodnoty
     * parametrov obmedzí do intervalu daného eigenhodnotami pre jednotlivé
     * eigenvektory a spätnou projekciou vráti tvar, ktorý sa podobá tvarom
     * trénovacej množiny.
     * 
     * @param shapeMat matica súradníc tvaru
     */
    public void pca(Mat shapeMat) {
        // množina koeficientov projekcie, upraví sa podľa rozptylu
        Mat b = new Mat();
        // premietnutie tvaru získaného metódou Optical Flow do PCA koeficientov
        Core.PCAProject(shapeMat, mean, eigenvectors, b);
        
        // úprava koeficientov, aby výsledný tvar vyhovoval tvarom trénovacej množiny
        double bVal, lowerBound, upperBound;
        for (int i = 0; i < b.cols(); i++) {
            bVal = b.get(0, i)[0];
            lowerBound = bLowerBounds.get(0, i)[0];
            upperBound = bUpperBounds.get(0, i)[0];
            if (bVal < lowerBound) {
                b.put(0, i, lowerBound);
            } else if (bVal > upperBound) {
                b.put(0, i, upperBound);
            }
        }
        
        // vygenerovanie upraveného tvaru
        Core.PCABackProject(b, mean, eigenvectors, shapeMat);
    }
    
    /**
     * Z bodov získaných pomocou OpenCV LK optického toku vypočíta tvar podobný
     * tým v trénovacej množine. Ak je tvar v povolených medziach, neupravuje
     * sa, inak sa hodnoty orežú a vráti sta tvar najbližší  tvarom trénovacej
     * množiny.
     * 
     * @param shape maticová reprezentácia bodov tvaru získaná z optického toku
     */
    public void getPlausibleShape(MatOfPoint2f shape) {
        int shapeSize = shape.rows();
        Mat shapeMat = new Mat(1, shapeSize*2, CvType.CV_64F);
        
        Utils.matOfPts2fToPCAMat(shape, shapeMat);
        pca(shapeMat);
        Utils.pcaMatTomatOfPts2f(shapeMat, shape);
    }
    
    /**
     * Sadu zarovnaných tvarov transformuje na maticu formátu potrebného pre
     * výpočet PCA.
     * Vznikne matica v tvare x11, x12, ..., x1N, y11, y12, ..., y1N
     *                        x21, x22, ..., x2N, y21, y22, ..., y2N
     *                        ...
     *                        xM1, xM2, ..., xMN, yM1, yM2, ..., yMN,
     * kde M je počet tvarov a N počet bodov, ktorými je tvar reprezentovaný.
     * 
     * @param mats matica tvarov
     * @return matica pre PCA
     */
    public static Mat matsToPCAMats(ArrayList<Mat> mats) {
        int ptsInShape = Constants.SPLINE_DIV_INTERVALS + 1;
        Mat pcaMat = new Mat(mats.size(),
                             ptsInShape *  2,
                             CvType.CV_64F);
        
        Mat m;
        for (int i = 0; i < pcaMat.rows(); i++) {
            m = mats.get(i);
            for (int j = 0; j < ptsInShape; j++) {
                pcaMat.put(i, j, m.get(j, 0)[0]);
                pcaMat.put(i, j+ptsInShape, m.get(j, 1)[0]);
            }
        }
        
        return pcaMat;
    }
}
