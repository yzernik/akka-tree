package nworks.actortree.visualizer.actors

import akka.actor.{Terminated, ActorRef, Actor}
import play.api.libs.json._
import nworks.actortree.visualizer.journal.Kafka

case class NewActorCreated(ref: ActorRef)


class ActorMonitor extends Actor {

  implicit val writes = new Writes[NewActorCreated] {
    override def writes(o: NewActorCreated): JsValue = JsObject(Seq(
      "path" -> JsString(o.ref.path.name),
      "action" -> JsString("add")
    ))
  }

  def receive = {
    case na: NewActorCreated =>
      context.watch(na.ref)
      val json = Json.stringify(Json.toJson(na))
      Kafka.producer.sendMessage(json)
      println(">>>> new actor is created with name " + na.ref.path.name)


    case Terminated(ref) =>
      val json = Json.stringify(JsObject(Seq(
        "path" -> JsString(ref.path.name),
        "action" -> JsString("remove")
      )))
      Kafka.producer.sendMessage(json)
      println(">>>> actor is removed with name " + ref.path.name)

  }

}
