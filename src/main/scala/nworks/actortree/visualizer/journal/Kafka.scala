package nworks.actortree.visualizer.journal

import java.io.File
import java.util.Properties

import kafka.producer.{ProducerConfig, KeyedMessage, Producer}
import kafka.server.{KafkaConfig, KafkaServer}
import kafka.server.{KafkaServer, KafkaConfig}
import kafka.utils._
import org.apache.curator.test.TestingServer
import play.api.libs.json.JsValue
import scala.collection.JavaConversions._

import scala.collection.immutable.Stream

object Kafka {

  private val zookeeperServer = new TestingServer()

  val zkConnectString = "localhost:" + zookeeperServer.getPort()
  val topic = "akka-tree-messages"

  val seed = "localhost:9092"

  lazy val producer = new KafkaProducer
}

private[journal] class KafkaProducer {

  val kafkaServer = startKafkaServer()
  val producer = createProducer(kafkaServer, Kafka.zkConnectString)

  def stop() = {
    producer.close()
    kafkaServer.shutdown()
    kafkaServer.awaitShutdown()
  }

  def sendMessage(m: String) = {
    val data = new KeyedMessage[String, String](Kafka.topic, m);
    producer.send(data);
  }

  private def createProducer(kafkaServer: KafkaServer, zkConnectString: String): Producer[String, String] = {
    val props = new Properties()
    props.put("metadata.broker.list", Kafka.seed)
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props.put("producer.type", "async")
    props.put("batch.size", "1")
    val config = new ProducerConfig(props)

    new Producer[String, String](config)
  }

  private def startKafkaServer() = {
    val tmpDir: File = new File(new File("target"), "temp-" + SystemTime.nanoseconds.toString)
    tmpDir.mkdirs()
    val props = createProperties(tmpDir.getAbsolutePath(), 9092, 1)
    val kafkaConfig: KafkaConfig = new KafkaConfig(props)

    val kafkaServer = new KafkaServer(kafkaConfig)
    kafkaServer.startup()
    kafkaServer
  }

  private def createProperties(logDir: String, port: Int, brokerId: Int) = {
    val properties = new Properties()
    properties.put("port", port + "")
    properties.put("broker.id", brokerId + "")
    properties.put("log.dir", logDir)
    properties.put("zookeeper.connect", Kafka.zkConnectString)
    properties
  }

}
