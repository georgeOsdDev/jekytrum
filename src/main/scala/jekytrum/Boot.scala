package jekytrum

import xitrum.Server

import jekytrum.model.Entry
import jekytrum.model.elasticsearch.Node
import jekytrum.handler.JekytrumChannelInitializer

object Boot {

  def main(args: Array[String]) {
    Config.start
    Node.start
    Entry.load
    val channelInitializer = new JekytrumChannelInitializer
    Server.start(channelInitializer)
  }
}
