package jekytrum.model

import java.io.File
import java.nio.file.{ Path, Paths }
import scala.collection.mutable.{ Map => MMap }
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.{ Failure, Success }
import xitrum.Log
import xitrum.util.FileMonitor
import jekytrum.Config

trait Entry {
  val title: String
  val body: String
  val lastModified: Long
  val categories: List[String]

  def toUrl: String = {
    if (categories.isEmpty) title
    else categories.last + "/" + title
  }

  def category: String = {
    if (categories.isEmpty) ""
    else categories.last
  }
}
case class EntryNormal(title: String,
                       body: String,
                       lastModified: Long,
                       categories: List[String]) extends Entry
case class Entry404(title: String = "404",
                    body: String = "404 Not Found",
                    lastModified: Long = 0L,
                    categories: List[String] = List.empty) extends Entry
case class Entry500(title: String = "500",
                    body: String = "500 System Error",
                    lastModified: Long = 0L,
                    categories: List[String] = List.empty) extends Entry

object Entry {

  private val WAIT_MS = 1000
  // @TODO move lookup table to cache engine for clustering usage
  private val lookup = MMap.empty[String, Entry]
  private val lastConvertTime = MMap.empty[String, Long]
  private val entry404 = new Entry404
  private val entry500 = new Entry500
  private val srcDir = new File(Config.jekytrum.srcDir)

