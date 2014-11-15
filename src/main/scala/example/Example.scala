package example

import akka.actor.{Props, ActorSystem, Actor}

object Example extends App {

  class ParentActor extends Actor {

    def receive = {
      case _ =>
        val ref = context.actorOf(Props[ChildActor], "child1")
        ref ! "child"
    }

  }

  class ChildActor extends Actor {

    def receive = {
      case x =>
        println("Hey got the message " + x)
    }
  }


  val system = ActorSystem("fff")
  val ref = system.actorOf(Props[ParentActor], "p1")
  ref ! "parent"


  Console.readLine()

  system.shutdown()

}
