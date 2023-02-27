package org.cytoscape.ding;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.cg.model.CustomGraphicsRange;
import org.cytoscape.cg.model.CustomGraphicsVisualProperty;
import org.cytoscape.cg.model.NullCustomGraphics;
import org.cytoscape.ding.impl.DLineType;
import org.cytoscape.ding.impl.strokes.AnimatedDashDotStroke;
import org.cytoscape.ding.impl.strokes.AnimatedEqualDashStroke;
import org.cytoscape.ding.impl.strokes.AnimatedLongDashStroke;
import org.cytoscape.ding.impl.strokes.BackwardSlashStroke;
import org.cytoscape.ding.impl.strokes.ContiguousArrowStroke;
import org.cytoscape.ding.impl.strokes.ForwardSlashStroke;
import org.cytoscape.ding.impl.strokes.ParallelStroke;
import org.cytoscape.ding.impl.strokes.PipeStroke;
import org.cytoscape.ding.impl.strokes.SeparateArrowStroke;
import org.cytoscape.ding.impl.strokes.SineWaveStroke;
import org.cytoscape.ding.impl.strokes.VerticalSlashStroke;
import org.cytoscape.ding.impl.strokes.ZigzagStroke;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.Visualizable;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.BooleanVisualProperty;
import org.cytoscape.view.presentation.property.DefaultVisualizableVisualProperty;
import org.cytoscape.view.presentation.property.DoubleVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.ObjectPositionVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.values.ObjectPosition;

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

public class DVisualLexicon extends BasicVisualLexicon {
	
	// Set of custom graphics positions.
	private static final Set<VisualProperty<ObjectPosition>> CG_POSITIONS = new HashSet<>();
	private static final Set<VisualProperty<CyCustomGraphics>> CG = new HashSet<>();
	private static final Set<VisualProperty<Double>> CG_SIZE = new HashSet<>();
	
	private static final Map<VisualProperty<CyCustomGraphics>, VisualProperty<Double>> CG_TO_SIZE = new HashMap<>();
	private static final Map<VisualProperty<CyCustomGraphics>, VisualProperty<ObjectPosition>> CG_TO_POSITION = new HashMap<>();
	private static final Map<VisualProperty<Double>, VisualProperty<CyCustomGraphics>> SIZE_TO_CG = new HashMap<>();
	
	private static final Set<VisualProperty<?>> UNSUPPORTED_VP_SET = new HashSet<>();


	// Root of Ding's VP tree.
	public static final VisualProperty<NullDataType> DING_ROOT = new NullVisualProperty(
			"DING_RENDERING_ENGINE_ROOT",
			"Ding Rndering Engine Root Visual Property");

	public static final VisualProperty<Boolean> NETWORK_FORCE_HIGH_DETAIL = new BooleanVisualProperty(
			Boolean.FALSE, "NETWORK_FORCE_HIGH_DETAIL", "Force High Detail", CyNetwork.class);
	public static final VisualProperty<Boolean> NETWORK_NODE_SELECTION = new BooleanVisualProperty(
			Boolean.TRUE, "NETWORK_NODE_SELECTION", "Network Node Selection",
			CyNetwork.class);
	public static final VisualProperty<Boolean> NETWORK_EDGE_SELECTION = new BooleanVisualProperty(
			Boolean.TRUE, "NETWORK_EDGE_SELECTION", "Network Edge Selection",
			CyNetwork.class);
	public static final VisualProperty<Boolean> NETWORK_ANNOTATION_SELECTION = new BooleanVisualProperty(
			Boolean.FALSE, "NETWORK_ANNOTATION_SELECTION", "Network Annotation Selection",
			CyNetwork.class);
	public static final VisualProperty<Boolean> NETWORK_NODE_LABEL_SELECTION = new BooleanVisualProperty(
			Boolean.FALSE, "NETWORK_NODE_LABEL_SELECTION", "Network Node Label Selection",
			CyNetwork.class);
	
	// Range object for custom graphics.
	private static final CustomGraphicsRange CG_RANGE = CustomGraphicsRange.getInstance();

