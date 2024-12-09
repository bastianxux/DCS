package com.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class MyKafkaStreaming {
    public static void main(String[] args) {
        // 创建Properties对象，配置Kafka Streaming配置项
        Properties prop = new Properties();
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, "demo");                                  // 配置任务名称
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");                      // 配置Kafka主机IP和端口，根据实际情况修改
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());     // 配置Key值类型
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());   // 配置Value值类型

        // 创建流构造器
        StreamsBuilder builder = new StreamsBuilder();

        // 用构造好的builder将in数据写入到out
        builder.stream("in").to("out");

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