package cz.vutbr.fit.xstrec01.Stechocard.Video;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.GUI.Canvas;
import javax.swing.JSlider;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class VidPlayer implements Runnable {
    
    private volatile boolean playing = false;
    private final VidData vidData;
    private final Canvas canvas;
    private final JSlider slider;
    
    public VidPlayer(VidData vid, Canvas framePane, JSlider slider) {
        this.vidData = vid;
        this.canvas = framePane;
        this.slider = slider;
    }

    @Override
    public void run() {
        int frameNo = vidData.getFrameNo();
        int sleepTime = (int)Math.round(1/vidData.getFramerate()*Constants.FRAMERATE_ADJUSTMENT);
        while (playing && frameNo < vidData.getFrameCnt()) {
            canvas.drawVideoFrame(vidData.getNextFrame());
            slider.setValue(frameNo);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                break;
            }
            frameNo = vidData.getFrameNo();
        }
    }
    
    public void setPlaying(boolean val) {
        playing = val;
    }
}
