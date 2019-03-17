package name.pszul.paradise

import org.scalatra._

class ParadiseApiServlet extends ScalatraServlet {

  get("/") {
    Ok()
  }

}
