package nworks.actortree.kafka

import akka.actor.{Terminated, Props, Actor, ActorRef}

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
//  private val map = new mutable.HashMap[String, String]()
  def receive = {
    case Recipient(ref) =>
//      context.watch(ref)
      val name = "recipient-" + counter
      ref ! ClientName(name)
      context.actorOf(SimpleConsumerWorker.props(Recipient(ref), name), name)
      counter += 1
/*
      map.put(ref.path.address.toString, name)
    case Stop(name) => // we got Stop message
      map.find(_._2 == name). // we try to find recipient by name (value for map) in map
        map(p => map.remove(p._1).get). // and remove it from map if found
        map(n => context.child(n).foreach(_ ! Stop(n))) // and after that we try to use name to stop our sender
    case Terminated(ref) =>
      context.unwatch(ref)
      map.
        remove(ref.path.address.toString). // on client termination we try to delete it from our map
        map(n => context.child(n).foreach(_ ! Stop(n))) // and if deleted - stop sender
*/
    case Stop(name) =>
      context.child(name).foreach(_ ! Stop(name))
  }
}


