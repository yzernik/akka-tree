package nworks.actortree.visualizer.web

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor.{ ActorPublisher, ActorPublisherMessage }
import org.joda.time.DateTime

import scala.concurrent.duration.DurationInt

object FlowEventPublisher {
  def props: Props = Props(new FlowEventPublisher)
}

class FlowEventPublisher extends ActorPublisher[Flow.Event] with ActorLogging {

  import context.dispatcher

  context.system.scheduler.schedule(2 seconds, 2 seconds) {
    self ! Flow.MessageAdded("akka", Message("Akka and AngularJS are a great combination!", DateTime.now))
  }

  override def receive: Receive = {
    case event: Flow.Event if isActive && totalDemand > 0 =>
      println("event: " + event)
      sourceEvent(event)
    case event: Flow.Event                                => log.warning("Can't source event [{}]", event)
    case ActorPublisherMessage.Cancel                     => context.stop(self)
  }

  private def sourceEvent(event: Flow.Event): Unit = {
    onNext(event)
    println("Sourced event: " + event)
  }
}
