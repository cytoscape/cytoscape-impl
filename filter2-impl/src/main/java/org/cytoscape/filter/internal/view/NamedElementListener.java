package org.cytoscape.filter.internal.view;

public interface NamedElementListener<T extends NamedElement> {
	void handleElementAdded(T element); 
	void handleElementRemoved(T element); 
}
