package algorithms;

import javafx.application.Platform;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

public class RandomClusterer extends Clusterer{

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

    @Override
    public void run() {
        AppUI uiComponent = ((AppUI) applicationTemplate.getUIComponent());
        dataSet.setLabelsNull();
        dataSet.getLabels().forEach((x,y) -> System.out.println(y)); //test

        if      (tocontinue())  { Platform.runLater(uiComponent::disableToolbar);
                                  runAlgorithmContinuously();
                                  Platform.runLater(uiComponent::enableToolbar); }

        else if (!tocontinue()) { Platform.runLater(uiComponent::showIntervalButton);
            uiComponent.enableToolbar();
            uiComponent.getNewButton().setDisable(true);
            uiComponent.getLoadButton().setDisable(true);
            uiComponent.getSaveButton().setDisable(true);
            //runAlgorithmInIntervals();
            uiComponent.enableToolbar();}
    }

    private void runAlgorithmContinuously(){
        for (int i = 1; i <= maxIterations; i++) {


        }
    }


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

    //for testing purposes
    public static void main(String[] args){

    }

}
