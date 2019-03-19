import name.pszul.paradise._
import org.scalatra._
import javax.servlet.ServletContext
import name.pszul.paradise.domain.impl.neo4j.Neo4jEntityRepository
import scala.collection.mutable.MutableList
import java.io.Closeable
import org.slf4j.LoggerFactory
import java.net.URL
import java.io.File
import name.pszul.paradise.AppConfigFactory

class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)

  val managedCloseable = MutableList[Closeable]()

  override def init(context: ServletContext) {
    val conf = AppConfigFactory.loadFrom(context)
    val entityRepository = Neo4jEntityRepository.fromConfig(conf)
    managedCloseable += entityRepository
    context.mount(new ParadiseApiServlet(entityRepository), "/*")
  }

  override def destroy(context: ServletContext) {
    managedCloseable.foreach(_.close())
  }
}
