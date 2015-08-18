package org.cytoscape.tableimport.internal.reader.ontology;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.ALT_ID;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.BROAD_SYNONYM;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.DEF;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.DISJOINT_FROM;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.EXACT_SYNONYM;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.ID;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.IS_A;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.IS_OBSOLETE;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.NAME;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.NARROW_SYNONYM;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.RELATED_SYNONYM;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.RELATIONSHIP;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.SUBSET;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.SYNONYM;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.XREF;
import static org.cytoscape.tableimport.internal.reader.ontology.OBOTags.XREF_ANALOG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.util.OntologyDAGManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBOReader extends AbstractTask implements CyNetworkReader {

	private static final Logger logger = LoggerFactory.getLogger(OBOReader.class);

	//private static final String[] COMPATIBLE_VERSIONS = { "1.2" };
	
	public static final String DAG_ATTR = "Ontology DAG";
	
	protected static final String TERM_NAME = "term name";

	public static final String OBO_PREFIX = "obo.";
	private static final String DEF_ORIGIN = "def_origin";
	protected static final String TERM_TAG = "[Term]";
	private List<String[]> interactionList;

	// DAG
	private CyNetwork ontologyDAG;
	private CyNetwork[] networks;

	private Map<String, String> header;
	
	private final InputStream inputStream;
	private final Map<String, CyNode> termID2nodeMap;
	private final String dagName;
	private final CyServiceRegistrar serviceRegistrar;

	public OBOReader(final String dagName, final InputStream oboStream, final CyServiceRegistrar serviceRegistrar) {
		this.inputStream = oboStream;
		this.dagName = dagName;
		this.serviceRegistrar = serviceRegistrar;

		termID2nodeMap = new HashMap<String, CyNode>();
		networks = new CyNetwork[1];
		interactionList = new ArrayList<String[]>();
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		BufferedReader bufRd = new BufferedReader(new InputStreamReader(inputStream));
		String line;

		// Create new DAG
		this.ontologyDAG = serviceRegistrar.getService(CyNetworkFactory.class).createNetwork();
		
		try {
			// Phase 1: read header information
			header = new HashMap<String, String>();
			
			while ((line = bufRd.readLine()) != null) {
				if (line.startsWith(TERM_TAG))
					break;
				else if (line.startsWith("!"))
					continue;
				else if (line.trim().length() != 0)
					parseHeader(line);
			}
			
			mapHeader();

			// Phase 2: read actual contents
			readEntry(bufRd);

		} finally {
			bufRd.close();
			inputStream.close();
		}

		buildEdge();

		networks[0] = this.ontologyDAG;
		
		OntologyDAGManager.addOntologyDAG(dagName, ontologyDAG);
		logger.debug("Number of terms loaded = " + this.termID2nodeMap.size());
		termID2nodeMap.clear();
	}

	private void parseHeader(final String line) {
		final int colonInx = line.indexOf(':');

		if (colonInx == -1)
			return;

		final String key = line.substring(0, colonInx).trim();
		final String val = line.substring(colonInx + 1).trim();
		header.put(key, val);
	}
	
	private void mapHeader() {
		final CyTable networkTable = this.ontologyDAG.getDefaultNetworkTable();

		for (String tag : header.keySet()) {
			if (networkTable.getColumn(tag) == null)
				networkTable.createColumn(tag, String.class, false);

			networkTable.getRow(ontologyDAG.getSUID()).set(tag, header.get(tag));
		}
		
		if(networkTable.getColumn(DAG_ATTR) == null)
			networkTable.createColumn(DAG_ATTR, Boolean.class, true);
		
		networkTable.getRow(ontologyDAG.getSUID()).set(DAG_ATTR, true);
		ontologyDAG.getRow(ontologyDAG).set(CyNetwork.NAME, dagName);
	}

	private void readEntry(final BufferedReader rd) throws IOException {
		String id = "";
		String line = null;

		String key;
		String val;

		int colonInx;

		String[] definitionParts;
		String[] synonymParts;
		String[] entry;
		String targetId;
		Map<String, String> synoMap;

		CyNode termNode = null;
		boolean termSection = true;
		while ((line = rd.readLine()) != null) {
			line = line.trim();

			if (line.length() == 0 || line.startsWith("!"))
				continue;

			// Tag?
			if (line.startsWith("[")) {
				if (line.startsWith(TERM_TAG) == false) {
					termSection = false;
					continue;
				} else {
					termSection = true;
				}
			}

			if (!termSection)
				continue;

			colonInx = line.indexOf(':');
			if (colonInx == -1)
				continue;

			key = line.substring(0, colonInx).trim();
			val = line.substring(colonInx + 1).trim();

			if (key.equals(ID.toString())) {
				// Create node for this term.
				termNode = termID2nodeMap.get(val);
				if (termNode == null) {
					termNode = this.ontologyDAG.addNode();
					ontologyDAG.getRow(termNode).set(CyNetwork.NAME, val);
					termID2nodeMap.put(val, termNode);
					id = val;					
				}
			} else if (key.equals(NAME.toString())) {
				// Name column is used in Cytoscape core, so use different tag instead.
				if (ontologyDAG.getRow(termNode).getTable().getColumn(TERM_NAME) == null)
					ontologyDAG.getRow(termNode).getTable().createColumn(TERM_NAME, String.class, true);
				ontologyDAG.getRow(termNode).set(TERM_NAME, val);
			} else if (key.equals(DEF.toString())) {
				definitionParts = val.split("\"");
				
				if (ontologyDAG.getRow(termNode).getTable().getColumn(key) == null)
					ontologyDAG.getRow(termNode).getTable().createColumn(key, String.class, true);
				ontologyDAG.getRow(termNode).set(key, definitionParts[1]);
				
				final List<String> originList = getReferences(val.substring(definitionParts[1].length() + 2));
				
				if (originList != null) {
					if (ontologyDAG.getRow(termNode).getTable().getColumn(DEF_ORIGIN) == null)
						ontologyDAG.getRow(termNode).getTable().createListColumn(DEF_ORIGIN, String.class, true);
					ontologyDAG.getRow(termNode).set(DEF_ORIGIN, originList);
				}
			} else if (key.equals(EXACT_SYNONYM.toString()) || key.equals(RELATED_SYNONYM.toString())
					|| key.equals(BROAD_SYNONYM.toString()) || key.equals(NARROW_SYNONYM.toString())
					|| key.equals(SYNONYM.toString())) {
				synonymParts = val.split("\"");

				if (ontologyDAG.getRow(termNode).getTable().getColumn(key) == null)
					ontologyDAG.getRow(termNode).getTable().createListColumn(key, String.class, true);
				
				List<String> listAttr = ontologyDAG.getRow(termNode).getList(key, String.class);

				if (listAttr == null)
					listAttr = new ArrayList<String>();

				listAttr.add(synonymParts[1]);

				ontologyDAG.getRow(termNode).set(key, listAttr);
			} else if (key.equals(RELATIONSHIP.toString())) {
				entry = val.split(" ");
				final String[] itr = new String[3];
				itr[0] = id;
				itr[1] = entry[1];
				itr[2] = entry[0];
				interactionList.add(itr);
			} else if (key.equals(IS_A.toString())) {

				int colonidx = val.indexOf('!');

				if (colonidx == -1)
					targetId = val.trim();
				else
					targetId = val.substring(0, colonidx).trim();

				final String[] itr = new String[3];
				itr[0] = id;
				itr[1] = targetId;
				itr[2] = "is_a";
				interactionList.add(itr);
			} else if (key.equals(IS_OBSOLETE.toString())) {
				if (ontologyDAG.getRow(termNode).getTable().getColumn(key) == null)
					ontologyDAG.getRow(termNode).getTable().createColumn(key, Boolean.class, true);
				try {
					ontologyDAG.getRow(termNode).set(key, Boolean.parseBoolean(val));
				} catch(Exception e) {
					// Ignore invalid entries.
					continue;
				}
			} else if (key.equals(XREF.toString()) || key.equals(XREF_ANALOG.toString())
					|| key.equals(ALT_ID.toString()) || key.equals(SUBSET.toString())
					|| key.equals(DISJOINT_FROM.toString())) {

				if (ontologyDAG.getRow(termNode).getTable().getColumn(key) == null)
					ontologyDAG.getRow(termNode).getTable().createListColumn(key, String.class, true);

				List<String> listAttr = ontologyDAG.getRow(termNode).getList(key, String.class);
				if (listAttr == null)
					listAttr = new ArrayList<String>();

				if (val != null) {
					if (key.equals(DISJOINT_FROM.toString())) {
						listAttr.add(val.split("!")[0].trim());
					} else
						listAttr.add(val);
				}
				ontologyDAG.getRow(termNode).set(key, listAttr);
			} else {
				// Others will be stored as a string attr.
				if (ontologyDAG.getRow(termNode).getTable().getColumn(key) == null)
					ontologyDAG.getRow(termNode).getTable().createColumn(key, String.class, true);
				ontologyDAG.getRow(termNode).set(key, val);
			}
		}
	}

	private void buildEdge() {
		for (String[] entry : this.interactionList) {
			CyEdge edge = ontologyDAG.addEdge(termID2nodeMap.get(entry[0]), termID2nodeMap.get(entry[1]), true);
			ontologyDAG.getRow(edge).set(CyEdge.INTERACTION, entry[2]);
		}
		interactionList.clear();
	}

	private List<String> getReferences(String list) {
		String trimed = list.trim();
		trimed = trimed.substring(trimed.indexOf("[") + 1, trimed.indexOf("]"));

		if (trimed.length() == 0) {
			return null;
		} else {
			List<String> entries = new ArrayList<String>();

			for (String entry : trimed.split(",")) {
				entries.add(entry.trim());
			}

			return entries;
		}
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork net) {
		final CyNetworkViewFactory netViewFactory = serviceRegistrar.getService(CyApplicationManager.class)
				.getDefaultNetworkViewRenderer().getNetworkViewFactory();
		final CyNetworkView view = netViewFactory.createNetworkView(ontologyDAG);
		
		return view;
	}

	@Override
	public CyNetwork[] getNetworks() {
		return networks;
	}
}
