package name.pszul.paradise.domain

trait EntityLike {
  require(clazz != null, "clazz not null")
  require(name != null, "name not null")
  def id: Long
  def clazz: String
  def name: String
}

/**
 * Class representing a summary reference to domain entity
 */
case class EntityRef(id: Long, clazz: String, name: String) extends EntityLike

/**
 * Class representing a domain entity
 */
case class Entity(id: Long, clazz: String, name: String) extends EntityLike {
  def toRef: EntityRef = EntityRef(id, clazz, name)
}
