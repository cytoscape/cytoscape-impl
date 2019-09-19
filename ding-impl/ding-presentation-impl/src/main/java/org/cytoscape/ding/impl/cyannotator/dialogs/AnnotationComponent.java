package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import org.cytoscape.ding.impl.cyannotator.annotations.AbstractAnnotation;

@SuppressWarnings("serial")
public class AnnotationComponent extends JComponent {
	
	private final AbstractAnnotation annotation;

	public AnnotationComponent(AbstractAnnotation annotation) {
		this.annotation = annotation;
	}

	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(Math.round((float)annotation.getWidth()), Math.round((float)annotation.getHeight()));
	}
	
	/**
	 * Annotations are supposed to be in node coordinates, but lets just pretend that
	 * node and image coordinates are the same thing here.
	 */
	@Override
	public void paint(Graphics g) {
		annotation.setLocation(0, 0);
		annotation.setSize(getWidth(), getHeight());
		annotation.paint(g, false);
	}
}
