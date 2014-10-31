package jekytrum.action

import org.json4s.Xml.toXml

import xitrum.annotation.{ GET, First }
import xitrum.{ Action, ActorAction }
import xitrum.util.SeriDeseri

import jekytrum.model.Entry
import jekytrum.model.elasticsearch.Node
import jekytrum.view.ViewHelper

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
class SearchAPI extends ActorAction with JekytrumAPI with ViewHelper {
  def execute() {
    val keyword = param("keyword")
    val resp = Node.searchEntry(keyword)
    val hits = resp.getHits.getHits.map { h =>
      Entry.getByKey(h.sourceAsMap.get("key").asInstanceOf[String])
    }
    paramo("type") match {
      case Some("json") =>
        respondResult(Map(
          "count" -> resp.getHits.getTotalHits,
          "hits" -> hits))
      case _ =>
        at("entries") = listEntries(None, Some(hits.toList))
        respondViewNoLayout[RootIndex]()
    }
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
