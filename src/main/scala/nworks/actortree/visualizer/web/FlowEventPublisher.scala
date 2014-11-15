package nworks.actortree.visualizer.web

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor.{ ActorPublisher, ActorPublisherMessage }
import nworks.actortree.visualizer.web.Flow.ActorAddedEvent
import org.joda.time.DateTime
import scala.concurrent.duration.DurationInt

object FlowEventPublisher {
  def props: Props =
    Props(new FlowEventPublisher)
}

class FlowEventPublisher extends ActorPublisher[Flow.Event] with ActorLogging {

  import context.dispatcher

  var counter = 1

  context.system.scheduler.schedule(2 seconds, 2 seconds) {
    self ! Flow.ActorAdded(s"akka://my-sys/user/service-a/worker$counter", ActorAddedEvent("created"))
    counter = counter + 1
  }

  override def receive: Receive = {
    case event: Flow.Event if isActive && totalDemand > 0 => sourceEvent(event)
    case event: Flow.Event                                => log.warning("Can't source event [{}]", event)
    case ActorPublisherMessage.Cancel                     => context.stop(self)
  }

  private def sourceEvent(event: Flow.Event): Unit = {
    onNext(event)
    log.debug("Sourced event [{}]", event)
  }
}