	public static final VisualProperty<Visualizable> NODE_CUSTOMPAINT_1 = new DefaultVisualizableVisualProperty(
			"NODE_CUSTOMPAINT_1", "Node Custom Paint 1", CyNode.class);
	public static final VisualProperty<Visualizable> NODE_CUSTOMPAINT_2 = new DefaultVisualizableVisualProperty(
			"NODE_CUSTOMPAINT_2", "Node Custom Paint 2", CyNode.class);
	public static final VisualProperty<Visualizable> NODE_CUSTOMPAINT_3 = new DefaultVisualizableVisualProperty(
			"NODE_CUSTOMPAINT_3", "Node Custom Paint 3", CyNode.class);
	public static final VisualProperty<Visualizable> NODE_CUSTOMPAINT_4 = new DefaultVisualizableVisualProperty(
			"NODE_CUSTOMPAINT_4", "Node Custom Paint 4", CyNode.class);
	public static final VisualProperty<Visualizable> NODE_CUSTOMPAINT_5 = new DefaultVisualizableVisualProperty(
			"NODE_CUSTOMPAINT_5", "Node Custom Paint 5", CyNode.class);
	public static final VisualProperty<Visualizable> NODE_CUSTOMPAINT_6 = new DefaultVisualizableVisualProperty(
			"NODE_CUSTOMPAINT_6", "Node Custom Paint 6", CyNode.class);
	public static final VisualProperty<Visualizable> NODE_CUSTOMPAINT_7 = new DefaultVisualizableVisualProperty(
			"NODE_CUSTOMPAINT_7", "Node Custom Paint 7", CyNode.class);
	public static final VisualProperty<Visualizable> NODE_CUSTOMPAINT_8 = new DefaultVisualizableVisualProperty(
			"NODE_CUSTOMPAINT_8", "Node Custom Paint 8", CyNode.class);
	public static final VisualProperty<Visualizable> NODE_CUSTOMPAINT_9 = new DefaultVisualizableVisualProperty(
			"NODE_CUSTOMPAINT_9", "Node Custom Paint 9", CyNode.class);

	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_1 = new DoubleVisualProperty(
			0.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_1", "Node Image/Chart Size 1",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_2 = new DoubleVisualProperty(
			0.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_2", "Node Image/Chart Size 2",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_3 = new DoubleVisualProperty(
			0.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_3", "Node Image/Chart Size 3",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_4 = new DoubleVisualProperty(
			0.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_4", "Node Image/Chart Size 4",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_5 = new DoubleVisualProperty(
			0.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_5", "Node Image/Chart Size 5",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_6 = new DoubleVisualProperty(
			0.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_6", "Node Image/Chart Size 6",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_7 = new DoubleVisualProperty(
			0.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_7", "Node Image/Chart Size 7",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_8 = new DoubleVisualProperty(
			0.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_8", "Node Image/Chart Size 8",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_9 = new DoubleVisualProperty(
			0.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_9", "Node Image/Chart Size 9",
			CyNode.class);

	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_1 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_1", "Node Image/Chart 1", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_2 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_2", "Node Image/Chart 2", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_3 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_3", "Node Image/Chart 3", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_4 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_4", "Node Image/Chart 4", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_5 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_5", "Node Image/Chart 5", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_6 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_6", "Node Image/Chart 6", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_7 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_7", "Node Image/Chart 7", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_8 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_8", "Node Image/Chart 8", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_9 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_9", "Node Image/Chart 9", CyNode.class);

