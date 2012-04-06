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
import java.util.Collection;

import org.cytoscape.ding.DArrowShape;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.LineType;


class EdgeViewDefaultSupport {

	private final Object lock;
	private final DEdgeDetails edgeDetails;
	private final VisualLexicon lexicon;
	
	EdgeViewDefaultSupport(final VisualLexicon lexicon, DEdgeDetails edgeDetails, Object lock) {
		this.edgeDetails = edgeDetails;
		this.lock = lock;
		this.lexicon = lexicon;
	}

	private Font font;
	private float fontSize;
	private LineType lineType;
	private float strokeWidth;
	
	private int transparency;
	private Integer labelTransparency = 255;
	
	private Paint unselectedPaint;
	private Paint selectedPaint;
	
	private Color labelColor;
	

	<T, V extends T> void setEdgeViewDefault(VisualProperty<? extends T> vpOriginal, V value) {
		
		final VisualProperty<?> vp = vpOriginal;
//		final VisualLexiconNode treeNode = lexicon.getVisualLexiconNode(vpOriginal);
//		
//		if(treeNode == null)
//			return;
//		
//		if(treeNode.getChildren().size() != 0) {
//			final Collection<VisualLexiconNode> children = treeNode.getChildren();
//			boolean shouldApply = false;
//			for(VisualLexiconNode node: children) {
//				if(node.isDepend()) {
//					shouldApply = true;
//					break;
//				}
//			}
//			if(shouldApply == false)
//				return;
//		}
//		
//		if(treeNode.isDepend())
//			return;
//		else
//			vp = vpOriginal;
//		
		if(value == null)
			value = (V) vp.getDefault();
		
		if (vp == DVisualLexicon.EDGE_STROKE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_UNSELECTED_PAINT) {
			// This is the parent of unselected color related visual property.
			// Will be called if dependency exists.
			setUnselectedPaint((Paint) value);
			setSourceEdgeEndUnselectedPaint((Paint) value);
			setTargetEdgeEndUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_TRANSPARENCY) {
			setTransparency(((Number) value).intValue());
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
		} else if (vp == DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT) {
			setSourceEdgeEndUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT) {
			setTargetEdgeEndUnselectedPaint((Paint) value);
		} else if (vp == BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE) {
			final ArrowShape shape = (ArrowShape) value;
			final String shapeID = shape.getSerializableString();
			setTargetEdgeEnd(DArrowShape.parseArrowText(shapeID).getRendererTypeID());
		} else if (vp == BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE) {
			final ArrowShape shape = (ArrowShape) value;
			final String shapeID = shape.getSerializableString();
			setSourceEdgeEnd(DArrowShape.parseArrowText(shapeID).getRendererTypeID());
		} else if (vp == BasicVisualLexicon.EDGE_LABEL) {
			setText((String) value);
		} else if (vp == DVisualLexicon.EDGE_LABEL_FONT_FACE) {
			setFont((Font) value, fontSize);
		} else if (vp == DVisualLexicon.EDGE_LABEL_FONT_SIZE) {
			setFont(font, (((Number) value).floatValue()));
		} else if (vp == BasicVisualLexicon.EDGE_LABEL_COLOR) {
			setTextPaint((Paint) value);
		} else if (vp == BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY) {
			if (labelColor != null && labelTransparency.equals(value) == false) {
				labelTransparency = ((Number) value).intValue();
				setTextPaint(labelColor);
			}
		} else if(vp == DVisualLexicon.EDGE_CURVED) {
			setCurved((Boolean) value);
		} else if(vp == DVisualLexicon.EDGE_BEND) {
			setBend((Bend)value);
		}
	}
	
	private void setBend(final Bend bend) {
		edgeDetails.setEdgeBendDefault(bend);
	}

	void setCurved(final Boolean curved) {
		synchronized (lock) {
			if(curved)
				edgeDetails.setLineTypeDefault(EdgeView.CURVED_LINES);
			else
				edgeDetails.setLineTypeDefault(EdgeView.STRAIGHT_LINES);
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
			edgeDetails.setColorLowDetailDefault((Color) transColor);			
		}
	}

	void setSelectedPaint(final Paint paint) {
		synchronized (lock) {
			selectedPaint = paint;
			final Paint transColor = getTransparentColor(paint);
			edgeDetails.setSelectedPaintDefault(transColor);
			edgeDetails.setSelectedColorLowDetailDefault((Color) transColor);
		}
	}
	
	private Paint getTransparentColor(Paint p) {
		if (p != null && p instanceof Color) 
			return new Color(((Color) p).getRed(), ((Color) p).getGreen(), ((Color) p).getBlue(), transparency);
		else
			return p;
	}

	public void setTargetEdgeEndUnselectedPaint(Paint paint) {
		synchronized (lock) {
			edgeDetails.setTargetArrowPaintDefault(paint);
		}
	}

	public void setSourceEdgeEndUnselectedPaint(final Paint paint) {
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
			labelColor = new Color(((Color) textPaint).getRed(), ((Color) textPaint).getGreen(),
					((Color) textPaint).getBlue(), labelTransparency);
			edgeDetails.setLabelPaintDefault(labelColor);
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
