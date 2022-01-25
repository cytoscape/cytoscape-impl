package org.cytoscape.search.internal.ui;

import java.awt.Component;

import org.cytoscape.application.swing.AbstractToolBarComponent;

public class SearchBoxToolbarComponent extends AbstractToolBarComponent {

	private final SearchBox searchBox; 
	
	public SearchBoxToolbarComponent(SearchBox searchBox) {
		this.searchBox = searchBox;
		setToolBarGravity(100000000000000.1f);
	}
	
	@Override
	public Component getComponent() {
		return searchBox;
	}

}
