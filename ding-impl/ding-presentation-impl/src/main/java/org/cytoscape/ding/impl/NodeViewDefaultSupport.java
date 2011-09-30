
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;

class NodeViewDefaultSupport {

	private final DNodeDetails nodeDetails;
	private final Object lock;

	
	// Default values
	private int transparency = 255;
	
	private Paint unselectedPaint;
	private Paint selectedPaint;
	
	private float fontSize = 12f;
	private Font font;
	
	
	NodeViewDefaultSupport(final DNodeDetails nodeDetails, final Object lock) {
		this.nodeDetails = nodeDetails;
		this.lock = lock;
	}

	
	<T, V extends T> void setNodeViewDefault(final VisualProperty<? extends T> vp, V value) {
		
		
		// Null means set value to VP's default.
		if(value == null)
			value = (V) vp.getDefault();

		if (vp == DVisualLexicon.NODE_SHAPE) {
			setShape(((NodeShape) value));
		} else if (vp == DVisualLexicon.NODE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == MinimalVisualLexicon.NODE_FILL_COLOR) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_PAINT) {
			setBorderPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_WIDTH) {
			setBorderWidth(((Number) value).floatValue());
		} else if (vp == DVisualLexicon.NODE_TRANSPARENCY) {
			setTransparency(((Integer) value));
		} else if (vp == MinimalVisualLexicon.NODE_LABEL) {
			setText(value.toString());
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_FACE) {
			final Font newFont = ((Font) value).deriveFont(fontSize);
			setFont(newFont,fontSize);
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_SIZE) {
			float newSize = ((Number) value).floatValue();
			setFont(font,newSize);
		} else if (vp == DVisualLexicon.NODE_TOOLTIP) {
			setToolTip((String) value);
		} else if (vp == MinimalVisualLexicon.NODE_LABEL_COLOR) {
			setTextPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_LABEL_POSITION) {
			this.setLabelPosition((ObjectPosition) value);
		}
	}

	private void setToolTip(String value) {
		// TODO Auto-generated method stub
		
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
			nodeDetails.setSelectedPaintDefault(getTransparentColor(paint));
		}
	}

	void setUnselectedPaint(Paint paint) {
		synchronized (lock) {
			unselectedPaint = paint;
			nodeDetails.setFillPaintDefault(getTransparentColor(paint));
			nodeDetails.setColorLowDetailDefault((Color)getTransparentColor(paint));
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
