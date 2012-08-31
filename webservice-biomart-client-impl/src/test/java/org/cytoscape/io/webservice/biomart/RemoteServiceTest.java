package org.cytoscape.io.webservice.biomart;

import static org.junit.Assert.*;

import java.util.Map;

import javax.swing.plaf.synth.Region;

import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * This is simply for service status testing.
 * Tests are disabled by default, so please enable it only when 
 * you cant to test connection to the remote service.
 *
 */
public class RemoteServiceTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore("Enable only when you want to test remote service.")
	@Test
	public void testConnection() throws Exception {
		final BiomartRestClient biomartRestClient = new BiomartRestClient("http://www.biomart.org/biomart/martservice");
		
		final Map<String, Map<String, String>> region = biomartRestClient.getRegistry();
		
		assertNotNull(region);
		assertTrue(region.size() != 0);
		
		System.out.println(region);
		
		Map<String, String> datasets = biomartRestClient.getAvailableDatasets("ensembl");
		assertNotNull(datasets);
		assertTrue(datasets.size() != 0);
	}

}
