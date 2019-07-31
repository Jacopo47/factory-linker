import redis.clients.jedis.Jedis

package object model {
  def closeConnection(client: Jedis): Unit = client.quit()
}
