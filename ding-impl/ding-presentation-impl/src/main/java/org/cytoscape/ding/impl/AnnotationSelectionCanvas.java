package org.cytoscape.ding.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import org.cytoscape.ding.impl.cyannotator.annotations.AnnotationSelection;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class AnnotationSelectionCanvas extends DingCanvas {

	private AnnotationSelection selection;
	
	public void setSelection(AnnotationSelection selection) {
		this.selection = selection;
		if(selection == null)
			image.clear();
	}
	
	@Override
	public Image paintImage(RenderDetailFlags flags) {
		if(selection == null)
			return null;
		
		image.clear();
		Graphics2D g = image.getGraphics();
		AffineTransform t = g.getTransform();
		g.translate(selection.getX(), selection.getY());
		selection.paint(g);
		g.setTransform(t);
		return image.getImage();
	}

}
