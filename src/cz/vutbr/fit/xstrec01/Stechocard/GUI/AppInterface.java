/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidPlayer;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidLoader;
import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.ShapeSerializer;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidData;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author juraj
 */
public class AppInterface extends JFrame implements ActionListener, ChangeListener{
    
    private final VidFrame vidFrame;
    private final ShapeSerializer shapes;
    private VidData vidData;
    private final JButton buttonNewShape;
    private final PointCntLabel pointCnt;
    private final JSlider vidSlider;
    private final PlayButton buttonPlay;
    private final JButton buttonPrevFrame;
    private final JButton buttonNextFrame;
    private final JFileChooser fc;
    private final JOptionPane loadingOptionPane;
    private final JDialog loadingDialog;
    
    
    public AppInterface() {
        // nastavenie okna
        super(Constants.appName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        
        fc = new JFileChooser();
        loadingOptionPane = new JOptionPane("Loading video, please wait",
                                            JOptionPane.INFORMATION_MESSAGE,
                                            JOptionPane.DEFAULT_OPTION,
                                            null,
                                            new Object[]{},
                                            null);
        
        loadingDialog = new JDialog();
        loadingDialog.setTitle("Loading...");
        loadingDialog.setModal(true);
        loadingDialog.setContentPane(loadingOptionPane);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(null);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menuApp = new JMenu("Program");
        menuBar.add(menuApp);
        JMenuItem menuItemOpen = new JMenuItem("Open");
        menuItemOpen.addActionListener(this);
        menuItemOpen.setActionCommand("menuOpen");
        menuApp.add(menuItemOpen);
        JMenuItem menuItemSave = new JMenuItem("Save shapes");
        menuItemSave.addActionListener(this);
        menuItemSave.setActionCommand("menuSave");
        menuApp.add(menuItemSave);        
        menuApp.addSeparator();
        JMenuItem menuItemQuit = new JMenuItem("Quit");
        menuItemQuit.addActionListener(this);
        menuItemQuit.setActionCommand("menuQuit");
        menuApp.add(menuItemQuit);
        setJMenuBar(menuBar);
        
        // datove struktury
        shapes = new ShapeSerializer();
        // pridaj prvy tvar
        shapes.addShape();
        vidData = null;
        
        // prvky GUI
        pointCnt = new PointCntLabel("Points: ");
        vidFrame = new VidFrame(shapes, pointCnt);
        vidFrame.setPreferredSize(new Dimension(Constants.vidWidth, Constants.vidHeight));
        
        vidSlider = new JSlider(JSlider.HORIZONTAL);
        vidSlider.setValue(0);
        vidSlider.addChangeListener(this);
        
        buttonPrevFrame = new JButton("<<");
        buttonPrevFrame.setActionCommand("buttonPrevFrame");
        buttonPrevFrame.addActionListener(this);
        
        buttonPlay = new PlayButton("Play");
        buttonPlay.setActionCommand("buttonPlay");
        buttonPlay.addActionListener(this);
        
        buttonNextFrame = new JButton(">>");
        buttonNextFrame.setActionCommand("buttonNextFrame");
        buttonNextFrame.addActionListener(this);
        
        JPanel vidButtonPane = new JPanel();
        vidButtonPane.setLayout(new FlowLayout(FlowLayout.LEFT));
        vidButtonPane.add(buttonPrevFrame);
        vidButtonPane.add(buttonPlay);
        vidButtonPane.add(buttonNextFrame);
        deactivateControls();
        
        JPanel vidControlPane = new JPanel();
        vidControlPane.setLayout(new BoxLayout(vidControlPane, BoxLayout.PAGE_AXIS));
        vidControlPane.add(vidSlider);
        vidControlPane.add(vidButtonPane);
        
        JPanel centerPane = new JPanel();
        centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.PAGE_AXIS));
        centerPane.add(vidFrame);
        centerPane.add(vidControlPane);
        
        add(centerPane, BorderLayout.CENTER);

        buttonNewShape = new JButton("New Shape");      
        buttonNewShape.setActionCommand("buttonAddShape");
        buttonNewShape.addActionListener(this);
               
        JPanel rightPane = new JPanel();
        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.PAGE_AXIS));
        rightPane.add(buttonNewShape);
        rightPane.add(pointCnt);
        
        add(rightPane, BorderLayout.EAST);

        // zobrazenie
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actComm = e.getActionCommand();
        int pos;
        switch (actComm) {
            case "buttonAddShape":
                if (buttonPlay.isEnabled()) {
                    shapes.addShape();
                    pointCnt.resetPts();
                    vidFrame.setImg(vidData.getFrame(VidPlayer.getPos()));
                }
                break;
            case "buttonPlay":
                if (buttonPlay.isPlay()) {
                    VidPlayer.setPlaying(false);
                } else {
                    VidPlayer.setPlaying(true);
                    new Thread(new VidPlayer(vidData, vidFrame, vidSlider)).start();
                }
                break;
            case "buttonPrevFrame":
                pos = VidPlayer.getPos() - 5;
                vidFrame.setImg(vidData.getFrame(pos));
                VidPlayer.setPos(pos);
                vidSlider.setValue(pos);
                vidSlider.repaint();
                break;
            case "buttonNextFrame":
                pos = VidPlayer.getPos() + 5;
                vidFrame.setImg(vidData.getFrame(pos));
                VidPlayer.setPos(pos);
                vidSlider.setValue(pos);
                vidSlider.repaint();
                break;
            case "menuOpen":
                int returnVal = fc.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String path = fc.getSelectedFile().getAbsolutePath();
                    System.out.println(path);
                    vidData = new VidData();
                    new Thread(new VidLoader(path, vidData, loadingDialog)).start();
                    loadingDialog.setVisible(true);
                    if (vidData.getFrameCnt() < 1) {
                        JOptionPane.showMessageDialog(null, "Unable to load the video");
                    } else {
                        vidFrame.setImg(vidData.getFrame(0));
                        vidSlider.setValue(0);
                        vidSlider.setMaximum((int)Math.round(vidData.getFrameCnt() - 1));
                        VidPlayer.resetVidPos();
                        activateControls();
                        vidFrame.setReadyState(true);
                    }
                }
                break;
            case "menuSave":
                break;
            case "menuQuit":
                System.exit(0);
                break;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == vidSlider) {
            int pos = vidSlider.getValue();
            vidFrame.setImg(vidData.getFrame(pos));
            VidPlayer.setPos(pos);
            //CapturingThread.setPlaying(false);
        }
    }
    
    private void activateControls() {
        vidSlider.setEnabled(true);
        buttonPrevFrame.setEnabled(true);
        buttonPlay.setEnabled(true);
        buttonNextFrame.setEnabled(true);
    }
    
    private void deactivateControls() {
        vidSlider.setEnabled(false);
        buttonPrevFrame.setEnabled(false);
        buttonPlay.setEnabled(false);
        buttonNextFrame.setEnabled(false);        
    }
    
    private boolean saveShapes() {
        
        return true;
    }
}
