package nworks.actortree.producer

import akka.actor._

object AdvisoryBoard {
  def props: Props = Props(new AdvisoryBoard())

  val name = "advisory-board"
}

class AdvisoryBoard extends Actor with ActorLogging {

  import AdvisoryBoard._

  def receive = {
    case message => log.debug(s"Actor $name received message: $message")
  }
}
