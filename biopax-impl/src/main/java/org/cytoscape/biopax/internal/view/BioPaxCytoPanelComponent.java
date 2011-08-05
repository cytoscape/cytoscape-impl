package org.cytoscape.biopax.internal.view;

import java.awt.Component;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.biopax.BioPaxContainer;

public class BioPaxCytoPanelComponent implements CytoPanelComponent {

	private final BioPaxContainer bpContainer;
	private final Icon icon;
	
	public BioPaxCytoPanelComponent(BioPaxContainer bpContainer) {
		this.bpContainer = bpContainer;
		URL url = getClass().getResource("read_obj.gif");
		icon = new ImageIcon(url);
	}
	
	@Override
	public Component getComponent() {
		return bpContainer.getComponent();
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public String getTitle() {
		return "Node Details";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

}
