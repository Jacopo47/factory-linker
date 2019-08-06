package model

import redis.clients.jedis.Jedis

package object dao {
  def EVENT_EMITTER_GROUP = "event-emitter"

  def UPDATER_GROUP = "updater"

  def FACTORY_MAIN_STREAM_KEY = "factory-stream"



  def closeConnection(client: Jedis): Unit = client.quit()
}

