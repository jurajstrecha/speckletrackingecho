package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * Plátno pre vykreslenie snímkov a anotovaných bodov. Udržiava zoznam anotovaných
 * bodov. Používajú sa na získanie tvaru pre uloženie do súboru a vyznačenie bodov,
 * ktoré chceme sledovať pomocou optického toku.
 * 
 * @author Juraj Strecha, xstrec01
 */
public final class Canvas extends JPanel {    
    private final ArrayList<Point> controlPts;
    private final ArrayList<Point> splinePoints;
    private boolean tracking;
    
    private Image img;
    private int xOffset;
    private int yOffset;
    
    public Canvas() {
        super();
        controlPts = new ArrayList<Point>();
        splinePoints = new ArrayList<Point>();
        
        addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent me) {
                    if (img != null && !tracking) {
                        controlPts.add(me.getPoint());
                        repaint();
                    }
                }                
            }
        );
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            g.drawImage(img, getXOffset(), getYOffset(), this);
        }
        drawControlPoints(g);
//        drawSplinePoints(g);
    }
    
    /**
     * Draws crosses in the place where the user declares a control point.
     * 
     * @param g Graphics context
     */
    private void drawControlPoints(Graphics g) {
        if (!controlPts.isEmpty()) {
            for (Point p: controlPts) {
                g.setColor(Constants.CONTROL_POINT_COLOR);
                int x = (int)Math.round(p.getX());
                int y = (int)Math.round(p.getY());
                g.drawLine(x + Constants.CROSS_HORIZ_DIAMETER, y - 1,
                           x - Constants.CROSS_HORIZ_DIAMETER, y - 1);
                g.drawLine(x + Constants.CROSS_HORIZ_DIAMETER, y,
                           x - Constants.CROSS_HORIZ_DIAMETER, y);
                g.drawLine(x + Constants.CROSS_HORIZ_DIAMETER, y + 1,
                           x - Constants.CROSS_HORIZ_DIAMETER, y + 1);

                g.drawLine(x - 1, y + Constants.CROSS_VERT_DIAMETER,
                           x - 1, y - Constants.CROSS_VERT_DIAMETER);
                g.drawLine(x, y + Constants.CROSS_VERT_DIAMETER,
                           x, y - Constants.CROSS_VERT_DIAMETER);
                g.drawLine(x + 1, y + Constants.CROSS_VERT_DIAMETER,
                           x + 1, y - Constants.CROSS_VERT_DIAMETER);
            }
        } 
    }
    
    /**
     * Vykreslí splajn z bodov v pamäti (this.splinePoints)
     * 
     * @param g Graphics context
     */
    private void drawSplinePoints(Graphics g) {
        if (splinePoints != null && !splinePoints.isEmpty()) {
            g.setColor(Constants.SPLINE_COLOR);
            Point recentPoint = splinePoints.get(0);
            Point currentPoint;
            Graphics2D g2 = (Graphics2D)g;
            g2.setStroke(new BasicStroke(Constants.SPLINE_THICKNESS));
            for (int i = 1; i < splinePoints.size(); i++) {
                currentPoint = splinePoints.get(i);
                g.drawLine(recentPoint.x, recentPoint.y,
                           currentPoint.x, currentPoint.y);
                recentPoint = currentPoint;
            }
        }
    }
    
    /**
     * Vykreslí farebný snímok na plátno.
     * 
     * @param img 
     */
    public void drawVideoFrame(Image img) {
        this.img = img;
        if (!tracking) {
            reset();
        }
        repaint();
    }
    
    /**
     * Vymaže anotované body bez uloženia.
     */
    public void reset() {
        controlPts.clear();
        repaint();
    }
    
    /**
     * Nastaví odsadenie snímku na plátne od ľavého horného okraja, aby bol vycentrovaný.
     * 
     * @param img snímok, ktorý ma byť zobrazený a vycentrovaný
     */
    public void setOffset(Image img) {
        int x = (this.getSize().width - img.getWidth(null)) / 2;
        int y = (this.getSize().height - img.getHeight(null)) / 2;
        if (x < 0 || y < 0) {
            x = 0;
            y = 0;
        }

        xOffset = x;
        yOffset = y;
    }
    
    private int getXOffset() {
        return this.xOffset;
    }
    
    private int getYOffset() {
        return this.yOffset;
    }
    
    public ArrayList<Point> getControlPts() {
        return controlPts;
    }
    
    public void setControlPts(ArrayList<Point> pts) {
        controlPts.clear();
        controlPts.addAll(pts);
    }
    
    /**
     * Poznačí stav, kedy nie je možné klikaním na plátno pridávať nové body,
     * pretože užívateľ vyznačil štruktúru, ktorú chce sledovať a tá je teraz
     * počítaná a nesmie byť zmenená.
     * 
     * @param val 
     */
    public void setTracking(boolean val) {
        tracking = val;
    }
}
