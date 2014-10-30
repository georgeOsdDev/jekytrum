package jekytrum.action

import org.json4s.Xml.toXml

import xitrum.annotation.{ GET, First }
import xitrum.{ Action, ActorAction }
import xitrum.util.SeriDeseri

import jekytrum.model.Entry

trait JekytrumAPI {
  this: ActorAction =>

  def respondResult(result: AnyRef) = {
    respondJson(Map("result" -> result))
  }
}

/**
 * @TODO Use elastic search
 */
@First
@GET("/jekytrum/api/search")
class SearchAPI extends ActorAction with JekytrumAPI {
  def execute() {
    val keyword = param("keyword")
    log.debug("do search with:" + keyword)
    respondResult(List())
  }
}

@First
@GET("/jekytrum/api/entries")
class ListAPI extends ActorAction with JekytrumAPI {
  def execute() {
    val category = paramo("category")
    val entries = Entry.findByCategory(category)
    respondResult(entries)
  }
}

@First
@GET("/jekytrum/api/categories")
class CategoryAPI extends ActorAction with JekytrumAPI {
  def execute() {
    val categories = Entry.allCategories
    respondResult(categories)
  }
}
