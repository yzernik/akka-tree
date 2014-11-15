package nworks.actortree.producer

import akka.actor._
import scala.concurrent.duration._
import util.OrgChanger._

object DirectorEngineering {
  def props: Props = Props(new DirectorEngineering())

  val name = "director-engineering"
}

class DirectorEngineering extends Actor with ActorLogging {
  import DirectorEngineering._

  println("Director Engineering created")

  hire(ChiefArchitect.props, ChiefArchitect.name, 2.seconds)
  hire(BackendLead.props, BackendLead.name, 3.seconds)
  hire(FrontendLead.props, FrontendLead.name, 4.seconds)

  def receive = {
    case ChiefArchitect.Quit =>
      fire(ChiefArchitect.props, ChiefArchitect.name)
      hire(ChiefArchitect.props, ChiefArchitect.name, 3.seconds)

    case FrontendLead.Quit =>
      fire(FrontendLead.props, FrontendLead.name)
      hire(FrontendLead.props, FrontendLead.name, 1.seconds)

    case BackendLead.Quit =>
      fire(BackendLead.props, BackendLead.name)
      hire(BackendLead.props, BackendLead.name, 2.seconds)
  }
}
