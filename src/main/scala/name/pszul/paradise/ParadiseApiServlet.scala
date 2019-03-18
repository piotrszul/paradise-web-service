package name.pszul.paradise

import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.slf4j.LoggerFactory
import name.pszul.paradise.domain.EntityRepository

class ParadiseApiServlet(entityRepository:EntityRepository) extends ScalatraServlet with JacksonJsonSupport {

  val logger =  LoggerFactory.getLogger(getClass)
  
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  
  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }
  
  get("/") {
    Ok()
  }

  
  /**
   * GET /node/:id 
   *	 
   * @param id The id of the node to retrieve
   */
  get("/node/:id") {
    val id = params("id").toLong
    entityRepository.getEntity(id).getOrElse(NotFound("Entity not found."))
  }
  
  /**
   * GET /node/:id 
   *	 
   * @param id The id of the node to retrieve
   */
  get("/node/:id/shortestPath/:toId") {
    val id = params("id").toLong
    val toId = params("toId").toLong
    entityRepository.findShortestPath(id,toId).getOrElse(NotFound("Path not found."))
  }
  
}
