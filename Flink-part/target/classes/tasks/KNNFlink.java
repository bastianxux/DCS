package tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class KNNFlink {

    private static KNNClassifier classifier = initClassifier();

    private static KNNClassifier initClassifier() {
        KNNClassifier classifier = new KNNClassifier(5);
        List<Point> data = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<String> lines = Files.readAllLines(Paths.get("/points_with_labels.json"));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                Map<String, Object> pointInfo = objectMapper.readValue(line, Map.class);

                double[] features = {((Double) pointInfo.get("x")).doubleValue(), ((Double) pointInfo.get("y")).doubleValue()};
                String label = (String) pointInfo.get("label");
                data.add(new Point(features, label));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        classifier.train(data);
        return classifier;
    }

    public static class DataSource implements SourceFunction<String> {
        private volatile boolean isRunning = true;

        @Override
        public void run(SourceContext<String> ctx) throws Exception {
            ObjectMapper objectMapper = new ObjectMapper();
            Random random = new Random();
            while (isRunning) {
                double x = Math.round(random.nextDouble() * 100) / 100.0;
                double y = Math.round(random.nextDouble() * 100) / 100.0;
                Map<String, Double> point = new HashMap<>();
                point.put("x", x);
                point.put("y", y);
                String jsonString = objectMapper.writeValueAsString(point);
                ctx.collect(jsonString);
                Thread.sleep(0);  // 100  50  10 0
            }
        }
        @Override
        public void cancel() {
            isRunning = false;
        }
    }

    public static class KNNProcessFunction extends RichMapFunction<String, Tuple2<String, Long>> {
        private final KNNClassifier knnClassifier = classifier;

        @Override
        public Tuple2<String, Long> map(String value) throws Exception {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Double> jsonMap = objectMapper.readValue(value, Map.class);
            double x = jsonMap.get("x");
            double y = jsonMap.get("y");
            double[] features = {x, y};
            String label = knnClassifier.predict(features);
            long currentTime = System.currentTimeMillis();
            return Tuple2.of("A", currentTime);
        }
    }

    public static class LatencyAggregateFunction implements AggregateFunction<Tuple2<String, Long>, Tuple2<Long, Long>, Double> {
        @Override
        public Tuple2<Long, Long> createAccumulator() {
            return Tuple2.of(0L, 0L);
        }

        @Override
        public Tuple2<Long, Long> add(Tuple2<String, Long> value, Tuple2<Long, Long> accumulator) {
            long latency = System.currentTimeMillis() - value.f1;
            return Tuple2.of(accumulator.f0 + latency, accumulator.f1 + 1);
        }

        @Override
        public Double getResult(Tuple2<Long, Long> accumulator) {
            return (double) accumulator.f0 / accumulator.f1;
        }

        @Override
        public Tuple2<Long, Long> merge(Tuple2<Long, Long> a, Tuple2<Long, Long> b) {
            return Tuple2.of(a.f0 + b.f0, a.f1 + b.f1);
        }
    }

    public static class MaxDataVolumeAggregateFunction implements AggregateFunction<Tuple2<String, Long>, Tuple2<Integer, Integer>, Integer> {
        @Override
        public Tuple2<Integer, Integer> createAccumulator() {
            return Tuple2.of(0, 0);
        }
        @Override
        public Tuple2<Integer, Integer> add(Tuple2<String, Long> value, Tuple2<Integer, Integer> accumulator) {
            int currentCount = accumulator.f0 + 1;
            int maxCount = Math.max(currentCount, accumulator.f1);
            return Tuple2.of(currentCount, maxCount);
        }
        @Override
        public Integer getResult(Tuple2<Integer, Integer> accumulator) {
            return accumulator.f1;
        }
        @Override
        public Tuple2<Integer, Integer> merge(Tuple2<Integer, Integer> a, Tuple2<Integer, Integer> b) {
            int mergedMax = Math.max(a.f1, b.f1);
            return Tuple2.of(0, mergedMax);
        }
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        DataStream<String> dataStream = env.addSource(new DataSource()).setParallelism(1);

        SingleOutputStreamOperator<Tuple2<String, Long>> knnResultStream = dataStream
                .map(new KNNProcessFunction())
                .setParallelism(4);

        SingleOutputStreamOperator<Tuple2<String, Long>> withTimestamps = knnResultStream
                .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<Tuple2<String, Long>>(Time.seconds(10)) {
                    @Override
                    public long extractTimestamp(Tuple2<String, Long> element) {
                        return element.f1;
                    }
                });

        // 将时间窗口大小调整为1分钟，并使用新的聚合函数进行聚合操作
        SingleOutputStreamOperator<Integer> maxDataVolumeStream = withTimestamps
                .keyBy(t -> t.f0)
                .timeWindow(Time.minutes(1))
                .aggregate(new MaxDataVolumeAggregateFunction());

        maxDataVolumeStream.print();

        env.execute("Flink KNN Example");
    }
}