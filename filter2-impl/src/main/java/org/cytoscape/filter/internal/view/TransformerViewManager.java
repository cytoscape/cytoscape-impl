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
	private List<TransformerComboBoxElement> filterComboBoxModel;
	private List<TransformerComboBoxElement> chainComboBoxModel;
	private TransformerManager transformerManager;

	public TransformerViewManager(TransformerManager transformerManager) {
		this.transformerManager = transformerManager;
		viewFactories = new HashMap<String, TransformerViewFactory>();
		filterComboBoxModel = new ArrayList<TransformerComboBoxElement>();
		filterComboBoxModel.add(new TransformerComboBoxElement("Add...", null));
		
		chainComboBoxModel = new ArrayList<TransformerComboBoxElement>();
		chainComboBoxModel.add(new TransformerComboBoxElement("Add...", null));
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
		
		List<TransformerComboBoxElement> model;
		if (transformer instanceof Filter) {
			model = filterComboBoxModel;
		} else {
			model = chainComboBoxModel;
		}
		
		TransformerComboBoxElement element = new TransformerComboBoxElement(transformer.getName(), factory.getId());
		viewFactories.put(factory.getId(), factory);
		model.add(element);
		Collections.sort(model);
	}
	
	public void unregisterTransformerViewFactory(TransformerViewFactory factory, Map<String, String> properties) {
		String id = factory.getId();
		viewFactories.remove(id);
		
		Iterator<TransformerComboBoxElement> iterator = filterComboBoxModel.iterator();
		while (iterator.hasNext()) {
			TransformerComboBoxElement element = iterator.next();
			if (id.equals(element.getId())) {
				iterator.remove();
			}
		}
	}
	
	List<TransformerComboBoxElement> getFilterComboBoxModel() {
		return filterComboBoxModel;
	}
	
	List<TransformerComboBoxElement> getChainComboBoxModel() {
		return chainComboBoxModel;
	}
	
	class TransformerComboBoxElement implements Comparable<TransformerComboBoxElement> {
		private String name;
		private String id;

		public TransformerComboBoxElement(String name, String id) {
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
		public int compareTo(TransformerComboBoxElement other) {
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
