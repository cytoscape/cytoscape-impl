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

import org.cytoscape.ding.ArrowShape;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.values.LineType;


class EdgeViewDefaultSupport {

	private final Object lock;
	private final DEdgeDetails edgeDetails;
	
	EdgeViewDefaultSupport(DEdgeDetails edgeDetails, Object lock) {
		this.edgeDetails = edgeDetails;
		this.lock = lock;
	}

	private Font font;
	private float fontSize;
	private LineType lineType;
	private float strokeWidth;
	
	private int transparency;
	
	private Paint unselectedPaint;
	private Paint selectedPaint;

	<T, V extends T> void setEdgeViewDefault(VisualProperty<? extends T> vp, V value) {
		
		if(value == null)
			value = (V) vp.getDefault();
		
		if (vp == DVisualLexicon.EDGE_STROKE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_TRANSPARENCY) {
			setTransparency(((Number) value).intValue());
		} else if (vp == DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
			setSourceEdgeEndSelectedPaint((Paint) value);			
			setTargetEdgeEndSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_UNSELECTED_PAINT) {
			setSourceEdgeEndUnselectedPaint((Paint) value);
			setTargetEdgeEndUnselectedPaint((Paint) value);
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_WIDTH) {
			final float newWidth = ((Number) value).floatValue();	
			if(strokeWidth != newWidth) {
				strokeWidth = newWidth;
				setStrokeWidth(newWidth);
				setStroke(DLineType.getDLineType(lineType).getStroke(newWidth));
			}
		} else if (vp == DVisualLexicon.EDGE_LINE_TYPE) {
			lineType = (LineType) value;
			final Stroke newStroke = DLineType.getDLineType(lineType).getStroke(strokeWidth);
			setStroke(newStroke);
		} else if (vp == DVisualLexicon.EDGE_SOURCE_ARROW_SELECTED_PAINT) {
			setSourceEdgeEndSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_TARGET_ARROW_SELECTED_PAINT) {
			setTargetEdgeEndSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT) {
			setSourceEdgeEndUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT) {
			setTargetEdgeEndUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_TARGET_ARROW_SHAPE) {
			setTargetEdgeEnd(((ArrowShape) value).getRendererTypeID());
		} else if (vp == DVisualLexicon.EDGE_SOURCE_ARROW_SHAPE) {
			setSourceEdgeEnd(((ArrowShape) value).getRendererTypeID());
		} else if (vp == MinimalVisualLexicon.EDGE_LABEL) {
			setText((String) value);
		} else if (vp == DVisualLexicon.EDGE_LABEL_FONT_FACE) {
			setFont((Font) value, fontSize);
		} else if (vp == DVisualLexicon.EDGE_LABEL_FONT_SIZE) {
			setFont(font, (((Integer) value).floatValue()));
		} else if (vp == MinimalVisualLexicon.EDGE_LABEL_COLOR) {
			setTextPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_LABEL_POSITION) {
			// FIXME: Not implemented yet.
		}
	}
		

	void setTransparency(int trans) {
		transparency = trans;
		setSelectedPaint(selectedPaint);
		setUnselectedPaint(unselectedPaint);
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

	void setUnselectedPaint(final Paint paint) {
		synchronized (lock) {
			unselectedPaint = paint;
			final Paint transColor = getTransparentColor(paint);
			edgeDetails.setSegmentPaintDefault(transColor);
			edgeDetails.setColorLowDetailDefault(transColor);
		}
	}

	void setSelectedPaint(final Paint paint) {
		synchronized (lock) {
			selectedPaint = paint;
			edgeDetails.setSelectedPaintDefault(getTransparentColor(paint));
		}
	}
	
	private Paint getTransparentColor(Paint p) {
		if (p != null && p instanceof Color) 
			return new Color(((Color) p).getRed(), ((Color) p).getGreen(), ((Color) p).getBlue(), transparency);
		else
			return p;
	}

	public void setSourceEdgeEndSelectedPaint(Paint paint) {
		synchronized (lock) {
			edgeDetails.setSourceArrowSelectedPaintDefault(paint);
		}
	}

	public void setTargetEdgeEndSelectedPaint(Paint paint) {
		synchronized (lock) {
			edgeDetails.setTargetArrowSelectedPaintDefault(paint);
		}
	}

	public void setTargetEdgeEndUnselectedPaint(Paint paint) {
		synchronized (lock) {
			edgeDetails.setTargetArrowPaintDefault(paint);
		}
	}

	public void setSourceEdgeEndUnselectedPaint(Paint paint) {
		synchronized (lock) {
			edgeDetails.setSourceArrowPaintDefault(paint);
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
			edgeDetails.setLabelPaintDefault(textPaint);
		}
	}


	public void setText(final String text) {
		synchronized (lock) {
			edgeDetails.setLabelTextDefault(text);
		}
	}

	public void setFont(Font newFont, float newSize) {
		synchronized (lock) {
			if ( newFont == null )
				return;
			if ( font != null && font.equals(newFont) && fontSize == newSize )
				return;
			
			font = newFont.deriveFont(newSize);
			fontSize = newSize;
			edgeDetails.setLabelFontDefault(font);
		}
	}


	public void setLabelWidth(double width) {
		synchronized (lock) {
			edgeDetails.setLabelWidthDefault(width);
		}
	}

}
