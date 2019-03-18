package name.pszul.paradise

import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.slf4j.LoggerFactory
import name.pszul.paradise.domain.EntityRepository

/**
 * Servlet implementing REST API
 */
class ParadiseApiServlet(entityRepository:EntityRepository) extends ScalatraServlet with JacksonJsonSupport {

  val logger =  LoggerFactory.getLogger(getClass)
  
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  
  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }
  
  /**
   * GET /
   * 
   * Gets the name of the API
   * @return the name of the API
   */
  
  get("/") {
    Ok("ParadiseAPI")
  }
  
  /**
   * GET /entity/:id 
   *	 
   * Retrieves the entity by id
   * 
   * @param id The id of the entity to retrieve
   * 
   * @return the entity if exits, otherwise 404
   */
  get("/entity/:id") {
    val id = params("id").toLong
    entityRepository.getEntity(id).getOrElse(NotFound("Entity not found."))
  }
  
  /**
   * GET /entity/:id/shortestPath/:toId" 
   *	 
   * Find the undirected shortest path between two entities
   * 
   * @param id The id of the starting entity
   * @param toId The id of the terminal entity
   * 
   * @return the shortest path between the entities if exits, otherwise 404
   */
  get("/entity/:id/shortestPath/:toId") {
    val id = params("id").toLong
    val toId = params("toId").toLong
    entityRepository.findShortestPath(id,toId).getOrElse(NotFound("Path not found."))
  }
  
}
