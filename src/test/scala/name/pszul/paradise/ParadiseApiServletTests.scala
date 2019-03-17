package name.pszul.paradise

import org.scalatra.test.scalatest._
import org.scalactic.source.Position.apply
import org.scalamock.scalatest.MockFactory
import name.pszul.paradise.domain.EntityRepository
import name.pszul.paradise.domain.Entity

class ParadiseApiServletTests extends ScalatraFlatSpec with MockFactory {

  val testEntity = Entity(20, "Test Name")
  val expectedJSONEntity = """{"id":20,"name":"Test Name"}"""
  val entityRepositoryStub = stub[EntityRepository]
  addServlet(new ParadiseApiServlet(entityRepositoryStub), "/*")

  "GET /node/:id" should "return status 404 if node exits" in {
    (entityRepositoryStub.getEntity _).when(10).returns(None)
    get("/node/10") {
      status should equal (404)
    }
  }

  "GET /node/:id" should "return status 200 and node data for exitsing node" in {
    (entityRepositoryStub.getEntity _).when(testEntity.id).returns(Some(testEntity))
    get(s"/node/${testEntity.id}") {
      status should equal (200)
      body should equal (expectedJSONEntity)
    }
  }

}
