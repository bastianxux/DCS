package tasks;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import java.util.Random;
import org.apache.flink.api.java.tuple.Tuple2;

public class RandomPointSource implements SourceFunction<Tuple2<Double, Double>> {
    private volatile boolean isRunning = true;

    @Override
    public void run(SourceContext<Tuple2<Double, Double>> ctx) throws Exception {
        Random random = new Random();
        while (isRunning) {
            // 生成随机的 x 和 y 值，保留两位小数
            double x = Math.round(random.nextDouble() * 100) / 100.0;
            double y = Math.round(random.nextDouble() * 100) / 100.0;
            ctx.collect(Tuple2.of(x, y));
            Thread.sleep(100); // 控制生成数据的频率，这里简单每100毫秒生成一个点
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}