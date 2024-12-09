package org.example;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Point {
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

