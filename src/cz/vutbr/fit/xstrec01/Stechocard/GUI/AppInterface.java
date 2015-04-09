package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.CatmullRom;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.Shapes;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidData;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidLoader;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidPlayer;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Main graphical user interface for the Catmull-Rom Spline Demonstration application.
 * Creates and initializes all interface components and adds their functionality as well.
 * 
 * @author Juraj Strecha, duri.strecha@gmail.com
 * @version 1.0
 */
public class AppInterface extends JFrame implements ActionListener, ChangeListener {
    private Canvas canvas;
    private JButton buttonGenerate;
    private JButton buttonReset;
    private VidData frames;
    private int mode;
    JPanel vidControlsPane;
    PlayButton buttonPlay;
    JSlider vidSlider;
    VidPlayer player;
    Shapes shapes;
    
    public AppInterface() {
        super(Constants.appName);
        
        // set up the application main window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Constants.BACKGROUND_COLOR);
        setMinimumSize(Constants.APP_WINDOW_MIN_SIZE);
        
        createMenu();
        
        // creater, initialize and add components to the interface
        createComponents(getContentPane());
        
        mode = Constants.MODE_SHAPES;
        enableControls(false);
        shapes = new Shapes();

        // display the window
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    /**
     * Construct, set and add all GUI components to the main window.
     * 
     * @param pane Pane for constructed components
     */
    private void createComponents(Container pane) {
        pane.setLayout(new BorderLayout());
        
        JPanel canvasPane = new JPanel();
        canvasPane.setBackground(Constants.BACKGROUND_COLOR);
        canvasPane.setLayout(new GridBagLayout());
        //canvasPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        canvas = new Canvas();
        canvas.setBackground(Constants.CANVAS_COLOR);
        canvas.setPreferredSize(Constants.CANVAS_SIZE);
        //canvas.setBorder(BorderFactory.createEmptyBorder(50,50,50,50));
        canvasPane.add(canvas);
        pane.add(canvasPane, BorderLayout.CENTER);
        vidControlsPane = new JPanel();
        vidControlsPane.setLayout(new BoxLayout(vidControlsPane, BoxLayout.Y_AXIS));

        JPanel vidSliderPane = new JPanel();
        vidSlider = addVidSlider(this);
        vidSlider.setMinimum(0);
        vidSlider.setValue(0);
        vidSlider.setPreferredSize(Constants.SLIDER_DIM);
        vidSliderPane.add(vidSlider);
        vidControlsPane.add(vidSliderPane);
        
        JPanel vidControlsHolder = new JPanel();
        JButton buttonRev = addButton("<<", vidControlsHolder, this);
        buttonPlay = new PlayButton("Play");
        buttonPlay.setActionCommand("Play");
        buttonPlay.addActionListener(this);
        vidControlsHolder.add(buttonPlay);
        JButton buttonFwd = addButton(">>", vidControlsHolder, this);
        vidControlsPane.add(vidControlsHolder);

        pane.add(vidControlsPane, BorderLayout.SOUTH);

        JPanel controls = new JPanel();
        controls.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        controls.setLayout(new BoxLayout(controls ,BoxLayout.Y_AXIS));
        buttonGenerate = addButton("Generate", controls, this);
        buttonGenerate.setMaximumSize(Constants.CTRL_BUTTONS_SIZE);
        controls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        buttonReset = addButton("Reset", controls, this);
        buttonReset.setMaximumSize(Constants.CTRL_BUTTONS_SIZE);
        controls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        JPanel controlsHolder = new JPanel();
        controlsHolder.add(controls);
        pane.add(controlsHolder, BorderLayout.EAST);        
    }
    
    /**
     * Creates button with preferred button size and adds it to the provided 
     * container.
     * 
     * @param container Container for the button to be added to
     * @param text Text displayed on the button
     */
    private static JButton addButton(String text, Container container, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        container.add(button);
        return button;
    }
    
    private static JMenuItem addMenuItem(String name, ActionListener listener) {
        JMenuItem item = new JMenuItem(name);
        item.setActionCommand(name);
        item.addActionListener(listener);
        return item;
    }
    
    private static JRadioButtonMenuItem addRadioMenuItem(String name, ActionListener listener, ButtonGroup group) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
        item.setActionCommand(name);
        item.addActionListener(listener);
        group.add(item);
        return item;
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu mainMenu = new JMenu("Application");
        menuBar.add(mainMenu);
        JMenuItem menuItemOpen = addMenuItem("Open Video", this);
        mainMenu.add(menuItemOpen);        
        JMenuItem menuItemExit = addMenuItem("Exit", this);
        mainMenu.add(menuItemExit);

        JMenu switchModeMenu = new JMenu("Mode");
        menuBar.add(switchModeMenu);
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem menuItemShapes = addRadioMenuItem("Create Shapes", this, group);
        switchModeMenu.add(menuItemShapes);
        JRadioButtonMenuItem menuItemTracking = addRadioMenuItem("Speckle Tracking", this, group);
        switchModeMenu.add(menuItemTracking);
        menuItemShapes.setSelected(true);
        
        setJMenuBar(menuBar);
    }
    
