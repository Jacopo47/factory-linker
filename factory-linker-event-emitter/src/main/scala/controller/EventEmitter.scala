package controller

import model.dao.ClientRedis.readStreamAsGroup
import model.dao.{ClientRedis, EVENT_EMITTER_GROUP, FACTORY_MAIN_STREAM_KEY}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class EventEmitter() {


  ClientRedis.initialRedisEnvironmentConfiguration()


  private def onStreamEntry(entries: Map[String, String]): Unit = {
    entries foreach { case (k, v) => println(s"$k -> $v") }
  }


  def start(): Future[Unit] = {
    Future {
      while (true) {
        readStreamAsGroup(FACTORY_MAIN_STREAM_KEY, EVENT_EMITTER_GROUP) match {
          case Some(data) =>
            data._2
              .map(_.getFields.asScala.toMap)
              .foreach(onStreamEntry)
          case None =>
            println("Nessun dato da leggere ...")
        }
      }
    }
  }

}