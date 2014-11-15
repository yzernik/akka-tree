package nworks.actortree.visualizer

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import nworks.actortree.visualizer.actors.ActorMonitor
import nworks.actortree.visualizer.web.HttpService
import scala.concurrent.duration._

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("actor-tree")

  val monitor = system.actorOf(Props[ActorMonitor], name = "monitor")

  // create and start our http service
  val service = system.actorOf(HttpService.props("localhost", 8080, 3.seconds), "http-service")
}