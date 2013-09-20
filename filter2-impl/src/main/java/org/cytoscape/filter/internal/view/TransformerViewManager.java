package org.cytoscape.filter.internal.view;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerFactory;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class TransformerViewManager {

	private Map<String, TransformerViewFactory> viewFactories;
	private List<FilterComboBoxElement> filterComboBoxModel;
	private TransformerManager transformerManager;

	public TransformerViewManager(TransformerManager transformerManager) {
		this.transformerManager = transformerManager;
		viewFactories = new HashMap<String, TransformerViewFactory>();
		filterComboBoxModel = new ArrayList<TransformerViewManager.FilterComboBoxElement>();
		filterComboBoxModel.add(new FilterComboBoxElement("Add...", null));
	}
	
	public Component createView(Transformer<CyNetwork, CyIdentifiable> transformer) {
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
		
		// First item has UX hint.  Keep it at the top.
		FilterComboBoxElement firstItem = filterComboBoxModel.remove(0);
		
		viewFactories.put(factory.getId(), factory);
		FilterComboBoxElement element = new FilterComboBoxElement(transformer.getName(), factory.getId());
		filterComboBoxModel.add(element);
		Collections.sort(filterComboBoxModel);
		
		filterComboBoxModel.add(0, firstItem);
	}
	
	public void unregisterTransformerViewFactory(TransformerViewFactory factory, Map<String, String> properties) {
		String id = factory.getId();
		viewFactories.remove(id);
		
		Iterator<FilterComboBoxElement> iterator = filterComboBoxModel.iterator();
		while (iterator.hasNext()) {
			FilterComboBoxElement element = iterator.next();
			if (id.equals(element.getId())) {
				iterator.remove();
			}
		}
	}
	
	List<FilterComboBoxElement> getFilterComboBoxModel() {
		return filterComboBoxModel;
	}
	
	class FilterComboBoxElement implements TransformerFactory<CyNetwork, CyIdentifiable>, Comparable<FilterComboBoxElement> {
		private String name;
		private String id;

		public FilterComboBoxElement(String name, String id) {
			this.name = name;
			this.id = id;
		}
		
		@Override
		public String getId() {
			return id;
		}

		@Override
		public Transformer<CyNetwork, CyIdentifiable> createTransformer() {
			return transformerManager.createTransformer(id);
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		@Override
		public int compareTo(FilterComboBoxElement other) {
			return name.compareToIgnoreCase(other.name);
		}
	}
}
