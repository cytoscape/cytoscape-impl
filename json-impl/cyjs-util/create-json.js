#!/usr/bin/env node

// Utility to generate Cytoscape.js JSON

var fs = require('fs')
var http = require('http');
var cytoscape = require('cytoscape');

//var graph1 = JSON.parse(fs.readFileSync('data/sample1.json', 'utf8'));

var emptyGraph = {
	data: {
		name: 'Test Network 1'
	},
	elements: {
		nodes: [],
		edges: []
	}
};

for (var i = 0; i < 100; i++) {
	emptyGraph.elements.nodes[i] = {
		data: {
			id: i.toString()
		}
	}
};

for (var i = 0; i < 100; i++) {
	emptyGraph.elements.edges[i] = {
		data: {
			id: 'e' + i.toString(),
			source: i.toString(),
			target: (Math.floor( Math.random()*100 )).toString()
		}
	}
};

var cy = cytoscape({
	elements: emptyGraph.elements,
	layout: {
    	name: 'circle',
    	radius: 1000
  	}
});

var sample1 = cy.elements().jsons();
console.log(JSON.stringify(sample1));
process.exit(0);