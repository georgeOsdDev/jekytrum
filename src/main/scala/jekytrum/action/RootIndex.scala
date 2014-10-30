package jekytrum.action

import xitrum.annotation.{ CacheActionMinute, GET }
import xitrum.Action

import jekytrum.view.ViewHelper

@GET("/")
class RootIndex extends Action with ViewHelper {
  def execute() {
    at("entries") = listEntries(paramo("category"))
    respondViewNoLayout()
  }
}
