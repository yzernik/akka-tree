package nworks.actortree.producer

import akka.actor._
import util.OrgChanger._
import scala.concurrent.duration._

object FrontendLead {
  def props: Props = Props(new FrontendLead)

  val name = "frontend-lead"

  case object Quit
}

class FrontendLead extends Actor with ActorLogging {
  import FrontendLead._

  println("Frontend Lead created")

  quit(Quit, 10.seconds)

  def receive = {
    case message => log.debug(s"Actor $name received message: $message")
  }
}
