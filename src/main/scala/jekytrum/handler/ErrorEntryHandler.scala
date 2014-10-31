package jekytrum.handler

import java.io.File
import scala.util.control.NonFatal
import scala.collection.mutable.{ Map => MMap }
import io.netty.channel.{ ChannelHandler, ChannelHandlerContext, ChannelPromise, ChannelOutboundHandlerAdapter, SimpleChannelInboundHandler }
import io.netty.handler.codec.http.{ HttpHeaders, HttpMethod, HttpRequest, HttpResponseStatus }
import ChannelHandler.Sharable
import HttpMethod.GET
import HttpResponseStatus.{ NOT_FOUND, INTERNAL_SERVER_ERROR }
import xitrum.{ Action, Config => XConfig }
import xitrum.handler.HandlerEnv
import xitrum.handler.inbound.Dispatcher
import jekytrum.Config
import jekytrum.action.NotFoundError

object ErrorEntry {
  val X_404_ENTRY = "X-404-entry"
  val X_404_ENTRY_FROM_ACTION = "X-404-entry-from-action"
  val X_500_ENTRY_FROM_ACTION = "X-500-entry-from-action"

  def set404Entry(env: HandlerEnv, pathInfo: String, fromAction: Boolean = true) {
    if (fromAction) HttpHeaders.setHeader(env.response, X_404_ENTRY_FROM_ACTION, pathInfo)
    else HttpHeaders.setHeader(env.response, X_404_ENTRY, pathInfo)
  }

  def set500Entry(env: HandlerEnv, pathInfo: String) {
    HttpHeaders.setHeader(env.response, X_500_ENTRY_FROM_ACTION, pathInfo)
  }

  def isContainHeader(env: HandlerEnv): Option[String] = {
    if (env.response.headers.contains(X_404_ENTRY)) Some(X_404_ENTRY)
    else if (env.response.headers.contains(X_404_ENTRY_FROM_ACTION)) Some(X_404_ENTRY_FROM_ACTION)
    else if (env.response.headers.contains(X_500_ENTRY_FROM_ACTION)) Some(X_500_ENTRY_FROM_ACTION)
    else None
  }

  def removeHeaders(env: HandlerEnv) {
    HttpHeaders.removeHeader(env.response, X_404_ENTRY)
    HttpHeaders.removeHeader(env.response, X_404_ENTRY_FROM_ACTION)
    HttpHeaders.removeHeader(env.response, X_500_ENTRY_FROM_ACTION)
  }
}

@Sharable
class ErrorEntryInboundHandler extends SimpleChannelInboundHandler[HandlerEnv] {
  override def channelRead0(ctx: ChannelHandlerContext, env: HandlerEnv) {
    val request = env.request
    if (request.getMethod != GET) {
      ctx.fireChannelRead(env)
      return
    }

    val pathInfo = request.getUri.split('?')(0)
    if (pathInfo.startsWith("/jekytrum/api") || pathInfo == "/") {
      ctx.fireChannelRead(env)
      return
    }

    checkSource(pathInfo) match {
      case Some(f) =>
        ctx.fireChannelRead(env)
      case None =>
        ErrorEntry.set404Entry(env, pathInfo, false)
        env.pathParams = MMap("title" -> Seq(pathInfo.drop(1)))
        Dispatcher.dispatch(classOf[NotFoundError], env)
        ctx.channel.writeAndFlush(env)
    }
  }

  private def checkSource(pathInfo: String): Option[File] = {
    val sourcePath =
      if (pathInfo.startsWith(File.separator)) Config.jekytrum.srcDir + pathInfo
      else Config.jekytrum.srcDir + File.separator + pathInfo
    val maybeDirectory = new File(sourcePath)
    val files =
      if (maybeDirectory.exists && maybeDirectory.isDirectory)
        (sourcePath + File.separator + "index.md", sourcePath + File.separator + "index.markdown")
      else
        (sourcePath + ".md", sourcePath + ".markdown")
    val md = new File(files._1)
    if (md.exists) Some(md)
    else {
      val markdown = new File(files._2)
      if (markdown.exists) Some(markdown)
      else None
    }
  }
}

@Sharable
class ErrorEntryOutboundHandler extends ChannelOutboundHandlerAdapter {
  override def write(ctx: ChannelHandlerContext, msg: Object, promise: ChannelPromise) {

    if (!msg.isInstanceOf[HandlerEnv]) {
      ctx.write(msg, promise)
      return
    }

    val env = msg.asInstanceOf[HandlerEnv]
    ErrorEntry.isContainHeader(env) match {
      case Some(ErrorEntry.X_404_ENTRY) =>
        env.response.setStatus(NOT_FOUND)
        ErrorEntry.removeHeaders(env)
        ctx.write(msg, promise)
      case Some(ErrorEntry.X_404_ENTRY_FROM_ACTION) =>
        env.response.setStatus(NOT_FOUND)
        ErrorEntry.removeHeaders(env)
        ctx.write(msg, promise)
      case Some(ErrorEntry.X_500_ENTRY_FROM_ACTION) =>
        env.response.setStatus(INTERNAL_SERVER_ERROR)
        ErrorEntry.removeHeaders(env)
        ctx.write(msg, promise)
      case Some(_) =>
        ctx.write(msg, promise)
      case None =>
        ctx.write(msg, promise)
    }
  }
}
