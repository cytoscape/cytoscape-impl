package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;

public class VisualStyleBuilder {

	private static final String VS_NAME_PREFIX = "NetworkAnalyzer Style: ";

	// Preset defaults
	private static final Color NODE_COLOR = Color.WHITE;
	private static final Color NODE_LABEL_COLOR = new Color(130, 130, 130);
	private static final Color EDGE_COLOR = new Color(100, 100, 100);

	private final VisualStyleFactory vsFactory;

	private final VisualMappingFunctionFactory passthroughFactory;
	private final VisualMappingFunctionFactory continuousFactory;

	public VisualStyleBuilder(final VisualStyleFactory vsFactory,
			final VisualMappingFunctionFactory passthroughFactory, final VisualMappingFunctionFactory continuousFactory) {
		this.vsFactory = vsFactory;
		this.passthroughFactory = passthroughFactory;
		this.continuousFactory = continuousFactory;
	}

	/**
	 * Creates a new visual style in Cytoscape's VizMapper depending on the by
	 * the user defined mappings of computed attributes.
	 * 
	 * @return New visual style.
	 **/
	public VisualStyle createVisualStyle(final CyNetworkView networkView, final MapParameterDialog parameterDialog) {

		final CyNetwork network = networkView.getModel();
		// Create new visual style
		final String name = network.getRow(network).get(CyNetwork.NAME, String.class);
		final VisualStyle visualStyle = vsFactory.createVisualStyle(VS_NAME_PREFIX + name);

		// Network VP
		final Color backGroundColor = SettingsSerializer.getPluginSettings().getBackgroundColor();
		visualStyle.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, backGroundColor);

		// Node Label Mapping
		final PassthroughMapping<String, String> labelPassthrough = (PassthroughMapping<String, String>) passthroughFactory
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
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 100);
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 3.0d);
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, EDGE_COLOR);

		// Apply new visual settings
		if (parameterDialog.attrNodeColor.length() > 0) {
			// Continuous Mapping - set node color
			final CyColumn col = network.getDefaultNodeTable().getColumn(parameterDialog.attrNodeColor);
			Class<?> attrValueType = col.getType();
			final VisualMappingFunction<?, Paint> conMapNodeColor = continuousFactory.createVisualMappingFunction(
					parameterDialog.attrNodeColor, attrValueType, BasicVisualLexicon.NODE_FILL_COLOR);
			addBoundaries(parameterDialog, conMapNodeColor, parameterDialog.attrNodeColor,
					parameterDialog.mapNodeColor, SettingsSerializer.getPluginSettings().getBrightColor(),
					SettingsSerializer.getPluginSettings().getMiddleColor(), SettingsSerializer.getPluginSettings()
							.getDarkColor());
			visualStyle.addVisualMappingFunction(conMapNodeColor);
		}

		if (parameterDialog.attrNodeSize.length() > 0) {
			final CyColumn col = network.getDefaultNodeTable().getColumn(parameterDialog.attrNodeSize);
			Class<?> attrValueType = col.getType();
			VisualMappingFunction<?, Double> conMapNodeSize = continuousFactory.createVisualMappingFunction(
					parameterDialog.attrNodeSize, attrValueType, BasicVisualLexicon.NODE_SIZE);
			addBoundaries(parameterDialog, conMapNodeSize, parameterDialog.attrNodeSize, parameterDialog.mapNodeSize, new Double(10.0),
					new Double(50.0), new Double(100.0));
			visualStyle.addVisualMappingFunction(conMapNodeSize);
		}
		if (parameterDialog.attrEdgeColor.length() > 0) {
			final CyColumn col = network.getDefaultEdgeTable().getColumn(parameterDialog.attrEdgeColor);
			Class<?> attrValueType = col.getType();
			final VisualMappingFunction<?, Paint> conMapEdgeColor = continuousFactory.createVisualMappingFunction(
					parameterDialog.attrEdgeColor, attrValueType, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
			addBoundaries(parameterDialog, conMapEdgeColor, parameterDialog.attrEdgeColor, parameterDialog.mapEdgeColor, SettingsSerializer
					.getPluginSettings().getBrightColor(), SettingsSerializer.getPluginSettings().getMiddleColor(),
					SettingsSerializer.getPluginSettings().getDarkColor());
			visualStyle.addVisualMappingFunction(conMapEdgeColor);
		}
		if (parameterDialog.attrEdgeSize.length() > 0) {
			final CyColumn col = network.getDefaultEdgeTable().getColumn(parameterDialog.attrEdgeSize);
			Class<?> attrValueType = col.getType();
			VisualMappingFunction<?, Double> conMapEdgeSize = continuousFactory.createVisualMappingFunction(
					parameterDialog.attrEdgeSize, attrValueType, BasicVisualLexicon.EDGE_WIDTH);
			addBoundaries(parameterDialog, conMapEdgeSize, parameterDialog.attrEdgeSize, parameterDialog.mapEdgeSize, new Double(1.0), new Double(4.0),
					new Double(8.0));
			visualStyle.addVisualMappingFunction(conMapEdgeSize);
		}

		return visualStyle;
	}

	/**
	 * Adds boundaries to the continuous mapping by defining minimal, maximal
	 * and middle values for both color and size mapping.
	 * 
	 * @param conMapNodeColor
	 *            Continuous mapping for which boundaries are set.
	 * @param attr
	 *            Node/Edge attribute that is mapped.
	 * @param mapType
	 *            Type of the mapping, i.e. low values to small sizes or
	 *            otherwise or low values to bright colors and otherwise
	 *            respectively.
	 * @param min
	 *            Minimal value of size/color for the mapping.
	 * @param mid
	 *            Middle value of size/color for the mapping.
	 * @param max
	 *            Maximal value of size/color for the mapping.
	 * @return The value of the <code>conMap</code> parameter.
	 **/
	private void addBoundaries(final MapParameterDialog parameterDialog,
			VisualMappingFunction<?, ?> conMapNodeColor, String attr, String mapType, Object min, Object mid,
			Object max) {
		Object min_ = min;
		Object mid_ = mid;
		Object max_ = max;
		if (min instanceof Color && mapType.equals(Messages.DI_LOWTODARK)) {
			min_ = max;
			max_ = min;
		}
		if (min instanceof Double && mapType.equals(Messages.DI_LOWTOLARGE)) {
			min_ = max;
			max_ = min;
		}

		// Create boundary conditions less than, equals, greater than
		BoundaryRangeValues bv0 = new BoundaryRangeValues(min_, min_, min_);
		BoundaryRangeValues bv1 = new BoundaryRangeValues(mid_, mid_, mid_);
		BoundaryRangeValues bv2 = new BoundaryRangeValues(max_, max_, max_);

		// Set the attribute point values associated with the boundary values
		((ContinuousMapping) conMapNodeColor).addPoint(parameterDialog.minAttrValue.get(attr).doubleValue(), bv0);
		((ContinuousMapping) conMapNodeColor).addPoint(parameterDialog.meanAttrValue.get(attr).doubleValue(), bv1);
		((ContinuousMapping) conMapNodeColor).addPoint(parameterDialog.maxAttrValue.get(attr).doubleValue(), bv2);
	}
}
