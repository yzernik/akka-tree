package nworks.actortree.kafka

import akka.actor.{Actor, ActorRef, Props, Terminated}

import scala.collection.mutable

case class Recipient(ref: ActorRef, clientId: String)

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
  val map = new mutable.HashMap[String, String]

  def receive = {
    case Recipient(ref, clientId) =>
      println(">>>>>>>>> new recipient " + clientId)
//      ref ! ClientName(clientId)
      context.actorOf(SimpleConsumerWorker.props(Recipient(ref, clientId)), clientId)
      map.put(ref.path.address.toString, clientId)
      context.watch(ref)
    case Terminated(ref) =>
      context.unwatch(ref)
      map.
        remove(ref.path.address.toString). // on client termination we try to delete it from our map
        map(n => context.child(n).foreach(_ ! Stop(n))) // and if deleted - stop sender
  }
}


