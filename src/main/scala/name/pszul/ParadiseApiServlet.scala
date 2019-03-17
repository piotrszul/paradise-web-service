package name.pszul

import org.scalatra._

class ParadiseApiServlet extends ScalatraServlet {

  get("/") {
    views.html.hello()
  }

}
