package org.cytoscape.io.internal.write.xgmml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.cytoscape.io.internal.read.xgmml.ObjectTypeMap;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * This writer serializes CyNetworkViews as XGMML files which are customized for session serialization.
 * It should not be used to export to standard XGMML files.
 */
public class SessionXGMMLNetworkViewWriter extends GenericXGMMLWriter {

	public SessionXGMMLNetworkViewWriter(final OutputStream outputStream,
										 final CyNetworkView networkView,
										 final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
										 final CyServiceRegistrar serviceRegistrar) {
		super(outputStream, networkView, unrecognizedVisualPropertyMgr, null, serviceRegistrar);
	}
	
	@Override
	protected void writeRootElementAtributes() throws IOException {
        writeAttributePair("id", networkView.getSUID());
        writeAttributePair("label", getLabel(networkView));
        writeAttributePair("cy:view", ObjectTypeMap.toXGMMLBoolean(true));
		writeAttributePair("cy:networkId", network.getSUID());
		
		if (visualStyle != null)
			writeAttributePair("cy:visualStyle", visualStyle.getTitle());
		
		final RenderingEngineManager renderingEngineMgr = serviceRegistrar.getService(RenderingEngineManager.class);
		final Collection<RenderingEngine<?>> renderingEngines = renderingEngineMgr.getRenderingEngines(networkView);
		
		if (renderingEngines != null && !renderingEngines.isEmpty())
			writeAttributePair("cy:rendererId", renderingEngines.iterator().next().getRendererId());
    }
	
	@Override
	protected void writeMetadata() throws IOException {
		// Ignore...
	}
	
	@Override
	protected void writeRootGraphAttributes() throws IOException {
		writeGraphics(networkView, true);
	}
	
	@Override
	protected void writeNodes() throws IOException {
		for (View<CyNode> view : networkView.getNodeViews())
			writeNodeView(networkView.getModel(), view);
	}
	
	@Override
	protected void writeEdges() throws IOException {
    	for (View<CyEdge> view : networkView.getEdgeViews())
			writeEdgeView(networkView.getModel(), view);
    }
	
	/**
	 * Do not use this method with locked visual properties!
	 */
	@Override
	protected boolean ignoreGraphicsAttribute(final CyIdentifiable element, String attName) {
    	// Only those visual properties that belong to the view (not a visual style) should be saved in the XGMML file.
    	boolean b = ((element instanceof CyNode) && !attName.matches("x|y|z"));
		b = b || (element instanceof CyEdge);
		b = b || ((element instanceof CyNetwork) && attName.matches(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT.getIdString()));
		
		return b;
	}
	
	/**
     * Output a single node view as XGMML
     *
     * @param view the node view to output
     * @throws IOException
     */
	private void writeNodeView(CyNetwork network, View<CyNode> view) throws IOException {
		// Output as a node tag
		writeElement("<node");
		writeAttributePair("id", view.getSUID());
		writeAttributePair("label", getLabel(network, view.getModel()));
		writeAttributePair("cy:nodeId", view.getModel().getSUID());
		write(">\n");
        depth++;
        
        // Output the node graphics if we have a view and it is a simple XGMML export
		writeGraphics(view, true);

		depth--;
		writeElement("</node>\n");
	}
	
	/**
     * Output a Cytoscape edge view as XGMML
     *
     * @param view the edge view to output
     * @throws IOException
     */
	private void writeEdgeView(CyNetwork network, View<CyEdge> view) throws IOException {
		// It is not necessary to write edges that have no locked visual properties
		boolean hasLockedVisualProps = false;
		Collection<VisualProperty<?>> visualProperties = getVisualLexicon().getAllDescendants(BasicVisualLexicon.EDGE);
		
		for (VisualProperty<?> vp : visualProperties) {
			if (view.isDirectlyLocked(vp)) {
				hasLockedVisualProps = true;
				break;
			}
		}
		
		if (hasLockedVisualProps) {
			writeElement("<edge");
			writeAttributePair("id", view.getSUID());
			writeAttributePair("label", getLabel(network, view.getModel()));
			writeAttributePair("cy:edgeId", view.getModel().getSUID());
			write(">\n");
			depth++;
	
			// Write the edge graphics
			writeGraphics(view, true);
	
			depth--;
			writeElement("</edge>\n");
		}
	}
}