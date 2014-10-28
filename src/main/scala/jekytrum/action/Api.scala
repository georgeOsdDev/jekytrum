package jekytrum.action

import xitrum.annotation.{ GET, First }
import xitrum.ActorAction

import jekytrum.model.Entry

/**
 * @TODO Use elastic search
 */
@First
@GET("/jekytrum/api/search")
class SearchAPI extends ActorAction() {
  def execute() {
    val keyword = param("keyword")
    log.debug("do search with:" + keyword)
    respondJson(Map("list" -> List()))
  }
}

@First
@GET("/jekytrum/api/tags")
class tagAPI extends ActorAction {
  def execute() {
    respondJson(Map("tags" -> List()))
  }
}

@First
@GET("/jekytrum/api/list")
class listAPI extends ActorAction {
  def execute() {
    respondJson(Map("list" -> List()))
  }
}