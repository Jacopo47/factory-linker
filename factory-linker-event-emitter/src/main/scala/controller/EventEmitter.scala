package controller

import model.dao.ClientRedis.readStreamAsGroup
import model.dao.{ClientRedis, EVENT_EMITTER_GROUP, FACTORY_MAIN_STREAM_KEY, FactoryData}
import model.logger.Log

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class EventEmitter() {


  ClientRedis.initialRedisEnvironmentConfiguration()


  private def onStreamEntry(entry: FactoryData): Unit = {
    println("\n\n ================================ \n")
    entry.toString
  }


  def start(): Future[Unit] = {
    Future {
      while (true) {
        try {
          readStreamAsGroup(FACTORY_MAIN_STREAM_KEY, EVENT_EMITTER_GROUP) match {
            case Some(data) =>
              data._2.map(e => FactoryData(e)).foreach(onStreamEntry)
            case None =>
              println("Nessun dato da leggere ...")
          }
        } catch {
          case e: Exception => Log.debug(e.getMessage)
        }

      }
    }
  }

}