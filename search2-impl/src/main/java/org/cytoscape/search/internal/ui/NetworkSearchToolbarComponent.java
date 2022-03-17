package org.cytoscape.search.internal.ui;

import java.awt.Component;

import org.cytoscape.application.swing.ToolBarComponent;

public class NetworkSearchToolbarComponent implements ToolBarComponent {

	private final NetworkSearchBox searchBox;
	
	public NetworkSearchToolbarComponent(NetworkSearchBox searchBox) {
		this.searchBox = searchBox;
	}
	
	@Override
	public Component getComponent() {
		return searchBox;
	}

	@Override
	public float getToolBarGravity() {
		return 100000000000000.1f;
	}

}
