package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class DataPoint {
    double x;
    double y;
    String label;

    // 添加默认构造方法，供Jackson进行反序列化使用
    public DataPoint() {
    }

    public DataPoint(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }
}

public class KnnClassifier {

    private List<DataPoint> dataSet = new ArrayList<>();

    public KnnClassifier() {
        try {
            InputStream inputStream = KnnClassifier.class.getClassLoader().getResourceAsStream("data.json");
            if (inputStream!= null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine())!= null) {
                    // 使用ObjectMapper解析每一行JSON数据为DataPoint对象
                    ObjectMapper objectMapper = new ObjectMapper();
                    DataPoint dataPoint = objectMapper.readValue(line, DataPoint.class);
                    dataSet.add(dataPoint);
                }
                reader.close();
            } else {
                System.out.println("找不到data.json文件");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 计算两点之间的欧几里得距离
    private double euclideanDistance(DataPoint p1, DataPoint p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // KNN算法核心逻辑，返回预测的标签，添加了对数据集为空的判断
    public String predict(DataPoint input, int k) {
        if (dataSet.isEmpty()) {
            System.out.println("数据集为空，无法进行预测，请检查data.json文件读取及解析是否正确。");
            return null;
        }
        List<DataPointDistance> distances = new ArrayList<>();
        // 计算输入点与数据集中每个点的距离，并记录
        for (DataPoint dataPoint : dataSet) {
            double distance = euclideanDistance(input, dataPoint);
            distances.add(new DataPointDistance(dataPoint, distance));
        }

        // 按照距离从小到大排序
        Collections.sort(distances, Comparator.comparingDouble(DataPointDistance::getDistance));

        // 选取距离最近的K个邻居
        List<DataPoint> neighbors = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            neighbors.add(distances.get(i).getDataPoint());
        }

        // 进行多数表决，统计每个标签出现的次数
        int countA = 0;
        int countB = 0;
        int countC = 0;
        for (DataPoint neighbor : neighbors) {
            if (neighbor.label.equals("A")) {
                countA++;
            } else if (neighbor.label.equals("B")) {
                countB++;
            } else {
                countC++;
            }
        }

        // 返回出现次数最多的标签作为预测结果
        if (countA >= countB && countA >= countC) {
            return "A";
        } else if (countB >= countA && countB >= countC) {
            return "B";
        } else {
            return "C";
        }
    }

    // 内部类，用于记录数据点和其到输入点的距离
    private class DataPointDistance {
        private DataPoint dataPoint;
        private double distance;

        public DataPointDistance(DataPoint dataPoint, double distance) {
            this.dataPoint = dataPoint;
            this.distance = distance;
        }

        public DataPoint getDataPoint() {
            return dataPoint;
        }

        public double getDistance() {
            return distance;
        }
    }

    public static void main(String[] args) {
        KnnClassifier knnClassifier = new KnnClassifier();
        DataPoint inputPoint = new DataPoint(50, 50, "");  // 这里示例输入一个点，可根据实际修改坐标
        int k = 5;  // 设定K值，可根据实际调整
        String predictedLabel = knnClassifier.predict(inputPoint, k);
        if (predictedLabel!= null) {
            System.out.println("预测的标签为: " + predictedLabel);
        }
    }
}