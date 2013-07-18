package org.cytoscape.filter.internal.transformers;

import org.cytoscape.filter.model.Filter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public abstract class AttributeFilter implements Filter<CyNetwork, CyIdentifiable> {
	static final String NODES = "nodes";
	static final String EDGES = "edges";
	static final String NODES_AND_EDGES = "nodes+edges";
	
	private ListSingleSelection<String> typeList = new ListSingleSelection<String>(NODES, EDGES, NODES_AND_EDGES);
	
	Class<? extends CyIdentifiable> elementType;
	
	@Tunable
	public ListSingleSelection<String> getType() {
		return typeList;
	}
	
	@Tunable
	public void setType(ListSingleSelection<String> type) {
		typeList = type;
		String value = type.getSelectedValue();
		if (NODES.equals(value)) {
			elementType = CyNode.class;
		} else if (EDGES.equals(value)) {
			elementType = CyEdge.class;
		} else {
			elementType = null;
		}
	}
	
	@Tunable(description="Attribute")
	public String attributeName;
	
	@Override
	public Class<CyNetwork> getContextType() {
		return CyNetwork.class;
	}
	
	@Override
	public Class<CyIdentifiable> getElementType() {
		return CyIdentifiable.class;
	}
}
