package org.cytoscape.biopax.internal;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.EntityReference;
import org.cytoscape.biopax.internal.util.AttributeUtil;
import org.cytoscape.biopax.internal.util.BioPaxReaderError;
import org.cytoscape.biopax.internal.util.VisualStyleUtil;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * BioPAX File / InputStream Reader Implementation.
 *
 * @author Ethan Cerami.
 * @author Igor Rodchenkov (re-factoring, using PaxTools API, Cytoscape 3)
 */
public class BioPaxReaderTask extends AbstractTask implements CyNetworkReader {
	
	private static final Logger log = LoggerFactory.getLogger(BioPaxReaderTask.class);
	
	private static final String CREATE_NEW_COLLECTION = "A new network collection";

	private final HashMap<String, CyRootNetwork> nameToRootNetworkMap;
	private final VisualStyleUtil visualStyleUtil;
	private final CyServices cyServices;

	private InputStream stream;
	private String inputName;
	private final Collection<CyNetwork> networks;
	private CyRootNetwork rootNetwork;	
	private CyNetworkReader anotherReader;
	
	/**
	 * BioPAX parsing/converting options.
	 * 
	 * @author rodche
	 *
	 */
	private static enum ReaderMode {
		/**
		 * Default BioPAX to Cytoscape network/view mapping: 
		 * entity objects (sub-classes, including interactions too) 
		 * will be CyNodes interconnected by edges that 
		 * correspond to biopax properties with Entity type domain and range; 
		 * some of dependent utility class objects and simple properties are used to
		 * generate node attributes.
		 */
		DEFAULT,
		
		/**
		 * BioPAX to SIF, and then to Cytoscape mapping:
		 * first, it converts BioPAX to SIF (using Paxtools library); next, 
		 * delegates network/view creation to the first available SIF anotherReader.
		 */
		SIF,
		
		/**
		 * BioPAX to SBGN, and then to Cytoscape network/view mapping:
		 * converts BioPAX to SBGN-ML (using Paxtools library); next, 
		 * delegates network/view creation to the first available SBGN anotherReader,
		 * e.g., CySBGN (if present).
		 */
		SBGN;
		
		static String[] names() {
			ReaderMode vals[] = ReaderMode.values();
			String names[] = new String[vals.length];
			for(int i= 0; i < vals.length; i++)
				names[i] = vals[i].toString();
			return names;
		}
	}

	
	@ProvidesTitle()
	public String tunableDialogTitle() {
		return "BioPAX Reader Task";
	}
	
	@Tunable(description = "Model mapping", groups = {"Options"}, 
			tooltip="<html>Choose how to read BioPAX:" +
					"<ul>" +
					"<li><strong>DEFAULT</strong>: map states, interactions to nodes; properties - to edges, attributes;</li>"+
					"<li><strong>SIF</strong>: convert BioPAX to SIF, use a SIF reader, add attributes;</li>" +
					"<li><strong>SBGN</strong>: convert BioPAX to SBGN, find a SBGN reader, etc.</li>" +
					"</ul></html>"
			, gravity=500, xorChildren=true)
	public ListSingleSelection<String> readerMode;
		
	@Tunable(description = "Networks collection" , groups = {"Options","DEFAULT"}, tooltip="Choose a root network", 
			dependsOn="readerMode=DEFAULT", 
			gravity=700, xorKey="DEFAULT")
	public ListSingleSelection<String> rootNetworkSelection;

	//TODO select inference rules (multi-selection) for the SIF converter
	//TODO migrate from sif-converter to new biopax pattern module
	@Tunable(description = "Binary interactions to infer" , groups = {"Options","SIF"}, tooltip="Select inference rules", 
			gravity=701, xorKey="SIF")
	public ListMultipleSelection<String> sifSelection;
	
