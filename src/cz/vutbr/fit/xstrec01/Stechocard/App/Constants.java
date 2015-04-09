package cz.vutbr.fit.xstrec01.Stechocard.App;

import java.awt.Color;
import java.awt.Dimension;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class Constants {
    /* App modes */
    public static final int MODE_SHAPES = 1;
    public static final int MODE_TRACKING = 2;
    
    public static final String appName = "Echocardiography Video Loading Testbed";
    public static final int CV_CAP_PROP_FPS = 5;
    public static final int CV_CAP_PROP_FRAME_COUNT = 7;
    public static final int vidWidth = 636;
    public static final int vidHeight = 434;
    
    /* colors */
    public static final Color BACKGROUND_COLOR = new Color(52, 73, 94);
    public static final Color CANVAS_COLOR = new Color(236, 240, 241);
    public static final Color SPLINE_COLOR = new Color(155, 89, 182);
    public static final Color TRANSLUCENT_CANVAS_COLOR = new Color(0, 0, 0, 100);
    public static final Color CONTROL_POINT_COLOR = new Color(211, 84, 0);
    
    /* dimensions */
    public static final Dimension APP_WINDOW_MIN_SIZE = new Dimension(840, 620);
    public static final Dimension CANVAS_SIZE = new Dimension(640, 480);
    public static final Dimension CTRL_BUTTONS_SIZE = new Dimension(110, 30);
    public static final Dimension CTRL_BUTTONS_GAP = new Dimension(0, 10);
    public static final Dimension SLIDER_DIM = new Dimension(550, 20);
    
    /* various numerical values */
    public static final int CROSS_VERT_DIAMETER = 4;
    public static final int CROSS_HORIZ_DIAMETER = 4;
    public static final int SPLINE_SAMPLES_PER_SPAN = 20;
    public static final int SPLINE_THICKNESS = 2;
    public static final float ALIGN_CENTER = 0.5f;    
    public static final int SPLINE_DIV_INTERVALS = 10;
    public static final int FRAME_ADJ_STEP = 5;
    // increasing the value slows the playback, decreasing speeds the playback up
    public static final int FRAMERATE_ADJUSTMENT = 400;
}
