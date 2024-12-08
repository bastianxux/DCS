package com.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class WordCount {
    public static void main(String[] args) {
        Properties prop = new Properties();
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordCount");
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        prop.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 3000);
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        builder.stream("wordin")    // 从wordin导入数据
                .flatMapValues((value) -> Arrays.asList(value.toString().split("\\s+")))    // 按照空白字符切割为多个单词
                .map((key, value) -> new KeyValue<>(value, "1"))    // 转换为（单词，1）的键值对形式
                .groupByKey()    // 根据单词分组
                .count()        // 计算各分组value的数量
                .toStream()
                .map(((key, value) -> new KeyValue<>(key, key + " : " + value.toString())))
                .to("wordout");    // 输出到wordout

        Topology topology = builder.build();
        final KafkaStreams kafkaStreams = new KafkaStreams(topology, prop);

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