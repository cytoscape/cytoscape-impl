package org.cytoscape.tableimport.internal.reader.ontology;


import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.TAB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.util.OntologyDAGManager;
import org.cytoscape.tableimport.internal.util.OntologyUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GeneAssociationReader extends AbstractTask implements CyTableReader {
	private static final Logger logger = LoggerFactory.getLogger(GeneAssociationReader.class);

	private static final String COMPATIBLE_VERSION = "gaf-version: 2.0";
	private static final String TAXON_RESOURCE_FILE = "tax_report.txt";
	private static final String LIST_DELIMITER = "\\|";

	// The following columns should be handled as List in GA v2 spec.
	private static final List<Integer> LIST_INDEX = new ArrayList<Integer>();
	private static final Map<String, String> NAMESPACE_MAP = new HashMap<String, String>();
	static {
		LIST_INDEX.add(4);
		LIST_INDEX.add(8);
		LIST_INDEX.add(11);
		LIST_INDEX.add(16);

		NAMESPACE_MAP.put("P", "biological process");
		NAMESPACE_MAP.put("F", "molecular function");
		NAMESPACE_MAP.put("C", "cellular component");
	}

	public static final String SYNONYM_COL_NAME = "Synonym";

	private static final String EVIDENCE_SUFFIX = " Evidence Code";
	private static final String REFERENCE_SUFFIX = " DB Reference";
	private static final String GO_PREFIX = "GO";
	private static final String ANNOTATION_PREFIX = "annotation";
	private static final String GA_DELIMITER = TAB.toString();
	private static final String ID = "ID";

	// This is minimum required fields. Max is 17 for v2
	private static final int EXPECTED_COL_COUNT = 15;

	private static final int DB_OBJ_ID = 1;
	private static final int OBJ_NAME = 9;
	private static final int ASPECT = 8;
	private static final int SYNONYM = 10;
	private static final int GOID = 4;

	private int key = 2;

	private InputStream is;
	// private Aliases nodeAliases;
	private Map<String, List<String>> attr2id;
	private CyTable nodeAttributes;
	// private GeneOntology geneOntology;
	private Map<String, String> speciesMap;

	private Map<String, String> uppercaseIDMap;

	private CyNetwork ontologyDAG;

	private CyTable dagTable;

	private final CyTableFactory tableFactory;

	private final String tableName;

	private boolean caseSensitive = true;

	// Global table (result)
	private CyTable table;
	private CyTable[] tables;

	private final String ontologyDagName;
	private List<String> termIDList;
	private final CyTableManager tableManager;

	/**
	 * Package protected because only in unit testing do we need to specify the
	 * taxon resource file. Normal operation should use one of the other
	 * constructors.
	 */
	public GeneAssociationReader(final CyTableFactory tableFactory, final String ontologyDagName,
	                             final InputStream is, final String tableName,
	                             final CyTableManager tableManager) throws IOException
	{
		logger.debug("DAG Manager key = " + ontologyDagName);
		this.ontologyDagName = ontologyDagName;
		this.tableFactory = tableFactory;
		this.is = is;
		this.tableManager = tableManager;

		this.tableName = tableName;

		logger.debug("Table Name = " + tableName);

		// GA file is only for nodes!
		// this.nodeAliases = Cytoscape.getOntologyServer().getNodeAliases();
		// this.nodeAttributes = Cytoscape.getNodeAttributes();

		// final Ontology testOntology =
		// Cytoscape.getOntologyServer().getOntologies().get(ontologyName);
		//
		// if (testOntology == null)
		// throw new IOException("Could not find ontology data for " +
		// ontologyName + " in memory.");
		//
		// if (testOntology.getClass() == GeneOntology.class)
		// this.geneOntology = (GeneOntology) testOntology;
		// else
		// throw new IllegalArgumentException(
		// "Given ontology is not Gene Ontology.  Gene Assiciation File can be used with GO only.");

		// Load taxonomy map
		this.buildTaxonMap();

		// // If key is NOT ID, create mapping.
		// if ((this.keyAttributeName != null) &&
		// !this.keyAttributeName.equals(ID))
		// buildMap();
		//
		// if (!caseSensitive) {
		// // // Create all uppercase ID map
		// // uppercaseIDMap = new HashMap<String, String>();
		// // List<CyNode> nodes = Cytoscape.getRootGraph().nodesList();
		// // for (CyNode node : nodes)
		// // uppercaseIDMap.put(node.getIdentifier().toUpperCase(),
		// // node.getIdentifier());
		// //
		// // nodes.clear();
		// // nodes = null;
		// }
	}

	private void buildTaxonMap() throws IOException {
		// get URL for resource file.
		final URL taxUrl = this.getClass().getClassLoader().getResource(TAXON_RESOURCE_FILE);

		if (taxUrl == null)
			throw new IllegalStateException("Could not find taxonomy ID conversion table.");

		BufferedReader taxonFileReader = null;

		try {
			taxonFileReader = new BufferedReader(new InputStreamReader(taxUrl.openStream()));
			final OntologyUtil ontologyUtil = new OntologyUtil();
			this.speciesMap = ontologyUtil.getTaxonMap(taxonFileReader);
		} finally {
			if (taxonFileReader != null)
				taxonFileReader.close();
		}
	}

	private void buildMap() {
		// attr2id = new HashMap<String, List<String>>();
		//
		// final Iterator<CyNode> it = Cytoscape.getRootGraph().nodesIterator();
		//
		// String nodeID = null;
		// Node node = null;
		// String attributeValue = null;
		// List<String> nodeIdList = null;
		//
		// while (it.hasNext()) {
		// node = it.next();
		// nodeID = node.getIdentifier();
		// attributeValue = nodeAttributes.getStringAttribute(nodeID,
		// keyAttributeName);
		//
		// if (attributeValue != null) {
		// if (attr2id.containsKey(attributeValue))
		// nodeIdList = attr2id.get(attributeValue);
		// else
		// nodeIdList = new ArrayList<String>();
		//
		// nodeIdList.add(nodeID);
		// attr2id.put(attributeValue, nodeIdList);
		// }
		// }
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Loading Gene Association File");
		taskMonitor.setStatusMessage("Importing annotation file.  Please wait...");
		taskMonitor.setProgress(-1.0);

		this.ontologyDAG = OntologyDAGManager.getOntologyDAG(ontologyDagName);

		if (ontologyDAG == null)
			logger.warn("Could not find associated Ontology DAG.");
		else {
			this.dagTable = ontologyDAG.getDefaultNodeTable();
			termIDList = ontologyDAG.getDefaultNodeTable().getColumn(CyNetwork.NAME).getValues(String.class);
			logger.debug("DAG Name = " + ontologyDAG.getRow(ontologyDAG).get(CyNetwork.NAME, String.class));
		}
		
		BufferedReader bufRd = new BufferedReader(new InputStreamReader(is));

		String line = null;
		String[] parts;
		int lineCounter = 0;

		while ((line = bufRd.readLine()) != null) {
			lineCounter++;
			// Validate.
			if (line.contains(COMPATIBLE_VERSION))
				break;
		}

		// Create result table
		table = tableFactory.createTable(tableName, CyNetwork.NAME, String.class, true, true);

		createColumns();

		while ((line = bufRd.readLine()) != null) {
			lineCounter++;
			if (line.startsWith("!"))
				continue;

			parts = line.split(GA_DELIMITER);
			// GA is a fixed format file. Read only valid lines.
			if (parts.length >= EXPECTED_COL_COUNT) {
				parseGA(parts);
			}
		}

		bufRd.close();
		is.close();
		is = null;
		// uppercaseIDMap.clear();
		// uppercaseIDMap = null;

		tables = new CyTable[1];
		tables[0] = table;

		tableManager.addTable(table);

	}

	private void createColumns() {
		// Create columns if necessary
		final GeneAssociationTag[] tags = GeneAssociationTag.values();
		int index = 0;
		for (GeneAssociationTag tag : tags) {
			index++;
			// Special cases: handled in the last part
			if (tag == GeneAssociationTag.GO_ID || tag == GeneAssociationTag.EVIDENCE
					|| tag == GeneAssociationTag.DB_REFERENCE || tag == GeneAssociationTag.ASPECT)
				continue;

			final String tagString = tag.toString();
			if (table.getColumn(tagString) == null) {
				if (LIST_INDEX.contains(index)) {
					table.createListColumn(tagString, String.class, false);
				} else {
					table.createColumn(tagString, String.class, false);
				}
			}
		}

		// Create one column per namespace
		for (String name : NAMESPACE_MAP.keySet()) {
			table.createListColumn(NAMESPACE_MAP.get(name), String.class, false);
			table.createListColumn(NAMESPACE_MAP.get(name) + EVIDENCE_SUFFIX, String.class, false);
			table.createListColumn(NAMESPACE_MAP.get(name) + REFERENCE_SUFFIX, String.class, false);
		}

		// Consolidated entry name list
		table.createListColumn(SYNONYM_COL_NAME, String.class, true);
	}

	private void parseGA(String[] entries) {

		final String goTermID = entries[GOID];

		// final String attributeName = ANNOTATION_PREFIX + "." + GO_PREFIX +
		// " "
		// + geneOntology.getAspect(entries[GOID]).name();

		// Add all aliases
		if ((entries[SYNONYM] != null) && (entries[SYNONYM].length() != 0)) {
			final String[] alias = entries[SYNONYM].split(PIPE.toString());
			// nodeAliases.add(entries[key], Arrays.asList(alias));
		}

		mapEntry(entries);
	}

	private String setAlias(final String key, final String objName, final String synoString, final String dbSpecificId) {
		//
		// final String[] synos = synoString.split(PIPE.toString());
		// final String[] objNames;
		// final Set<String> idSet = new TreeSet<String>();
		//
		// if ((objName != null) && (objName.length() != 0)) {
		// String[] tempObj = objName.split(":");
		//
		// if (tempObj.length != 0) {
		// objNames = tempObj[0].split(",");
		//
		// for (String name : objNames)
		// idSet.add(name);
		// }
		// }
		//
		// idSet.add(key);
		//
		// if ((dbSpecificId != null) && (dbSpecificId.length() != 0))
		// idSet.add(dbSpecificId);
		//
		// for (String synonym : synos)
		// idSet.add(synonym);
		//
		// for (String id : idSet) {
		// if (Cytoscape.getCyNode(id) != null) {
		// // Match found
		// if (idSet.size() != 1)
		// idSet.remove(id);
		//
		// nodeAliases.add(id, new ArrayList<String>(idSet));
		//
		// return id;
		// } else if (!caseSensitive) {
		// final String originalID = uppercaseIDMap.get(id.toUpperCase());
		// if (originalID == null)
		// continue;
		//
		// if (idSet.size() != 1)
		// idSet.remove(originalID);
		//
		// nodeAliases.add(originalID, new ArrayList<String>(idSet));
		// return originalID;
		//
		// }
		// }
		//
		return null;
	}

	private List<String> setMultipleAliases(String key, String synoString) {
		final String[] synos = synoString.split(PIPE.toString());
		final Set<String> idSet = new TreeSet<String>();
		idSet.add(key);

		for (String synonym : synos)
			idSet.add(synonym);

		String targetID = null;
		for (String id : idSet) {
			if (!caseSensitive) {
				targetID = uppercaseIDMap.get(id.toUpperCase());
				if (targetID == null)
					continue;
			} else
				targetID = id;

			List<String> nodeIDs = attr2id.get(targetID);
			if (nodeIDs != null) {
				return nodeIDs;
			}
		}

		return null;
	}

	private void mapEntry(final String[] entries) {
		String fullName = null;

		// Set primary key for the table, which is DB Object ID
		final String primaryKeyValue = entries[DB_OBJ_ID];
		final CyRow row = table.getRow(primaryKeyValue);
		row.set(CyNetwork.NAME, primaryKeyValue);

		// Check namespace
		final String namespace = NAMESPACE_MAP.get(entries[ASPECT]);

		for (int i = 0; i < EXPECTED_COL_COUNT; i++) {
			final GeneAssociationTag tag = GeneAssociationTag.values()[i];

			switch (tag) {

			// Evidence code and GO ID should be organized by namespace.
			case GO_ID:
				String goidString = entries[i];
				if(this.termIDList != null)
					goidString = convertToName(goidString);
				
				List<String> currentList = row.getList(namespace, String.class);
				if (currentList == null)
					currentList = new ArrayList<String>();

				if (currentList.contains(goidString) == false)
					currentList.add(goidString);
				row.set(namespace, currentList);

				// TODO: create term name list here.

				break;

			case EVIDENCE:
			case DB_REFERENCE:
				final String value = entries[i];
				String columnName = namespace;
				if (tag == GeneAssociationTag.EVIDENCE)
					columnName = columnName + EVIDENCE_SUFFIX;
				else
					columnName = columnName + REFERENCE_SUFFIX;

				List<String> valueList = row.getList(columnName, String.class);
				if (valueList == null)
					valueList = new ArrayList<String>();
				if (valueList.contains(value) == false)
					valueList.add(value);
				row.set(columnName, valueList);

				break;
			case TAXON:
				final String taxID = entries[i].split(":")[1];
				final String taxName = speciesMap.get(taxID);
				if (taxName != null)
					row.set(tag.toString(), taxName);
				else if (taxID != null)
					row.set(tag.toString(), taxID);
				break;

			case ASPECT:
				// Ignore these lines
				break;

			case DB_OBJECT_ID:
			case DB_OBJECT_SYMBOL:
			case DB_OBJECT_SYNONYM:
				// Create consolidated id list attribute.
				List<String> synList = row.getList(SYNONYM_COL_NAME, String.class);
				if (synList == null)
					synList = new ArrayList<String>();

				if (tag == GeneAssociationTag.DB_OBJECT_SYNONYM) {
					final String[] vals = entries[i].split(LIST_DELIMITER);
					for (String val : vals) {
						if (synList.contains(val) == false)
							synList.add(val);
					}
				} else {
					if (synList.contains(entries[i]) == false)
						synList.add(entries[i]);
				}
				row.set(SYNONYM_COL_NAME, synList);
				break;
			default:
				if (LIST_INDEX.contains(i + 1)) {
					final String[] vals = entries[i].split(LIST_DELIMITER);

					List<String> listVals = row.getList(tag.toString(), String.class);
					if (listVals == null)
						listVals = new ArrayList<String>();
					for (String val : vals) {
						if (listVals.contains(val) == false)
							listVals.add(val);
					}
					row.set(tag.toString(), listVals);
				} else
					row.set(tag.toString(), entries[i]);
				break;
			}
		}
	}
	
	private String convertToName(final String id) {
		final Collection<CyRow> rows = ontologyDAG.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, id);
		if (rows != null) {
			final CyRow row = rows.iterator().next();
			final String termName = row.get(OBOReader.TERM_NAME, String.class);
			if(termName != null)
				return termName;
			else
				return id;
		} else
			return id;
	}

	@Override
	public CyTable[] getTables() {
		return tables;
	}
}
