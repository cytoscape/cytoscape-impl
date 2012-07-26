package org.cytoscape.io.internal.write.nnf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

		final Writer writer = new OutputStreamWriter(outputStream);
				
		
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
