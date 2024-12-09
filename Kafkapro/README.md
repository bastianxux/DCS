一、项目概述
本项目旨在复现基于 Kafka Streaming 的多个数据处理案例，包括基础数据传输以帮助初学者理解和掌握 Kafka Streaming 的基本功能和使用方法。
二、环境准备
（一）安装 Java
1. 从 Oracle 官网（https://www.oracle.com/java/technologies/javase-downloads.html）下载适合您操作系统的 Java Development Kit（JDK）版本，建议选择 Java 8 或更高版本。
2. 按照安装向导完成 Java 的安装。安装完成后，打开命令行终端，输入java -version命令，如果能够正确显示 Java 版本信息，则表示安装成功。
（二）安装 Kafka
1. 从 Kafka 官方网站（https://kafka.apache.org/downloads）下载最新稳定版本的 Kafka。
2. 解压下载的压缩包到您选择的目录，例如在 Linux 系统下，可以使用命令tar -zxvf kafka_<version>.tgz（将<version>替换为实际下载的版本号）。

3. 进入解压后的 Kafka 目录，编辑配置文件config/server.properties。主要配置项如下：
  ○ broker.id：为每个 Kafka broker 设置唯一的 ID，例如broker.id=0。
  ○ listeners：指定 Kafka 监听的地址和端口，如listeners=PLAINTEXT://localhost:9092（表示使用本地地址和 9092 端口进行通信，如果在服务器环境中，需根据实际情况修改为服务器的 IP 地址）。
  ○ log.dirs：设置 Kafka 存储日志文件的目录，例如log.dirs=/tmp/kafka-logs（可根据实际需求修改目录路径）。
4. 启动 Zookeeper（Kafka 依赖 Zookeeper 进行协调管理）。在 Kafka 目录下，执行命令bin/zookeeper-server-start.sh config/zookeeper.properties（Linux 系统下）。如果是 Windows 系统，则执行bin\windows\zookeeper-server-start.bat config\zookeeper.properties。

5. 启动 Kafka broker。在另一个命令行窗口中，执行bin/kafka-server-start.sh config/server.properties（Linux 系统）或bin\windows\kafka-server-start.bat config\server.properties（Windows 系统）。

（三）安装 Maven
1. 从 Maven 官方网站（https://maven.apache.org/download.cgi）下载最新版本的 Maven 二进制压缩包。
2. 解压压缩包到您指定的目录，例如在 Linux 系统下使用tar -zxvf apache-maven-<version>.tar.gz（将<version>替换为实际下载的版本号）。
3. 配置 Maven 环境变量。编辑~/.bashrc文件（如果使用的是其他 Shell 环境，对应编辑相应的配置文件），添加以下内容（假设 Maven 解压到/opt/maven目录下，根据实际情况修改路径）：
export MAVEN_HOME=/opt/maven
export PATH=$PATH:$MAVEN_HOME/bin
保存后，在命令行执行source ~/.bashrc使配置生效。在命令行输入mvn -version，如果能正确显示 Maven 版本信息，则表示安装成功。
三、项目创建与依赖导入
（一）创建 Maven 项目
1. 打开命令行终端，进入您想要创建项目的目录。
2. 执行以下命令创建一个 Maven 项目：
mvn archetype:generate -DgroupId=com.example -DartifactId=kafka-streaming-demo -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
其中，groupId是项目的组织唯一标识符，artifactId是项目的唯一标识符，这里创建了一个名为kafka-streaming-demo的项目。
（二）导入依赖
1. 进入项目目录kafka-streaming-demo，找到pom.xml文件，使用文本编辑器打开。
2. 在<dependencies>标签内添加以下依赖项：
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.0.0</version>
</dependency>
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka_2.11</artifactId>
    <version>2.0.0</version>
</dependency>
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-streams</artifactId>
    <version>2.0.0</version>
</dependency>

