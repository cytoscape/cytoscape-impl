package org.cytoscape.welcome.internal;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class VisualStyleBuilder {

	private static final Color NODE_COLOR = Color.WHITE;
	private static final Color NODE_LABEL_COLOR = new Color(130, 130, 130);
	private static final Color EDGE_COLOR = new Color(150, 150, 150);
	private static final String nodeColorColumn = "Indegree";
	private static final String NODE_SIZE_COLUMN = "Indegree";

	private final VisualStyleFactory vsFactory;
	private final BendFactory bendFactory;

	private final VisualMappingFunctionFactory continupousMappingFactory;
	private final VisualMappingFunctionFactory discreteMappingFactory;
	private final VisualMappingFunctionFactory passthroughMappingFactory;

	public VisualStyleBuilder(final VisualStyleFactory vsFactory,
			final VisualMappingFunctionFactory continupousMappingFactory,
			final VisualMappingFunctionFactory discreteMappingFactory,
			final VisualMappingFunctionFactory passthroughMappingFactory, final BendFactory bendFactory) {
		this.vsFactory = vsFactory;
		this.continupousMappingFactory = continupousMappingFactory;
		this.discreteMappingFactory = discreteMappingFactory;
		this.passthroughMappingFactory = passthroughMappingFactory;
		this.bendFactory = bendFactory;
	}

	public final VisualStyle buildVisualStyle(final CyNetworkView networkView) {
		final CyNetwork network = networkView.getModel();
		final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		final VisualStyle visualStyle = vsFactory.createVisualStyle("Network Analyzer Style: " + networkName);

		// Network VP
		final Color backGroundColor = Color.white;
		visualStyle.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, backGroundColor);

		// Node Label Mapping
		final PassthroughMapping<String, String> labelPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(CyNetwork.NAME, String.class, BasicVisualLexicon.NODE_LABEL);
		visualStyle.addVisualMappingFunction(labelPassthrough);

		// Node View Defaults
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, NODE_LABEL_COLOR);

		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0.0d);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 30d);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 30d);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_SIZE, 30d);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 200);

		// Edge View Defaults
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 150);
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 2.0d);
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, EDGE_COLOR);
		final double angle = Math.PI/2;
		Double sinVal = Math.sin(angle);
		Double cosVal = Math.cos(angle);
		final Bend defBend = bendFactory.parseSerializableString("0.8117209636412094,0.5840454410278249,0.6715391110621636");
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_BEND, defBend);

		// Apply new visual settings

		final CyColumn col = network.getDefaultNodeTable().getColumn(nodeColorColumn);
		Class<?> attrValueType = col.getType();
		@SuppressWarnings("unchecked")
		final ContinuousMapping<Integer, Paint> conMapNodeColor = ((ContinuousMapping<Integer, Paint>) continupousMappingFactory
				.createVisualMappingFunction(nodeColorColumn, attrValueType, BasicVisualLexicon.NODE_FILL_COLOR));
		// Create boundary conditions less than, equals, greater than
		final BoundaryRangeValues<Paint> bv0 = new BoundaryRangeValues<Paint>(Color.white, Color.white, Color.white);
		final BoundaryRangeValues<Paint> bv1 = new BoundaryRangeValues<Paint>(Color.RED, Color.RED, Color.RED);

		// Set the attribute point values associated with the boundary values
		final Integer min = ((Number) pickMin(network.getDefaultNodeTable(), col)).intValue();
		final Integer max = ((Number) pickMax(network.getDefaultNodeTable(), col)).intValue();
		conMapNodeColor.addPoint(min, bv0);
		conMapNodeColor.addPoint(max, bv1);
		visualStyle.addVisualMappingFunction(conMapNodeColor);
		
		final CyColumn nodeSizeCol = network.getDefaultNodeTable().getColumn(NODE_SIZE_COLUMN);
		Class<?> nodeSizeColType = nodeSizeCol.getType();
		@SuppressWarnings("unchecked")
		final ContinuousMapping<Integer, Double> conMapNodeSize = ((ContinuousMapping<Integer, Double>) continupousMappingFactory
				.createVisualMappingFunction(NODE_SIZE_COLUMN, nodeSizeColType, BasicVisualLexicon.NODE_SIZE));
		// Create boundary conditions less than, equals, greater than
		final BoundaryRangeValues<Double> bvns0 = new BoundaryRangeValues<Double>(10d, 10d, 10d);
		final BoundaryRangeValues<Double> bvns1 = new BoundaryRangeValues<Double>(200d, 200d, 200d);

		// Set the attribute point values associated with the boundary values
		final Integer minSize = ((Number) pickMin(network.getDefaultNodeTable(), col)).intValue();
		final Integer maxSize = ((Number) pickMax(network.getDefaultNodeTable(), col)).intValue();
		conMapNodeSize.addPoint(minSize, bvns0);
		conMapNodeSize.addPoint(maxSize, bvns1);
		visualStyle.addVisualMappingFunction(conMapNodeSize);
		Set<VisualPropertyDependency<?>> deps = visualStyle.getAllVisualPropertyDependencies();
		for(VisualPropertyDependency<?> dep:deps) {
			final String depName = dep.getIdString();
			if(depName.equals("nodeSizeLocked"))
				dep.setDependency(true);
		}
		
		
		
		// if (parameterDialog.attrNodeSize.length() > 0) {
		// final CyColumn col =
		// network.getDefaultNodeTable().getColumn(parameterDialog.attrNodeSize);
		// Class<?> attrValueType = col.getType();
		// VisualMappingFunction<?, Double> conMapNodeSize =
		// continupousMappingFactory.createVisualMappingFunction(
		// parameterDialog.attrNodeSize, attrValueType,
		// BasicVisualLexicon.NODE_SIZE);
		// addBoundaries(parameterDialog, conMapNodeSize,
		// parameterDialog.attrNodeSize, parameterDialog.mapNodeSize, new
		// Double(10.0),
		// new Double(50.0), new Double(100.0));
		// visualStyle.addVisualMappingFunction(conMapNodeSize);
		// }
		// if (parameterDialog.attrEdgeColor.length() > 0) {
		// final CyColumn col =
		// network.getDefaultEdgeTable().getColumn(parameterDialog.attrEdgeColor);
		// Class<?> attrValueType = col.getType();
		// final VisualMappingFunction<?, Paint> conMapEdgeColor =
		// continupousMappingFactory.createVisualMappingFunction(
		// parameterDialog.attrEdgeColor, attrValueType,
		// BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		// addBoundaries(parameterDialog, conMapEdgeColor,
		// parameterDialog.attrEdgeColor, parameterDialog.mapEdgeColor,
		// SettingsSerializer
		// .getPluginSettings().getBrightColor(),
		// SettingsSerializer.getPluginSettings().getMiddleColor(),
		// SettingsSerializer.getPluginSettings().getDarkColor());
		// visualStyle.addVisualMappingFunction(conMapEdgeColor);
		// }
		// if (parameterDialog.attrEdgeSize.length() > 0) {
		// final CyColumn col =
		// network.getDefaultEdgeTable().getColumn(parameterDialog.attrEdgeSize);
		// Class<?> attrValueType = col.getType();
		// VisualMappingFunction<?, Double> conMapEdgeSize =
		// continupousMappingFactory.createVisualMappingFunction(
		// parameterDialog.attrEdgeSize, attrValueType,
		// BasicVisualLexicon.EDGE_WIDTH);
		// addBoundaries(parameterDialog, conMapEdgeSize,
		// parameterDialog.attrEdgeSize, parameterDialog.mapEdgeSize, new
		// Double(1.0), new Double(4.0),
		// new Double(8.0));
		// visualStyle.addVisualMappingFunction(conMapEdgeSize);
		// }

		return visualStyle;
	}

	private Double pickMin(final CyTable table, final CyColumn column) {
		final List<CyRow> rows = table.getAllRows();

		Double minNumber = Double.POSITIVE_INFINITY;
		for (CyRow row : rows) {
			Double value = ((Number) row.get(column.getName(), column.getType())).doubleValue();
			if (value < minNumber)
				minNumber = value;
		}
		return minNumber;
	}

	private Double pickMax(final CyTable table, final CyColumn column) {
		final List<CyRow> rows = table.getAllRows();

		Double maxNumber = Double.NEGATIVE_INFINITY;
		for (CyRow row : rows) {
			Double value = ((Number) row.get(column.getName(), column.getType())).doubleValue();
			if (value > maxNumber)
				maxNumber = value;
		}
		return maxNumber;
	}

}
