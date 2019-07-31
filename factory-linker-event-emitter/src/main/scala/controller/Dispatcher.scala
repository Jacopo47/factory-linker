package controller


import controller.Dispatcher.{PORT, TIMEOUT}
import io.vertx.core.http.HttpHeaders
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.http.HttpServerOptions
import io.vertx.scala.ext.web.{Router, RoutingContext}
import model.api.{Error, Message, RouterResponse}
import org.json4s.jackson.Serialization.write


object Dispatcher {
  val applicationJson: String = "application/json"
  val USER = "user:"
  var HOST: String = "localhost"
  val PORT: Int = 4700
  var PASSWORD: Option[String] = Some("")
  val RESULT = "result"
  val TIMEOUT = 1000

  private var discoveryUrl: String = "127.0.0.1"
  private var myIp: String = "127.0.0.1"

  def setDiscovery(value: String): Unit = discoveryUrl = value

  val DISCOVERY_PORT: Int = 2000

  def DISCOVERY_URL: String = discoveryUrl

  def setMyIp(value: String): Unit = myIp = value

  def MY_IP: String = myIp

  val VERTX = Vertx.vertx()
}


case class Dispatcher() extends ScalaVerticle {


  override def start(): Unit = {

    val router = Router.router(vertx)



    val options = HttpServerOptions()
    options.setCompressionSupported(true)
      .setIdleTimeout(TIMEOUT)


    var port = PORT

    if (System.getenv("PORT") != null) port = System.getenv("PORT").toInt

    router.route().handler(ctx => {
      val err = Error(Some(s"Error 404 not found"))

      ctx.response().setStatusCode(404)
      ctx.response().putHeader(HttpHeaders.CONTENT_TYPE.toString, "application/json; charset=utf-8")
      ctx.response().end(write(err))
    })

    vertx.createHttpServer(options)
      .requestHandler(router.accept _).listen(port)
  }

  /**
    * Welcome response.
    */
  private val hello: (RoutingContext, RouterResponse) => Unit = (_, res) => {
    res.sendResponse(Message("Hello to everyone"))
  }


  /**
    * Respond with a generic error message.
    *
    */
  private val responseError: (RoutingContext, RouterResponse) => Unit = (_, res) => {

    res.setGenericError(Some("Error"))
      .sendResponse(Error())
  }
}
