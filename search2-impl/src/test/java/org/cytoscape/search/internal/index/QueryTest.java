package org.cytoscape.search.internal.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.search.internal.LogSilenceRule;
import org.cytoscape.search.internal.search.NetworkSearchTask;
import org.cytoscape.search.internal.search.SearchResults;
import org.cytoscape.search.internal.search.SearchResults.Status;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;


public class QueryTest {

	@Rule public TestRule logSilenceRule = new LogSilenceRule(); 
	
	private static final String TEST_ID = "TestID";
	
	private static final String NODE_COMMON = "namespace::COMMON";
	private static final String NODE_DEGREE_LAYOUT = "degree.layout";
	private static final String NODE_GAL_FILTERED_GAL1R_GEXP = "gal1RGexp";
	private static final String NODE_DUMMY_TEXT = "dummyText";
	
	private static final String EDGE_EDGEBETWEENNESS = "EdgeBetweenness";
	
	private CyNetwork network;
	private SearchManager searchManager;
	
	
	
	public static CyNetwork createTestNetwork() {
		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetwork network = networkTestSupport.getNetwork();
		
		CyTable nodeTable = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		nodeTable.createColumn(TEST_ID, Integer.class, false); // We can't chose the SUID value so use this instead
		nodeTable.createColumn(NODE_COMMON, String.class, false);
		nodeTable.createColumn(NODE_DEGREE_LAYOUT, Integer.class, false);
		nodeTable.createColumn(NODE_GAL_FILTERED_GAL1R_GEXP, Double.class, false); // Use a namespace
		nodeTable.createColumn(NODE_DUMMY_TEXT, String.class, false); // Use a namespace
		
		CyTable edgeTable = network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
		edgeTable.createColumn(TEST_ID, Integer.class, false); // We can't chose the SUID value so use this instead
		edgeTable.createColumn(EDGE_EDGEBETWEENNESS, Double.class, false);
		
		addNode(network, 1,  "YIL015W", "BAR1",   2, -0.622, "Far far away, behind the word mountains, far from the countries");
		addNode(network, 2,  "YJL159W", "HSP150", 2, -0.357, "Vokalia and Consonantia, there live the blind texts. Separated they");
		addNode(network, 3,  "YKR097W", "PCK1",   2,  1.289, "live in Bookmarksgrove right at the coast of the Semantics, a large");
		addNode(network, 4,  "YPR119W", "CLB2",   2, -0.234, "language ocean. A small river named Duden flows by their place and");
		addNode(network, 5,  "YGR108W", "CLB1",   3,  -0.25, "supplies it with the necessary regelialia. It is a paradisematic");
		addNode(network, 6,  "YAL040C", "CLN3",   2, -0.027, "country, in which roasted parts of sentences fly into your mouth.");
		addNode(network, 7,  "YGL008C", "PMA1",   2, -0.352, "Even the all-powerful Pointing has no control about the blind texts it");
		addNode(network, 8,  "YDR461W", "MFA1",   3, -0.659, "is an almost unorthographic life One day however a small line of");
		addNode(network, 9,  "YNL145W", "MFA2",   3, -0.764, "blind text by the name of Lorem Ipsum decided to leave for the far");
		addNode(network, 10, "YJL157C", "FAR1",   4, -0.158, "World of Grammar. The Big Oxmox advised her not to do so, because");
		addNode(network, 11, "YFL026W", "STE2",   3, -0.653, "there were thousands of bad Commas, wild Question Marks and");
		addNode(network, 12, "YJL194W", "CDC6",   2,  0.018, "devious Semikoli, but the Little Blind Text didn’t listen. She packed");
		addNode(network, 13, "YCR084C", "TUP1",   2,  0.044, "her seven versalia, put her initial into the belt and made herself on");
		addNode(network, 14, "YHR084W", "STE12",  4, -0.109, "the way. When she reached the first hills of the Italic Mountains, she");
		addNode(network, 15, "YBR112C", "SSN6",   1,  0.108, "had a last view back on the skyline of her hometown");
		addNode(network, 16, "YCL067C",  null,    6,  0.169, "Bookmarksgrove, the headline of Alphabet Village and the subline of");
		addNode(network, 17, "YER111C", "SWI4",   2,  0.195, "her own road, the Line Lane. Pityful a rethoric question ran over her");
		addNode(network, 18, "YDR146C", "SWI5",   2,  -0.19, "cheek, then she continued her way. On her way she met a copy. The");
		addNode(network, 19, "YPR113W", "PIS1",   1, -0.495, "copy warned the Little Blind Text, that where it came from it would");
		addNode(network, 20, "YMR043W", "MCM1",  99, -0.183, "have been rewritten a thousand times and everything that was left");
		addNode(network, 21, "YBR160W", "CDC28",  3, -0.016, "from its origin would be the word \"and\" and the Little Blind Text");
		
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
	
	private static Long addNode(CyNetwork network, int testId, String sharedName, String common, int degree, double galexp, String dummyText) {
		CyNode node = network.addNode();
		CyRow row = network.getRow(node);
		row.set(CyRootNetwork.SHARED_NAME, sharedName);
		row.set(TEST_ID, testId);
		row.set(NODE_COMMON, common);
		row.set(NODE_DEGREE_LAYOUT, degree);
		row.set(NODE_GAL_FILTERED_GAL1R_GEXP, galexp);
		row.set(NODE_DUMMY_TEXT, dummyText);
		
		return node.getSUID();
	}
	
	private static Long addEdge(CyNetwork network, int testId, String sourceName, String targetName, String interaction, double edgeBetweenness) {
		CyTable nodeTable = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		Long sourceSuid = nodeTable.getMatchingKeys(CyRootNetwork.SHARED_NAME, sourceName, Long.class).iterator().next();
		Long targetSuid = nodeTable.getMatchingKeys(CyRootNetwork.SHARED_NAME, targetName, Long.class).iterator().next();
		CyNode source = network.getNode(sourceSuid);
		CyNode target = network.getNode(targetSuid);
		CyEdge edge = network.addEdge(source, target, false);
		CyRow row = network.getRow(edge);
		row.set(CyRootNetwork.SHARED_NAME, sourceName + " (" + interaction + ") " + targetName); // TODO this might be automatic
		row.set(CyRootNetwork.SHARED_INTERACTION, interaction);
		row.set(TEST_ID, testId);
		row.set(EDGE_EDGEBETWEENNESS, edgeBetweenness);
		return edge.getSUID();
	}
	
	private static Long getSUID(CyTable table, int testID) {
		return table.getMatchingKeys(TEST_ID, testID, Long.class).iterator().next();
	}
	
	
	@Before
	public void initializeTestNetwork() throws Exception {
		// Each test network will have a different SUID
		network = createTestNetwork();
		
		Path baseDir = Files.createTempDirectory("search2_impl_");
		baseDir.toFile().deleteOnExit();
		
		var registrar = mock(CyServiceRegistrar.class);
		searchManager = new SearchManager(registrar, baseDir);
		
		var future1 = searchManager.addTable(network.getDefaultNodeTable(), TableType.NODE);
		var future2 = searchManager.addTable(network.getDefaultEdgeTable(), TableType.EDGE);
		
		future1.get(); // wait for network to be indexed
		future2.get();
	}
	
	private SearchResults queryIndex(String query) {
		NetworkSearchTask searchTask = new NetworkSearchTask(searchManager, network, query);
		var results = searchTask.runQuery(mock(TaskMonitor.class));
		assertEquals("Error running search", Status.SUCCESS, results.getStatus());
		return results;
	}
	
	private void assertNodeHits(SearchResults results, int ... ids) {
		assertEquals("wrong number of hits", ids.length, results.getNodeHitCount());
		if(ids.length == 0)
			return;
		CyTable nodeTable = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		List<String> nodeHits = results.getNodeHits();
		for(int id : ids) {
			Long suid = nodeTable.getMatchingKeys(TEST_ID, id, Long.class).iterator().next();
			assertTrue("id " + id + " not in query results", nodeHits.contains(String.valueOf(suid)));
		}
	}
	
	private void assertEdgeHits(SearchResults results, int ... ids) {
		assertEquals("wrong number of hits", ids.length, results.getEdgeHitCount());
		if(ids.length == 0)
			return;
		CyTable edgeTable = network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
		List<String> edgeHits = results.getEdgeHits();
		for(int id : ids) {
			Long suid = edgeTable.getMatchingKeys(TEST_ID, id, Long.class).iterator().next();
			assertTrue("id " + id + " not in query results", edgeHits.contains(String.valueOf(suid)));
		}
	}
	
	
	@Test
	public void testBasicQueries() {
		SearchResults results;
		
		results = queryIndex("BAR1");
		assertNodeHits(results, 1);
		assertEdgeHits(results);
		
		results = queryIndex("bar1"); // Case insensitive
		assertNodeHits(results, 1);
		assertEdgeHits(results);

		results = queryIndex("YMR043W");
		assertNodeHits(results, 20);
		assertEdgeHits(results, 3, 4, 6, 7, 8, 9, 10, 11, 14, 15, 17, 20, 21, 22, 24, 25, 27, 28);

		results = queryIndex("ymR043W"); // Case insensitive
		assertNodeHits(results, 20);
		assertEdgeHits(results, 3, 4, 6, 7, 8, 9, 10, 11, 14, 15, 17, 20, 21, 22, 24, 25, 27, 28);

		results = queryIndex("BAR1 YMR043W"); // two terms
		assertNodeHits(results, 1, 20);
		assertEdgeHits(results, 3, 4, 6, 7, 8, 9, 10, 11, 14, 15, 17, 20, 21, 22, 24, 25, 27, 28);

		results = queryIndex("");
		assertNodeHits(results);
		assertEdgeHits(results);

		results = queryIndex("blah");
		assertNodeHits(results);
		assertEdgeHits(results);
	}
	
	
	@Test
	public void testCaseInsensitiveFieldNames() {
		SearchResults results;
		
		results = queryIndex("dummyText:blind");
		assertNodeHits(results, 2, 7, 9, 12, 19, 21);
		
		results = queryIndex("dummytext:blind");
		assertNodeHits(results, 2, 7, 9, 12, 19, 21);
		
		results = queryIndex("DuMmYtExT:blind");
		assertNodeHits(results, 2, 7, 9, 12, 19, 21);
		
		results = queryIndex("edgebetweenness:9");
		assertEdgeHits(results, 19);
		
		results = queryIndex("EDGEBETWEENNESS:9");
		assertEdgeHits(results, 19);
	}
	
	
	@Test
	public void testWildcardQueries() {
		SearchResults results;
		
		results = queryIndex("BAR?");
		assertNodeHits(results, 1);
		assertEdgeHits(results);
		
		results = queryIndex("CLB?");
		assertNodeHits(results, 4, 5);
		assertEdgeHits(results);
		
		results = queryIndex("C???");
		assertNodeHits(results, 4, 5, 6, 12, 19); // but not CDC28 because that has 5 chars
		assertEdgeHits(results);
		
		results = queryIndex("BAR? C???");
		assertNodeHits(results, 1, 4, 5, 6, 12, 19);
		assertEdgeHits(results);
		
		results = queryIndex("YC*");
		assertNodeHits(results, 13, 16);
		assertEdgeHits(results, 16, 18, 19, 22, 23, 26, 29);
		
		results = queryIndex("YC* BAR? C???");
		assertNodeHits(results, 1, 4, 5, 6, 12, 13, 16, 19);
		assertEdgeHits(results, 16, 18, 19, 22, 23, 26, 29);
	}
	
	
	@Test
	public void testUpdateRows() throws Exception {
		SearchResults results;
		
		results = queryIndex("foo bazinga baz");
		assertNodeHits(results);
		
		var nodeTable = network.getDefaultNodeTable();
		
		Long nodeSuid1  = getSUID(nodeTable, 1);
		Long nodeSuid9  = getSUID(nodeTable, 9);
		Long nodeSuid15 = getSUID(nodeTable, 15);
		
		nodeTable.getRow(nodeSuid1).set(NODE_COMMON, "foo");
		nodeTable.getRow(nodeSuid9).set(NODE_COMMON, "bazinga");
		nodeTable.getRow(nodeSuid15).set(NODE_COMMON, "baz");
		
		var keys = Set.of(nodeSuid1, nodeSuid9, nodeSuid15);
		searchManager.updateRows(nodeTable, keys).get();
		
		results = queryIndex("foo");
		assertNodeHits(results, 1);
		
		results = queryIndex("bazinga");
		assertNodeHits(results, 9);
		
		results = queryIndex("baz");
		assertNodeHits(results, 15);
		
		results = queryIndex("SSN6"); // This was replaced with 'baz', should not have results anymore
		assertNodeHits(results);
		
		results = queryIndex("YMR043W"); // Sanity test, querying a node that wasn't changed should still work
		assertNodeHits(results, 20);
	}
	
	
	@Test
	public void testDeleteRows() throws Exception {
		var nodeTable = network.getDefaultNodeTable();
		assertEquals(21, searchManager.getDocumentCount(nodeTable));
		
		SearchResults results = queryIndex("BAR1 MFA2 SSN6");
		assertNodeHits(results, 1, 9, 15);
		
		Long nodeSuid1  = getSUID(nodeTable, 1);
		Long nodeSuid9  = getSUID(nodeTable, 9);
		Long nodeSuid15 = getSUID(nodeTable, 15);
		
		CyNode node1  = network.getNode(nodeSuid1);
		CyNode node9  = network.getNode(nodeSuid9);
		CyNode node15 = network.getNode(nodeSuid15);
		
		network.removeNodes(List.of(node1, node9, node15));
		
		var keys = Set.of(nodeSuid1, nodeSuid9, nodeSuid15);
		searchManager.updateRows(nodeTable, keys).get();
		
		results = queryIndex("BAR1 MFA2 SSN6");
		assertNodeHits(results);
		
		assertEquals(18, searchManager.getDocumentCount(nodeTable));
	}
	

	@Test
	public void testAddRows() throws Exception {
		var nodeTable = network.getDefaultNodeTable();
		assertEquals(21, searchManager.getDocumentCount(nodeTable));
		
		Long nodeSuid90 = addNode(network, 90, "QWERTY", "frodo", 99, -0.999, "blah");
		Long nodeSuid91 = addNode(network, 91, "ASDFGH", "sam", 99, -0.999, "blah");
		Long nodeSuid92 = addNode(network, 92, "ZXCVBN", "gandalf", 99, -0.999, "blah");
		
		var keys = Set.of(nodeSuid90, nodeSuid91, nodeSuid92);
		
		searchManager.updateRows(nodeTable, keys).get();
		
		assertEquals(24, searchManager.getDocumentCount(nodeTable));
		
		SearchResults results = queryIndex("QWERTY ASDFGH ZXCVBN");
		assertNodeHits(results, 90, 91, 92);
	}
	
	
	@Test
	public void testDeleteColumn() throws Exception {
		var nodeTable = network.getDefaultNodeTable();

		Long nodeSuid1  = getSUID(nodeTable, 1);
		Long nodeSuid9  = getSUID(nodeTable, 9);
		Long nodeSuid15 = getSUID(nodeTable, 15);
		
		SearchResults results = queryIndex("BAR1 MFA2 SSN6");
		assertNodeHits(results, 1, 9, 15);
		
		nodeTable.deleteColumn(NODE_COMMON);
		searchManager.reindexTable(nodeTable).get();
		
		results = queryIndex("BAR1 MFA2 SSN6");
		assertNodeHits(results);
		
		nodeTable.createColumn(NODE_COMMON, String.class, false);
		nodeTable.getRow(nodeSuid1).set(NODE_COMMON, "frodo");
		nodeTable.getRow(nodeSuid9).set(NODE_COMMON, "sam");
		nodeTable.getRow(nodeSuid15).set(NODE_COMMON, "gandalf");
		
		searchManager.reindexTable(nodeTable);
		
		results = queryIndex("BAR1 MFA2 SSN6");
		assertNodeHits(results);
		
		results = queryIndex("frodo sam gandalf");
		assertNodeHits(results, 1, 9, 15);
	}
	
	
	@Test
	public void testRenameColumn() throws Exception {
		var nodeTable = network.getDefaultNodeTable();

		SearchResults results = queryIndex("BAR1 MFA2 SSN6");
		assertNodeHits(results, 1, 9, 15);
		
		nodeTable.getColumn(NODE_COMMON).setName("ReNamed");
		searchManager.reindexTable(nodeTable).get();
		
		results = queryIndex("BAR1 MFA2 SSN6");
		assertNodeHits(results, 1, 9, 15);
		
		results = queryIndex("COMMON:BAR1");
		assertNodeHits(results);
		
		results = queryIndex("renamed:BAR1");
		assertNodeHits(results, 1);
	}
	
	
	@Test
	public void testStopWords() throws Exception {
		// Stop words should not be removed, should be able to find 'the'
		SearchResults results = queryIndex("the");
		assertNodeHits(results, 1, 2, 3, 5, 7, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 21);
	}
	
	
	@Test
	public void testNumericRangeQueries() throws Exception {
		SearchResults results;
		
		results = queryIndex("degree.layout:99");
		assertNodeHits(results, 20);
		
		results = queryIndex("degree.layout:[3 TO 6]");
		assertNodeHits(results, 5, 8, 9, 10, 11, 14, 16, 21);
		
		results = queryIndex("degree.layout:[3 TO 6}");
		assertNodeHits(results, 5, 8, 9, 10, 11, 14, 21);
		
		results = queryIndex(EDGE_EDGEBETWEENNESS+":9");
		assertEdgeHits(results, 19);
		
		results = queryIndex(EDGE_EDGEBETWEENNESS+":[300.0 TO 500.0]");
		assertEdgeHits(results, 9, 11, 14, 15, 16, 24, 25, 28);
		
		results = queryIndex(EDGE_EDGEBETWEENNESS+":[4.83333333 TO 9.83333333]");
		assertEdgeHits(results, 1, 12, 13, 23, 26, 29, 19);
		
		results = queryIndex(EDGE_EDGEBETWEENNESS+":[4.8 TO 9.9]");
		assertEdgeHits(results, 1, 12, 13, 23, 26, 29, 19);
		
		results = queryIndex(EDGE_EDGEBETWEENNESS+":[4.83333333 TO 9.83333333}");
		assertEdgeHits(results, 1, 12, 13, 19);
		
		results = queryIndex(EDGE_EDGEBETWEENNESS+":{4.83333333 TO 9.83333333}");
		assertEdgeHits(results, 19);
		
		// TODO: The MultiFieldQueryParser doesn't support this. 
		// If you want to do a numeric query you must specify the column name.
//		results = queryIndex("19179.59362859");
//		assertEdgeHits(results, 6);
	}


	@Test
	public void testColumnNamespaces() throws Exception {
		SearchResults results;
		
		results = queryIndex("BAR1");
		assertNodeHits(results, 1);
		
		// Namespace separator must be escaped
		results = queryIndex("namespace\\:\\:common:BAR1");
		assertNodeHits(results, 1);
		
		// Can use just the name without the namespace.
		results = queryIndex("common:BAR1");
		assertNodeHits(results, 1);

		final String COMMON2 = "namespace2::common";
		final String NUMERIC = "numeric::mycolumn";
		
		var nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(COMMON2, String.class, false);
		nodeTable.createColumn(NUMERIC, Double.class, false);
		
		Long nodeSuid1  = getSUID(nodeTable, 1);
		Long nodeSuid9  = getSUID(nodeTable, 9);
		Long nodeSuid15 = getSUID(nodeTable, 15);
		
		nodeTable.getRow(nodeSuid1).set(COMMON2, "sam");
		nodeTable.getRow(nodeSuid9).set(COMMON2, "frodo");
		nodeTable.getRow(nodeSuid15).set(COMMON2, "gandalf");
		nodeTable.getRow(nodeSuid1).set(NUMERIC, 1.0);
		nodeTable.getRow(nodeSuid9).set(NUMERIC, 2.0);
		nodeTable.getRow(nodeSuid15).set(NUMERIC, 3.0);
		
		searchManager.reindexTable(nodeTable).get();
		
		// The first column named 'common' should win
		results = queryIndex("common:BAR1");
		assertNodeHits(results, 1);
		results = queryIndex("common:sam");
		assertNodeHits(results);
		
		// Should work for numbers
		results  = queryIndex("mycolumn:2.0");
		assertNodeHits(results, 9);
	}
	
	
	@Test
	public void testListColumns() throws Exception {
		final String INT_LIST = "nodeIntList";
		final String STR_LIST = "nodeStrList";
		
		var nodeTable = network.getDefaultNodeTable();
		nodeTable.createListColumn(INT_LIST, Integer.class, false);
		nodeTable.createListColumn(STR_LIST, String.class, false);
		
		Long nodeSuid1  = getSUID(nodeTable, 1);
		Long nodeSuid9  = getSUID(nodeTable, 9);
		Long nodeSuid15 = getSUID(nodeTable, 15);
		
		nodeTable.getRow(nodeSuid1) .set(INT_LIST, List.of(100, 101, 102));
		nodeTable.getRow(nodeSuid9 ).set(INT_LIST, List.of(102, 103, 104));
		nodeTable.getRow(nodeSuid15).set(INT_LIST, List.of(100));
		nodeTable.getRow(nodeSuid1) .set(STR_LIST, List.of("sam", "gandalf", "frodo"));
		nodeTable.getRow(nodeSuid9) .set(STR_LIST, List.of("gandalf", "legolas"));
		nodeTable.getRow(nodeSuid15).set(STR_LIST, List.of("legolas", "boromir"));
		
		searchManager.reindexTable(nodeTable).get();
		
		SearchResults results;
		
		results = queryIndex("gandalf");
		assertNodeHits(results, 1, 9);
		results = queryIndex("nodestrlist:gandalf");
		assertNodeHits(results, 1, 9);
		results = queryIndex("gandalf legolas");
		assertNodeHits(results, 1, 9, 15);
		
		results = queryIndex("nodeIntList:102");
		assertNodeHits(results, 1, 9);
		results = queryIndex("nodeIntList:[100 TO 102]");
		assertNodeHits(results, 1, 9, 15);
		results = queryIndex("nodeIntList:{100 TO 102]");
		assertNodeHits(results, 1, 9);
	}

	
	@Test
	public void testBooleanColumns() throws Exception {
		final String BOOL = "boolCol";
		
		var nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(BOOL, Boolean.class, false);
		
		Long nodeSuid1  = getSUID(nodeTable, 1);
		Long nodeSuid9  = getSUID(nodeTable, 9);
		Long nodeSuid15 = getSUID(nodeTable, 15);
		
		nodeTable.getRow(nodeSuid1) .set(BOOL, true);
		nodeTable.getRow(nodeSuid9) .set(BOOL, false);
		nodeTable.getRow(nodeSuid15).set(BOOL, true);
		
		searchManager.reindexTable(nodeTable).get();
		
		SearchResults results;
		
		// Booleans are indexed as strings with value "true" or "false"
		results = queryIndex("true");
		assertNodeHits(results, 1, 15);
		results = queryIndex("boolCol:true");
		assertNodeHits(results, 1, 15);
		results = queryIndex("false");
		assertNodeHits(results, 9);
		results = queryIndex("name:true");
		assertNodeHits(results);
	}


	@Test
	public void testBooleanOperators() throws Exception {
		SearchResults results;
		
		results = queryIndex("far");
		assertNodeHits(results, 1, 9);
		results = queryIndex("from");
		assertNodeHits(results, 1, 19, 21);
		results = queryIndex("far from");
		assertNodeHits(results, 1, 9, 19, 21);
		results = queryIndex("far OR from");
		assertNodeHits(results, 1, 9, 19, 21);
		results = queryIndex("\"far from\"");
		assertNodeHits(results, 1);
		results = queryIndex("far AND from");
		assertNodeHits(results, 1);
		results = queryIndex("\"blind text by\"");
		assertNodeHits(results, 9);
	}
	
	
}
