#!/bin/bash

. "/opt/spark/bin/load-spark-env.sh"

#if [ "$SPARK_WORKLOAD" == "master" ]; then
#    export SPARK_MASTER_HOST=$(hostname)
#    cd /opt/spark/bin && ./spark-class org.apache.spark.deploy.master.Master --webui-port $SPARK_MASTER_WEBUI_PORT >>$SPARK_MASTER_LOG
#elif [ "$SPARK_WORKLOAD" == "worker" ]; then
#    cd /opt/spark/bin && ./spark-class org.apache.spark.deploy.worker.Worker --webui-port $SPARK_WORKER_WEBUI_PORT $SPARK_MASTER >>$SPARK_WORKER_LOG
if [ "$SPARK_WORKLOAD" == "master" ]; then
    export SPARK_MASTER_HOST=$(hostname)
    cd /opt/spark/bin && ./spark-class org.apache.spark.deploy.master.Master --webui-port $SPARK_MASTER_WEBUI_PORT --driver-ui-port SPARK_MASTER_DRIVER_UI_PORT >>$SPARK_MASTER_LOG
elif [ "$SPARK_WORKLOAD" == "worker" ]; then
    cd /opt/spark/bin && ./spark-class org.apache.spark.deploy.worker.Worker --webui-port $SPARK_WORKER_WEBUI_PORT --driver-ui-port SPARK_WORKER_DRIVER_UI_PORT $SPARK_MASTER >>$SPARK_WORKER_LOG
elif [ "$SPARK_WORKLOAD" == "submit" ]; then
    echo "SPARK SUBMIT"
else
    echo "Undefined Workload Type $SPARK_WORKLOAD, must specify: master, worker, submit"
fi
