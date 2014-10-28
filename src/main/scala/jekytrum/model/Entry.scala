package jekytrum.model

import java.io.File
import scala.collection.mutable.{ Map => MMap }
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.{ Failure, Success }

import xitrum.Log

import jekytrum.Config

case class Entry(title: String, body: String, createdAt: Long, updatedAt: Long, tags: List[String])

object Entry {

  // @TODO move lookup table to cache engine for clustering usage
  private val lookup = MMap.empty[String, Entry]
  private val entry404 = new Entry("404", "404 Not Found", 0L, 0L, List.empty)
  private val entry500 = new Entry("500", "500 System Error", 0L, 0L, List.empty)

  def load() = {
    Log.debug("Convert source markdown files with " + Config.jekytrum.converter.getClass)
    getMarkdownSources(Config.jekytrum.srcDir).foreach { file =>
      val key = trimExt(file.toString.drop(Config.jekytrum.srcDir.length + 1))
      val name = file.getName
      val title = trimExt(name)
      Config.jekytrum.converter.convert(file).onComplete {
        case Failure(e) =>
          Log.warn(s"Failed to convert:${key}", e)
          lookup(key) = entry500
        case Success(None) =>
          Log.warn(s"Failed to convert:${key}")
          lookup(key) = entry500
        case Success(Some(htmlString)) =>
          val entry = new Entry(title, htmlString, 0L, 0L, List.empty)
          lookup(key) = entry
      }
      logEntries(key)
    }
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

  private def logEntries(key: String) {
    Log.info(s"Entry found: - ${key}")
  }

  private def trimExt(path: String): String = {
    path.substring(0, path.lastIndexOf('.'))
  }

  private def getMarkdownSources(srcDir: String): Seq[File] = {
    val targetDir = if (srcDir.startsWith("/")) new File(srcDir.drop(1)) else new File(srcDir)
    ls(targetDir).filter(f =>
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
