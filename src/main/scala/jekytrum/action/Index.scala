package jekytrum.action

import xitrum.annotation.{ CacheActionMinute, GET, Last }

import jekytrum.model.Entry

@CacheActionMinute(5)
@GET("/")
class RootIndex extends DefaultLayout {

  def execute() {
    respondEntry(Entry.getByKey(("index")))
  }

  def respondEntry(entry: Entry) = {
    at("entry") = entry
    respondInlineView(entry.body)
  }

  def removeMarkdownExt(key: String): String = {
    if (key.endsWith(".md") || key.endsWith(".MD"))
      key.dropRight(3)
    else if (key.endsWith(".markdown") || key.endsWith(".MARKDOWN"))
      key.dropRight(9)
    else
      key
  }
}

@CacheActionMinute(5)
@GET("/:title")
class EntryIndex extends RootIndex {
  override def execute() {
    respondEntry(Entry.getByKey(removeMarkdownExt(param("title"))))
  }
}

@CacheActionMinute(5)
@GET("/:parent/:*")
class SubEntryIndex extends RootIndex {
  override def execute() {
    val keys = toknize(List(param("parent")), paramo("*"))
    val key = keys.mkString("/")
    respondEntry(Entry.getByKey(removeMarkdownExt(key)))
  }

  private def toknize(parent: List[String], children: Option[String]): List[String] = {
    children match {
      case Some(t) =>
        val current = t dropWhile { _ == '/' }
        val next = current.split("/")(0)
        if (current == next)
          parent ::: List(next)
        else
          toknize(parent ::: List(next), Some(t.substring(next.length + 1)))
      case None => parent
    }
  }
}