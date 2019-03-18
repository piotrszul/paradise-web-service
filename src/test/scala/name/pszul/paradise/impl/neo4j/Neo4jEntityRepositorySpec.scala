package name.pszul.paradise.impl.neo4j

import org.scalatra.test.scalatest._
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterAll
import org.neo4j.harness.TestServerBuilders
import org.neo4j.driver.v1.GraphDatabase
import name.pszul.paradise.domain.impl.neo4j.Neo4jEntityRepository
import org.scalatest.Matchers
import name.pszul.paradise.domain.Entity
import name.pszul.paradise.domain.Path

class Neo4jEntityRepositorySpec extends FlatSpec with BeforeAndAfterAll with Matchers {

  import name.pszul.paradise.domain.DomainFixtures._

  // setup in  process instance of Neo4j for testing
  lazy val graphDb = TestServerBuilders
    .newInProcessBuilder()
    .withFixture(cypherToCreateFixtures)
    .newServer();

  // setup the driver and repository connected to the test neo4j
  lazy val driver = GraphDatabase.driver(graphDb.boltURI())
  lazy val entityRepository = new Neo4jEntityRepository(driver)

  override def afterAll() {
    // cleanup Neo4J resources
    driver.close()
    graphDb.close()
  }

  "getEntity" should "return None when entity id does not exists" in {
    entityRepository.getEntity(nonExistingId_0) should be(None)
  }

  "getEntity" should "return populated when entity id exists" in {
    entityRepository.getEntity(testEntity_0.id) should be(Some(testEntity_0))
  }

  "findShortestPath" should "return None when entities do not exist" in {
    entityRepository.findShortestPath(nonExistingId_0, nonExistingId_2) should be(None)
  }

  "findShortestPath" should "return None when a path cannot be found between existing entities" in {
    entityRepository.findShortestPath(testEntity_0.id, testEntity_1.id) should be(None)
  }

  "findShortestPath" should "return a directed path when exists" in {
    entityRepository.findShortestPath(testAddress_2.id, testEntity_1.id) should be(Some(testPath_2_1))
  }

  "findShortestPath" should "return a undirected path when exists" in {
    entityRepository.findShortestPath(testEntity_1.id, testAddress_2.id) should be(Some(testPath_1_2))
  }
}
