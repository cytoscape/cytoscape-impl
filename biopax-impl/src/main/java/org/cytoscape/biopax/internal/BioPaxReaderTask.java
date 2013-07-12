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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.biopax.paxtools.model.Model;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.biopax.internal.util.BioPaxUtil;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * GraphReader Implementation for BioPAX Files.
 *
 * @author Ethan Cerami.
 * @author Igor Rodchenkov (re-factoring, using PaxTools API)
 */
public class BioPaxReaderTask extends AbstractTask implements CyNetworkReader {
	
	public static final Logger log = LoggerFactory.getLogger(BioPaxReaderTask.class);
	public static final String CREATE_NEW_COLLECTION_STRING ="Create new network collection";

	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkNaming naming;
	

	private InputStream stream;
	private String inputName;
	private CyNetwork network;
	private CyRootNetwork rootNetwork;
	private final HashMap<String, CyRootNetwork> nameToRootNetworkMap;

	
	/**
	 * Constructor
	 * @param applicationManager 
	 * @param rootNetworkManager 
	 * @param networkManager 
	 * @param model PaxTools BioPAX Model
	 */
	public BioPaxReaderTask(InputStream stream, String inputName, 
			CyNetworkFactory networkFactory, CyNetworkViewFactory viewFactory, 
			CyNetworkNaming naming, CyNetworkManager networkManager, 
			CyRootNetworkManager rootNetworkManager, CyApplicationManager applicationManager) 
	{
		this.stream = stream;
		this.inputName = inputName;
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
		this.naming = naming;
		
		// initialize the network Collection
		nameToRootNetworkMap = new HashMap<String, CyRootNetwork>();
		for (CyNetwork net : networkManager.getNetworkSet()) {
			final CyRootNetwork rootNet = rootNetworkManager.getRootNetwork(net);
			if (!nameToRootNetworkMap.containsValue(rootNet ) )
				nameToRootNetworkMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
		}		
		List<String> rootNames = new ArrayList<String>();
		rootNames.add(CREATE_NEW_COLLECTION_STRING);
		rootNames.addAll(nameToRootNetworkMap.keySet());
		rootNetworkList = new ListSingleSelection<String>(rootNames);
		rootNetworkList.setSelectedValue(rootNames.get(0));		
		// set the default selection
		final List<CyNetwork> selectedNetworks = applicationManager.getSelectedNetworks();
		if (selectedNetworks != null && selectedNetworks.size() > 0){
			CyNetwork selectedNetwork = applicationManager.getSelectedNetworks().get(0);
			String rootName = "";
			if (selectedNetwork instanceof CySubNetwork){
				CySubNetwork subnet = (CySubNetwork) selectedNetwork;
				CyRootNetwork rootNet = subnet.getRootNetwork();
				rootName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
			} else {
				rootName = selectedNetwork.getRow(selectedNetwork).get(CyNetwork.NAME, String.class);
			}				
			rootNetworkList.setSelectedValue(rootName);
		}
	}

	
	public ListSingleSelection<String> rootNetworkList;
	
	@Tunable(description = "Network Collection" ,groups=" ")
	public ListSingleSelection<String> getRootNetworkList(){
		return rootNetworkList;
	}
	public void setRootNetworkList (ListSingleSelection<String> roots){
		rootNetworkList = roots;
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Create Network from BioPAX";
	}
	
	
	@Override
	public void cancel() {
	}
	
	
	public void run(TaskMonitor taskMonitor) throws Exception 
	{
		Model model = BioPaxUtil.read(stream);
		if(model == null) {
			log.error("Failed to read BioPAX model");
			return;
		}
		
		log.info("Model contains " + model.getObjects().size()
				+ " BioPAX elements");
		
// giving up this expensive normalization...
//		//normalize/infer properties: displayName, cellularLocation, organism, dartaSource
//		// a hack to skip running property reasoner for already normalized data...
//		if(model.getXmlBase() == null || !model.getXmlBase().contains("pathwaycommons")) {
//			BioPaxUtil.fixDisplayName(model);
//			ModelUtils.inferPropertiesFromParent(model, 
//					new HashSet<String>(Arrays.asList("dataSource", "organism", "cellularLocation")));
//		}
		
		// Map BioPAX Data to Cytoscape Nodes/Edges (run as task)
		BioPaxMapper mapper = new BioPaxMapper(model, networkFactory, taskMonitor);
		String networkName = getNetworkName(model);
		network = mapper.createCyNetwork(networkName, rootNetwork);
		
		if (network.getNodeCount() == 0) {
			log.error("Pathway is empty. Please check the BioPAX source file.");
		}
	}

	
	private String getNetworkName(Model model) {
		// make a network name from pathway name(s) or the file name
		String name = BioPaxUtil.getName(model);
		
		if(name == null || "".equalsIgnoreCase(name)) {
			name = inputName;
			if(log.isDebugEnabled())
				log.debug("Network name will be the file name: " + name);
		} else if(name.length() > 100) {
			if(log.isDebugEnabled())
				name = inputName + " - " + name.substring(0, 100);
				log.debug("Based on multiple pathways network name is too long; " +
					"it will be truncated: " + name);
		}
		
		// Take appropriate adjustments, if name already exists
		name = naming.getSuggestedNetworkTitle(StringEscapeUtils.unescapeHtml(name));
		
		if(log.isDebugEnabled())
			log.debug("New BioPAX network name is: " + name);
		
		return name;
	}


	@Override
	public CyNetwork[] getNetworks() {
		return new CyNetwork[]{network};
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		return viewFactory.createNetworkView(network);
		//there is a BioPAX Networks Tracker that listens to the network added events and sets the visual style, layout, etc...
	}

}
