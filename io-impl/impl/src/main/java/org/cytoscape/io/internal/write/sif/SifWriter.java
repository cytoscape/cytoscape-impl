package org.cytoscape.io.internal.write.sif;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

public class SifWriter implements CyWriter {

	// TODO this should come from model-api
	private static final String NODE_NAME_ATTR_LABEL = "name";
	private static final String INTERACTION_ATTR_LABEL = "interaction";
	
	private OutputStream outputStream;
	private CyNetwork network;

	public SifWriter(OutputStream outputStream, CyNetwork network) {
		this.outputStream = outputStream;
		this.network = network; 
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);

		final Writer writer = new OutputStreamWriter(outputStream);
		try {
			final String lineSep = System.getProperty("line.separator");
			final List<CyNode> nodeList = network.getNodeList();
	
			int i = 0;
			for ( CyNode node : nodeList ) {
				if (taskMonitor != null) {
					//  Report on Progress
					double percent = ((double) i++ / nodeList.size());
					taskMonitor.setProgress(percent);
				}
	
				String canonicalName = network.getRow(node).get(NODE_NAME_ATTR_LABEL, String.class);
				List<CyEdge> edges = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);
	
				if (edges.size() == 0) {
					writer.write(canonicalName + lineSep);
				} else {
					for ( CyEdge edge : edges ) {
	
						if (node == edge.getSource()) { //do only for outgoing edges
							CyNode target = edge.getTarget();
							String canonicalTargetName = network.getRow(target).get(NODE_NAME_ATTR_LABEL,String.class);
							String interactionName = network.getRow(edge).get(INTERACTION_ATTR_LABEL,String.class);
	
							if (interactionName == null) {
								interactionName = "xx";
							}
	
							writer.write(canonicalName);
							writer.write("\t");
							writer.write(interactionName);
							writer.write("\t");
							writer.write(canonicalTargetName);
							writer.write(lineSep);
						}
					} 
				} 
			} 
		} finally {
			writer.close();
		}
		taskMonitor.setProgress(1.0);
	}
	
	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

}
