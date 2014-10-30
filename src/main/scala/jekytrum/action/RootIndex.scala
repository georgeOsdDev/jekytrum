package jekytrum.action

import xitrum.annotation.{ CacheActionMinute, GET }
import xitrum.Action

import jekytrum.view.ViewHelper

@GET("/")
class RootIndex extends Action {
  def execute() {
    at("entries") = ViewHelper.listEntries(paramo("category"))
    respondViewNoLayout()
  }
}