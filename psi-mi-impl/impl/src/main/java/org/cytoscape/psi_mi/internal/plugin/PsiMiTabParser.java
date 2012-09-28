package org.cytoscape.psi_mi.internal.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsiMiTabParser {

	private static final Logger logger = LoggerFactory.getLogger(PsiMiTabParser.class);

	private static final int BUFFER_SIZE = 100000;

	private static final String ATTR_PREFIX = "PSIMI-25.";

	// Node Attr Names
	private static final String INTERACTOR_TYPE = ATTR_PREFIX + "interactor type";
	private static final String ALIASES = ATTR_PREFIX + "aliases";
	private static final String TAXONIDS = ATTR_PREFIX + "taxon ID";
	private static final String TAXONDBS = ATTR_PREFIX + "taxon DB";

	// Edge Attr Names
	private static final String INTERACTION = CyEdge.INTERACTION; // should
																	// already
																	// exist
	private static final String DETECTION_METHOD_ID = ATTR_PREFIX + "detection method ID";
	private static final String DETECTION_METHOD = ATTR_PREFIX + "detection method";
	private static final String INTERACTION_TYPE = ATTR_PREFIX + "interaction type";
	private static final String INTERACTION_TYPE_ID = ATTR_PREFIX + "interaction type ID";
	private static final String SOURCE_DB = ATTR_PREFIX + "source DB";
	private static final String EDGE_SCORE = ATTR_PREFIX + "edge score";
	private static final String AUTHORS = ATTR_PREFIX + "authors";
	private static final String PUBLICATION_ID = ATTR_PREFIX + "publication ID";
	private static final String PUBLICATION_DB = ATTR_PREFIX + "publication DB";

	// Stable IDs which maybe used for mapping later
	private static final String CHEBI = "chebi";
	private static final String COMPOUND = "compound";

	//private Map<String, CyNode> nodeMap;

	private final InputStream inputStream;
	private final CyNetworkFactory cyNetworkFactory;

	private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager; 
	
	
	private boolean cancelFlag = false;

	public PsiMiTabParser(final InputStream inputStream, final CyNetworkFactory cyNetworkFactory, 
			final CyNetworkManager cyNetworkManager, final CyRootNetworkManager cyRootNetworkManager) {
		this.inputStream = inputStream;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	public CyNetwork parse(final TaskMonitor taskMonitor) throws IOException {

		long start = System.currentTimeMillis();

		taskMonitor.setProgress(-1.0);

		//this.nodeMap = new HashMap<String, CyNode>();

		final CyNetwork network = this.rootNetwork.addSubNetwork();//cyNetworkFactory.createNetwork();

		initColumns(network);

		String line;
		final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream), BUFFER_SIZE);
		final MITABLine25 mline = new MITABLine25();

		long interactionCount = 0;
		while ((line = br.readLine()) != null) {
			if (cancelFlag) {
				cleanup(br);
				return network;
			}

			// Ignore comment line
			if (line.startsWith("#"))
				continue;

			try {
				processFull(network, mline, line);
			} catch (Exception ex) {
				logger.warn("Could not parse this line: " + line, ex);
				continue;
			}
			if (++interactionCount % 100 == 0)
				taskMonitor.setStatusMessage("parsed " + interactionCount + " interactions");
		}

		br.close();
		//nodeMap.clear();
		//nodeMap = null;

		logger.info("MITAB Parse finished in " + (System.currentTimeMillis() - start) + " msec.");

		return network;
	}

