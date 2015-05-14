package cz.vutbr.fit.xstrec01.Stechocard.Video;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.GUI.Canvas;
import cz.vutbr.fit.xstrec01.Stechocard.GUI.PlayButton;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.PCA;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.Utils;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import javax.swing.JSlider;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.video.Video;

/**
 * Riadi prehrávanie snímkov videa a volá funkcie pre sledovanie vyznačených bodov
 * v móde sledovania.
 *
 * @author Juraj Strecha, xstrec01
 */
public class VidPlayer implements Runnable {
    
    private volatile boolean playing = false;
    private boolean tracking = false;
    private final VidData vidData;
    private final Canvas canvas;
    private final JSlider slider;
    private final PlayButton playButton;
    
    public VidPlayer(VidData vid, Canvas framePane, JSlider slider, PlayButton playButton) {
        this.vidData = vid;
        this.canvas = framePane;
        this.slider = slider;
        this.playButton = playButton;
    }

    @Override
    public void run() {
        int frameNo = vidData.getFrameNo();
        int sleepTime = (int)Math.round(1/vidData.getFramerate()*Constants.FRAMERATE_ADJUSTMENT);
        
        ArrayList<Point> ctrlPts = canvas.getControlPts();
        MatOfPoint2f pts = new MatOfPoint2f();
        pts.alloc(ctrlPts.size());        
        Utils.ptsToMatOfPts2f(ctrlPts, pts);
        MatOfPoint2f nextPts = new MatOfPoint2f();
        nextPts.alloc(ctrlPts.size());
        MatOfByte state = new MatOfByte();
        Image frame;
        Mat grayImg;
        Mat nextGrayImg;
                
        while (playing && frameNo < vidData.getFrameCnt() - 1) {
            
            if (tracking) {
                grayImg = vidData.getCurrentGrayFrame();
                frame = vidData.getNextFrame();
                nextGrayImg = vidData.getCurrentGrayFrame();
                if (!ctrlPts.isEmpty()) {
                    // =============== Lucas-Kanade optical flow ===============
                    Video.calcOpticalFlowPyrLK(grayImg,
                                               nextGrayImg,
                                               pts,
                                               nextPts,
                                               state,
                                               new MatOfFloat());
                }
                
                PCA.getInstance().getPlausibleShape(nextPts);
                
                Utils.matOfPts2fToPts(nextPts, ctrlPts);
                nextPts.copyTo(pts);
                canvas.drawVideoFrame(frame);
            } else {
                canvas.drawVideoFrame(vidData.getNextFrame());
            }
            
            frameNo = vidData.getFrameNo();
            slider.setValue(frameNo);
            
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                break;
            }
        }
        
        if (frameNo >= vidData.getFrameCnt() - 1) {
            playButton.setPlay(false);
        }
    }
    
    /**
     * Nastaví príznak prehrávania signálom od tlačítka Play.
     * 
     * @param val true, ak užívateľ stlačil tlačidlo Play a chce prehrávať, inak false
     */
    public void setPlaying(boolean val) {
        playing = val;
    }
    
    /**
     * Poznačí, či je aplikácia v móde sledovania pohybu. Ak nie je nastavené,
     * prehrávač nesleduje body, iba prehráva snímky videa.
     * 
     * @param val true, ak je zapnutý mód sledovania bodov, inak false
     */
    public void setTracking(boolean val) {
        tracking = val;
    }
}
