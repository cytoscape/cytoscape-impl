package org.cytoscape.ding.icon;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.impl.DLineType;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;


/**
 * Static factory for icons.
 *
 */
public class VisualPropertyIconFactory {	
	
	public static <V> Icon createIcon(VisualProperty<V> vp, V value, int w, int h) {
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
			icon = new CustomGraphicsIcon(((CyCustomGraphics) value), w, h, ((CyCustomGraphics) value).getDisplayName());
		} else if(value instanceof ObjectPosition) {
			icon = new ObjectPositionIcon((ObjectPosition) value, w, h, "Label");
		}  else if(value instanceof Font) {
			icon = new FontFaceIcon((Font) value, w, h, "");
		} else {
			// If not found, use text as icon.
			icon = new TextIcon(value, w, h, value.toString());
		}
		
		return icon;
	}
}
