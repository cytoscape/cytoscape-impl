package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import org.cytoscape.ding.impl.cyannotator.annotations.AbstractAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

@SuppressWarnings("serial")
public class AnnotationComponent extends JComponent {

	private final AbstractAnnotation annotation;

	public AnnotationComponent(AbstractAnnotation annotation) {
		this.annotation = annotation;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(Math.round((float) annotation.getWidth()), Math.round((float) annotation.getHeight()));
	}

	/**
	 * Annotations are supposed to be in node coordinates, but lets just pretend
	 * that node and image coordinates are the same thing here.
	 */
	@Override
	public void paint(Graphics g) {
		annotation.setLocation(0, 0);
		annotation.setSize(getWidth(), getHeight());
		annotation.paint(g, false);
	}
}
