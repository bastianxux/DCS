package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainingDataLoader {
    public static double[][] getTrainingData(String filePath) {
        List<double[]> dataList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine())!= null) {
                JsonNode jsonNode = objectMapper.readTree(line);
                double x = jsonNode.get("x").asDouble();
                double y = jsonNode.get("y").asDouble();
                double[] features = {x, y};
                dataList.add(features);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList.toArray(new double[0][0]);
    }

    public static int[] getTrainingLabels(String filePath) {
        List<Integer> labelList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine())!= null) {
                JsonNode jsonNode = objectMapper.readTree(line);
                String labelStr = jsonNode.get("label").asText();
                int label = getLabelValue(labelStr);
                labelList.add(label);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return labelList.stream().mapToInt(Integer::intValue).toArray();
    }

    private static int getLabelValue(String labelStr) {
        // 根据实际标签情况进行转换，这里简单示例将 "A" 转换为 0，"B" 转换为 1，"C" 转换为 2，可根据实际调整
        if ("A".equals(labelStr)) {
            return 0;
        } else if ("B".equals(labelStr)) {
            return 1;
        } else {
            return 2;
        }
    }
}