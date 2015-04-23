package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import cz.vutbr.fit.xstrec01.Stechocard.Video.VidData;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Data structure holding points representing user annotated shapes.
 * Can select Constants.SPLINE_DIV_INTERVALS + 1 points from shape in spline form.
 * Stores serialized shapes onto the hard drive.
 * 
 * @author Bc. Juraj Strecha, xstrec01@stud.fit.vutbr.cz
 */
public final class Shapes extends ArrayList<Shape>{   
    private static final Logger logger = Logger.getLogger(VidData.class.getName());
   
    /**
     * Create a set of points from user annotated points using spline
     * representation and spline interval division and point selection.
     * The reduced points are stored as a shape into object memory.
     * 
     * @param shape Points provided by the user
     * @return true if the shape was stored, false otherwise
     */
    public boolean serializeShape(Shape shape) {
        ArrayList<Point> spline;
        ArrayList<Point> mainPoints;
        
        if (shape == null) {
            return false;
        }

        spline = CatmullRom.calculateSpline(shape.getAnnotatedPoints());
        if (spline == null) {
            return false;
        }

        mainPoints = CatmullRom.divideSpline(spline, Constants.SPLINE_DIV_INTERVALS);
        if (mainPoints == null) {
            return false;
        }
        
        shape.addSplinePoints(mainPoints);
        add(shape);
        return true;
    }
    
    /**
     * Stores serialized shape as a file on the hard drive, lets the user
     * select file name and destination by displaying a save dialog.
     * 
     * @return true if stored successfully, false otherwise
     */
    public static boolean saveShapes(Shapes shapes, String filename) {
        // take all serialized shapes and store them on the disk using user dialog
        if (shapes == null || shapes.isEmpty()) {            
            return false;
        }
        
        File f = null;
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            f = new File(filename);
            fw = new FileWriter(f);
            bw = new BufferedWriter(fw);
            
            JSONArray jsonShapes = new JSONArray();
            for (Shape shape: shapes) {
                JSONObject jsonShape = new JSONObject();
                JSONArray jsonAnnotated = new JSONArray();
                for (Point p: shape.annotatedPoints) {
                    JSONArray jsonPoint = new JSONArray();
                    jsonPoint.add(p.x);
                    jsonPoint.add(p.y);
                    jsonAnnotated.add(jsonPoint);
                }
                JSONArray jsonSpline = new JSONArray();
                for (Point p: shape.getSplinePoints()) {
                    JSONArray jsonPoint = new JSONArray();
                    jsonPoint.add(p.x);
                    jsonPoint.add(p.y);
                    jsonSpline.add(jsonPoint);
                }
                jsonShape.put("annotated", jsonAnnotated);
                jsonShape.put("spline", jsonSpline);
                jsonShapes.add(jsonShape);
            }
            jsonShapes.writeJSONString(bw);
            
        } catch (IOException ex) {
            logger.log(Level.ALL, "Unable to load file to store shapes");
            return false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (Exception ex) {}
            }
        }
        return true;
    }
    
    public static boolean loadShapes(Shapes shapes, String filename) {
        if (shapes == null) {
            return false;
        }
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        File f = null;
        FileReader fr = null;
        BufferedReader br = null;
               
        try {
            f = new File(filename);
            if (f.canRead()) {
                fr = new FileReader(f);
                br = new BufferedReader(fr);
                
                JSONParser parser = new JSONParser();                
                Object o = parser.parse(br);
                JSONArray jsonShapes = (JSONArray)o;
                for (int i = 0; i < jsonShapes.size(); i++) {
                    JSONObject jsonShape = (JSONObject)jsonShapes.get(i);
                    JSONArray jsonAnnotated = (JSONArray)jsonShape.get("annotated");
                    JSONArray jsonSpline = (JSONArray)jsonShape.get("spline");
                    JSONArray point;
                    long x, y;
                    
                    Shape shape = new Shape();
                    for (int j = 0; j < jsonAnnotated.size(); j++) {
                        point = (JSONArray)jsonAnnotated.get(j);
                        x = (long)point.get(0);
                        y = (long)point.get(1);
                        // JSON Simple dokaze dekodovat iba do long, pre vytvorenie
                        // Point potrebujeme int
                        shape.addAnnotatedPoint(new Point((int)x, (int)y));
                    }
                    
                    for (int j = 0; j < jsonSpline.size(); j++) {
                        point = (JSONArray)jsonSpline.get(j);
                        x = (long)point.get(0);
                        y = (long)point.get(1);
                        shape.addSplinePoint(new Point((int)x, (int)y));
                    }
                    shapes.add(shape);
                }                
            } else {
                logger.log(Level.SEVERE, "Unable to open file for read: {0}", filename);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "loadShapes: ", ex);
        } finally {

        }
        
        
        return true;
    }
}
