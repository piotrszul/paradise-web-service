package name.pszul.paradise.domain.impl.neo4j

import name.pszul.paradise.domain.EntityRepository
import name.pszul.paradise.domain.Entity
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Session
import collection.JavaConverters._
import org.neo4j.driver.v1.Value
import com.typesafe.config.Config
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.AuthTokens
import java.io.Closeable
import name.pszul.paradise.domain.Path
import name.pszul.paradise.domain.impl.neo4j.Mapper._

/**
 * Neo4j/Cypher implementation of EntityRepository
 */
class Neo4jEntityRepository(driver: Driver) extends EntityRepository with Closeable {

  def querySingle[T](cypherQuery: String, params: Map[String, Any], mapper: Value => T): Option[T] = {
    var session: Session = null
    try {
      session = driver.session()
      val statementResult = session.run(cypherQuery, params.mapValues(_.asInstanceOf[Object]).asJava)
      if (statementResult.hasNext()) {
        val record = statementResult.single()
        val value = record.get(0)
        Some(mapper(value))
      } else {
        None
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  override def getEntity(id: Long): Option[Entity] = {
    querySingle("MATCH (n) WHERE ID(n) = $id RETURN n", Map("id" -> id), toEntity)
  }

  override def findShortestPath(startId: Long, endId: Long): Option[Path] = {
    querySingle(
      "MATCH path=shortestPath((b)-[*]-(e)) WHERE ID(b)=$start_id AND ID(e)=$end_id RETURN path",
      Map("start_id" -> startId, "end_id" -> endId), toPath)
  }

  /**
   * Release resources
   */
  override def close() {
    driver.close()
  }

}

/**
 * Companion object for Neo4jEntityRepository
 */
object Neo4jEntityRepository {

  /**
   * Creates a Neo4jEntityRepository from a typesafe config.
   *
   * The config should include the followin keys: `neo4j.url`, `neo4j.username`, `neo4j.password`.
   *
   * @param config The configuration to use
   * @return Neo4jEntityRepository connected database from the config
   */
  def fromConfig(conf: Config): Neo4jEntityRepository = {
    val driver = GraphDatabase.driver(
      conf.getString("neo4j.url"),
      AuthTokens.basic(conf.getString("neo4j.username"), conf.getString("neo4j.password")))
    new Neo4jEntityRepository(driver)
  }
}
