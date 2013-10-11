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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringEscapeUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.util.BioPaxIOException;
import org.cytoscape.biopax.internal.util.AttributeUtil;
import org.cytoscape.biopax.internal.util.VisualStyleUtil;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
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
 * @author Igor Rodchenkov (re-factoring, using PaxTools API, Cytoscape 3)
 */
public class BioPaxReaderTask extends AbstractTask implements CyNetworkReader {
	
	private static final Logger log = LoggerFactory.getLogger(BioPaxReaderTask.class);
	
	private static final String CREATE_NEW_COLLECTION ="Create new network collection";

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
	public static enum ReaderMode {
		/**
		 * Default BioPAX to Cytoscape network/view mapping: 
		 * entity objects (sub-classes, including interactions too) 
		 * will be CyNodes interconnected by edges that 
		 * correspond to biopax properties with Entity type domain and range; 
		 * some of dependent utility class objects and simple properties are used to
		 * generate node attributes.
		 */
		DEFAULT("States,interactions ->nodes; properties ->edges,attributes"),
		
		/**
		 * BioPAX to SIF, and then to Cytoscape mapping:
		 * first, it converts BioPAX to SIF (using Paxtools library); next, 
		 * delegates network/view creation to the first available SIF anotherReader.
		 */
		SIF("BioPAX to SIF"),
		
		/**
		 * BioPAX to SBGN, and then to Cytoscape network/view mapping:
		 * converts BioPAX to SBGN-ML (using Paxtools library); next, 
		 * delegates network/view creation to the first available SBGN anotherReader,
		 * e.g., CySBGN (if present).
		 */
		SBGN("BioPAX to SBGN");
		
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

	
	@Tunable(description = "Apply BioPAX/SIF visual style and force-directed layout?", groups = "Options")
	public boolean applyStyle = true;
	
	
	@ProvidesTitle
	public String getTitle() {
		return "Import Network from BioPAX";
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
		this.networks = new HashSet<CyNetwork>();
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
		
		readerModeSelection = new ListSingleSelection<ReaderMode>(ReaderMode.values());
		readerModeSelection.setSelectedValue(ReaderMode.DEFAULT);
	}
	
	
	@Override
	public void cancel() {
	}
	
	
	public void run(TaskMonitor taskMonitor) throws Exception 
	{
		taskMonitor.setTitle("BioPAX reader");
		taskMonitor.setProgress(0.0);
		
		// import BioPAX data into a new in-memory model
		final Model model = BioPaxMapper.read(stream);
		
		if(model == null) {
			log.error("Failed to read BioPAX model");
			return;
		}
		
		final String networkName = getNetworkName(model);
		log.info("Model " + networkName + " contains " 
				+ model.getObjects().size() + " BioPAX elements");
		
		//set parent/root network (can be null - add a new networks group)
		rootNetwork = nameToRootNetworkMap.get(rootNetworkSelection.getSelectedValue());
		
		final BioPaxMapper mapper = new BioPaxMapper(model, cyServices.networkFactory);
			
		switch (readerModeSelection.getSelectedValue()) {
		case DEFAULT:
			anotherReader = null;
			// Map BioPAX Data to Cytoscape Nodes/Edges (run as task)
			taskMonitor.setStatusMessage("Mapping BioPAX model to CyNetwork...");
			CyNetwork network = mapper.createCyNetwork(networkName, rootNetwork);
			if (network.getNodeCount() == 0)
				throw new BioPaxIOException("Pathway is empty. Please check the BioPAX source file.");
			// set the biopax network mapping type for other plugins
			AttributeUtil.set(network, network, BioPaxMapper.BIOPAX_NETWORK, "DEFAULT", String.class);
			networks.add(network);
//			cyServices.networkManager.addNetwork(network); //TODO required or not?
			break;
		case SIF:
			//convert to SIF
			taskMonitor.setStatusMessage("Mapping BioPAX model to SIF, then to " +
					"CyNetwork (using the first discovered SIF reader)...");
			File sifFile = File.createTempFile("tmp_biopax", ".sif");
			sifFile.deleteOnExit();
			BioPaxMapper.convertToSif(model, new FileOutputStream(sifFile));
			// try to discover a SIF reader and pass the data there
			anotherReader =  cyServices.networkViewReaderManager.getReader(sifFile.toURI(), networkName);		
			if(anotherReader != null) {
				insertTasksAfterCurrentTask(
					anotherReader, 
					new AbstractTask() {
					@Override
					public void run(TaskMonitor taskMonitor) throws Exception {
						taskMonitor.setStatusMessage("Creating node attributes from BioPAX properties...");
						taskMonitor.setProgress(0.0);
						CyNetwork[] cyNetworks = anotherReader.getNetworks();
						int i = 0;
						for (CyNetwork net : cyNetworks) {	
							networks.add(net);
							//create attributes from biopax properties
							createBiopaxSifAttributes(model, net, mapper, taskMonitor);
							// set the biopax network mapping type for other plugins
							AttributeUtil.set(net, net, BioPaxMapper.BIOPAX_NETWORK, "SIF", String.class);
							taskMonitor.setProgress(++i/cyNetworks.length);
						}
					}
				})
				;				
			} else {
				//fail with a message
				throw new BioPaxIOException("No SIF readers found.");
			}
			break;
		case SBGN:
			//convert to SBGN
			taskMonitor.setStatusMessage("Mapping BioPAX model to SBGN, " +
					"then to CyNetwork (using the first discovered SBGN reader)...");
			File sbgnFile = File.createTempFile("tmp_biopax", ".sbgn");
			sbgnFile.deleteOnExit(); 
			BioPaxMapper.convertToSBGN(model, new FileOutputStream(sbgnFile));
			// try to discover a SBGN reader to pass the xml data there
			anotherReader =  cyServices.networkViewReaderManager.getReader(sbgnFile.toURI(), networkName);
			if(anotherReader != null) {				
				insertTasksAfterCurrentTask(
//				cyServices.taskManager.execute( new TaskIterator(	
					anotherReader, 
					new AbstractTask() {
					@Override
					public void run(TaskMonitor taskMonitor) throws Exception {
						taskMonitor.setProgress(0.0);
						for (CyNetwork network : anotherReader.getNetworks()) {	
							networks.add(network);
							
							//TODO create attributes from biopax properties (depends on actual SBGN reader, if any) ?
						
							// set the biopax network mapping type for other plugins
							AttributeUtil.set(network, network, BioPaxMapper.BIOPAX_NETWORK, "SBGN", String.class);	
						}
						taskMonitor.setProgress(1.0);
					}
				})
//				)
				;
			} else {
				//fail with a message
				throw new BioPaxIOException("No SBGN readers found, or BioPAX to SBGN " +
						"conversion failed (check " + sbgnFile.getAbsolutePath());
			}
			break;
		default:
			break;
		}
	}

	
	private void createBiopaxSifAttributes(Model model, CyNetwork cyNetwork, 
			BioPaxMapper mapper, TaskMonitor taskMonitor) {
			
		taskMonitor.setStatusMessage("Updating SIF network " +
				"node/edge attributes from the BioPAX model...");				

		// Set the Quick Find Default Index
		AttributeUtil.set(cyNetwork, cyNetwork, "quickfind.default_index", CyNetwork.NAME, String.class);

		// we need the biopax sub-model to create node/edge attributes
		final Set<String> uris = new HashSet<String>();
		for (CyNode node : cyNetwork.getNodeList()) {
			//hack: we know that the built-in Cy3 SIF reader uses URIs 
			// from the Pathway Commons SIF data to fill the NAME column by default...
			String uri = cyNetwork.getRow(node).get(CyNetwork.NAME, String.class);
			if(uri != null && !uri.contains("/group/")) {
				uris.add(uri);
			} 
		}

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
			} else { //e == null						
				if(uri.contains("/group/")) {
					AttributeUtil.set(cyNetwork, node, BioPaxMapper.BIOPAX_ENTITY_TYPE, "(generic)", String.class);
					AttributeUtil.set(cyNetwork, node, BioPaxMapper.BIOPAX_URI, uri, String.class);
					AttributeUtil.set(cyNetwork, node, CyRootNetwork.SHARED_NAME, "(generic)", String.class);
					AttributeUtil.set(cyNetwork, node, CyNetwork.NAME, "(generic)", String.class);
				} else {
					log.warn("URI, which is not a generated " +
							"generic/group's one, is not found on the server: " + uri);
				}
			}
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
		VisualStyle style = null; 
		
