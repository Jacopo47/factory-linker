
import updater.Updater

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  Await.result(Updater().start(), Duration.Inf)
}