package com.knoldus.kafkawiter

import java.util.Properties

import com.knoldus.util.ConfigConstants
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object KafkaWriter {
  private val props = new Properties()
  props.put("bootstrap.servers", ConfigConstants.kafkaBootStrapServer)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  private val producer = new KafkaProducer[String, String](props)

  def writeToKafka(key: String, json: String): Unit = {
    val record: ProducerRecord[String, String] = new ProducerRecord[String, String](ConfigConstants.detectorTopic, key, json)
    producer.send(record).get()
  }

  def closeProducer = {
    producer.close()
  }
}
