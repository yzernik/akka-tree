package nworks.actortree.kafka

import akka.actor.{Props, Actor, ActorRef}

case class Recipient(ref: ActorRef)

case class KafkaMessage(message: String)

case class Stop(name: String)

case class ClientName(name: String)

object KafkaDispatcher {
  def props = Props(new KafkaDispatcher())
}

/**
 * User: Evgeny Zhoga
 * Date: 15.11.14
 */
class KafkaDispatcher extends Actor {
  private var counter = 0
  def receive = {
    case Recipient(ref) =>
      val name = "recipient-" + counter
      ref ! ClientName(name)
      context.actorOf(SimpleConsumerWorker.props(Recipient(ref), name), name)
      counter += 1
    case Stop(name) =>
      context.child(name).foreach(_ ! Stop(name))
  }
}


