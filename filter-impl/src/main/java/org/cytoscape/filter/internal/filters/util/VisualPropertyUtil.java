package org.cytoscape.filter.internal.filters.util;

import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.Visualizable;

public class VisualPropertyUtil {
	@SuppressWarnings("unchecked")
	public static <T> T get(VisualLexicon lexicon, View<?> view, String id, VisualProperty<Visualizable> root, Class<T> type) {
		for (VisualProperty<?> property : lexicon.getAllDescendants(root)) {
			if (!property.getIdString().equals(id)) {
				continue;
			}
			return (T) view.getVisualProperty(property);
		}
		return null;
	}
}
