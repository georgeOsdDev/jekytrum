package jekytrum.converter

import scala.concurrent._
import ExecutionContext.Implicits.global

import com.ning.http.client.{ AsyncHttpClient, AsyncCompletionHandler, Response }

class GFMConverter extends MarkdownConverter {
  private val asyncHttpClient = new AsyncHttpClient
  private val GITHUB_API_URL = "https://api.github.com/markdown/raw"

  override def convert(src: String) = {
    val builder = asyncHttpClient.preparePost(GITHUB_API_URL)
      .addHeader("Content-type", "text/plain")
      .setBody(src)

    val p = Promise[Option[String]]
    builder.execute(new AsyncCompletionHandler[Response] {
      override def onCompleted(response: Response): Response = {
        if (response.getStatusCode == 200) p.success(Some(response.getResponseBody)) else p.success(None)
        response
      }

      override def onThrowable(t: Throwable) {
        p.failure(t)
      }
    })
    p.future
  }
}