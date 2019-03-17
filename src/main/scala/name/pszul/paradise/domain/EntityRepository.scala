package name.pszul.paradise.domain

/**
 * Interface for reporsitory of Entities 
 */

trait EntityRepository {
 
  /**
   * Retrives the domain entity with by id
   * @param id The id of entity to retrieve
   * @return  Some[Entity] if exits for None otherwise
   */
  def getEntity(id:Long):Option[Entity]

}