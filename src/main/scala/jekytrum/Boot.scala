package jekytrum

import xitrum.Server

import jekytrum.model.Entry
import jekytrum.handler.JekytrumChannelInitializer

object Boot {

  def main(args: Array[String]) {

    // @TODO
    // Start ElasticSearch node
    Config.start
    Entry.load
    val channelInitializer = new JekytrumChannelInitializer
    Server.start(channelInitializer)
  }
}