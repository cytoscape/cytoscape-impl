package org.cytoscape.view.vizmap.gui.internal.view;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.GraphObjectType;

public class VisualPropertySheetModel extends AbstractVizMapperModel {

//	// The type of the table that the mappings get values from
	private final GraphObjectType tableType;
	// The type of the lexicon VPs
	private final Class<? extends CyIdentifiable> lexiconType;
	
	private final VisualStyle style;
	private final VisualLexicon lexicon;

	
	public VisualPropertySheetModel(Class<? extends CyIdentifiable> lexiconType, GraphObjectType tableType, VisualStyle style, VisualLexicon lexicon) {
		this.tableType = tableType;
		this.lexiconType = lexiconType;
		this.style = style;
		this.lexicon = lexicon;
	}
	
	
	public Class<? extends CyIdentifiable> getLexiconType() {
		return lexiconType;
	}
	
	public GraphObjectType getTableType() {
		return tableType;
	}
	
	public VisualStyle getVisualStyle() {
		return style;
	}
	
	public VisualLexicon getVisualLexicon() {
		return lexicon;
	}
	
	public VisualProperty<?> getRootVisualProperty() {
		if (lexiconType == CyNode.class) return BasicVisualLexicon.NODE;
		if (lexiconType == CyEdge.class) return BasicVisualLexicon.EDGE;
		if (lexiconType == CyColumn.class) return BasicTableVisualLexicon.CELL;
		return BasicVisualLexicon.NETWORK;
	}
	
	public String getTitle() {
		if (lexiconType == CyNode.class) return "Node";
		if (lexiconType == CyEdge.class) return "Edge";
		if (lexiconType == CyColumn.class) return "Column";
		return "Network";
	}
	
}