这些依赖项用于在项目中使用 Kafka 相关功能，版本号可根据实际情况调整，但需确保相互兼容。
3. 保存pom.xml文件后，在命令行执行mvn clean install命令，Maven 会自动下载并安装相关依赖到本地仓库。
四、案例复现
基础数据传输案例
1. 在项目的src/main/java/com/example目录下（根据项目的 groupId 和 artifactId 可能有所不同），创建一个名为MyStreamDemo.java的 Java 类。
2. 复制以下代码到MyStreamDemo.java文件中：
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class MyStreamDemo {
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
1. 在命令行中，进入 Kafka 安装目录，使用以下命令创建 “in” 和 “out” topic（如果已经存在则无需重复创建）：
代码有问题：
bin/kafka-topics.sh --create --topic in --partitions 1 --replication-factor 1 --zookeeper localhost:2181
bin/kafka-topics.sh --create --topic out --partitions 1 --replication-factor 1 --zookeeper localhost:2181
注意改为
从你执行创建 Kafka 主题的命令及报错信息来看，问题在于你使用的 Kafka 版本（3.9.0）中，kafka-topics.sh命令的参数发生了变化，不再使用--zookeeper参数来指定 Zookeeper 地址。在这个版本中，应该使用--bootstrap-server参数来指定 Kafka 集群的地址（在单机环境下就是 Kafka broker 的地址）。
以下是正确的命令格式：
创建 “in” 主题
bin/kafka-topics.sh --create --topic in --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
创建 “out” 主题
bin/kafka-topics.sh --create --topic out --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
请注意，如果你在实际环境中 Kafka broker 监听的端口不是 9092，需要将上述命令中的端口号修改为实际的端口号。
在 Kafka 3.9.0 版本中，--bootstrap-server参数用于与 Kafka 集群进行通信，而 Zookeeper 的连接信息是通过 Kafka 的配置文件（config/server.properties）中的zookeeper.connect参数来配置的，Kafka 客户端（包括kafka-topics.sh等工具）会根据配置文件中的信息自动连接到 Zookeeper。因此，在使用 Kafka 相关命令行工具时，需要按照新版本的参数规范来操作。

1. 运行MyStreamDemo类（在 IDE 中直接运行或在命令行中使用mvn exec:java -Dexec.mainClass="com.example.MyStreamDemo"命令运行，注意根据实际项目包名修改类路径）。

2. 程序启动后，打开新的命令行窗口，进入 Kafka 安装目录，使用以下命令向 “in” topic 发送数据：
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic in
在生产者窗口输入一些数据，然后在另一个命令行窗口中，使用以下命令消费 “out” topic 的数据，检查是否接收到从 “in” topic 传输过来的数据：
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic out --from-beginning



总结
如果不启动你提供的这个 Java 项目，是不能进行数据从 “in” 主题到 “out” 主题的传输的，原因如下：
1. 项目功能的主动性
● 该 Java 项目是专门设计用于从 Kafka 的 “in” 主题读取数据，并将其传输到 “out” 主题的。它通过连接到 Kafka 集群（配置为localhost:9092），利用 Kafka Streams API 构建了一个流处理拓扑来实现这个功能。如果项目不启动，就没有任何机制来主动触发数据的读取和传输操作。
2. 与 Kafka 交互的实现
● 项目中的代码通过KafkaStreams类和相关配置来建立与 Kafka 的连接，并定义了从特定主题读取和写入数据的逻辑。只有当项目运行时，这些代码才会被执行，从而与 Kafka 进行交互，按照定义的规则处理数据。没有项目的运行，Kafka 不会自动知晓要进行这样的数据传输操作，因为它只是一个消息中间件，依赖于外部的应用程序来产生和消费数据。
3. 数据处理流程的驱动
● 整个数据传输过程是由项目中的代码驱动的。从创建StreamsBuilder来构建流处理拓扑，到配置各种参数，再到启动KafkaStreams实例，每一步都是为了实现从 “in” 主题获取数据并传输到 “out” 主题的功能。如果不启动项目，这个数据处理流程就不会被启动，数据将不会在主题之间流动。
所以，要实现数据在 “in” 和 “out” 主题之间的传输，必须启动这个 Java 项目，让它作为数据传输的驱动程序与 Kafka 进行交互。