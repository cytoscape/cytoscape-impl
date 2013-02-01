package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.Paint;
import java.awt.Stroke;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.presentation.property.values.Bend;

public interface EdgeView extends GraphViewObject {
	
	/**
	 * Draws splined curves for edges.
	 */
	static final int CURVED_LINES = 1;

	/**
	 * Draws straight lines for edges.
	 */
	static final int STRAIGHT_LINES = 2;


	/**
	 * @return the Edge to which we are a view on
	 */
	CyEdge getCyEdge();


	/**
	 * @param width
	 *            set a new line width for this edge
	 */
	void setStrokeWidth(float width);

	/**
	 * @param stroke
	 *            the stroke to use on this edge
	 */
	void setStroke(Stroke stroke);

	/**
	 * @param line_type
	 *            set a new line type for the edge
	 */
	void setLineCurved(int line_type);


	/**
	 * This really refers to the <B>Stroke</B>, TODO: Make separte stroke
	 * methods
	 * 
	 * @param paint
	 *            the paint for this node
	 */
	public void setUnselectedPaint(Paint paint);

	/**
	 * This really refers to the <B>Stroke</B>, TODO: Make separte stroke
	 * methods
	 * 
	 * @param paint
	 *            the paint for this node
	 */
	void setSelectedPaint(Paint paint);

	/**
	 * @param paint
	 *            set the value for the source edge end when selected
	 */
	void setSourceEdgeEndSelectedPaint(Paint paint);

	/**
	 * @param paint
	 *            set the value for the target edge end when selected
	 */
	public void setTargetEdgeEndSelectedPaint(Paint paint);

	/**
	 * @param paint
	 *            set the value for the source edge end
	 */
	public void setSourceEdgeEndPaint(Paint paint);

	/**
	 * @param paint
	 *            set the value for the target edge end
	 */
	public void setTargetEdgeEndPaint(Paint paint);


	/**
	 * When we are selected then we draw ourselves red, and draw any handles.
	 */
	public boolean setSelected(boolean state);

	/**
	 * @return selected state
	 */
	public boolean isSelected();

	/**
	 * Sets the Drawing style for the edge end.
	 */
	void setSourceEdgeEnd(int type);

	/**
	 * Sets the Drawing style for the edge end.
	 */
	void setTargetEdgeEnd(int type);

	/**
	 * @return the Bend used
	 */
	Bend getBend();

	void setBend(final Bend bend);

	/**
	 * Sets Tooltip text for edge.
	 */
	void setToolTip(final String tip);

	/**
	 * Set Transparency of the edge.
	 * 
	 * @param transparency Java opacity: 0 to 255
	 */
	void setTransparency(final int transparency);
	
	/**
	 * Set Transparency of the edge.
	 * 
	 * @param transparency Java opacity: 0 to 255
	 */
	void setLabelTransparency(final int transparency);
}
