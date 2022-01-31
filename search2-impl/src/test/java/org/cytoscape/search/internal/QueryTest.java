package org.cytoscape.search.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.search.internal.index.IndexCreateTask;
import org.cytoscape.search.internal.index.IndexCreateTask.Result;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.search.internal.search.SearchResults;
import org.cytoscape.search.internal.search.SearchTask;
import org.cytoscape.work.TaskMonitor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;


public class QueryTest {
	
	@Rule public TestRule logSilenceRule = new LogSilenceRule(); 
	
	private final NetworkTestSupport networkTestSupport = new NetworkTestSupport();
	
	
	public CyNetwork createTestNetwork() {
		CyNetwork network = networkTestSupport.getNetwork();
		
		CyTable nodeTable = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		nodeTable.createColumn("TestID", Integer.class, false); // We can't chose the SUID value so use this instead
		nodeTable.createColumn("COMMON", String.class, false);
		nodeTable.createColumn("degree.layout", Integer.class, false);
		nodeTable.createColumn("galFiltered::gal1RGexp", Double.class, false); // Use a namespace
		
		CyTable edgeTable = network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
		edgeTable.createColumn("TestID", Integer.class, false); // We can't chose the SUID value so use this instead
		edgeTable.createColumn("EdgeBetweenness", Double.class, false);
		
		// TODO Add more columns, one with more complex text like lorem ipsum, and one List column
		addNode(network, 1,  "YIL015W", "BAR1", 2, -0.622);
		addNode(network, 2,  "YJL159W", "HSP150", 2, -0.357);
		addNode(network, 3,  "YKR097W", "PCK1", 2, 1.289);
		addNode(network, 4,  "YPR119W", "CLB2", 2, -0.234);
		addNode(network, 5,  "YGR108W", "CLB1", 3, -0.25);
		addNode(network, 6,  "YAL040C", "CLN3", 2, -0.027);
		addNode(network, 7,  "YGL008C", "PMA1", 2, -0.352);
		addNode(network, 8,  "YDR461W", "MFA1", 3, -0.659);
		addNode(network, 9,  "YNL145W", "MFA2", 3, -0.764);
		addNode(network, 10, "YJL157C", "FAR1", 4, -0.158);
		addNode(network, 11, "YFL026W", "STE2", 3, -0.653);
		addNode(network, 12, "YJL194W", "CDC6", 2, 0.018);
		addNode(network, 13, "YCR084C", "TUP1", 2, 0.044);
		addNode(network, 14, "YHR084W", "STE12", 4, -0.109);
		addNode(network, 15, "YBR112C", "SSN6", 1, 0.108);
		addNode(network, 16, "YCL067C", "ALPHA2", 6, 0.169);
		addNode(network, 17, "YER111C", "SWI4", 2, 0.195);
		addNode(network, 18, "YDR146C", "SWI5", 2, -0.19);
		addNode(network, 19, "YPR113W", "PIS1", 1, -0.495);
		addNode(network, 20, "YMR043W", "MCM1", 18, -0.183);
		addNode(network, 21, "YBR160W", "CDC28", 3, -0.016);
		
		addEdge(network, 1,  "YNL145W", "YHR084W", "pd", 4.83333333);
		addEdge(network, 2,  "YJL157C", "YAL040C", "pp", 115.0);
		addEdge(network, 3,  "YER111C", "YMR043W", "pd", 2361.73260073);
		addEdge(network, 4,  "YBR160W", "YMR043W", "pd", 940.0);
		addEdge(network, 5,  "YBR160W", "YGR108W", "pp", 48.0);
		addEdge(network, 6,  "YDR146C", "YMR043W", "pd", 19179.59362859);
		addEdge(network, 7,  "YPR119W", "YMR043W", "pd", 18360.0);
		addEdge(network, 8,  "YJL194W", "YMR043W", "pd", 988.0);
		addEdge(network, 9,  "YPR113W", "YMR043W", "pd", 496.0);
		addEdge(network, 10, "YGL008C", "YMR043W", "pd", 988.0);
		addEdge(network, 11, "YHR084W", "YMR043W", "pp", 485.5);
		addEdge(network, 12, "YHR084W", "YDR461W", "pd", 4.83333333);
		addEdge(network, 13, "YHR084W", "YFL026W", "pd", 4.83333333);
		addEdge(network, 14, "YAL040C", "YMR043W", "pd", 381.0);
		addEdge(network, 15, "YMR043W", "YIL015W", "pd", 487.0);
		addEdge(network, 16, "YCR084C", "YBR112C", "pp", 496.0);
		addEdge(network, 17, "YMR043W", "YJL159W", "pd", 988.0);
		addEdge(network, 18, "YCR084C", "YCL067C", "pp", 988.0);
		addEdge(network, 19, "YCL067C", "YIL015W", "pd", 9.0);
		addEdge(network, 20, "YMR043W", "YKR097W", "pd", 3136.17527473);
		addEdge(network, 21, "YMR043W", "YGR108W", "pd", 4058.08455433);
		addEdge(network, 22, "YCL067C", "YMR043W", "pp", 1447.5);
		addEdge(network, 23, "YCL067C", "YDR461W", "pd", 9.83333333);
		addEdge(network, 24, "YMR043W", "YDR461W", "pd", 484.33333333);
		addEdge(network, 25, "YMR043W", "YNL145W", "pd", 484.33333333);
		addEdge(network, 26, "YCL067C", "YFL026W", "pd", 9.83333333);
		addEdge(network, 27, "YMR043W", "YJL157C", "pd", 9243.86127206);
		addEdge(network, 28, "YMR043W", "YFL026W", "pd", 484.33333333);
		addEdge(network, 29, "YNL145W", "YCL067C", "pd", 9.83333333);
		
		// sanity check
		assertEquals(21, network.getNodeCount());
		assertEquals(29, network.getEdgeCount());
		
		return network;
	}
	
