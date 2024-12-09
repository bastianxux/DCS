package com.example;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

// 让Point类实现Clusterable接口，满足KMeansPlusPlusClusterer泛型参数要求
class Point implements Clusterable {
    double x;
    double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Point fromString(String str) {
        String[] parts = str.split(",");
        return new Point(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    @Override
    public double[] getPoint() {
        return new double[]{x, y};
    }
}

public class MyKnnStream {
    public static void main(String[] args) {
        // 创建Properties对象，配置Kafka Streaming配置项
        Properties prop = new Properties();
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, "demo");
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // 创建流构造器
        StreamsBuilder builder = new StreamsBuilder();

        // 从输入主题获取流数据
        KStream<String, String> inputStream = builder.stream("in");

        // 对输入流数据进行处理，这里进行KNN计算（简单示例，实际更复杂）
        KStream<String, String> outputStream = inputStream.mapValues(value -> {
            Point inputPoint = Point.fromString(value);
            // 这里简单模拟已有一些聚类中心（实际应用中这些聚类中心可能来自历史数据训练等）
            List<Point> centroids = new ArrayList<>();
            centroids.add(new Point(1.0, 1.0));
            centroids.add(new Point(5.0, 5.0));
            centroids.add(new Point(10.0, 10.0));

            // 使用KMeansPlusPlusClusterer辅助计算最近邻（简单替代KNN思路，实际应用应使用专业KNN库）
            KMeansPlusPlusClusterer<Point> clusterer = new KMeansPlusPlusClusterer<>(3, 100, new EuclideanDistance());
            List<CentroidCluster<Point>> clusters = clusterer.cluster(centroids);

            double minDistance = Double.MAX_VALUE;
            Point nearestCentroid = null;
            for (CentroidCluster<Point> cluster : clusters) {
                Point centroid = (Point) cluster.getCenter();
                double distance = new EuclideanDistance().compute(inputPoint.getPoint(), centroid.getPoint());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestCentroid = centroid;
                }
            }

            // 返回最近邻的信息（这里简单返回坐标字符串，可按需求调整格式）
            return nearestCentroid.x + "," + nearestCentroid.y;
        });

        // 将处理后的数据写入到输出主题
        outputStream.to("out");

        // 构建Topology结构
        Topology topology = builder.build();
        final KafkaStreams kafkaStreams = new KafkaStreams(topology, prop);

        // 固定的启动方式
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread("stream") {
            @Override
            public void run() {
                kafkaStreams.close();
                latch.countDown();
            }
        });

        kafkaStreams.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}