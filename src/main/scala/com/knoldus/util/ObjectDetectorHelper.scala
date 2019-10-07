package com.knoldus.util

import com.knoldus.kafkawiter.KafkaWriter
import com.knoldus.models.Model.ImageSetMessage
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ObjectDetectorHelper extends App {
  implicit val formats: DefaultFormats.type = DefaultFormats
  try {

    println("===================================")
    val res = for {
      validPath <- ConnectionProvider.getValidPath
      values <- Future.sequence(validPath.map {
        case (path: String, path2: String, path3: String) =>
          Future{
            ConnectionProvider.getCounterForPath(path, path2, path3)
              .map(counter => {
                val imageSet = ImageSetMessage(path2, path3, ConfigConstants.unitId, s"$path2-00000", if (counter <= 599) 599 else 5999)
                KafkaWriter.writeToKafka(imageSet.unitId,
                  write(imageSet))
                0
              })
          }
      })
    } yield Future.sequence(values).map { _ =>
      println("process complete")
      0
    }
    println("===================================")

    Await.ready(res.map(_ => {
      ConnectionProvider.fs.close()
      0
    }), Duration.Inf)
  } catch {
    case exception: Exception =>
      println("Exception occurred")
      ConnectionProvider.fs.close()
  }
}
