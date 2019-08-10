package updater

import model.dao._
import model.logger.Log
import java.io.IOException

import com.corundumstudio.socketio.{Configuration, SocketIOServer}
import model.api.Error
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class Updater(server: SocketIOServer) {

  implicit val formats: DefaultFormats.type = DefaultFormats

  ClientRedis.initialRedisEnvironmentConfiguration()


  private def onStreamEntry(entry: FactoryData): Unit = {
    val temperature = entry.rotaryTemperature
    val velocity = entry.rotaryVelocity

    server.getBroadcastOperations.sendEvent("hardware-data-update", write(HardwareDataMessageUpdate(velocity, temperature)))
  }


  def start(): Future[Unit] = {
    Log.debug("Updater is running...")
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

case class HardwareDataMessageUpdate(velocity: Double, temperature: Double)

object Updater {
  def apply(server: SocketIOServer): Updater = new Updater(server)
}