	private static void addNode(CyNetwork network, int testId, String sharedName, String common, int degree, double galexp) {
		CyNode node = network.addNode();
		CyRow row = network.getRow(node);
		row.set(CyRootNetwork.SHARED_NAME, sharedName);
		row.set("TestID", testId);
		row.set("COMMON", common);
		row.set("degree.layout", degree);
		row.set("galFiltered::gal1RGexp", galexp);
	}
	
	private static void addEdge(CyNetwork network, int testId, String sourceName, String targetName, String interaction, double edgeBetweenness) {
		CyTable nodeTable = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		Long sourceSuid = nodeTable.getMatchingKeys(CyRootNetwork.SHARED_NAME, sourceName, Long.class).iterator().next();
		Long targetSuid = nodeTable.getMatchingKeys(CyRootNetwork.SHARED_NAME, targetName, Long.class).iterator().next();
		CyNode source = network.getNode(sourceSuid);
		CyNode target = network.getNode(targetSuid);
		CyEdge edge = network.addEdge(source, target, false);
		CyRow row = network.getRow(edge);
		row.set(CyRootNetwork.SHARED_NAME, sourceName + " (" + interaction + ") " + targetName); // TODO this might be automatic
		row.set(CyRootNetwork.SHARED_INTERACTION, interaction);
		row.set("TestID", testId);
		row.set("EdgeBetweenness", edgeBetweenness);
	}
	
	
	private static void buildIndex(Path indexPath, CyNetwork network) {
		IndexCreateTask task = new IndexCreateTask(indexPath, network);
		Result result = task.call();
		assertTrue(result.sucesss());
	}
	
	private static SearchResults queryIndex(SearchManager searchManager, CyNetwork network, String query) {
		SearchTask searchTask = new SearchTask(searchManager, network, query);
		return searchTask.runQuery(mock(TaskMonitor.class));
	}
	
	private static void assertNodeHits(SearchResults results, CyNetwork network, int ... ids) {
		assertEquals(ids.length, results.getNodeHitCount());
		CyTable nodeTable = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		List<String> nodeHits = results.getNodeHits();
		for(int id : ids) {
			Long suid = nodeTable.getMatchingKeys("TestID", id, Long.class).iterator().next();
			
			System.out.println("suid: " + suid);
			assertTrue(nodeHits.contains(String.valueOf(suid)));
		}
	}
	
	private static void assertNodeEmpty(SearchResults results) {
		assertEquals(0, results.getNodeHitCount());
	}
	
	private static void assertEdgeHits(SearchResults results, CyNetwork network, int ... ids) {
		assertEquals(ids.length, results.getEdgeHitCount());
		CyTable edgeTable = network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
		List<String> edgeHits = results.getEdgeHits();
		for(int id : ids) {
			Long suid = edgeTable.getMatchingKeys("TestID", id, Long.class).iterator().next();
			assertTrue(edgeHits.contains(String.valueOf(suid)));
		}
	}
	
	private static void assertEdgeEmpty(SearchResults results) {
		assertEquals(0, results.getEdgeHitCount());
	}
	
	
	@Test
	public void testBasicQueries() throws Exception {
		CyNetwork net = createTestNetwork();

		Path baseDir = Files.createTempDirectory("search2_impl_");
		baseDir.toFile().deleteOnExit();
		SearchManager manager = new SearchManager(baseDir);
		Path indexPath = manager.getIndexPath(net);
		
		buildIndex(indexPath, net);
		
		{
			SearchResults results = queryIndex(manager, net, "BAR1");
			assertNodeHits(results, net, 1);
			assertEdgeEmpty(results);
		}
		{
			SearchResults results = queryIndex(manager, net, "bar1"); // Case insensitive
			assertNodeHits(results, net, 1);
			assertEdgeEmpty(results);
		}
		{
			SearchResults results = queryIndex(manager, net, "YMR043W");
			assertNodeHits(results, net, 20);
			assertEdgeHits(results, net, 3, 4, 6, 7, 8, 9, 10, 11, 14, 15, 17, 20, 21, 22, 24, 25, 27, 28);
		}
		{
			SearchResults results = queryIndex(manager, net, "ymR043W"); // Case insensitive
			assertNodeHits(results, net, 20);
			assertEdgeHits(results, net, 3, 4, 6, 7, 8, 9, 10, 11, 14, 15, 17, 20, 21, 22, 24, 25, 27, 28);
		}
		{
			SearchResults results = queryIndex(manager, net, "BAR1 YMR043W");
			assertNodeHits(results, net, 1, 20);
			assertEdgeHits(results, net, 3, 4, 6, 7, 8, 9, 10, 11, 14, 15, 17, 20, 21, 22, 24, 25, 27, 28);
		}
		{
			SearchResults results = queryIndex(manager, net, "");
			assertNodeEmpty(results);
			assertEdgeEmpty(results);
		}
		{
			SearchResults results = queryIndex(manager, net, "blah");
			assertNodeEmpty(results);
			assertEdgeEmpty(results);
		}

	}
	
	
}