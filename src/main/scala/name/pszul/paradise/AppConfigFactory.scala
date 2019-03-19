package name.pszul.paradise

import javax.servlet.ServletContext
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File
import org.slf4j.LoggerFactory

/**
 * Factory for creating application configuration
 */
object AppConfigFactory {

  val logger = LoggerFactory.getLogger(getClass)

  val InitParamConfigFile = "config-file"

  /**
   * Loads application configuration from a file defined in
   * init parameter `config-file`.
   *
   * @param context: ServletContext to use
   *
   * @return typsafe configuration object
   */
  def loadFrom(context: ServletContext): Config = {
    val configFile = context.getInitParameter(InitParamConfigFile)
    logger.info("Loading configuration from file: {}", configFile)
    ConfigFactory.parseFile(new File(configFile))
  }
}
