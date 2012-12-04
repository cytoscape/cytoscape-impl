package org.cytoscape.io.internal.write.xgmml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.cytoscape.io.internal.read.xgmml.ObjectTypeMap;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;

/**
 * This writer serializes CyNetworkViews as XGMML files which are customized for session serialization.
 * It should not be used to export to standard XGMML files.
 */
public class SessionXGMMLNetworkViewWriter extends GenericXGMMLWriter {

	public SessionXGMMLNetworkViewWriter(final OutputStream outputStream,
										 final RenderingEngineManager renderingEngineMgr,
										 final CyNetworkView networkView,
										 final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
										 final CyNetworkManager networkMgr,
										 final CyRootNetworkManager rootNetworkMgr,
										 final VisualMappingManager vmMgr) {
		super(outputStream, renderingEngineMgr, networkView, unrecognizedVisualPropertyMgr, networkMgr, rootNetworkMgr,
				vmMgr, null);
	}
	
	@Override
	protected void writeRootElementAtributes() throws IOException {
        writeAttributePair("id", networkView.getSUID());
        writeAttributePair("label", getLabel(networkView));
        writeAttributePair("cy:view", ObjectTypeMap.toXGMMLBoolean(true));
		writeAttributePair("cy:networkId", network.getSUID());
		
		if (visualStyle != null)
			writeAttributePair("cy:visualStyle", visualStyle.getTitle());
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
		Collection<VisualProperty<?>> visualProperties = visualLexicon.getAllDescendants(BasicVisualLexicon.EDGE);
		
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