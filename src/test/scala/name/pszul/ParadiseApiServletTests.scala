package name.pszul

import org.scalatra.test.scalatest._

class ParadiseApiServletTests extends ScalatraFunSuite {

  addServlet(classOf[ParadiseApiServlet], "/*")

  test("GET / on ParadiseApiServlet should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

}
