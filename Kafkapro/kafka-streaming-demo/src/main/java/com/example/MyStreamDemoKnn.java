package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.streams.KeyValue;

public class MyStreamDemoKnn {
    public static void main(String[] args) {
        // 创建Properties对象，配置Kafka Streaming配置项
        Properties prop = new Properties();
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, "demo");
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // 创建流构造器
        StreamsBuilder builder = new StreamsBuilder();

        // 假设训练数据文件路径，根据实际情况修改
        String trainingDataFilePath = "/data.json";
        double[][] trainingData = TrainingDataLoader.getTrainingData(trainingDataFilePath);
        int[] trainingLabels = TrainingDataLoader.getTrainingLabels(trainingDataFilePath);

        // 创建KNN实例，设置近邻数量k（这里假设k为3，可根据实际情况调整）
        KNN knn = new KNN(3, trainingData, trainingLabels);

        // 从主题读取数据，进行KNN计算后，将结果输出到新的主题（假设为“knn_result”）
        builder.stream("wordin")
                .map((key, value) -> new KeyValue<>("sum", value.toString()))    // 因为上面指定的是String类型的value值，所以将数值转为String类型
                .groupByKey()
                .reduce((value1, value2) -> {    // 数值相加功能，顺带输出下中间计算的结果，逻辑和spark的reduce功能完全相同
                    int sum = Integer.valueOf(value1) + Integer.valueOf(value2);
                    System.out.println(Integer.valueOf(value1) + "+" + Integer.valueOf(value2) + " = " + sum);
                    return Integer.toString(sum);
                })
                .toStream()
                .to("knn_result");

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