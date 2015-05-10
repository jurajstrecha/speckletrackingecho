package cz.vutbr.fit.xstrec01.Stechocard.App;

import java.awt.Color;
import java.awt.Dimension;

/**
 * Konštanty používané na rôznych miestach aplikácie, je možné pomocou nich
 * upraviť vzhľad programu a prispôsobiť funkčnosť.
 * 
 * @author Juraj Strecha, xstrec01
 */
public class Constants {
    public static final String APP_NAME = "Spcekle Tracking Echocardiography";
    
    // mody aplikacie
    public static final int MODE_SHAPES = 1;
    public static final int MODE_TRACKING = 2;
    
    // OpenCV konstanty chybajuce v Java wrapperoch
    public static final int CV_CAP_PROP_FPS = 5;
    
    // farby prvkov uzivatelskeho rozhrania a prvkov na platne
    public static final Color BACKGROUND_COLOR = new Color(52, 73, 94);
    public static final Color CANVAS_COLOR = new Color(236, 240, 241);
    public static final Color SPLINE_COLOR = new Color(155, 89, 182);
    public static final Color TRANSLUCENT_CANVAS_COLOR = new Color(0, 0, 0, 100);
    public static final Color CONTROL_POINT_COLOR = new Color(211, 84, 0);
    
    // konstanty pre nastavenie GUI
    public static final Dimension APP_WINDOW_MIN_SIZE = new Dimension(840, 620);
    public static final Dimension CANVAS_SIZE = new Dimension(640, 480);
    public static final Dimension CTRL_BUTTONS_SIZE = new Dimension(110, 30);
    public static final Dimension CTRL_BUTTONS_GAP = new Dimension(0, 10);
    public static final Dimension SLIDER_DIM = new Dimension(550, 20);
    public static final Dimension LOADING_DIALOG_DIM = new Dimension(300, 200);

    // rozmer rieseneho problemu, pre 2D STE bude vzdy 2
    public static final int SPACE_DIMENSION = 2;

    // rozmery znacky vyznaceneho bodu v obraze v pixeloch
    public static final int CROSS_VERT_DIAMETER = 4;
    public static final int CROSS_HORIZ_DIAMETER = 4;
    // hrubka ciary pre vykresleny spline
    public static final int SPLINE_THICKNESS = 2;
    
    // pocet bodov intervalu pri vypocte spline
    public static final int SPLINE_SAMPLES_PER_SPAN = 20;
    // pocet intervalov, na kolko bude rozdeleny spline pre ziskanie rovnomerne
    // rozlozenych bodov, pocet bodov je vzdy SPLINE_DIV_INTERVALS + 1
    public static final int SPLINE_DIV_INTERVALS = 10;
    
    // velkost kroku (v snimkoch) tlacidiel GUI << a >>
    public static final int FRAME_ADJ_STEP = 5;
    // zvysovanie hodnoty spolamuje prehravanie, znizovanie zrychluje
    public static final int FRAMERATE_ADJUSTMENT = 400;
}
