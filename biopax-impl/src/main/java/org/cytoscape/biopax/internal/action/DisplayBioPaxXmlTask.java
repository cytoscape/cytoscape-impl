package org.cytoscape.biopax.internal.action;

import java.awt.Component;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.cytoscape.biopax.internal.BioPaxFactory;
import org.cytoscape.biopax.internal.MapBioPaxToCytoscapeImpl;
import org.cytoscape.biopax.util.BioPaxUtil;
import org.cytoscape.biopax.util.BioPaxVisualStyleUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DisplayBioPaxXmlTask implements Task {
	public static final Logger log = LoggerFactory.getLogger(DisplayBioPaxXmlTask.class);
	private final View<CyNode> nodeView;
	private final CyNetworkView networkView;
	private final BioPaxFactory factory;

	public DisplayBioPaxXmlTask(View<CyNode> nodeView, CyNetworkView networkView, BioPaxFactory factory) {
		this.nodeView = nodeView;
		this.networkView = networkView;
		this.factory = factory;
	}
	
	@Override
	public void cancel() {
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNode node = nodeView.getModel();
		CyRow row = node.getCyRow();
		String biopaxId = row.get(MapBioPaxToCytoscapeImpl.BIOPAX_RDF_ID, String.class);
		String name = row.get(CyNode.NAME, String.class);
		
		CyNetwork network = networkView.getModel();
		Model m = BioPaxUtil.getNetworkModel(network.getSUID());
		BioPAXElement bpe =  m.getByID(biopaxId);
		StringWriter writer = new StringWriter();
		if (bpe != null) {
			log.info("printing " + bpe + " OWL");
			try {
				SimpleIOHandler simpleExporter = new SimpleIOHandler(m.getLevel());
				//TODO Fix: it prints '<:null' instead '<bp:' when using writeObject method!
				simpleExporter.writeObject(writer, bpe);
			} catch (Exception e) {
				log.error("Faild printing '" + name + "' to OWL", e);
			}
		} else {
			log.info("Node : " + name 
					+ ", BP element not found : " + biopaxId);
		}
		
		String owlxml = writer.toString();
		String label = row.get(BioPaxVisualStyleUtil.BIOPAX_NODE_LABEL, String.class);
		
		Component component = factory.getCySwingApplication().getJFrame();
		JOptionPane.showMessageDialog(component, owlxml, label, JOptionPane.PLAIN_MESSAGE);
	}
	
}
