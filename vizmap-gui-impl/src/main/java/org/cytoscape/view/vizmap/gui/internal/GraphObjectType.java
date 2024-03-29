package org.cytoscape.view.vizmap.gui.internal;

import java.util.Comparator;
import java.util.Objects;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * This is a wrapper for types that are restricted to CyNode.class, CyEdge.class or CyNetwork.class.
 * This is meant for method arguments that should not be CyColumn.class, to make it more type safe.
 */
public record GraphObjectType(Class<? extends CyIdentifiable> type) implements Comparable<GraphObjectType> {
	
	public GraphObjectType {
		if(type != CyNode.class && type != CyEdge.class && type != CyNetwork.class) {
			throw new IllegalArgumentException("type must be a graph object type, got: " + type);
		}
	}
	
	
	public static GraphObjectType of(Class<? extends CyIdentifiable> type) {
		return new GraphObjectType(type);
	}
	
	public static GraphObjectType node() {
		return of(CyNode.class);
	}
	
	public static GraphObjectType edge() {
		return of(CyEdge.class);
	}
	
	public static GraphObjectType network() {
		return of(CyNetwork.class);
	}

	
	public boolean isNode() {
		return this.type == CyNode.class;
	}
	
	public boolean isEdge() {
		return this.type == CyEdge.class;
	}
	
	

	@Override
	public int compareTo(GraphObjectType that) {
		return 
			Objects.compare(this, that, 
				Comparator.comparing((GraphObjectType o) -> o.type().toString())
				.reversed()
			);
	}
	
	
}
