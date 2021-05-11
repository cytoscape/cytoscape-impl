package org.cytoscape.ding.impl;

import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;

public class LabelSelectionManager {
	
	
	private Set<View<CyNode>> selectedNodeLabels = new HashSet<>();
	
	
	public void add(View<CyNode> node) {
		selectedNodeLabels.add(node);
	}
	
	public void clear() {
		selectedNodeLabels.clear();
	}
	
	public boolean isEmpty() {
		return selectedNodeLabels.isEmpty();
	}
	
	public void paint(Graphics2D g) {
//		Graphics2D g = (Graphics2D) graphics.create();
//		g.setColor(UIManager.getColor("Focus.color"));
//		
//		// Draw selection rectangle
//			for (LabelSelection label : selectedLabels) {
//				g.draw(label.getRectangle());
//			}
	}

}
