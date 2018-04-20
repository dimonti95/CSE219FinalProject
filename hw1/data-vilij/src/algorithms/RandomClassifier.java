package algorithms;

import javafx.application.Platform;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;
    private int     j; //test
    private ApplicationTemplate applicationTemplate;

    private final int maxIterations;
    private final int updateInterval;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue,
                            ApplicationTemplate applicationTemplate) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.applicationTemplate = applicationTemplate;
        initialize();
    }

    private void initialize() {
        j = 1; //test
    }

    @Override
    public void run() {
        if      (tocontinue())  { runAlgorithmContinuously(); }
        else if (!tocontinue()) { runAlgorithmInIntervals();  }
    }

    private void runAlgorithmContinuously(){
        for (int i = 1; i <= maxIterations; /*&& tocontinue();*/ i++) {
            int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int constant = new Double(RAND.nextDouble() * 100).intValue();

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            // the question for me is how do I take these 3 integers as output of this mock algorithm and
            // translate it into a 2D line each iteration?.....

            /* creating x-y plane values we will connect with a line in the chart */
            //constant     = -constant;                  //y intercept
            //xCoefficient = -xCoefficient/yCoefficient; //slope

            /* lets start by making it plot a random point that changes every iteration. then worry about the line */

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //((AppUI) applicationTemplate.getUIComponent()).displayIntervalIteration(xCoefficient, yCoefficient, constant);

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i);
                flush();
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                break;
            }
        }
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
    }

    private void runAlgorithmInIntervals() {
        for(; j <= updateInterval && j <= maxIterations; j++){
            int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int constant = new Double(RAND.nextDouble() * 100).intValue();

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (j % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", j);
                flush();
            }
            if (j > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", j);
                flush();
                break;
            }
        }
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
                //((AppUI) applicationTemplate.getUIComponent()).displayIntervalIteration(output.get(0), output.get(1), output.get(2));
        Platform.runLater(
                () -> {
                    ((AppUI) applicationTemplate.getUIComponent()).displayIntervalIteration(output.get(0), output.get(1), output.get(2));
                }
        );
    }

    /**
     * A placeholder main method to just make sure this code runs smoothly
     */
    public static void main(String... args) throws IOException {
        DataSet dataset = DataSet.fromTSDFile(Paths.get("/Users/nickdimonti/IdeaProjects/ndimonti/cse219homework/hw1/data-vilij/resources/data/sample-data.tsd"));
        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true, null);
        classifier.run(); // no multithreading yet
    }
}

