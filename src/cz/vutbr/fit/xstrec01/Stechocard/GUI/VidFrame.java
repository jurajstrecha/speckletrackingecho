package cz.vutbr.fit.xstrec01.Stechocard.GUI;

import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.ShapeSerializer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.opencv.core.Mat;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class VidFrame extends JPanel {
    private Image img;
    private boolean isClicked = false;
    private boolean ready = false;
    private Point click;
    private final static int crossMarkHalfLineSize = 6;
    private final static Color crossMarkColor = Color.RED;
    private final ShapeSerializer shapes;
    private final PointCntLabel cntLabel;
    
    public VidFrame(ShapeSerializer shapes, PointCntLabel cntLabel) {
        super();
        this.shapes = shapes;
        this.cntLabel = cntLabel;
        addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent me) { 
                    if (ready) {
                        click = me.getPoint();
                        isClicked = true;
                        cntLabel.incPts();
                        int[] point = {click.x, click.y};
                        shapes.addPoint(point);
                        repaint();
                    }
                }                
            }
        );
    }
    
    public void setImg(Mat img) {
        int imgType = img.channels() > 1 ?
                      BufferedImage.TYPE_3BYTE_BGR :
                      BufferedImage.TYPE_BYTE_GRAY;
        this.img = matToImg(img, img.cols(), img.cols(), imgType);
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, null);
        if (isClicked) {
            isClicked = false;
            drawCross(g);
            drawCross(img.getGraphics());
        }
    }
    
    private void drawCross(Graphics g) {
        g.setColor(crossMarkColor);
        g.drawLine(click.x + crossMarkHalfLineSize, click.y,
                   click.x - crossMarkHalfLineSize, click.y);
        g.drawLine(click.x, click.y + crossMarkHalfLineSize,
                   click.x, click.y - crossMarkHalfLineSize);
    }
    
    public void drawSpline(ArrayList<int[]> spline) {
        Graphics g = img.getGraphics();
        g.setColor(Color.GREEN);
        for (int i = 0; i < spline.size()- 1; i++) {
            g.drawLine(spline.get(i)[0], spline.get(i)[1], spline.get(i + 1)[0], spline.get(i + 1)[1]);            
        }
        
        int i = spline.size() - 1;
        g.drawLine(spline.get(i - 1)[0], spline.get(i - 1)[1], spline.get(i)[0], spline.get(i)[1]);
        
        repaint();
    }
    
    public static Image matToImg(Mat capFrame, int frameWidth, int frameHeight, int imgType) {
        BufferedImage image = new BufferedImage(frameWidth, frameHeight, imgType);
        int bufferSize = capFrame.channels() * frameWidth * frameHeight;
        final byte[] imBuffer = new byte[bufferSize];
        capFrame.get(0, 0, imBuffer);
        final byte[] imPixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(imBuffer, 0, imPixels, 0, imBuffer.length); 
        
        return image;
    }
    
    public void setReadyState(boolean val) {
        this.ready = val;
    }
}
