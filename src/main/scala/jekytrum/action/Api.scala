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
@GET("/jekytrum/api/list")
class ListAPI extends ActorAction {
  def execute() {
    val category = paramo("category")
    Entry.findByCategory(category)
    respondJson(Map("entries" -> Entry.findByCategory(category)))
  }
}

@First
@GET("/jekytrum/api/categories")
class CategoryAPI extends ActorAction {
  def execute() {
    respondJson(Map("categories" -> Entry.allCategories))
  }
}