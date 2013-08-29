package org.cytoscape.biopax.internal.action;

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

@Deprecated
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
