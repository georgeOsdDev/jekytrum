package jekytrum

import scala.util.control.NonFatal
import com.typesafe.config.{ Config => TConfig }
import xitrum.{ Config => XConfig, Log }
import jekytrum.converter.{ MarkdownConverter, NoConverter }

class JekytrumConfig(config: TConfig) {
  val srcDir = if (config.hasPath("srcDir")) config.getString("srcDir") else "src/main/markdown"
  val encoding = if (config.hasPath("encoding")) config.getString("encoding") else "utf-8"
  val converter = getConveterInstance

  private def getConveterInstance: MarkdownConverter = {
    if (!config.hasPath("converter")) {
      Log.error("Converter class is not specifyed. NoConverter.class will be used.")
      new NoConverter
    } else {
      try {
        val className = config.getString("converter")
        val klass = Thread.currentThread.getContextClassLoader.loadClass(className)
        klass.newInstance().asInstanceOf[MarkdownConverter]
      } catch {
        case NonFatal(e) =>
          XConfig.exitOnStartupError("Could not load cache converter, please check config/jekytrum.conf", e)
          throw e
      }
    }
  }
  // dummy
  def start() {}
}

object Config {
  private val conf = XConfig.application.getConfig("jekytrum")
  val jekytrum = new JekytrumConfig(conf)
}