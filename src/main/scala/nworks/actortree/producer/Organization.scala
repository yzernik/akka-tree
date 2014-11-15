package nworks.actortree.producer

import akka.actor.{Actor, Props, ActorSystem}
import akka.routing.RoundRobinPool
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Organization {

  def start: Unit = {
    val system = ActorSystem("organization")
    system.scheduler.scheduleOnce(5.seconds)(system.actorOf(CEO.props, CEO.name))


    system.actorOf(RoundRobinPool(5).props(Props(new Actor {
      override def receive: Receive = {
        case _ =>
      }
    })), name = "router")
  }
}
