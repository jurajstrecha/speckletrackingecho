package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.Shape;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.Shapes;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidData;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidLoader;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidPlayer;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
public final class AppInterface extends JFrame {
    private Canvas canvas;
    private final VidData frames;
    private int mode;
    JMenuItem menuItemSave;
    JMenuItem menuItemLoad;
    private JPanel vidControlsPane;
    private PlayButton buttonPlay;
    private JSlider vidSlider;
    private VidPlayer player;
    private final Shapes shapes;
    private final JOptionPane optionPane;
    private final JDialog dialog;
    
    private static final Logger logger = Logger.getLogger(VidData.class.getName());
    
    public AppInterface() {
        super(Constants.appName);
        logger.setLevel(Level.FINE);
        
        // set up the application main window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Constants.BACKGROUND_COLOR);
        setMinimumSize(Constants.APP_WINDOW_MIN_SIZE);
        
        createMenu();
        createComponents(getContentPane());
        
        optionPane = new JOptionPane("Please wait", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        dialog = new JDialog();
        initLoadingDialog();
        
        enableControls(false);
        mode = Constants.MODE_SHAPES;
        shapes = new Shapes();
        frames = VidData.getInstance();

        // display the window
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
        openVideo();
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
        
        canvas = new Canvas();
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
        buttonPlay.setActionCommand("Play");
        buttonPlay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

        JPanel controls = new JPanel();
        controls.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        controls.setLayout(new BoxLayout(controls ,BoxLayout.Y_AXIS));
        JButton buttonAddShape = addButton("Add Shape", controls);
        buttonAddShape.setMaximumSize(Constants.CTRL_BUTTONS_SIZE);
        buttonAddShape.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Shape shape = canvas.getShape();
                if (shape.getAnnotatedPoints().isEmpty()) {
                    logger.log(Level.FINE, "No annotated points to store");
                    System.err.println("No annotated points to store");
                } else {
                    if (shapes.serializeShape(shape)) {
                        canvas.reset();
                        logger.log(Level.FINE, "Shape stored. {0} shapes so far.", shapes.size());
                        System.out.println("Stored. " + shapes.size() + " so far.");
                    } else {
                        logger.log(Level.SEVERE, "Shape storing failed");
                        System.err.println("Shape storing failed");
                    }
                }
            }            
        });
        controls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        JButton buttonReset = addButton("Reset", controls);
        buttonReset.setMaximumSize(Constants.CTRL_BUTTONS_SIZE);
        buttonReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.reset();
            }            
        });
        controls.add(Box.createRigidArea(Constants.CTRL_BUTTONS_GAP));
        JPanel controlsHolder = new JPanel();
        controlsHolder.add(controls);
        pane.add(controlsHolder, BorderLayout.EAST);        
    }
    
    private void initLoadingDialog() {
        dialog.setTitle("Loading");
        dialog.setPreferredSize(new Dimension(300, 200));
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
    }
    
    /**
     * Creates button with preferred button size and adds it to the provided 
     * container.
     * 
     * @param container Container for the button to be added to
     * @param text Text displayed on the button
     */
    private static JButton addButton(String text, Container container) {
        JButton button = new JButton(text);
        container.add(button);
        return button;
    }

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
        
        ButtonGroup group = new ButtonGroup();
        
        JRadioButtonMenuItem menuItemShapes = new JRadioButtonMenuItem("Create Shapes");
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
                setMode(Constants.MODE_TRACKING);
            }            
        });
        group.add(menuItemTracking);
        switchModeMenu.add(menuItemTracking);
        
        setJMenuBar(menuBar);
    }
    
    private void setMode(int mode) {
        if (mode == Constants.MODE_SHAPES) {
            System.out.println("MODE_SHAPES");
        } else if (mode == Constants.MODE_TRACKING) {
            System.out.println("MODE_TRACKING");
        }
        this.mode = mode;
    }
    
    private int getMode() {
        return mode;
    }
       
    private void initSlider(int max, int val) {
        vidSlider.setMinimum(0);
        vidSlider.setMaximum(max);
        vidSlider.setValue(val);
    }
    
    private void openVideo() {        
        JFileChooser fc = new JFileChooser();
        int ret = fc.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            dialog.setVisible(true);
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
                player = new VidPlayer(frames, canvas, vidSlider);
                buttonPlay.setPlay(false);
                // zobraz ovladanie videa
                enableControls(true);
            }
            dialog.setVisible(false);            
        }
    }
       
    private void enableControls(boolean val) {
        vidControlsPane.setVisible(val);
        menuItemSave.setEnabled(val);
        menuItemLoad.setEnabled(val);
    }
    
    private boolean loadVideo(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        return VidLoader.load(path, frames);
    }
    
    private void setFrame(int newPos) {
        if (newPos < 0) {
            newPos = 0;
        } else if (newPos >= frames.getFrameCnt()) {
            newPos = frames.getFrameCnt() -1;
        }

        Image img = frames.getFrame(newPos);
        canvas.reset();
        canvas.drawVideoFrame(img);
        vidSlider.setValue(newPos);
    }
    
    private void setFrameRev() {
        setFrame(frames.getFrameNo() - Constants.FRAME_ADJ_STEP);
    }
    
    private void setFrameFwd() {
        setFrame(frames.getFrameNo() + Constants.FRAME_ADJ_STEP);
    }
    
    private void saveShapes() {
        if (shapes.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Shape list is empty, there is nothing to save");
        } else {
            JFileChooser fc = new JFileChooser();
            int ret = fc.showSaveDialog(this);
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
        int ret = fc.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String filename = file.getPath();
            // uloz vsetky naklikane tvary do JSON suboru s uzivatelovym nazvom
            if (Shapes.loadShapes(shapes, filename)) {
                JOptionPane.showMessageDialog(null, "Shapes loaded successfully");
                System.out.println("Shapes: " + shapes.size());
            } else {
                JOptionPane.showMessageDialog(null, "Loading shapes failed");
            }
        }
    }
}
