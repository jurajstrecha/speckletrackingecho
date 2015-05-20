package cz.vutbr.fit.xstrec01.Stechocard.Video;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.CatmullRom;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.PCA;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.Procrustes;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.TrackedShape;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.MatUtils;
import java.awt.Point;
import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.video.Video;

/**
 * Sledovanie optického toku a odhad nového tvaru v nasledujúcom snímku
 * na základe bodov anotovaných užívateľom. Obmedzenie tvaru podľa parametrov
 * modelu.
 *
 * @author Juraj Strecha, xstrec01
 */
public final class OpticalFlow {
    // triedu obsahuje iba statické funkcie, nesmie byť inštanciovaná
    private OpticalFlow(){}
    
    /**
     * Vypočíta tvary pre všetky snímky videa pomocou optického toku medzi
     * dvomi po sebe nasledujúcimi snímkami.
     * 
     * @param initialShape počiatočný anotovaný tvar, ktorý chceme sledovať
     * @param vidData štruktúra so snímkami videa
     * @param shapes sada tvarov pre uloženie vypočítaných odhadnutých tvarov pre každý snímok
     */
    public static void calcOpticalFlow(ArrayList<Point> initialShape, VidData vidData, ArrayList<TrackedShape> shapes) {
        if (initialShape == null ||
            initialShape.size() < 3 ||
            vidData == null) {
            
            return;
        }
               
        TrackedShape shape = new TrackedShape();
        // uloženie prvého odhadnutého tvaru - body vyznačené užívateľom
        // ako počiatočný tvar pre sledovanie
        ArrayList<Point> divSplinePoints;
        divSplinePoints = CatmullRom.divideSpline(
                                     CatmullRom.calculateSpline(initialShape),
                                                                Constants.SPLINE_DIV_INTERVALS);
        shape.getControlPoints().addAll(divSplinePoints);
        shape.calcSplinePoints();    
        // index aktuálneho snímku
        int initFrameNo = vidData.getFrameNo();
        shapes.set(initFrameNo, shape);
        
        // štruktúry pre zarovnávanie tvaru
        Mat H = new Mat(Constants.SPACE_DIMENSION,
                        Constants.SPACE_DIMENSION,
                        CvType.CV_64F);
        Mat R = new Mat(Constants.SPACE_DIMENSION,
                        Constants.SPACE_DIMENSION,
                        CvType.CV_64F);
        Mat t = new Mat(Constants.SPACE_DIMENSION, 1, CvType.CV_64F);
        
        // body tvaru v aktuálnom snímku
        MatOfPoint2f currPts = new MatOfPoint2f();
        currPts.alloc(divSplinePoints.size());
        // prevod vyznačeného počiatočného tvaru z Point na dvojkanálovú maticu float
        MatUtils.ptsToMatOfPts2f(divSplinePoints, currPts);
        
        // body tvaru očakávané v nasledujúcom snímku
        MatOfPoint2f nextPts = new MatOfPoint2f();
        nextPts.alloc(divSplinePoints.size());

        // vektor chýb
        MatOfByte state = new MatOfByte();
        
        // šedotónové snímky, súčasný a nasledujúci
        Mat currGrayImg, nextGrayImg;
        
        Size winSize = new Size(17, 17);
        
        for (int i = initFrameNo; i < vidData.getFrameCnt() - 1; i++) {
            currGrayImg = vidData.getGrayFrame(i);
            nextGrayImg = vidData.getGrayFrame(i + 1);

            Video.calcOpticalFlowPyrLK(currGrayImg,
                                       nextGrayImg,
                                       currPts,
                                       nextPts,
                                       state,
                                       new MatOfFloat(),
                                       winSize,
                                       2);
            
            shape = getPlausibleTrackedShape(nextPts, H, R, t);
            shape.calcSplinePoints();
            
            shapes.set(i + 1, shape);
            
            nextPts.copyTo(currPts);
        }
        
        // ak sme nezačínali od prvého snímku, musíme spracovať aj snímky naľavo
        // od počiatočného
        if (initFrameNo > 0) {
            MatUtils.ptsToMatOfPts2f(divSplinePoints, currPts);
            for (int i = initFrameNo; i > 0; i--) {
                currGrayImg = vidData.getGrayFrame(i);
                nextGrayImg = vidData.getGrayFrame(i - 1);

                Video.calcOpticalFlowPyrLK(currGrayImg,
                                          nextGrayImg,
                                          currPts,
                                          nextPts,
                                          state,
                                          new MatOfFloat(),
                                          winSize,
                                          2);
                
                shape = getPlausibleTrackedShape(nextPts, H, R, t);
                shape.calcSplinePoints();

                shapes.set(i - 1, shape);
                
                nextPts.copyTo(currPts);
            }
        }
        
    }

