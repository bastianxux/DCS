from pyspark import SparkContext
from pyspark import TaskContext
from pyspark import SparkConf
from pyspark.streaming import StreamingContext
import json
import time
import numpy as np

conf = SparkConf() \
    .setAppName("SocketStreamingApp") \
    .set("spark.default.parallelism", "8") \
    .set('spark.executor.memory', '2g') \
    .set("spark.executor.cores",
            '2')


sc = SparkContext(conf=conf)
# sc = SparkContext(appName="SocketStreamingApp")
# ssc = StreamingContext(sc, batchDuration=1)
# ssc = StreamingContext(sc, batchDuration=2)
ssc = StreamingContext(sc, batchDuration=4)
ssc.socketStreamReceiverErrorPolicy = "DISCONNECT"

HOST = 'socket-server'
PORT = 65432

socket_stream = ssc.socketTextStream(HOST, PORT)

# 加载 points 数据
points_path = "../data/points_with_labels.json"
points = sc.textFile(points_path).map(json.loads).collect()
points_with_labels = [(np.array([p['x'], p['y']]), p['label']) for p in points]  # 包含标签的元组列表
points_coords = np.array([p[0] for p in points_with_labels])  # 仅包含坐标部分


# k-NN 计算
def compute_knn(query_point, k=5):
    distances = np.linalg.norm(points_coords - query_point, axis=1)  # L2 距离
    nearest_indices = np.argsort(distances)[:k]
    nearest_points = [(points_with_labels[idx][0], points_with_labels[idx][1]) for idx in nearest_indices]
    nearest_distances = distances[nearest_indices]
    return nearest_points, nearest_distances


def process_batch(rdd):
    def process_partition(partition):
        context = TaskContext.get()
        print(context)
        # if context is None:
        #     print("TaskContext is not available. This code is running in Driver or improperly configured context.")
        #     return []
        # node_info = f"Node: {context.hostname()}, Partition: {context.partitionId()}" if context else "Node: Unknown"
        results = []
        for record in partition:
            try:
                x, y = map(float, record.split(","))
                query_point = np.array([x, y])
                nearest_points, nearest_distances = compute_knn(query_point)

                result = {
                    # "node_info": node_info,
                    "query_point": query_point.tolist(),
                    "nearest_points": nearest_points,
                    "nearest_distances": nearest_distances.tolist(),
                }
                results.append(result)
            except ValueError:
                results.append({"node_info": "node_info", "error": f"Invalid record: {record}"})
        return results

    if not rdd.isEmpty():
        partition_results = rdd.mapPartitions(process_partition).collect()
        for result in partition_results:
            print(result)


def process_batch_with_metrics(rdd):
    if not rdd.isEmpty():
        start_time = time.time()
        batch_size = rdd.count()
        print(f"Batch size: {batch_size}")

        # 正常处理数据
        process_batch(rdd)

        end_time = time.time()
        batch_duration = end_time - start_time  # 处理时间
        print(f"Batch processed in {batch_duration:.2f} seconds")
        if batch_duration > 0:
            print(f"Throughput: {batch_size / batch_duration:.2f} records/second")


# 设置流式处理
socket_stream.foreachRDD(process_batch_with_metrics)

# 启动 StreamingContext
ssc.start()
ssc.awaitTermination()