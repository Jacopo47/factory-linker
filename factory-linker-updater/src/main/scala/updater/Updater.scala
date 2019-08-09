package updater

import javax.websocket.server.ServerEndpoint
import javax.websocket.{OnOpen, Session}
import model.dao._
import model.logger.Log
import org.json.JSONObject

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

@ServerEndpoint("hardware")
class Updater {
  ClientRedis.initialRedisEnvironmentConfiguration()

  var session: Option[Session] = None

  @OnOpen
  private def onOpen(session: Session) {
    this.session = Some(session)
  }

  private def onStreamEntry(entry: FactoryData): Unit = {
    val obj = new JSONObject()
    obj.put("temperature", entry.partCount)
    obj.put("velocity", entry.partCount)

    if (session.isDefined) {
      session.get.getBasicRemote.sendText(obj.toString)
      Log.debug("Updater has send data!")
    } else {
      Log.warn("Cannot send data from updater because websocket session not defined")
    }
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

object Updater {
  def apply(): Updater = new Updater()
}