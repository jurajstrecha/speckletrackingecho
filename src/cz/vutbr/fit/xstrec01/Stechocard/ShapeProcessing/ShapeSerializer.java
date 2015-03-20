package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class ShapeSerializer extends ArrayList<ArrayList<Point>> {
    public ShapeSerializer() {
        super();
    }
    
    public void addPoint(Point p) {
        this.get(this.size() - 1).add(p);
    }
    
    public void addShape() {
        this.add(new ArrayList<>());
    }
    
    public ArrayList<Point> getShape(int i) {
        return this.get(i);
    }
}
