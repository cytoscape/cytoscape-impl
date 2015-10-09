package org.cytoscape.filter.internal.view;

import java.util.List;

import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class TransformerElement extends NamedElement {
	
	private List<Transformer<CyNetwork, CyIdentifiable>> chain;

	public TransformerElement(String name, List<Transformer<CyNetwork, CyIdentifiable>> chain) {
		super(name);
		this.chain = chain;
	}
	
	public List<Transformer<CyNetwork, CyIdentifiable>> getChain() {
		return chain;
	}
}
