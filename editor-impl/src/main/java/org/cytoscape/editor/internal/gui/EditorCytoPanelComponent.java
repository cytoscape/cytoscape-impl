
package org.cytoscape.editor.internal.gui;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CySwingApplication;

public class EditorCytoPanelComponent implements CytoPanelComponent {

	private ShapePalette panel;

	public EditorCytoPanelComponent(ShapePalette panel) {
		this.panel = panel;
	}

    public Component getComponent() { return panel; }

    public CytoPanelName getCytoPanelName() { return CytoPanelName.WEST; } 

    public String getTitle() { return "Editor"; }

    public Icon getIcon() { return null; }
}

