package org.example;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.topology.BasicOutputCollector;

import java.io.IOException;
import java.util.*;
import java.io.BufferedReader;
import java.net.Socket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.tinylog.Logger;
import java.io.FileReader;


public class KNNTopology {
    private static KNNClassifier classifier =new KNNClassifier(100000) ;
    static  {
        List<Point> data = new ArrayList<>();
        String filePath = "/points_with_labels.json";
        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Map<String, Object> jsonMap = objectMapper.readValue(line, Map.class);
                double x = (Double) jsonMap.get("x");
                double y = (Double)jsonMap.get("y");
                String label = (String) jsonMap.get("label");
                double[] features = {x, y};
                Point point = new Point(features, label);
                data.add(point);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        classifier.train(data);
    }

    public static class DataSpout extends BaseRichSpout {
        private SpoutOutputCollector collector;

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            this.collector = collector;
        }

        @Override
        public void nextTuple() {
            ObjectMapper objectMapper = new ObjectMapper();

            Random random = new Random();

            long emitTime = System.currentTimeMillis();

            double x = Math.round(random.nextDouble() * 100) / 100.0;
            double y = Math.round(random.nextDouble() * 100) / 100.0;

            Map<String, Double> point = new HashMap<>();
            point.put("x", x);
            point.put("y", y);
            String jsonString = "";
            try {
                jsonString = objectMapper.writeValueAsString(point);
            } catch (Exception e) {
                e.printStackTrace();
            }
            collector.emit(new Values(jsonString,emitTime));

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("Date","Time"));
        }
        @Override
        public void close() {
        }
    }

    public static class DataKNN extends BaseBasicBolt {
        private long sum = 0;
        private long windowSizeMs = 10000;  // 10000 毫秒的窗口大小
        private long count = 0;
        public void execute(Tuple input, BasicOutputCollector collector) {
            String DateLine = input.getStringByField("Date");
            long emitTime = input.getLongByField("Time");
            ObjectMapper objectMapper = new ObjectMapper();
            // 将 JSON 字符串反序列化
            Map<String, Double> jsonMap ;
            try {
                jsonMap = objectMapper.readValue(DateLine, Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            double x = jsonMap.get("x");
            double y = jsonMap.get("y");
            double[] features = {x, y};
            String res = classifier.predict(features);
            Logger.info("Knn res for message: {}",res);
            long currentTime = System.currentTimeMillis();
            long latency = currentTime - emitTime;
            long late = 0;
            sum += latency;
            count += 1;
            late = sum;
            if(sum >= windowSizeMs) {
                sum = sum/count;
                //Logger.info("latency for message: {}ms ,count for message: {}",sum, count);
                count = 0;
                sum = 0;
            }
            collector.emit(new Values("jsonString",late));
        }
        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("lab","time"));
        }
    }

    public static class Count extends BaseBasicBolt{
        private long DateSum = 0;
        private long TimeSum = 0;
        public void execute(Tuple input, BasicOutputCollector collector) {
            DateSum += 1;
            long emitTime = input.getLongByField("time");
            TimeSum += emitTime;
            if(emitTime >= 10000) {
                //Logger.info("sum for message: {}",DateSum);
            }
        }
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }
    }

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("DataSpout", new DataSpout(),4);
        builder.setBolt("KNN", new DataKNN(),4)
                .shuffleGrouping("DataSpout");
        builder.setBolt("Count", new Count()).globalGrouping("KNN");
        Config conf = new Config();
        conf.setNumWorkers(2);
        //集群方式提交
        StormSubmitter.submitTopologyWithProgressBar("wordCount", conf,
                builder.createTopology());
    }
}