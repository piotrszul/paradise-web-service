package name.pszul.paradise.domain.impl.neo4j

import name.pszul.paradise.domain.EntityRepository
import name.pszul.paradise.domain.Entity
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Session
import collection.JavaConverters._
import org.neo4j.driver.v1.Value

/**
 * Neo4j/Cypher implementation of EntityRepository
 */
class Neo4jEntityRepository(driver:Driver) extends EntityRepository {

  def toEntity(value:Value):Entity = {
    val node = value.asNode()
    Entity(node.id, node.get("name").asString())
  }
  
  override def getEntity(id: Long): Option[Entity] = {
    val cypherQuery = "MATCH (n) WHERE ID(n) = $id RETURN n"
    var session:Session = null
    try {
      session = driver.session()
      val statementResult = session.run(cypherQuery, Map("id" -> id.asInstanceOf[Object]).asJava)
      if (statementResult.hasNext()) {
        val record = statementResult.single()
        val value = record.get(0)
        Some(toEntity(value))
      } else {
        None
      }
    } finally {
      if (session!=null) {
        session.close()
      }
    }   
  }
}
