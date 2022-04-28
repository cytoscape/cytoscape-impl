package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.view.presentation.property.LabelBackgroundShapeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LabelBackgroundShape;
import org.cytoscape.view.presentation.property.values.NodeShape;

/**
 * Ding Implementation of node shapes
 * 
 */
public class DNodeShape implements NodeShape {

	private static final DNodeShape RECTANGLE = new DNodeShape(GraphGraphics.SHAPE_RECTANGLE,
			NodeShapeVisualProperty.RECTANGLE.getDisplayName(),
			NodeShapeVisualProperty.RECTANGLE.getSerializableString());
	private static final DNodeShape ROUND_RECTANGLE = new DNodeShape(GraphGraphics.SHAPE_ROUNDED_RECTANGLE,
			NodeShapeVisualProperty.ROUND_RECTANGLE.getDisplayName(),
			NodeShapeVisualProperty.ROUND_RECTANGLE.getSerializableString());
	private static final DNodeShape TRIANGLE = new DNodeShape(GraphGraphics.SHAPE_TRIANGLE,
			NodeShapeVisualProperty.TRIANGLE.getDisplayName(), NodeShapeVisualProperty.TRIANGLE.getSerializableString());
	private static final DNodeShape PARALLELOGRAM = new DNodeShape(GraphGraphics.SHAPE_PARALLELOGRAM,
			NodeShapeVisualProperty.PARALLELOGRAM.getDisplayName(),
			NodeShapeVisualProperty.PARALLELOGRAM.getSerializableString());
	private static final DNodeShape DIAMOND = new DNodeShape(GraphGraphics.SHAPE_DIAMOND,
			NodeShapeVisualProperty.DIAMOND.getDisplayName(), NodeShapeVisualProperty.DIAMOND.getSerializableString());
	private static final DNodeShape ELLIPSE = new DNodeShape(GraphGraphics.SHAPE_ELLIPSE,
			NodeShapeVisualProperty.ELLIPSE.getDisplayName(), NodeShapeVisualProperty.ELLIPSE.getSerializableString());
	private static final DNodeShape HEXAGON = new DNodeShape(GraphGraphics.SHAPE_HEXAGON,
			NodeShapeVisualProperty.HEXAGON.getDisplayName(), NodeShapeVisualProperty.HEXAGON.getSerializableString());
	private static final DNodeShape OCTAGON = new DNodeShape(GraphGraphics.SHAPE_OCTAGON,
			NodeShapeVisualProperty.OCTAGON.getDisplayName(), NodeShapeVisualProperty.OCTAGON.getSerializableString());

	// Ding specific node shapes.
	public static final DNodeShape VEE = new DNodeShape(GraphGraphics.SHAPE_VEE, "V", "VEE");

	private static final Map<NodeShape, DNodeShape> DEF_SHAPE_MAP;

	static {
		DEF_SHAPE_MAP = new HashMap<NodeShape, DNodeShape>();
		DEF_SHAPE_MAP.put(NodeShapeVisualProperty.RECTANGLE, RECTANGLE);
		DEF_SHAPE_MAP.put(NodeShapeVisualProperty.DIAMOND, DIAMOND);
		DEF_SHAPE_MAP.put(NodeShapeVisualProperty.ELLIPSE, ELLIPSE);
		DEF_SHAPE_MAP.put(NodeShapeVisualProperty.HEXAGON, HEXAGON);
		DEF_SHAPE_MAP.put(NodeShapeVisualProperty.OCTAGON, OCTAGON);
		DEF_SHAPE_MAP.put(NodeShapeVisualProperty.PARALLELOGRAM, PARALLELOGRAM);
		DEF_SHAPE_MAP.put(NodeShapeVisualProperty.ROUND_RECTANGLE, ROUND_RECTANGLE);
		DEF_SHAPE_MAP.put(NodeShapeVisualProperty.TRIANGLE, TRIANGLE);
		DEF_SHAPE_MAP.put(VEE, VEE);
	}

	public static final DNodeShape getDShape(final NodeShape shape) {
		if (DEF_SHAPE_MAP.get(shape) == null)
			return RECTANGLE;
		else
			return DEF_SHAPE_MAP.get(shape);
	}
	
	public static final DNodeShape getDShape(final LabelBackgroundShape shape) {
		if(shape == LabelBackgroundShapeVisualProperty.RECTANGLE)
			return RECTANGLE;
		if(shape == LabelBackgroundShapeVisualProperty.ROUND_RECTANGLE)
			return ROUND_RECTANGLE;
		return null;
	}

	private final Byte rendererShapeID;
	private final String displayName;
	private final String serializableString;

	private static final Map<Byte, Shape> nodeShapes = GraphGraphics.getNodeShapes();

	public DNodeShape(final Byte rendererShapeID, final String displayName, final String serializableString) {
		this.rendererShapeID = rendererShapeID;
		this.displayName = displayName;
		this.serializableString = serializableString;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

	@Override
	public String getSerializableString() {
		return serializableString;
	}

	public Byte getNativeShape() {
		return this.rendererShapeID;
	}

	public Shape getShape() {
		return nodeShapes.get(rendererShapeID);
	}
}
