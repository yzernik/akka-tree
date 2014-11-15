package nworks.actortree.visualizer

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import nworks.actortree.kafka.KafkaDispatcher
import nworks.actortree.producer.Organization
import nworks.actortree.visualizer.actors.ActorMonitor
import nworks.actortree.visualizer.web.HttpService
import scala.concurrent.duration._

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("actor-tree")

  val monitor = system.actorOf(Props[ActorMonitor], name = "monitor")

  val kafkaDispatcher = system.actorOf(KafkaDispatcher.props, "kafka-dispatcher")

  // create and start our http service
  val service = system.actorOf(HttpService.props("127.0.0.1", 8080, 3.seconds), "http-service")


  //for testing
  Organization.start

//  val test = ActorSystem("test")
//  val parent = test.actorOf(Props(new Actor {
//    def receive = {
//      case _ =>
//        (0 to 3) foreach { x =>
//          context.actorOf(Props(new Actor {
//            def receive = Actor.emptyBehavior
//          }))
//        }
//    }
//  }), "parent")
//
//  parent ! "create"

}