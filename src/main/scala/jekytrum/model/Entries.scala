package jekytrum.model

import scala.io.Source
import java.io.File
import scala.collection.mutable.{ Map => MMap }
import org.fusesource.scalamd.Markdown

import xitrum.Log

case class Entry(title: String, body: String, createdAt: Long, updatedAt: Long, tags: List[String])

object Entry {

  private val encoding = "utf-8"
  private val srcDir = "src/main/markdown" // @TODO move config
  private val lookup = MMap.empty[String, Entry]
  private val entry404 = new Entry("404", "404 Not Found", 0L, 0L, List.empty)

  def load() = {
    getMarkdownSources(srcDir).foreach { file =>
      val source = Source.fromFile(file.toString, encoding)
      val name = file.getName
      val title = trimExt(name)
      val entry = new Entry(title, Markdown(source.getLines.mkString("\n")), 0L, 0L, List.empty)

      // Cache converted entry by key
      // src/markdown/parent/1.md              => "parent/1"
      // src/markdown/parent/child/2.markdown  => "parent/child/2"
      // src/markdown/3.md                     => "3"
      //
      // @TODO move lookuptable to lrucache or hazalcast or etc...

      lookup(trimExt(file.toString.drop(srcDir.length + 1))) = entry
    }
    logEntries
  }

  def getByKey(key: String): Entry = {
    lookup.get(key) match {
      case Some(entry) => entry
      case None        => tryIndex(key)
    }
  }

  // index fallback
  private def tryIndex(key: String): Entry = {
    val indexKey =
      if (key.endsWith("/"))
        key + "index"
      else
        key + "/index"
    lookup.get(indexKey) match {
      case Some(entry) => entry
      case None        => entry404
    }
  }

  private def logEntries() {
    Log.info(s"Found entries:\n - " + lookup.toSeq.sortBy(_._1).map(_._1).mkString("\n - "))
  }

  private def trimExt(path: String): String = {
    path.substring(0, path.lastIndexOf('.'))
  }

  private def getMarkdownSources(srcDir: String): Seq[File] = {
    ls(new File(srcDir)).filter(f =>
      !f.isDirectory && (
        f.getPath.endsWith(".md") ||
        f.getPath.endsWith(".markdown") ||
        f.getPath.endsWith(".MD") ||
        f.getPath.endsWith(".MARKDOWN")))
  }

  private def ls(file: File): Seq[File] = {
    val files = file.listFiles
    files ++ files.filter(_.isDirectory).flatMap(ls)
  }

}
