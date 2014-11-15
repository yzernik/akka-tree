package nworks.actortree.kafka

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import akka.actor.{Props, Actor}
import kafka.api._
import kafka.common.{TopicAndPartition, ErrorMapping}
import kafka.consumer.SimpleConsumer
import nworks.actortree.visualizer.journal.Kafka

/**
 * User: Evgeny Zhoga
 * Date: 15.11.14
 */
object SimpleConsumerWorker {
  val correlationID = 10

  def props(recipient: Recipient) = Props(new SimpleConsumerWorker(recipient))

  def getLastOffset(consumer: SimpleConsumer,
                    topic: String,
                    partition: Int,
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
class SimpleConsumerWorker(recipient: Recipient) extends Actor {
  import SimpleConsumerWorker._

  private val clientName = recipient.clientId
  private val topic = Kafka.topic
  private val partition = 0
  private val (seeds, port) = Option(Kafka.seed.split(":")).map(a => (List(a(0)), a(1).toInt)).head
  private var mReplicaBrokers: List[String] = _
  private var flag = new AtomicBoolean(true)
  val debugFlag = new AtomicBoolean(true)
  val debugCounter = new AtomicInteger(0)
  new Thread() {
    override def run(): Unit = {
      try {
        runConsumer(topic, partition, seeds, port)
      } catch {
        case e: Exception =>
          Console.println("Oops:" + e)
          e.printStackTrace();
      }
    }
  }.start()

  override def receive: Receive = {
    case Stop(requestedName) if clientName == requestedName => flag.set(false)
  }

  private def runConsumer(aTopic: String,
                          aPartition: Int,
                          aSeedBrokers: List[String],
                          aPort: Int) {
    // find the meta data about the topic and partition we are interested in
    //
    val metadata = findLeader(aSeedBrokers, aPort, aTopic, aPartition)
    if (metadata == null) {
      Console.println("Can't find metadata for Topic and Partition. Exiting")
      return
    }
    if (metadata.leader.isEmpty) {
      System.out.println("Can't find Leader for Topic and Partition. Exiting")
      return
    }
    var leadBroker = metadata.leader.get.host
    //    clientName = "Client_" + aTopic + "_" + aPartition

    var consumer: SimpleConsumer = null
    try {
      consumer = new SimpleConsumer(leadBroker, aPort, 100000, 64 * 1024, clientName)
      var readOffset = getLastOffset(consumer, aTopic, aPartition, kafka.api.OffsetRequest.EarliestTime, clientName)

      var numErrors = 0
      while (flag.get()) {
        def getResult(iterations: Int = 5): FetchResponse = {
          if (consumer == null) {
            consumer = new SimpleConsumer(leadBroker, aPort, 100000, 64 * 1024, clientName)
          }

          if (iterations == 0) {
            throw new RuntimeException()
          }
          val req = new FetchRequestBuilder().
            clientId(clientName).
            addFetch(aTopic, aPartition, readOffset, 100000). // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
            build()
          val fetchResponse = consumer.fetch(req)

          if (!fetchResponse.hasError) {
            fetchResponse
          } else {
            // Something went wrong!
            val code = fetchResponse.errorCode(aTopic, aPartition)
            Console.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code)

            if (code == ErrorMapping.OffsetOutOfRangeCode) {
              // We asked for an invalid offset. For simple case ask for the last element to reset
              readOffset = getLastOffset(consumer, aTopic, aPartition, kafka.api.OffsetRequest.LatestTime, clientName)
            } else {
              consumer.close()
              consumer = null
              leadBroker = findNewLeader(leadBroker, aTopic, aPartition, aPort)
            }
            getResult(iterations - 1)
          }
        }
        val fetchResponse = getResult()

        var numRead = 0l
        for (messageAndOffset <- fetchResponse.messageSet(aTopic, aPartition)) {
          val currentOffset = messageAndOffset.offset
          if (currentOffset < readOffset) {
            Console.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset)
          } else {
            readOffset = messageAndOffset.nextOffset
            val payload = messageAndOffset.message.payload

            val bytes = new Array[Byte](payload.limit())
            payload.get(bytes)

            //            Console.println(String.valueOf(messageAndOffset.offset) + ": " + new String(bytes, "UTF-8"))
            val message: String = new String(bytes)
            //            Console.println(String.valueOf(messageAndOffset.offset) + ": " + message)

//            if (debugFlag.getAndSet(false))

            println(s">>>>> Kafka consumer sended ${debugCounter.incrementAndGet()} messages to $clientName    --- Current message: $message")
            recipient.ref ! KafkaMessage(message)

            numRead += 1
          }
        }

        if (numRead == 0) {
          Thread.sleep(1000)
        }
      }
    } finally {
      if (consumer != null) consumer.close()
    }
  }


  private def findNewLeader(aOldLeader: String, aTopic: String, aPartition: Int, aPort: Int, iterations: Int = 3, goToSleep: Boolean = false): String = {
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

    val metadata = findLeader(mReplicaBrokers, aPort, aTopic, aPartition)
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

        def find(i: Iterator[TopicMetadata]):PartitionMetadata  =
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
    if (returnMetaData != null) {
      mReplicaBrokers = returnMetaData.replicas.map(_.host).toList
    }
    returnMetaData
  }
}
