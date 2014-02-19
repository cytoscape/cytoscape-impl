package org.cytoscape.filter.internal;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;

public class ModelUtil {
	public static NamedTransformer<?, ?> createNamedTransformer(String name, Transformer<?, ?>... transformers) {
		return new NamedTransformerImpl(name, transformers);
	}
	
	private static class NamedTransformerImpl implements NamedTransformer<Object, Object> {
		String name;
		private List<Transformer<Object, Object>> transformers;
		
		@SuppressWarnings("unchecked")
		public NamedTransformerImpl(String name, Transformer<?, ?>... transformers) {
			this.name = name;
			
			this.transformers = new ArrayList<Transformer<Object, Object>>(transformers.length);
			for (Transformer<?, ?> transformer : transformers) {
				this.transformers.add((Transformer<Object, Object>) transformer);
			}
		}
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public List<Transformer<Object, Object>> getTransformers() {
			return transformers;
		}
	}
}
