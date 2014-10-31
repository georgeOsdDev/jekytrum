package jekytrum.action

import xitrum.annotation.{ Error404, Error500 }
import jekytrum.model.{ Entry404, Entry500 }

@Error404
class NotFoundError extends EntryLayout {
  override def execute() {
    val entry = new Entry404
    at("entry") = entry
    respondInlineView(entry.body)
  }
}

@Error500
class ServerError extends EntryLayout {
  override def execute() {
    val entry = new Entry500
    at("entry") = entry
    respondInlineView(entry.body)
  }
}
