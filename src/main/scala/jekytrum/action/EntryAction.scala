package jekytrum.action

import java.io.File
import xitrum.annotation.{ CacheActionMinute, GET, First, Last }
import xitrum.Action
import jekytrum.Config
import jekytrum.handler.ErrorEntry
import jekytrum.model.{ Entry, Entry404, Entry500 }
import jekytrum.view.ViewHelper

trait EntryLayout extends Action with ViewHelper {
  override def layout = renderViewNoLayout[EntryLayout]()

  def respondEntry(key: String) = {
    val entry = Entry.getByKey(key)
    if (entry.isInstanceOf[Entry404])
      ErrorEntry.set404Entry(handlerEnv, key)
    if (entry.isInstanceOf[Entry500])
      ErrorEntry.set500Entry(handlerEnv, key)
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
class EntryAction extends Action with EntryLayout {
  override def execute() {
    respondEntry(removeMarkdownExt(param("title")))
  }
}

@CacheActionMinute(5)
@Last
@GET("/:parent/:*")
class SubEntryAction extends Action with EntryLayout with Tokenizer {
  override def execute() {
    val keys = tokenize(List(param("parent")), paramo("*"))
    val key = keys.mkString("/")
    respondEntry(removeMarkdownExt(key))
  }
}

@CacheActionMinute(5)
@First
@GET("""/:image<*.(jpg|JPG|jpeg|JPEG|gif|GIF|png|PNG|bmp|BMP)$>""")
class ImageShow extends Action {
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
class SubImageShow extends ImageShow with Tokenizer {
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