package model.api


import io.vertx.core.buffer.Buffer
import io.vertx.scala.core.{MultiMap, Vertx}
import io.vertx.scala.ext.web.client.{HttpRequest, HttpResponse, WebClient}
import org.json4s.jackson.Serialization.read

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * HTTP Client request.
  */
sealed trait ClientRequest {

  /**
    * Primary url.
    *
    * @return
    * The primary url of the request.
    */
  def url: String

  /**
    * Server port.
    *
    * @return
    * The server port, if it's required.
    */
  def port: Option[Int]

  /**
    * Relative uri to the resource.
    *
    * @return
    * the relative uri.
    */
  def relativePath: String

  /**
    * Executed method in case of success of the request.
    *
    * @return
    * The handler in case of success.
    */
  def onSuccess: Option[String] => Unit

  /**
    * Executed method in case of fail of the request.
    *
    * @return
    * The handler in case of fail.
    */
  def onFail: Throwable => Unit

  /**
    * Request's params
    *
    * @return
    * the request's params.
    */
  def requestParams: Option[Map[String, Any]]


  protected def successRequest(result: HttpResponse[Buffer],
                               onSuccess: Option[String] => Unit,
                               onFail: Throwable => Unit): Unit = {

    if (result.statusCode() == ResponseStatus.OK_CODE) {
      onSuccess(result.bodyAsString())
    } else {
      try {
        val errorMsg = read[Error](result.bodyAsString().get)
        onFail(new Throwable(errorMsg.cause.getOrElse("No error description")))
      } catch {
        case _: Exception => onFail(new Throwable(result.bodyAsString().getOrElse("")))
      }
    }
  }
}

case class GetRequest(url: String,
                      relativePath: String,
                      onSuccess: Option[String] => Unit,
                      onFail: Throwable => Unit,
                      requestParams: Option[Map[String, Any]] = None,
                      port: Option[Int] = None,
                      handleHeader: HttpRequest[_] => Unit = _ => {}) extends ClientRequest {

  val client: WebClient = WebClient.create(Vertx.vertx())

  val complexUri = new StringBuffer(relativePath)
  var first: Boolean = true
  requestParams match {
    case Some(params) =>
      params foreach { case (k, v) =>
        if (first) {
          complexUri.append("?" + k + "=" + v.toString)
          first = false
        } else {
          complexUri.append("&" + k + "=" + v.toString)
        }
      }
    case None =>
  }


  var future: HttpRequest[Buffer] = _

  port match {
    case Some(p) => future = client.get(p, url, complexUri.toString)
    case None => future = client.get(url, complexUri.toString)
  }

  handleHeader(future)

  future.sendFuture().onComplete {
    case Success(result) =>
      successRequest(result, onSuccess, onFail)
    case Failure(cause) =>
      onFail(cause)
  }

}


case class PostRequest(url: String,
                       relativePath: String,
                       onSuccess: Option[String] => Unit,
                       onFail: Throwable => Unit,
                       requestParams: Option[Map[String, Any]] = None,
                       port: Option[Int] = None) extends ClientRequest {

  implicit def toMultiMap(params: Map[String, Any]): MultiMap = {
    val form = MultiMap.caseInsensitiveMultiMap()
    params map { case (k, v) => form.set(k, v.toString) }

    form
  }

  val client: WebClient = WebClient.create(Vertx.vertx())

  val complexUri = new StringBuffer(relativePath)
  var first: Boolean = true


  var future: HttpRequest[Buffer] = _

  port match {
    case Some(p) => future = client.post(p, url, complexUri.toString)
    case None => future = client.post(url, complexUri.toString)
  }


  requestParams match {
    case Some(params) =>
      future.sendFormFuture(params)
        .onComplete {
          case Success(result) =>
            successRequest(result, onSuccess, onFail)
          case Failure(cause) =>
            onFail(cause)
        }
    case None =>
      future.sendFuture().onComplete {
        case Success(result) =>
          successRequest(result, onSuccess, onFail)
        case Failure(cause) =>
          onFail(cause)
      }
  }


}