package jekytrum

import xitrum.Server

import jekytrum.model.Entry

object Boot {
  def main(args: Array[String]) {

    // @TODO
    // Start ElasticSearch node

    Entry.load
    Server.start()
  }
}
