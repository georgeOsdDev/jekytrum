package jekytrum.converter

import scala.concurrent.{ Future, ExecutionContext }
import ExecutionContext.Implicits.global

import org.pegdown.{ Extensions, PegDownProcessor }

class PegdownConverter extends MarkdownConverter {
  override def convert(src: String) = {
    val processor = new PegDownProcessor(Extensions.ALL)
    val f: Future[Option[String]] = Future {
      Some(processor.markdownToHtml(src))
    }
    f
  }
}