package jekytrum.model

import java.io.File
import scala.collection.mutable.{ Map => MMap }
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.{ Failure, Success }
import xitrum.Log
import xitrum.util.FileMonitor
import jekytrum.Config

case class Entry(title: String, body: String, lastModified: Long, tags: List[String])

object Entry {

  // @TODO move lookup table to cache engine for clustering usage
  private val lookup = MMap.empty[String, Entry]
  private val entry404 = new Entry("404", "404 Not Found", 0L, List.empty)
  private val entry500 = new Entry("500", "500 System Error", 0L, List.empty)
  private val srcDir = if (Config.jekytrum.srcDir.startsWith("/"))
    new File(Config.jekytrum.srcDir.drop(1))
  else
    new File(Config.jekytrum.srcDir)

  def load() = {
    Log.debug("Convert source markdown files with " + Config.jekytrum.converter.getClass)
    getMarkdownSources(srcDir).foreach { file =>
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
          val entry = new Entry(title, htmlString, file.lastModified, List.empty)
          lookup(key) = entry
          watchDelete(file)
      }
      logEntries(key)
    }
    watch
  }

  private def watch() {
    Log.info(s"Start monitoring source directory: ${srcDir.toPath}")
    FileMonitor.monitorRecursive(FileMonitor.MODIFY, srcDir.toPath, { path =>
      val file = path.toFile
      if (filterMarkdown(file)) {
        Log.info(s"Entry changed: ${file}")
        updateOrInsert(file)
      }
      if (file.isDirectory) {
        Log.info(s"Directory changed: ${file}")
        FileMonitor.unmonitorRecursive(FileMonitor.MODIFY, srcDir.toPath)
        FileMonitor.monitorRecursive(FileMonitor.MODIFY, srcDir.toPath)
      }
    })
  }
  
  private def watchDelete(entry: File) {
    println("watch on delete"+entry)
    // @TODO delete event does not fire?
    FileMonitor.monitorRecursive(FileMonitor.DELETE, entry.toPath, { path =>
      Log.info(s"Entry removed: ${entry}")
      delete(entry)
    })
  }
  
  private def delete(file: File) {
    val key = trimExt(file.toString.drop((xitrum.root + "/" + Config.jekytrum.srcDir).length + 1))
    lookup.remove(key)
  }

  def getByKey(key: String): Entry = {
    lookup.get(key) match {
      case Some(entry) => entry
      case None        => tryIndex(key)
    }
  }

  private def updateOrInsert(file: File) {
    val key = trimExt(file.toString.drop((xitrum.root + "/" + Config.jekytrum.srcDir).length + 1))
    val name = file.getName
    val title = trimExt(name)
    val existing = lookup.isDefinedAt(key)
    if (existing && lookup(key).lastModified == file.lastModified) {
      return
    }

    Config.jekytrum.converter.convert(file).onComplete {
      case Failure(e) =>
        Log.warn(s"Failed to convert:${key}", e)
        if (!existing) lookup(key) = entry500
      case Success(None) =>
        Log.warn(s"Failed to convert:${key}")
        if (!existing) lookup(key) = entry500
      case Success(Some(htmlString)) =>
        val entry = new Entry(title, htmlString, file.lastModified, List.empty)
        lookup(key) = entry
        if (!existing) watchDelete(file)
    }
    logEntries(key, existing)
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

  private def trimExt(path: String): String = {
    path.substring(0, path.lastIndexOf('.'))
  }

  private def getMarkdownSources(srcDir: File): Seq[File] = {
    ls(srcDir).filter(filterMarkdown)
  }

  private def filterMarkdown(f: File): Boolean = {
    !f.isDirectory && (
      f.getPath.endsWith(".md") ||
      f.getPath.endsWith(".markdown") ||
      f.getPath.endsWith(".MD") ||
      f.getPath.endsWith(".MARKDOWN"))
  }

  private def ls(file: File): Seq[File] = {
    val files = file.listFiles
    files ++ files.filter(_.isDirectory).flatMap(ls)
  }

  private def logEntries(key: String, update: Boolean = false) {
    if (update)
      Log.info(s"Entry updated: - ${key}")
    else
      Log.info(s"Entry found: - ${key}")
  }

}
