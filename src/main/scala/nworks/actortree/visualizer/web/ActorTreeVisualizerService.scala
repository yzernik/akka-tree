package nworks.actortree.visualizer.web

import akka.actor.{Props, Actor}
import play.api.libs.json.Json
import spray.can.Http
import spray.routing.HttpService
import spray.httpx.{TwirlSupport, PlayJsonSupport}
import spray.routing._
import spray.http._
import MediaTypes._
import HttpHeaders._
import CacheDirectives._


object EventSourceMediaType {
  val `text/event-stream` = MediaType.custom("text/event-stream")
  MediaTypes.register(`text/event-stream`)
}

class ActorTreeVisualizerService extends Actor with HttpService with PlayJsonSupport {

  import EventSourceMediaType._

  override def actorRefFactory = context

  override def receive = runRoute(routes)

  lazy val routes = {
    get {
      path("") {
        respondWithMediaType(`text/html`) {
          complete {
            ???
            //html.index("Actor Tree")
          }
        }
      } ~
      path("actortree" / "es") {
        respondWithMediaType(`text/event-stream`) {
          sse
        }
      }
    }
  }

  private def sse(ctx: RequestContext) {
    actorRefFactory.actorOf(Props(new SSEActor(ctx)), name = "sse")
  }

  override def postStop(): Unit = {
    super.postStop()
    println("Stopping this service " + self.path)
  }
}


object EventCollector {

  case object CreateNewEventSource

  case object CloseEventSource
}

class EventCollector extends Actor {

  override def receive = Actor.emptyBehavior
}

class SSEActor(ctx: RequestContext) extends Actor {

  val responseStart = HttpResponse(
    headers = `Cache-Control`(CacheDirectives.`no-cache`) :: `Connection`("Keep-Alive") :: Nil,
    entity = ":" + (" " * 2049) + "\n" // 2k padding for IE using Yaffle
  )

  override def preStart = {
    ctx.responder ! ChunkedResponseStart(responseStart)
    self ! 'process
  }

  override def receive = {
    case 'process =>
      1 to 10 foreach (x => ctx.responder ! MessageChunk(s"data: This is a SSE ${x}\n\n"))
    case x : Http.ConnectionClosed =>
      println("!!!!!! Connection is closed")
      context.stop(self)
  }
}
