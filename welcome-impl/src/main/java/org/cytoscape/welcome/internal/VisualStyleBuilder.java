package org.cytoscape.welcome.internal;

import java.awt.Color;
import java.awt.Font;
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
	private static final Color NODE_BORDER_COLOR = new Color(180, 180, 180);
	private static final Color NODE_MAPPING_COLOR = new Color(0x00, 0xCE, 0xD1);

	private static final Color EDGE_MAPPING_MIN_COLOR = new Color(180, 180, 180);
	private static final Color EDGE_MAPPING_MAX_COLOR = new Color(0x7A, 0xC5, 0xCD);

	private static final Color NODE_LABEL_COLOR = new Color(100, 100, 100);
	private static final Color EDGE_COLOR = new Color(180, 180, 180);
	private static final String NODE_COLOR_COLUMN = "BetweennessCentrality";
	private static final String NODE_SIZE_COLUMN = "BetweennessCentrality";
	private static final String NODE_LABEL_SIZE_COLUMN = "BetweennessCentrality";
	private static final String EDGE_WIDTH_COLUMN = "EdgeBetweenness";
	private static final String EDGE_COLOR_COLUMN = "EdgeBetweenness";
	
	private static Font NODE_LABEL_FONT;
	static  {
		NODE_LABEL_FONT = new Font("HelveticaNeue-UltraLight", Font.PLAIN, 10);
		if(NODE_LABEL_FONT == null)
			NODE_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);
	}

	// Bend definition. We can tweak this value later.
	private static final String EDGE_BEND_DEFINITION = "0.8117209636412094,0.5840454410278249,0.6715391110621636";
	private static final String NODE_LABEL_POSITION_DEFINITION = "S,NW,c,0.00,0.00";

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
		final VisualStyle visualStyle = vsFactory.createVisualStyle("NetworkAnalyzer Style: " + networkName);

		// Network VP
		final Color backGroundColor = Color.white;
		visualStyle.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, backGroundColor);

		// Node Label Mapping
		final PassthroughMapping<String, String> labelPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(CyNetwork.NAME, String.class, BasicVisualLexicon.NODE_LABEL);
		visualStyle.addVisualMappingFunction(labelPassthrough);

		// Node View Defaults
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, NODE_BORDER_COLOR);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, NODE_LABEL_COLOR);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_FACE, NODE_LABEL_FONT);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, 210);
		
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 1.0d);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, 150);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 30d);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 30d);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_SIZE, 30d);
		visualStyle.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 190);

		// Edge View Defaults
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 80);
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 1.0d);
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, EDGE_COLOR);

		final Bend defBend = bendFactory.parseSerializableString(EDGE_BEND_DEFINITION);
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_BEND, defBend);
		
		// Node Color
		final CyColumn col = network.getDefaultNodeTable().getColumn(NODE_COLOR_COLUMN);
		Class<?> attrValueType = col.getType();
		@SuppressWarnings("unchecked")
		final ContinuousMapping<Double, Paint> conMapNodeColor = ((ContinuousMapping<Double, Paint>) continupousMappingFactory
				.createVisualMappingFunction(NODE_COLOR_COLUMN, attrValueType, BasicVisualLexicon.NODE_FILL_COLOR));
		final BoundaryRangeValues<Paint> bv0 = new BoundaryRangeValues<Paint>(Color.white, Color.white, Color.white);
		final BoundaryRangeValues<Paint> bv1 = new BoundaryRangeValues<Paint>(NODE_MAPPING_COLOR, NODE_MAPPING_COLOR,
				NODE_MAPPING_COLOR);
		final Double min = pickMin(network.getDefaultNodeTable(), col);
		final Double max = pickMax(network.getDefaultNodeTable(), col);
		conMapNodeColor.addPoint(min, bv0);
		conMapNodeColor.addPoint(max, bv1);
		visualStyle.addVisualMappingFunction(conMapNodeColor);

		// Node Size
		final CyColumn nodeSizeCol = network.getDefaultNodeTable().getColumn(NODE_SIZE_COLUMN);
		Class<?> nodeSizeColType = nodeSizeCol.getType();
		@SuppressWarnings("unchecked")
		final ContinuousMapping<Double, Double> conMapNodeSize = ((ContinuousMapping<Double, Double>) continupousMappingFactory
				.createVisualMappingFunction(NODE_SIZE_COLUMN, nodeSizeColType, BasicVisualLexicon.NODE_SIZE));
		final BoundaryRangeValues<Double> bvns0 = new BoundaryRangeValues<Double>(10d, 10d, 10d);
		final BoundaryRangeValues<Double> bvns1 = new BoundaryRangeValues<Double>(100d, 100d, 100d);

		conMapNodeSize.addPoint(min, bvns0);
		conMapNodeSize.addPoint(max, bvns1);
		visualStyle.addVisualMappingFunction(conMapNodeSize);

		// Node Label Size
		final CyColumn nodeLabelSizeCol = network.getDefaultNodeTable().getColumn(NODE_LABEL_SIZE_COLUMN);
		Class<?> nodeLabelSizeColType = nodeLabelSizeCol.getType();
		@SuppressWarnings("unchecked")
		final ContinuousMapping<Double, Integer> conMapNodeLabelSize = ((ContinuousMapping<Double, Integer>) continupousMappingFactory
				.createVisualMappingFunction(NODE_LABEL_SIZE_COLUMN, nodeLabelSizeColType,
						BasicVisualLexicon.NODE_LABEL_FONT_SIZE));
		final BoundaryRangeValues<Integer> bvnls0 = new BoundaryRangeValues<Integer>(10, 10, 10);
		final BoundaryRangeValues<Integer> bvnls1 = new BoundaryRangeValues<Integer>(100, 100, 100);
		// FIXME: replace min&max if you use different column
		conMapNodeLabelSize.addPoint(min, bvnls0);
		conMapNodeLabelSize.addPoint(max, bvnls1);
		visualStyle.addVisualMappingFunction(conMapNodeLabelSize);

		// Edge Width
		final CyColumn edgeWidthCol = network.getDefaultEdgeTable().getColumn(EDGE_WIDTH_COLUMN);
		final Class<?> edgeWidthColType = edgeWidthCol.getType();
		@SuppressWarnings("unchecked")
		final ContinuousMapping<Double, Double> conMapEdgeWidth = ((ContinuousMapping<Double, Double>) continupousMappingFactory
				.createVisualMappingFunction(EDGE_WIDTH_COLUMN, edgeWidthColType, BasicVisualLexicon.EDGE_WIDTH));
		final BoundaryRangeValues<Double> bvew0 = new BoundaryRangeValues<Double>(1d, 1d, 1d);
		final BoundaryRangeValues<Double> bvew1 = new BoundaryRangeValues<Double>(12d, 12d, 12d);
		final Double minEdge = pickMin(network.getDefaultEdgeTable(), edgeWidthCol);
		final Double maxEdge = pickMax(network.getDefaultEdgeTable(), edgeWidthCol);

		conMapEdgeWidth.addPoint(minEdge, bvew0);
		conMapEdgeWidth.addPoint(maxEdge, bvew1);
		visualStyle.addVisualMappingFunction(conMapEdgeWidth);

		// Edge transparency
		final CyColumn edgeTransCol = network.getDefaultEdgeTable().getColumn(EDGE_WIDTH_COLUMN);
		final Class<?> edgeTransColType = edgeTransCol.getType();
		@SuppressWarnings("unchecked")
		final ContinuousMapping<Double, Integer> conMapEdgeTrans = ((ContinuousMapping<Double, Integer>) continupousMappingFactory
				.createVisualMappingFunction(EDGE_WIDTH_COLUMN, edgeTransColType, BasicVisualLexicon.EDGE_TRANSPARENCY));
		final BoundaryRangeValues<Integer> bvet0 = new BoundaryRangeValues<Integer>(80, 80, 80);
		final BoundaryRangeValues<Integer> bvet1 = new BoundaryRangeValues<Integer>(220, 220, 220);
		conMapEdgeTrans.addPoint(minEdge, bvet0);
		conMapEdgeTrans.addPoint(maxEdge, bvet1);
		visualStyle.addVisualMappingFunction(conMapEdgeTrans);

		// Edge Color
		final CyColumn edgeColorCol = network.getDefaultEdgeTable().getColumn(EDGE_COLOR_COLUMN);
		final Class<?> edgeColorColType = edgeColorCol.getType();
		@SuppressWarnings("unchecked")
		final ContinuousMapping<Double, Paint> conMapEdgeColor = ((ContinuousMapping<Double, Paint>) continupousMappingFactory
				.createVisualMappingFunction(EDGE_COLOR_COLUMN, edgeColorColType,
						BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		final BoundaryRangeValues<Paint> bvec0 = new BoundaryRangeValues<Paint>(EDGE_MAPPING_MIN_COLOR,
				EDGE_MAPPING_MIN_COLOR, EDGE_MAPPING_MIN_COLOR);
		final BoundaryRangeValues<Paint> bvec1 = new BoundaryRangeValues<Paint>(EDGE_MAPPING_MAX_COLOR,
				EDGE_MAPPING_MAX_COLOR, EDGE_MAPPING_MAX_COLOR);
		conMapEdgeColor.addPoint(minEdge, bvec0);
		conMapEdgeColor.addPoint(maxEdge, bvec1);
		visualStyle.addVisualMappingFunction(conMapEdgeColor);

		// Set Lock
		Set<VisualPropertyDependency<?>> deps = visualStyle.getAllVisualPropertyDependencies();
		for (VisualPropertyDependency<?> dep : deps) {
			final String depName = dep.getIdString();
			if (depName.equals("nodeSizeLocked"))
				dep.setDependency(true);
		}

		return visualStyle;
	}

	private Double pickMin(final CyTable table, final CyColumn column) {
		final List<CyRow> rows = table.getAllRows();

		Double minNumber = Double.POSITIVE_INFINITY;
		for (CyRow row : rows) {
			final Object rawValue = row.get(column.getName(), column.getType());
			if (rawValue == null)
				continue;

			Double value = ((Number) rawValue).doubleValue();
			if (value < minNumber)
				minNumber = value;
		}
		return minNumber;
	}

	private Double pickMax(final CyTable table, final CyColumn column) {
		final List<CyRow> rows = table.getAllRows();

		Double maxNumber = Double.NEGATIVE_INFINITY;
		for (CyRow row : rows) {
			final Object rawValue = row.get(column.getName(), column.getType());
			if (rawValue == null)
				continue;

			Double value = ((Number) rawValue).doubleValue();
			if (value > maxNumber)
				maxNumber = value;
		}
		return maxNumber;
	}

}