	// Location of custom graphics
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_1 = new ObjectPositionVisualProperty(
			ObjectPosition.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_1",
			"Node Image/Chart Position 1", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_2 = new ObjectPositionVisualProperty(
			ObjectPosition.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_2",
			"Node Image/Chart Position 2", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_3 = new ObjectPositionVisualProperty(
			ObjectPosition.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_3",
			"Node Image/Chart Position 3", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_4 = new ObjectPositionVisualProperty(
			ObjectPosition.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_4",
			"Node Image/Chart Position 4", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_5 = new ObjectPositionVisualProperty(
			ObjectPosition.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_5",
			"Node Image/Chart Position 5", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_6 = new ObjectPositionVisualProperty(
			ObjectPosition.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_6",
			"Node Image/Chart Position 6", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_7 = new ObjectPositionVisualProperty(
			ObjectPosition.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_7",
			"Node Image/Chart Position 7", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_8 = new ObjectPositionVisualProperty(
			ObjectPosition.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_8",
			"Node Image/Chart Position 8", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_9 = new ObjectPositionVisualProperty(
			ObjectPosition.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_9",
			"Node Image/Chart Position 9", CyNode.class);

	// Compound Node VPs
	public static final VisualProperty<NodeShape> COMPOUND_NODE_SHAPE = 
	 	new NodeShapeVisualProperty(NodeShapeVisualProperty.ROUND_RECTANGLE, "COMPOUND_NODE_SHAPE", "Shape (Compound Node)", CyNode.class);
	public static final VisualProperty<Double> COMPOUND_NODE_PADDING = 
		new DoubleVisualProperty(10.0, ARBITRARY_DOUBLE_RANGE, "COMPOUND_NODE_PADDING", "Padding (Compound Node)", CyNode.class);
	// TODO: transparency?

	// Edge VPs
	public static final VisualProperty<Boolean> EDGE_CURVED = new BooleanVisualProperty(true, "EDGE_CURVED",
			"Edge Curved", CyEdge.class);


	// Ding-local line types
	private static final DLineType ZIGZAG = new DLineType("Zigzag", "ZIGZAG",
			new ZigzagStroke(1.0f));
	private static final DLineType SINEWAVE = new DLineType("Sinewave",
			"SINEWAVE", new SineWaveStroke(1.0f));
	private static final DLineType VERTICAL_SLASH = new DLineType(
			"Vertical Slash", "VERTICAL_SLASH", new VerticalSlashStroke(1.0f,
					PipeStroke.Type.VERTICAL));
	private static final DLineType FORWARD_SLASH = new DLineType(
			"Forward Slash", "FORWARD_SLASH", new ForwardSlashStroke(1.0f,
					PipeStroke.Type.FORWARD));
	private static final DLineType BACKWARD_SLASH = new DLineType(
			"Backward Slash", "BACKWARD_SLASH", new BackwardSlashStroke(1.0f,
					PipeStroke.Type.BACKWARD));
	private static final DLineType PARALLEL_LINES = new DLineType(
			"Parallel Lines", "PARALLEL_LINES", new ParallelStroke(1.0f));
	private static final DLineType CONTIGUOUS_ARROW = new DLineType(
			"Contiguous Arrow", "CONTIGUOUS_ARROW", new ContiguousArrowStroke(
					1.0f));
	private static final DLineType SEPARATE_ARROW = new DLineType(
			"Separate Arrow", "SEPARATE_ARROW", new SeparateArrowStroke(1.0f));

	// For marquee or marching ants animations.  Not sure what the
	// right number of these
	private static final DLineType MARQUEE_DASH = new DLineType(
			"Marquee Dash", "MARQUEE_DASH", 
			new AnimatedLongDashStroke(1.0f));

	private static final DLineType MARQUEE_EQUAL = new DLineType(
			"Marquee Equal Dash", "MARQUEE_EQUAL", 
			new AnimatedEqualDashStroke(1.0f));

	private static final DLineType MARQUEE_DASH_DOT = new DLineType(
			"Marquee Dash Dot", "MARQUEE_DASH_DOT", 
			new AnimatedDashDotStroke(1.0f));
	
	
	public static final VisualProperty<Boolean> DUMMY = new BooleanVisualProperty(
			Boolean.FALSE, "Ding Dummy VP", "", CyNetwork.class);
	
	
	static {
		CG.add(NODE_CUSTOMGRAPHICS_1);
		CG.add(NODE_CUSTOMGRAPHICS_2);
		CG.add(NODE_CUSTOMGRAPHICS_3);
		CG.add(NODE_CUSTOMGRAPHICS_4);
		CG.add(NODE_CUSTOMGRAPHICS_5);
		CG.add(NODE_CUSTOMGRAPHICS_6);
		CG.add(NODE_CUSTOMGRAPHICS_7);
		CG.add(NODE_CUSTOMGRAPHICS_8);
		CG.add(NODE_CUSTOMGRAPHICS_9);
		
		CG_POSITIONS.add(NODE_CUSTOMGRAPHICS_POSITION_1);
		CG_POSITIONS.add(NODE_CUSTOMGRAPHICS_POSITION_2);
		CG_POSITIONS.add(NODE_CUSTOMGRAPHICS_POSITION_3);
		CG_POSITIONS.add(NODE_CUSTOMGRAPHICS_POSITION_4);
		CG_POSITIONS.add(NODE_CUSTOMGRAPHICS_POSITION_5);
		CG_POSITIONS.add(NODE_CUSTOMGRAPHICS_POSITION_6);
		CG_POSITIONS.add(NODE_CUSTOMGRAPHICS_POSITION_7);
		CG_POSITIONS.add(NODE_CUSTOMGRAPHICS_POSITION_8);
		CG_POSITIONS.add(NODE_CUSTOMGRAPHICS_POSITION_9);

		CG_SIZE.add(NODE_CUSTOMGRAPHICS_SIZE_1);
		CG_SIZE.add(NODE_CUSTOMGRAPHICS_SIZE_2);
		CG_SIZE.add(NODE_CUSTOMGRAPHICS_SIZE_3);
		CG_SIZE.add(NODE_CUSTOMGRAPHICS_SIZE_4);
		CG_SIZE.add(NODE_CUSTOMGRAPHICS_SIZE_5);
		CG_SIZE.add(NODE_CUSTOMGRAPHICS_SIZE_6);
		CG_SIZE.add(NODE_CUSTOMGRAPHICS_SIZE_7);
		CG_SIZE.add(NODE_CUSTOMGRAPHICS_SIZE_8);
		CG_SIZE.add(NODE_CUSTOMGRAPHICS_SIZE_9);

		CG_TO_SIZE.put(NODE_CUSTOMGRAPHICS_1, NODE_CUSTOMGRAPHICS_SIZE_1);
		CG_TO_SIZE.put(NODE_CUSTOMGRAPHICS_2, NODE_CUSTOMGRAPHICS_SIZE_2);
		CG_TO_SIZE.put(NODE_CUSTOMGRAPHICS_3, NODE_CUSTOMGRAPHICS_SIZE_3);
		CG_TO_SIZE.put(NODE_CUSTOMGRAPHICS_4, NODE_CUSTOMGRAPHICS_SIZE_4);
		CG_TO_SIZE.put(NODE_CUSTOMGRAPHICS_5, NODE_CUSTOMGRAPHICS_SIZE_5);
		CG_TO_SIZE.put(NODE_CUSTOMGRAPHICS_6, NODE_CUSTOMGRAPHICS_SIZE_6);
		CG_TO_SIZE.put(NODE_CUSTOMGRAPHICS_7, NODE_CUSTOMGRAPHICS_SIZE_7);
		CG_TO_SIZE.put(NODE_CUSTOMGRAPHICS_8, NODE_CUSTOMGRAPHICS_SIZE_8);
		CG_TO_SIZE.put(NODE_CUSTOMGRAPHICS_9, NODE_CUSTOMGRAPHICS_SIZE_9);

		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_1, NODE_CUSTOMGRAPHICS_POSITION_1);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_2, NODE_CUSTOMGRAPHICS_POSITION_2);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_3, NODE_CUSTOMGRAPHICS_POSITION_3);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_4, NODE_CUSTOMGRAPHICS_POSITION_4);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_5, NODE_CUSTOMGRAPHICS_POSITION_5);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_6, NODE_CUSTOMGRAPHICS_POSITION_6);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_7, NODE_CUSTOMGRAPHICS_POSITION_7);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_8, NODE_CUSTOMGRAPHICS_POSITION_8);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_9, NODE_CUSTOMGRAPHICS_POSITION_9);
		
