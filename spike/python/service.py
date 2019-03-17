from flask import Flask, url_for, jsonify, abort
from neo4j import GraphDatabase 
import os

app = Flask(__name__)

driver = GraphDatabase.driver("bolt://localhost:7687",
        auth=(os.environ['NEO4J_USERNAME'],os.environ['NEO4J_PASSWORD']))

def node_to_ext(n):              
    return dict(id = n.id, labels = list(n.labels), properties = dict(n))


@app.route('/')
def index():
    return 'Paradise API'


@app.route('/node/<int:node_id>', methods=['GET'])
def get_node(node_id):
    with driver.session() as session:
       result = session.run('MATCH (n) WHERE ID(n)=$node_id RETURN n',node_id=node_id).single()
    if result is None:
        abort(404)
    else:
        return jsonify(node_to_ext(result['n']))
    

