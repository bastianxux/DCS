package com.example;

import org.apache.commons.math3.util.MathArrays;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KNN {
    private int k;
    private double[][] trainingData;
    private int[] trainingLabels;

    public KNN(int k, double[][] trainingData, int[] trainingLabels) {
        this.k = k;
        this.trainingData = trainingData;
        this.trainingLabels = trainingLabels;
    }

    public int predict(double[] inputFeatureVector) {
        double[] distances = new double[trainingData.length];
        // 计算输入特征向量与训练数据中各样本的距离（使用欧式距离公式）
        for (int i = 0; i < trainingData.length; i++) {
            double[] diff = MathArrays.ebeSubtract(trainingData[i], inputFeatureVector);
            distances[i] = calculateEuclideanDistance(diff);
        }

        // 使用Java内置的排序方法对距离数组进行排序
        Arrays.sort(distances);

        // 获取最近的k个邻居的距离
        double[] nearestNeighbors = Arrays.copyOfRange(distances, 0, k);

        // 进行简单的投票机制来确定预测类别（这里可根据实际情况优化）
        int[] labelCounts = new int[getUniqueLabels().length];
        for (double distance : nearestNeighbors) {
            int index = Arrays.binarySearch(distances, distance);
            int label = trainingLabels[index];
            labelCounts[label]++;
        }

        return getMostFrequentLabel(labelCounts);
    }

    // 封装计算欧式距离的方法，提高代码可读性，并处理可能的数据类型问题
    private double calculateEuclideanDistance(double[] diff) {
        double sumOfSquares = 0;
        for (double element : diff) {
            sumOfSquares += element * element;
        }
        return Math.sqrt(sumOfSquares);
    }

    private int[] getUniqueLabels() {
        Set<Integer> uniqueLabels = new HashSet<>();
        for (int label : trainingLabels) {
            uniqueLabels.add(label);
        }
        return uniqueLabels.stream().mapToInt(Integer::intValue).toArray();
    }

    private int getMostFrequentLabel(int[] labelCounts) {
        int maxCount = 0;
        int predictedLabel = -1;
        int[] uniqueLabels = getUniqueLabels();
        for (int i = 0; i < uniqueLabels.length; i++) {
            if (labelCounts[i] > maxCount) {
                maxCount = labelCounts[i];
                predictedLabel = uniqueLabels[i];
            }
        }
        return predictedLabel;
    }
}