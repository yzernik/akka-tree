package nworks.actortree.producer

import akka.actor._
import scala.concurrent.duration._

object CEO {
  def props: Props = Props(new CEO())

  val name = "ceo"
}

class CEO extends Actor with ActorLogging {
  import context.dispatcher
  import CEO._

  println("CEO created")

  context.system.scheduler.scheduleOnce(1.second)(context.actorOf(DirectorEngineering.props))
  context.system.scheduler.scheduleOnce(20.second)(context.actorOf(AdvisoryBoard.props))
  context.system.scheduler.scheduleOnce(30.seconds)(context.actorOf(DirectorSales.props))
  context.system.scheduler.scheduleOnce(50.seconds)(context.actorOf(DirectorMarketing.props))

//  context.actorOf(DirectorEngineering.props)
//  context.actorOf(AdvisoryBoard.props)
//  context.actorOf(DirectorSales.props)
//  context.actorOf(DirectorMarketing.props)

  def receive = {
    case message => log.debug(s"Actor $name received message: $message")
  }
}
