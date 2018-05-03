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
    private ApplicationTemplate applicationTemplate;
    private int intervalCounter;

    private final int maxIterations;
    private final int updateInterval;

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
        this.intervalCounter = 0;
    }

    @Override
    public void run() {
        AppUI uiComponent = ((AppUI) applicationTemplate.getUIComponent());

        if      (tocontinue())  { Platform.runLater(uiComponent::disableToolbar);
                                  runAlgorithmContinuously();
                                  Platform.runLater(uiComponent::enableToolbar); }

        else if (!tocontinue()) { Platform.runLater(uiComponent::showIntervalButton);
                                  uiComponent.enableToolbar();
                                  uiComponent.getNewButton().setDisable(true);
                                  uiComponent.getLoadButton().setDisable(true);
                                  uiComponent.getSaveButton().setDisable(true);
                                  runAlgorithmInIntervals();
                                  uiComponent.enableToolbar();}

        uiComponent.getRunButton().setDisable(false);
        Platform.runLater(uiComponent::generateDataInformation); // back to Algorithm Type menu
    }

    private void runAlgorithmContinuously(){
        for (int i = 1; i <= maxIterations; i++) {
            int xCoefficient =  new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            intervalCounter++;

            if(intervalCounter == updateInterval) {
                intervalCounter = 0;
                showContinuousInterval();
            }

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i);
                flush();
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                showContinuousInterval();
                break;
            }
        }
    }

    private void showContinuousInterval(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Platform.runLater(
                () -> ((AppUI) applicationTemplate.getUIComponent()).displayIntervalIteration(output.get(0), output.get(1), output.get(2))
        );
    }

    private void runAlgorithmInIntervals() {
        for(int i = 1; i <= maxIterations; i++){
            int xCoefficient =  new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            intervalCounter++;

            if(intervalCounter == updateInterval) {
                intervalCounter = 0;
                showInterval();
            }

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i);
                flush();
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                showInterval();
                break;
            }
        }
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
    }

    private void showInterval(){
        AppUI uiComponent = ((AppUI) applicationTemplate.getUIComponent());
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            uiComponent.getNextIntervalBtn().setDisable(true);
            uiComponent.getScrnshotButton().setDisable(true); // simulating running algorithm
            Thread.sleep(1000);
            uiComponent.getNextIntervalBtn().setDisable(false);
            uiComponent.getScrnshotButton().setDisable(false); // simulating running algorithm
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Platform.runLater(
                () -> ((AppUI) applicationTemplate.getUIComponent()).displayIntervalIteration(output.get(0), output.get(1), output.get(2))
        );
    }

    // for internal viewing only
    private void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    public void notifyThread(){
        synchronized (this) {
            notify();
        }
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

