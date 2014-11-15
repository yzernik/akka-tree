package nworks.actortree.kafka

import akka.actor.Actor.Receive
import akka.actor.{Props, Actor, ActorSystem}

/**
 * User: Evgeny Zhoga <ezhoga@yandex-team.ru>
 * Date: 15.11.14
 */
object KafkaDispatcherTest {
  def main(args: Array[String]): Unit = {
    val s = ActorSystem("consumers")

    val dispatcher = s.actorOf(Props[KafkaDispatcher])

    val testReceiver1 = s.actorOf(Props[TestReceiver], "receiver-1")

    dispatcher ! Receipient(testReceiver1)

    val testReceiver2 = s.actorOf(Props[TestReceiver], "receiver-2")

    dispatcher ! Receipient(testReceiver2)

  }
}

class TestReceiver extends Actor {
  var clientName: String = _
  override def receive: Receive = {
    case KafkaMessage(message) =>
      println(s"---------NAME: $clientName ------- received $message")
    case ClientName(name) =>
      clientName = name
  }
}
