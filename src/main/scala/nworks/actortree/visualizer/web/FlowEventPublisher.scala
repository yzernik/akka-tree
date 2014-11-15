package nworks.actortree.visualizer.web

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor.{ ActorPublisher, ActorPublisherMessage }
import nworks.actortree.kafka.KafkaMessage
import org.joda.time.DateTime
import scala.concurrent.duration.DurationInt

object FlowEventPublisher {
  def props: Props =
    Props(new FlowEventPublisher)
}

class FlowEventPublisher extends ActorPublisher[KafkaMessage] with ActorLogging {

  import context.dispatcher

  override def receive: Receive = {
    case event: KafkaMessage if isActive && totalDemand > 0 => sourceEvent(event)
    case ActorPublisherMessage.Cancel                     => context.stop(self)
  }

  private def sourceEvent(event: KafkaMessage): Unit = {
    onNext(event)
    log.debug("Sourced event [{}]", event)
  }
}
