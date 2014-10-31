package jekytrum

import java.io.File
import scala.util.control.NonFatal
import com.typesafe.config.{ Config => TConfig }
import xitrum.{ Action, Config => XConfig, Log }

import jekytrum.converter.{ MarkdownConverter, NoConverter }

class ThemeConfig(config: TConfig) {

  val themeDir = "theme"
  val indexLayout = if (config.hasPath("indexLayout")) themeDir + File.separator + config.getString("indexLayout") else "jekytrum/action/RootIndex"
  val entryLayout = if (config.hasPath("entryLayout")) themeDir + File.separator + config.getString("entryLayout") else "jekytrum/action/EntryLayout"
}

class JekytrumConfig(config: TConfig) {
  val srcDir = getDir("srcDir", "src" + File.separator + "main" + File.separator + "markdown")
  val dataDir = getDir("dataDir", "tmp" + File.separator + "data")
  val encoding = if (config.hasPath("encoding")) config.getString("encoding") else "utf-8"
  val converter = getConveterInstance
  val theme: Option[ThemeConfig] = if (config.hasPath("theme")) Some(new ThemeConfig(config.getConfig("theme"))) else None

  private def getDir(key: String, default: String): String = {
    if (config.hasPath(key)) {
      if (config.getString(key).startsWith(File.separator)) config.getString(key).drop(1)
      else config.getString(key)
    } else default
  }

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
}

object Config {
  private val conf = XConfig.application.getConfig("jekytrum")
  val jekytrum = new JekytrumConfig(conf)
  // dummy
  def start() {}

}
