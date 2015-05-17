package cz.vutbr.fit.xstrec01.Stechocard.Video;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.CatmullRom;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.PCA;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.Procrustes;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.Shapes;
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
import org.opencv.video.Video;

/**
 * 
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
        
        PCA pca = PCA.getInstance();
        
        if (!pca.isInitialized()) {
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
        
        // všetky vypočítané tvary zarovnávame na priemerný tvar trénovacej množiny
        double[] meanCentroid;
        Mat meanCenteredForH;
        Mat meanMat = new Mat(divSplinePoints.size(), Constants.SPACE_DIMENSION, CvType.CV_64F);
        MatUtils.pcaMatToMat(pca.getMeanShape(), meanMat);
        meanCentroid = Procrustes.calculateShapeCentroid(meanMat);
        meanCenteredForH = Procrustes.centeredPts(meanMat, meanCentroid);
        
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
        
        for (int i = initFrameNo; i < vidData.getFrameCnt() - 1; i++) {
            currGrayImg = vidData.getGrayFrame(i);
            nextGrayImg = vidData.getGrayFrame(i + 1);

            Video.calcOpticalFlowPyrLK(currGrayImg,
                                       nextGrayImg,
                                       currPts,
                                       nextPts,
                                       state,
                                       new MatOfFloat());
            
            shape = getPlausibleTrackedShape(nextPts, H, R, t, meanCentroid, meanCenteredForH);
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
                                          new MatOfFloat());
                
                shape = getPlausibleTrackedShape(nextPts, H, R, t, meanCentroid, meanCenteredForH);
                shape.calcSplinePoints();

                shapes.set(i - 1, shape);
                
                nextPts.copyTo(currPts);
            }
        }
        
    }

    private static TrackedShape getPlausibleTrackedShape(MatOfPoint2f trackedPoints,
                                                  Mat H,
                                                  Mat R,
                                                  Mat t,
                                                  double[] meanCentroid,
                                                  Mat meanCenteredForH) {
        TrackedShape shape = new TrackedShape();
        Mat shapeMat = new Mat(trackedPoints.rows(), Constants.SPACE_DIMENSION, CvType.CV_64F);
        MatUtils.matOfPoint2fToMat(trackedPoints, shapeMat);
        double[] shapeCentroid = Procrustes.calculateShapeCentroid(shapeMat);
        Mat shapeCenteredForH = Procrustes.centeredPts(shapeMat, shapeCentroid);

        // výpočet kovariančnej matice
        Core.gemm(shapeCenteredForH.t(), meanCenteredForH, 1, new Mat(), 0, H);
        // výpočet rotačnej matice R
        Procrustes.getRotationMat(H, R);
        // výpočet translačného vektoru t
        Procrustes.getTranslationVec(meanCentroid, shapeCentroid, R, t);

        // rotácia a translácia na priemerný tvar trénovacej množiny
        Mat rotatedShapePt = new Mat(Constants.SPACE_DIMENSION, 1, CvType.CV_64F);
        for (int i = 0; i < shapeMat.rows(); i++) {
            Core.gemm(R, shapeMat.row(i).t(), 1, new Mat(), 0, rotatedShapePt);
            shapeMat.put(i, 0, rotatedShapePt.get(0, 0)[0] + t.get(0, 0)[0]);
            shapeMat.put(i, 1, rotatedShapePt.get(1, 0)[0] + t.get(1, 0)[0]);
        }

        // prevod 2D tvaru na vektor pre PCA analýzu
        Mat shapePcaMat = new Mat(1, shapeMat.rows() * 2, CvType.CV_64F);
        MatUtils.matToPCAMat(shapeMat, shapePcaMat);

        // PCA projekcia, úprava koeficientov - normalizácia a spätná PCA projekcia
        PCA pca = PCA.getInstance();
        if (!pca.isInitialized()) {
            return null;
        }
        pca.pca(shapePcaMat);
        
        // spätný prevod vektor pre PCA analýzu na 2D tvar pre rotáciu a transláciu
        MatUtils.pcaMatToMat(shapePcaMat, shapeMat);
        
        // spätná rotácia a translácia
        R = R.t();
        for (int i = 0; i < shapeMat.rows(); i++) {
            Core.gemm(R, shapeMat.row(i).t(), 1, new Mat(), 0, rotatedShapePt);
            shapeMat.put(i, 0, rotatedShapePt.get(0, 0)[0] - (R.get(0, 0)[0]*t.get(0, 0)[0] + R.get(0, 1)[0]*t.get(1, 0)[0]));
            shapeMat.put(i, 1, rotatedShapePt.get(1, 0)[0] - (R.get(1, 0)[0]*t.get(0, 0)[0] + R.get(1, 1)[0]*t.get(1, 0)[0]));
        }
        
        // prevod matice na zobrazitelné body
        ArrayList<Point> shapePoints = new ArrayList<Point>(shapeMat.rows());
        MatUtils.matToPts(shapeMat, shapePoints);
        
        shape.getControlPoints().addAll(shapePoints);

        return shape;
    }
}
