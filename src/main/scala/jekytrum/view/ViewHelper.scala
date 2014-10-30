package jekytrum.view

import jekytrum.model.Entry

object ViewHelper {

  private val dataFormat = new java.text.SimpleDateFormat("YYYY/MM/dd HH:mm:ss")

  def listEntries(category: Option[String]): String = {
    val entries = Entry.findByCategory(category)
    """<ul class="entryList">""" +
      entries.sortBy(_("updatedAt")).map { e =>
        s"""
<li class="entry">
  <a href="/${e("url")}">${e("url").split("/").last}</a>
  <span class="updatedAt">(${dataFormat.format(e("updatedAt").toLong).toString})</span>
  <span class="category"><a href="/?category=${e("category")}">${e("category")}</span>
</li>
"""
      }.mkString("\n") +
      "</ul>"
  }

  def ListCategory: String = {
    val categories = Entry.allCategories
    """
<ul class="categoryList">
  <li class="category">
    <a href="/">/</a>
  </li>""" +
      categories.sorted.map { c =>
        s"""
  <li class="category">
    <a href="/?category=${c}">${c}</a>
  </li>
"""
      }.mkString("\n") +
      "</ul>"
  }
}