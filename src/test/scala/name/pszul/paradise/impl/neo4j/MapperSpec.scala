package name.pszul.paradise.impl.neo4j

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalamock.scalatest.MockFactory
import org.neo4j.driver.v1.types.Node
import collection.JavaConverters._
import org.neo4j.driver.v1.Value
import name.pszul.paradise.domain.impl.neo4j.Mapper
import org.neo4j.driver.internal.value.StringValue

class MapperSpec extends FlatSpec with Matchers with MockFactory {

  import name.pszul.paradise.domain.DomainFixtures._
  import name.pszul.paradise.impl.neo4j.Neo4JFixtures._

  it should "map Node correctly to Entity" in {
    Mapper.toEntity(TestNode_1) should be(TestEntity_0)
  }
}
