package nworks.actortree.visualizer.actors

import akka.actor.{Terminated, ActorRef, Actor}
import play.api.libs.json._
import nworks.actortree.visualizer.journal.Kafka

case class NewActorCreated(ref: ActorRef)


class ActorMonitor extends Actor {

  implicit val writes = new Writes[NewActorCreated] {
    override def writes(o: NewActorCreated): JsValue =
      Json.obj("actorpath" -> o.ref.path.name, "event" -> Json.obj("type" -> "created"))
  }

  def receive = {
    case na: NewActorCreated =>
      context.watch(na.ref)
      val json = Json.stringify(Json.toJson(na))
      Kafka.producer.sendMessage(json)
      println(">>>> new actor is created with name " + na.ref.path.name)


    case Terminated(ref) =>
      val json = Json.stringify(Json.obj("actorpath" -> ref.path.name, "event" -> Json.obj("type" -> "removed")))
      Kafka.producer.sendMessage(json)
      println(">>>> actor is removed with name " + ref.path.name)

  }

}
