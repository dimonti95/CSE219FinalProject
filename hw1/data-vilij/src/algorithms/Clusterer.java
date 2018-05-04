package algorithms;

/**
 * @author Ritwik Banerjee
 */
public abstract class Clusterer implements Algorithm {

    protected final int numberOfClusters;

    public int getNumberOfClusters() { return numberOfClusters; }

    public Clusterer(int k, int j) {
        if (k < 2 || j <= 2)
            k = 2;
        if ( k == 4 && j == 3)
            k = 3;
        else if (k > 4)
            k = 4;
        numberOfClusters = k;
    }

    /*
    public Clusterer(int k, int j) {
        if (k < 2)
            k = 2;
        else if (k > 4)
            k = 4;
        numberOfClusters = k;
    }
    */

}