		switch (readerModeSelection.getSelectedValue()) {
		case DEFAULT:
			style = visualStyleUtil.getBioPaxVisualStyle();
			view = cyServices.networkViewFactory.createNetworkView(network);
			break;
		case SIF:
			style = visualStyleUtil.getBinarySifVisualStyle();
			view = anotherReader.buildCyNetworkView(network);
			break;
		case SBGN:
		default:
			view = anotherReader.buildCyNetworkView(network);
			//TODO: a layout for SBGN views (if not already done)? 
			break;
		}

		if(view != null) {
			if(!cyServices.networkViewManager.getNetworkViews(network).contains(view))
				cyServices.networkViewManager.addNetworkView(view);

			final VisualStyle vs = style;
			if(vs != null) {
				final CyNetworkView v = view;
				//optionally apply style and layout
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if(applyStyle) {
							layout(v); //runs in a separate task/thread
							cyServices.mappingManager.setVisualStyle(vs, v);
							vs.apply(v);
							v.updateView();
						}						
					}
				});
			}
		}
		
		return view;
	}


	private void layout(CyNetworkView view) {
		// do layout
		CyLayoutAlgorithm layout = cyServices.layoutManager.getLayout("force-directed");
		if (layout == null) {
			layout = cyServices.layoutManager.getLayout(CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME);
			log.warn("'force-directed' layout not found; will use the default one.");
		}
		cyServices.taskManager.execute(layout.createTaskIterator(view, 
				layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS,""));
	}

}
