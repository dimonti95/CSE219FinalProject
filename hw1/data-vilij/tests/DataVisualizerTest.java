import algorithms.*;
import dataprocessors.TSDProcessor;
import org.junit.Assert;
import org.junit.Test;
import javafx.geometry.Point2D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class DataVisualizerTest {

    private TSDProcessor processor = new TSDProcessor();
    private String tsdTestString = "@a\tlabel1\t1,1";

    /* Tests for parsing line of data in TSD format to create an instance object (including boundary values) */

    /***
     *  Processes a valid string of tab separated data.
     *  @result String will be processed without any errors. Then compares to the expected output.
     */
    @Test
    public void tsdValidDataTest() throws Exception {
        processor.processString(tsdTestString);

        Map                  pointsMapOutput   = processor.getDataPoints();
        Map<String, Point2D> pointsMapExpected = new HashMap<>();
        pointsMapExpected.put("@a", new Point2D(1, 1));

        Map                  labelsMapOutput  = processor.getDataLabels();
        Map<String, String>  labelsMapExpected = new HashMap<>();
        labelsMapExpected.put("@a", "label1");

        assertEquals(pointsMapExpected, pointsMapOutput);
        assertEquals(labelsMapExpected, labelsMapOutput);
    }

    /***
     *  An upper bound test that processes an invalid string of tab separated data with an empty string as a label name
     *  then compared to the expected output.
     *  @result Test will fail and the proccessString method throw an Exception because of the empty String value.
     */
    @Test (expected = Exception.class)
    public void tsdLowerBoundaryFailTest() throws Exception{
        String twoDecimalsTSD = "@a\t\t1,1";

        processor.processString(twoDecimalsTSD);

        Map                  output   = processor.getDataLabels();
        Map<String, String> expected = new HashMap<>();
        expected.put("@a", "");

        assertEquals(expected, output);
    }

    /***
     *  An upper bound test that processes a valid string of tab separated data with points to the maximum amount of
     *  decimal place aloud as specified in the SRS.
     *  @result String will be processed without any errors. Then compared to the expected output.
     */
    @Test
    public void tsdUpperBoundaryTest() throws Exception {
        String twoDecimalsTSD = "@a\tlabel1\t1.11,1.11";

        processor.processString(twoDecimalsTSD);

        Map                  output   = processor.getDataPoints();
        Map<String, Point2D> expected = new HashMap<>();
        expected.put("@a", new Point2D(1.11, 1.11));

        assertEquals(expected, output);
    }

    /***
     *  A lower bound test that processes a valid string of tab separated data with labels containing the string of
     *  characters "null" which is a special case that indicates a null label as specified in the SRS. An empty String
     *  is not considered valid label input.
     *  @result String will be processed without any errors. Then compared to the expected output.
     */
    @Test
    public void tsdLowerBoundaryTest() throws Exception {
        String twoDecimalsTSD = "@a\tnull\t1,1";

        processor.processString(twoDecimalsTSD);

        Map                  output  = processor.getDataLabels();
        Map<String, String> expected = new HashMap<>();
        expected.put("@a", "null");

        assertEquals(expected, output);
    }


    /* Tests for saving data from the text-area in the UI to a .tsd file. */

    /***
     *  A Save test that saves a new .tsd file containing the tab separated data string to a valid existing path.
     *  @result File will be created and saved without any errors and the string length of the data being passed
     *  will be equal to the length of the data in the file.
     */
    @Test
    public void saveTest() throws FileNotFoundException {
        String dataDirPath = "/" + "data";
        URL    dataDirURL  = getClass().getResource(dataDirPath);

        if (dataDirURL == null) throw new FileNotFoundException("Directory not found under resources.");

        File file = new File(dataDirURL.getFile() + "/newTSDFile.tsd");

        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(file.toPath()))) {
            writer.write((tsdTestString + System.getProperty("line.separator")));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        assertTrue(new File(file.getPath()).exists());
        Assert.assertTrue(file.length() == 14);
    }

    /***
     *  A Save test that saves a new .tsd file containing the tab separated data string to a invalid nonexistent path.
     *  @result File will be not be created or saved. Instead a FileNotFoundException occurs.
     */
    @Test (expected = FileNotFoundException.class)
    public void saveFailTest() throws FileNotFoundException {
        String dataDirPath = "/" + "nonexistentPath"; // path doesn't exist
        URL    dataDirURL  = getClass().getResource(dataDirPath);

        if (dataDirURL == null) throw new FileNotFoundException("Directory not found under resources.");

        File file = new File(dataDirURL.getFile() + "/newTSDFile.tsd");

        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(file.toPath()))) {
            writer.write((tsdTestString + System.getProperty("line.separator")));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


    /* Tests for Run configuration values for classification and clustering. (including boundary value analysis) */

    /***
     *  Tests the Run configuration values for a RandomClusterer object.
     *  @result Valid input is passed to the constructor and each value is compared to be equal to a RandomClusterer
     *  object containing the expected input.
     */
    @Test
    public void randomClustererRunConfigTest() {
        int     maxIterations       = 10;
        int     updateInterval      = 2;
        int     totalDistinctLabels = 2;
        boolean continuourRun       = false;

        /* code from ClusteringConfigUI implementation */
        //if(maxIterations <= 0)       { maxIterations = 1; }
        //if(updateInterval <= 0)      { updateInterval = 1; }
        //if(totalDistinctLabels <= 0) { totalDistinctLabels = 2; }

        Clusterer outputRandomClusterer = new RandomClusterer(null,
                                                                      maxIterations,
                                                                      updateInterval,
                                                                      totalDistinctLabels,
                                                                      4,
                                                                      continuourRun,
                                                                      null);

        Clusterer expectedRandomClusterer = new RandomClusterer(null,
                                                                        10,
                                                                        2,
                                                                        2,
                                                                        4,
                                                                        false,
                                                                        null);

        assertEquals(expectedRandomClusterer.getMaxIterations(), outputRandomClusterer.getMaxIterations());
        assertEquals(expectedRandomClusterer.getUpdateInterval(), outputRandomClusterer.getUpdateInterval());
        assertEquals(expectedRandomClusterer.getNumberOfClusters(), outputRandomClusterer.getNumberOfClusters());
        assertEquals(expectedRandomClusterer.tocontinue(), outputRandomClusterer.tocontinue());
    }

    /***
     *  Tests the Run configuration values for a KMeansClusterer object.
     *  @result Valid input is passed to the constructor and each value is compared to be equal to a KMeansClusterer
     *  object containing the expected input.
     */
    @Test
    public void kMeansClustererRunConfigTest() {
        int     maxIterations       = 10;
        int     updateInterval      = 2;
        int     totalDistinctLabels = 2;
        boolean continuourRun       = false;

        /* code from ClusteringConfigUI implementation */
        //if(maxIterations <= 0)       { maxIterations = 1; }
        //if(updateInterval <= 0)      { updateInterval = 1; }
        //if(totalDistinctLabels <= 0) { totalDistinctLabels = 2; }

        Clusterer outputKMeansClusterer = new KMeansClusterer(null,
                                                                        maxIterations,
                                                                        updateInterval,
                                                                        totalDistinctLabels,
                                                                        4,
                                                                        continuourRun,
                                                                        null);

        Clusterer expectedKMeansClusterer = new KMeansClusterer(null,
                                                                10,
                                                                2,
                                                                2,
                                                                4,
                                                                false,
                                                                null);

        assertEquals(expectedKMeansClusterer.getMaxIterations(), outputKMeansClusterer.getMaxIterations());
        assertEquals(expectedKMeansClusterer.getUpdateInterval(), outputKMeansClusterer.getUpdateInterval());
        assertEquals(expectedKMeansClusterer.getNumberOfClusters(), outputKMeansClusterer.getNumberOfClusters());
        assertEquals(expectedKMeansClusterer.tocontinue(), outputKMeansClusterer.tocontinue());
    }

    /***
     *  Tests the Run configuration values for a RandomClassifier object.
     *  @result Valid input is passed to the constructor and each value is compared to be equal to a RandomClassifier
     *  object containing the expected input.
     */
    @Test
    public void randomClassifierRunConfigTest() {
        int     maxIterations       = 10;
        int     updateInterval      = 2;
        boolean continuourRun       = false;

        /* code from ClassificationConfigUI implementation */
        //if(maxIterations <= 0)       { maxIterations = 1; }
        //if(updateInterval <= 0)      { updateInterval = 1; }

        Classifier outputRandomClassifier = new RandomClassifier(null,
                                                                        maxIterations,
                                                                        updateInterval,
                                                                        continuourRun,
                                                                        null);

        Classifier expectedRandomClassifier = new RandomClassifier(null,
                                                                    10,
                                                                    2,
                                                                    false,
                                                                    null);

        assertEquals(expectedRandomClassifier.getMaxIterations(), outputRandomClassifier.getMaxIterations());
        assertEquals(expectedRandomClassifier.getUpdateInterval(), outputRandomClassifier.getUpdateInterval());
        assertEquals(expectedRandomClassifier.tocontinue(), outputRandomClassifier.tocontinue());
    }

    /***
     *  Tests the bounds of the maxIterations field.
     *  @result The input is passed into a RandomClusterer Algorith object and compared to the expected output.
     */
    @Test
    public void iterationsLowerBoundTest() throws Exception {
        int     maxIterations       = 1;
        int     updateInterval      = 2;
        int     totalDistinctLabels = 4;
        boolean continuourRun       = false;

        Clusterer outputRandomClusterer = new RandomClusterer(null,
                maxIterations,
                updateInterval,
                totalDistinctLabels,
                4,
                continuourRun,
                null);

        assertEquals(1, outputRandomClusterer.getMaxIterations());
    }

    /***
     *  Tests the bounds of the updateInterval field.
     *  @result The input is passed into a RandomClusterer Algorith object and compared to the expected output.
     */
    @Test
    public void intervalLowerBoundTest() throws Exception {
        int     maxIterations       = 10;
        int     updateInterval      = 1;
        int     totalDistinctLabels = 4;
        boolean continuourRun       = false;

        Clusterer outputRandomClusterer = new RandomClusterer(null,
                maxIterations,
                updateInterval,
                totalDistinctLabels,
                4,
                continuourRun,
                null);

        assertEquals(1, outputRandomClusterer.getUpdateInterval());
    }

    /***
     *  Tests the bounds of the totalDistinctLabels field.
     *  @result The input is passed into a RandomClusterer Algorith object and compared to the expected output.
     */
    @Test
    public void totalDistictLabelsLowerBoundTest() throws Exception {
        int     maxIterations       = 10;
        int     updateInterval      = 1;
        int     totalDistinctLabels = 2;
        boolean continuourRun       = false;

        Clusterer outputRandomClusterer = new RandomClusterer(null,
                maxIterations,
                updateInterval,
                totalDistinctLabels,
                4,
                continuourRun,
                null);

        assertEquals(2, outputRandomClusterer.getNumberOfClusters());
    }

    /***
     *  Tests invalid input using code from the ClusteringConfigUI class and asserts whether or not the output
     *  is handled and replaced with default valid input.
     *  @result The input is passed into the RandomClusterer and handled using principles of graceful degradation.
     */
    @Test
    public void invalidInputRandomClustererRunConfigTest() throws Exception {
        int     maxIterations       = -1;
        int     updateInterval      = -1;
        int     totalDistinctLabels = -1;
        boolean continuourRun       = false;

        /* code from ClusteringConfigUI implementation */
        if(maxIterations <= 0)       { maxIterations = 1; }
        if(updateInterval <= 0)      { updateInterval = 1; }
        if(totalDistinctLabels <= 0) { totalDistinctLabels = 2; }

        Clusterer outputRandomClusterer = new RandomClusterer(null,
                maxIterations,
                updateInterval,
                totalDistinctLabels,
                4,
                continuourRun,
                null);

        assertEquals(1, outputRandomClusterer.getMaxIterations());
        assertEquals(1, outputRandomClusterer.getUpdateInterval());
        assertEquals(2, outputRandomClusterer.getNumberOfClusters());
        assertEquals(false, outputRandomClusterer.tocontinue());
    }

    /***
     *  Tests invalid input using code from the ClusteringConfigUI class and asserts whether or not the output
     *  is handled and replaced with default valid input.
     *  @result The input is passed into the KMeansClusterer and handled using principles of graceful degradation.
     */
    @Test
    public void invalidInputKMeansClustererRunConfigTest() throws Exception {
        int     maxIterations       = -1;
        int     updateInterval      = -1;
        int     totalDistinctLabels = -1;
        boolean continuourRun       = false;

        /* simulating ClusteringConfigUI implementation */
        if(maxIterations <= 0)       { maxIterations = 1; }
        if(updateInterval <= 0)      { updateInterval = 1; }
        if(totalDistinctLabels <= 0) { totalDistinctLabels = 2; }

        Clusterer outputKMeansClusterer = new KMeansClusterer(null,
                maxIterations,
                updateInterval,
                totalDistinctLabels,
                4,
                continuourRun,
                null);

        assertEquals(1, outputKMeansClusterer.getMaxIterations());
        assertEquals(1, outputKMeansClusterer.getUpdateInterval());
        assertEquals(2, outputKMeansClusterer.getNumberOfClusters());
        assertEquals(false, outputKMeansClusterer.tocontinue());
    }

    /***
     *  Tests invalid input using code from the ClassificationConfigUI class and asserts whether or not the output
     *  is handled and replaced with default valid input.
     *  @result The input is passed into the RandomClassifier and handled using principles of graceful degradation.
     */
    @Test
    public void invalidInputRandomClassifierRunConfigTest() throws Exception {
        int     maxIterations       = -1;
        int     updateInterval      = -1;
        boolean continuourRun       = false;

        /* simulating ClusteringConfigUI implementation */
        if(maxIterations <= 0)       { maxIterations = 1; }
        if(updateInterval <= 0)      { updateInterval = 1; }

        Classifier outputRandomClassifier = new RandomClassifier(null,
                                                                         maxIterations,
                                                                         updateInterval,
                                                                         continuourRun,
                                                                         null);

        assertEquals(1, outputRandomClassifier.getMaxIterations());
        assertEquals(1, outputRandomClassifier.getUpdateInterval());
        assertEquals(false, outputRandomClassifier.tocontinue());
    }


}