//	private void processMinimum(final CyNetwork network, final MITABLine25 mline, final String line) {
//		mline.readLine(line);
//
//		final String sourceRawID = mline.sourceRawID;
//		final String targetRawID = mline.targetRawID;
//
//		// create nodes
//		CyNode source = nodeMap.get(sourceRawID);
//		if (source == null) {
//			source = network.addNode();
//			nodeMap.put(sourceRawID, source);
//		}
//		CyNode target = nodeMap.get(targetRawID);
//		if (target == null) {
//			target = network.addNode();
//			nodeMap.put(targetRawID, target);
//		}
//
//		final CyRow sourceRow = network.getRow(source);
//		final CyRow targetRow = network.getRow(target);
//
//		// set various node attrs
//		sourceRow.set(CyNetwork.NAME, sourceRawID);
//		targetRow.set(CyNetwork.NAME, targetRawID);
//
//		setInteractorType(sourceRow, mline.srcAliases);
//		setInteractorType(targetRow, mline.tgtAliases);
//
//		setAliases(sourceRow, mline.srcAliases, mline.srcDBs);
//		setAliases(targetRow, mline.tgtAliases, mline.tgtDBs);
//
//		setTaxID(sourceRow, mline.srcTaxonIDs, mline.srcTaxonDBs);
//		setTaxID(targetRow, mline.tgtTaxonIDs, mline.tgtTaxonDBs);
//
//		// create edge
//		final CyEdge e = network.addEdge(source, target, true);
//		CyRow edgeRow = network.getRow(e);
//
//		// set various edge attrs
//		String interactionId = "unknown";
//		if (mline.interactionIDs.size() > 0)
//			interactionId = mline.interactionIDs.get(0);
//
//		edgeRow.set(INTERACTION, interactionId);
//		edgeRow.set(CyNetwork.NAME, sourceRawID + " (" + interactionId + ") " + targetRawID);
//
//		setTypedEdgeListAttribute(edgeRow, mline.interactionTypes, INTERACTION_TYPE_ID, INTERACTION_TYPE);
//		setTypedEdgeListAttribute(edgeRow, mline.detectionMethods, DETECTION_METHOD_ID, DETECTION_METHOD);
//		setEdgeListAttribute(edgeRow, mline.sourceDBs, SOURCE_DB);
//		setEdgeListAttribute(edgeRow, mline.edgeScoreStrings, EDGE_SCORE);
//
//		setPublication(edgeRow, mline.publicationValues, mline.publicationDBs);
//		setAuthors(edgeRow, mline.authors);
//	}

	private void processFull(final CyNetwork network, final MITABLine25 mline, final String line) {
		mline.readLine(line);

		final String sourceRawID = mline.sourceRawID;
		final String targetRawID = mline.targetRawID;


		// create nodes
//		CyNode source = nodeMap.get(sourceRawID);
//		if (source == null) {
//			source = network.addNode();
//			nodeMap.put(sourceRawID, source);
//		}
//		CyNode target = nodeMap.get(targetRawID);
//		if (target == null) {
//			target = network.addNode();
//			nodeMap.put(targetRawID, target);
//		}

		CyNode source;
		if (this.nMap.get(sourceRawID) == null){
			source = network.addNode();
			this.nMap.put(sourceRawID, this.rootNetwork.getNode(source.getSUID()));
		}
		else {
			CyNode parentNode = this.nMap.get(sourceRawID);
			CySubNetwork subnet = (CySubNetwork) network;
			subnet.addNode(parentNode);
			source = subnet.getNode(parentNode.getSUID()); 
		}

		CyNode target;
		if (this.nMap.get(targetRawID) == null){
			target = network.addNode();
			this.nMap.put(targetRawID, this.rootNetwork.getNode(target.getSUID()));
		}
		else {
			CyNode parentNode = this.nMap.get(targetRawID);
			CySubNetwork subnet = (CySubNetwork) network;
			subnet.addNode(parentNode);
			target = subnet.getNode(parentNode.getSUID()); 
		}

		
		final CyRow sourceRow = network.getRow(source);
		final CyRow targetRow = network.getRow(target);

		// set various node attrs
		sourceRow.set(CyNetwork.NAME, sourceRawID);
		targetRow.set(CyNetwork.NAME, targetRawID);
		
		final List<String> sDB = mline.sourceDBs;
		for(int i=0; i<sDB.size(); i++) {
			String dbName = sDB.get(i);
			if(sourceRow.getTable().getColumn(dbName) == null)
				sourceRow.getTable().createColumn(dbName, String.class, true);
			
			sourceRow.set(dbName, mline.sourceIDs.get(i));
		}
		
		

		setInteractorType(sourceRow, mline.srcAliases);
		setInteractorType(targetRow, mline.tgtAliases);

		setAliases(sourceRow, mline.srcAliases, mline.srcDBs);
		setAliases(targetRow, mline.tgtAliases, mline.tgtDBs);

		setTaxID(sourceRow, mline.srcTaxonIDs, mline.srcTaxonDBs);
		setTaxID(targetRow, mline.tgtTaxonIDs, mline.tgtTaxonDBs);

		// create edge
		final CyEdge e = network.addEdge(source, target, true);
		CyRow edgeRow = network.getRow(e);

		// set various edge attrs
		String interactionId = "unknown";
		if (mline.interactionIDs.size() > 0)
			interactionId = mline.interactionIDs.get(0);

		edgeRow.set(INTERACTION, interactionId);
		edgeRow.set(CyNetwork.NAME, sourceRawID + " (" + interactionId + ") " + targetRawID);

		setTypedEdgeListAttribute(edgeRow, mline.interactionTypes, INTERACTION_TYPE_ID, INTERACTION_TYPE);
		setTypedEdgeListAttribute(edgeRow, mline.detectionMethods, DETECTION_METHOD_ID, DETECTION_METHOD);
		setEdgeListAttribute(edgeRow, mline.sourceDBs, SOURCE_DB);
		setEdgeListAttribute(edgeRow, mline.edgeScoreStrings, EDGE_SCORE);

		setPublication(edgeRow, mline.publicationValues, mline.publicationDBs);
		setAuthors(edgeRow, mline.authors);
	}

	private void setTaxID(CyRow row, List<String> taxonIDs, List<String> taxonDBs) {
		row.set(TAXONIDS, taxonIDs);
		row.set(TAXONDBS, taxonDBs);
	}

	private void setPublication(CyRow row, List<String> pubID, List<String> pubDB) {
		for (int i = 0; i < pubID.size(); i++) {
			listAttrMapper(row, PUBLICATION_ID, pubID.get(i));
			listAttrMapper(row, PUBLICATION_DB, pubDB.get(i));
		}
	}

	private void setAuthors(CyRow row, List<String> authors) {
		for (String val : authors) {
			listAttrMapper(row, AUTHORS, val);
		}
	}

	private void setAliases(CyRow row, List<String> aliases, List<String> aliasDBs) {
		for (String s : aliases) {
			int ind = s.indexOf('(');
			if (ind > 0)
				s = s.substring(0, ind);
			listAttrMapper(row, ALIASES, s);
		}
	}

	private void setEdgeListAttribute(CyRow row, List<String> entry, String key) {
		for (String val : entry) {
			listAttrMapper(row, key, val);
		}
	}

	private void setTypedEdgeListAttribute(CyRow row, List<String> entry, String idKey, String descKey) {
		for (String val : entry) {
			String id = "";
			String desc = "";

			// Extract description between parens.
			int openParen = val.indexOf('(');
			if (openParen >= 0) {
				int closeParen = val.indexOf(')');
				if (closeParen > openParen)
					desc = val.substring(openParen + 1, closeParen);
			}

			// Extract ID between quotes.
			int firstQuote = val.indexOf('"');
			if (firstQuote >= 0) {
				int secondQuote = val.indexOf('"', firstQuote + 1);
				if (secondQuote > firstQuote) {
					id = val.substring(firstQuote + 1, secondQuote);
				}
			}

			// If we can't parse properly, just shove the whole
			// thing in description.
			if (desc.equals("") || id.equals("")) {
				listAttrMapper(row, descKey, val);
			} else {
				listAttrMapper(row, idKey, id);
				listAttrMapper(row, descKey, desc);
			}
		}
	}

	private void listAttrMapper(CyRow row, String attrName, String value) {
		List<String> currentAttr = row.getList(attrName, String.class);

		if (currentAttr == null) {
			currentAttr = new ArrayList<String>();
			currentAttr.add(value);
			row.set(attrName, currentAttr);
		} else if (currentAttr.contains(value) == false) {
			currentAttr.add(value);
			row.set(attrName, currentAttr);
		}
	}

	public void cancel() {
		cancelFlag = true;
	}

	private void cleanup(final Reader br) throws IOException {
		br.close();
		//nodeMap.clear();
		//nodeMap = null;
	}

	private void setInteractorType(CyRow row, List<String> aliases) {
		// Set type if not protein
		if (aliases.contains(CHEBI))
			row.set(INTERACTOR_TYPE, COMPOUND);
	}

	private void initColumns(CyNetwork network) {
		final CyTable nodeTable = network.getDefaultNodeTable();
		createListColumn(nodeTable, INTERACTOR_TYPE, String.class);
		createListColumn(nodeTable, ALIASES, String.class);
		createListColumn(nodeTable, TAXONIDS, String.class);
		createListColumn(nodeTable, TAXONDBS, String.class);

		final CyTable edgeTable = network.getDefaultEdgeTable();
		createListColumn(edgeTable, INTERACTION_TYPE, String.class);
		createListColumn(edgeTable, INTERACTION_TYPE_ID, String.class);
		createListColumn(edgeTable, DETECTION_METHOD, String.class);
		createListColumn(edgeTable, DETECTION_METHOD_ID, String.class);
		createListColumn(edgeTable, SOURCE_DB, String.class);
		createListColumn(edgeTable, EDGE_SCORE, String.class);
		createListColumn(edgeTable, AUTHORS, String.class);
		createListColumn(edgeTable, PUBLICATION_ID, String.class);
		createListColumn(edgeTable, PUBLICATION_DB, String.class);
	}

	private void createListColumn(CyTable table, String colName, Class<?> type) {
		if (table.getColumn(colName) == null)
			table.createListColumn(colName, String.class, false);
	}

	// support import network in different collection
	private CyRootNetwork rootNetwork;
	public void setRootNetwork(CyRootNetwork rootNetwork){
		this.rootNetwork = rootNetwork;
	}
	
	private Map<Object, CyNode> nMap;
	public void setNodeMap(Map<Object, CyNode> nMap){
		this.nMap = nMap;
	}
}
