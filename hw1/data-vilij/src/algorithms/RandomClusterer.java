package algorithms;

import javafx.application.Platform;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class RandomClusterer extends Clusterer{

    private static final Random RAND = new Random();
    private DataSet dataSet;
    private ApplicationTemplate applicationTemplate;
    private int intervalCounter;

    private final int maxIterations;
    private final int updateInterval;

    private final AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }

    public RandomClusterer(DataSet dataSet,
                           int maxIterations,
                           int updateInterval,
                           int numberOfClusters,
                           boolean tocontinue,
                           ApplicationTemplate applicationTemplate) {
        super(numberOfClusters);
        this.dataSet = dataSet;
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
                                  uiComponent.disableToolbar();
                                  uiComponent.getScrnshotButton().setDisable(false);
                                  runAlgorithmInIntervals();
                                  uiComponent.enableToolbar(); }

        uiComponent.getRunButton().setDisable(false);
        Platform.runLater(uiComponent::generateDataInformation); // back to Algorithm Type menu
    }

    /** Displaying each interval continuously */
    private void runAlgorithmContinuously(){
        for (int i = 1; i <= maxIterations; i++) {

            dataSet.getLabels().forEach((x,y) -> dataSet.updateLabel(x, String.valueOf(RAND.nextInt(numberOfClusters) + 1)));

            intervalCounter++;

            if(intervalCounter == updateInterval) {
                intervalCounter = 0;
                showContinuousInterval();
                System.out.println("Iteration: " + i); // for internal viewing
            }
        }
    }

    private void showContinuousInterval(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataSet.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart(), applicationTemplate);
    }

    /**  Displaying each interval in stages */
    private void runAlgorithmInIntervals() {
        for (int i = 1; i <= maxIterations; i++) {

            dataSet.getLabels().forEach((x,y) -> dataSet.updateLabel(x, String.valueOf(RAND.nextInt(numberOfClusters) + 1)));

            intervalCounter++;

            if(intervalCounter == updateInterval) {
                intervalCounter = 0;
                showInterval();
                System.out.println("Iteration: " + i); // for internal viewing
            }
        }
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
            uiComponent.getScrnshotButton().setDisable(true); // simulating running algorithm
            uiComponent.getNextIntervalBtn().setDisable(true);
            Thread.sleep(1000);
            uiComponent.getScrnshotButton().setDisable(false); // simulating running algorithm
            uiComponent.getNextIntervalBtn().setDisable(false);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException");
        }

        dataSet.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart(), applicationTemplate);
    }

    public void notifyThread(){
        synchronized (this) {
            notify();
        }
    }
    
}
