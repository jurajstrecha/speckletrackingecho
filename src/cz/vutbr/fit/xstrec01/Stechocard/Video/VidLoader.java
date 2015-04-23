package cz.vutbr.fit.xstrec01.Stechocard.Video;

import org.opencv.core.Mat;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_WIDTH;
import org.opencv.highgui.VideoCapture;
import static cz.vutbr.fit.xstrec01.Stechocard.App.Constants.CV_CAP_PROP_FPS;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Otvori video subor na zadanej ceste, ulozi informacie o videu a do datovej
 * struktury VidData ulozi snimky prekonvertovane z matice na bufferovany
 * obrazok.
  
 * @author Bc. Juraj Strecha
 */
final public class VidLoader {
    
    public static boolean load(String path, VidData vidData) {
        VideoCapture cap;
        try {
            cap = new VideoCapture(path);
        } catch(Exception e) {
            return false;
        }
        if (!cap.isOpened()) {
            return false;
        } else {
            if (vidData == null) {
                vidData = VidData.getInstance();
            } else {
                vidData.reset();
            }
            
            Mat frameMat = new Mat();
            cap.read(frameMat);
            
            double frameRate = cap.get(CV_CAP_PROP_FPS);
            int frameWidth = (int)cap.get(CV_CAP_PROP_FRAME_WIDTH);
            int frameHeight = (int)cap.get(CV_CAP_PROP_FRAME_HEIGHT);
            int imgType = frameMat.channels() > 1 ?
                      BufferedImage.TYPE_3BYTE_BGR :
                      BufferedImage.TYPE_BYTE_GRAY;

            vidData.setFramerate(frameRate);
            vidData.setFrameWidth(frameWidth);
            vidData.setFrameHeight(frameHeight);
            
            do {
                vidData.addFrame(matToImg(frameMat, frameWidth, frameHeight, imgType));
            } while (cap.read(frameMat));
                  
            
            cap.release();
            return true;
        }
    }
    
    public static Image matToImg(Mat capFrame, int frameWidth, int frameHeight, int imgType) {
        BufferedImage image = new BufferedImage(frameWidth, frameHeight, imgType);
        int bufferSize = capFrame.channels() * frameWidth * frameHeight;
        final byte[] imBuffer = new byte[bufferSize];
        capFrame.get(0, 0, imBuffer);
        final byte[] imPixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(imBuffer, 0, imPixels, 0, imBuffer.length); 
        
        return image;
    }

}
