package nworks.actortree.visualizer.actors

import akka.actor.Actor
import monitor.NewActorCreated

class ActorMonitor extends Actor {

  def receive = {
    case na: NewActorCreated =>
      println(">>>> new actor is created with name " + na.ref.path.name)
  }

}
