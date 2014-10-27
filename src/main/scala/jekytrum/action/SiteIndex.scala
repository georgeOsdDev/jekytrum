package jekytrum.action

import xitrum.annotation.GET

import jekytrum.model.Entries

@GET("/:title")
class SiteIndex extends DefaultLayout {
  def execute() {
    val content = Entries.getContent(param("title"))
    respondInlineView(content)
  }
}
