package nworks.actortree.visualizer.web

import org.joda.time.DateTime
import play.api.libs.json._
import nworks.actortree.visualizer.util.JsonFormats._

case class Message(text: String, dateTime: DateTime)

object Message {
  implicit val format = Json.format[Message]
}
