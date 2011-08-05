
package org.cytoscape.editor.internal.gui;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CySwingApplication;

public class EditorCytoPanelComponent implements CytoPanelComponent {

	private ShapePalette panel;

	private final ImageIcon nodeIcon; 
	private final ImageIcon edgeIcon;
	private final ImageIcon netIcon;

	public EditorCytoPanelComponent(CySwingApplication app) {

		nodeIcon = new ImageIcon(getClass().getResource("/images/node.png"));
		edgeIcon = new ImageIcon(getClass().getResource("/images/edge.png"));
		netIcon = new ImageIcon(getClass().getResource("/images/network.png"));

		panel = new ShapePalette();

		panel.addShape(app,"NODE_TYPE","unknown", nodeIcon, "Node");
		panel.addShape(app,"EDGE_TYPE","unknown", edgeIcon, "Edge");
		panel.addShape(app,"NETWORK_TYPE","unknown", netIcon, "Network");
	}

    public Component getComponent() { return panel; }

    public CytoPanelName getCytoPanelName() { return CytoPanelName.WEST; } 

    public String getTitle() { return "Editor"; }

    public Icon getIcon() { return null; }
}

