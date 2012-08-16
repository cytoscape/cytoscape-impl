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


import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import org.cytoscape.ding.DArrowShape;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.Label;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.presentation.property.values.LineType;


/**
 * Ding implementation of Edge View.
 */
public class DEdgeView extends AbstractDViewModel<CyEdge> implements EdgeView, Label, EdgeAnchors {

	// Parent network view.  This view exists only in this network view.
	private final DGraphView dGraphView;
	private final HandleFactory handleFacgtory;

	// Since Fonts are created from size and font face, we need this local value.
	private Integer fontSize;
	private LineType lineType;
	private boolean selected;
	
	DEdgeView(final DGraphView dGraphView, final CyEdge model, final HandleFactory handleFactory) {
		super(model);

		if (dGraphView == null)
			throw new IllegalArgumentException("Constructor needs its parent DGraphView.");

		this.handleFacgtory = handleFactory;
		this.dGraphView = dGraphView;
		this.selected = false;
		this.fontSize = DVisualLexicon.EDGE_LABEL_FONT_SIZE.getDefault();
	}

	@Override
	public CyEdge getCyEdge() {
		return model;
	}


	@Override
	public GraphView getGraphView() {
		return dGraphView;
	}


	@Override
	public void setStrokeWidth(final float width) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_edgeDetails.overrideSegmentThickness(model, width);
			dGraphView.m_contentChanged = true;
		}
	}


	@Override
	public void setStroke(Stroke stroke) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_edgeDetails.overrideSegmentStroke(model, stroke);
			dGraphView.m_contentChanged = true;
		}
	}


	@Override
	public void setLineCurved(int lineType) {
		if ((lineType == EdgeView.CURVED_LINES) || (lineType == EdgeView.STRAIGHT_LINES)) {
			synchronized (dGraphView.m_lock) {
				dGraphView.m_edgeDetails.overrideLineCurved(model, lineType);
				dGraphView.m_contentChanged = true;
			}
		} else
			throw new IllegalArgumentException("unrecognized line type");
	}


	@Override
	public void setUnselectedPaint(final Paint paint) {
		synchronized (dGraphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");
			
			final Paint transpColor = getTransparentColor(paint, dGraphView.m_edgeDetails.getTransparency(model));
			
			if (!isSelected()) {
				dGraphView.m_edgeDetails.setUnselectedPaint(model, transpColor);
				dGraphView.m_contentChanged = true;
			}
		}
	}


	@Override
	public void setSelectedPaint(final Paint paint) {
		synchronized (dGraphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, dGraphView.m_edgeDetails.getTransparency(model));
			
			if (isSelected()) {
				dGraphView.m_edgeDetails.setSelectedPaint(model, transpColor);
				dGraphView.m_contentChanged = true;
			}
		}
	}

	@Override
	public void setSourceEdgeEndSelectedPaint(Paint paint) {
		synchronized (dGraphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, dGraphView.m_edgeDetails.getTransparency(model));
			if (isSelected()) {
				dGraphView.m_edgeDetails.overrideSourceArrowSelectedPaint(model, transpColor);
				dGraphView.m_contentChanged = true;
			}
		}
	}

	@Override
	public void setTargetEdgeEndSelectedPaint(Paint paint) {
		synchronized (dGraphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, dGraphView.m_edgeDetails.getTransparency(model));
			if (isSelected()) {
				dGraphView.m_edgeDetails.overrideTargetArrowSelectedPaint(model, transpColor);
				dGraphView.m_contentChanged = true;
			}
		}
	}

	@Override
	public void setSourceEdgeEndPaint(final Paint paint) {
		synchronized (dGraphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, dGraphView.m_edgeDetails.getTransparency(model));
			
			if (!isSelected()) {
				dGraphView.m_edgeDetails.overrideSourceArrowPaint(model, transpColor);
				dGraphView.m_contentChanged = true;
			}
		}
	}

	@Override
	public void setTargetEdgeEndPaint(Paint paint) {
		synchronized (dGraphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, dGraphView.m_edgeDetails.getTransparency(model));
			dGraphView.m_edgeDetails.overrideTargetArrowPaint(model, transpColor);
			
			if (!isSelected()) {
				dGraphView.m_contentChanged = true;
			}
		}
	}

	private final void select() {
		final boolean somethingChanged;

		synchronized (dGraphView.m_lock) {
			somethingChanged = selectInternal(false);

			if (somethingChanged)
				dGraphView.m_contentChanged = true;
		}
	}

	// Should synchronize around m_view.m_lock.
	boolean selectInternal(boolean selectAnchors) {
		if (selected)
			return false;

		selected = true;
		dGraphView.m_edgeDetails.select(model);		
		dGraphView.m_selectedEdges.insert(model.getSUID());

		List<Handle> handles = dGraphView.m_edgeDetails.getBend(model).getAllHandles();
		for (int j = 0; j < handles.size(); j++) {
			final Handle handle = handles.get(j);
			final Point2D newPoint = handle.calculateHandleLocation(dGraphView.getViewModel(),this);
			final double x = newPoint.getX();
			final double y = newPoint.getY();
			final double halfSize = dGraphView.getAnchorSize() / 2.0;
			
			dGraphView.m_spacialA.insert((model.getSUID() << 6) | j,
					(float) (x - halfSize), (float) (y - halfSize),
					(float) (x + halfSize), (float) (y + halfSize));

			if (selectAnchors)
				dGraphView.m_selectedAnchors.insert((model.getSUID() << 6) | j);
		}
		return true;
	}

	public void unselect() {
		final boolean somethingChanged;

		synchronized (dGraphView.m_lock) {
			somethingChanged = unselectInternal();

			if (somethingChanged)
				dGraphView.m_contentChanged = true;
		}
	}

	// Should synchronize around m_view.m_lock.
	boolean unselectInternal() {
		if (!selected)
			return false;

		selected = false;
		dGraphView.m_edgeDetails.unselect(model);
		dGraphView.m_selectedEdges.delete(model.getSUID());

		final int numHandles = dGraphView.m_edgeDetails.getBend(model).getAllHandles().size();
		for (int j = 0; j < numHandles; j++) {
			dGraphView.m_selectedAnchors.delete((model.getSUID() << 6) | j);
			dGraphView.m_spacialA.delete((model.getSUID() << 6) | j);
		}
		return true;
	}

	@Override
	public boolean setSelected(boolean state) {
		if (state)
			select();
		else
			unselect();

		return true;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	
	@Override
	public void setSourceEdgeEnd(final int rendererTypeID) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_edgeDetails.overrideSourceArrow(model, (byte) rendererTypeID);
		}

		dGraphView.m_contentChanged = true;
	}

	@Override
	public void setTargetEdgeEnd(final int rendererTypeID) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_edgeDetails.overrideTargetArrow(model, (byte) rendererTypeID);
		}

		dGraphView.m_contentChanged = true;

	}


	@Override
	public void setToolTip(String tip) {
		dGraphView.m_edgeDetails.m_edgeTooltips.put(model, tip);
	}


	@Override
	public Paint getTextPaint() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.m_edgeDetails.getLabelPaint(model, 0);
		}
	}

	@Override
	public void setTextPaint(Paint textPaint) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_edgeDetails.overrideLabelPaint(model, 0, textPaint);
			dGraphView.m_contentChanged = true;
		}
	}

	@Override
	public String getText() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.m_edgeDetails.getLabelText(model, 0);
		}
	}

	@Override
	public void setText(final String text) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_edgeDetails.overrideLabelText(model, 0, text);

			if ("".equals(dGraphView.m_edgeDetails.getLabelText(model, 0)))
				dGraphView.m_edgeDetails.overrideLabelCount(model, 0); // TODO is this correct?
			else
				dGraphView.m_edgeDetails.overrideLabelCount(model, 1);

			dGraphView.m_contentChanged = true;
		}
	}

	@Override
	public Font getFont() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.m_edgeDetails.getLabelFont(model, 0);
		}
	}
	
	@Override
	public void setFont(final Font font) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_edgeDetails.overrideLabelFont(model, 0, font);
			dGraphView.m_contentChanged = true;
		}
	}

	protected final void moveHandleInternal(final int inx, double x, double y) {
		final Bend bend = dGraphView.m_edgeDetails.getBend(model);
		final HandleImpl handle = (HandleImpl) bend.getAllHandles().get(inx);
		handle.defineHandle(dGraphView.getViewModel(), this, x, y);

		if (dGraphView.m_spacialA.delete((model.getSUID() << 6) | inx))
			dGraphView.m_spacialA.insert((model.getSUID() << 6) | inx,
					(float) (x - (dGraphView.getAnchorSize() / 2.0d)),
					(float) (y - (dGraphView.getAnchorSize() / 2.0d)),
					(float) (x + (dGraphView.getAnchorSize() / 2.0d)),
					(float) (y + (dGraphView.getAnchorSize() / 2.0d)));
	}

	/**
	 * Add a new handle and returns its index.
	 * 
	 * @param pt location of handle
	 * @return new handle index.
	 */
	protected int addHandlePoint(final Point2D pt) {
		synchronized (dGraphView.m_lock) {
			
			// Obtain existing Bend object
			final Bend bend = dGraphView.m_edgeDetails.getBend(model, true);
			
			if (bend.getAllHandles().size() == 0) {
				// anchors object is empty. Add first handle.
				addHandleInternal(0, pt);
				// Index of this handle, which is first (0)
				return 0;
			}

			final Point2D sourcePt = dGraphView.getDNodeView(getCyEdge().getSource()).getOffset();
			final Point2D targetPt = dGraphView.getDNodeView(getCyEdge().getTarget()).getOffset();
			final Handle firstHandle = bend.getAllHandles().get(0); 
			final Point2D point = firstHandle.calculateHandleLocation(dGraphView.getViewModel(),this);
			double bestDist = (pt.distance(sourcePt) + pt.distance(point)) - sourcePt.distance(point);
			int bestInx = 0;

			for (int i = 1; i < bend.getAllHandles().size(); i++) {
				final Handle handle1 = bend.getAllHandles().get(i);
				final Handle handle2 = bend.getAllHandles().get(i-1);
				final Point2D point1 = handle1.calculateHandleLocation(dGraphView.getViewModel(),this);
				final Point2D point2 = handle2.calculateHandleLocation(dGraphView.getViewModel(),this);

				final double distCand = (pt.distance(point2) + pt.distance(point1)) - point1.distance(point2);

				if (distCand < bestDist) {
					bestDist = distCand;
					bestInx = i;
				}
			}

			final int lastIndex = bend.getAllHandles().size() - 1;
			final Handle lastHandle = bend.getAllHandles().get(lastIndex);
			final Point2D lastPoint = lastHandle.calculateHandleLocation(dGraphView.getViewModel(),this);
			
			final double lastCand = (pt.distance(targetPt) + pt.distance(lastPoint)) - targetPt.distance(lastPoint);

			if (lastCand < bestDist) {
				bestDist = lastCand;
				bestInx = bend.getAllHandles().size();
			}

			addHandleInternal(bestInx, pt);

			return bestInx;
		}
	}
	
	/**
	 * Insert a new handle to bend object.
	 * 
	 * @param insertInx
	 * @param handleLocation
	 */
	private void addHandleInternal(final int insertInx, final Point2D handleLocation) {
		synchronized (dGraphView.m_lock) {
			final Bend bend = dGraphView.m_edgeDetails.getBend(model);			
			final Handle handle = handleFacgtory.createHandle(dGraphView, this, handleLocation.getX(), handleLocation.getY());
			bend.insertHandleAt(insertInx, handle);

			if (selected) {
				for (int j = bend.getAllHandles().size() - 1; j > insertInx; j--) {
					dGraphView.m_spacialA.exists((model.getSUID() << 6) | (j - 1),
							dGraphView.m_extentsBuff, 0);
					dGraphView.m_spacialA.delete((model.getSUID() << 6) | (j - 1));
					dGraphView.m_spacialA.insert((model.getSUID() << 6) | j,
							dGraphView.m_extentsBuff[0], dGraphView.m_extentsBuff[1],
							dGraphView.m_extentsBuff[2], dGraphView.m_extentsBuff[3]);

					if (dGraphView.m_selectedAnchors.delete((model.getSUID() << 6) | (j - 1)))
						dGraphView.m_selectedAnchors.insert((model.getSUID() << 6) | j);
				}
				
				dGraphView.m_spacialA.insert((model.getSUID() << 6) | insertInx,
						(float) (handleLocation.getX() - (dGraphView.getAnchorSize() / 2.0d)),
						(float) (handleLocation.getY() - (dGraphView.getAnchorSize() / 2.0d)),
						(float) (handleLocation.getX() + (dGraphView.getAnchorSize() / 2.0d)),
						(float) (handleLocation.getY() + (dGraphView.getAnchorSize() / 2.0d)));
			}

			dGraphView.m_contentChanged = true;
		}
	}

	void removeHandle(int inx) {
		synchronized (dGraphView.m_lock) {
			final Bend bend = dGraphView.m_edgeDetails.getBend(model);
			bend.removeHandleAt(inx);
			//m_anchors.remove(inx);

			if (selected) {
				dGraphView.m_spacialA.delete((model.getSUID() << 6) | inx);
				dGraphView.m_selectedAnchors.delete((model.getSUID() << 6) | inx);

				for (int j = inx; j < bend.getAllHandles().size(); j++) {
					dGraphView.m_spacialA.exists((model.getSUID() << 6) | (j + 1),
							dGraphView.m_extentsBuff, 0);
					dGraphView.m_spacialA.delete((model.getSUID() << 6) | (j + 1));
					dGraphView.m_spacialA.insert((model.getSUID() << 6) | j,
							dGraphView.m_extentsBuff[0], dGraphView.m_extentsBuff[1],
							dGraphView.m_extentsBuff[2], dGraphView.m_extentsBuff[3]);

					if (dGraphView.m_selectedAnchors.delete((model.getSUID() << 6) | (j + 1)))
						dGraphView.m_selectedAnchors.insert((model.getSUID() << 6) | j);
				}
			}
			dGraphView.m_contentChanged = true;
		}
	}

	// Interface org.cytoscape.graph.render.immed.EdgeAnchors:
	@Override
	public int numAnchors() {
		final Bend bend; 
		if(isValueLocked(DVisualLexicon.EDGE_BEND))
			bend = this.getVisualProperty(DVisualLexicon.EDGE_BEND);
		else
			bend = dGraphView.m_edgeDetails.getBend(model);
		
		final int numHandles = bend.getAllHandles().size();
		
		if (numHandles == 0)
			return 0;
		
		if (dGraphView.m_edgeDetails.getLineCurved(model) == EdgeView.CURVED_LINES)
			return numHandles;
		else
			return 2 * numHandles;
	}

	/**
	 * Actual method to be used in the Graph Renderer.
	 */
	@Override
	public void getAnchor(int anchorIndex, float[] anchorArr, int offset) {
		final Bend bend; 
		if(isValueLocked(DVisualLexicon.EDGE_BEND))
			bend = this.getVisualProperty(DVisualLexicon.EDGE_BEND);
		else
			bend = dGraphView.m_edgeDetails.getBend(model);
		
		final Handle handle;
		if (dGraphView.m_edgeDetails.getLineCurved(model) == EdgeView.CURVED_LINES)
			handle = bend.getAllHandles().get(anchorIndex);
		else
			handle = bend.getAllHandles().get(anchorIndex/2);

		final Point2D newPoint = handle.calculateHandleLocation(dGraphView.getViewModel(),this);
		anchorArr[offset] = (float) newPoint.getX();
		anchorArr[offset + 1] = (float) newPoint.getY();
	}

	public void setLabelWidth(double width) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_edgeDetails.overrideLabelWidth(model, width);
			dGraphView.m_contentChanged = true;
		}
	}


	@Override
	public void setTransparency(final int trans) {
		synchronized (dGraphView.m_lock) {
			Integer transparency;
			if (trans < 0 || trans > 255) {
				// If out of range, use default value.
				transparency = BasicVisualLexicon.EDGE_TRANSPARENCY.getDefault();
			} else {
				transparency = trans;
			}
			dGraphView.m_edgeDetails.overrideTransparency(model, transparency);
			
			setUnselectedPaint(dGraphView.m_edgeDetails.getUnselectedPaint(model));
			setSelectedPaint(dGraphView.m_edgeDetails.getSelectedPaint(model));
			setTargetEdgeEndPaint(dGraphView.m_edgeDetails.getTargetArrowPaint(model));
			setSourceEdgeEndPaint(dGraphView.m_edgeDetails.getSourceArrowPaint(model));
			
			dGraphView.m_contentChanged = true;
		}
	}
	
	@Override
	public void setLabelTransparency(final int trans) {
		synchronized (dGraphView.m_lock) {
			Integer transparency;
			if (trans < 0 || trans > 255) {
				// If out of range, use default value.
				transparency = BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY.getDefault();
			} else {
				transparency = trans;
			}
			
			dGraphView.m_edgeDetails.overrideLabelTransparency(model, transparency);
			setTextPaint(dGraphView.m_edgeDetails.getLabelPaint(model, 0));
			
			dGraphView.m_contentChanged = true;
		}
	}
	
	@Override
	public void setBend(final Bend bend) {
		synchronized (dGraphView.m_lock) {
			dGraphView.m_edgeDetails.m_edgeBends.put(model, bend);
		}
		dGraphView.m_contentChanged = true;
	}
	
	@Override
	public Bend getBend() {
		synchronized (dGraphView.m_lock) {
			return dGraphView.m_edgeDetails.getBend(model);
		}
	}

	
	/**
	 * This method sets a mapped value.  NOT Defaults.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <T, V extends T> void applyVisualProperty(final VisualProperty<? extends T> vpOriginal, V value) {
		VisualProperty<?> vp = vpOriginal;
		
		// If value is null, simply use the VP's default value.
		if (value == null)
			value = (V) vp.getDefault();

		if (vp == DVisualLexicon.EDGE_STROKE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
			setSourceEdgeEndSelectedPaint((Paint) value);
			setTargetEdgeEndSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_UNSELECTED_PAINT) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_WIDTH) {
			final float currentWidth = dGraphView.m_edgeDetails.getWidth(model);
			final float newWidth = ((Number) value).floatValue();
			
			if (currentWidth != newWidth) {
				setStrokeWidth(newWidth);
				setStroke(DLineType.getDLineType(lineType).getStroke(newWidth));
			}
		} else if (vp == DVisualLexicon.EDGE_LINE_TYPE) {
			lineType = (LineType) value;
			final Stroke newStroke = DLineType.getDLineType(lineType).getStroke(dGraphView.m_edgeDetails.getWidth(model));
			setStroke(newStroke);
		} else if (vp == DVisualLexicon.EDGE_TRANSPARENCY) {
			setTransparency(((Number) value).intValue());
		} else if (vp == DVisualLexicon.EDGE_LABEL_TRANSPARENCY) {
			final int labelTransparency = ((Number) value).intValue();
			setLabelTransparency(labelTransparency);
		} else if (vp == DVisualLexicon.EDGE_SOURCE_ARROW_SELECTED_PAINT) {
			setSourceEdgeEndSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_TARGET_ARROW_SELECTED_PAINT) {
			setTargetEdgeEndSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT) {
			setSourceEdgeEndPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT) {
			setTargetEdgeEndPaint((Paint) value);
		} else if (vp == BasicVisualLexicon.EDGE_SELECTED) {
			setSelected((Boolean) value);
		} else if (vp == BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE) {
			final ArrowShape shape = (ArrowShape) value;
			final String shapeID = shape.getSerializableString();
			setTargetEdgeEnd(DArrowShape.parseArrowText(shapeID).getRendererTypeID());
		} else if (vp == BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE) {
			final ArrowShape shape = (ArrowShape) value;
			final String shapeID = shape.getSerializableString();
			setSourceEdgeEnd(DArrowShape.parseArrowText(shapeID).getRendererTypeID());
		} else if (vp == BasicVisualLexicon.EDGE_LABEL) {
			setText(value.toString());
		} else if (vp == DVisualLexicon.EDGE_TOOLTIP) {
			setToolTip(value.toString());
		} else if (vp == DVisualLexicon.EDGE_LABEL_FONT_FACE) {
			final Font newFont = ((Font) value).deriveFont(fontSize);
			setFont(newFont);
		} else if (vp == DVisualLexicon.EDGE_LABEL_FONT_SIZE) {
			int newSize = ((Number) value).intValue();
			if (newSize != fontSize) {
				fontSize = newSize;
				final Font f = getFont();
				if (f != null)
					setFont(f.deriveFont(newSize));
			}
		} else if (vp == BasicVisualLexicon.EDGE_LABEL_COLOR) {
			setTextPaint((Paint) value);
		} else if (vp == BasicVisualLexicon.EDGE_VISIBLE) {
			if (((Boolean) value).booleanValue())
				dGraphView.showGraphObject(this);
			else
				dGraphView.hideGraphObject(this);
		} else if (vp == DVisualLexicon.EDGE_CURVED) {
			final Boolean curved = (Boolean) value;
			if (curved)
				setLineCurved(EdgeView.CURVED_LINES);
			else
				setLineCurved(EdgeView.STRAIGHT_LINES);
		} else if (vp == DVisualLexicon.EDGE_BEND) {
			setBend((Bend) value);
		}
	}

	@Override
	protected <T, V extends T> V getDefaultValue(VisualProperty<T> vp) {
		return dGraphView.m_edgeDetails.getDefaultValue(vp);
	}
}
