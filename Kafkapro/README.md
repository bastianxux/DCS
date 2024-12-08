# Kafka
一、项目概述
本项目旨在复现基于 Kafka Streaming 的多个数据处理案例，包括基础数据传输、计算数字总和、WordCount 以及不同类型的窗口操作（跳跃时间窗口、滚动时间窗口、会话窗口、滑动时间窗口），以帮助初学者理解和掌握 Kafka Streaming 的基本功能和使用方法。
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

第二种方法(运行失败)
在Mac上安装Kafka可以通过以下步骤完成：
安装错误：https://github.com/Homebrew/brew
安装Homebrew： 如果你还没有安装Homebrew，可以通过以下命令安装：
Sh
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
安装Java： Kafka依赖于Java，因此需要确保Java已经安装。你可以通过以下命令安装Java：
Sh
brew install openjdk
安装Kafka： 使用Homebrew安装Kafka：
Sh
brew install kafka
启动Zookeeper： Kafka依赖于Zookeeper来管理集群状态。启动Zookeeper：
Sh
zkServer start
启动Kafka服务器： 在另一个终端窗口中，启动Kafka服务器：
Sh
kafka-server-start /usr/local/etc/kafka/server.properties
验证安装： 你可以通过创建一个主题并发送和接收消息来验证Kafka是否安装成功。
创建一个主题：
Sh
kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
列出所有主题：
Sh
kafka-topics --list --bootstrap-server localhost:9092
发送消息：
Sh
kafka-console-producer --broker-list localhost:9092 --topic test
接收消息：
Sh
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning
通过以上步骤，你应该能够在Mac上成功安装并运行Kafka。
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