import controller.Server
import io.vertx.scala.core.Vertx

object Main extends App {
  Vertx.vertx().deployVerticle(Server())
}
