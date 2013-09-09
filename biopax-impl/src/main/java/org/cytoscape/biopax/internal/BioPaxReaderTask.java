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

import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringEscapeUtils;
import org.biopax.paxtools.model.Model;
import org.cytoscape.biopax.internal.util.AttributeUtil;
import org.cytoscape.biopax.internal.util.VisualStyleUtil;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * BioPAX File / InputStream Reader Implementation.
 *
 * @author Ethan Cerami.
 * @author Igor Rodchenkov (re-factoring, using PaxTools API)
 */
public class BioPaxReaderTask extends AbstractTask implements CyNetworkReader {
	
	private static final Logger log = LoggerFactory.getLogger(BioPaxReaderTask.class);
	
	private static final String CREATE_NEW_COLLECTION ="Create new network collection";

	private final HashMap<String, CyRootNetwork> nameToRootNetworkMap;
	private final VisualStyleUtil visualStyleUtil;
	private final CyServices cyServices;

	private InputStream stream;
	private String inputName;
	private CyNetwork network;
	private CyRootNetwork rootNetwork;
	
	/*
	 * BioPAX parsing/converting options.
	 * 
	 * @author rodche
	 *
	 */
	public static enum ReaderMode {
		/**
		 * Default BioPAX to Cytoscape network/view mapping: 
		 * entity objects (sub-classes, including interactions too) 
		 * will be CyNodes interconnected by edges that 
		 * correspond to biopax properties with Entity type domain and range; 
		 * some of dependent utility class objects and simple properties are used to
		 * generate node attributes.
		 */
		DEFAULT("BioPAX states,interactions -> nodes; BioPAX properties -> edges/attributes"),
		
		/**
		 * BioPAX to SIF, and then to Cytoscape mapping:
		 * first, it converts BioPAX to SIF (using Paxtools library); next, 
		 * delegates network/view creation to the first available SIF reader.
		 */
		SIF("BioPAX to SIF first, add attributes"),
		
		/**
		 * BioPAX to SBGN, and then to Cytoscape network/view mapping:
		 * converts BioPAX to SBGN-ML (using Paxtools library); next, 
		 * delegates network/view creation to the first available SBGN reader,
		 * e.g., CySBGN (if present).
		 */
		SBGN("BioPAX to SBGN first, add attributes");
		
		public final String descr;
		
		private ReaderMode(String descr) {
			this.descr = descr;
		}
		
		@Override
		public String toString() {
			return name() + ": " + descr;
		}
	}
	
	
	@Tunable(description = "Target network collection" , groups = "Options")
	public ListSingleSelection<String> rootNetworkSelection;

	
	@Tunable(description = "Model mapping", groups = "Options")
	public ListSingleSelection<ReaderMode> readerModeSelection;

	
	@Tunable(description = "Apply a BioPAX-aware visual style?", groups = "Options")
	public boolean applyVisualStyle = true;
	
