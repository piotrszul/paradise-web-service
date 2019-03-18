package name.pszul.paradise.domain

/**
 * Interface for repository of Entities
 */

trait EntityRepository {

  /**
   * Retrives the domain entity with by id
   * @param id The id of entity to retrieve
   * @return  Some[Entity] if exits for None otherwise
   */
  def getEntity(id: Long): Option[Entity]

  /**
   * Finds the undirected shortest path between two entities
   *
   * @param stardId The id of the entity at the start of the path
   * @param endId The id of the entity at the  end of the path
   *
   * @return Some[Path] when the path exits or None otherwise
   */
  def findShortestPath(startId: Long, endId: Long): Option[Path]

}
