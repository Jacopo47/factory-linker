package model.logger

import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.{Level, LogManager}


object Log {
  private val logger = LogManager.getLogger(Log.getClass)
  Configurator.setLevel(logger.getName, Level.DEBUG)



  def debug(msg: String): Unit = logger.debug(msg)

  def info(msg: String): Unit = logger.info(msg)

  def warn(msg: String): Unit = logger.warn(msg)

  def error(msg: String): Unit = logger.error(msg)
}
