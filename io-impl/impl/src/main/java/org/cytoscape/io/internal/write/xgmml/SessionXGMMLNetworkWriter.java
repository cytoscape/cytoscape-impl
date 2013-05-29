package org.cytoscape.io.internal.write.xgmml;

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

import org.cytoscape.io.internal.read.xgmml.ObjectTypeMap;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.SavePolicy;
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
		super(outputStream, renderingEngineMgr, network, unrecognizedVisualPropertyMgr, networkMgr, rootNetworkMgr, 
				null);

		if (rootNetwork.getSavePolicy() != SavePolicy.SESSION_FILE)
			throw new IllegalArgumentException(
					"Network cannot be saved because the root network's save policy is not \"SESSION_FILE\": "
							+ network);
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
		for (final CySubNetwork subNet : subNetworks) {
			if (!writtenNetMap.containsKey(subNet) && isSerializable(subNet)) {
				writeSubGraph(subNet);
			}
		}
	}

	@Override
	protected void writeNodes() throws IOException {
		for (CyNode node : network.getNodeList()) {
			// Only if not already written inside a nested graph
			if (!writtenNodeMap.containsKey(node))
				writeNode(network, node);
		}
	}
	
	@Override
	protected void writeNode(final CyNetwork net, final CyNode node) throws IOException {
		final boolean written = writtenNodeMap.containsKey(node);
		
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
			
			if (netPointer == null || !isSerializable(netPointer)) {
				write("/>\n");
			} else {
				write(">\n");
				depth++;
				
				// Write node's sub-graph:
				if (isRegistered(netPointer)) {
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
    protected void writeEdges() throws IOException {
		for (CyEdge edge : network.getEdgeList()) {
			// Only if not already written inside a nested graph
			if (!writtenEdgeMap.containsKey(edge))
				writeEdge(network, edge);
		}
	}
	
	@Override
	protected void writeEdge(final CyNetwork net, final CyEdge edge) throws IOException {
		writeElement("<edge");
		final boolean written = writtenEdgeMap.containsKey(edge);
		
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
	
	@Override
	protected boolean isSerializable(final CyNetwork net) {
    	return net.getSavePolicy() == SavePolicy.SESSION_FILE && !isDisposed(net);
    }
	
	private boolean isDisposed(final CyNetwork net) {
		return net.getDefaultNetworkTable() == null || net.getDefaultNodeTable() == null ||
			   net.getDefaultEdgeTable() == null;
	}
}
