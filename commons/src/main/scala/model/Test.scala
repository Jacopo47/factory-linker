package model


import java.util.AbstractMap.SimpleImmutableEntry

import redis.clients.jedis.{StreamEntry, StreamEntryID}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class Test() {

  def EVENT_EMITTER_GROUP = "event-emitter"

  def UPDATER_GROUP = "updater"

  def STREAM_KEY = "factory-stream"


  def test() = {
    ClientRedis(client => {
      println(client.get("foo"))
    })
  }

  def rangeStream() = {
    ClientRedis(db => {
      db.xrange(STREAM_KEY, null, null, -1).forEach(e => println(e.getID))
    })
  }

  def readStreamForever(): Unit = {
    var lastStreamID = new StreamEntryID(0, 0)
    while (true) {
      val result = Test().readStream(lastStreamID)

      result.headOption match {
        case Some(value) =>
          value._2.foreach(e => println(e.getFields))
          lastStreamID = value._2.last.getID
        case None =>
      }
    }
  }

  def readStream(lastStreamId: StreamEntryID = new StreamEntryID(0, 0)): Seq[(String, Seq[StreamEntry])] = {
    ClientRedis(db => {
      val streamQuery = new SimpleImmutableEntry[String, StreamEntryID](STREAM_KEY, lastStreamId)

      val result = db.xread(1, 5000L, streamQuery)

      result.asScala.map(e => (e.getKey, e.getValue.asScala))
    })
  }


  def readStreamAsEventEmitter(): Unit = {
      var lastStreamID = new StreamEntryID(0, 0)
      while (true) {
        readStreamAsGroup(STREAM_KEY, EVENT_EMITTER_GROUP, lastStreamID) match {
          case Some(data) =>
            println("EVENT EMITTER ha letto ->")
            data._2.foreach(entry => entry.getFields.forEach((k, v) => println(s"$k -> $v")))
            lastStreamID = data._2.last.getID
          case None =>
        }
      }
  }

  def readStreamAsUpdater(): Unit = {
      var lastStreamID = new StreamEntryID(0, 0)
      while (true) {
        readStreamAsGroup(STREAM_KEY, UPDATER_GROUP, lastStreamID) match {
          case Some(data) =>
            println("UPDATER ha letto ->")
            data._2.foreach(entry => entry.getFields.forEach((k, v) => println(s"$k -> $v")))
            lastStreamID = data._2.last.getID
          case None =>
        }
      }
  }


  def readStreamAsGroup(stream: String, group: String, lastStreamID: StreamEntryID = StreamEntryID.UNRECEIVED_ENTRY): Option[(String, Seq[StreamEntry])] = {
    ClientRedis(db => {
      val streamQuery = new SimpleImmutableEntry[String, StreamEntryID](stream, lastStreamID)
      val result = db
        .xreadGroup(group, "stupid-client", 1, 5000L, true, streamQuery)
        .asScala
        .find(e => e.getKey.equals(stream))
        .map(e => (e.getKey, e.getValue.asScala))

      result match {
        case Some(value) => if (value._2.isEmpty) return None else return Some(value)
        case None => return None
      }
    })
  }


}
