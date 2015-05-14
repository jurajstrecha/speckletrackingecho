package cz.vutbr.fit.xstrec01.Stechocard.App;

import cz.vutbr.fit.xstrec01.Stechocard.GUI.AppInterface;
import javax.swing.SwingUtilities;
import org.opencv.core.Core;

/**
 * Hlavná trieda načíta OpenCV knižnicu a spustí aplikáciu
 * 
 * @author Juraj Strecha, xstrec01
 */
public class Stechocard {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AppInterface();
            }
        });
    }

}
