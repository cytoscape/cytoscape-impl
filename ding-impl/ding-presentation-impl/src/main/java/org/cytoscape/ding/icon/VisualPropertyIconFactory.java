package org.cytoscape.ding.icon;

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

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import org.cytoscape.ding.DArrowShape;
import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.impl.DLineType;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;


/**
 * Static factory for icons.
 *
 */
public class VisualPropertyIconFactory {	
	
	public static <V> Icon createIcon(V value, int w, int h) {
		if(value == null)
			return null;
		
		Icon icon = null;
		
		if(value instanceof Color) {
			icon = new ColorIcon((Color) value, w, h, value.toString());
		} else if(value instanceof NodeShape) {
			final DNodeShape dShape;
			if(NodeShapeVisualProperty.isDefaultShape((NodeShape) value))
				dShape = DNodeShape.getDShape((NodeShape) value);
			else
				dShape = (DNodeShape) value;
			icon = new NodeIcon(dShape.getShape(), w, h, dShape.getDisplayName());
		} else if(value instanceof LineType) {
			icon = new StrokeIcon(DLineType.getDLineType((LineType) value).getStroke(2f), w, h, value.toString());
		} else if(value instanceof CyCustomGraphics) {
			final String name = ((CyCustomGraphics) value).getDisplayName();
			
			if (name != null)
				icon = new CustomGraphicsIcon(((CyCustomGraphics) value), w, h, name);
		} else if(value instanceof ObjectPosition) {
			icon = new ObjectPositionIcon((ObjectPosition) value, w, h, "Label");
		} else if(value instanceof Font) {
			icon = new FontFaceIcon((Font) value, w, h, "");
		} else if(value instanceof ArrowShape) {
			final ArrowShape arrowShape = (ArrowShape) value;
			final DArrowShape dShape;
			if(ArrowShapeVisualProperty.isDefaultShape(arrowShape))
				dShape = DArrowShape.getArrowShape(arrowShape);
			else
				dShape = DArrowShape.NONE;
			
			if(dShape.getShape() == null)
				icon = new TextIcon(value, w, h, ""); // No arrow
			else
				icon = new ArrowIcon(dShape.getShape(), w, h, dShape.getDisplayName());
		} else if(value instanceof Bend) {
			icon = new EdgeBendIcon((Bend) value, w, h, value.toString());
		} else {
			// If not found, use return value of toString() as icon.
			icon = new TextIcon(value, w, h, value.toString());
		}
		
		return icon;
	}
}
