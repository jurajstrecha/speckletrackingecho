package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.App.ModelLoader;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.PCA;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.Procrustes;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.Shape;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.Shapes;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.TrackedShape;
import cz.vutbr.fit.xstrec01.Stechocard.Video.OpticalFlow;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidData;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidLoader;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidPlayer;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.opencv.core.Mat;

/**
 * Grafické užívateľské rozhranie. Riadenie udalostí generovaných prvkami
 * rozhrania. Volanie konštruktoru vytvorí okno aplikácie a nastaví prvky
 * riadenia programu tak, aby sa s aplikáciou dalo pracovať.
 * 
 * @author Juraj Strecha, xstrec01
 */
public final class AppInterface extends JFrame {
    private final Canvas canvas;
    private final Shapes shapes;
    private final VidData frames;
    private VidPlayer player;    
    private int mode;
    JMenuItem menuItemSave;
    JMenuItem menuItemLoad;
    ButtonGroup group;
    private JPanel vidControlsPane;
    private PlayButton buttonPlay;
    private JSlider vidSlider;
    private final JDialog loadingDialog;
    JPanel trackingControls;
    JPanel annotControls;
    JLabel shapeCntLabel;
    ArrayList<TrackedShape> trackedShapes;
    
    public AppInterface() {
        super(Constants.APP_NAME);

        // základné nastavenie okna aplikácie
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Constants.BACKGROUND_COLOR);
        setMinimumSize(Constants.APP_WINDOW_MIN_SIZE);
        
        createMenu();
        canvas = new Canvas();
        createComponents(getContentPane());
        
        // dialógové okno sa zobrazí pri načítavaní videa
        loadingDialog = new JDialog(this, "Loading", false);
        initLoadingDialog();
        
        // ovládanie videa, vypnuté, kým nie je video načítané
        enableControls(false);
        // počiatočným módom je anotovanie tvarov
        mode = Constants.MODE_SHAPES;
        
