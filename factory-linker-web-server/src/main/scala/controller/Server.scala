package controller

import io.vertx.core.http.HttpMethod
import io.vertx.scala.ext.web.RoutingContext
import model.api
import model.api.{Dispatcher, Error, ExecutionStateData, ExecutionTime, MachineHardwareData, MachinesExecutionTime, MachinesHardwareData, MachinesPrograms, Program, ProgramData, ResponseArray, RouterResponse}
import model.dao.{ClientRedis, FACTORY_MAIN_STREAM_KEY, FactoryData}
import model.logger.Log
import model.utilities.UNAVAILABLE
import org.joda.time.{DateTime, Minutes}

import scala.collection.JavaConverters._

class Server(routes: Map[(String, HttpMethod), (RoutingContext, RouterResponse) => Unit]) extends Dispatcher(routes) {

  override def start(): Unit = {
    super.start()
    Log.info("Server is running...")
  }
}

object Server {
  def apply(): Server = {
    new Server(Map(
      ("/hardwareData", HttpMethod.GET) -> getHardwareData,
      ("/executionData", HttpMethod.GET) -> getExecutionTimeForState,
      ("/programData", HttpMethod.GET) -> getProgramData)
    )
  }

  private val getHardwareData: (RoutingContext, RouterResponse) => Unit = (_, res) => {
    try {
      res.sendResponse(
        MachinesHardwareData(
          ClientRedis {
            client =>
              client.xrevrange(FACTORY_MAIN_STREAM_KEY, null, null, 100)
          }.asScala
            .map(FactoryData(_))
            .groupBy(e => e.machineName)
            .map(e => (e._1, e._2.map(_.getHardwareData)))
            .map(e => MachineHardwareData(e._1, e._2.map(_.time), e._2.map(_.rotaryVelocity), e._2.map(_.temperature))).toSeq))
    } catch {
      case ex: Exception => res.sendResponse(Error(Some(ex.getMessage)))
    }
  }

  private val getExecutionTimeForState: (RoutingContext, RouterResponse) => Unit = (_, res) => {
    try {
      val streamEntries = ClientRedis {
        client =>
          client.xrange(FACTORY_MAIN_STREAM_KEY, null, null, Int.MaxValue)
      }.asScala

      var lastTime: DateTime = FactoryData(streamEntries.head).datetimeSource

      val machines = streamEntries.map(FactoryData(_)).map(e => {
        val datetime = e.datetimeSource

        val difference = Minutes.minutesBetween(lastTime, datetime)
        lastTime = datetime

        (e.machineName, e.execution, difference.getMinutes)
      })
        .groupBy(_._1)

      val result: Seq[ExecutionTime] = machines
        .mapValues(e => e.groupBy(_._2).mapValues(_.map(_._3).sum))
        .map(e => {
          val app = e._2.map(d => ExecutionStateData(d._1.asString, d._2))
          ExecutionTime(e._1, app.map(_.state).toSeq, app.map(_.valueAsMinutes).toSeq)
        }).toSeq

      res.sendResponse(MachinesExecutionTime(result))
    } catch {
      case ex: Exception => res.sendResponse(Error(Some(ex.getMessage)))
    }
  }

  private val getProgramData: (RoutingContext, RouterResponse) => Unit = (_, res) => {
    try {
      val streamEntries = ClientRedis {
        client =>
          client.xrange(FACTORY_MAIN_STREAM_KEY, null, null, Int.MaxValue)
      }.asScala


      val machines = streamEntries.map(FactoryData(_))
        .filter(!_.program.equalsIgnoreCase(UNAVAILABLE))
        .groupBy(_.machineName)

      val result: Seq[Program] = machines
        .mapValues(e => {
          e.groupBy(_.program).mapValues(_.map(_.partCount).sortWith(_ > _)).mapValues(e => {
            e.head - e.last
          })
        })
        .map(e => {
          val app = e._2.map(d => ProgramData(d._1, d._2))
          Program(e._1, app.toSeq)
        }).toSeq

      res.sendResponse(MachinesPrograms(result))
    } catch {
      case ex: Exception => res.sendResponse(Error(Some(ex.getMessage)))
    }
  }
}

