package org.cytoscape.biopax.internal.action;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.biopax.internal.util.BioPaxUtil;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
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
	private CySwingApplication cySwingApplication;

	public DisplayBioPaxXmlTask(View<CyNode> nodeView, CyNetworkView networkView, CySwingApplication cySwingApplication) {
		this.nodeView = nodeView;
		this.networkView = networkView;
		this.cySwingApplication = cySwingApplication;
	}
	
	@Override
	public void cancel() {
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNode node = nodeView.getModel();
		CyRow row = networkView.getModel().getRow(node);
		String owlxml = row.get(BioPaxUtil.BIOPAX_DATA, String.class);
		String label = row.get(CyNetwork.NAME, String.class);	
		Component component = cySwingApplication.getJFrame();
		JOptionPane.showMessageDialog(component, owlxml, label, JOptionPane.PLAIN_MESSAGE);
	}
	
}
