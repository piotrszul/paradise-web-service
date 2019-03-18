package name.pszul.paradise.domain

/**
 * Fixtures for sample domain objects to use for testing
 */
object DomainFixtures {

  // exising entities
  val testEntity_0 = Entity(0L, "TestEntity_0")
  val testEntity_1 = Entity(1L, "TestEntity_1")
  val testAddress_2 = Entity(2L, "TestAddress_2")
  val testPath_1_2 = Path(testEntity_1, testAddress_2)
  val testPath_2_1 = Path(testAddress_2, testEntity_1)

  // cypher expression to create fixtures
  val cypherToCreateFixtures = "" +
    " CREATE (n0:Entity {name: 'TestEntity_0', node_id:'0'})" + // this will be created id ID(p) = 0
    " CREATE (n1:Entity {name: 'TestEntity_1', node_id:'1'})" +
    " CREATE (n2:Address {name: 'TestAddress_2', node_id:'2'})" +
    " WITH n2,n1 CREATE (n2)-[:address_of]->(n1)"

  // non existent entites
  val nonExistingId_0 = -1
  val nonExistingId_2 = -2
}
