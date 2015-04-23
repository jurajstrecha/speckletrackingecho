package cz.vutbr.fit.xstrec01.Stechocard.Video;

import java.awt.Image;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Trieda reprezentuje nacitane video. V zozname drzi vsetky snimky videa.
 * Uklada tiez obsluzne informacie ako rozmery, celkovy pocet snimkov, framerate
 * a cislo snimku, ktory je zobrazeny na platne, teda snimok, o ktory aplikacia
 * naposledy ziadala.
 * 
 * @author Bc. Juraj Strecha, xstrec01@stud.fit.vutbr.cz
 */
public final class VidData {
    private double framerate;
    private double frameWidth;
    private double frameHeight;
    private int frameCnt;
    private final ArrayList<Image> frames;
    private int frameNo;
    
    private static VidData instance = null;
    
    private static final Logger logger = Logger.getLogger(VidData.class.getName());
    
    private VidData() {
        this.frames = new ArrayList<>();
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
     * Vrati snimok, ktoreho poradove cislo
     * 
     * @return Snimok videa
     */
    public Image getCurrentFrame() {
        return frames.get(frameNo);
    }
    
    /**
     * Vrati i-ty snimok. Indexovane od 0.
     * 
     * @param i Cislo snimku
     * @return Snimok na i-tom indexe, ak existuje, inak null
     */
    public Image getFrame(int i) {
        if (i < 0 || i >= frames.size()) {
            logger.log(Level.SEVERE, "Index {0} out of range, max: {1}", new Object[]{i, frameCnt});
            return null;
        }
        frameNo = i;            
        return frames.get(i);
    }
    
    public void reset() {
        frames.clear();
        framerate = 0;
        frameWidth = 0;
        frameHeight = 0;
        frameCnt = 0;
        frameNo = 0;
    }
    
    /**
     * Vlozi novy snimok do zoznamu. Pouziva sa pri nahravani videa.
     * 
     * @param frame Snimok, ktory bude pridany do zoznamu
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
     * Nastavi zoznam snimkov na poskytnuty zoznam.
     * 
     * @param newFrames Zoznam novych snimkov
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
     * Vrati dalsi snimok v poradi. Po dosiahnuti konca videa vracia posledny
     * snimok.
     * 
     * @return Snimok videa
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
}
