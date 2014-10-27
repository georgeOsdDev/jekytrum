package jekytrum.model

import scala.io.Source
import java.io.File
import scala.collection.mutable.{Map => MMap}
import org.fusesource.scalamd.Markdown

object Entries {

  private val encoding = "utf-8"
  private val srcDir   = "src/main/markdown"
  private val lookup   = MMap.empty[String, String]

  def load() = {
    ls(srcDir).foreach{ file =>
      val source = Source.fromFile(file.toString, encoding)
      val name = file.getName
      lookup(name.substring(0, name.lastIndexOf('.'))) = Markdown(source.getLines.mkString("\n"))
    }
  }

  def ls (dir: String) : Seq[File] = {
    new File(dir).listFiles.filter(f => f.getPath.endsWith(".md") || f.getPath.endsWith(".markdown"))
  }

  def getContent(title:String):String = {

    lookup.get(title) match {
      case Some(content) => content
      case None => "404 Not Found"
    }
  }
}
