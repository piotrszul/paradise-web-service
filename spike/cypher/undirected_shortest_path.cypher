MATCH path=shortestPath((b)-[*]-(e))
WHERE ID(b)=60201 AND ID(e)=9
RETURN path;
