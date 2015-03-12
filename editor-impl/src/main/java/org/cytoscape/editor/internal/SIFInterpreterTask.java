package org.cytoscape.editor.internal;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
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

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class SIFInterpreterTask extends AbstractNetworkViewTask {

	@Tunable(description="Type in a nodes/edges expression in SIF format (e.g. A inhibits B):")
	public String sifString;

	private final VisualMappingManager vmm;
	private final CyEventHelper eh;
	private final CyNetwork network;

	@ProvidesTitle
	public String getTitle() {
		return "SIF Interpreter";
	}
	
	public SIFInterpreterTask(final CyNetworkView view, final VisualMappingManager vmm, final CyEventHelper eh) {
		super(view);
		this.vmm = vmm;
		this.eh = eh;
		network = view.getModel();
	}

	@Override
	public void run(TaskMonitor tm) {
		if (sifString == null)
			throw new NullPointerException("SIF input string is null");

		String[] terms = sifString.split("\\s");
		
		if (terms != null) {
			if (terms.length > 0) {
				String name1 = terms[0].trim();
				
				if (!name1.equals(null)) {
					CyNode node1 = findNode(terms[0]);
					
					if (node1 == null) {
						node1 = network.addNode();
						network.getRow(node1).set("name", terms[0]);

						// nv1 = view.getNodeView(node1);
						// double[] nextLocn = new double[2];
						// nextLocn[0] = p.getX();
						// nextLocn[1] = p.getY();
						// view.xformComponentToNodeCoords(nextLocn);
						// nv1.setOffset(nextLocn[0], nextLocn[1]);
					} else {
						// nv1 = view.getNodeView(node1);
					}

					// double spacing = 3.0 *view.getNodeView(node1).getWidth();

					if (terms.length == 3) {
						// simple case of 'A interaction B'
						CyNode node2 = findNode(terms[2]);
						
						if (node2 == null) {
							node2 = network.addNode();
							network.getRow(node2).set("name", terms[2]);

							// nv2 = view.getNodeView(node2);
							// nv2.setOffset(nv1.getXPosition() + spacing, nv1.getYPosition());
						}

						CyEdge edge = network.addEdge(node1, node2, true);
						network.getRow(edge).set("name", terms[1]);

					} else if (terms.length > 3) {
						// process multiple targets and one source
						List<View<CyNode>> nodeViews = new ArrayList<View<CyNode>>();
						String interactionType = terms[1];
						
						for (int i = 2; i < terms.length; i++) {
							CyNode node2 = findNode(terms[i]);
							
							if (node2 == null) {
								node2 = network.addNode();
								network.getRow(node2).set("name", terms[i]);

								// nv2 = view.getNodeView(node2);

								// nv2.setOffset(nv1.getXPosition() + spacing, nv1
								// .getYPosition());
							}

							CyEdge edge = network.addEdge(node1, node2, true);
							network.getRow(edge).set("name", terms[1]);
							// doCircleLayout(nodeViews, nv1);
						}
					}
				}
				
				// Apply visual style
				eh.flushPayloadEvents(); // To make sure the edge view is created before applying the style
				VisualStyle vs = vmm.getVisualStyle(view);
				vs.apply(view);
				view.updateView();
			}
		}
	}

	@Override
	public void cancel() {
	}

	private void doCircleLayout(List<View<CyNode>> nodeViews, View<CyNode> nv1) {

/*
		// // Compute Radius

		int r = (int) Math.max((nodeViews.size() * nv1.getWidth()) / Math.PI, 30);

		// Compute angle step
		double phi = (2 * Math.PI) / nodeViews.size();

		// Arrange vertices in a circle
		for (int i = 0; i < nodeViews.size(); i++) {

			View<CyNode> nv = nodeViews.get(i);
			nv.setOffset(
					nv1.getXPosition() + (int) (r * Math.sin(i * phi)), 
					nv1.getYPosition() + (int) (r * Math.cos(i * phi)));
		}

		// update view
*/
	}

	private CyNode findNode(String name) {
		for ( CyNode node : network.getNodeList() ) 
			if ( network.getRow(node).get("name",String.class).equals(name) ) 
				return node;	
		return null;
	}
}

