package cz.vutbr.fit.xstrec01.Stechocard.Video;

import cz.vutbr.fit.xstrec01.Stechocard.GUI.VidFrame;
import javax.swing.JSlider;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class VidPlayer implements Runnable {
    
    private static boolean playing = false;
    private static int pos = 0;
    private final VidData vid;
    private final VidFrame framePane;
    private final JSlider slider;
    
    public VidPlayer(VidData vid, VidFrame framePane, JSlider slider) {
        this.vid = vid;
        this.framePane = framePane;
        this.slider = slider;
    }

    @Override
    public void run() {
        int frameNo = pos == vid.getFrameCnt() ? 0 : pos;
        int sleepTime = (int)Math.round(1/vid.getFramerate()*1000);
        while (playing && frameNo < vid.getFrameCnt()) {
            framePane.setImg(vid.getFrame(frameNo));
            pos = frameNo;
            slider.setValue(frameNo);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                
            }
            frameNo++;
        }
    }
    
    public static void setPlaying(boolean val) {
        playing = val;
    }
    
    public static void resetVidPos() {
        pos = 0;
    }
    
    public static void setPos(int curentPos) {
        pos = curentPos;
    }
    
    public static int getPos() {
        return pos;
    }
}
