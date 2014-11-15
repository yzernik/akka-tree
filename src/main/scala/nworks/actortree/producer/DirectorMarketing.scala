package nworks.actortree.producer

import akka.actor._

object DirectorMarketing {
  def props: Props = Props(new DirectorMarketing())

  val name = "director-marketing"
}

class DirectorMarketing extends Actor with ActorLogging {

  import DirectorMarketing._

  def receive = {
    case message => log.debug(s"Actor $name received message: $message")
  }
}
