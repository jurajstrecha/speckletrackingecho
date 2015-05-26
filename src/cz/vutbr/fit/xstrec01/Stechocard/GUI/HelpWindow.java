package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import java.awt.Container;
import java.awt.Insets;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Okno s nápovedou k aplikácii.
 * 
 * @authorJuraj Strecha, xstrec01
 */
public class HelpWindow extends JFrame {
    
    private static final String text = ""
    + "<html><body>"
    + "<center><h3>Manual</h3></center>"
    + "<p>Application tracks marked points in the echocardiography video as the heart muscle moves during the heart cycle using the Optical Flow motion estimation and Active Shape Model definition obtained from the training set.</p>"
    + "<h3>Installation</h3>"
    + "<p>For instructions on installing and running the application the correct way see the Readme.txt file in the application root directory.</p>"
    + "<p>Example echocardiography video file and corresponding annotated shape set JSON file are enclosed in the /res directory in application root.</p>"
    + "<h3>Controls</h3>"
    + "<ul>"
    + "<li><b>Menu->Application->Open Video</b> - loads the video in h264/mp4 format</li>"
    + "<li><b>Menu->Application->Load Shapes</b> - loads pre-annotated shapes training set from JSON file for the model</li>"
    + "<li><b>Menu->Application->Save Shapes</b> - stores the annotated shapes list to the specified file on the disc</li>"
    + "<li><b>Menu->Application->Exit</b> - Quits the application</li>"
    + "<li><b>Menu->Mode->Shapes Annotation</b> - shapes training set definition/annotation mode</li>"
    + "<li><b>Menu->Mode->Speckle Tracking</b> - moving heart muscle shape tracking mode using the model generate from the training set</li>"
    + "<li><b>Menu->Help->Manual</b> - this manual page describing controls and usage of the program</li>"
    + "</ul>"
    + "There are two modes in the application: <b>Shapes Annotation</b> and <b>Speckle Tracking</b>. One can switch between them using the Mode menu group items. A model must be generated before entering the Speckle Tracking mode."
    + "<h3>Definig the model</h3>"
    + "<p>To track the movement a model mus be created. The model will persist after it has been created and can be overwritten by generating a new one using the shape training set. If there is a pre-annotated file with shapes stored in the JSON file format on the disc, load it to the the application using the menu item <b>Load Shapes</b> under the Application menu group. New training set can be set up easily by loading a video and annotating various shapes on different frames of the video. The player in the application works like an ordinary video player application. <b>Play</b> button starts the video playback and pauses it respectively. &lt&lt and &gt&gt forwards and rewinds respectively the video by 5 frames. Seeking with a slider can set the position of the video to any avalible frame.</p>"
    + "<p>While in the Shapes Annotation mode, clicking the video frame annotates one point of the shape. You can put as many point as you want to define a shape. However points MUST be placed continuously one by one in the clockwise direction. When a mistake in the shape annotation occurs, whole set of points must be removed by pressing a <b>Reset</b> button an the shape annotation process starts over. Once the plausible shape is created, press <b>Confirm Shape</b> button to store it to the list. The number of shapes annotated so far is displayed under the Reset button as <b>Shapes: n</b> message. When you reach the desired shape count, the list can be persisted to the disc by using the <b>Save Shapes</b> menu item.</p>"
    + "<h3>Shape tracking</h3>"
    + "<p>By pressing the <b>Generate Model</b> button the application switches its state to the <b>Speckle Tracking</b> mode. You can always return to the Shapes Annotation Mode using the <b>Shapes Annotation</b> menu item under the Mode menu in the menu bar. In tracking mode a initial shape must be defined on one of the frames. When this is done, press <b>Set Tracking</b> button to acknowledge the application that the initial tracking shape has been set and now the tracking process can begin by pressing the <b>Play</b> button. Once the video reaches the end the tracking stops. You can reset the current shape estimation by pressing the <b>button</b> and start over.</p>"
    + "</body></html>";
    
    public HelpWindow() {
        super();
        setPreferredSize(Constants.HELP_WINDOW_SIZE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        Container pane = getContentPane();
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text/html");
        textPane.setMargin(new Insets(20,20,20,20));
        textPane.setText(text);
        textPane.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(textPane);
        pane.add(scrollPane);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }    
}
