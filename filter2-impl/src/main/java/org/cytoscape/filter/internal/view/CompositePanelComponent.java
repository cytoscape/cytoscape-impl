package org.cytoscape.filter.internal.view;

import javax.swing.JComponent;

import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public interface CompositePanelComponent {
	
	JComponent getComponent();
	
	void updateLayout();
	
	JComponent getSeparator();
	
	int getTransformerCount();
	
	Transformer<CyNetwork,CyIdentifiable> getTransformerAt(int index);
	
	TransformerElementViewModel<? extends SelectPanelComponent> getViewModel(Transformer<CyNetwork,CyIdentifiable> transformer);
	
	void removeTransformer(int index, boolean unregister);
}
