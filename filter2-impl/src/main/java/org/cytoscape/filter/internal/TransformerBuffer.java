package org.cytoscape.filter.internal;

import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.filter.model.TransformerSource;

interface TransformerBuffer<C, E> extends TransformerSource<C, E>, TransformerSink<E> {
	void clear();
}
