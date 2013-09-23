package org.cytoscape.filter.internal.view;

public interface Matcher<T> {
	boolean matches(T item);
}