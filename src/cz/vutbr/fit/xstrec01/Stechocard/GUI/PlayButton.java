package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import javax.swing.JButton;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class PlayButton extends JButton {
    private boolean play = false;
    
    public PlayButton(String label) {
        super(label);
    }
    
    public boolean isPlay() {
        return play;
    }
    
    public void setPlay(boolean state) {
        play = state;
    }
}
