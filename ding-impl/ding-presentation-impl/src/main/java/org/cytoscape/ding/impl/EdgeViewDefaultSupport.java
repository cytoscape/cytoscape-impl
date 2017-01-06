package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

import static org.cytoscape.ding.DVisualLexicon.EDGE_CURVED;
import static org.cytoscape.ding.DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT;
import static org.cytoscape.ding.DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_BEND;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SOURCE_ARROW_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;

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
				setStroke(DLineType.getDLineType(lineType).getStroke(newWidth), lineType);
			}
		} else if (vp == EDGE_LINE_TYPE) {
			lineType = (LineType) value;
			final Stroke newStroke = DLineType.getDLineType(lineType).getStroke(edgeDetails.m_segmentThicknessDefault.floatValue());
			setStroke(newStroke, lineType);
		} else if (vp == EDGE_SOURCE_ARROW_UNSELECTED_PAINT) {
			setSourceEdgeEndUnselectedPaint((Paint) value);
		} else if (vp == EDGE_TARGET_ARROW_UNSELECTED_PAINT) {
			setTargetEdgeEndUnselectedPaint((Paint) value);
		} else if (vp == EDGE_TARGET_ARROW_SHAPE) {
			final ArrowShape shape = (ArrowShape) value;
			final String shapeID = shape.getSerializableString();
			setTargetEdgeEnd(DArrowShape.parseArrowText(shapeID).getPresentationShape());
		} else if (vp == EDGE_SOURCE_ARROW_SHAPE) {
			final ArrowShape shape = (ArrowShape) value;
			final String shapeID = shape.getSerializableString();
			setSourceEdgeEnd(DArrowShape.parseArrowText(shapeID).getPresentationShape());
		} else if (vp == EDGE_SOURCE_ARROW_SIZE) {
			double newSize = ((Number) value).doubleValue();
			setSourceArrowSize(newSize);
		} else if (vp == EDGE_TARGET_ARROW_SIZE) {
			double newSize = ((Number) value).doubleValue();
			setTargetArrowSize(newSize);
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
				setLabelTransparency(newLabelTransparency);
		} else if (vp == DVisualLexicon.EDGE_LABEL_WIDTH) {
			double newSize = ((Number) value).doubleValue();
			setLabelWidth(newSize);
		} else if (vp == EDGE_CURVED) {
			setCurved((Boolean) value);
		} else if (vp == EDGE_BEND) {
			setBend((Bend) value);
		} else {
			synchronized (lock) {
				edgeDetails.setDefaultValue(vp, value);
			}
		}
	}

	private void setBend(final Bend bend) {
		synchronized (lock) {
			edgeDetails.setEdgeBendDefault(bend);
		}
	}

	private void setCurved(final Boolean curved) {
		synchronized (lock) {
			if (curved)
				edgeDetails.setLineCurvedDefault(EdgeView.CURVED_LINES);
			else
				edgeDetails.setLineCurvedDefault(EdgeView.STRAIGHT_LINES);
		}
	}

	private void setTransparency(int trans) {
		synchronized (lock) {
			if (trans < 0 || trans > 255)
				trans = DVisualLexicon.EDGE_TRANSPARENCY.getDefault();
			
			edgeDetails.setTransparencyDefault(trans);
		}

		setSelectedPaint(edgeDetails.m_selectedPaintDefault);
		setUnselectedPaint(edgeDetails.m_unselectedPaintDefault);
	}
	
	private void setLabelTransparency(int trans) {
		synchronized (lock) {
			if (trans < 0 || trans > 255)
				trans = DVisualLexicon.EDGE_LABEL_TRANSPARENCY.getDefault();
	
			edgeDetails.setLabelTransparencyDefault(trans);
		}
		
		setTextPaint(edgeDetails.m_labelPaintDefault);
	}
	
	void setLabelWidth(double width) {
		synchronized (lock) {
			edgeDetails.setLabelWidthDefault(width);
		}
	}

	private void setStrokeWidth(float width) {
		synchronized (lock) {
			edgeDetails.setSegmentThicknessDefault(width);
		}
	}

	private void setStroke(Stroke stroke, LineType type) {
		synchronized (lock) {
			edgeDetails.setSegmentStrokeDefault(stroke, type);
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

	public void setSourceEdgeEnd(final ArrowShape arrowShape) {
		synchronized (lock) {
			edgeDetails.setSourceArrowDefault(arrowShape);
		}
	}

	public void setTargetEdgeEnd(final ArrowShape arrowShape) {
		synchronized (lock) {
			edgeDetails.setTargetArrowDefault(arrowShape);
		}
	}
	
	private void setSourceArrowSize(final double size) {
		synchronized (lock) {
			edgeDetails.setSourceArrowSizeDefault(size);
		}
	}
	
	private void setTargetArrowSize(final double size) {
		synchronized (lock) {
			edgeDetails.setTargetArrowSizeDefault(size);
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

	private void setFont(Font newFont, float newSize) {
		synchronized (lock) {
			if (newFont == null)
				return;
			
			Font font = edgeDetails.m_labelFontDefault;
			float fontSize = font.getSize();
			
			if (font != null && font.equals(newFont) && fontSize == newSize)
				return;

			font = newFont.deriveFont(newSize);
			edgeDetails.setLabelFontDefault(font);
		}
	}
}
