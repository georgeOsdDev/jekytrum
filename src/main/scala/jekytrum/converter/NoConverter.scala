package jekytrum.converter

import scala.concurrent.Future

class NoConverter extends MarkdownConverter {
  override def convert(src: String) = {
    Future.successful(Some(src))
  }
}

