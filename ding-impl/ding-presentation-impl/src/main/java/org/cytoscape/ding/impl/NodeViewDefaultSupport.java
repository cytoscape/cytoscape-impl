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

import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;

final class NodeViewDefaultSupport extends AbstractViewDefaultSupport {

	private final DNodeDetails nodeDetails;
	private final Object lock;

	NodeViewDefaultSupport(final DNodeDetails nodeDetails, final Object lock) {
		this.nodeDetails = nodeDetails;
		this.lock = lock;
	}

	<V> void setNodeViewDefault(final VisualProperty<?> vpOriginal, V value) {
		final VisualProperty<?> vp = vpOriginal;

		// Null means set value to VP's default.
		if (value == null)
			value = (V) vp.getDefault();

		if (vp == DVisualLexicon.NODE_SHAPE) {
			setShape(((NodeShape) value));
		} else if (vp == DVisualLexicon.NODE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == BasicVisualLexicon.NODE_FILL_COLOR) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_PAINT) {
			setBorderPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_LINE_TYPE) {
			setBorderLineType((LineType) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_TRANSPARENCY) {
			setBorderTransparency(((Number) value).intValue());
		} else if (vp == DVisualLexicon.NODE_BORDER_WIDTH) {
			setBorderWidth(((Number) value).floatValue());
		} else if (vp == DVisualLexicon.NODE_TRANSPARENCY) {
			setTransparency(((Number) value).intValue());
		} else if (vp == BasicVisualLexicon.NODE_LABEL) {
			setText(value.toString());
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_FACE) {
			final int currentFontSize = nodeDetails.m_labelFontDefault.getSize();
			final Font newFont = ((Font) value).deriveFont(currentFontSize);
			setFont(newFont);
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_SIZE) {
			final float newFontSize = ((Number) value).floatValue();
			final Font newFont = nodeDetails.m_labelFontDefault.deriveFont(newFontSize);
			setFont(newFont);
		} else if (vp == DVisualLexicon.NODE_TOOLTIP) {
			setToolTip((String) value);
		} else if (vp == BasicVisualLexicon.NODE_LABEL_COLOR) {
			setTextPaint((Paint) value);
		} else if (vp == BasicVisualLexicon.NODE_LABEL_TRANSPARENCY) {
			setLabelTransparency(((Number) value).intValue());
		} else if (vp == DVisualLexicon.NODE_LABEL_POSITION) {
			this.setLabelPosition((ObjectPosition) value);
		} else if (vp == DVisualLexicon.NODE_LABEL_WIDTH) {
			double newSize = ((Number) value).doubleValue();
			setLabelWidth(newSize);
		}
	}

	private void setToolTip(String tooltip) {
		synchronized (lock) {
			nodeDetails.setTooltipTextDefault(tooltip);
		}
	}

	void setShape(final NodeShape shape) {
		synchronized (lock) {
			if (shape == null)
				nodeDetails.setShapeDefault(NodeShapeVisualProperty.RECTANGLE);
			else
				nodeDetails.setShapeDefault(shape);
		}
	}

	void setSelectedPaint(Paint paint) {
		synchronized (lock) {
			nodeDetails.setSelectedPaintDefault(getTransparentColor(paint, nodeDetails.transparencyDefault));
			nodeDetails.setSelectedColorLowDetailDefault((Color) getTransparentColor(paint,
					nodeDetails.transparencyDefault));
		}
	}

	void setUnselectedPaint(Paint paint) {
		synchronized (lock) {
			nodeDetails.setUnselectedPaintDefault(getTransparentColor(paint, nodeDetails.transparencyDefault));
			nodeDetails.setColorLowDetailDefault((Color) getTransparentColor(paint, nodeDetails.transparencyDefault));
		}
	}

	void setTransparency(int trans) {
		nodeDetails.setTransparencyDefault(trans);

		setSelectedPaint(nodeDetails.m_selectedPaintDefault);
		setUnselectedPaint(nodeDetails.m_unselectedPaintDefault);
	}
	
	void setBorderTransparency(int trans) {
		nodeDetails.setBorderTransparencyDefault(trans);
		setBorderPaint(nodeDetails.m_borderPaintDefault);
	}
	
	void setLabelTransparency(int trans) {
		nodeDetails.setLabelTransparencyDefault(trans);
		setTextPaint(nodeDetails.m_labelPaintDefault);
	}

	void setBorderPaint(Paint paint) {
		synchronized (lock) {
			final Paint borderColor = getTransparentColor(paint, nodeDetails.transparencyBorderDefault);
			nodeDetails.setBorderPaintDefault(borderColor);
		}
	}

	void setBorderWidth(float width) {
		synchronized (lock) {
			nodeDetails.setBorderWidthDefault(width);
		}
	}
	
	void setBorderLineType(final LineType lineType) {
		synchronized (lock) {
			nodeDetails.setBorderLineTypeDefault(lineType);
		}
	}

	void setTextPaint(Paint textPaint) {
		synchronized (lock) {
			final Paint trasnparentColor = getTransparentColor(textPaint, nodeDetails.transparencyLabelDefault);
			nodeDetails.setLabelPaintDefault(trasnparentColor);
		}
	}

	void setText(String text) {
		synchronized (lock) {
			nodeDetails.setLabelTextDefault(text);
		}
	}

	void setFont(Font newFont) {
		synchronized (lock) {
			nodeDetails.setLabelFontDefault(newFont);
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
