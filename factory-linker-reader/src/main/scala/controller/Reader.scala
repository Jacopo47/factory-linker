package controller

import controller.Reader.{MTCONNECT_AGENT_URL, PATH_BASE, PATH_CURRENT}
import javax.xml.parsers.DocumentBuilder
import model.{Config, MTConnectConfig}
import model.api.GetRequest
import model.logger.Log
import org.dom4j.{Document, DocumentHelper}

class Reader(config: Config) {

  def read(): Unit = {
    config.mtConnectEndpoints.foreach(endpoint => {
      GetRequest(endpoint.endpoint, endpoint.relativePath, {
        case Some(data) =>
          Log.info(s"MACHINE: ${endpoint.machineName}")
          val document: Document = DocumentHelper.parseText(data)

          endpoint.nodes.foreach(node => {
            Log.info(s"Node: ${node.name} -> ${document.valueOf(node.xpath)}")
          })
        case None => Log.debug("No data found!")
      }, {
        case error: Throwable => Log.error(s"Unexpected error, details: ${error.getMessage}")
        case _ => Log.error("Error on retrieve data from MT CONNECT")
      })
    })

  }
}

object Reader {
  def apply(config: Config): Reader = new Reader(config)

  val MTCONNECT_AGENT_URL = "smstestbed.nist.gov"
  val PATH_BASE = "/vds"
    val PATH_CURRENT = "/current"
}


