package controller

import model.dao.ClientRedis.readStreamAsGroup
import model.dao.{ClientRedis, EVENT_EMITTER_GROUP, FACTORY_MAIN_STREAM_KEY, FactoryData}
import model.logger.Log
import model.utilities.UNAVAILABLE

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class EventEmitter() {


  ClientRedis.initialRedisEnvironmentConfiguration()


  private def onStreamEntry(entry: FactoryData): Unit = {
    if (entry.machineName.equalsIgnoreCase(UNAVAILABLE)) {
      Log.warn(s"Machine: ${entry.machineName} triggered alarm!! Check as soon as possible!!")
      // Aggiungere update tramite socket.io
    }
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