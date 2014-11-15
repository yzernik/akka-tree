package nworks.actortree.visualizer.web

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status}
import akka.http.Http
import akka.http.server.{Route, ScalaRoutingDSL}
import akka.io.IO
import akka.pattern.{ask, pipe}
import akka.stream.FlowMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import akka.util.Timeout
import play.api.libs.json.Json

import scala.concurrent.duration.DurationInt

object HttpService {

  private case object Shutdown

  def props(interface: String, port: Int, bindTimeout: Timeout): Props =
    Props(new HttpService(interface, port, bindTimeout))

  def flowEventToSseMessage(event: Flow.Event): Sse.Message =
    event match {
      case actorAdded: Flow.ActorAdded =>
        val data = Json.stringify(Json.toJson(actorAdded))
        Sse.Message(data)
    }
}

class HttpService(interface: String, port: Int, bindTimeout: Timeout)
    extends Actor
    with ActorLogging
    with ScalaRoutingDSL {

  import context.dispatcher
  import HttpService._

  private implicit val materializer = FlowMaterializer()

  IO(Http)(context.system)
    .ask(Http.Bind(interface, port))(bindTimeout)
    .mapTo[Http.ServerBinding]
    .pipeTo(self)

  override def receive: Receive = {
    case serverBinding: Http.ServerBinding =>
      log.info(s"Listening on $interface:$port")
      log.info(s"To shutdown, send GET request to http://$interface:$port/shutdown")
      handleConnections(serverBinding).withRoute(route)

    case Status.Failure(cause) =>
      log.error(cause, s"Could not bind to $interface:$port!")
      throw cause

    case Shutdown =>
      context.system.shutdown()
  }

  private def route: Route =
    assets ~ shutdown ~ messages

  private def assets: Route =
    // format: OFF
    path("") {
      getFromResource("web/index.html")
    } ~
    getFromResourceDirectory("web") // format: ON

  private def shutdown: Route =
    path("shutdown") {
      get {
        complete {
          context.system.scheduler.scheduleOnce(500 millis, self, Shutdown)
          log.info("Shutting down now ...")
          "Shutting down now ..."
        }
      }
    }

  private def messages: Route =
    path("messages") {
      get {
        complete {
          val source = Source(ActorPublisher[Flow.Event](createFlowEventPublisher()))
          Sse.response(source, flowEventToSseMessage)
        }
      }
    }

  protected def createFlowEventPublisher(): ActorRef =
    context.actorOf(FlowEventPublisher.props)
}
