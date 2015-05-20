package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;

/**
 * Objekt pre výpočet analýzy hlavných komponent podľa návrhového vzoru Factory.
 *
 * @author Juraj Strecha, xstrec01
 */
public final class PCA {
    private static PCA instance = null;
    
    public Mat eigenvectors;
    public Mat eigenvalues;
    public Mat mean;
    public Mat bLowerBounds;
    public Mat bUpperBounds;
    public boolean initialized;
    
    private PCA() {
        eigenvectors = new Mat();
        eigenvalues = new Mat();
        mean = new Mat();
        initialized = false;
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
        eigenvectors = new Mat();
        eigenvalues = new Mat();
        mean = new Mat();
        
        // konštanta Constants.PCA_VARIANCE_TO_RETAIN udáva veľkosť odchýlky,
        // ktorú chceme z množiny tvarov zachovať, implicitne 98%
        Core.PCAComputeVar(matForPCA, mean, eigenvectors, Constants.PCA_VARIANCE_TO_RETAIN);
        
        Mat covar = new Mat();
        Core.calcCovarMatrix(matForPCA, covar, new Mat(), Core.COVAR_NORMAL | Core.COVAR_ROWS | Core.COVAR_SCALE);        
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
            // 
            bLowerBounds.put(0, i, deviation * -Constants.SIGMA_MULTIPLIER);
            bUpperBounds.put(0, i, deviation * Constants.SIGMA_MULTIPLIER);
        }
        MatUtils.printMat(bLowerBounds);
        MatUtils.printMat(bUpperBounds);
        // poznač, že hodnoty boli nastavené
        initialized = true;
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
    
    public Mat getMeanShape() {
        return mean;
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
    
    /**
     * Zistí, či boli vypočítané eigenvektory a eigenhodnoty trénovacej množiny.
     * 
     * @return true, ak prebehla inicializácia, inak false
     */
    public boolean isInitialized() {
        return initialized;
    }
}
