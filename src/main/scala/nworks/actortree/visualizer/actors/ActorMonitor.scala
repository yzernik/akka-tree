package nworks.actortree.visualizer.actors

import akka.actor.{Terminated, ActorRef, Actor}
import play.api.libs.json._
import nworks.actortree.visualizer.journal.Kafka

case class NewActorCreated(ref: ActorRef)

case class ActorMailboxSizeChanged(ref: ActorRef, mailboxSize: Int)


class ActorMonitor extends Actor {

  implicit val newActorCreatedWrites = new Writes[NewActorCreated] {
    override def writes(o: NewActorCreated): JsValue =
      Json.obj("actorpath" -> o.ref.path.address.toString, "event" -> Json.obj("type" -> "started"))
  }

  implicit val mailboxSizeChangedWrites = new Writes[ActorMailboxSizeChanged] {
    override def writes(o: ActorMailboxSizeChanged): JsValue =
      Json.obj("actorpath" -> o.ref.path.address.toString,
        "event" -> Json.obj("type" -> "mailboxSizeChanged"),
        "size" -> o.mailboxSize.toString
      )
  }

  def receive = {
    case na: NewActorCreated =>
      context.watch(na.ref)
      val json = Json.stringify(Json.toJson(na))
      Kafka.producer.sendMessage(json)

    case m: ActorMailboxSizeChanged =>
      val json = Json.stringify(Json.toJson(m))
      Kafka.producer.sendMessage(json)

    case Terminated(ref) =>
      val json = Json.stringify(Json.obj("actorpath" -> ref.path.address.toString, "event" -> Json.obj("type" -> "terminated")))
      Kafka.producer.sendMessage(json)
  }

}
