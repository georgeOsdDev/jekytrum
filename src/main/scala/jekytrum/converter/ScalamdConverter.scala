package jekytrum.converter

import scala.concurrent.{ Future, ExecutionContext }
import ExecutionContext.Implicits.global

import org.fusesource.scalamd.Markdown

class ScalamdConverter extends MarkdownConverter {
  override def convert(src: String) = {
    val f: Future[Option[String]] = Future {
      Some(Markdown(src))
    }
    f
  }
}