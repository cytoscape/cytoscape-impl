package org.cytoscape.task.internal.quickstart.subnetworkbuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NCBISearchClientTest {

	private NCBISearchClient client;

	@Before
	public void setUp() throws Exception {
		client = new NCBISearchClient();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSearchClient() throws Exception {

		final String disease = "Rheumatoid Arthritis";
		final String go1 = "neurogenesis";
		final String go2 = "Rho GTPase activity";

		// Enable this for client test.
		// final Set<String> result1 = client.search(disease, go1);
		//
		// assertNotNull(result1);
		// assertEquals(322, result1.size());
		//
		// final Set<String> result2 = client.search(disease, go1 + ", " + go2);
		//
		// assertNotNull(result2);
		// assertEquals(391, result2.size());
	}

	@Test
	public void testIDConversion() throws Exception {

		final String symbols = "yap1 yap2 yap3";
		final String symbols2 = "YGL122C YGL097W YOR204W YPR080W YBR118W YGL097W YDR429C YFL017C " +
				"YAL003W YAL003W YGL044C YGR014W YGL229C YGL229C YOL123W YJL030W YJL013C YIL061C " +
				"YBR112C YCL067C YOR167C YNR050C YNL050C YEL015W YOR167C YLR264W YNR053C YDL013W " +
				"YAL038W YCR012W YGR254W YHR174W YIL133C YLR044C YOL120C YNL301C YCL030C YDR171W " +
				"YBR093C YER074W YIL069C YAL038W YOL127W YDR050C YOL086C YER143W";

		// Enable this to test client.
		
//		final Set<String> result1 = client.convert(symbols, true);
//		final Set<String> result2= client.convert(symbols2, true);
//
//		System.out.println("Value = " + result1.size());
//		System.out.println("Value = " + result2.size());
//
//		assertNotNull(result1);

	}

}
