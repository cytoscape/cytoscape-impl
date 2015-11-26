package org.cytoscape.internal.util;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public final class ViewUtil {

	public static String getTitle(final CyNetworkView view) {
		String title = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		
		if (title == null || title.trim().isEmpty())
			title = view.getModel().getRow(view.getModel()).get(CyNetwork.NAME, String.class);
		
		return title;
	}
	
	public static String createUniqueKey(final CyNetworkView view) {
		return view.getSUID() + "__" + view.getRendererId() + "__" + view.hashCode();
	}
	
	private ViewUtil() {
	}
}
