package org.cytoscape.filter.internal;

import java.util.List;

import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.filter.model.TransformerSource;

public interface TransformerExecutionStrategy {
	<C, E> void execute(C context, List<Transformer<C, E>> transformers, TransformerSource<C, E> source, TransformerSink<E> sink);
}