		SIZE_TO_CG.put(NODE_CUSTOMGRAPHICS_SIZE_1, NODE_CUSTOMGRAPHICS_1);
		SIZE_TO_CG.put(NODE_CUSTOMGRAPHICS_SIZE_2, NODE_CUSTOMGRAPHICS_2);
		SIZE_TO_CG.put(NODE_CUSTOMGRAPHICS_SIZE_3, NODE_CUSTOMGRAPHICS_3);
		SIZE_TO_CG.put(NODE_CUSTOMGRAPHICS_SIZE_4, NODE_CUSTOMGRAPHICS_4);
		SIZE_TO_CG.put(NODE_CUSTOMGRAPHICS_SIZE_5, NODE_CUSTOMGRAPHICS_5);
		SIZE_TO_CG.put(NODE_CUSTOMGRAPHICS_SIZE_6, NODE_CUSTOMGRAPHICS_6);
		SIZE_TO_CG.put(NODE_CUSTOMGRAPHICS_SIZE_7, NODE_CUSTOMGRAPHICS_7);
		SIZE_TO_CG.put(NODE_CUSTOMGRAPHICS_SIZE_8, NODE_CUSTOMGRAPHICS_8);
		SIZE_TO_CG.put(NODE_CUSTOMGRAPHICS_SIZE_9, NODE_CUSTOMGRAPHICS_9);
		
