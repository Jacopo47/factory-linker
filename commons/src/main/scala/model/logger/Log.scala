package model.logger

import io.vertx.core.logging.{Logger, LoggerFactory}


object Log {
  private val logger: Logger = LoggerFactory.getLogger(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME)

  def debug(msg: String): Unit = logger.debug(msg, "")

  def info(msg: String): Unit = logger.info(msg, "")

  def error(msg: String): Unit = logger.error(msg, "")
}
