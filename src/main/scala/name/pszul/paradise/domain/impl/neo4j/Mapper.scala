package name.pszul.paradise.domain.impl.neo4j

import collection.JavaConverters._
import org.neo4j.driver.v1.Value
import name.pszul.paradise.domain.Entity
import name.pszul.paradise.domain.Path
import org.neo4j.driver.v1.types.Node

/**
 * Functions for mapping neo4j Values to domain objects.
 */
object Mapper {

  def toEntity(neo4jNode: Node): Entity = {
    val labels = neo4jNode.labels.asScala.toList
    require(labels.size == 1, "We expect exactly one label")
    val singleLabel = labels.head
    Entity(neo4jNode.id, singleLabel, neo4jNode.get("name").asString())
  }

  def toEntity(value: Value): Entity = toEntity(value.asNode())

  def toPath(value: Value): Path = {
    val neo4Path = value.asPath()
    Path(neo4Path.nodes().asScala.map(toEntity).map(_.toRef).toList)
  }
}