		UNSUPPORTED_VP_SET.add(BasicVisualLexicon.NODE_SELECTED);
		UNSUPPORTED_VP_SET.add(BasicVisualLexicon.EDGE_SELECTED);
		UNSUPPORTED_VP_SET.add(BasicVisualLexicon.NODE_DEPTH);
		UNSUPPORTED_VP_SET.add(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION);
		UNSUPPORTED_VP_SET.add(BasicVisualLexicon.NETWORK_DEPTH);
	}

	public DVisualLexicon() {
		super(DING_ROOT);

		// Add Ding-dependent line types.
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(ZIGZAG);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(BACKWARD_SLASH);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(CONTIGUOUS_ARROW);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(FORWARD_SLASH);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(PARALLEL_LINES);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(SEPARATE_ARROW);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(SINEWAVE);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(VERTICAL_SLASH);

		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(MARQUEE_DASH);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(MARQUEE_EQUAL);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(MARQUEE_DASH_DOT);
		
		// Add Ding-dependent node shapes.
		((DiscreteRange<NodeShape>) NODE_SHAPE.getRange()).addRangeValue(DNodeShape.VEE);

		addVisualProperty(NETWORK_FORCE_HIGH_DETAIL, NETWORK);
		addVisualProperty(NETWORK_NODE_SELECTION, NETWORK);
		addVisualProperty(NETWORK_EDGE_SELECTION, NETWORK);
		addVisualProperty(NETWORK_ANNOTATION_SELECTION, NETWORK);
		addVisualProperty(NETWORK_NODE_LABEL_SELECTION, NETWORK);

		addVisualProperty(COMPOUND_NODE_SHAPE, NODE);
		addVisualProperty(COMPOUND_NODE_PADDING, NODE);
		// addVisualProperty(COMPOUND_NODE_TRANSPARENCY, NODE);

		// Parent of Custom Graphics related
		addVisualProperty(NODE_CUSTOMPAINT_1, NODE_PAINT);
		addVisualProperty(NODE_CUSTOMPAINT_2, NODE_PAINT);
		addVisualProperty(NODE_CUSTOMPAINT_3, NODE_PAINT);
		addVisualProperty(NODE_CUSTOMPAINT_4, NODE_PAINT);
		addVisualProperty(NODE_CUSTOMPAINT_5, NODE_PAINT);
		addVisualProperty(NODE_CUSTOMPAINT_6, NODE_PAINT);
		addVisualProperty(NODE_CUSTOMPAINT_7, NODE_PAINT);
		addVisualProperty(NODE_CUSTOMPAINT_8, NODE_PAINT);
		addVisualProperty(NODE_CUSTOMPAINT_9, NODE_PAINT);

		// Custom Graphics. Currently Cytoscape supports 9 objects/node.
		addVisualProperty(NODE_CUSTOMGRAPHICS_1, NODE_CUSTOMPAINT_1);
		addVisualProperty(NODE_CUSTOMGRAPHICS_2, NODE_CUSTOMPAINT_2);
		addVisualProperty(NODE_CUSTOMGRAPHICS_3, NODE_CUSTOMPAINT_3);
		addVisualProperty(NODE_CUSTOMGRAPHICS_4, NODE_CUSTOMPAINT_4);
		addVisualProperty(NODE_CUSTOMGRAPHICS_5, NODE_CUSTOMPAINT_5);
		addVisualProperty(NODE_CUSTOMGRAPHICS_6, NODE_CUSTOMPAINT_6);
		addVisualProperty(NODE_CUSTOMGRAPHICS_7, NODE_CUSTOMPAINT_7);
		addVisualProperty(NODE_CUSTOMGRAPHICS_8, NODE_CUSTOMPAINT_8);
		addVisualProperty(NODE_CUSTOMGRAPHICS_9, NODE_CUSTOMPAINT_9);

		addVisualProperty(NODE_CUSTOMGRAPHICS_SIZE_1, NODE_SIZE);
		addVisualProperty(NODE_CUSTOMGRAPHICS_SIZE_2, NODE_SIZE);
		addVisualProperty(NODE_CUSTOMGRAPHICS_SIZE_3, NODE_SIZE);
		addVisualProperty(NODE_CUSTOMGRAPHICS_SIZE_4, NODE_SIZE);
		addVisualProperty(NODE_CUSTOMGRAPHICS_SIZE_5, NODE_SIZE);
		addVisualProperty(NODE_CUSTOMGRAPHICS_SIZE_6, NODE_SIZE);
		addVisualProperty(NODE_CUSTOMGRAPHICS_SIZE_7, NODE_SIZE);
		addVisualProperty(NODE_CUSTOMGRAPHICS_SIZE_8, NODE_SIZE);
		addVisualProperty(NODE_CUSTOMGRAPHICS_SIZE_9, NODE_SIZE);

		// These are children of NODE_CUSTOMGRAPHICS.
		addVisualProperty(NODE_CUSTOMGRAPHICS_POSITION_1, NODE_CUSTOMPAINT_1);
		addVisualProperty(NODE_CUSTOMGRAPHICS_POSITION_2, NODE_CUSTOMPAINT_2);
		addVisualProperty(NODE_CUSTOMGRAPHICS_POSITION_3, NODE_CUSTOMPAINT_3);
		addVisualProperty(NODE_CUSTOMGRAPHICS_POSITION_4, NODE_CUSTOMPAINT_4);
		addVisualProperty(NODE_CUSTOMGRAPHICS_POSITION_5, NODE_CUSTOMPAINT_5);
		addVisualProperty(NODE_CUSTOMGRAPHICS_POSITION_6, NODE_CUSTOMPAINT_6);
		addVisualProperty(NODE_CUSTOMGRAPHICS_POSITION_7, NODE_CUSTOMPAINT_7);
		addVisualProperty(NODE_CUSTOMGRAPHICS_POSITION_8, NODE_CUSTOMPAINT_8);
		addVisualProperty(NODE_CUSTOMGRAPHICS_POSITION_9, NODE_CUSTOMPAINT_9);
		
		// Define edge end related VP
		addVisualProperty(EDGE_CURVED, EDGE);

		createLookupMap();
	}

	private void createLookupMap() {
		// 2.x VizMap Properties:
		addIdentifierMapping(CyNode.class, "nodeCustomGraphics1", NODE_CUSTOMGRAPHICS_1);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphics2", NODE_CUSTOMGRAPHICS_2);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphics3", NODE_CUSTOMGRAPHICS_3);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphics4", NODE_CUSTOMGRAPHICS_4);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphics5", NODE_CUSTOMGRAPHICS_5);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphics6", NODE_CUSTOMGRAPHICS_6);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphics7", NODE_CUSTOMGRAPHICS_7);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphics8", NODE_CUSTOMGRAPHICS_8);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphics9", NODE_CUSTOMGRAPHICS_9);
		
		addIdentifierMapping(CyNode.class, "nodeCustomGraphicsPosition1", NODE_CUSTOMGRAPHICS_POSITION_1);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphicsPosition2", NODE_CUSTOMGRAPHICS_POSITION_2);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphicsPosition3", NODE_CUSTOMGRAPHICS_POSITION_3);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphicsPosition4", NODE_CUSTOMGRAPHICS_POSITION_4);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphicsPosition5", NODE_CUSTOMGRAPHICS_POSITION_5);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphicsPosition6", NODE_CUSTOMGRAPHICS_POSITION_6);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphicsPosition7", NODE_CUSTOMGRAPHICS_POSITION_7);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphicsPosition8", NODE_CUSTOMGRAPHICS_POSITION_8);
		addIdentifierMapping(CyNode.class, "nodeCustomGraphicsPosition9", NODE_CUSTOMGRAPHICS_POSITION_9);
		
		addIdentifierMapping(CyEdge.class, "edgeCurved", EDGE_CURVED);
	}

	static Set<VisualProperty<ObjectPosition>> getGraphicsPositionVP() {
		return CG_POSITIONS;
	}

	static Set<VisualProperty<Double>> getGraphicsSizeVP() {
		return CG_SIZE;
	}

	public static VisualProperty<Double> getAssociatedCustomGraphicsSizeVP(VisualProperty<?> cgVP) {
		return CG_TO_SIZE.get(cgVP);
	}

	public static VisualProperty<ObjectPosition> getAssociatedCustomGraphicsPositionVP(VisualProperty<?> cgVP) {
		return CG_TO_POSITION.get(cgVP);
	}
	
	@SuppressWarnings("rawtypes")
	public static VisualProperty<CyCustomGraphics> getAssociatedCustomGraphicsVP(VisualProperty<Double> cgSizeVP) {
		return SIZE_TO_CG.get(cgSizeVP);
	}
	
	@SuppressWarnings("rawtypes")
	public static Set<VisualProperty<CyCustomGraphics>> getCustomGraphicsVisualProperties() {
		return CG;
	}
	
	@Override
	public boolean isSupported(final VisualProperty<?> vp) {
		if (UNSUPPORTED_VP_SET.contains(vp))
			return false;
		
		return super.isSupported(vp);
	}
	
	public boolean isAnimated(LineType lineType) {
		return lineType == MARQUEE_DASH || lineType == MARQUEE_DASH_DOT || lineType == MARQUEE_EQUAL;
	}
	
}
