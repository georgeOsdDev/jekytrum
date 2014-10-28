package jekytrum

import xitrum.Server

import jekytrum.model.Entry

object Boot {
  def main(args: Array[String]) {

    // @TODO
    // Start ElasticSearch node
    // Start File monitor
    // Start File reader actor
    // Start File converter actor

    Entry.load
    Server.start()
  }
}
