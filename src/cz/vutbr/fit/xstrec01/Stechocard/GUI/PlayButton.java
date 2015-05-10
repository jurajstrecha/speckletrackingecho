package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import javax.swing.JButton;

/**
 * Tlačítko pre spúšťanie/prerušovanie prehrávania. Udržiava si v pamäti svoj stav.
 *
 * @author Juraj Strecha, xstrec01
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
        if (state == true) {
            setText("PAUSE");
        } else {
            setText("PLAY");
        }
        play = state;
    }
}
