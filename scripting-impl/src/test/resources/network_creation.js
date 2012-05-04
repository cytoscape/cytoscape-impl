// Generate a complete graph

importPackage(Packages.org.cytoscape.app.CyAppAdapter);
importPackage(Packages.org.cytoscape.model.CyNetwork);

// Extract arguments
var numberOfNodes = args[0];
if(numberOfNodes == null)
	numberOfNodes = 10;

var newNetwork = cyAppAdapter.getCyNetworkFactory().createNetwork();
newNetwork.getRow(newNetwork).set("name", "Complete Graph with " + numberOfNodes + " Nodes");

// Register it to the manager
cyAppAdapter.getCyNetworkManager().addNetwork(newNetwork);

var nodes = new Array();
for( i = 0; i < numberOfNodes; i++) {
	var nodeName = "Node " + i;
	var node = newNetwork.addNode();
	newNetwork.getRow(node).set("name", nodeName);
	nodes[i] = node;
}

var edgeCount = 0;
for( i = 0; i < numberOfNodes; i++) {
	var source = nodes[i];
	for( j = 0; j < numberOfNodes; j++) {
		var target = nodes[j];
		if(newNetwork.containsEdge(source, target) == false && newNetwork.containsEdge(target, source) == false && j != i) {
			var edge = newNetwork.addEdge(source, target, true);
			newNetwork.getRow(edge).set("name", "Edge " + edgeCount++);
			newNetwork.getRow(edge).set("interaction", "interacts_with");
		}
	}
}