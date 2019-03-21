package name.pszul.paradise.impl.neo4j

import collection.JavaConverters._
import org.neo4j.driver.v1.Value
import name.pszul.paradise.domain.impl.neo4j.Mapper
import org.neo4j.driver.internal.value.StringValue
import org.neo4j.driver.internal.InternalNode

/**
 * Fixtures for Neo4J objects
 */
object Neo4JFixtures {

  val TestNode_1 = new InternalNode(0L, List("Entity").asJavaCollection,
    Map[String, Value]("name" -> new StringValue("TestEntity_0")).asJava)

}
