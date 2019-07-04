package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.AnnotationCanvas;
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
	
	
	void changeCanvas(String canvas);

	AnnotationCanvas getCanvas();

	CyAnnotator getCyAnnotator();

	void moveAnnotationRelative(Point2D location);

	void drawAnnotation(Graphics g, double x, double y, double scaleFactor);

	boolean isUsedForPreviews();

	void contentChanged();

	JDialog getModifyDialog();

	// Overrides of Component
	void paint(Graphics g);
	
	default void print(Graphics g) {
		paint(g);
	}

	// Group support
	void setGroupParent(GroupAnnotation parent);

	GroupAnnotation getGroupParent();

	// Drag support
	void setOffset(Point2D offset);

	Point2D getOffset();

	void saveBounds();

	Rectangle2D getInitialBounds();
	
	Class<? extends Annotation> getType();
	
	void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
	
	void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

	boolean contains(int x, int y);

	Rectangle getBounds();

	int getX();

	int getY();

	int getWidth();

	int getHeight();

	void setLocation(int x, int y);

	Point getLocation();

	void setBounds(int i, int j, int width, int height);
}
