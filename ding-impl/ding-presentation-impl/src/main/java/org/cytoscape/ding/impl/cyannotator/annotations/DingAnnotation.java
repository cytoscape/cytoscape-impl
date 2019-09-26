package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public interface DingAnnotation extends Annotation {
	
	public static enum CanvasID {
		FOREGROUND, 
		BACKGROUND;
		
		public String toArgName() {
			return this == BACKGROUND ? Annotation.BACKGROUND : Annotation.FOREGROUND;
		}
		public static CanvasID fromArgName(String prop) {
			return DingAnnotation.BACKGROUND.equals(prop) ? BACKGROUND : FOREGROUND;
		}
	}
	
	void changeCanvas(CanvasID canvasId);

	CanvasID getCanvas();
	
	@Override
	default String getCanvasName() {
		return getCanvas().toArgName();
	}
	
	CyAnnotator getCyAnnotator();

	boolean isUsedForPreviews();

	void contentChanged();

	JDialog getModifyDialog();

	// Overrides of Component
	void paint(Graphics g, boolean showSelected);
	
	// Group support
	void setGroupParent(GroupAnnotation parent);

	GroupAnnotation getGroupParent();

	Class<? extends Annotation> getType();
	
	void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
	
	void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

	
	double getX();
	
	double getY();
	
	/** Set location in node coordinates */
	void setLocation(double x, double y);
	
	double getWidth();
	
	double getHeight();
	
	default boolean contains(double x, double y) {
		return (x > getX() && y > getY() && x-getX() < getWidth() && y-getY() < getHeight());
	}
	
	default boolean contains(Point2D p) {
		return contains(p.getX(), p.getY());
	}
	
	int getZOrder();
	
	void setZOrder(int z);
	
	default void setZoom(double zoom) {}

	default double getZoom() { return 1.0; }
	

	@Override
	default void setSpecificZoom(double zoom) {
		setZoom(zoom);
	}
	
	@Override
	default double getSpecificZoom() {
		return getZoom();
	}
	
	/**
	 * Returns the bounds of this annotation in NODE COORDINATES.
	 */
	default Rectangle2D getBounds() {
		return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
	}
}
