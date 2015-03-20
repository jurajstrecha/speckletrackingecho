package cz.vutbr.fit.xstrec01.Stechocard.Video;

import java.util.ArrayList;
import org.opencv.core.Mat;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class VidData {
    private double framerate;
    private double frameWidth;
    private double frameHeight;
    private double frameCnt;
    private final ArrayList<Mat> frames;
    private int frameNo;
    
    public VidData() {
        this.frames = new ArrayList<>();
        this.frameCnt = 0;
        this.frameNo = 0;
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
    
    public Mat getFrame(int i) {
        if (i > -1 && i < frames.size()) {
            return frames.get(i);
        } else if (i < 0) {
            return frames.get(0);
        } else if (i >= frames.size()) {
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
    
    public void addFrame(Mat frame) {
        frames.add(frame);
    }
    
    public void setFrameNo(int no) {
        frameNo = no;
    }
    
    public Mat getNextFrame() {
        if (frameNo <= frameCnt) {
            Mat ret = getFrame();
            frameNo++;
            return ret;
        } else {
            return null;
        }
    }
    
    public Mat getFrame() {
        return frames.get(frameNo);
    }
}
