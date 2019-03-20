Paradise Web Service
====================

The project follows the standard `maven/sbt` source layout for web applications:

- `src` - source files
- `target`  - compiled artefacts

with some additional directories:

- `dev` -  supporting develpement/build files 
- `conf` - configuration files
- `etl` - database ingeest 
- `spike` - technical spike 


# Design

## Clarifications

- There’s no need for the API to be scalable, provided performance is reasonable for individual queries.
- Yes, we can treat the graph as being static – a one-off ingest is sufficient.
- You may use any language/graph database/frameworks that you choose.  The role we’re hiring for is Scala, which may be a good option for the language.
- You’re free to choose the graph structure to best model the problem; shortest path on an undirected graph would be fine.


## Techology choices

Given the preference for Scala, the web service API will be based on Scala, `Scalatra` and `ScalaTest` for testing.  

The major decision here is, which graph 'database' to use. Considering the scale of data and the requirements `neo4j` with `cypher` seem to be a  reasonable choice, for the following reasons:

- it can easily handle graph of the given size (and much larger ones)
- it is an database system and as such suitable for OLTP workloads (such as backed for REST services) 
- it comes with built-in shortest path `shortesPath` function 
- the sample dataset appears to be exported from neo4j so it should be straightforward to map in back to the neo4j property graph model
- it has good documentation and active community 
- it comes with a variety of drivers for different languages (https://neo4j.com/developer/language-guides/)
- it comes with a JVM testing harness (https://medium.com/neo4j/testing-your-neo4j-based-java-application-34bef487cc3c)


As for other options:

- GraphX, GraphFrames: not really an OLTP solution 
- JanusGraph: may have some benefits for extremely large graphs. Possibly applicable but I am not familiar with it, and seems much more complicated in terms of architecture. Uses `gremlin` as query language so possibly easier to substitute than for another graph database (than `neo4j` with `cyhper`). But no goal here.
- Apache ThinkerPop: Possibly applicable but does not seem to have some of advantages of neo4j. 


Cypher vs gremlin: There is a `germlin` connector to neo4j but for simplicity I am going to use `cypher` (as there are not explicit requirements to make the API graph database agnostic and `cypher` is less likely not to be compatible with neo4j in any way)

I will validate these choices with a [spike](spike/README.md)


## Graph Databse design

Since the extract is (appears to be) from Neo4j the sensible approach seem to recreate the schema of the original graph.

Which comes down to creating nodes:

- ID(n): id from `n.node_id` column 
- labels: Entity, Officer, Intermediary, Address and Other
- properties: `"valid_until","country_codes","countries","sourceID","jurisdiction_description","service_provider","jurisdiction","closed_date","incorporation_date","ibcRUC","type","status","company_type","note"`

and relations:
	
- types: `registered_address, officer_of, connected_to, intermediary_of, same_name_as, same_id_as`
- properties: `idx` - back reference to the `idx` field from the SQL dump

Notes:

- there is longer discussion of the choices for nodes ids in [spike](spike/README.md). Assuming however that neo4j is 'read-only' view importing the nodes with actual ids form SQL dump seem to be the best option.
- all properties are typed as `STRINGS` (to correspond with `TEXT` type in the SQL dump). More fine grained types can be introduced later
- `null` values: the SQL dump appears to be somewhat inconsistent here. No rows have the actual `NULL` values but some columns e.g. `n.status` come with values such as `''` (empty string) or `'null'`. 
  For the purpose of this assignment I going to assume if a value `IS NOT NULL` in SQL it should be imported to Neo4J verbatim.  


## Rest API design

	GET /entity/:entity_id  -> JSON(entity)

	eg: {"id":9,"class":"Address","properties":{"address":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","closed_date":"","company_type":"","countries":"","country_codes":"","ibcRUC":"","incorporation_date":"","jurisdiction":"","jurisdiction_description":"","name":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","node_id":"59217552","note":"","service_provider":"","sourceID":"Paradise Papers - Malta corporate registry","status":"","type":"","valid_until":"Malta corporate registry data is current through 2016"}}


	GET /entity/:entity_id/shortestPath/:to_entity_id -> JSON(path)

	e.g: {"steps":[{"id":9,"class":"Address","name":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","uri":"http://127.0.0.1:5000/entity/9"},{"id":59242,"class":"Entity","name":"ALA INT. LIMITED","uri":"http://127.0.0.1:5000/entity/59242"},{"id":60201,"class":"Entity","name":"Euroyacht Limited","uri":"http://127.0.0.1:5000/entity/60201"}]}

Notes:

- Both endpoints return HTTP 404 if entity/path cannot be found
- The `entity` resource is defined by it's `id`, `name`, `class` and set of arbitrary `properties`. Here I assume that each node in the graph has only only one label which use as entity's `class`. 
- The path response contains a list of entity summaries (not full node) that includes: id, class, name.
- To make the API nice entity/entity summary responses also include the `uri` to the entity (`"uri":"http://127.0.0.1:5000/entity/60201"`)

Assumptions:

- Service is 'public', no AAA needed at this stage

##  ETL Design

Since this is one off ingest a 'low tech' shell script should suffice.
The general workflow is:
	
- use `mysql` to `SELECT ... INTO OUTFILE` to export csv files for nodes and edges
- validate the extract (number of nodes/edges vs number of lines)
- import csv to Neo4j using `neo4j-import`
- validate the import (number of imported nodes/edges vs number of lines)


# Build & Run

## Running the ETL

Pre-requisites:
	
- a running instance of MySQL 5.7+ (with access detail)
- mysql client installed locally
- neo4j community edition (version 3.5.3) installed locally in `$NEO4J_HOME` (with initial password for `neo4j` changed)

Import the SQL dump to the sql database, e.g.:

	curl -v https://[url-masked]/paradise.sql  | mysql -u <USERNAME> -p

Configure the etl script in `$HOME/.paradise.sh`. You can copy the template from `etl/conf/paradise.sh.template`.
Per minimum you need to set `NEO4J_HOME`, `MYSQL_USER`, `MYSQL_PWD`. See comment in `etl/bin/run_etl.sh` for more info.

Run the etl script with:

	./etl/bin/run_etl.sh

This will ingest the data to the neo4j database named `paradise.db`.

Point your neo4j instance to this database setting in `$NEOJ4_HOME/conf/neo4j.conf`:

	dbms.active_database=paradise.db

Restart neo4j.
	
	`$NEOJ4_HOME/bin/neo4j restart

## Building and running REST API

Prerequisites:
	
- sbt version 1.1.4+

To build the project war use:

	sbt package

This will run the unit test produce `target/scala-2.11/paradise-web-service_2.11-0.1.0-SNAPSHOT.war`

### Running locally 

Create a configuration file at `conf/local.conf` with connection details to the neo4j server to use, e.g.:

	neo4j {
	    url = "bolt://localhost:7687"
	    username = "neo4j"
	    password = "neo4j"
	}

Note: (this file is ignored in .gitgnore)

To start the api use:

	sbt tomcat:start tomcat:join

This will start tomcat container runing at port 8080 with the webapp delpoyed at `/api`.

To test use:

	curl -i http://localhost:8080/api/entity/34178939

or

	curl -i http://localhost:8080/api/entity/39041817/shortestPath/39172370


### Running with docker-compose

To is a self contained setup with small anonymyzed extract of the full database suitable for integration testing.

Prerequisites:
	
- docker-compose installed locally

To start with the test database (small anonmyzed extract of the full database) use:

	./dev/run_with_compose.sh up

This starts containers with neo4j (exposing bolt on port 7777) and tomcat (at port 8888)

To test use:

	curl -i http://localhost:8888/api/entity/34178939

or

	curl -i http://localhost:8888/api/entity/39041817/shortestPath/39172370


To connect to neo4j (authentication is off):

	$NEO4J_HOME/cypher-shell -a bolt://localhost:7777


The database extract is in `src/test/neo4j/test.db.tar.gz` can be updated with:

	dev/build_test_db.sh

To stop the container use:
	
	./dev/run_with_compose.sh stop


### Deploying and configuring

The application configuraion is defined  in a `HOCON` formated file.
The current options are:

	// configure neo4j connection
    neo4j {
        url = "bolt://localhost:7687" 	// the url to neo4j bolt connection to use
        username = "neo4j"				// neo4j username
        password = "neo4j"				// neo4h password
    }

The path to the config file is passed to the web application in the `config-file` `initParameter`.

These can be set configured during deployment in a container specific manner. 
E.g. for tomcat in a context descriptor file:

	<Context >
	    <Parameter name="config-file" value="conf/local.conf"/>
	</Context>


### Running integration tests

Integration test are created using python [tavern](https://taverntesting.github.io/) REST tesing framework.
The tests require the `docker-compose` setup to be running

Prerequisites

	- `tavern` and `py.test` installed, .e.g; `pip install tavern[py.test]` 

To run the tests use:

	./dev/run_with_compose.sh up
	# in another terminal
	./dev/run-it.sh
	./dev/run_with_compose.sh stop


# TOOD, Open issues and tasks

- [TODO] Current implementation does not add `uri` element to `entity` responses
- [TODO] Current implementation does not add `class` and `properties` elements to `entity responses`
- [Issue] Neo4j `shortedPath` does not work if the start and end nodes are the same. Currently API fails with code 500. If finding path between the same 
node is required (not sure if it's useful) then the implementation can be extended to use alternative query in this 
case: `MATCH path=(n)-[*..5]-(n) WHERE ID(n)=$ID RETURN path ORDER BY lenght(path) LIMIT 1`. As search for unlimited paths is likely to take a lot of time
the max length should be constrained (here to 5) possibly with a configurable parameter.
- [Issue] Neo4j driver is eagerly checks for connectivity to the server. The web app fails to start if the server is not available (and cannot recover from it). It should start and fail on requests, and recover once the server is available. This seem to work if the database becomes unavailable after the driver is initialized (e.g. fails per requests and recovers).
- [IDEA] To make to output more useful the `path` resource could include the types of relations as well as nodes in the path, 
e.g. `{steps: [relType:"address-of", "from":{ "id"... }, "to":{ ...}}, ...]}`
- [TODO] Add more stringent verification of the ingest by comparing counts of nodes and edges of different typed between mysql and neo4j after ingest
- [TODO] Include more logging. At this stage since the application does little more than bridging REST API to neo4j most of the logging can be done by cross-cutting entry points to different layers (REST layer, persistence layer etc).
- [TODO] Automate running of end to end integration tests on docker-compose setup
- [TODO] Add in build scala based integation tests (see: https://github.com/orrsella/scala-e2e-testing)









