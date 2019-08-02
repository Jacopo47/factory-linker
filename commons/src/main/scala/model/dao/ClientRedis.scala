package model.dao

import java.util.AbstractMap.SimpleImmutableEntry

import redis.clients.jedis.{Jedis, StreamEntry, StreamEntryID}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

/**
  * Create a space for executing Redis commands.
  * Give to the body a connection to the db and after all operations close the connection.
  *
  * @param body
  * Function
  */
class ClientRedis[T](body: Jedis => T) {

  def createSpace(): T = {
    val client = RedisConnection.getConnection

    try {
      body(client)
    } finally {
      client.close()
    }

  }
}

object ClientRedis {
  def apply[T](body: Jedis => T): T = {
    new ClientRedis[T](body).createSpace()
  }

  def readStreamAsUpdater(): Unit = {
    Future {
      while (true) {
        println("UPDATER ascolta...")
        readStreamAsGroup(FACTORY_MAIN_STREAM_KEY, UPDATER_GROUP) match {
          case Some(data) =>
            println("UPDATER ha letto ->")
            data._2.foreach(entry => entry.getFields.forEach((k, v) => println(s"$k -> $v")))
          case None =>
        }
      }
    }

  }

  def readStreamAsGroup(stream: String, group: String, lastStreamID: StreamEntryID = StreamEntryID.UNRECEIVED_ENTRY): Option[(String, Seq[StreamEntry])] = {
    ClientRedis(db => {
      val streamQuery = new SimpleImmutableEntry[String, StreamEntryID](stream, lastStreamID)
      val result = db
        .xreadGroup(group, "stupid-client", 1, 5000L, true, streamQuery)

      if (Option(result).isEmpty) return None

      result.asScala
        .find(e => e.getKey.equals(stream))
        .map(e => (e.getKey, e.getValue.asScala)) match {
        case Some(value) => if (value._2.isEmpty) return None else return Some(value)
        case None => return None
      }
    })
  }

  def initialRedisEnvironmentConfiguration(): Unit = {
    ClientRedis {
      client =>
        try {
          client.xgroupCreate(FACTORY_MAIN_STREAM_KEY, EVENT_EMITTER_GROUP, StreamEntryID.LAST_ENTRY, true)
        } catch {
          case _: Throwable => println(s"$EVENT_EMITTER_GROUP group already in stream")
        }
        try {
          client.xgroupCreate(FACTORY_MAIN_STREAM_KEY, UPDATER_GROUP, StreamEntryID.LAST_ENTRY, true)
        } catch {
          case _: Throwable => println(s"$UPDATER_GROUP group already in stream")
        }
    }
  }
}
