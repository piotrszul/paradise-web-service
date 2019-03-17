import name.pszul.paradise._
import org.scalatra._
import javax.servlet.ServletContext
import com.typesafe.config.{Config, ConfigFactory}
import name.pszul.paradise.domain.impl.neo4j.Neo4jEntityRepository
import scala.collection.mutable.MutableList
import java.io.Closeable
import org.slf4j.LoggerFactory

class ScalatraBootstrap extends LifeCycle {
  
  val logger =  LoggerFactory.getLogger(getClass)
  
  val managedCloseable  = MutableList[Closeable]()
  
  override def init(context: ServletContext) {
    val conf: Config = ConfigFactory.load()
    val entityRepository = Neo4jEntityRepository.fromConfig(conf)
    managedCloseable += entityRepository
    context.mount(new ParadiseApiServlet(entityRepository), "/*")
  }
  
  override def destroy(context: ServletContext) {
    managedCloseable.foreach(_.close())
  }
}
