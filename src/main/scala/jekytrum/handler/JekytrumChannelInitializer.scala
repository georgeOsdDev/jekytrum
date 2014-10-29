package jekytrum.handler

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.socket.SocketChannel

import xitrum.handler.DefaultHttpChannelInitializer
import xitrum.handler.inbound.Dispatcher
import xitrum.handler.outbound.XSendFile

@Sharable
class JekytrumChannelInitializer extends DefaultHttpChannelInitializer {
  lazy val deletedEntryInboundHandler = new ErrorEntryInboundHandler
  lazy val deletedEntryOutboundHandler = new ErrorEntryOutboundHandler
  override def initChannel(ch: SocketChannel) {
    super.initChannel(ch)
    val p = ch.pipeline
    p.addBefore(classOf[Dispatcher].getName, classOf[ErrorEntryInboundHandler].getName, deletedEntryInboundHandler)
    p.addAfter(classOf[XSendFile].getName, classOf[ErrorEntryOutboundHandler].getName, deletedEntryOutboundHandler)
  }
}