    private static TrackedShape getPlausibleTrackedShape(MatOfPoint2f y,
                                                  Mat H,
                                                  Mat R,
                                                  Mat t) {
        // výsledok
        TrackedShape shape = new TrackedShape();
        
        PCA pca = PCA.getInstance();
        if (!pca.isInitialized()) {
            return null;
        }
        
        // tvar, ktorý sa iteráciami mení, prispôsobuje, zjemňuje, na začiatku mean
        Mat xMat = new Mat(Constants.SPLINE_DIV_INTERVALS + 1,
                              Constants.SPACE_DIMENSION,
                              CvType.CV_64F);
        
        MatUtils.pcaMatToMat(pca.getMeanShape(), xMat);
        
        // tvar, ktorý je na začiatku cyklu vždy rovnakký ako ten vysledovaný
        // optickým tokom, podobnostnou transformáciou sa zmení, ale vždy
        // vráti na pôvodný, updatuje tvar X
        Mat yMat = new Mat(y.rows(),
                           Constants.SPACE_DIMENSION,
                           CvType.CV_64F);
        
        MatUtils.matOfPoint2fToMat(y, yMat);

        // nemení sa
        double[] initCentroid = Procrustes.calculateShapeCentroid(yMat);
        Mat initCenteredForH = Procrustes.centeredPts(yMat, initCentroid);
        
        double[] shapeCentroid;
        Mat shapeCenteredForH;
        
        Mat initShape = yMat.clone();
        
        double err;
        int cnt = 0;
        do {
            yMat = initShape.clone();
            cnt++;
            
            shapeCentroid = Procrustes.calculateShapeCentroid(xMat);
            shapeCenteredForH = Procrustes.centeredPts(xMat, shapeCentroid);
                    
            // výpočet kovariančnej matice
            Core.gemm(initCenteredForH.t(), shapeCenteredForH, 1, new Mat(), 0, H);
            // výpočet rotačnej matice R
            Procrustes.getRotationMat(H, R);
            // výpočet translačného vektoru t
            Procrustes.getTranslationVec(shapeCentroid, initCentroid, R, t);

            // rotácia a translácia na priemerný tvar trénovacej množiny
            Mat rotatedShapePt = new Mat(Constants.SPACE_DIMENSION, 1, CvType.CV_64F);
            for (int i = 0; i < yMat.rows(); i++) {
                Core.gemm(R, yMat.row(i).t(), 1, new Mat(), 0, rotatedShapePt);
                yMat.put(i, 0, rotatedShapePt.get(0, 0)[0] + t.get(0, 0)[0]);
                yMat.put(i, 1, rotatedShapePt.get(1, 0)[0] + t.get(1, 0)[0]);
            }

            // prevod 2D tvaru na vektor pre PCA analýzu
            Mat shapePcaMat = new Mat(1, yMat.rows() * 2, CvType.CV_64F);
            MatUtils.matToPCAMat(yMat, shapePcaMat);

            // PCA projekcia, úprava koeficientov - normalizácia a spätná PCA projekcia
            pca.pca(shapePcaMat);

            // spätný prevod vektor pre PCA analýzu na 2D tvar pre rotáciu a transláciu
            MatUtils.pcaMatToMat(shapePcaMat, yMat);
           
            err = Procrustes.calculateErr(yMat, xMat);
            xMat = yMat.clone();
        
        } while (err * err > 1);
        
        // spätná rotácia a translácia
        R = R.t();
        Mat rotatedShapePt = new Mat(Constants.SPACE_DIMENSION, 1, CvType.CV_64F);
        for (int i = 0; i < yMat.rows(); i++) {
            Core.gemm(R, yMat.row(i).t(), 1, new Mat(), 0, rotatedShapePt);
            yMat.put(i, 0, rotatedShapePt.get(0, 0)[0] - (R.get(0, 0)[0]*t.get(0, 0)[0] + R.get(0, 1)[0]*t.get(1, 0)[0]));
            yMat.put(i, 1, rotatedShapePt.get(1, 0)[0] - (R.get(1, 0)[0]*t.get(0, 0)[0] + R.get(1, 1)[0]*t.get(1, 0)[0]));
        }
        
        // prevod matice na zobrazitelné body
        ArrayList<Point> shapePoints = new ArrayList<Point>(yMat.rows());
        MatUtils.matToPts(yMat, shapePoints);
        
        shape.getControlPoints().addAll(shapePoints);

        return shape;
    }
}
