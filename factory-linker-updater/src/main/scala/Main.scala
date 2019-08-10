import com.corundumstudio.socketio.{Configuration, SocketIOServer}
import updater.Updater

object Main extends App {

  val config = new Configuration()
  config.setHostname("localhost")
  config.setPort(9092)

  val server = new SocketIOServer(config)

  server.start()

  Updater(server).start()
  Thread.sleep(Integer.MAX_VALUE)

  server.stop()
}
