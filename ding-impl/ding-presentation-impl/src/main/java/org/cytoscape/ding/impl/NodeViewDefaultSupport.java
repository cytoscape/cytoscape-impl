
/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.ding.impl;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.Label;
import org.cytoscape.ding.NodeView;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.customgraphics.Layer;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.impl.customgraphics.CustomGraphicsPositionCalculator;
import org.cytoscape.ding.impl.customgraphics.vector.VectorCustomGraphics;
import org.cytoscape.ding.impl.visualproperty.CustomGraphicsVisualProperty;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.CustomGraphic;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;

class NodeViewDefaultSupport {

	private final DNodeDetails nodeDetails;
	private final Object lock;

	private int transparency = 255;
	private Paint unselectedPaint;
	private Paint selectedPaint;
	private float fontSize = 12f;
	private Font font;

	
	NodeViewDefaultSupport(DNodeDetails nodeDetails, Object lock) {
		this.nodeDetails = nodeDetails;
		this.lock = lock;
	}

	<T, V extends T> void setNodeViewDefault(final VisualProperty<? extends T> vp, V value) {
		
		if(value == null)
			value = (V) vp.getDefault();

		if (vp == DVisualLexicon.NODE_SHAPE) {
			setShape(((NodeShape) value));
		} else if (vp == MinimalVisualLexicon.NODE_FILL_COLOR) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_PAINT) {
			setBorderPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_WIDTH) {
			setBorderWidth(((Number) value).floatValue());
		} else if (vp == DVisualLexicon.NODE_TRANSPARENCY) {
			setTransparency(((Integer) value));
		} else if (vp == MinimalVisualLexicon.NODE_LABEL) {
			setText(value.toString());
		} else if (vp == MinimalVisualLexicon.NODE_LABEL_COLOR) {
			setTextPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_FACE) {
			final Font newFont = ((Font) value).deriveFont(fontSize);
			setFont(newFont,fontSize);
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_SIZE) {
			float newSize = ((Number) value).floatValue();
			setFont(font,newSize);
		} else if (vp == DVisualLexicon.NODE_LABEL_POSITION) {
			setLabelPosition((ObjectPosition) value);
		} 
	}

	void setShape(final NodeShape shape) {
		synchronized (lock) {
			final DNodeShape dShape;
			if (NodeShapeVisualProperty.isDefaultShape(shape))
				dShape = DNodeShape.getDShape(shape);
			else
				dShape = (DNodeShape) shape;

			nodeDetails.setShapeDefault(dShape);
		}
	}
	
	void setSelectedPaint(Paint paint) {
		synchronized (lock) {
			selectedPaint = paint;
			nodeDetails.setColorLowDetailDefault((Color)getTransparentColor(paint));
		}
	}

	void setUnselectedPaint(Paint paint) {
		synchronized (lock) {
			unselectedPaint = paint;
			nodeDetails.setFillPaintDefault(getTransparentColor(paint));
		}
	}

	private Paint getTransparentColor(Paint p) {
		if (p != null && p instanceof Color) 
			return new Color(((Color) p).getRed(), ((Color) p).getGreen(), ((Color) p).getBlue(), transparency);
		else
			return p;
	}

	void setTransparency(int trans) {
		transparency = trans;
		setSelectedPaint(selectedPaint);
		setUnselectedPaint(unselectedPaint);
	}

	
	void setBorderPaint(Paint paint) {
		synchronized (lock) {
			nodeDetails.setBorderPaintDefault(paint);
		}
	}

	void setBorderWidth(float width) {
		synchronized (lock) {
			nodeDetails.setBorderWidthDefault(width);
		}
	}

	void setTextPaint(Paint textPaint) {
		synchronized (lock) {
			nodeDetails.setLabelPaintDefault(textPaint);
		}
	}

	void setText(String text) {
		synchronized (lock) {
			nodeDetails.setLabelTextDefault(text);
		}
	}

	void setFont(Font newFont, float newSize) {
		synchronized (lock) {
			if ( newFont == null )
				return;
		 	if ( font != null && font.equals(newFont) && fontSize == newSize )
				return;

			font = newFont.deriveFont(newSize);
			fontSize = newSize;
			nodeDetails.setLabelFontDefault(font);
		}
	}

	void setLabelWidth(double width) {
		synchronized (lock) {
			nodeDetails.setLabelWidthDefault(width);
		}
	}

	public void setLabelPosition(final ObjectPosition labelPosition) {
		synchronized (lock) {
			nodeDetails.setLabelTextAnchorDefault(labelPosition.getAnchor().getConversionConstant());
			nodeDetails.setLabelNodeAnchorDefault(labelPosition.getTargetAnchor().getConversionConstant());
			nodeDetails.setLabelJustifyDefault(labelPosition.getJustify().getConversionConstant());
			nodeDetails.setLabelOffsetVectorXDefault(labelPosition.getOffsetX());
			nodeDetails.setLabelOffsetVectorYDefault(labelPosition.getOffsetY());
		}
	}
}
