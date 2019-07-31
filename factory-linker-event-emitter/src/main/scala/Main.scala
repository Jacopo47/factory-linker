import controller.Dispatcher
import io.vertx.scala.core.Vertx

object Main {
  def main(args: Array[String]): Unit = {
    Vertx.vertx().deployVerticle(Dispatcher())
  }
}