        // dátové štruktúry - tvary a snímky načítaného videa
        shapes = Shapes.getInstance();      
        frames = VidData.getInstance();
        trackedShapes = new ArrayList<TrackedShape>();
        ModelLoader.load(PCA.getInstance());

        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    /**
     * Vytvor, nastav a vlož do GUI komponenty.
     * 
     * @param pane hlavná plocha aplikačného rozhrania
     */
    private void createComponents(Container pane) {
        pane.setLayout(new BorderLayout());
        
        JPanel canvasPane = new JPanel();
        canvasPane.setBackground(Constants.BACKGROUND_COLOR);
        canvasPane.setLayout(new GridBagLayout());
        
        canvas.setBackground(Constants.CANVAS_COLOR);
        canvas.setPreferredSize(Constants.CANVAS_SIZE);        
        canvasPane.add(canvas);
        pane.add(canvasPane, BorderLayout.CENTER);
        
        vidControlsPane = new JPanel();
        vidControlsPane.setLayout(new BoxLayout(vidControlsPane, BoxLayout.Y_AXIS));

        JPanel vidSliderPane = new JPanel();
        vidSlider = new JSlider();
        initSlider(0, 0);
        vidSlider.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
                setFrame(vidSlider.getValue());
            }            
        });
        vidSlider.setPreferredSize(Constants.SLIDER_DIM);
        vidSliderPane.add(vidSlider);
        vidControlsPane.add(vidSliderPane);
        
        JPanel vidControlsHolder = new JPanel();
        JButton buttonRev = addButton("<<", vidControlsHolder);
        buttonRev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFrameRev();
            }            
        });
        
        buttonPlay = new PlayButton("Play");
        buttonPlay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buttonPlay.isPlay()) {
                    buttonPlay.setPlay(false);
                    player.setPlaying(false);
                    
                } else {
                    buttonPlay.setPlay(true);
                    if (player != null) {
                        player.setPlaying(true);
                        new Thread(player).start();
                    }
                }
            }            
        });
        vidControlsHolder.add(buttonPlay);
        
        JButton buttonFwd = addButton(">>", vidControlsHolder);
        buttonFwd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFrameFwd();
            }            
        });
        
        vidControlsPane.add(vidControlsHolder);

        pane.add(vidControlsPane, BorderLayout.SOUTH);

        annotControls = new JPanel();
        annotControls.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        annotControls.setLayout(new BoxLayout(annotControls ,BoxLayout.Y_AXIS));
        
        // tlačidlo pre potvrdenie anotovaného tvaru, ten sa uloží do zoznamu
        JButton buttonAddShape = addButton("Confirm Shape", annotControls);
        buttonAddShape.setMaximumSize(Constants.CTRL_BUTTONS_SIZE);
        buttonAddShape.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                Shape shape;
                ArrayList<Point> controlPts = canvas.getControlPts();
                if (controlPts.isEmpty()) {
                    System.err.println("No annotated points to store");
                } else {
                    shape = new Shape();
                    shape.addAnnotatedPoints(controlPts);
                    if (shapes.serializeShape(shape)) {
                        canvas.reset();
                        shapeCntLabel.setText(Constants.SHAPE_CNT_LABEL_TEXT + shapes.size());
                    } else {
                        System.err.println("Shape storing failed");
                    }
                }
            }
            
        });
        
        // zruší aktuálne vyznačené body zatiaľ nepotvrdeného tvaru
        annotControls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        JButton buttonReset = addButton("Reset Shape", annotControls);
        buttonReset.setMaximumSize(Constants.CTRL_BUTTONS_SIZE);
        buttonReset.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.reset();
            }
            
        });
        
        // zobrazenie počtu anotovaných tvarov v zozname, naklikaných alebo načítaných
        // zo súboru
        annotControls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        shapeCntLabel = new JLabel(Constants.SHAPE_CNT_LABEL_TEXT + 0, SwingConstants.CENTER);
        shapeCntLabel.setMaximumSize(Constants.CTRL_BUTTONS_SIZE);
        annotControls.add(shapeCntLabel);

        annotControls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        annotControls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        annotControls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        JButton buttonGenerate = addButton("Generate Model", annotControls);
        buttonGenerate.setMaximumSize(Constants.CTRL_BUTTONS_SIZE);
        buttonGenerate.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!frames.isLoaded()) {
                    JOptionPane.showMessageDialog(Frame.getFrames()[0],
                                                  "Load a video first");

                } else if (shapes.isEmpty()) {
                    JOptionPane.showMessageDialog(Frame.getFrames()[0],
                                                  "Annotate or load shapes before generating a model");
                } else if (shapes.size() == 1) {
                    JOptionPane.showMessageDialog(Frame.getFrames()[0],
                                                  "More than one shape needed for generating the model");                    
                } else {
                    generateModel();
                    setMode(Constants.MODE_TRACKING);
                    // ulož model natrvalo do súboru, odkiaľ sa vždy pri štarte načíta
                    ModelLoader.save(PCA.getInstance());
                }
            }            
        });
        
        annotControls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        JPanel controlsHolder = new JPanel();
        controlsHolder.add(annotControls);
        annotControls.setVisible(true);
        
        trackingControls = new JPanel();
        trackingControls.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        trackingControls.setLayout(new BoxLayout(trackingControls ,BoxLayout.Y_AXIS));
        
        // tlačidlo potvrdí vyznačený tvar, ktorý sa bude sledovať
        JButton buttonSetTrackingPts = addButton("Set tracking", trackingControls);
        buttonSetTrackingPts.setMaximumSize(Constants.CTRL_BUTTONS_SIZE);
        buttonSetTrackingPts.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // ak nebol označený žiaden bod na sledovanie
                if (!canvas.getControlPts().isEmpty()) {
                    loadingDialog.setVisible(true);
                    if (!trackedShapes.isEmpty()) {
                        trackedShapes.clear();
                    }
                    for (int i = 0; i < frames.getFrameCnt(); i++) {
                        trackedShapes.add(null);
                    }
                    OpticalFlow.calcOpticalFlow(canvas.getControlPts(), frames, trackedShapes);
                    player.setTracking(true);
                    canvas.setTracking(true);
                    canvas.reset();
                    canvas.setSplinePts(trackedShapes.get(frames.getFrameNo()).getSplinePoints());
                    canvas.setControlPts(trackedShapes.get(frames.getFrameNo()).getControlPoints());
                    canvas.repaint();
                    loadingDialog.setVisible(false);
                }
            }
            
        });
        trackingControls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        
        // tlačidlo zruší aktuálne sledovaný tvar, vymaže body z plátna
        JButton buttonTrackingReset = addButton("Reset", trackingControls);
        buttonTrackingReset.setMaximumSize(Constants.CTRL_BUTTONS_SIZE);
        buttonTrackingReset.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                resetTracking();
            }
            
        });
        trackingControls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        controlsHolder.add(trackingControls);
        trackingControls.setVisible(false);
        
        pane.add(controlsHolder, BorderLayout.EAST);        
    }
    
    /**
     * Vygeneruje model z naanotovaných alebo načítaných tvarov.
     */
    private void generateModel() {
        ArrayList<Mat> alignedShapes = Procrustes.analyze(shapes);
        PCA pca = PCA.getInstance();
        pca.init(alignedShapes);
    }
    
    /**
     * Vytvorí dialógové okno, ktoré sa zobrazí pri nahrávaní videa.
     */
    private void initLoadingDialog() {
        loadingDialog.setPreferredSize(Constants.LOADING_DIALOG_DIM);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(null);
    }
    
    /**
     * Vytvorí tlačítko s textom a pridá ho na plochu, ktorá bola zadaná.
     * 
     * @param container plocha, kam sa má tlačítko pridať
     * @param text text zobrazený na tlačítku
     */
    private static JButton addButton(String text, Container container) {
        JButton button = new JButton(text);
        container.add(button);
        return button;
    }

    /**
     * Inicializuje menu aplikácie.
     */
    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu mainMenu = new JMenu("Application");
        menuBar.add(mainMenu);
        
        JMenuItem menuItemOpen = new JMenuItem("Open Video");
        menuItemOpen.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                openVideo();
            }            
        });
        mainMenu.add(menuItemOpen); 
        mainMenu.addSeparator();
        
        menuItemSave = new JMenuItem("Save Annotated Shapes");
        menuItemSave.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                saveShapes();
            }            
        });
        menuItemSave.setEnabled(false);
        mainMenu.add(menuItemSave);

        menuItemLoad = new JMenuItem("Load Annotated Shapes");
        menuItemLoad.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                loadShapes();
            }            
        });
        menuItemLoad.setEnabled(false);
        mainMenu.add(menuItemLoad);
        mainMenu.addSeparator();
        
        JMenuItem menuItemExit = new JMenuItem("Exit");
        menuItemExit.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }        
        });
        mainMenu.add(menuItemExit);

        JMenu switchModeMenu = new JMenu("Mode");
        menuBar.add(switchModeMenu);
        
        group = new ButtonGroup();
        
        JRadioButtonMenuItem menuItemShapes = new JRadioButtonMenuItem("Annotate Shapes");
        menuItemShapes.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(Constants.MODE_SHAPES);
            }            
        });
        menuItemShapes.setSelected(true);
        group.add(menuItemShapes);
        switchModeMenu.add(menuItemShapes);
        
        JRadioButtonMenuItem menuItemTracking = new JRadioButtonMenuItem("Speckle Tracking");
        menuItemTracking.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (PCA.getInstance().isInitialized())  {
                    setMode(Constants.MODE_TRACKING);
                } else {
                    JOptionPane.showMessageDialog(Frame.getFrames()[0], "Generate a model before the tracking");
                    Enumeration elements = group.getElements();
                    JRadioButtonMenuItem b = (JRadioButtonMenuItem)elements.nextElement();
                    b.setSelected(true);
                }
            }            
        });
        group.add(menuItemTracking);
        switchModeMenu.add(menuItemTracking);
        
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        JMenuItem menuItemHelp = new JMenuItem("Manual");
        menuItemHelp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new HelpWindow();
            }
            
        });
        helpMenu.add(menuItemHelp);
        
        setJMenuBar(menuBar);
    }
    
    private void resetTracking() {
        if (!trackedShapes.isEmpty()) {
            trackedShapes.clear();
        }
        canvas.setTracking(false);
        canvas.reset();
        player.setTracking(false);
    }
    
    /**
     * Nastaví mód, v ktorom užívateľ pracuje.
     * 
     * @param mode 
     */
    private void setMode(int mode) {
        if (mode == Constants.MODE_SHAPES) {
            setShapesMode();
        } else if (mode == Constants.MODE_TRACKING) {
            setTrackingMode();
        }
        this.mode = mode;
    }
    
    /**
     * Zistí, či je aktívny anotačný mód.
     * 
     * @return true, ak je aktívny anotačný mód, inak false
     */
    private boolean isShapesMode() {
        return mode == Constants.MODE_SHAPES;
    }

    /**
     * Zistí, či je aktívny sledovací mód.
     * 
     * @return true, ak je aktívny sledovací mód, inak false
     */
    private boolean isTrackingMode() {
        return mode == Constants.MODE_TRACKING;
    }
    
    /**
     * Inicializuje posuvník pre ovládanie videa.
     * 
     * @param max maximálna hodnota, počet snímkov videa
     * @param val aktuálna hodnota, najcastejsie prvý snimok - 0
     */
    private void initSlider(int max, int val) {
        vidSlider.setMinimum(0);
        vidSlider.setMaximum(max);
        vidSlider.setValue(val);
    }
    
    /**
     * Nastaví prostredie a príznaky pre mód sledovania.
     */
    private void setTrackingMode() {
        Enumeration elements = group.getElements();
        JRadioButtonMenuItem b = (JRadioButtonMenuItem)elements.nextElement();
        b= (JRadioButtonMenuItem)elements.nextElement();
        b.setSelected(true);
        canvas.reset();
        annotControls.setVisible(false);
        trackingControls.setVisible(true);
        enableMenuShapePersistance(false);
    }
    
    /**
     * Nastaví prostredie a príznaky pre mód anotovania.
     */
    private void setShapesMode() {
        canvas.setTracking(false);
        canvas.reset();
        annotControls.setVisible(true);
        trackingControls.setVisible(false);

        if (frames.isLoaded()) {
            enableMenuShapePersistance(true);
        }
        
    }
    
    /**
     * Nahrá video do aplikácie. Snímky vo farbe aj šedi uloží do štruktúry VidData.
     */
    private void openVideo() {        
        JFileChooser fc = new JFileChooser();
        int ret = fc.showDialog(this, "Open Video");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            loadingDialog.setVisible(true);
            if (!loadVideo(file.getPath())) {
                System.err.println("Unable to load the video");
            } else {
                // zobraz prvy snimok videa
                canvas.setOffset(frames.getCurrentFrame());
                canvas.drawVideoFrame(frames.getNextFrame());
                // nastav posuvnik ovladania videa
                int frameCnt = frames.getFrameCnt();
                initSlider(frameCnt - 1, 0);
                // inicializuj ovladanie videa
                player = new VidPlayer(frames, canvas, vidSlider, buttonPlay, trackedShapes);
                buttonPlay.setPlay(false);
                // zobraz ovladanie videa
                enableControls(true);
                if (isTrackingMode()) {
                    resetTracking();
                }
            }
            loadingDialog.setVisible(false);
        }
    }

    /**
     * Zapne ovládacie prevky videa a povolí nahrávať a ukladať tvary zo súborov.
     * 
     * @param val true pre zapnutie, false pre vypnutie
     */
    private void enableControls(boolean val) {
        vidControlsPane.setVisible(val);
        if (!isTrackingMode()) {
            enableMenuShapePersistance(val);
        }
    }
    
    /**
     * Povolí nahrávať a ukladať tvary zo súborov cez položky menu aplikácie.
     * 
     * @param val true pre zapnutie, false pre vypnutie
     */
    private void enableMenuShapePersistance(boolean val) {
        menuItemSave.setEnabled(val);
        menuItemLoad.setEnabled(val);        
    }
    
    /**
     * Nahrá video zo špecifikovaného súboru do internej štruktúry VidData ako snímky.
     * 
     * @param path cesta k súboru s videom
     * @return false, ak pri nahrávaní dojde ku chybe, inak false
     */
    private boolean loadVideo(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        return VidLoader.load(path, frames);
    }
    
    /**
     * V reakcii na posúvnik ovládania videa nastaví snímok s indexom na plátno.
     * 
     * @param newPos hodnota posuvníka, značí číslo snímku indexované od 0
     */
    private void setFrame(int newPos) {
        if (newPos < 0) {
            newPos = 0;
        } else if (newPos >= frames.getFrameCnt()) {
            newPos = frames.getFrameCnt() -1;
        }

        if (canvas.isTracking()) {
            if (trackedShapes.get(newPos) != null) {
                canvas.setSplinePts(trackedShapes.get(newPos).getSplinePoints());
                canvas.setControlPts(trackedShapes.get(newPos).getControlPoints());
            } else {
                canvas.setSplinePts(null);
                canvas.setControlPts(null);
            }
        }
        Image img = frames.getFrame(newPos);
        canvas.drawVideoFrame(img);
    }
    
    /**
     * Krokovanie videa smerom dozadu.
     */
    private void setFrameRev() {
        vidSlider.setValue(frames.getFrameNo() - Constants.FRAME_ADJ_STEP);
    }
    
    /**
     * Krokovanie videa smerom dopredu.
     */
    private void setFrameFwd() {
        vidSlider.setValue(frames.getFrameNo() + Constants.FRAME_ADJ_STEP);
    }
    
    /**
     * Uloží anotované a potvrdené tvary do súboru.
     */
    private void saveShapes() {
        if (shapes.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Shape list is empty, there is nothing to save");
        } else {
            JFileChooser fc = new JFileChooser();
            int ret = fc.showDialog(this, "Save shapes");
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String filename = file.getPath();
                // uloz vsetky naklikane tvary do JSON suboru s uzivatelovym nazvom
                if (Shapes.saveShapes(shapes, filename)) {
                    JOptionPane.showMessageDialog(null, "Shapes saved successfully");
                } else {
                    JOptionPane.showMessageDialog(null, "Saving shapes failed");
                }
            }
        }
    }
    
    /**
     * Nahrá anotované tvary zo súboru do programu, aby k nim mohli užívatelia
     * pridať ďalšie, prípadne ich rovno spracovať.
     */
    private void loadShapes() {        
        if (!shapes.isEmpty()) {
            int res = JOptionPane.showConfirmDialog(null,
                    "Shape list is not empty. Would you like to overwrite existing shapes?");
            if (res == JOptionPane.YES_OPTION) {
                shapes.clear();
            } else if (res == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        JFileChooser fc = new JFileChooser();
        int ret = fc.showDialog(this, "Load shapes");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String filename = file.getPath();
            // uloz vsetky naklikane tvary do JSON suboru s uzivatelovym nazvom
            if (Shapes.loadShapes(shapes, filename)) {
                //JOptionPane.showMessageDialog(null, "Shapes loaded successfully");
                shapeCntLabel.setText(Constants.SHAPE_CNT_LABEL_TEXT + shapes.size());
            } else {
                JOptionPane.showMessageDialog(null, "Loading shapes failed");
            }
        }
    }
}
