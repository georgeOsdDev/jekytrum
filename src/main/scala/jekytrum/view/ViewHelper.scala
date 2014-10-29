package jekytrum.view

import jekytrum.model.Entry

object ViewHelper {
  def listEntries(category: Option[String]): String = {
    val entries = Entry.findByCategory(category)
    """<ul class="entryList">""" +
      entries.map { e =>
        s"""<li class="entry"><a href="/${e("url")}">${e("url").split("/").last}(${e("updatedAt")})</a></li>"""
      }.mkString("\n") +
      "</ul>"
  }
  def ListCategory: String = {
    val categories = Entry.allCategories
    """<ul class="categoryList">""" +
      categories.map(c => s"""<li class="category">${c}</li>""").mkString("\n") +
      "</ul>"
  }
}