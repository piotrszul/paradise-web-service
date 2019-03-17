End to end (throwawa) spike
===========================

This is to get a quick working end to end solution including ETL and web service to identify possible issues, validate neo4j as the graph database and get idea on REST API design.


## Step 1 - Import sample data to local mysql instance

This seem to work:

	curl -v https://s3-ap-southeast-2.amazonaws.com/stellar-interview/paradise.sql  | mysql -u crypto -p

Created database `paradise` with the expexted schema.

## Step 2 - ETL

Validate the idea of using `mysql` to `SELECT ... INTO OUTFILE` to export csv data from mysql to be imported by `neo4j-import`.

Some relevat resources: 

- http://www.mysqltutorial.org/mysql-export-table-to-csv/
- https://neo4j.com/developer/guide-import-csv/#_super_fast_batch_importer_for_huge_datasets
- https://neo4j.com/docs/operations-manual/current/tutorial/import-tool/



