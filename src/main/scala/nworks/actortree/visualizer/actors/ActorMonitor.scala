package nworks.actortree.visualizer.actors

import akka.actor.{Terminated, ActorRef, Actor}
import play.api.libs.json._
import nworks.actortree.visualizer.journal.Kafka

case class NewActorCreated(ref: ActorRef)

class ActorMonitor extends Actor {

  implicit val writes = new Writes[NewActorCreated] {
    override def writes(o: NewActorCreated): JsValue =
      Json.obj("actorpath" -> o.ref.path.toString, "event" -> Json.obj("type" -> "started"))
  }

  def receive = {
    case na: NewActorCreated =>
      context.watch(na.ref)
      val json = Json.stringify(Json.toJson(na))
      Kafka.producer.sendMessage(json)

    case Terminated(ref) =>
      val json = Json.stringify(Json.obj("actorpath" -> ref.path.address.toString, "event" -> Json.obj("type" -> "terminated")))
      Kafka.producer.sendMessage(json)
  }

}
