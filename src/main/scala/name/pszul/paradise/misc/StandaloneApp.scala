package name.pszul.paradise.misc
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ DefaultServlet, ServletContextHandler }
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import name.pszul.paradise.AppConfigFactory

/**
 * Standalone application for testing the API servlets.
 */
object StandaloneApp { // this is my entry object as specified in sbt project definition

  val DefaultPort = 8080
  def main(args: Array[String]) {

    // Set config.file property for typesafe config to use
    val port = if (System.getenv("PORT") != null) System.getenv("PORT").toInt else DefaultPort

    val server = new Server(port)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.setInitParameter(AppConfigFactory.InitParamConfigFile, "conf/local.conf")
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start
    server.join
  }
}
