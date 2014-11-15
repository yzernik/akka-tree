package nworks.actortree.visualizer.web

import play.api.libs.json.Json

object Flow {

  sealed trait Event

  case class ActorAdded(actorPath: String, event: ActorAddedEvent) extends Event
  case class ActorAddedEvent(`type`: String)
  object ActorAdded {
    implicit val formatEvent = Json.format[ActorAddedEvent]
    implicit val format = Json.format[ActorAdded]
  }
}