	//TODO init SBGN options if required
	@Tunable(description = "SBGN options" , groups = {"Options","SBGN"}, tooltip="Currently not available", 
			gravity=701, xorKey="SBGN")
	public ListSingleSelection<String> sbgnSelection;
	
	
	/**
	 * Constructor
	 * 
	 * @param stream
	 * @param inputName a file or pathway name (can be later updated using actual data)
	 * @param cyServices
	 */
	public BioPaxReaderTask(InputStream stream, String inputName, 
			CyServices cyServices, VisualStyleUtil visualStyleUtil) 
	{
		this.networks = new HashSet<CyNetwork>();
		this.stream = stream;
		this.inputName = inputName;
		this.cyServices = cyServices;
		this.visualStyleUtil = visualStyleUtil;
		
		// initialize the root networks Collection
		nameToRootNetworkMap = new HashMap<String, CyRootNetwork>();
		for (CyNetwork net : cyServices.networkManager.getNetworkSet()) {
			final CyRootNetwork rootNet = cyServices.rootNetworkManager.getRootNetwork(net);
			if (!nameToRootNetworkMap.containsValue(rootNet))
				nameToRootNetworkMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
		}		
		List<String> rootNames = new ArrayList<String>();
		rootNames.add(CREATE_NEW_COLLECTION);
		rootNames.addAll(nameToRootNetworkMap.keySet());
		rootNetworkSelection = new ListSingleSelection<String>(rootNames);
		rootNetworkSelection.setSelectedValue(CREATE_NEW_COLLECTION);
		
		readerMode = new ListSingleSelection<String>(ReaderMode.names());
		readerMode.setSelectedValue(ReaderMode.DEFAULT.toString());	
		
		sifSelection = new ListMultipleSelection<String>();
		sbgnSelection = new ListSingleSelection<String>();
	}
	
	
	public void run(TaskMonitor taskMonitor) throws Exception 
	{
		taskMonitor.setTitle("BioPAX reader");
		taskMonitor.setProgress(0.0);
		
		// import BioPAX data into a new in-memory model
		Model model = null;
		try {
			model = BioPaxMapper.read(stream);
		} catch (Throwable e) {
			throw new BioPaxReaderError("BioPAX reader failed to build a BioPAX model " +
					"(check the data for syntax errors) - " + e);
		}
		
		if(model == null) {
			throw new BioPaxReaderError("BioPAX reader did not find any BioPAX data there.");
		}
		
		final String networkName = getNetworkName(model);
		String msg = "Model " + networkName + " contains " 
				+ model.getObjects().size() + " BioPAX elements";
		log.info(msg);
		taskMonitor.setStatusMessage(msg);
		
		//set parent/root network (can be null - add a new networks group)
		rootNetwork = nameToRootNetworkMap.get(rootNetworkSelection.getSelectedValue());
		
		final BioPaxMapper mapper = new BioPaxMapper(model, cyServices.networkFactory);
			
		ReaderMode selectedMode = ReaderMode.valueOf(readerMode.getSelectedValue());
		switch (selectedMode) {
		case DEFAULT:
			anotherReader = null;
			// Map BioPAX Data to Cytoscape Nodes/Edges (run as task)
			taskMonitor.setStatusMessage("Mapping BioPAX model to CyNetwork...");
			CyNetwork network = mapper.createCyNetwork(networkName, rootNetwork);
			if (network.getNodeCount() == 0)
				throw new BioPaxReaderError("Pathway is empty. Please check the BioPAX source file.");
			// set the biopax network mapping type for other plugins
			AttributeUtil.set(network, network, BioPaxMapper.BIOPAX_NETWORK, "DEFAULT", String.class);
			//(the network name attr. was already set by the biopax mapper)
			
			//register the network
			networks.add(network);
			break;
		case SIF:
			//convert to EXTENDED SIF
			taskMonitor.setStatusMessage("Mapping BioPAX model to SIF, then to " +
					"CyNetwork (using the first discovered SIF reader)...");
			final File sifEdgesFile = File.createTempFile("tmp_biopax2sif_edges", ".sif");
			sifEdgesFile.deleteOnExit();
			final File sifNodesFile = File.createTempFile("tmp_biopax2sif_nodes", ".sif");
			sifNodesFile.deleteOnExit();
			//TODO change - to use only selected (via tunables dialog) sif rules
			BioPaxMapper.convertToExtendedBinarySIF(model, 
					new FileOutputStream(sifEdgesFile), new FileOutputStream(sifNodesFile), null);
			//TODO option: generate and use blacklist
			// try to discover a SIF reader and pass the data there
			anotherReader =  cyServices.networkViewReaderManager.getReader(sifEdgesFile.toURI(), networkName);		
			if(anotherReader != null) {
				final Model m = model;
				insertTasksAfterCurrentTask(
					anotherReader, 
					new AbstractTask() {
					@Override
					public void run(TaskMonitor taskMonitor) throws Exception {
						taskMonitor.setTitle("BioPAX reader");
						taskMonitor.setStatusMessage("Creating node attributes from BioPAX properties...");
						CyNetwork[] cyNetworks = anotherReader.getNetworks();
						for (CyNetwork net : cyNetworks) {	
							//create attributes from biopax properties
							createBiopaxSifAttributes(m, net, sifNodesFile, taskMonitor);
							// set the biopax network mapping type for other plugins
							AttributeUtil.set(net, net, BioPaxMapper.BIOPAX_NETWORK, "SIF", String.class);
							//set the network name (very important!)
							AttributeUtil.set(net, net, CyNetwork.NAME, networkName, String.class);
							//register the network
							networks.add(net);	
							taskMonitor.setStatusMessage("SIF network updated...");
						}
					}
				})
				;				
			} else {
				//fail with a message
				throw new BioPaxReaderError("No SIF readers found in the Cytoscape framework.");
			}
			
			break;
		case SBGN:
			//convert to SBGN
			taskMonitor.setStatusMessage("Mapping BioPAX model to SBGN, " +
					"then to CyNetwork (using the first discovered SBGN reader)...");
			File sbgnFile = File.createTempFile("tmp_biopax", ".sbgn.xml");
			sbgnFile.deleteOnExit(); 
			BioPaxMapper.convertToSBGN(model, new FileOutputStream(sbgnFile));
			// try to discover a SBGN reader to pass the xml data there
			anotherReader =  cyServices.networkViewReaderManager.getReader(sbgnFile.toURI(), networkName);
			if(anotherReader != null) {				
				insertTasksAfterCurrentTask(
					anotherReader, 
					new AbstractTask() {
					@Override
					public void run(TaskMonitor taskMonitor) throws Exception {
						taskMonitor.setTitle("BioPAX reader");
						taskMonitor.setStatusMessage("Updating attributess...");
						for (CyNetwork network : anotherReader.getNetworks()) {	
							//TODO create attributes from biopax properties (depends on actual SBGN reader, if any) ?
							// set the biopax network mapping type for other plugins
							AttributeUtil.set(network, network, BioPaxMapper.BIOPAX_NETWORK, "SBGN", String.class);	
							// set the network name attribute!
							AttributeUtil.set(network, network, CyNetwork.NAME, networkName, String.class);
							//register it
							networks.add(network);							
						}
						taskMonitor.setProgress(1.0);
					}
				})
				;
			} else {
				//fail with a message
				throw new BioPaxReaderError("No SBGN readers found, or BioPAX to SBGN " +
						"conversion failed (check " + sbgnFile.getAbsolutePath());
			}
			break;
		default:
			break;
		}
	}

	
	private void createBiopaxSifAttributes(Model model, CyNetwork cyNetwork, 
			File sifNodes, TaskMonitor taskMonitor) throws IOException {
			
		taskMonitor.setStatusMessage("Updating SIF network " +
				"node/edge attributes from the BioPAX model...");
		
		//parse the extended sif nodes file into map: "URI" -> other attributes (as a single TSV string)
		Map<String, String> uriToDescriptionMap = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new FileReader(sifNodes));
		while(reader.ready()) {
			String line = reader.readLine();
			if(line.trim().isEmpty())
				continue; //skip blank lines if any accidentally present there
			//columns are: URI\tTYPE\tNAME\tUnifXrefs(semicolon-separated)
			String[] cols = line.split("\t");			
			assert cols.length == 4 : "BUG: unexpected number of columns (" +
					cols.length + "; must be 4) in the SIF file: " + sifNodes.getAbsolutePath();
			// put into the map
			uriToDescriptionMap.put(cols[0], 
				StringUtils.join(ArrayUtils.remove(cols, 0), '\t'));
		}
		reader.close();
		

