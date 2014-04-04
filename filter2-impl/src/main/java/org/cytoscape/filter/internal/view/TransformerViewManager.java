package org.cytoscape.filter.internal.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class TransformerViewManager {

	private Map<String, TransformerViewFactory> viewFactories;
	private List<TransformerViewElement> filterConditionViewElements;
	private List<TransformerViewElement> chainTransformerViewElements;
	private TransformerManager transformerManager;

	public TransformerViewManager(TransformerManager transformerManager) {
		this.transformerManager = transformerManager;
		viewFactories = new HashMap<String, TransformerViewFactory>();
		filterConditionViewElements = new ArrayList<TransformerViewElement>();
		chainTransformerViewElements = new ArrayList<TransformerViewElement>();
	}
	
	public JComponent createView(Transformer<CyNetwork, CyIdentifiable> transformer) {
		TransformerViewFactory viewFactory = viewFactories.get(transformer.getId());
		if (viewFactory == null) {
			return null;
		}
		return viewFactory.createView(transformer);
	}

	public void registerTransformerViewFactory(TransformerViewFactory factory, Map<String, String> properties) {
		Transformer<?, ?> transformer = transformerManager.createTransformer(factory.getId());
		if (!transformer.getContextType().equals(CyNetwork.class)) {
			return;
		}
		if (!transformer.getElementType().equals(CyIdentifiable.class)) {
			return;
		}
		
		List<TransformerViewElement> list;
		if (transformer instanceof Filter) {
			list = filterConditionViewElements;
		} else {
			list = chainTransformerViewElements;
		}
		
		TransformerViewElement element = new TransformerViewElement(transformer.getName(), factory.getId());
		viewFactories.put(factory.getId(), factory);
		list.add(element);
		Collections.sort(list);
	}
	
	public void unregisterTransformerViewFactory(TransformerViewFactory factory, Map<String, String> properties) {
		String id = factory.getId();
		viewFactories.remove(id);
		removeTransformerViewElement(id, filterConditionViewElements);
		removeTransformerViewElement(id, chainTransformerViewElements);
	}
	
	void removeTransformerViewElement(String id, List<TransformerViewElement> elements) {
		Iterator<TransformerViewElement> iterator = elements.iterator();
		while (iterator.hasNext()) {
			TransformerViewElement element = iterator.next();
			if (id.equals(element.getId())) {
				iterator.remove();
			}
		}
	}
	
	List<TransformerViewElement> getFilterConditionViewElements() {
		return filterConditionViewElements;
	}
	
	List<TransformerViewElement> getChainTransformerViewElements() {
		return chainTransformerViewElements;
	}
	
	class TransformerViewElement implements Comparable<TransformerViewElement> {
		private String name;
		private String id;

		public TransformerViewElement(String name, String id) {
			this.name = name;
			this.id = id;
		}
		
		public String getId() {
			return id;
		}

		@Override
		public String toString() {
			return name;
		}
		
		@Override
		public int compareTo(TransformerViewElement other) {
			if (id == null && other.id == null) {
				return 0;
			}
			if (id == null) {
				return -1;
			}
			if (other.id == null) {
				return 1;
			}
			return name.compareToIgnoreCase(other.name);
		}
	}
}
