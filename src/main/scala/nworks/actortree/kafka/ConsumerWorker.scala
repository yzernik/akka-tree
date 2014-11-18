package nworks.actortree.kafka

import java.util.Properties

import akka.actor.{Actor, Props}
import kafka.consumer.{Consumer, ConsumerConfig}
import nworks.actortree.visualizer.journal.Kafka

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * User: Evgeny Zhoga
 * Date: 18.11.14
 */
class ConsumerWorker(recipient: Recipient) extends Actor {
  var running = true
  var counter = 0
  val config = new ConsumerConfig(
    new Properties() {
      putAll(Map(
        "client.id" -> recipient.clientId,
        "consumer.id" -> recipient.clientId,
        "group.id" -> s"akka-tree-${recipient.clientId}-group",
        "zookeeper.connect" -> Kafka.zkConnectString,
        "auto.commit.interval.ms" -> "1000",
        "offsets.storage" -> "kafka"
      ))
    }
  )
  var consumer = Consumer.create(config)
  val streams = consumer.createMessageStreams(Map(
    Kafka.topic -> new java.lang.Integer(1)
  ))(Kafka.topic)
  println(s"streams size: ${streams.size}")
  val topicIterator = streams.head.iterator()
  self ! Consume()

  override def receive: Receive = {
    case Stop(clientId) if config.clientId == clientId =>
      running = false
      shutdown()
    case Consume() =>
      import context.dispatcher
      if (running && topicIterator.hasNext()) {
        Future {
          try {
            Some(new String(topicIterator.next().message()))
          } catch {
            case NonFatal(e) =>
              println(s"[EXCEPTION] => ${e.getMessage}")
              None
          }
        }.foreach(self ! Consumed(_))
      } else self ! Consumed(None)
    case Consumed(message) =>
      counter += 1
      println(s">>>>> Kafka consumer sended $counter messages to ${recipient.clientId}    --- Current message: ${message.getOrElse("None")}")
      message.filter(_ => running).foreach(m => {
        recipient.ref ! KafkaMessage(m)
        self ! Consume()
      })
  }
  def shutdown(): Unit = {
    if (consumer != null) consumer.shutdown()
  }
}

object ConsumerWorker {
  def props(recipient: Recipient) = Props(new ConsumerWorker(recipient))
}
