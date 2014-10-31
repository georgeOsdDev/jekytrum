package jekytrum.model.elasticsearch

import java.io.File
import scala.concurrent.Future
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.action.search.SearchResponse
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.SimpleAnalyzer
import com.sksamuel.elastic4s.WhitespaceAnalyzer
import xitrum.util.SeriDeseri
import jekytrum.Config
import jekytrum.model.Entry
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse

object Node {

  private val home = new File(Config.jekytrum.dataDir + "_" + System.currentTimeMillis).getAbsolutePath

  private val settings = ImmutableSettings.settingsBuilder()
    .put("http.enabled", false)
    .put("path.home", home)

  val client = ElasticClient.local(settings.build)
  client.execute {
    create index "jekytrum" mappings (
      "entries" as (
        "key" typed StringType,
        "title" typed StringType analyzer SimpleAnalyzer,
        "body" typed StringType analyzer SimpleAnalyzer,
        "lastModified" typed LongType,
        "categories" typed StringType))
  }

  def start() {}

  def saveEntry(entry: Entry): Unit = {
    client.execute { index into "jekytrum/entry" doc entry }
  }

  def deleteEntry(entry: Entry): Future[DeleteByQueryResponse] = {
    client.execute {
      delete from "jekytrum/entry" where s"key:${entry.key}"
    }
  }

  def updateEntry(entry: Entry): Unit = {
    deleteEntry(entry).await
    saveEntry(entry)
  }

  def searchEntry(keyword: String): SearchResponse = {
    client.execute {
      search in "jekytrum/entry" query keyword
    }.await
  }
}
