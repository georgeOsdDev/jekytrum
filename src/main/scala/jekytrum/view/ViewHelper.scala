package jekytrum.view

import xitrum.Action
import jekytrum.model.Entry

trait ViewHelper {
  this: Action =>

  private val dataFormat = new java.text.SimpleDateFormat("YYYY/MM/dd HH:mm:ss")

  def listEntries(category: Option[String], result: Option[List[Entry]]): String = {
    val entries = result match {
      case Some(r) => r
      case _       => Entry.findByCategory(category)
    }
    val list = entries.sortBy(_.lastModified).map { e =>
      s"""
<li class="entry">
  <a href="${absUrlPrefix}/${e.toUrl}">${e.title}</a>
  <span class="updatedAt">(${dataFormat.format(e.lastModified).toString})</span>
  <span class="category"><a href="${absUrlPrefix}/?category=${e.category}">${e.category}</a></span>
</li>
"""
    }
    s"""<ul class="entryList">${list.mkString("\n")}</ul>"""
  }

  def listCategories: String = {
    val categories = Entry.allCategories
    val list = categories.sorted.map { c =>
      s"""
  <li class="category">
    <a href="/?category=${c}">${c}</a>
  </li>
"""
    }
    s"""<ul class="categoryList"><li class="category"><a href="/">/</a></li>${list.mkString("\n")}</ul>"""
  }

  def nextLink(current: Entry): String = {
    current.next match {
      case Some(e) => s"""<a href="${absUrlPrefix}/${e.toUrl}">${e.title}</a>"""
      case None    => ""
    }
  }

  def prevLink(current: Entry): String = {
    current.prev match {
      case Some(e) => s"""<a href="${absUrlPrefix}/${e.toUrl}">${e.title}</a>"""
      case None    => ""
    }
  }

  val searchBox: String = {
    """
<div class="jekytrum-search"><form action="/jekytrum/api/search" method="GET">
  <input type="text" name="keyword"></input>
  <input type="submit" value="Search"/>
</form></div>
"""
  }
}
