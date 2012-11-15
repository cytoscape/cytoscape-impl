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
import java.awt.Stroke;

import org.cytoscape.ding.DArrowShape;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.LineType;

import static org.cytoscape.ding.DVisualLexicon.*;

final class EdgeViewDefaultSupport extends AbstractViewDefaultSupport {

	private final Object lock;
	private final DEdgeDetails edgeDetails;

	private LineType lineType;

	EdgeViewDefaultSupport(final DEdgeDetails edgeDetails, final Object lock) {
		this.edgeDetails = edgeDetails;
		this.lock = lock;
	}

	@Override
	protected <V> void setViewDefault(final VisualProperty<V> vp, V value) {
		if (value == null)
			value = (V) vp.getDefault();

		// // Type check:
		// if(vp.getRange().getType().isAssignableFrom(value.getClass()) ==
		// false)
		// return;

		//
		// Check each Visual Property Type.
		// Make sure checking ALL!
		//
		if (vp == EDGE_STROKE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == EDGE_STROKE_UNSELECTED_PAINT) {
			setUnselectedPaint((Paint) value);
		} else if (vp == EDGE_UNSELECTED_PAINT) {
			setUnselectedPaint((Paint) value);
		} else if (vp == EDGE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == EDGE_TRANSPARENCY) {
			setTransparency(((Number) value).intValue());
		} else if (vp == EDGE_WIDTH) {
			final float newWidth = ((Number) value).floatValue();
			Float currentWidth = edgeDetails.m_segmentThicknessDefault.floatValue();
			if (currentWidth.floatValue() != newWidth) {
				setStrokeWidth(newWidth);
				setStroke(DLineType.getDLineType(lineType).getStroke(newWidth));
			}
		} else if (vp == EDGE_LINE_TYPE) {
			lineType = (LineType) value;
			final Stroke newStroke = DLineType.getDLineType(lineType).getStroke(edgeDetails.m_segmentThicknessDefault.floatValue());
			setStroke(newStroke);
		} else if (vp == EDGE_SOURCE_ARROW_UNSELECTED_PAINT) {
			setSourceEdgeEndUnselectedPaint((Paint) value);
		} else if (vp == EDGE_TARGET_ARROW_UNSELECTED_PAINT) {
			setTargetEdgeEndUnselectedPaint((Paint) value);
		} else if (vp == EDGE_TARGET_ARROW_SHAPE) {
			final ArrowShape shape = (ArrowShape) value;
			final String shapeID = shape.getSerializableString();
			setTargetEdgeEnd(DArrowShape.parseArrowText(shapeID).getRendererTypeID());
		} else if (vp == EDGE_SOURCE_ARROW_SHAPE) {
			final ArrowShape shape = (ArrowShape) value;
			final String shapeID = shape.getSerializableString();
			setSourceEdgeEnd(DArrowShape.parseArrowText(shapeID).getRendererTypeID());
		} else if (vp == EDGE_LABEL) {
			setText((String) value);
		} else if (vp == EDGE_LABEL_FONT_FACE) {
			final int fontSize = edgeDetails.m_labelFontDefault.getSize();
			setFont((Font) value, fontSize);
		} else if (vp == EDGE_LABEL_FONT_SIZE) {
			final int currentFontSize = edgeDetails.m_labelFontDefault.getSize();
			final int newFontSize = ((Number) value).intValue();
			if (currentFontSize != newFontSize)
				setFont(edgeDetails.m_labelFontDefault, newFontSize);
		} else if (vp == EDGE_LABEL_COLOR) {
			setTextPaint((Paint) value);
		} else if (vp == EDGE_LABEL_TRANSPARENCY) {
			final int newLabelTransparency = ((Number) value).intValue();
			final int currentLabelTransparency = edgeDetails.labelTransparencyDefault;
			if (newLabelTransparency != currentLabelTransparency)
				setTextPaint(edgeDetails.m_labelPaintDefault);
		} else if (vp == EDGE_CURVED) {
			setCurved((Boolean) value);
		} else if (vp == EDGE_BEND) {
			setBend((Bend) value);
		}
	}

	private void setBend(final Bend bend) {
		edgeDetails.setEdgeBendDefault(bend);
	}

	void setCurved(final Boolean curved) {
		synchronized (lock) {
			if (curved)
				edgeDetails.setLineCurvedDefault(EdgeView.CURVED_LINES);
			else
				edgeDetails.setLineCurvedDefault(EdgeView.STRAIGHT_LINES);
		}
	}

	void setTransparency(final int trans) {
		if (trans < 0 || trans > 255)
			edgeDetails.transparencyDefault = DVisualLexicon.EDGE_TRANSPARENCY.getDefault();
		else
			edgeDetails.transparencyDefault = trans;

		setSelectedPaint(edgeDetails.m_selectedPaintDefault);
		setUnselectedPaint(edgeDetails.m_unselectedPaintDefault);
	}

	void setStrokeWidth(float width) {
		synchronized (lock) {
			edgeDetails.setSegmentThicknessDefault(width);
		}
	}

	void setStroke(Stroke stroke) {
		synchronized (lock) {
			edgeDetails.setSegmentStrokeDefault(stroke);
		}
	}

	private void setUnselectedPaint(final Paint paint) {
		synchronized (lock) {
			final int transparency = edgeDetails.transparencyDefault;
			final Paint transColor = getTransparentColor(paint, transparency);
			edgeDetails.setSegmentPaintDefault(transColor);
			edgeDetails.setColorLowDetailDefault((Color) transColor);
		}
	}

	private void setSelectedPaint(final Paint paint) {
		synchronized (lock) {
			final Paint transColor = getTransparentColor(paint, edgeDetails.transparencyDefault);
			edgeDetails.setSelectedPaintDefault(transColor);
			edgeDetails.setSelectedColorLowDetailDefault((Color) transColor);
		}
	}

	private void setTargetEdgeEndUnselectedPaint(Paint paint) {
		synchronized (lock) {
			final Paint transColor = getTransparentColor(paint, edgeDetails.transparencyDefault);
			edgeDetails.setTargetArrowPaintDefault(transColor);
		}
	}

	public void setSourceEdgeEndUnselectedPaint(final Paint paint) {
		synchronized (lock) {
			final Paint transColor = getTransparentColor(paint, edgeDetails.transparencyDefault);
			edgeDetails.setSourceArrowPaintDefault(transColor);
		}
	}

	public void setSourceEdgeEnd(final int rendererTypeID) {
		synchronized (lock) {
			edgeDetails.setSourceArrowDefault((byte) rendererTypeID);
		}
	}

	public void setTargetEdgeEnd(final int rendererTypeID) {
		synchronized (lock) {
			edgeDetails.setTargetArrowDefault((byte) rendererTypeID);
		}
	}

	public void setTextPaint(Paint textPaint) {
		synchronized (lock) {
			final Paint transColor = getTransparentColor(textPaint, edgeDetails.labelTransparencyDefault);
			edgeDetails.setLabelPaintDefault(transColor);
		}
	}

	public void setText(final String text) {
		synchronized (lock) {
			edgeDetails.setLabelTextDefault(text);
		}
	}

	private void setFont(Font newFont, int newSize) {
		synchronized (lock) {
			Font font = edgeDetails.m_labelFontDefault;
			int fontSize = font.getSize();
			if (newFont == null)
				return;
			if (font != null && font.equals(newFont) && fontSize == newSize)
				return;

			font = newFont.deriveFont(newSize);
			edgeDetails.setLabelFontDefault(font);
		}
	}
}
