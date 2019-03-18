package name.pszul.paradise.domain

/**
 * Represents a path form the first entity to the last in the list
 */
case class Path(steps:List[Entity]) 

object Path {
  def apply(steps:Entity*):Path = Path(steps.toList)
}