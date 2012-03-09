// Generate a complete graph

importPackage( Packages.javax.swing );
importPackage( Packages.org.cytoscape.app.CyAppAdapter );
importPackage( Packages.org.cytoscape.model.CyNetwork );

var newNetwork = cyAppAdapter.getCyNetworkFactory().createNetwork();
cyAppAdapter.getCyNetworkManager().addNetwork(newNetwork);

var nodes = new Array();

println("Network registered!: ");
