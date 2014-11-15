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

  context.system.scheduler.scheduleOnce(1.second)(context.actorOf(DirectorEngineering.props, DirectorEngineering.name))
  context.system.scheduler.scheduleOnce(5.second)(context.actorOf(AdvisoryBoard.props, AdvisoryBoard.name))
  context.system.scheduler.scheduleOnce(10.seconds)(context.actorOf(DirectorSales.props, AdvisoryBoard.name))
  context.system.scheduler.scheduleOnce(10.seconds)(context.actorOf(DirectorMarketing.props, DirectorMarketing.name))

  def receive = {
    case message => log.debug(s"Actor $name received message: $message")
  }
}
