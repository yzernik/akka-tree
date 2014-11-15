package nworks.actortree.visualizer.web

import play.api.libs.json.Json

object Flow {

  sealed trait Event

  case class MessageAdded(flowName: String, message: Message) extends Event
  object MessageAdded { implicit val format = Json.format[MessageAdded] }
}
