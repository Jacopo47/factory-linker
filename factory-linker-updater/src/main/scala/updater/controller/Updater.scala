package updater.controller

import io.socket.client.IO
import model.dao._
import model.logger.Log
import model.utilities._
import org.json.JSONObject

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class Updater() {


  ClientRedis.initialRedisEnvironmentConfiguration()

  private val socket = IO.socket("http://localhost")

  private def onStreamEntry(entry: FactoryData): Unit = {
    val obj = new JSONObject()
    obj.put("temperature", entry.partCount)
    obj.put("velocity", entry.partCount)

    socket.emit("hardware-data-update", obj)
  }


  def start(): Future[Unit] = {
    Future {
      while (true) {
        try {
          ClientRedis.readStreamAsGroup(FACTORY_MAIN_STREAM_KEY, EVENT_EMITTER_GROUP) match {
            case Some(data) =>
              data._2.map(e => FactoryData(e)).foreach(onStreamEntry)
            case None =>
              Log.debug("Nessun dato da leggere ...")
          }
        } catch {
          case e: Exception => Log.debug(e.getMessage)
        }

      }
    }
  }

}