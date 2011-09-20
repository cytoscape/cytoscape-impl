package org.cytoscape.biopax.internal;

import java.io.InputStream;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.cytoscape.biopax.MapBioPaxToCytoscape;
import org.cytoscape.biopax.NetworkListener;
import org.cytoscape.biopax.util.BioPaxUtil;
import org.cytoscape.biopax.util.BioPaxVisualStyleUtil;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * GraphReader Implementation for BioPAX Files.
 *
 * @author Ethan Cerami.
 * @author Igor Rodchenkov (re-factoring, using PaxTools API)
 */
public class BioPaxNetworkViewReaderTask extends AbstractTask implements CyNetworkReader {
	
	public static final Logger log = LoggerFactory.getLogger(BioPaxNetworkViewReaderTask.class);

	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkNaming naming;
	private final VisualMappingManager mappingManager;
	private final BioPaxVisualStyleUtil bioPaxVisualStyleUtil;
	private final NetworkListener networkListener;

	private InputStream stream;

	private String inputName;

	private CyNetwork network;

	/**
	 * Constructor
	 *
	 * @param fileName File Name.
	 */
//	public BioPaxNetworkViewReaderTask(String fileName) {
//		this.fileName = fileName;
//		this.model = null;
//		layout = getDefaultLayoutAlgorithm();
//	}

	/**
	 * Constructor
	 * @param model PaxTools BioPAX Model
	 */
	public BioPaxNetworkViewReaderTask(InputStream stream, String inputName, 
			CyNetworkFactory networkFactory, CyNetworkViewFactory viewFactory, 
			CyNetworkNaming naming, NetworkListener networkListener, 
			VisualMappingManager mappingManager, BioPaxVisualStyleUtil bioPaxVisualStyleUtil) 
	{
		this.stream = stream;
		this.inputName = inputName;
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
		this.naming = naming;
		this.networkListener = networkListener;
		this.mappingManager = mappingManager;
		this.bioPaxVisualStyleUtil = bioPaxVisualStyleUtil;
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
		
		//normalize/infer properties: displayName, cellularLocation, organism, dartaSource
		fixDisplayName(model);
		ModelUtils mu = new ModelUtils(model);
		mu.inferPropertyFromParent("dataSource");
		mu.inferPropertyFromParent("organism");
		mu.inferPropertyFromParent("cellularLocation");
		
		// Map BioPAX Data to Cytoscape Nodes/Edges (run as task)
		MapBioPaxToCytoscape mapper = new MapBioPaxToCytoscapeImpl(model, networkFactory, taskMonitor);
		String networkName = getNetworkName(model);
		network = mapper.createCyNetwork(networkName);
		
		if (network.getNodeCount() == 0) {
			log.error("Pathway is empty! Please check the BioPAX source file.");
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
		name = naming.getSuggestedNetworkTitle(name);
		
		if(log.isDebugEnabled())
			log.debug("New BioPAX network name is: " + name);
		
		return name;
	}


	@Override
	public CyNetwork[] getCyNetworks() {
		return new CyNetwork[]{network};
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		CyNetworkView view = viewFactory.getNetworkView(network);
		networkListener.registerNetwork(view);
		
		//  Set-up the BioPax Visual Style
		VisualStyle bioPaxVisualStyle = bioPaxVisualStyleUtil.getBioPaxVisualStyle();
//        // set tooltips
//		BioPaxVisualStyleUtil.setNodeToolTips(view);
//		// set style
		mappingManager.setVisualStyle(bioPaxVisualStyle, view);

		//  Set up BP UI
//		CytoscapeWrapper.initBioPaxPlugInUI();
//      bpContainer.showLegend();
		
		// add node's context menu
		// TODO: NodeViewTaskFactory?
//		BiopaxNodeCtxMenuListener nodeCtxMenuListener = new BiopaxNodeCtxMenuListener();
//		view.addNodeContextMenuListener(nodeCtxMenuListener);
		
		return view;
	}
	
	
	private void fixDisplayName(Model model) {
		if (log.isInfoEnabled())
			log.info("Trying to auto-fix 'null' displayName...");
		// where it's null, set to the shortest name if possible
		for (Named e : model.getObjects(Named.class)) {
			if (e.getDisplayName() == null) {
				if (e.getStandardName() != null) {
					e.setDisplayName(e.getStandardName());
				} else if (!e.getName().isEmpty()) {
					String dsp = e.getName().iterator().next();
					for (String name : e.getName()) {
						if (name.length() < dsp.length())
							dsp = name;
					}
					e.setDisplayName(dsp);
				}
			}
		}
		// if required, set PE name to (already fixed) ER's name...
		for(EntityReference er : model.getObjects(EntityReference.class)) {
			for(SimplePhysicalEntity spe : er.getEntityReferenceOf()) {
				if(spe.getDisplayName() == null || spe.getDisplayName().trim().length() == 0) {
					if(er.getDisplayName() != null && er.getDisplayName().trim().length() > 0) {
						spe.setDisplayName(er.getDisplayName());
					}
				}
			}
		}
	}
}
