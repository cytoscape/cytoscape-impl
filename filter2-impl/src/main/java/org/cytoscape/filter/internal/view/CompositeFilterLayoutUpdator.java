package org.cytoscape.filter.internal.view;

import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.TransformerListener;


/**
 * Updates the View's layout when the number of child filters changes.
 */
public class CompositeFilterLayoutUpdator implements TransformerListener {

	public interface LayoutUpdatable {
		void updateLayout();
	}
	
	private final CompositeFilter<?,?> model;
	private final LayoutUpdatable view;
	private int savedLength;
	
	public CompositeFilterLayoutUpdator(LayoutUpdatable view, CompositeFilter<?,?> model) {
		this.view = view;
		this.model = model;
		this.savedLength = model.getLength();
	}
	
	@Override
	public synchronized void handleSettingsChanged() {
		if(savedLength != model.getLength()) {
			view.updateLayout();
		}
		savedLength = model.getLength();
	}
	
}