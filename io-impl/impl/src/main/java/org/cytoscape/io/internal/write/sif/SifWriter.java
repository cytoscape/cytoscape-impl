package org.cytoscape.io.internal.write.sif;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

public class SifWriter implements CyWriter {

	// TODO this should come from model-api
	private static final String NODE_NAME_ATTR_LABEL = "name";
	private static final String INTERACTION_ATTR_LABEL = "interaction";
	
	private OutputStream outputStream;
	private CyNetwork network;

	public SifWriter(OutputStream outputStream, CyNetworkView view) {
		this.outputStream = outputStream;
		this.network = view.getModel();
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
	
				String canonicalName = node.getCyRow().get(NODE_NAME_ATTR_LABEL, String.class);
				List<CyEdge> edges = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);
	
				if (edges.size() == 0) {
					writer.write(canonicalName + lineSep);
				} else {
					for ( CyEdge edge : edges ) {
	
						if (node == edge.getSource()) { //do only for outgoing edges
							CyNode target = edge.getTarget();
							String canonicalTargetName = target.getCyRow().get(NODE_NAME_ATTR_LABEL,String.class);
							String interactionName = edge.getCyRow().get(INTERACTION_ATTR_LABEL,String.class);
	
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