		// Set the Quick Find Default Index
		AttributeUtil.set(cyNetwork, cyNetwork, "quickfind.default_index", CyNetwork.NAME, String.class);

		if (cancelled) return;

		// Set node/edge attributes from the Biopax Model
		for (CyNode node : cyNetwork.getNodeList()) {
			String uri = cyNetwork.getRow(node).get(CyNetwork.NAME, String.class);
			BioPAXElement e = model.getByID(uri);// can be null (for generic/group nodes)
			if(e instanceof EntityReference || e instanceof Entity) 
			{
				//note: in fact, SIF formatted data contains only ERs, PEs (no sub-classes), and Complexes / Generics.
				BioPaxMapper.createAttributesFromProperties(e, model, node, cyNetwork);
			} else if (e != null){
				log.warn("SIF network has an unexpected node: " + uri 
						+ " of type " + e.getModelInterface());
				BioPaxMapper.createAttributesFromProperties(e, model, node, cyNetwork);
			} else { //e == null; the URI/ID was auto-generated by the sif-converter and not present in the model
				AttributeUtil.set(cyNetwork, node, BioPaxMapper.BIOPAX_URI, uri, String.class);				
				//set other attributes from the tmp_biopax2sif_nodes*.sif file
				String sifNodeAttrs = uriToDescriptionMap.get(uri);
				assert (sifNodeAttrs != null && !sifNodeAttrs.isEmpty()) : "Bug: no SIF attributes found for " + uri;
				String[] cols = sifNodeAttrs.split("\t");
				AttributeUtil.set(cyNetwork, node, BioPaxMapper.BIOPAX_ENTITY_TYPE, cols[0], String.class);
				AttributeUtil.set(cyNetwork, node, CyRootNetwork.SHARED_NAME, cols[1], String.class);
				AttributeUtil.set(cyNetwork, node, CyNetwork.NAME, cols[1], String.class);
				if(cols.length > 2) { //no xrefs is possible for some generic nodes
					List<String> xrefs = Arrays.asList(cols[2].split(";"));
					AttributeUtil.set(cyNetwork, node, 
							BioPaxMapper.BIOPAX_RELATIONSHIP, xrefs, String.class);
					AttributeUtil.set(cyNetwork, node, CyNetwork.HIDDEN_ATTRS, 
							BioPaxMapper.BIOPAX_RELATIONSHIP_REFERENCES, xrefs, String.class);
				}
			}
		}
	}


	private String getNetworkName(Model model) {
		// make a network name from pathway name(s) or the file name
		String name = BioPaxMapper.getName(model);
		
		if(name == null || name.trim().isEmpty()) {
			name = (inputName == null || inputName.trim().isEmpty()) 
				? "BioPAX_Network"  : inputName;
		} else {
			int l = (name.length()<100) ? name.length() : 100;
			name = (inputName == null || inputName.trim().isEmpty()) 
				? name.substring(0, l)
				: inputName; //preferred
		}
		
		// Take appropriate adjustments, if name already exists
		name = cyServices.naming.getSuggestedNetworkTitle(
			StringEscapeUtils.unescapeHtml(name) + 
			" (" + readerMode.getSelectedValue() + ")");
		
		log.info("New BioPAX network name is: " + name);
		
		return name;
	}


	@Override
	public CyNetwork[] getNetworks() {
		return networks.toArray(new CyNetwork[] {});
	}

	
	/* Looks, unless called directly, this runs once the view is created 
	 * for the first time, i.e., after the network is imported from a biopax file/stream 
	 * (so it's up to the user or another app. then to apply custom style/layout to 
	 * new view, should the first one is destroyed and new one created.
	 */
	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		
		CyNetworkView view;		
		//visual style depends on the tunable
//		VisualStyle style = null; 		
		ReaderMode currentMode = ReaderMode.valueOf(readerMode.getSelectedValue());
		switch (currentMode) {
		case DEFAULT:
//			style = visualStyleUtil.getBioPaxVisualStyle();
			view = cyServices.networkViewFactory.createNetworkView(network);
			break;
		case SIF:
//			style = visualStyleUtil.getBinarySifVisualStyle();
			view = anotherReader.buildCyNetworkView(network);
			break;
		case SBGN:
		default:
			view = anotherReader.buildCyNetworkView(network);
			//TODO: a layout for SBGN views (if not already done)? 
			break;
		}

		if(!cyServices.networkViewManager.getNetworkViews(network).contains(view))
			cyServices.networkViewManager.addNetworkView(view);
		
		return view;
	}

}
