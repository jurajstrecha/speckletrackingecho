package cz.vutbr.fit.xstrec01.Stechocard.Video;

import java.awt.Image;
import java.util.ArrayList;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class VidData {
    private double framerate;
    private double frameWidth;
    private double frameHeight;
    private double frameCnt;
    private final ArrayList<Image> frames;
    private int frameNo;
    
    public VidData() {
        this.frames = new ArrayList<>();
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
    
    public double getFrameCnt() {
        return frameCnt;
    }
    
    public void setFrameCnt(double cnt) {
        frameCnt = cnt;
    }

    public int getFrameNo() {
        return frameNo;
    }
    
    public Image getFrame() {
        return frames.get(frameNo);
    }
    
    public Image getFrame(int i) {
        if (i > -1 && i < frames.size()) {
            frameNo = i;
            return frames.get(i);
        } else if (i < 0) {
            frameNo = 0;
            return frames.get(0);
        } else if (i >= frames.size()) {
            frameNo = frames.size() - 1;
            return frames.get(frames.size() - 1);
        } else {
            return null;
        }
    }
    
    public void clearFrames() {
        frames.clear();
        frameCnt = 0;
        frameNo = 0;
    }
    
    public void addFrame(Image frame) {
        frames.add(frame);
    }
    
    public void setFrameNo(int no) {
        frameNo = no;
    }
    
    public Image getNextFrame() {
        if (frameNo <= frameCnt) {
            Image ret = getFrame();
            frameNo++;
            return ret;
        } else {
            return null;
        }
    }
}
