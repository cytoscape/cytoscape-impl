package org.cytoscape.view.vizmap.gui.internal.view;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;

public class VisualPropertySheetModel extends AbstractVizMapperModel {

	private final Class<? extends CyIdentifiable> targetDataType;
	private final VisualStyle style;
	private final RenderingEngine<?> engine;
	private final VisualLexicon lexicon;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	@SuppressWarnings("unchecked")
	public VisualPropertySheetModel(final Class<? extends CyIdentifiable> targetDataType,
									final VisualStyle style,
									final RenderingEngine<?> engine,
									final VisualLexicon lexicon) {
		if (targetDataType != CyNode.class && targetDataType != CyEdge.class && targetDataType != CyNetwork.class)
			throw new IllegalArgumentException("'targetDataType' must be CyNode.class, CyEdge.class or CyNetwork.class");
		if (style == null)
			throw new IllegalArgumentException("'style' must not be null");
		if (lexicon == null)
			throw new IllegalArgumentException("'lexicon' must not be null");
		
		this.targetDataType = targetDataType;
		this.style = style;
		this.engine = engine;
		this.lexicon = lexicon;
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public Class<? extends CyIdentifiable> getTargetDataType() {
		return targetDataType;
	}
	
	public VisualStyle getVisualStyle() {
		return style;
	}
	
	public VisualLexicon getVisualLexicon() {
		return lexicon;
	}
	
	public RenderingEngine<?> getRenderingEngine() {
		return engine;
	}
	
	public VisualProperty<?> getRootVisualProperty() {
		if (targetDataType == CyNode.class) return BasicVisualLexicon.NODE;
		if (targetDataType == CyEdge.class) return BasicVisualLexicon.EDGE;
		return BasicVisualLexicon.NETWORK;
	}
	
	public String getTitle() {
		if (targetDataType == CyNode.class) return "Node";
		if (targetDataType == CyEdge.class) return "Edge";
		return "Network";
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
}
