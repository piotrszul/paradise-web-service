Paradise Web Service
====================

# Design

## Clarifications

- There’s no need for the API to be scalable, provided performance is reasonable for individual queries.
- Yes, we can treat the graph as being static – a one-off ingest is sufficient.
- You may use any language/graph database/frameworks that you choose.  The role we’re hiring for is Scala, which may be a good option for the language.
- You’re free to choose the graph structure to best model the problem; shortest path on an undirected graph would be fine.


## Techology choices

Given the preference for Scala, the web service API will be based on Scala, Scalatra and ScalaTest for testing.  

The major decision here is, which graph 'database' to choose from. Considering the scale of data and the requirements `neo4j` with `cypher` seem to be a  reasonable choice, for the following reasons:

- it can easily handle graph of the given size (and much larger ones)
- it is an database system ans as such suitable for OLTP workloads (such as backed for REST services) 
- it comes with built-in shortest path `shortesPath` function 
- the sample dataset appears to be exported from neo4j so it should be straighforward to map in back to the neo4j property graph model
- it has good documentation and active community 
- it comes with a variety of drivers for different languages (https://neo4j.com/developer/language-guides/)
- it comes with a JVM testing harness (https://medium.com/neo4j/testing-your-neo4j-based-java-application-34bef487cc3c)


As for other options:

- GraphX, GraphFrames: not really an OLTP solution 
- JanusGraph: may have some benefits for extremly large graphs. Possibly applicable but I am not familiar with it, and seems much more complicated in terms of architecture. Uses `gremlin` as query language so possibly easier to substitute than for another graph database (than neo4j with cyhper). But no goal here.
- Apache ThinkerPop: Possibly applicable but does not seem to have some of advantages of neo4j. 


Cypher vs gremlin: There is a 'germlin' connector to neo4j but for simplicity I am going to use cypher (as there are not requiremnts for make the API graph database agnostic and cypher is less likely not to be compatbile with neo4j in any way)

I will validate these choices with a  [spike](spike/README.md)


## Graph Databse design

Since the extract is (appears to be) from Neo4j the sensible approach seem to recreate the schema of the original graph.

Which comes down to creating nodes with nodes:

- ID(n): id from `n.node_id` column 
- labels: Entity, Officer, Intermediary, Address and Other
- properties: "valid_until","country_codes","countries","sourceID","jurisdiction_description","service_provider","jurisdiction","closed_date","incorporation_date","ibcRUC","type","status","company_type","note" 

and relations:
	
- types: registered_address, officer_of, connected_to, intermediary_of, same_name_as, same_id_as
- properties: idx - back reference to the idx field from the SQL dump

Notes:

- there is longer discussion of the choices for nodes ids in [spike](spike/README.md). Assuming however that neo4j is `read-only` view imporing the nodes with actual ids form SQL dump seem to be the best option.
- all properties are typed as STRINGS (to correspond with TEXT type in the SQL dump)
- null values: the SQL dump appears to be somewhat inconsistent here. No rows have the actual `NULL` values but some columns e.g. `n.status` come with values such as `''` (empty string) or `'null'`. 
  For the purpose of this assignment I going to assume if a value `IS NOT NULL` in SQL it should be literaly imported to Neo4J.  


## Rest API design

	GET /entity/:entity_id  -> JSON(entity)

	eg: {"id":9,"labels":["Address"],"properties":{"address":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","closed_date":"","company_type":"","countries":"","country_codes":"","ibcRUC":"","incorporation_date":"","jurisdiction":"","jurisdiction_description":"","name":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","node_id":"59217552","note":"","service_provider":"","sourceID":"Paradise Papers - Malta corporate registry","status":"","type":"","valid_until":"Malta corporate registry data is current through 2016"}}


	GET /entity/:entity_id/shortestPath/:to_entity_id -> JSON(path)

	e.g: {"steps":[{"id":9,"labels":["Address"],"name":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","uri":"http://127.0.0.1:5000/entity/9"},{"id":59242,"labels":["Entity"],"name":"ALA INT. LIMITED","uri":"http://127.0.0.1:5000/entity/59242"},{"id":60201,"labels":["Entity"],"name":"Euroyacht Limited","uri":"http://127.0.0.1:5000/entity/60201"}]}

Notes:

- Both endpoints return HTTP 404 if entity/path cannot be found
- The path response contains a list of entity summaries (not full node) that includes: id, labels, name.
- To make the API nice entity/entity summary responses also include the `uri` to the entity (`"uri":"http://127.0.0.1:5000/entity/60201"`)

Assumptions:

- Service is 'public', no AAA needed at this stage

##  ETL Design

Since this is one off ingest a 'low tech' shell script should suffice.
The genernal wokflow is:
	
- use `mysql` to `SELECT ... INTO OUTFILE` to export csv files for nodes and edges
- validate the extract (number of nodes/edges vs. number of lines)
- import csv to Neo4j using `neo4j-import`
- validate the import (number of imported nodes/edges vs. number of lines)


# Build & Run

## Running the ETL


Pre-requisites:
	
	- a running instance of MySQL 5.7+ (with access detail)
	- mysql client installed locally
	- neo4j community edition (version 3.5.3) installed locally in $NEO4J_HOME (with initial password for `neo4j` changed)

Import the SQL dump to the sql database, e.g.:

	curl -v https://[url-masked]/paradise.sql  | mysql -u <USERNAME> -p

Configure the etl script in `$HOME/.paradise.sh`. You can copy the tmplate from `etc/conf/paradise.sh.template`.
Per mininum you need to set `NEO4J_HOME`, `MYSQL_USER`, `MYSQL_PWD`. See comment in `etl/bin/run_etl.sh` for more info.

Run the etl script with:

	./etl/bin/run_etl.sh

This will ingest the data to the neo4j database named `paradise.db`.

Point your neo4j instance to this database setting in `$NEOJ4_HOME/conf/neo4j.conf`:

	dbms.active_database=paradise.db

Restart neo4j.
	
	`$NEOJ4_HOME/bin/neo4j restart







# Open issues and tasks














