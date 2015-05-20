package cz.vutbr.fit.xstrec01.Stechocard.Video;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.GUI.Canvas;
import cz.vutbr.fit.xstrec01.Stechocard.GUI.PlayButton;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.TrackedShape;
import java.util.ArrayList;
import javax.swing.JSlider;

/**
 * Riadi prehrávanie snímkov videa a volá funkcie pre sledovanie vyznačených bodov
 * v móde sledovania.
 *
 * @author Juraj Strecha, xstrec01
 */
public final class VidPlayer implements Runnable {
    
    private volatile boolean playing = false;
    private boolean tracking = false;
    private final VidData vidData;
    private final Canvas canvas;
    private final JSlider vidSlider;
    private final PlayButton playButton;
    private final ArrayList<TrackedShape> trackedShapes;
    
    public VidPlayer(VidData vid, Canvas framePane, JSlider slider, PlayButton playButton, ArrayList<TrackedShape> trackedShapes) {
        this.vidData = vid;
        this.canvas = framePane;
        this.vidSlider = slider;
        this.playButton = playButton;
        this.trackedShapes = trackedShapes;
    }

    @Override
    public void run() {
        int frameNo = vidData.getFrameNo();
        int sleepTime = (int)Math.round(1/vidData.getFramerate()*Constants.FRAMERATE_ADJUSTMENT);
                
        while (playing && frameNo < vidData.getFrameCnt() - 1) {
            
            canvas.drawVideoFrame(vidData.getNextFrame());
            if (tracking) {
                canvas.setSplinePts(trackedShapes.get(frameNo).getSplinePoints());
            }
            frameNo = vidData.getFrameNo();
            vidSlider.setValue(frameNo);
            
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
