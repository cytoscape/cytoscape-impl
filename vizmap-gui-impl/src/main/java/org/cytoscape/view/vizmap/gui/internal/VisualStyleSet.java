package org.cytoscape.view.vizmap.gui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.cytoscape.view.vizmap.VisualStyle;

public class VisualStyleSet {
	
	private final VisualStyle networkStyle;
	private final Map<String, VisualStyle> nodeColumnStyles;
	private final Map<String, VisualStyle> edgeColumnStyles;
	
	public VisualStyleSet(
		VisualStyle networkStyle,
		Map<String,VisualStyle> nodeColumnStyles, 
		Map<String,VisualStyle> edgeColumnStyles
	) {
		this.networkStyle = Objects.requireNonNull(networkStyle);
		this.nodeColumnStyles = nodeColumnStyles == null ? Map.of() : nodeColumnStyles;
		this.edgeColumnStyles = edgeColumnStyles == null ? Map.of() : edgeColumnStyles;
	}


	public VisualStyle getNetworkStyle() {
		return networkStyle;
	}

	public Map<String,VisualStyle> getNodeColumnStyles() {
		return nodeColumnStyles;
	}

	public Map<String,VisualStyle> getEdgeColumnStyles() {
		return edgeColumnStyles;
	}
	
	
	public Collection<VisualStyle> getAllStyles() {
		var list = new ArrayList<VisualStyle>();
		list.add(networkStyle);
		list.addAll(nodeColumnStyles.values());
		list.addAll(edgeColumnStyles.values());
		return list;
	}

}
