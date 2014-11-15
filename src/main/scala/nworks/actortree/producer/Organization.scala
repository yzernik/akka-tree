package nworks.actortree.producer

import akka.actor.ActorSystem
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Organization {

  def start: Unit = {
    val system = ActorSystem("organization")
    system.scheduler.scheduleOnce(3.seconds)(system.actorOf(CEO.props, CEO.name))
  }
}
