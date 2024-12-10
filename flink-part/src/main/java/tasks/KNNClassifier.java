package tasks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.Serializable;

public class KNNClassifier implements Serializable {
    private List<Point> trainingSet;
    private int k;

    public KNNClassifier(int k) {
        this.k = k;
        this.trainingSet = new ArrayList<>();
    }

    public void train(List<Point> trainingData) {
        this.trainingSet.addAll(trainingData);
    }

    public String predict(double[] features) {
        List<DistanceLabel> distances = new ArrayList<>();
        for (Point point : trainingSet) {
            double distance = calculateEuclideanDistance(features, point.getFeatures());
            distances.add(new DistanceLabel(distance, point.getLabel()));
        }
        Collections.sort(distances);

        Map<String, Integer> labelCount = new HashMap<>();
        for (int i = 0; i < k; i++) {
            String label = distances.get(i).label;
            labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
        }

        return Collections.max(labelCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private double calculateEuclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }

    private class DistanceLabel implements Comparable<DistanceLabel> {
        double distance;
        String label;

        public DistanceLabel(double distance, String label) {
            this.distance = distance;
            this.label = label;
        }

        @Override
        public int compareTo(DistanceLabel other) {
            return Double.compare(this.distance, other.distance);
        }
    }
}
