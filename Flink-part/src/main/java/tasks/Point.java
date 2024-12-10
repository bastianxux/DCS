package tasks;

import java.io.Serializable;

public class Point implements Serializable {
    private double[] features;
    private String label;

    public Point(double[] features, String label) {
        this.features = features;
        this.label = label;
    }

    public double[] getFeatures() {
        return features;
    }

    public String getLabel() {
        return label;
    }
}