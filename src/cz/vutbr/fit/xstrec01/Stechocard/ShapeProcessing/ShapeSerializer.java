package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import java.util.ArrayList;

/**
 *
 * @author Bc. Juraj Strecha
 */
public class ShapeSerializer extends ArrayList<ArrayList<int[]>> {
    
    private float deltaT;
    
    public ShapeSerializer() {
        super();
    }
    
    public void addPoint(int[] p){
        this.get(this.size() - 1).add(p);
    }
    
    public void addShape() {
        this.add(new ArrayList<>());
    }
    
    public ArrayList<int[]> getShape(int i) {
        return this.get(i);
    }
    
    public ArrayList<int[]> getRecentShape() {
        if (this.size() > 0) {
            return this.getShape(this.size() - 1);
        }
        return null;
    }
    
    public ArrayList<int[]> saveRecentShape() {
        ArrayList<int[]> recentShape = getRecentShape();
              
        if (recentShape != null && recentShape.size() > 3) {
            this.deltaT = 1.0f / (float)(recentShape.size() * 10);
            
            // kopiruj prvy a posledny prvok na zaciatok a koniec pola
            ArrayList<int[]> CRShape = new ArrayList<int[]>(recentShape.size() + 2);
            CRShape.add(recentShape.get(0));
            CRShape.addAll(recentShape);
            CRShape.add(recentShape.get(recentShape.size() - 1));
            recentShape = CRShape;
            CRShape = null;    
            
            ArrayList<int[]> poly = new ArrayList<int[]>();
            float t = 0.0f;
            while(t < 1.0) {
                poly.add(RCPoint(recentShape, t));                
                t += this.deltaT;
            }
            
//            for (int i = 0; i < poly.size(); i++) {
//               System.out.println(poly.get(i)[0] + ", " + poly.get(i)[1]);
//            }
            return poly;
        }
        
        return null;
    }
    
    // source: http://www.codeproject.com/Articles/30838/Overhauser-Catmull-Rom-Splines-for-Camera-Animatio
    static public int[] CREquation(int[] p1, int[] p2, int[] p3, int[] p4, float t) {
        int result[] = new int[2];
        
        float t2 = t * t;
        float t3 = t2 * t;
        
        float b1 = 0.5f * (  -t3 + 2*t2 - t);
        float b2 = 0.5f * ( 3*t3 - 5*t2 + 2);
        float b3 = 0.5f * (-3*t3 + 4*t2 + t);
        float b4 = 0.5f * (   t3 -   t2    );
               
        result[0] = Math.round(b1*p1[0] + b2*p2[0] + b3*p3[0] + b4*p4[0]);
        result[1] = Math.round(b1*p1[1] + b2*p2[1] + b3*p3[1] + b4*p4[1]);
        
        return result;
    }
    
    private int[] RCPoint(ArrayList<int[]> recentShape, float t) {
        int p = (int)(t / deltaT);
        
        int p0 = p - 1;
        int p1 = p;
        int p2 = p + 1;
        int p3 = p + 2;
        
        p0 = bound(p0);
        p1 = bound(p1);
        p2 = bound(p2);
        p3 = bound(p3);
        
        float localT = (t - deltaT * (float)p) / deltaT;
        
        return CREquation(recentShape.get(p0),
                          recentShape.get(p1),
                          recentShape.get(p2),
                          recentShape.get(p3),
                          localT);
    }
    
    private int bound(int p) {
        if (p < 0) {
            return 0;
        } else if (p >= getRecentShape().size() - 1) {
            return getRecentShape().size() - 1;
        } else {
            return p;
        }
    }
}
