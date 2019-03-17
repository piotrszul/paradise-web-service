package name.pszul.paradise

import org.scalatra.test.scalatest._
import org.scalactic.source.Position.apply

class ParadiseApiServletTests extends ScalatraFunSuite {

  addServlet(classOf[ParadiseApiServlet], "/*")

  test("GET / on ParadiseApiServlet should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

}
