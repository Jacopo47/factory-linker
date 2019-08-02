import controller.Reader
import model.{Config, MTConnectConfig}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods

import scala.io.Source

object Main extends App {
  val CONFIG_FILE_PATH = "config.json"

  implicit val formats: DefaultFormats.type = DefaultFormats

  val file = Source.fromResource(CONFIG_FILE_PATH)

  val fileContents: String = {
    try  {
      file.mkString
    } finally {
      file.close()
    }
  }

  val config: Config = JsonMethods.parse(fileContents).extract[Config]

  Reader(config).read()
}
