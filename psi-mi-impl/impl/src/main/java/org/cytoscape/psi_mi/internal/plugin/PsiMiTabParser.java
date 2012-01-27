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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.property.session.Network;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsiMiTabParser {
	
	private static final Logger logger = LoggerFactory.getLogger(PsiMiTabParser.class);
	
	private static final int BUFFER_SIZE = 100000;

	// Separator for multiple entries.
	private static final String SEPARATOR = "\\|";
	private static final String ATTR_PREFIX = "PSI-MI-25.";

	private static final int COLUMN_COUNT = 15;

	// Reg.Ex for parsing entry
	private final static Pattern miPttr = Pattern.compile("MI:\\d{4}");
	private final static Pattern miNamePttr = Pattern.compile("\\(.+\\)");

	private static final String TAB = "\t";
	private static final String INTERACTION = "interaction";

	// Attr Names
	private static final String DETECTION_METHOD = ATTR_PREFIX + "interaction detection method";
	private static final String INTERACTION_TYPE = ATTR_PREFIX + "interaction type";
	private static final String SOURCE_DB = ATTR_PREFIX + "source database";
	private static final String INTERACTION_ID = ATTR_PREFIX + "Interaction ID";
	private static final String EDGE_SCORE = ATTR_PREFIX + "confidence score";

	// Stable IDs which maybe used for mapping later
	private static final String UNIPROT = "uniprotkb";
	private static final String ENTREZ_GENE = "entrezgene/locuslink";
	private static final String ENTREZ_GENE_SYN = "entrez gene/locuslink";

	private static final String CHEBI = "chebi";

	private static final String INTERACTOR_TYPE = ATTR_PREFIX + "interactor type";
	private static final String COMPOUND = "compound";

	private Matcher matcher;

	private Map<String, CyNode> nodeMap;

	private final InputStream inputStream;
	private final CyNetworkFactory cyNetworkFactory;
	
	private boolean cancelFlag = false;

	public PsiMiTabParser(final InputStream inputStream, final CyNetworkFactory cyNetworkFactory) {
		this.inputStream = inputStream;
		this.cyNetworkFactory = cyNetworkFactory;
	}

	public CyNetwork parse(final TaskMonitor taskMonitor) throws IOException {
		
		long start = System.currentTimeMillis();
		
		this.nodeMap = new HashMap<String, CyNode>();

		String[] entry;
		String[] sourceID;
		String[] targetID;

		String[] detectionMethods;

		String[] sourceDB;
		String[] interactionID;
		String[] interactionType;

		String[] edgeScore;

		final CyNetwork network = cyNetworkFactory.createNetwork();

		final CyTable nodeTable = network.getDefaultNodeTable();
		if (nodeTable.getColumn(INTERACTOR_TYPE) == null)
			nodeTable.createColumn(INTERACTOR_TYPE, String.class, false);
		if (nodeTable.getColumn(INTERACTOR_TYPE + ".name") == null)
			nodeTable.createColumn(INTERACTOR_TYPE + ".name", String.class, false);

		final CyTable edgeTable = network.getDefaultEdgeTable();
		if (edgeTable.getColumn(INTERACTION_ID) == null)
			edgeTable.createColumn(INTERACTION_ID, String.class, false);
		if (edgeTable.getColumn(INTERACTION_TYPE) == null) {
			edgeTable.createListColumn(INTERACTION_TYPE, String.class, false);
			edgeTable.createListColumn(INTERACTION_TYPE + ".name", String.class, false);
		}
		if (edgeTable.getColumn(DETECTION_METHOD) == null) {
			edgeTable.createListColumn(DETECTION_METHOD, String.class, false);
			edgeTable.createListColumn(DETECTION_METHOD + ".name", String.class, false);
		}
		if (edgeTable.getColumn(SOURCE_DB) == null)
			edgeTable.createListColumn(SOURCE_DB, String.class, false);
		if (edgeTable.getColumn(EDGE_SCORE) == null)
			edgeTable.createListColumn(EDGE_SCORE, Double.class, false);

		String line;
		final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream), BUFFER_SIZE);

		long interactionCount = 0;
		while ((line = br.readLine()) != null) {
			
			if(cancelFlag) {
				cleanup(br);
				return network;
			}
			
			// Ignore comment line
			if (line.startsWith("#"))
				continue;

			try {
				entry = line.split(TAB);

				// Validate entry list.
				if (entry == null || entry.length < COLUMN_COUNT)
					continue;

				sourceID = entry[0].split(SEPARATOR);
				targetID = entry[1].split(SEPARATOR);
				final String sourceRawID = sourceID[0].split(":")[1];
				final String targetRawID = targetID[0].split(":")[1];

				CyNode source = nodeMap.get(sourceRawID);
				if (source == null) {
					source = network.addNode();
					nodeMap.put(sourceRawID, source);
				}
				CyNode target = nodeMap.get(targetRawID);
				if (target == null) {
					target = network.addNode();
					nodeMap.put(targetRawID, target);
				}

				network.getRow(source).set(CyTableEntry.NAME, sourceRawID);
				network.getRow(target).set(CyTableEntry.NAME, targetRawID);

				// Set type if not protein
				if (sourceID[0].contains(CHEBI)) {
					network.getRow(source).set(INTERACTOR_TYPE, COMPOUND);
				}
				if (targetID[0].contains(CHEBI))
					network.getRow(target).set(INTERACTOR_TYPE, COMPOUND);

				// Aliases
				setAliases(network.getRow(source), entry[0].split(SEPARATOR));
				setAliases(network.getRow(target), entry[1].split(SEPARATOR));
				setAliases(network.getRow(source), entry[2].split(SEPARATOR));
				setAliases(network.getRow(target), entry[3].split(SEPARATOR));
				setAliases(network.getRow(source), entry[4].split(SEPARATOR));
				setAliases(network.getRow(target), entry[5].split(SEPARATOR));

				// Tax ID (pick first one only)
				setTaxID(network.getRow(source), entry[9].split(SEPARATOR)[0]);
				setTaxID(network.getRow(target), entry[10].split(SEPARATOR)[0]);

				sourceDB = entry[12].split(SEPARATOR);
				interactionID = entry[13].split(SEPARATOR);

				edgeScore = entry[14].split(SEPARATOR);

				detectionMethods = entry[6].split(SEPARATOR);
				interactionType = entry[11].split(SEPARATOR);

				final CyEdge e = network.addEdge(source, target, true);
				network.getRow(e).set(INTERACTION, interactionID[0]);

				setEdgeListAttribute(network.getRow(e), interactionType, INTERACTION_TYPE);
				setEdgeListAttribute(network.getRow(e), detectionMethods, DETECTION_METHOD);
				setEdgeListAttribute(network.getRow(e), sourceDB, SOURCE_DB);

				// Map scores
				setEdgeScoreListAttribute(network.getRow(e), edgeScore, EDGE_SCORE);

				network.getRow(e).set(INTERACTION_ID, interactionID[0]);

				setPublication(network.getRow(e), entry[8].split(SEPARATOR), entry[7].split(SEPARATOR));
				
//				interactionCount++;
//				taskMonitor.setStatusMessage(interactionCount + " interactions loaded.");
			} catch (Exception ex) {
				logger.warn("Could not parse this line: " + line, ex);
				continue;
			}
		}

		br.close();
		nodeMap.clear();
		nodeMap = null;

		logger.info("MITAB Parse finished in " + (System.currentTimeMillis() - start) + " msec.");
		
		return network;
	}

	private void setTaxID(CyRow row, String value) {
		String[] buf = value.split(":", 2);
		String attrName;
		String taxonName;
		if (buf != null && buf.length == 2) {
			attrName = ATTR_PREFIX + buf[0];

			if (row.getTable().getColumn(attrName) == null) {
				row.getTable().createColumn(attrName, String.class, false);
				row.getTable().createColumn(attrName + ".name", String.class, false);
			}

			matcher = miNamePttr.matcher(buf[1]);
			if (matcher.find()) {
				taxonName = matcher.group();
				row.set(attrName, buf[1].split("\\(")[0]);
				row.set(attrName + ".name", taxonName.substring(1, taxonName.length() - 1));
			} else {
				row.set(attrName, buf[1]);
			}
		}
	}

	private void setPublication(CyRow row, String[] pubID, String[] authors) {
		String key = null;
		String[] temp;

		for (String val : pubID) {
			temp = val.split(":", 2);
			if (temp == null || temp.length < 2)
				continue;

			key = ATTR_PREFIX + temp[0];
			listAttrMapper(row, key, temp[1]);
		}

		for (String val : authors) {
			key = ATTR_PREFIX + "author";
			listAttrMapper(row, key, val);
		}
	}

	private void setAliases(CyRow row, String[] entry) {
		String key = null;
		String[] temp;
		String value;

		for (String val : entry) {
			temp = val.split(":", 2);
			if (temp == null || temp.length < 2)
				continue;

			key = ATTR_PREFIX + temp[0];
			value = temp[1].replaceAll("\\(.+\\)", "");
			listAttrMapper(row, key, value);
		}
	}

	private void setEdgeListAttribute(CyRow row, String[] entry, String key) {

		String value;
		String name;

		for (String val : entry) {
			value = trimPSITerm(val);
			name = trimPSIName(val);

			listAttrMapper(row, key, value);
			listAttrMapper(row, key + ".name", name);
		}
	}

	// Special case for edge scores
	private void setEdgeScoreListAttribute(CyRow row, String[] entry, String key) {

		String scoreString;
		String scoreType;

		for (String val : entry) {
			final String[] parts = val.split(":");
			if (parts == null || parts.length != 2)
				continue;

			scoreString = parts[1];
			scoreType = parts[0];
			final String colName = key + "." + scoreType;

			if (row.getTable().getColumn(colName) == null)
				row.getTable().createListColumn(colName, Double.class, false);

			try {
				final Double score = Double.parseDouble(scoreString);
				row.set(key + "." + scoreType, score);
			} catch (Exception e) {
				// if (scoreString != null
				// && scoreString.trim().equals("") == false)
				// row.set(key + "." + scoreType, scoreString);

				continue;
			}
		}
	}

	private void listAttrMapper(CyRow row, String attrName, String value) {
		if (row.getTable().getColumn(attrName) == null)
			row.getTable().createListColumn(attrName, String.class, false);

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

	private String trimPSITerm(String original) {
		String miID = null;

		matcher = miPttr.matcher(original);

		if (matcher.find()) {
			miID = matcher.group();
		} else {
			miID = "-";
		}

		return miID;
	}

	private String trimPSIName(String original) {
		String miName = null;

		matcher = miNamePttr.matcher(original);

		if (matcher.find()) {
			miName = matcher.group();
			miName = miName.substring(1, miName.length() - 1);
		} else {
			miName = "-";
		}

		return miName;
	}
	
	public void cancel() {
		cancelFlag = true;
	}
	
	private void cleanup(Reader br) throws IOException {
		br.close();
		nodeMap.clear();
		nodeMap = null;
	}

}
