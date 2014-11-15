package nworks.actortree.producer

import akka.actor._
import scala.concurrent.duration._
import util.OrgChanger._

object CEO {
  def props: Props = Props(new CEO())

  val name = "ceo"
}

class CEO extends Actor with ActorLogging {
  import context.dispatcher
  import CEO._

  println("CEO created")

//  hire(DirectorEngineering.props, DirectorEngineering.name, 1.second)
  hire(AdvisoryBoard.props, AdvisoryBoard.name, 3.second)
  hire(DirectorSales.props, DirectorSales.name, 4.second)
  hire(DirectorMarketing.props, DirectorMarketing.name, 5.second)

  def receive = {
    case message => log.debug(s"Actor $name received message: $message")
  }
}
