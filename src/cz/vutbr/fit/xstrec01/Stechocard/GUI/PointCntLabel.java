package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import javax.swing.JLabel;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class PointCntLabel extends JLabel {
    
    private int pts = 0;
    private final String text;
    
    public PointCntLabel(String text) {
        super(text);
        this.text = text ;
        paintText();
    }
    
    private void paintText() {
        this.setText(this.text + this.pts);
    }
    
    public void incPts() {
        this.pts++;
        paintText();
    }
    
    public void resetPts() {
        this.pts = 0;
        paintText();
    }
}
