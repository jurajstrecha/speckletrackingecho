package cz.vutbr.fit.xstrec01.Stechocard.Video;

import java.awt.Image;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Mat;

/**
 * Trieda reprezentuje načítané video. V zozname drží všetky snímky videa.
 * Ukladá tiež obslužné informácie ako rozmery videa, počet snímkov, framerate
 * a číslo snímku, ktorý je zobrazený na plátne, teda snímok, o ktorý aplikácia
 * naposledy žiadala.
 * 
 * @author Juraj Strecha, xstrec01
 */
public final class VidData {
    private double framerate;
    private double frameWidth;
    private double frameHeight;
    private int frameCnt;
    private final ArrayList<Image> frames;
    private final ArrayList<Mat> grayFrames;
    private int frameNo;
    
    private static VidData instance = null;
    
    private static final Logger logger = Logger.getLogger(VidData.class.getName());
    
    private VidData() {
        frames = new ArrayList<Image>();
        grayFrames = new ArrayList<Mat>();
    }
    
    public static VidData getInstance() {
        if (instance == null) {
            instance = new VidData();
        }
        return instance;
    }
    
    public double getFramerate() {
        return this.framerate;
    }
    
    public void setFramerate(double rate) {
        framerate = rate;
    }
    
    public double getFrameWidth() {
        return this.frameWidth;
    }
    
    public void setFrameWidth(double width) {
        frameWidth = width;
    }
    
    public double getFrameHeight() {
        return this.frameHeight;
    }
    
    public void setFrameHeight(double height) {
        frameHeight = height;
    }
    
    public int getFrameCnt() {
        return frameCnt;
    }

    public int getFrameNo() {
        return frameNo;
    }
    
    /**
     * Vráti snímok, ktorého poradové číslo je aktuálne nastavené.
     * 
     * @return snímok videa
     */
    public Image getCurrentFrame() {
        return frames.get(frameNo);
    }
    
    /**
     * Vráti i-ty snímok. Indexované od 0.
     * 
     * @param i číslo snímku
     * @return snímok na i-tom indexe, ak existuje, inak null
     */
    public Image getFrame(int i) {
        if (i < 0 || i >= frames.size()) {
            logger.log(Level.SEVERE, "Index {0} out of range, max: {1}", new Object[]{i, frameCnt});
            return null;
        }
        frameNo = i;            
        return frames.get(i);
    }
    
    /**
     * Vráti i-ty šedotónový snímok. Indexované od 0.
     * 
     * @param i číslo snímku
     * @return snímok na i-tom indexe, ak existuje, inak null
     */
    public Mat getGrayFrame(int i) {
        if (i < 0 || i >= frames.size()) {
            logger.log(Level.SEVERE, "Index {0} out of range, max: {1}", new Object[]{i, frameCnt});
            return null;
        }
   
        return grayFrames.get(i);        
    }
    
    public void reset() {
        frames.clear();
        grayFrames.clear();
        framerate = 0;
        frameWidth = 0;
        frameHeight = 0;
        frameCnt = 0;
        frameNo = 0;
    }
    
    /**
     * Vloží nový snímok do zoznamu. Používa sa pri nahrávaní videa.
     * 
     * @param frame snímok, ktorý bude pridaný do zoznamu
     */
    public void addFrame(Image frame) {
        if (frame == null) {
            logger.log(Level.SEVERE, "Adding empty frame");
        }
        else {
            frames.add(frame);
            frameCnt++;
        }
    }
    
    /**
     * Vloží nový šedotónový snímok do zoznamu. Používa sa pri nahrávaní videa.
     * 
     * @param frame snímok, ktorý bude pridaný do zoznamu
     */
    public void addGrayFrame(Mat frame) {
        if (frame == null) {
            logger.log(Level.SEVERE, "Adding empty frame");
        }
        else {
            grayFrames.add(frame);
        }
    }

    /**
     * Nastaví zoznam snímkov na poskytnutý zoznam.
     * 
     * @param newFrames zoznam nových snímkov
     */
    public void setFrames(ArrayList<Image> newFrames) {
        if (newFrames == null || newFrames.size() < 1) {
            logger.log(Level.SEVERE, "Adding empty frame list");
        }
        else {
            frames.clear();
            frameCnt = 0;
            for (Image frame: newFrames) {
                addFrame(frame);
            }
        }
    }
    
    /**
     * Vráti ďalší snímok v poradí. Po dosiahnutí konca videa vracia posledný
     * snímok.
     * 
     * @return snímok videa
     */
    public Image getNextFrame() {
        if (frameNo < frameCnt) {
            Image frame = getCurrentFrame();
            frameNo++;
            return frame;
        } else {
            logger.log(Level.SEVERE, "Frame number exceeded, returning last frame in list");
            frameNo = (int)frameCnt - 1;
            return getCurrentFrame();
        }
    }
    
    /**
     * Overí, či bolo načítané video.
     * 
     * @return true, ak bolo video načítané, inak false
     */
    public boolean isLoaded() {
        return !frames.isEmpty();
    }
    
}
