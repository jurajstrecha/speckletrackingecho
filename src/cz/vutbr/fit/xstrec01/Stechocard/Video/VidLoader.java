package cz.vutbr.fit.xstrec01.Stechocard.Video;

import javax.swing.JDialog;
import org.opencv.core.Mat;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_WIDTH;
import org.opencv.highgui.VideoCapture;
import static cz.vutbr.fit.xstrec01.Stechocard.App.Constants.CV_CAP_PROP_FPS;
import static cz.vutbr.fit.xstrec01.Stechocard.App.Constants.CV_CAP_PROP_FRAME_COUNT;

/**
 *
 * @author Bc. Juraj Strecha
 */
final public class VidLoader implements Runnable {
    private final String path;
    private final VidData vidData;
    private final JDialog loadingDialog;
    
    public VidLoader(String path, VidData vidData, JDialog loadingDialog) {
        this.path = path;
        this.vidData = vidData;
        this.loadingDialog = loadingDialog;
    }

    @Override
    public void run() {
        VideoCapture cap = new VideoCapture(path);
        if (!cap.isOpened()) {
            System.err.println("Unable to load the video");
        } else {
            
            Mat frameMat = new Mat();
            cap.read(frameMat);

            vidData.setFramerate(cap.get(CV_CAP_PROP_FPS));
            vidData.setFrameCnt(cap.get(CV_CAP_PROP_FRAME_COUNT));
            vidData.setFrameWidth(cap.get(CV_CAP_PROP_FRAME_WIDTH));
            vidData.setFrameHeight(cap.get(CV_CAP_PROP_FRAME_HEIGHT));
            
            do {
                vidData.addFrame(frameMat.clone());
            } while (cap.read(frameMat));
                  
            
            cap.release();
            loadingDialog.dispose();
        }
    }

}
