package org.cytoscape.io.internal.write.nnf;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

public class NnfWriter implements CyWriter {
	
	private OutputStream outputStream;
	//private CyNetwork network;
	private final CyNetworkManager cyNetworkManagerServiceRef;

	public NnfWriter(CyNetworkManager cyNetworkManagerServiceRef, OutputStream outputStream) {
		this.outputStream = outputStream;
		//this.network = network; 
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
	}

	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		taskMonitor.setStatusMessage("Exporting Nested Networks...");

		final Writer writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8").newEncoder());

		final Set<CyNetwork> networks = this.cyNetworkManagerServiceRef.getNetworkSet();
		final float networkCount = networks.size();
		try {
			float writtenCount = 0.0f;
			for (final CyNetwork network : networks) {
				writeNetwork(writer, network);
				++writtenCount;
				taskMonitor.setProgress(Math.round(writtenCount / networkCount));
			}
		} catch (Exception e) {
			taskMonitor.setStatusMessage("Cannot export networks as NNF.");
		}
		finally {
			writer.close();
		}		
		
		taskMonitor.setProgress(1.0);
	}
	
	
	private void writeNetwork(Writer writer, final CyNetwork network) throws IOException {
		final String title = network.getRow(network).get(CyNetwork.NAME, String.class);

		final Set<String> encounteredNodes = new HashSet<String>();

		final List<CyEdge> edges = (List<CyEdge>)network.getEdgeList();
		for (final CyEdge edge : edges) {
			writer.write(escapeID(title) + " ");
			
			CyNode source = edge.getSource();
			final String sourceID = network.getRow(source).get(CyNetwork.NAME, String.class);
			
			encounteredNodes.add(sourceID);
			writer.write(escapeID(sourceID) + " ");

			String interactionName = network.getRow(edge).get("interaction", String.class); 
			
			if (interactionName == null)
				interactionName = "xx";
			writer.write(escapeID(interactionName) + " ");

			CyNode target = edge.getTarget();
			final String targetID = network.getRow(target).get(CyNetwork.NAME, String.class);
			
			encounteredNodes.add(targetID);
			writer.write(escapeID(targetID) + "\n");
		}

		final List<CyNode> nodes = network.getNodeList();
		for (final CyNode node : nodes) {
			final String nodeID = network.getRow(node).get(CyNetwork.NAME, String.class); 
			
			if (!encounteredNodes.contains(nodeID))
				writer.write(escapeID(title) + " " + escapeID(nodeID) + "\n");
		}
	}

	
	private String escapeID(final String ID) {
		final StringBuilder builder = new StringBuilder(ID.length());
		for (int i = 0; i < ID.length(); ++i) {
			final char ch = ID.charAt(i);
			if (ch == ' ' || ch == '\t' || ch == '\\')
				builder.append('\\');
			builder.append(ch);
		}

		return builder.toString();
	}

	
	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

}
