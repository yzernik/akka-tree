package nworks.actortree.producer

import akka.actor._

object DirectorSales {
  def props: Props = Props(new DirectorSales())

  val name = "director-sales"
}

class DirectorSales extends Actor with ActorLogging {

  import DirectorSales._

  def receive = {
    case message => log.debug(s"Actor $name received message: $message")
  }
}
