package jekytrum.action

import java.io.File
import xitrum.annotation.{ CacheActionMinute, GET, First, Last }
import xitrum.Action
import jekytrum.Config
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
@Last
@GET("/:parent/:*")
class SubEntryIndex extends RootIndex with Tokenizer {
  override def execute() {
    val keys = tokenize(List(param("parent")), paramo("*"))
    val key = keys.mkString("/")
    respondEntry(Entry.getByKey(removeMarkdownExt(key)))
  }
}

@CacheActionMinute(5)
@First
@GET("""/:image<*.(jpg|JPG|jpeg|JPEG|gif|GIF|png|PNG|bmp|BMP)$>""")
class ImageIndex extends Action {
  def execute() {
    val imagePath = param("image")
    respondImage(imagePath)
  }

  def respondImage(imagePath: String) {
    val file = new File(Config.jekytrum.srcDir + "/" + imagePath)
    if (file.isFile && file.getAbsolutePath.startsWith(xitrum.root + "/" + Config.jekytrum.srcDir))
      respondFile(file.getAbsolutePath)
    else
      respond404Page
  }
}

@CacheActionMinute(5)
@First
@GET("""/:parent/:image<*.(jpg|JPG|jpeg|JPEG|gif|GIF|png|PNG|bmp|BMP)$>""")
class SubImageIndex extends ImageIndex with Tokenizer {
  override def execute() {
    val keys = tokenize(List(param("parent")), paramo("image"))
    val imagePath = keys.mkString("/")
    respondImage(imagePath)
  }
}

// remove unexpected "/"
trait Tokenizer {
  this: Action =>

  def tokenize(parent: List[String], children: Option[String]): List[String] = {
    children match {
      case Some(t) =>
        val current = t dropWhile { _ == '/' }
        val next = current.split("/")(0)
        if (current == next)
          parent ::: List(next)
        else
          tokenize(parent ::: List(next), Some(t.substring(next.length + 1)))
      case None => parent
    }
  }
}