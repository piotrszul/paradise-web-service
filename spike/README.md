End to end (throwawa) spike
===========================

This is to get a quick working end to end solution including ETL and web service to identify possible issues, validate neo4j as the graph database and get idea on REST API design.


## Step 1 - Import sample data to local mysql intance

This seem to work:

	curl -v https://s3-ap-southeast-2.amazonaws.com/stellar-interview/paradise.sql  | mysql -u crypto -p

Created database `paradise` with the expexted schema.

