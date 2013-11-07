package org.cytoscape.filter.internal.view;

public abstract class NamedElement {
	public String name;

	public NamedElement(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public abstract boolean isPlaceholder();
}