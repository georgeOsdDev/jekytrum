package jekytrum.model.elasticsearch

import org.elasticsearch.common.settings.ImmutableSettings
import com.sksamuel.elastic4s.ElasticClient

class Node {
  val settings = ImmutableSettings.settingsBuilder()
    .put("http.enabled", false)
    .put("path.home", "/var/elastic/")
  val client = ElasticClient.local(settings.build)
}