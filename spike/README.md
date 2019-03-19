End to end (throwawa) spike
===========================

This is to get a quick working end to end solution including ETL and web service to identify possible issues, validate neo4j as the graph database and get idea on REST API design.


## Step 1 - Import sample data to local mysql instance

This seem to work:

	curl -v https://[url-masked]/paradise.sql  | mysql -u crypto -p

Created database `paradise` with the expexted schema.

## Step 2 - ETL

Validate the idea of using `mysql` to `SELECT ... INTO OUTFILE` to export csv data from mysql to be imported by `neo4j-import`.

The sql database seem to be an export from neo4j, so just trying to recreate the same schema after import seem to be reasonable.
The only question is what to use as the source of label(s) for graph nodes. One way would be to use the source table (e.g. nodes.officer -> LABEL:officer).
Another is to use the `labels(n)` field. Since all of them are JSON_ARRAYS od lenght 1 it should be easy to extrect that in SQL, as this is potentially mode flexible.
Another consideration is uniquness of the IDs. In out case all the nodes.* have unique ids so there is not need to use namespaces in neo4j.

With these assumptions should be able to extrect all nodes in one format and import with this header:

	node_id:ID, :LABEL, valid_until, name ... 

And for relations(edges)

	:START_ID,:END_ID,:TYPE,idx

All nodes and edges imported in a few seconds so the approach works.

To run:

	./bin/extract_mysql.sh
	./bin/import_neo4j.sh


TODO:

- consider typing the properties
- would be more flexible to provide neo4j import headers externally so that the extract does need to change if the import schema does (e.g property types)


Some releant resources: 

- http://www.mysqltutorial.org/mysql-export-table-to-csv/
- https://neo4j.com/developer/guide-import-csv/#_super_fast_batch_importer_for_huge_datasets
- https://neo4j.com/docs/operations-manual/current/tutorial/import-tool/

## Step 3 - Check the shortestPath function in `cypher`

In principle the following query should return the shortest path (the directed version):

	    MATCH path=shortestPath((b)-[*]->(e))
        WHERE ID(b)=60201 AND ID(e)=9
        RETURN path;


or (undirected version):

	    MATCH path=shortestPath((b)-[*]-(e))
        WHERE ID(b)=60201 AND ID(e)=9
        RETURN path;

Tested through cyper-shell seem to work as expexted and under 1s (!30ms):

	./bin/run_cypher.sh < cypher/undirected_shortest_path.cypher


## Step 4 - Spike end to end REST service using Neo4j driver 

Because it should be fast I am going to use python here, with flask as webservice framework.

As the first setp just test basic connectivity to get a node:

	GET /node/:node_id

To run:
	
	./bin/run_api.sh

And to test get node id = 9:

	curl -i http://127.0.0.1:5000/node/9

and get shorest path from node 9 to node 60201

	curl -i http://127.0.0.1:5000/node/9/shortestPath/60201

So the general REST API deisgn will be:


	GET /node/:node_id  -> JSON(node)

	eg: {"id":9,"labels":["Address"],"properties":{"address":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","closed_date":"","company_type":"","countries":"","country_codes":"","ibcRUC":"","incorporation_date":"","jurisdiction":"","jurisdiction_description":"","name":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","node_id":"59217552","note":"","service_provider":"","sourceID":"Paradise Papers - Malta corporate registry","status":"","type":"","valid_until":"Malta corporate registry data is current through 2016"}}


	GET /node/:node_id/shorestPath/:to_node_id -> JSON(path)

	e.g: {"shortestPath":[{"id":9,"labels":["Address"],"name":"40, VILLA FAIRHOLME, SIR AUGUSTUS BARTOLO STREET,","uri":"http://127.0.0.1:5000/node/9"},{"id":59242,"labels":["Entity"],"name":"ALA INT. LIMITED","uri":"http://127.0.0.1:5000/node/59242"},{"id":60201,"labels":["Entity"],"name":"Euroyacht Limited","uri":"http://127.0.0.1:5000/node/60201"}]}


Notes:

- The path response contains a list of node summaries (not full node) that includes: id, labels, name.
- To make the API nice node/node summary responses also include the `uri` to the node (`"uri":"http://127.0.0.1:5000/node/60201"`)


TODO:
	
-  in the node `id` and `node_id` are different - need to investigate ...


Some related resources:

- FLASK: https://blog.miguelgrinberg.com/post/designing-a-restful-api-with-python-and-flask
- FLASK: http://flask.pocoo.org/docs/1.0/tutorial/tests/
- Neo4j python driver:  https://github.com/neo4j/neo4j-python-driver


# Step 5 - Nodes ids.

	ID(p) = 9 >>> node_id = "59217552" 
	ID(p) = 60201 >>> node_id = "82000988"

The import worked OK, but the internal ids ID(p) are different than p.node_id.
The alternative query is possible to use node_id instead:

	MATCH (b {node_id:'59217552'}),(e {node_id:'82000988'}),path=shortestPath((b)-[*]-(e)) RETURN path;

But it requires full table scan to find the nodes (while the ID(p) does not).
Indexing on `node_id` may not help here eithe becasue indexes seem to be only used if a specific lable is requested.

Nevertheless for this size of data even the queries with full scan run under ~50ms so both options are fine. 

Another option is to use `--id-type=ACTUAL` option with import tools. This should solve the issue but the ids in extract need
to be sorted in increasing order. (But can be done). This may be the best ... (if we are talking about read-only view).

TODO:

- ask what it should be.


## Web Application configuration

See: https://stackoverflow.com/questions/13956651/externalizing-tomcat-webapp-config-from-war-file

In code:
	
	getServletContext().getInitParameter(name)) 
	
Tomcat setup (`$CATALINA_BASE/conf/[enginename]/[hostname]/$APP.xml`):

	<Context docBase="${basedir}/src/main/webapp" reloadable="true">
    		<Parameter name="min" value="dev"/>
	</Context>
	