package algorithms;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private DataSet       dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;

    /** added */
    private final boolean       isContinuous;
    private int                 intervalCounter;
    private ApplicationTemplate applicationTemplate;

    public KMeansClusterer(DataSet dataset,
                           int maxIterations,
                           int updateInterval,
                           int numberOfClusters,
                           int numberOfInstances,
                           boolean isContinuous,
                           ApplicationTemplate applicationTemplate) {
        super(numberOfClusters, numberOfInstances);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.isContinuous = isContinuous;
        this.tocontinue = new AtomicBoolean(false);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }

    @Override
    public void run() {
        AppUI uiComponent = ((AppUI) applicationTemplate.getUIComponent());

        if      (isContinuous)  { Platform.runLater(uiComponent::disableToolbar);
                                  runAlgorithmContinuously();
                                  Platform.runLater(uiComponent::enableToolbar); }

        else                    { uiComponent.disableToolbar();
                                  Platform.runLater(uiComponent::showIntervalButton);
                                  uiComponent.getScrnshotButton().setDisable(false);
                                  runAlgorithmInIntervals();
                                  uiComponent.enableToolbar(); }

        uiComponent.getRunButton().setDisable(false);
        Platform.runLater(uiComponent::generateDataInformation); // back to Algorithm Type menu
    }

    /** Continuous Run methods */
    private void runAlgorithmContinuously(){
        initializeCentroids();
        int iteration = 0;
        while (iteration++ < maxIterations & tocontinue.get()) {
            assignLabels();
            recomputeCentroids();
            if(!tocontinue.get()) { showContinuousInterval(); System.out.println("Iteration: " + iteration); return; } // display final iteration if algorithm stops
            intervalCounter++;
            if(intervalCounter == updateInterval) {
                intervalCounter = 0;
                showContinuousInterval();
                System.out.println("Iteration: " + iteration); // for internal viewing
            }
        }
    }

    /* called from runAlgorithmContinuously() */
    private void showContinuousInterval(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataset.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart(), applicationTemplate);
    }

    /**  Displaying each interval in stages */
    private void runAlgorithmInIntervals() {
        initializeCentroids();
        int iteration = 0;
        while (iteration++ < maxIterations & tocontinue.get()) {
            assignLabels();
            recomputeCentroids();
            if(!tocontinue.get()) { showInterval(); System.out.println("Iteration: " + iteration); return; } // display final iteration if algorithm stops
            intervalCounter++;
            if(intervalCounter == updateInterval) {
                intervalCounter = 0;
                showInterval();
                System.out.println("Iteration: " + iteration); // for internal viewing
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
            uiComponent.getScrnshotButton().setDisable(true); // running algorithm
            uiComponent.getNextIntervalBtn().setDisable(true);
            Thread.sleep(1000);
            uiComponent.getNextIntervalBtn().setDisable(false);
            uiComponent.getScrnshotButton().setDisable(false); // running algorithm
        } catch (InterruptedException e) {
            System.out.println("Interrupted Exception");
        }

        dataset.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart(), applicationTemplate);
    }

    public void notifyThread(){
        synchronized (this) {
            notify();
        }
    }

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                i = (++i % instanceNames.size()); //bugfix
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance      = Double.MAX_VALUE;
            int    minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }




}