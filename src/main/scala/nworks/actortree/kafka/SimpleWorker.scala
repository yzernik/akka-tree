package nworks.actortree.kafka

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Terminated, Actor, Props}
import kafka.api._
import kafka.common.{ErrorMapping, TopicAndPartition}
import kafka.consumer.SimpleConsumer
import nworks.actortree.visualizer.journal.Kafka
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * User: Evgeny Zhoga
 * Date: 16.11.14
 */
class SimpleWorker(recipient: Recipient) extends Actor {
  import SimpleWorker._

  val clientName = recipient.clientId
  var seeds = initialSeeds
  var leader: String = _
  var consumer: SimpleConsumer = _
  var running = true
  val debugCounter = new AtomicInteger(0)

  var readOffset = try {
    val metadata = findLeader(seeds, port, topic, partition)

    if (metadata == null) {
      Console.println("Can't find metadata for Topic and Partition. Exiting")
    }

    if (metadata.leader.isEmpty) {
      System.out.println("Can't find Leader for Topic and Partition. Exiting")
    }

    seeds = metadata.replicas.map(_.host).toList
    leader = metadata.leader.get.host
    consumer = getConsumer(leader, clientName)

    getLastOffset(consumer, kafka.api.OffsetRequest.EarliestTime, clientName)
  } finally {
    if (consumer != null) consumer.close()
  }

  self ! Consume()

  def receive = {
    case Consume() =>
      consume()
    case Stop(requestedName) if clientName == requestedName =>
      running = false
  }

  def consume(): Unit = {
    if (running) try {
      val fetchResponse = getResult(readOffset)

      var numRead = 0l

      val read = (for (messageAndOffset <- fetchResponse.messageSet(topic, partition)) yield {
        val currentOffset = messageAndOffset.offset
        if (currentOffset < readOffset) {
          Console.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset)
          0l
        } else {
          readOffset = messageAndOffset.nextOffset
          val payload = messageAndOffset.message.payload

          val bytes = new Array[Byte](payload.limit())
          payload.get(bytes)

          val message: String = new String(bytes)

          println(s">>>>> Kafka consumer sended ${debugCounter.incrementAndGet()} messages to $clientName    --- Current message: $message")
          recipient.ref ! KafkaMessage(message)

          1l
        }
      }) exists(_ > 0)

      if (read) {
        self ! Consume()
      } else {
        consumeIn(100 milliseconds)
      }
    } finally {
      if (consumer != null) consumer.close()
    }
    else {
      if (consumer != null) consumer.close()
    }
  }
  def consumeIn(duration: FiniteDuration): Unit = {
    context.system.scheduler.scheduleOnce(duration, self, Consume())
  }
  def getResult(readOffset: Long, iterations: Int = 5): FetchResponse = {
    if (consumer == null) {
      consumer = getConsumer(leader, clientName)
    }

    if (iterations == 0) {
      throw new RuntimeException()
    }
    val req = new FetchRequestBuilder().
      clientId(clientName).
      addFetch(topic, partition, readOffset, 100000). // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
      build()
    val fetchResponse = consumer.fetch(req)

    if (!fetchResponse.hasError) {
      fetchResponse
    } else {
      // Something went wrong!
      val code = fetchResponse.errorCode(topic, partition)
      Console.println("Error fetching data from the Broker:" + leader + " Reason: " + code)

      val newReadOffset = if (code == ErrorMapping.OffsetOutOfRangeCode) {
        // We asked for an invalid offset. For simple case ask for the last element to reset
        getLastOffset(consumer, kafka.api.OffsetRequest.LatestTime, clientName)
      } else {
        consumer.close()
        consumer = null
        leader = findNewLeader(leader, topic, partition, port)
        readOffset
      }
      getResult(newReadOffset, iterations - 1)
    }
  }

  private def findNewLeader(
                             aOldLeader: String,
                             aTopic: String,
                             aPartition: Int,
                             aPort: Int,
                             iterations: Int = 3,
                             goToSleep: Boolean = false
                             ): String = {
    if (iterations == 0) {
      Console.println("Unable to find new leader after Broker failure. Exiting")
      throw new Exception("Unable to find new leader after Broker failure. Exiting")
    }
    if (goToSleep) {
      try {
        Thread.sleep(1000)
      } catch {
        case e: InterruptedException =>
      }
    }

    val metadata = findLeader(seeds, aPort, aTopic, aPartition)
    if (metadata == null) {
      findNewLeader(aOldLeader, aTopic, aPartition, aPort, iterations - 1, goToSleep = true)
    } else if (metadata.leader == null) {
      findNewLeader(aOldLeader, aTopic, aPartition, aPort, iterations - 1, goToSleep = true)
    } else if (aOldLeader.equalsIgnoreCase(metadata.leader.get.host) && iterations == 3) {
      // first time through if the leader hasn't changed give ZooKeeper a second to recover
      // second time, assume the broker did recover before failover, or it was a non-Broker issue
      //
      findNewLeader(aOldLeader, aTopic, aPartition, aPort, iterations - 1, goToSleep = true)
    } else {
      metadata.leader.get.host
    }
  }

  private def findLeader(aSeedBrokers: List[String], aPort: Int, aTopic: String, aPartition: Int): PartitionMetadata = {
    var returnMetaData: PartitionMetadata = null
    //    loop:
    for (seed <- aSeedBrokers) {
      var consumer: SimpleConsumer = null
      try {
        consumer = new SimpleConsumer(seed, aPort, 100000, 64 * 1024, "leaderLookup")
        val topics = Seq(aTopic)
        val req = new TopicMetadataRequest(topics, correlationID)
        val resp = consumer.send(req)

        def find(i: Iterator[TopicMetadata]): PartitionMetadata =
          if (!i.hasNext) null
          else i.next().partitionsMetadata.find(_.partitionId == aPartition).getOrElse(find(i))

        returnMetaData = find(resp.topicsMetadata.iterator)
      } catch {
        case e: Exception =>
          Console.println("Error communicating with Broker [" + seed + "] to find Leader for [" + aTopic
            + ", " + aPartition + "] Reason: " + e)
      } finally {
        if (consumer != null) consumer.close()
      }
    }

    returnMetaData
  }
}

object SimpleWorker {
  def props(recipient: Recipient) = Props(new SimpleWorker(recipient))

  private val correlationID = 10
  private val topic = Kafka.topic
  private val partition = 0
  private val (initialSeeds, port) = Option(Kafka.seed.split(":")).map(a => (List(a(0)), a(1).toInt)).head

  def getConsumer(leader: String, clientName: String) = new SimpleConsumer(leader, port, 100000, 64 * 1024, clientName)

  def getLastOffset(consumer: SimpleConsumer,
                    whichTime: Long,
                    clientName: String): Long = {

    val topicAndPartition = new TopicAndPartition(topic, partition)
    val requestInfo = Map(topicAndPartition -> new PartitionOffsetRequestInfo(whichTime, 1))

    val request = new kafka.api.OffsetRequest(
      requestInfo = requestInfo, correlationId = correlationID)
    val response = consumer.getOffsetsBefore(request)

    if (response.hasError) {
      Console.println("Error fetching data Offset Data the Broker. Reason: " + response.describe(details = true))
      0l
    } else {
      response.offsetsGroupedByTopic.
        get(topic).
        map(m => m.get(new TopicAndPartition(topic, partition)).map(por => por.offsets.headOption.getOrElse(0l)).getOrElse(0l)).getOrElse(0l)
    }
    // always start from zero for now
    //    0l
  }
}