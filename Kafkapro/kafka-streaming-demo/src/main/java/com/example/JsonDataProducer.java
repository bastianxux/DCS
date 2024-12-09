package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class JsonDataProducer {
    public static void main(String[] args) {
        // 配置Kafka生产者属性
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // 创建Kafka生产者实例
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        try (BufferedReader br = new BufferedReader(new FileReader("data.json"))) {
            ObjectMapper objectMapper = new ObjectMapper();
            String line;
            while ((line = br.readLine())!= null) {
                JsonNode jsonNode = objectMapper.readTree(line);
                // 这里使用JSON数据中的某个字段作为键，或者可以使用其他合适的规则生成键，这里以 "x" 字段为例
                String key = jsonNode.get("x").asText();
                String value = jsonNode.toString();
                ProducerRecord<String, String> record = new ProducerRecord<>("wordin", key, value);
                producer.send(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
}