	@Tunable(description = "Use the force-directed layout?", groups = "Options")
	public boolean applyLayout = true;
	
	
	@ProvidesTitle
	public String getTitle() {
		return "Create a Network from BioPAX";
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param stream
	 * @param inputName
	 * @param cyServices
	 */
	public BioPaxReaderTask(InputStream stream, String inputName, 
			CyServices cyServices, VisualStyleUtil visualStyleUtil) 
	{
		this.stream = stream;
		this.inputName = inputName;
		this.cyServices = cyServices;
		this.visualStyleUtil = visualStyleUtil;
		
		// initialize the network Collection
		nameToRootNetworkMap = new HashMap<String, CyRootNetwork>();
		for (CyNetwork net : cyServices.networkManager.getNetworkSet()) {
			final CyRootNetwork rootNet = cyServices.rootNetworkManager.getRootNetwork(net);
			if (!nameToRootNetworkMap.containsValue(rootNet ) )
				nameToRootNetworkMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
		}		
		List<String> rootNames = new ArrayList<String>();
		rootNames.add(CREATE_NEW_COLLECTION);
		rootNames.addAll(nameToRootNetworkMap.keySet());
		rootNetworkSelection = new ListSingleSelection<String>(rootNames);
		rootNetworkSelection.setSelectedValue(CREATE_NEW_COLLECTION);		
		
		// guess default target network group (root) from the first of currently selected network
//		final List<CyNetwork> selectedNetworks = cyServices.applicationManager.getSelectedNetworks();
//		if (selectedNetworks != null && !selectedNetworks.isEmpty()){
//			CyNetwork selectedNetwork = cyServices.applicationManager.getSelectedNetworks().get(0);
//			String rootName = "";
//			if (selectedNetwork instanceof CySubNetwork){
//				CySubNetwork subnet = (CySubNetwork) selectedNetwork;
//				CyRootNetwork rootNet = subnet.getRootNetwork();
//				rootName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
//			} else {
//				rootName = selectedNetwork.getRow(selectedNetwork).get(CyNetwork.NAME, String.class);
//			}				
//			rootNetworkList.setSelectedValue(rootName);
//		}
		
		readerModeSelection = new ListSingleSelection<ReaderMode>(ReaderMode.values());
		readerModeSelection.setSelectedValue(ReaderMode.DEFAULT);
	}
	
	
	@Override
	public void cancel() {
	}
	
	
	public void run(TaskMonitor taskMonitor) throws Exception 
	{
		Model model = BioPaxMapper.read(stream);
		if(model == null) {
			log.error("Failed to read BioPAX model");
			return;
		}
		
		String networkName = getNetworkName(model);
		log.info("Model " + networkName + " contains " 
				+ model.getObjects().size() + " BioPAX elements");
			
		switch (readerModeSelection.getSelectedValue()) {
		case DEFAULT:
			// Map BioPAX Data to Cytoscape Nodes/Edges (run as task)
			BioPaxMapper mapper = new BioPaxMapper(model, cyServices.networkFactory, taskMonitor);
			network = mapper.createCyNetwork(networkName, rootNetwork);
			// set the biopax network mapping type for other plugins
			AttributeUtil.set(network, network, BioPaxMapper.BIOPAX_NETWORK, "DEFAULT", String.class);
			break;
		case SIF:
			//TODO convert, delegate to a SIF converter
			// set the biopax network mapping type for other plugins
			AttributeUtil.set(network, network, BioPaxMapper.BIOPAX_NETWORK, "SIF", String.class);
			//TODO create node attributes from biopax properties
			break;
		case SBGN:
			//TODO convert, delegate to a SBGN converter
			// set the biopax network mapping type for other plugins
			AttributeUtil.set(network, network, BioPaxMapper.BIOPAX_NETWORK, "SBGN", String.class);
			//TODO create node attributes from biopax properties
			break;
		default:
			break;
		}
		
		if (network.getNodeCount() == 0) {
			log.error("Pathway is empty. Please check the BioPAX source file.");
		}
	}

	
	private String getNetworkName(Model model) {
		// make a network name from pathway name(s) or the file name
		String name = BioPaxMapper.getName(model);
		
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
		name = cyServices.naming.getSuggestedNetworkTitle(StringEscapeUtils.unescapeHtml(name));
		
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
		final CyNetworkView view = cyServices.networkViewFactory.createNetworkView(network);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				if(applyVisualStyle) { //optionally apply style
					//visual style depends on the tunable
					VisualStyle style = null; 				
					switch (readerModeSelection.getSelectedValue()) {
					case DEFAULT:
						style= visualStyleUtil.getBioPaxVisualStyle();
						break;
					case SIF:
						style= visualStyleUtil.getBinarySifVisualStyle();
						break;
					default:
						break;
					}

					if(style != null) {
						cyServices.mappingManager.setVisualStyle(style, view);
						style.apply(view);
					}
				}
				
				if(applyLayout) {
					// do layout
					CyLayoutAlgorithm layout = cyServices.layoutManager.getLayout("force-directed");
					if (layout == null) {
						layout = cyServices.layoutManager.getLayout(CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME);
						log.warn("'force-directed' layout not found; will use the default one.");
					}
					cyServices.taskManager.execute(layout.createTaskIterator(view, 
							layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS,""));
				}
				
				view.updateView();
			} 
		});
		
		return view;
	}

}
