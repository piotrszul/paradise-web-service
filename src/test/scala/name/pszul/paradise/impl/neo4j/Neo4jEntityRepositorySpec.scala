package name.pszul.paradise.impl.neo4j

import org.scalatra.test.scalatest._
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterAll
import org.neo4j.harness.TestServerBuilders
import org.neo4j.driver.v1.GraphDatabase
import name.pszul.paradise.domain.impl.neo4j.Neo4jEntityRepository
import org.scalatest.Matchers
import name.pszul.paradise.domain.Entity

class Neo4jEntityRepositorySpec extends FlatSpec with BeforeAndAfterAll with Matchers  {

   val validEntity = Entity(0L, "TestEntity")
  
   // setup in  process instance of Neo4j for testing
   lazy val graphDb = TestServerBuilders
            .newInProcessBuilder()
            .withFixture(
                "CREATE (:Entity {name: 'TestEntity', node_id:'33'})" // this will be created id ID(p) = 0
            )
            .newServer();
   
   lazy val driver = GraphDatabase.driver(graphDb.boltURI())  
   lazy val entityRepository = new Neo4jEntityRepository(driver)
   
   
   override def afterAll() {
     // cleanup Neo4J resources
     driver.close()
     graphDb.close()
   }
  
   "getEntity" should "return None when entity id does not exists" in { 
     entityRepository.getEntity(1L) should be (None)
   } 

   "getEntity" should "return populated when entity id exists" in { 
     entityRepository.getEntity(0L) should be (Some(validEntity))
   } 
  
}