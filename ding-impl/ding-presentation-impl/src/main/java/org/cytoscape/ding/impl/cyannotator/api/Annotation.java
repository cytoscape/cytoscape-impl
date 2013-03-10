package org.cytoscape.ding.impl.cyannotator.api;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JComponent;
import javax.swing.JDialog;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;


public interface Annotation {
	public static final String BACKGROUND =  "background";
	public static final String FOREGROUND =  "foreground";

	public String getCanvasName();
	public void setCanvas(String canvas);
	public void changeCanvas(String canvas);
	public JComponent getCanvas();

	public JComponent getComponent();
	public void addComponent(JComponent canvas);

	public UUID getUUID();

	public CyAnnotator getCyAnnotator();

	public void moveAnnotation(Point2D location);
	public void removeAnnotation();

	public double getZoom();
	public void setZoom(double zoom);

	public double getSpecificZoom();
	public void setSpecificZoom(double zoom);

	public boolean isSelected();
	public void setSelected(boolean selected);

	public void addArrow(ArrowAnnotation arrow);
	public void removeArrow(ArrowAnnotation arrow);
	public Set<ArrowAnnotation> getArrows();

	public Map<String,String> getArgMap();

	public void drawAnnotation(Graphics g, double x, 
	                           double y, double scaleFactor);

	public boolean usedForPreviews();
	public void setUsedForPreviews(boolean v);

	public void update();
	public void contentChanged();

	public JDialog getModifyDialog();

	// Overrides of Component
	public void paint(Graphics g);
}
