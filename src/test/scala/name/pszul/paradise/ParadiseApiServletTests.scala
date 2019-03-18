package name.pszul.paradise

import org.scalatra.test.scalatest._
import org.scalactic.source.Position.apply
import org.scalamock.scalatest.MockFactory
import name.pszul.paradise.domain.EntityRepository
import name.pszul.paradise.domain.Entity
import name.pszul.paradise.domain.Path
import java.net.HttpURLConnection

class ParadiseApiServletTests extends ScalatraFlatSpec with MockFactory {

  import name.pszul.paradise.domain.DomainFixtures._
  import name.pszul.paradise.JsonFixtures._
  import HttpURLConnection._

  // setup stub for the entityRepository
  val entityRepositoryStub = stub[EntityRepository]
  // setup the API servlet with stubbed repository
  addServlet(new ParadiseApiServlet(entityRepositoryStub), "/*")

  "GET /entity/:id" should "return status 404 if entity does not exit" in {
    (entityRepositoryStub.getEntity _).when(nonExistingId_0).returns(None)
    get(s"/entity/${nonExistingId_0}") {
      status should equal(HTTP_NOT_FOUND)
    }
  }

  "GET /entity/:id" should "return status 200 and entity data for exitsing entity" in {
    (entityRepositoryStub.getEntity _).when(testEntity_0.id).returns(Some(testEntity_0))
    get(s"/entity/${testEntity_0.id}") {
      status should equal(HTTP_OK)
      body should equal(testEntity_0_asJson)
    }
  }

  "GET /entity/:id/shortestPath/:toId" should "return status 404 if path does not exist" in {
    (entityRepositoryStub.findShortestPath _).when(testEntity_0.id, testEntity_1.id).returns(None)
    get(s"/entity/${testEntity_0.id}/shortestPath/${testEntity_1.id}") {
      status should equal(HTTP_NOT_FOUND)
    }
  }

  "GET /entity/:id/shortestPath/:toId" should "return status 200 and valid path if it exist" in {
    (entityRepositoryStub.findShortestPath _).when(testEntity_1.id, testAddress_2.id).returns(Some(testPath_1_2))
    get(s"/entity/${testEntity_1.id}/shortestPath/${testAddress_2.id}") {
      status should equal(HTTP_OK)
      body should equal(testPath_1_2_asJson)
    }
  }

}
