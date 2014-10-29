package jekytrum

import xitrum.Server

import jekytrum.model.Entry
import jekytrum.handler.JekytrumChannelInitializer

object Boot {

  def main(args: Array[String]) {

    // @TODO
    // Start ElasticSearch node

    Entry.load
    val channelInitializer = new JekytrumChannelInitializer
    Server.start(channelInitializer)
  }
}