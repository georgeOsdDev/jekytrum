package jekytrum

import com.typesafe.config.{ Config => TConfig }

import xitrum.Log

import jekytrum.converter.{ MarkdownConverter, NoConverter }

class JekytrumConfig(config: TConfig) {
  val srcDir = if (config.hasPath("srcDir")) config.getString("srcDir") else "src/main/markdown"

  val converter = getConveterInstance

  private def getConveterInstance: MarkdownConverter = {
    if (!config.hasPath("converter")) {
      Log.error("Converter class is not specifyed.")
      new NoConverter
    } else {
      val className = config.getString("converter")
      val klass = Thread.currentThread.getContextClassLoader.loadClass(className)
      klass.newInstance().asInstanceOf[MarkdownConverter]
    }
  }
}

object Config {
  private val conf = xitrum.Config.application.getConfig("jekytrum")
  val jekytrum = new JekytrumConfig(conf)
}