import dataprocessors.TSDProcessor;
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

public class DataVisualizerTest {

    private TSDProcessor processor = new TSDProcessor();
    private String tsdTestString = "@a\tlabel1\t1,1";

    /** Tests for parsing line of data in TSD format to create an instance object (including boundary values) */

    @Test
    public void tsdValidDataTest(){
        try { processor.processString(tsdTestString); }
        catch (Exception e) { e.printStackTrace(); }

        Map                  output   = processor.getDataPoints();
        Map<String, Point2D> expected = new HashMap<>();
        expected.put("@a", new Point2D(1, 1));

        assertEquals(expected, output);
    }

    @Test
    public void tsdUpperBoundaryTest(){
        String twoDecimalsTSD = "@a\tlabel1\t1.11,1.11";

        try { processor.processString(twoDecimalsTSD); }
        catch (Exception e) { e.printStackTrace(); }

        Map                  output   = processor.getDataPoints();
        Map<String, Point2D> expected = new HashMap<>();
        expected.put("@a", new Point2D(1.11, 1.11));

        assertEquals(expected, output);
    }

    @Test
    public void tsdLowerBoundaryTest(){
        String twoDecimalsTSD = "@a\tnull\t1,1";

        try { processor.processString(twoDecimalsTSD); }
        catch (Exception e) { e.printStackTrace(); }

        Map                  output  = processor.getDataLabels();
        Map<String, String> expected = new HashMap<>();
        expected.put("@a", "null");

        assertEquals(expected, output);
    }


    /** Tests for saving data from the text-area in the UI to a .tsd file. */

    @Test
    public void saveTest() throws FileNotFoundException {
        String dataDirPath = "/" + "data";
        URL    dataDirURL  = getClass().getResource(dataDirPath);

        if (dataDirURL == null) throw new FileNotFoundException("Directory not found under resources.");

        File file = new File(dataDirURL.getFile());

        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(file.toPath()))) {
            writer.write((tsdTestString));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }




    /** Tests for Run configuration values for classification and clustering. (including boundary value analysis) */



}
