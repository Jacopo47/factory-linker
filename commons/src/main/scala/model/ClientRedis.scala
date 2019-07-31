package model

import redis.clients.jedis.Jedis


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
}
