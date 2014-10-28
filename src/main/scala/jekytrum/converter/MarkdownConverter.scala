package jekytrum.converter

import java.io.File
import scala.concurrent.Future
import org.apache.commons.io.FileUtils

trait MarkdownConverter {

  def convert(src: String): Future[Option[String]]

  def convert(file: File): Future[Option[String]] = {
    convert(FileUtils.readFileToString(file))
  }
}