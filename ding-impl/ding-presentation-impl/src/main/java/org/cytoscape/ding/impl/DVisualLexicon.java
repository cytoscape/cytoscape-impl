/*
 Copyright (c) 2006, 2007, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
import java.awt.Paint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.ding.Bend;
import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.CustomGraphicsRange;
import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.impl.strokes.BackwardSlashStroke;
import org.cytoscape.ding.impl.strokes.ContiguousArrowStroke;
import org.cytoscape.ding.impl.strokes.ForwardSlashStroke;
import org.cytoscape.ding.impl.strokes.ParallelStroke;
import org.cytoscape.ding.impl.strokes.PipeStroke;
import org.cytoscape.ding.impl.strokes.SeparateArrowStroke;
import org.cytoscape.ding.impl.strokes.SineWaveStroke;
import org.cytoscape.ding.impl.strokes.VerticalSlashStroke;
import org.cytoscape.ding.impl.strokes.ZigzagStroke;
import org.cytoscape.ding.impl.visualproperty.CustomGraphicsVisualProperty;
import org.cytoscape.ding.impl.visualproperty.EdgeBendVisualProperty;
import org.cytoscape.ding.impl.visualproperty.ObjectPositionVisualProperty;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.Visualizable;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.BooleanVisualProperty;
import org.cytoscape.view.presentation.property.DefaultVisualizableVisualProperty;
import org.cytoscape.view.presentation.property.DoubleVisualProperty;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.PaintVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;


public class DVisualLexicon extends BasicVisualLexicon {
	// Set of custom graphics positions.
	private static final Set<VisualProperty<?>> CG_POSITIONS = new HashSet<VisualProperty<?>>();
	private static final Set<VisualProperty<?>> CG_SIZE = new HashSet<VisualProperty<?>>();
	private static final Map<VisualProperty<?>, VisualProperty<?>> CG_TO_SIZE = new HashMap<VisualProperty<?>, VisualProperty<?>>();
	private static final Map<VisualProperty<?>, VisualProperty<ObjectPosition>> CG_TO_POSITION = new HashMap<VisualProperty<?>, VisualProperty<ObjectPosition>>();

	// Root of Ding's VP tree.
	public static final VisualProperty<NullDataType> DING_ROOT = new NullVisualProperty(
			"DING_RENDERING_ENGINE_ROOT",
			"Ding Rndering Engine Root Visual Property");

	public static final VisualProperty<Boolean> NETWORK_NODE_SELECTION = new BooleanVisualProperty(
			Boolean.TRUE, "NETWORK_NODE_SELECTION", "Network Node Selection",
			CyNetwork.class);
	public static final VisualProperty<Boolean> NETWORK_EDGE_SELECTION = new BooleanVisualProperty(
			Boolean.TRUE, "NETWORK_EDGE_SELECTION", "Network Edge Selection",
			CyNetwork.class);

	public static final VisualProperty<ObjectPosition> NODE_LABEL_POSITION = new ObjectPositionVisualProperty(
			ObjectPositionImpl.DEFAULT_POSITION, "NODE_LABEL_POSITION",
			"Node Label Position", CyNode.class);

	// Range object for custom graphics.
	private static final CustomGraphicsRange CG_RANGE = new CustomGraphicsRange();

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
			50.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_1", "Node Custom Graphics Size 1",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_2 = new DoubleVisualProperty(
			50.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_2", "Node Custom Graphics Size 2",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_3 = new DoubleVisualProperty(
			50.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_3", "Node Custom Graphics Size 3",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_4 = new DoubleVisualProperty(
			50.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_4", "Node Custom Graphics Size 4",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_5 = new DoubleVisualProperty(
			50.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_5", "Node Custom Graphics Size 5",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_6 = new DoubleVisualProperty(
			50.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_6", "Node Custom Graphics Size 6",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_7 = new DoubleVisualProperty(
			50.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_7", "Node Custom Graphics Size 7",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_8 = new DoubleVisualProperty(
			50.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_8", "Node Custom Graphics Size 8",
			CyNode.class);
	public static final VisualProperty<Double> NODE_CUSTOMGRAPHICS_SIZE_9 = new DoubleVisualProperty(
			50.0, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_CUSTOMGRAPHICS_SIZE_9", "Node Custom Graphics Size 9",
			CyNode.class);

	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_1 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_1", "Node Custom Graphics 1", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_2 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_2", "Node Custom Graphics 2", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_3 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_3", "Node Custom Graphics 3", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_4 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_4", "Node Custom Graphics 4", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_5 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_5", "Node Custom Graphics 5", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_6 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_6", "Node Custom Graphics 6", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_7 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_7", "Node Custom Graphics 7", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_8 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_8", "Node Custom Graphics 8", CyNode.class);
	public static final VisualProperty<CyCustomGraphics> NODE_CUSTOMGRAPHICS_9 = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE,
			"NODE_CUSTOMGRAPHICS_9", "Node Custom Graphics 9", CyNode.class);

	// Location of custom graphics
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_1 = new ObjectPositionVisualProperty(
			ObjectPositionImpl.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_1",
			"Node Custom Graphics Position 1", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_2 = new ObjectPositionVisualProperty(
			ObjectPositionImpl.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_2",
			"Node Custom Graphics Position 2", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_3 = new ObjectPositionVisualProperty(
			ObjectPositionImpl.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_3",
			"Node Custom Graphics Position 3", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_4 = new ObjectPositionVisualProperty(
			ObjectPositionImpl.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_4",
			"Node Custom Graphics Position 4", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_5 = new ObjectPositionVisualProperty(
			ObjectPositionImpl.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_5",
			"Node Custom Graphics Position 5", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_6 = new ObjectPositionVisualProperty(
			ObjectPositionImpl.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_6",
			"Node Custom Graphics Position 6", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_7 = new ObjectPositionVisualProperty(
			ObjectPositionImpl.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_7",
			"Node Custom Graphics Position 7", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_8 = new ObjectPositionVisualProperty(
			ObjectPositionImpl.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_8",
			"Node Custom Graphics Position 8", CyNode.class);
	public static final VisualProperty<ObjectPosition> NODE_CUSTOMGRAPHICS_POSITION_9 = new ObjectPositionVisualProperty(
			ObjectPositionImpl.DEFAULT_POSITION,
			"NODE_CUSTOMGRAPHICS_POSITION_9",
			"Node Custom Graphics Position 9", CyNode.class);

	// Edge VPs
	public static final VisualProperty<Paint> EDGE_SOURCE_ARROW_SELECTED_PAINT = new PaintVisualProperty(
			Color.YELLOW, BasicVisualLexicon.PAINT_RANGE,
			"EDGE_SOURCE_ARROW_SELECTED_PAINT",
			"Edge Source Arrow Selected Paint", CyEdge.class);
	public static final VisualProperty<Paint> EDGE_TARGET_ARROW_SELECTED_PAINT = new PaintVisualProperty(
			Color.YELLOW, BasicVisualLexicon.PAINT_RANGE,
			"EDGE_TARGET_ARROW_SELECTED_PAINT",
			"Edge Target Arrow Selected Paint", CyEdge.class);
	public static final VisualProperty<Paint> EDGE_SOURCE_ARROW_UNSELECTED_PAINT = new PaintVisualProperty(
			Color.BLACK, BasicVisualLexicon.PAINT_RANGE,
			"EDGE_SOURCE_ARROW_UNSELECTED_PAINT",
			"Edge Source Arrow Unselected Paint", CyEdge.class);
	public static final VisualProperty<Paint> EDGE_TARGET_ARROW_UNSELECTED_PAINT = new PaintVisualProperty(
			Color.BLACK, BasicVisualLexicon.PAINT_RANGE,
			"EDGE_TARGET_ARROW_UNSELECTED_PAINT",
			"Edge Target Arrow Unselected Paint", CyEdge.class);
	
	public static final VisualProperty<Bend> EDGE_BEND = new EdgeBendVisualProperty(
			EdgeBendVisualProperty.DEFAULT_EDGE_BEND, "EDGE_BEND",
			"Edge Bend");
	
	public static final VisualProperty<Boolean> EDGE_CURVED = new BooleanVisualProperty(true, "EDGE_CURVED",
			"Edge Curved", CyEdge.class);
	

	// TODO: Implement if possible (in rendering engine)
//	public static final VisualProperty<ObjectPosition> EDGE_LABEL_POSITION = new ObjectPositionVisualProperty(
//			ObjectPositionImpl.DEFAULT_POSITION, "EDGE_LABEL_POSITION",
//			"Edge Label Position", CyEdge.class);

	// Ding specific node shapes.
	private static final NodeShape VEE = new DNodeShape(GraphGraphics.SHAPE_VEE, "V", "VEE");

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
	
	

	static {
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

		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_1,
				NODE_CUSTOMGRAPHICS_POSITION_1);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_2,
				NODE_CUSTOMGRAPHICS_POSITION_2);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_3,
				NODE_CUSTOMGRAPHICS_POSITION_3);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_4,
				NODE_CUSTOMGRAPHICS_POSITION_4);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_5,
				NODE_CUSTOMGRAPHICS_POSITION_5);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_6,
				NODE_CUSTOMGRAPHICS_POSITION_6);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_7,
				NODE_CUSTOMGRAPHICS_POSITION_7);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_8,
				NODE_CUSTOMGRAPHICS_POSITION_8);
		CG_TO_POSITION.put(NODE_CUSTOMGRAPHICS_9,
				NODE_CUSTOMGRAPHICS_POSITION_9);
	}

	public DVisualLexicon(final CustomGraphicsManager manager) {
		super(DING_ROOT);

		// Add new Shapes to the default
		((DiscreteRange<NodeShape>) NODE_SHAPE.getRange()).addRangeValue(VEE);

		// Add Ding-dependent line types.
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(ZIGZAG);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(BACKWARD_SLASH);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(CONTIGUOUS_ARROW);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(FORWARD_SLASH);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(PARALLEL_LINES);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(SEPARATE_ARROW);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(SINEWAVE);
		((DiscreteRange<LineType>) EDGE_LINE_TYPE.getRange()).addRangeValue(VERTICAL_SLASH);

		CG_RANGE.setManager(manager);

		addVisualProperty(NETWORK_NODE_SELECTION, NETWORK);
		addVisualProperty(NETWORK_EDGE_SELECTION, NETWORK);

		addVisualProperty(NODE_LABEL_POSITION, NODE);

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
		
		addVisualProperty(EDGE_SOURCE_ARROW_SELECTED_PAINT, EDGE_SELECTED_PAINT);
		addVisualProperty(EDGE_TARGET_ARROW_SELECTED_PAINT, EDGE_SELECTED_PAINT);
		addVisualProperty(EDGE_SOURCE_ARROW_UNSELECTED_PAINT, EDGE_UNSELECTED_PAINT);
		addVisualProperty(EDGE_TARGET_ARROW_UNSELECTED_PAINT, EDGE_UNSELECTED_PAINT);
		
		// Define edge end related VP
		addVisualProperty(EDGE_BEND, EDGE);
		addVisualProperty(EDGE_CURVED, EDGE);

		//addVisualProperty(EDGE_LABEL_POSITION, EDGE);

		createLookupMap();
	}

	private void createLookupMap() {
		// XGMML:
		addIdentifierMapping(CyEdge.class, "sourceArrowColor",
				EDGE_SOURCE_ARROW_UNSELECTED_PAINT);
		addIdentifierMapping(CyEdge.class, "targetArrowColor",
				EDGE_TARGET_ARROW_UNSELECTED_PAINT);

		// 2.x VizMap Properties:
		// TODO: missing node properties
		// addIdentifierMapping(CyNode.class,"nodeLabelOpacity",NODE_LABEL_TRANSPARENCY);
		// addIdentifierMapping(CyNode.class,"nodeBorderOpacity",NODE_BORDER_TRANSPARENCY);
		// addIdentifierMapping(CyNode.class,"nodeLabelWidth",NODE_LABEL_WIDTH);
		
		addIdentifierMapping(CyEdge.class, "edgeSourceArrowColor",
				EDGE_SOURCE_ARROW_UNSELECTED_PAINT);
		addIdentifierMapping(CyEdge.class, "edgeTargetArrowColor",
				EDGE_TARGET_ARROW_UNSELECTED_PAINT);
		// TODO: missing edge properties
		// addIdentifierMapping(CyEdge.class,"edgeLabelOpacity",EDGE_LABEL_TRANSPARENCY);
		// addIdentifierMapping(CyEdge.class,"edgeLabelWidth",EDGE_LABEL_WIDTH);
		// addIdentifierMapping(CyEdge.class,"edgeOpacity",EDGE_TRANPARENCY);

		// TODO add more!
		
		addIdentifierMapping(CyEdge.class, "edgeBend", EDGE_BEND);
		addIdentifierMapping(CyEdge.class, "edgeCurved", EDGE_CURVED);
	}

	static Set<VisualProperty<?>> getGraphicsPositionVP() {
		return CG_POSITIONS;
	}

	static Set<VisualProperty<?>> getGraphicsSizeVP() {
		return CG_SIZE;
	}

	static VisualProperty<?> getAssociatedCustomGraphicsSizeVP(
			VisualProperty<?> cgVP) {
		return CG_TO_SIZE.get(cgVP);
	}

	static VisualProperty<ObjectPosition> getAssociatedCustomGraphicsPositionVP(
			VisualProperty<?> cgVP) {
		return CG_TO_POSITION.get(cgVP);
	}
}
