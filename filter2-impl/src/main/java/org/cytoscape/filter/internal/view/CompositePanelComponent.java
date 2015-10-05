package org.cytoscape.filter.internal.view;

import javax.swing.JComponent;

import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public interface CompositePanelComponent {
	
	JComponent getSeparator();
	
	int getModelCount();
	
	Transformer<CyNetwork,CyIdentifiable> getModelAt(int index);
	
	TransformerElementViewModel<? extends SelectPanelComponent> getViewModel(Transformer<CyNetwork,CyIdentifiable> transformer);
}
