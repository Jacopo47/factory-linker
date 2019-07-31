package model.api

/**
  * This class is used for define the message accepted to the RouterResponse.
  *
  */

sealed trait JsonResponse

case class Message(message: String) extends JsonResponse
case class Error(cause: Option[String] = None) extends JsonResponse
