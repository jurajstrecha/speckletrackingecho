package cz.vutbr.fit.xstrec01.Stechocard.App;

import java.awt.Color;
import java.awt.Dimension;
import org.opencv.core.Size;

/**
 * Konštanty používané na rôznych miestach aplikácie, je možné pomocou nich
 * upraviť vzhľad programu a prispôsobiť funkčnosť.
 * 
 * @author Juraj Strecha, xstrec01
 */
public class Constants {    
    // množstvo variability, ktoré je povolené pre zmenu koeficientov modelu
    public static final double SIGMA_MULTIPLIER = 2;
    // počet intervalov, na koľko bude rozdelený spline pre získanie rovnomerne
    // rozloženych bodov, počet bodov je vždy SPLINE_DIV_INTERVALS + 1
    public static final int SPLINE_DIV_INTERVALS = 8;
    public static final Size OPTICAL_FLOW_WIN_SIZE = new Size(13, 13);
    public static final int OPTICAL_FLOW_PYRAMID_HEIGHT = 1;

    
    public static final String APP_NAME = "Spcekle Tracking Echocardiography";
    public static String MODEL_FILE_PATH = "res/model.json";
    
    // mody aplikacie
    public static final int MODE_SHAPES = 1;
    public static final int MODE_TRACKING = 2;
    
    // množstvo zmien, ktoré chceme metódou PCA popísať, ovplyvňuje počet
    // eigenvektorov, ktoré vráti Core.PCACompute
    public static final double PCA_VARIANCE_TO_RETAIN  = 0.98;   
    
    // OpenCV konštanty chýbajuce v Java wrapperoch
    public static final int CV_CAP_PROP_FPS = 5;
    
    // farby prvkov užívateľského rozhrania a prvkov na plátne
    public static final Color BACKGROUND_COLOR = new Color(52, 73, 94);
    public static final Color CANVAS_COLOR = new Color(236, 240, 241);
    public static final Color SPLINE_COLOR = new Color(155, 89, 182);
    public static final Color TRANSLUCENT_CANVAS_COLOR = new Color(0, 0, 0, 100);
    public static final Color CONTROL_POINT_COLOR = new Color(211, 84, 0);
    
    // konštanty pre nastavenie GUI
    public static final Dimension APP_WINDOW_MIN_SIZE = new Dimension(840, 620);
    public static final Dimension CANVAS_SIZE = new Dimension(640, 480);
    public static final Dimension CTRL_BUTTONS_SIZE = new Dimension(150, 30);
    public static final Dimension CTRL_BUTTONS_GAP = new Dimension(0, 10);
    public static final Dimension SLIDER_DIM = new Dimension(550, 20);
    public static final Dimension LOADING_DIALOG_DIM = new Dimension(300, 150);
    public static final Dimension HELP_WINDOW_SIZE = new Dimension(800,480);

    // rozmer riešeného problému, pre 2D STE bude vždy 2
    public static final int SPACE_DIMENSION = 2;

    // rozmery značky vyznačeného bodu v obraze v pixeloch
    public static final int CROSS_VERT_DIAMETER = 4;
    public static final int CROSS_HORIZ_DIAMETER = 4;
    // hrúbka čiary pre vykresleny spline
    public static final int SPLINE_THICKNESS = 2;
    
    // počet bodov intervalu pri výpočte spline
    public static final int SPLINE_SAMPLES_PER_SPAN = 20;
    
    // veľkost kroku (v snímkoch) tlačidiel GUI '<<' a '>>'
    public static final int FRAME_ADJ_STEP = 5;
    // zvyšovanie hodnoty spomaľuje prehrávanie, znižovanie zrýchľuje
    public static final int FRAMERATE_ADJUSTMENT = 400;
    // text indikátoru počtu anotovaných tvarov
    public static final String SHAPE_CNT_LABEL_TEXT = "Shapes: ";
}
