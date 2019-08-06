package controller

import model.Config
import model.api.GetRequest
import model.dao.ClientRedis
import model.logger.Log
import model.utilities.NULL_DATA
import org.dom4j.{Document, DocumentHelper, Namespace}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._

class Reader(config: Config) {


  val system = akka.actor.ActorSystem("system")

  system.scheduler.schedule(0 second, 60 second, () => read())

  def read(): Unit = {
    config.mtConnectEndpoints.foreach(endpoint => {
      GetRequest(endpoint.endpoint, endpoint.relativePath, {
        case Some(data) =>
          val document: Document = DocumentHelper.parseText(data)

          val namespace = new Namespace(endpoint.namespace.prefix, endpoint.namespace.uri)
          document.getRootElement.add(namespace)

          onDataFound(endpoint.machineName, endpoint.nodes.map(node => {
            node.name -> (Option(document.selectSingleNode(node.xpath)) match {
              case Some(value) => value.getText
              case None => NULL_DATA
            })
          }).toMap)
        case None => Log.debug("No data found!")
      }, {
        case error: Throwable => Log.warn(s"Unexpected error, details: ${error.getMessage}")
        case _ => Log.warn("Error on retrieve data from MT CONNECT")
      })
    })

  }

  private def onDataFound(machineName: String, fields: Map[String, String]): Unit = {
    val values = Map("machine" -> machineName) ++ fields

    ClientRedis defaultAddToMainStream values
  }
}

object Reader {
  def apply(config: Config): Reader = new Reader(config)
}


