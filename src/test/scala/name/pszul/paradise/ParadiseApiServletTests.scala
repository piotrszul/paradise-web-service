package name.pszul.paradise

import org.scalatra.test.scalatest._
import org.scalactic.source.Position.apply
import org.scalamock.scalatest.MockFactory
import name.pszul.paradise.domain.EntityRepository
import name.pszul.paradise.domain.Entity
import name.pszul.paradise.domain.Path

class ParadiseApiServletTests extends ScalatraFlatSpec with MockFactory {

  import name.pszul.paradise.domain.Fixtures._
  
  val testEntity_0_asJson = """{"id":0,"name":"TestEntity_0"}"""
  val testPath_1_2_asJson = """{"steps":[{"id":1,"name":"TestEntity_1"},{"id":2,"name":"TestAddress_2"}]}"""
  
  // setup stub for the entityRepository
  val entityRepositoryStub = stub[EntityRepository]
  addServlet(new ParadiseApiServlet(entityRepositoryStub), "/*")

  "GET /entity/:id" should "return status 404 if entity does not exit" in {
    (entityRepositoryStub.getEntity _).when(-1L).returns(None)
    get("/entity/-1") {
      status should equal (404)
    }
  }

  "GET /entity/:id" should "return status 200 and entity data for exitsing entity" in {
    (entityRepositoryStub.getEntity _).when(testEntity_0.id).returns(Some(testEntity_0))
    get(s"/entity/${testEntity_0.id}") {
      status should equal (200)
      body should equal (testEntity_0_asJson)
    }
  }

  "GET /entity/:id/shortestPath/:toId" should "return status 404 if path does not exist" in {
    (entityRepositoryStub.findShortestPath _).when(0L,1L).returns(None)
    get("/entity/0/shortestPath/1") {
      status should equal (404)
    }
  }

  "GET /entity/:id/shortestPath/:toId" should "return status 200 and valid path if it exist" in {
    (entityRepositoryStub.findShortestPath _).when(1L,2L).returns(Some(Path(testEntity_1, testAddress_2)))
    get("/entity/1/shortestPath/2") {
      status should equal (200)
      body should equal (testPath_1_2_asJson)
    }
  }

}
