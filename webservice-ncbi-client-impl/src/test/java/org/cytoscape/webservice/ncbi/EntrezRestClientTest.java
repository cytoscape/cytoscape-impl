package org.cytoscape.webservice.ncbi;


import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.webservice.ncbi.rest.EntrezRestClient;
import org.junit.Before;
import org.junit.Test;

public class EntrezRestClientTest {

	
	private EntrezRestClient client;
	
	@Before
	public void setUp() throws Exception {
		NetworkTestSupport support = new NetworkTestSupport();
		client = new EntrezRestClient(support.getNetworkFactory(), null);
	}
	
	@Test
	public void testEntrezRestClientSearch() throws Exception {
//		Set<String> result = client.search("human muscular dystrophy");
//		
//		assertNotNull(result);
//		assertEquals(214, result.size());
	}
	
	@Test
	public void testEntrezRestClientImportNetwork() throws Exception {
		Set<String> result = new HashSet<String>();
		
		// TP53
		result.add("7157");
//		final CyNetwork net = client.importNetwork(result);
//		
//		assertNotNull(net);
//		assertFalse(net.getNodeCount() == 0);
//		assertFalse(net.getEdgeCount() == 0);
	}

}
