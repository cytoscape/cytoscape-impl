package org.cytoscape.filter.internal.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComponent;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class TransformerViewManager {

	private Map<String, TransformerViewFactory> viewFactories;
	private Map<String, Map<String,String>> viewProperties;
	private List<TransformerViewElement> filterConditionViewElements;
	private List<TransformerViewElement> chainTransformerViewElements;
	private TransformerManager transformerManager;

	private final Object lock = new Object();
	
	public TransformerViewManager(TransformerManager transformerManager) {
		this.transformerManager = transformerManager;
		viewFactories = new ConcurrentHashMap<>(16, 0.75f, 2);
		viewProperties = new ConcurrentHashMap<>(16, 0.75f, 2);
		filterConditionViewElements = new ArrayList<>();
		chainTransformerViewElements = new ArrayList<>();
	}
	
	public JComponent createView(Transformer<CyNetwork, CyIdentifiable> transformer) {
		TransformerViewFactory viewFactory = viewFactories.get(transformer.getId());
		if (viewFactory == null) {
			return null;
		}
		return viewFactory.createView(transformer);
	}
	
	public String getAddButtonTooltip(Transformer<?,?> transformer) {
		Map<String,String> props = viewProperties.get(transformer.getId());
		if(props == null)
			return null;
		return props.get("addButtonTooltip");
	}
	
	public void registerTransformerViewFactory(TransformerViewFactory factory, Map<String, String> properties) {
		Transformer<?, ?> transformer = transformerManager.createTransformer(factory.getId());
		if (!transformer.getContextType().equals(CyNetwork.class)) {
			return;
		}
		if (!transformer.getElementType().equals(CyIdentifiable.class)) {
			return;
		}
		
		synchronized (lock) {
			List<TransformerViewElement> list;
			if (transformer instanceof Filter) {
				list = filterConditionViewElements;
			} else {
				list = chainTransformerViewElements;
			}
			
			TransformerViewElement element = new TransformerViewElement(transformer.getName(), factory.getId());
			viewFactories.put(factory.getId(), factory);
			viewProperties.put(factory.getId(), new HashMap<>(properties));
			list.add(element);
			Collections.sort(list);
		}
	}
	
	public void unregisterTransformerViewFactory(TransformerViewFactory factory, Map<String, String> properties) {
		String id = factory.getId();
		viewFactories.remove(id);
		synchronized (lock) {
			removeTransformerViewElement(id, filterConditionViewElements);
			removeTransformerViewElement(id, chainTransformerViewElements);
		}
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
		synchronized (lock) {
			return new ArrayList<TransformerViewElement>(filterConditionViewElements);
		}
	}
	
	List<TransformerViewElement> getChainTransformerViewElements() {
		synchronized (lock) {
			return new ArrayList<TransformerViewElement>(chainTransformerViewElements);
		}
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
