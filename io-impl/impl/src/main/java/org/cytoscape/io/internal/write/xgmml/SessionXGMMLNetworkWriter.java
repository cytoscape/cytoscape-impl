package org.cytoscape.io.internal.write.xgmml;

import java.io.IOException;
import java.io.OutputStream;

import org.cytoscape.io.internal.read.xgmml.ObjectTypeMap;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.presentation.RenderingEngineManager;

/**
 * This writer serializes CyNetworks as XGMML files which are customized for session serialization.
 * It should not be used to export to standard XGMML files.
 */
public class SessionXGMMLNetworkWriter extends GenericXGMMLWriter {

	public SessionXGMMLNetworkWriter(final OutputStream outputStream,
									 final RenderingEngineManager renderingEngineMgr,
									 final CyNetwork network,
									 final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
									 final CyNetworkManager networkMgr,
									 final CyRootNetworkManager rootNetworkMgr) {
		super(outputStream, renderingEngineMgr, network, unrecognizedVisualPropertyMgr, networkMgr, rootNetworkMgr);
	}

	@Override
	protected void writeRootElementAtributes() throws IOException {
        writeAttributePair("id", network.getSUID());
        writeAttributePair("label", getLabel(network, network));
    	writeAttributePair("cy:view", ObjectTypeMap.toXGMMLBoolean(false));
    	writeAttributePair("cy:registered", ObjectTypeMap.toXGMMLBoolean(isRegistered(network)));
    }
	
	@Override
	protected void writeMetadata() throws IOException {
		// Ignore...
	}
	
	@Override
	protected void writeRootGraphAttributes() throws IOException {
		// Write sub-graphs first
		for (CySubNetwork subNet : subNetworks) {
			if (!writtenNetMap.containsKey(subNet)) {
				writeSubGraph(subNet);
			}
		}
	}
	
	@Override
	protected void writeNode(final CyNetwork net, final CyNode node) throws IOException {
		boolean written = writtenNodeMap.containsKey(node);
		
		// Output the node
		writeElement("<node");
		
		if (written) {
			// Write as an XLink only
			writeAttributePair("xlink:href", "#" + node.getSUID());
			write("/>\n");
		} else {
			// Remember that we've wrote this node
	     	writtenNodeMap.put(node, node);
			
			// Write the actual node with its properties
			writeAttributePair("id", node.getSUID());
			writeAttributePair("label", getLabel(net, node));
			
			final CyNetwork netPointer = node.getNetworkPointer();
			
			if (netPointer == null) {
				write("/>\n");
			} else {
				write(">\n");
				depth++;
				
				// Write node's sub-graph:
				if (subNetworks.contains(netPointer)) {
					// Because this network is registered (is also a child network), just write the reference.
					// The content will be saved later, under the root graph
					// (it's important to save the child network graphs in the correct order).
					writeSubGraphReference(netPointer);
				} else {
					writeSubGraph(netPointer);
				}
				
				depth--;
				writeElement("</node>\n");
			}
		}
	}
	
	@Override
	protected void writeEdge(CyNetwork net, CyEdge edge) throws IOException {
		writeElement("<edge");
		boolean written = writtenEdgeMap.containsKey(edge);
		
		if (written) {
			// Write as an XLink only
			writeAttributePair("xlink:href", "#" + edge.getSUID());
			write("/>\n");
		} else {
			// Remember that we've wrote this edge
			writtenEdgeMap.put(edge, edge);
			
			writeAttributePair("id", edge.getSUID());
			writeAttributePair("label", getLabel(net, edge));
			writeAttributePair("source", edge.getSource().getSUID());
			writeAttributePair("target", edge.getTarget().getSUID());
			writeAttributePair("cy:directed",  ObjectTypeMap.toXGMMLBoolean(edge.isDirected()));
			
			write("/>\n");
		}
	}
	
	@Override
	protected void writeSubGraph(final CyNetwork net) throws IOException {
		if (net == null)
			return;
		
		final CyRootNetwork otherRoot = rootNetworkMgr.getRootNetwork(net);
		final boolean sameRoot = rootNetwork.equals(otherRoot);
		
		if (!sameRoot) {
			// This network belongs to another XGMML file, but that's ok because this XGMML is part of a CYS file,
			// which means that both files will be saved.
			writeSubGraphReference(net);
		} else {
			super.writeSubGraph(net);
		}
	}
	
	@Override
	protected void writeAttributes(CyRow row) throws IOException {
		// Ignore...
    }
	
	@Override
	protected boolean ignoreGraphicsAttribute(final CyIdentifiable element, String attName) {
		return true;
	}
}
