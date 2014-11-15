package nworks.actortree.visualizer

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import nworks.actortree.visualizer.actors.ActorMonitor
import nworks.actortree.visualizer.web.ActorTreeVisualizerService
import spray.can.Http

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("actor-tree")

  val monitor = system.actorOf(Props[ActorMonitor], name = "monitor")

  // create and start our service actor
  val service = system.actorOf(Props[ActorTreeVisualizerService], "visualizer-service")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}