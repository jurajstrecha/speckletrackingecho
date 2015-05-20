package cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing;

import cz.vutbr.fit.xstrec01.Stechocard.App.Constants;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Dátová štruktúra uchováva body, ktoré reprezentujú užívateľom anotovaný tvar.
 * Pri serializácii sa najskôr z bodov tvar vypočíta splajn, a potom sa krivka
 * rovnomerne rozdelí podľa vzdialenosti na Constants.SPLINE_DIV_INTERVALS
 * intervalov. Vznikne tak Constants.SPLINE_DIV_INTERVALS + 1 bodov a tie
 * reprezentujú tvar pre ďalšie počítanie.
 * 
 * @author Juraj Strecha, xstrec01
 */
public final class Shapes extends ArrayList<Shape> {
    private static Shapes instance = null;
    
    private Shapes(){
        super();
    }

    public static Shapes getInstance() {
        if (instance == null) {
            instance = new Shapes();
        }
        return instance;
    }
    
    /**
     * Z užívateľom anotovaného tvaru (sady bodov) vytvorí reprezentáciu tvaru
     * pomocou preloženia parametrickou krivkou - splajnom a rozdelením splajnu
     * na intervaly, čím vznikne rovnaký počet bodov pre každý z tvarov a tvar
     * nie je závislý na počte užívateľom označených bodov.
     * 
     * @param shape body vyznačené užívateľom
     * @return true, ak sa poradilo tvar uložiť, inak false
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
     * Uloží serializované tvary do užívateľom zvoleného súboru. Používa objekty
     * a funkcie knižnice Google JSONSimple.
     * 
     * @param shapes databáza anotovaných tvarov
     * @param filename názov súboru
     * @return true, ak uloženie prebehlo v poriadku, inak false
     */
    public static boolean saveShapes(Shapes shapes, String filename) {
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
                jsonShape.put("annotated", jsonAnnotated);
                jsonShapes.add(jsonShape);
            }
            jsonShapes.writeJSONString(bw);
            
        } catch (IOException ex) {
            System.err.println("Unable to load file to store shapes");
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

    /**
     * Zo zadaného súboru na disku nahrá serializované tvary. Používa objekty
     * a funkcie knižnice Google JSONSimple.
     * 
     * @param shapes databáza anotovaných tvarov
     * @param filename názov súboru
     * @return true, ak nahratie prebehlo v poriadku, inak false
     */
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
                ArrayList<Point> splinePts;
                for (Object jsonShape1 : jsonShapes) {
                    JSONObject jsonShape = (JSONObject) jsonShape1;
                    JSONArray jsonAnnotated = (JSONArray)jsonShape.get("annotated");
                    JSONArray point;
                    long x, y;
                    Shape shape = new Shape();
                    for (Object jsonAnnotated1 : jsonAnnotated) {
                        point = (JSONArray) jsonAnnotated1;
                        x = (long)point.get(0);
                        y = (long)point.get(1);
                        // JSON Simple dokáže dekódovať iba do long, pre vytvorenie
                        // Point potrebujeme int
                        shape.addAnnotatedPoint(new Point((int)x, (int)y));
                    }
                    splinePts = CatmullRom.calculateSpline(shape.annotatedPoints);
                    shape.addSplinePoints(CatmullRom.divideSpline(splinePts, Constants.SPLINE_DIV_INTERVALS));
                    shapes.add(shape);
                }                
            } else {
                System.err.println("Unable to open file for read: {0}" + filename);
            }
        } catch (IOException | ParseException ex) {
            System.err.println("loadShapes: " + ex.getMessage());
        }      
        
        return true;
    }
}
