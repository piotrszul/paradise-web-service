End to end (throwawa) spike
===========================

This is to get a quick working end to end solution including ETL and web service to identify possible issues, validate neo4j as the graph database and get idea on REST API design.


## Step 1 - Import sample data to local mysql instance

This seem to work:

	curl -v https://s3-ap-southeast-2.amazonaws.com/stellar-interview/paradise.sql  | mysql -u crypto -p

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
TODO:

- consider typing the properties
- would be more flexible to provide neo4j import headers externally so that the extract does need to change if the import schema does (e.g property types)


Some releant resources: 

- http://www.mysqltutorial.org/mysql-export-table-to-csv/
- https://neo4j.com/developer/guide-import-csv/#_super_fast_batch_importer_for_huge_datasets
- https://neo4j.com/docs/operations-manual/current/tutorial/import-tool/



