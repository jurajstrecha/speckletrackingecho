package cz.vutbr.fit.xstrec01.Stechocard.App;

import cz.vutbr.fit.xstrec01.Stechocard.ShapeProcessing.PCA;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Načíta už vytvorený model zo súboru vo formáte JSON do aplikácie. Uloží
 * vytvorený model do súboru, odkiaľ sa vždy pri štarte načíta.
 * 
 * @author Juraj Strecha, xstrec01
 */
public final class ModelLoader {
    private ModelLoader(){}
    
    public static void load(PCA pca) {
        JSONParser jp = new JSONParser();
        File f = null;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            f = new File(Constants.MODEL_FILE_PATH);
            if (f.canRead()) {
                fr = new FileReader(f);
                br = new BufferedReader(fr);
                
                JSONParser parser = new JSONParser();                
                JSONArray model = (JSONArray)parser.parse(br);
                
                for (Object obj : model) {
                    JSONObject modelItem = (JSONObject) obj;
                    if (modelItem.containsKey("eigenvectors")) {
                        Mat eigenvectorsMat = new Mat();
                        JSONArray eigenvectors = (JSONArray)modelItem.get("eigenvectors");
                        JSONArray eigenvector;
                        Mat eigenvectorMat;
                        for (int i = 0; i < eigenvectors.size(); i++) {
                            eigenvector = (JSONArray)eigenvectors.get(i);
                            eigenvectorMat = new Mat(1, eigenvector.size(), CvType.CV_64F);
                            for (int j = 0; j < eigenvector.size(); j++) {
                                eigenvectorMat.put(0, j, (double)eigenvector.get(j));
                            }
                            eigenvectorsMat.push_back(eigenvectorMat);
                        }
                        pca.eigenvectors = eigenvectorsMat;
                        
                    } else if (modelItem.containsKey("eigenvalues")) {
                        JSONArray eigenvalues = (JSONArray)modelItem.get("eigenvalues");
                        Mat eigenvaluesMat = new Mat(eigenvalues.size(), 1, CvType.CV_64F);
                        for (int i = 0; i < eigenvalues.size(); i++) {
                            eigenvaluesMat.put(i, 0, (double)eigenvalues.get(i));
                        }
                        pca.eigenvalues = eigenvaluesMat;
                    } else if (modelItem.containsKey("bLowerBounds")) {
                        JSONArray lowerb = (JSONArray)modelItem.get("bLowerBounds");
                        Mat lbMat = new Mat(1, lowerb.size(), CvType.CV_64F);
                        for (int i = 0; i < lowerb.size(); i++) {
                            lbMat.put(0, i, (double)lowerb.get(i));
                        }
                        pca.bLowerBounds = lbMat;
                    } else if (modelItem.containsKey("bUpperBounds")) {
                        JSONArray upperb = (JSONArray)modelItem.get("bUpperBounds");
                        Mat ubMat = new Mat(1, upperb.size(), CvType.CV_64F);
                        for (int i = 0; i < upperb.size(); i++) {
                            ubMat.put(0, i, (double)upperb.get(i));
                        }
                        pca.bUpperBounds = ubMat;
                    } else if (modelItem.containsKey("mean")) {
                        JSONArray mean = (JSONArray)modelItem.get("mean");
                        Mat meanMat = new Mat(1, mean.size(), CvType.CV_64F);
                        for (int i = 0; i < mean.size(); i++) {
                            meanMat.put(0, i, (double)mean.get(i));
                        }
                        pca.mean = meanMat;
                    }
                }                
                
                pca.initialized = true;
            }
        } catch (IOException | ParseException ex) {
            System.err.println("ModelLoader.load(): " + ex.getMessage());
        }
    }
    
    public static void save(PCA pca) {
        File f = null;
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            f = new File(Constants.MODEL_FILE_PATH);
            f.getParentFile().mkdirs();
            fw = new FileWriter(f);
            bw = new BufferedWriter(fw);
            
            JSONArray model = new JSONArray();

            // eigenvektory
            JSONArray eigenvectors = new JSONArray();
            JSONArray eigenvectorValues;
            Mat eigenvectorsMat = pca.eigenvectors;
            int vectorCnt = eigenvectorsMat.rows();
            int vectorLen = eigenvectorsMat.cols();
            for (int i = 0; i < vectorCnt; i++) {
                eigenvectorValues = new JSONArray();
                for (int j = 0; j < vectorLen; j++) {
                    eigenvectorValues.add(eigenvectorsMat.get(i, j)[0]);
                }
                eigenvectors.add(eigenvectorValues);
            }
            JSONObject vectors = new JSONObject();
            vectors.put("eigenvectors", eigenvectors);
            model.add(vectors);
            
            // eigenvalues
            JSONArray eigenvalues = new JSONArray();
            Mat eigenvaluesMat = pca.eigenvalues;
            for (int i = 0; i < eigenvaluesMat.rows(); i++) {
                eigenvalues.add(eigenvaluesMat.get(i, 0)[0]);
            }            
            JSONObject values = new JSONObject();
            values.put("eigenvalues", eigenvalues);
            model.add(values);
            
            // dolné hranice hodnôt vektora b
            JSONObject lowerb = new JSONObject();
            JSONArray lowerbValues = new JSONArray();
            Mat lowerbMat = pca.bLowerBounds;
            for (int i = 0; i < lowerbMat.cols(); i++) {
                lowerbValues.add(lowerbMat.get(0, i)[0]);
            }
            lowerb.put("bLowerBounds", lowerbValues);
            model.add(lowerb);
            
            // horné hranice hodnôt vektora b
            JSONObject upperb = new JSONObject();
            JSONArray upperbValues = new JSONArray();
            Mat upperbMat = pca.bUpperBounds;
            for (int i = 0; i < upperbMat.cols(); i++) {
                upperbValues.add(upperbMat.get(0, i)[0]);
            }
            upperb.put("bUpperBounds", upperbValues);
            model.add(upperb);
            
            // priemerný tvar
            JSONObject mean = new JSONObject();
            JSONArray meanValues = new JSONArray();
            Mat meanMat = pca.mean;
            for (int i = 0; i < meanMat.cols(); i++) {
                meanValues.add(meanMat.get(0, i)[0]);
            }
            mean.put("mean", meanValues);
            model.add(mean);
            
            model.writeJSONString(bw);
            
        } catch (IOException ex) {
            System.err.println("ModelLoader.save(): " + ex.getMessage());
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (Exception ex) {}
            }
        }
    }
}
