package com.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class SumStreamDemo {
    public static void main(String[] args) {
        Properties prop = new Properties();
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, "sum");
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        prop.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 3000);
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        builder.stream("sumIn") // 从sumin主题获取数据
                .map((key, value) -> new KeyValue<>("sum", value.toString()))    // 因为上面指定的是String类型的value值，所以将数值转为String类型
                .groupByKey()
                .reduce((value1, value2) -> {    // 数值相加功能，顺带输出下中间计算的结果，逻辑和spark的reduce功能完全相同
                    int sum = Integer.valueOf(value1) + Integer.valueOf(value2);
                    System.out.println(Integer.valueOf(value1) + "+" + Integer.valueOf(value2) + " = " + sum);
                    return Integer.toString(sum);
                })
                .toStream().to("sumOut");    // 重新转为stream后输出到sumout主题

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