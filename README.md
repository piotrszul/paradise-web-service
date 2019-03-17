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


Cypher vs gremlin: There is a 'germlin' connector to neo4j but for simplicity I am going to use cypher (as there are not requiremnts for make the API graph database agnostic)

I will validate these choise with a  [spike](spike/README.md)


## Rest API design

	GET /node/:node_id  -> JSON(node)

	eg: {"id":9,"labels":["Address"],"properties":{"address":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","closed_date":"","company_type":"","countries":"","country_codes":"","ibcRUC":"","incorporation_date":"","jurisdiction":"","jurisdiction_description":"","name":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","node_id":"59217552","note":"","service_provider":"","sourceID":"Paradise Papers - Malta corporate registry","status":"","type":"","valid_until":"Malta corporate registry data is current through 2016"}}


	GET /node/:node_id/shortestPath/:to_node_id -> JSON(path)

	e.g: {"shortestPath":[{"id":9,"labels":["Address"],"name":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","uri":"http://127.0.0.1:5000/node/9"},{"id":59242,"labels":["Entity"],"name":"ALA INT. LIMITED","uri":"http://127.0.0.1:5000/node/59242"},{"id":60201,"labels":["Entity"],"name":"Euroyacht Limited","uri":"http://127.0.0.1:5000/node/60201"}]}


Notes:

- The path response contains a list of node summaries (not full node) that includes: id, labels, name.
- To make the API nice node/node summary responses also include the `uri` to the node (`"uri":"http://127.0.0.1:5000/node/60201"`)





## Build & Run ##


```sh
$ cd paradise-web-service
$ sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.
