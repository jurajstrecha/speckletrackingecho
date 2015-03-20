package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class PlayButton extends JButton {
    private boolean play = false;
    
    public PlayButton(String label) {
        super(label);
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (play) {
                    play = false;
                    setText("Play");
                } else {
                    play = true;
                    setText("Pause");
                }
            }
        });
    }
    
    public boolean isPlay() {
        return play;
    }
}
