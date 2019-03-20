package name.pszul.paradise.domain

/**
 * Fixtures for sample domain objects to use for testing
 */
object DomainFixtures {

  // exising entities
  val TestEntity_0 = Entity(0L, "Entity", "TestEntity_0")
  val TestEntity_1 = Entity(1L, "Entity", "TestEntity_1")
  val TestAddress_2 = Entity(2L, "Address", "TestAddress_2")
  val TestPath_1_2 = Path(TestEntity_1.toRef, TestAddress_2.toRef)
  val TestPath_2_1 = Path(TestAddress_2.toRef, TestEntity_1.toRef)

  // cypher expression to create fixtures
  val CypherToCreateFixtures = "" +
    " CREATE (n0:Entity {name: 'TestEntity_0', node_id:'0'})" + // this will be created id ID(p) = 0
    " CREATE (n1:Entity {name: 'TestEntity_1', node_id:'1'})" +
    " CREATE (n2:Address {name: 'TestAddress_2', node_id:'2'})" +
    " WITH n2,n1 CREATE (n2)-[:address_of]->(n1)"

  // non existent entites
  val NonExistingId_0 = -1
  val NonExistingId_2 = -2
}
