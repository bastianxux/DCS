package org.example;

import java.util.*;

public class KNNClassifier {

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
        // 计算距离并存储
        List<DistanceLabel> distances = new ArrayList<>();
        for (Point point : trainingSet) {
            double distance = calculateEuclideanDistance(features, point.getFeatures());
            distances.add(new DistanceLabel(distance, point.getLabel()));
        }

        // 排序
        Collections.sort(distances);

        // 取前k个点，统计类别
        Map<String, Integer> labelCount = new HashMap<>();
        for (int i = 0; i < k; i++) {
            String label = distances.get(i).label;
            labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
        }

        // 返回出现次数最多的类别
        return Collections.max(labelCount.entrySet(),
                Map.Entry.comparingByValue()).getKey();
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
