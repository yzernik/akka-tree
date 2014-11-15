package akkatree

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

@JSExport
object AkkaTree {
    @JSExport
    def onMessage(event: js.Dictionary[String]): Unit = {

      println(event.get("actorpath"))
    }
}