  def load() = {
    Log.debug("Convert source markdown files with " + Config.jekytrum.converter.getClass)
    val resources = ls(srcDir)
    getMarkdownSources(resources).foreach { file =>
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
          Log.info(s"Entry converted: from File(${file}) to URL(/${key})")
          val entry = new EntryNormal(title, htmlString, file.lastModified, categorize(key))
          lookup(key) = entry
          watchDelete(file)
          watchModify(file)
      }
    }
    watchDir(srcDir)
  }

  def getByKey(key: String): Entry = {
    lookup.get(key) match {
      case Some(entry) => entry
      case None        => tryConvert(key)
    }
  }

  def findByCategory(category: Option[String]): List[Map[String, String]] = {
    category match {
      case Some(c) => lookup.values.filter(_.categories.contains(c)).map { e =>
        Map("url" -> e.toUrl, "updatedAt" -> e.lastModified.toString, "category" -> e.category)
      }.toList
      case None => lookup.values.map { e =>
        Map("url" -> e.toUrl, "updatedAt" -> e.lastModified.toString, "category" -> e.category)
      }.toList
    }
  }

  def allCategories: List[String] = {
    lookup.values.map(_.categories).flatMap(f => f).toSeq.distinct.toList
  }

  // Monitor create/delete event
  private def watchDir(dir: File) {
    val absPath = absolutePathFromFile(dir)

    FileMonitor.unmonitorRecursive(FileMonitor.CREATE, absPath)
    FileMonitor.monitorRecursive(FileMonitor.CREATE, absPath, { path =>
      Log.debug("CREATE event on directory " + dir.toString + " : " + path.toString)
      val file = path.toFile
      if (filterMarkdown(file)) {
        // if file exists, it means new file or update
        if (file.exists) updateOrInsert(path.toFile)
        else delete(file)
      }
      if (file.isDirectory) {
        Log.info(s"Directory deleted: ${file}")
      }
      watchDir(dir)
    })

    FileMonitor.unmonitorRecursive(FileMonitor.MODIFY, absPath)
    FileMonitor.monitorRecursive(FileMonitor.MODIFY, absPath, { path =>
      Log.debug("Modify event on directory " + dir.toString + " : " + path.toString)
      val file = path.toFile
      if (filterMarkdown(file)) {
        // if file exists, it means new file or update
        if (file.exists) updateOrInsert(path.toFile)
        else delete(file)
      }
      if (file.isDirectory) {
        Log.info(s"Directory modified: ${file}")
      }
      watchDir(dir)
    })

    FileMonitor.unmonitorRecursive(FileMonitor.DELETE, absPath)
    FileMonitor.monitorRecursive(FileMonitor.DELETE, absPath, { path =>
      Log.debug("DELETE event on directory " + dir.toString + " : " + path.toString)
      val file = path.toFile
      if (filterMarkdown(file)) {
        // if file exists, it means new file or update
        if (file.exists) updateOrInsert(path.toFile)
        else delete(file)
      }
      if (file.isDirectory) {
        Log.info(s"Directory created: ${file}")
      }
      watchDir(dir)
    })
  }

  private def watchDelete(entry: File) {
    val absPath = absolutePathFromFile(entry)
    FileMonitor.unmonitor(FileMonitor.DELETE, absPath)
    FileMonitor.monitor(FileMonitor.DELETE, absPath, { path =>
      Log.debug("Delete event on file " + path.toString)
      delete(entry)
      FileMonitor.unmonitor(FileMonitor.DELETE, absPath)
    })
  }

  // Monitor update event
  private def watchModify(entry: File) {
    val absPath = absolutePathFromFile(entry)
    FileMonitor.unmonitor(FileMonitor.MODIFY, absPath)
    FileMonitor.monitor(FileMonitor.MODIFY, absPath, { path =>
      Log.debug("Modyfy event on file " + path.toString)
      updateOrInsert(path.toFile)
    })
  }

  private def updateOrInsert(file: File) {
    val key = trimExt(file.toString.drop((xitrum.root + "/" + Config.jekytrum.srcDir).length + 1))
    val name = file.getName
    val title = trimExt(name)
    val existing = lookup.isDefinedAt(key)
    if (!file.exists ||
      existing && file.lastModified == lookup(key).lastModified ||
      existing && debounce(key)) {
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
        Log.info("Entry converted: from File(" + file + ") to URL(/" + key + ")")
        val entry = new EntryNormal(title, htmlString, file.lastModified, categorize(key))
        lookup(key) = entry
        if (!existing) {
          watchDelete(file)
          watchModify(file)
        }
    }
  }

  private def debounce(key: String, wait: Long = WAIT_MS): Boolean = {
    if (System.currentTimeMillis - lastConvertTime.getOrElse(key, 0L) < wait)
      true
    else {
      lastConvertTime(key) = System.currentTimeMillis
      false
    }
  }

  private def delete(file: File) {
    val key = trimExt(file.getAbsolutePath.drop((xitrum.root + "/" + Config.jekytrum.srcDir).length + 1))
    lookup.remove(key)
  }

  private def tryConvert(key: String): Entry = {
    val file = {
      val md = new File(Config.jekytrum.srcDir + "/" + key + ".md")
      if (md.exists) md
      else new File(Config.jekytrum.srcDir + "/" + key + ".markdown")
    }
    if (file.exists) {
      val name = file.getName
      val title = trimExt(name)
      val future = Config.jekytrum.converter.convert(file)
      Await.ready(future, Duration.Inf)
      future.value.get match {
        case Failure(e) =>
          Log.warn(s"Failed to convert:${key}", e)
          lookup(key) = entry500
          entry500
        case Success(None) =>
          Log.warn(s"Failed to convert:${key}")
          lookup(key) = entry500
          entry500
        case Success(Some(htmlString)) =>
          Log.info(s"Entry converted: from File(${file}) to URL(/${key})")
          val entry = new EntryNormal(title, htmlString, file.lastModified, categorize(key))
          lookup(key) = entry
          watchDelete(file)
          watchModify(file)
          entry
      }
    } else tryIndex(key)
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

  private def absolutePathFromFile(f: File): Path = {
    f.toPath.toAbsolutePath
  }

  private def trimExt(path: String): String = {
    path.substring(0, path.lastIndexOf('.'))
  }

  private def getMarkdownSources(resources: Seq[File]): Seq[File] = {
    resources.filter(filterMarkdown)
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

  private def categorize(key: String): List[String] = {
    if (!key.contains("/")) List.empty
    else {

      def categoryze(acc: List[String], keys: Seq[String]): List[String] = {
        if (keys.isEmpty) acc
        else {
          val parent = if (acc.isEmpty) "" else acc.last + "/"
          categoryze(acc ++ List(parent + keys.head), keys.tail)
        }
      }
      val keys = key.split("/")
      categoryze(List.empty, keys.init)
    }
  }

}
