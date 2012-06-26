package org.cytoscape.ding;

import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;

public class DNodeShape implements NodeShape {

	private static final DNodeShape RECTANGLE = new DNodeShape(
			GraphGraphics.SHAPE_RECTANGLE,
			NodeShapeVisualProperty.RECTANGLE.getDisplayName(),
			NodeShapeVisualProperty.RECTANGLE.getSerializableString());
	private static final DNodeShape ROUND_RECTANGLE = new DNodeShape(
			GraphGraphics.SHAPE_ROUNDED_RECTANGLE, 
			NodeShapeVisualProperty.ROUND_RECTANGLE.getDisplayName(),
			NodeShapeVisualProperty.ROUND_RECTANGLE.getSerializableString());
	private static final DNodeShape TRIANGLE = new DNodeShape(
			GraphGraphics.SHAPE_TRIANGLE,
			NodeShapeVisualProperty.TRIANGLE.getDisplayName(),
			NodeShapeVisualProperty.TRIANGLE.getSerializableString());
	private static final DNodeShape PARALLELOGRAM = new DNodeShape(
			GraphGraphics.SHAPE_PARALLELOGRAM,
			NodeShapeVisualProperty.PARALLELOGRAM.getDisplayName(),
			NodeShapeVisualProperty.PARALLELOGRAM.getSerializableString());
	private static final DNodeShape DIAMOND = new DNodeShape(
			GraphGraphics.SHAPE_DIAMOND,
			NodeShapeVisualProperty.DIAMOND.getDisplayName(),
			NodeShapeVisualProperty.DIAMOND.getSerializableString());
	private static final DNodeShape ELLIPSE = new DNodeShape(
			GraphGraphics.SHAPE_ELLIPSE,
			NodeShapeVisualProperty.ELLIPSE.getDisplayName(),
			NodeShapeVisualProperty.ELLIPSE.getSerializableString());
	private static final DNodeShape HEXAGON = new DNodeShape(
			GraphGraphics.SHAPE_HEXAGON,
			NodeShapeVisualProperty.HEXAGON.getDisplayName(),
			NodeShapeVisualProperty.HEXAGON.getSerializableString());
	private static final DNodeShape OCTAGON = new DNodeShape(
			GraphGraphics.SHAPE_OCTAGON,
			NodeShapeVisualProperty.OCTAGON.getDisplayName(),
			NodeShapeVisualProperty.OCTAGON.getSerializableString());

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
	}

	public static final DNodeShape getDShape(final NodeShape shape) {
		return DEF_SHAPE_MAP.get(shape);
	}

	private final Byte rendererShapeID;

	private final String displayName;
	private final String serializableString;

	private static final Map<Byte, Shape> nodeShapes = GraphGraphics.getNodeShapes();

	public DNodeShape(final Byte rendererShapeID, final String displayName,
			final String serializableString) {
		this.displayName = displayName;
		this.serializableString = serializableString;

		this.rendererShapeID = rendererShapeID;
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
