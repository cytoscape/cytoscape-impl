package org.cytoscape.biopax.internal;

import java.io.InputStream;

import org.apache.commons.lang.StringEscapeUtils;
import org.biopax.paxtools.model.Model;
import org.cytoscape.biopax.internal.util.BioPaxUtil;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
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
public class BioPaxReaderTask extends AbstractTask implements CyNetworkReader {
	
	public static final Logger log = LoggerFactory.getLogger(BioPaxReaderTask.class);

	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkNaming naming;

	private InputStream stream;

	private String inputName;

	private CyNetwork network;


	/**
	 * Constructor
	 * @param model PaxTools BioPAX Model
	 */
	public BioPaxReaderTask(InputStream stream, String inputName, 
			CyNetworkFactory networkFactory, CyNetworkViewFactory viewFactory, 
			CyNetworkNaming naming) 
	{
		this.stream = stream;
		this.inputName = inputName;
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
		this.naming = naming;
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
//		// a hack to skip running property reasoner for already normalized data... TODO generalize
//		if(model.getXmlBase() == null || !model.getXmlBase().contains("pathwaycommons")) {
//			BioPaxUtil.fixDisplayName(model);
//			ModelUtils.inferPropertiesFromParent(model, 
//					new HashSet<String>(Arrays.asList("dataSource", "organism", "cellularLocation")));
//		}
		
		// Map BioPAX Data to Cytoscape Nodes/Edges (run as task)
		BioPaxMapper mapper = new BioPaxMapper(model, networkFactory, taskMonitor);
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
