package name.pszul.paradise.domain

/**
 * Represents a path form the first entity to the last in the list
 */
case class Path(steps: List[EntityRef])

object Path {
  def apply(steps: EntityRef*): Path = Path(steps.toList)
}
