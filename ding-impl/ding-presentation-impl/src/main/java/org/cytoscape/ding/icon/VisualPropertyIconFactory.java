package org.cytoscape.ding.icon;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import org.cytoscape.cg.util.CustomGraphicsIcon;
import org.cytoscape.ding.DArrowShape;
import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.impl.DLineType;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.LabelBackgroundShapeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.ObjectPositionVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.EdgeStacking;
import org.cytoscape.view.presentation.property.values.LabelBackgroundShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.values.ObjectPosition;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * Static factory for icons.
 */
public final class VisualPropertyIconFactory {	
	
	public static <V> Icon createIcon(V value, int w, int h) {
		return createIcon(value, null, w, h);
	}
	
	public static <V> Icon createIcon(V value, VisualProperty<V> vp, int w, int h) {
		if (value == null)
			return null;
		
		Icon icon = null;
		
		if (value instanceof Color color) {
			icon = new ColorIcon(color, w, h, value.toString());
		} else if (value instanceof NodeShape shape) {
			final DNodeShape dShape;
			
			if (NodeShapeVisualProperty.isDefaultShape(shape))
				dShape = DNodeShape.getDShape(shape);
			else
				dShape = (DNodeShape) shape;
			
			icon = new NodeIcon(dShape.getShape(), w, h, dShape.getDisplayName());
		} else if (value instanceof LabelBackgroundShape shape) {
			var dShape = DNodeShape.getDShape(shape);
			
			if (dShape != null)
				icon = new NodeIcon(dShape.getShape(), w, h, dShape.getDisplayName());
			else
				icon = new TextIcon(LabelBackgroundShapeVisualProperty.NONE, w, h, "");
		} else if (value instanceof LineType line) {
			icon = new StrokeIcon(DLineType.getDLineType(line).getStroke(2f), w, h, value.toString());
		} else if (value instanceof CyCustomGraphics<?> graphics) {
			var name = graphics.getDisplayName();
			icon = new CustomGraphicsIcon(graphics, w, h, name);
		} else if (value instanceof ObjectPosition op) {
			icon = new ObjectPositionIcon(op, (ObjectPositionVisualProperty) vp, w, h, "Label");
		} else if (value instanceof Font font) {
			icon = new FontFaceIcon(font, w, h, "");
		} else if (value instanceof ArrowShape arrowShape) {
			final DArrowShape dShape;
			
			if (ArrowShapeVisualProperty.isDefaultShape(arrowShape))
				dShape = DArrowShape.getArrowShape(arrowShape);
			else
				dShape = DArrowShape.NONE;

			if (dShape.getShape() == null)
				icon = new TextIcon(value, w, h, ""); // No arrow
			else
				icon = new ArrowIcon(dShape.getShape(), w, h, dShape.getDisplayName());
		} else if (value instanceof Bend bend) {
			icon = new EdgeBendIcon(bend, w, h, value.toString());
		} else if (value instanceof EdgeStacking es) {
			icon = new EdgeStackingIcon(es, w, h, value.toString());
		} else {
			// If not found, use return value of toString() as icon.
			icon = new TextIcon(value, w, h, value.toString());
		}
		
		return icon;
	}
	
	private VisualPropertyIconFactory() {
		// Restrict instantiation
	}
}
