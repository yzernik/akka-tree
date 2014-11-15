package nworks.actortree.visualizer.bridge

import spray.http.ContentType
import spray.http.ContentTypes
import spray.http.MediaTypes
import spray.http.MediaTypes._
import spray.httpx.TwirlSupport
import spray.httpx.marshalling
import marshalling.Marshaller
import spray.httpx.marshalling.Marshaller
import play.twirl.api.{ Xml, Txt, Html }
import spray.http._
import MediaTypes._

/**
 * A trait providing Marshallers for the Twirl template result types.
 */
trait PlayTwirlSupport {

  implicit val twirlHtmlMarshaller =
    twirlMarshaller[Html](`text/html`, `application/xhtml+xml`)

  implicit val twirlTxtMarshaller =
    twirlMarshaller[Txt](ContentTypes.`text/plain`)

  implicit val twirlXmlMarshaller =
    twirlMarshaller[Xml](`text/xml`)

  protected def twirlMarshaller[T](marshalTo: ContentType*): Marshaller[T] =
    Marshaller.delegate[T, String](marshalTo: _*)(_.toString)
}

object PlayTwirlSupport extends PlayTwirlSupport
