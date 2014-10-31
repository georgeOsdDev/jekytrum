package jekytrum.action

import xitrum.annotation.{ CacheActionMinute, GET }
import xitrum.Action

import jekytrum.Config
import jekytrum.view.ViewHelper

@GET("/")
class RootIndex extends Action with ViewHelper {
  def execute() {
    at("entries") = listEntries(paramo("category"), None)

    Config.jekytrum.theme match {
      case Some(t) =>
        respondViewNoLayout(t.indexLayout)
      case None => respondViewNoLayout()
    }
  }
}