    private static JSlider addVidSlider(ChangeListener listener) {
        JSlider slider = new JSlider();
        
        slider.setMinimum(0);
        slider.setValue(0);
        slider.addChangeListener(listener);
        
        return slider;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) { 
        switch(e.getActionCommand()) {
            case "Open Video":
                JFileChooser fc = new JFileChooser();
                int ret = fc.showOpenDialog(this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    if (frames == null) {
                        frames = new VidData();
                    }
                    File file = fc.getSelectedFile();
                    if (!loadVideo(file.getPath())) {
                        System.err.println("Unable to load the video");
                    } else {
                        canvas.drawVideoFrame(frames.getNextFrame());
                        vidSlider.setMaximum((int)frames.getFrameCnt());
                        player = new VidPlayer(frames, canvas, vidSlider);
                        buttonPlay.setPlay(false);
                        enableControls(true);
                    }
                }

                break;
                
            case "Exit":
                System.exit(0);
                break;
                
            case "Create Shapes":
                System.out.println("MODE_SHAPES");
                mode = Constants.MODE_SHAPES;
                break;

            case "Speckle Tracking":
                System.out.println("MODE_TRACKING");
                mode = Constants.MODE_TRACKING;
                break;
                
            case "Play":
                if (buttonPlay.isPlay()) {
                    buttonPlay.setPlay(false);
                    buttonPlay.setText("Play");
                    player.setPlaying(false);
                    
                } else {
                    buttonPlay.setPlay(true);
                    buttonPlay.setText("Pause");
                    if (player != null) {
                        player.setPlaying(true);
                        new Thread(player).start();
                    }
                }
                break;
                
            case "Generate":
                ArrayList<Point> controlPoints = canvas.getControlPoints();
                if (controlPoints != null && controlPoints.size() > 1) {
                    shapes.serializeShape(controlPoints);
                    ArrayList<Point> splinePoints;
                    splinePoints = CatmullRom.calculateSpline(controlPoints);
                    canvas.setSplinePoints(splinePoints);
                }
                break;
                
            case "Reset":
                canvas.reset();
                break;
                
            case "<<":
                setFrameRev();
                break;

            case ">>":
                setFrameFwd();
                break;
        }
    }
    
    private int getMode() {
        return mode;
    }
    
    private void enableControls(boolean val) {
        vidControlsPane.setVisible(val);

    }
    
    private boolean loadVideo(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        if (frames == null) {
            frames = new VidData();
        }
        
        return VidLoader.load(path, frames);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(vidSlider)) {
            setFrame(vidSlider.getValue());
        }
    }
    
    private void setFrame(int i) {
        Image img = frames.getFrame(i);
        canvas.reset();
        canvas.drawVideoFrame(img);
        vidSlider.setValue(i);
    }
    
    private void setFrameRev() {
        setFrame(frames.getFrameNo() - Constants.FRAME_ADJ_STEP);
    }
    
    private void setFrameFwd() {
        setFrame(frames.getFrameNo() + Constants.FRAME_ADJ_STEP);
    } 
}
