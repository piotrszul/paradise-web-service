package name.pszul.paradise.domain

/**
 * Interface for reporsitory of Entities 
 */

trait EntityRepository {
 
  /**
   * Retrives the entiry with by id
   * @param id: the id of entity to retreve
   * @return  Some[Entiry] if exits for None otherwise
   */
  def getEntity(id:Long):Option[Entity]

}