package de.mpg.mpi_inf.bioinf.netanalyzer;

import java.awt.Color;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;

public class VisualStyleBuilder {

	private static final String VS_NAME_PREFIX = "Network Analyzer Style: ";

	// Preset defaults
	private static final Color NODE_COLOR = Color.WHITE;
	private static final Color NODE_LABEL_COLOR = new Color(100, 100, 100);
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
	public VisualStyle createVisualStyle(final CyNetworkView networkView) {

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

		// Edge View Defaults
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 3.0d);
		visualStyle.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, EDGE_COLOR);

		// // Apply new visual settings
		// if (attrNodeColor.length() > 0) {
		// // Continuous Mapping - set node color
		// name += "_NodeColor_" + attrNodeColor;
		// ContinuousMapping conMapNodeColor = getColorMapping(attrNodeColor,
		// ObjectMapping.NODE_MAPPING);
		// conMapNodeColor = addBoundaries(conMapNodeColor, attrNodeColor,
		// mapNodeColor, SettingsSerializer
		// .getPluginSettings().getBrightColor(),
		// SettingsSerializer.getPluginSettings().getMiddleColor(),
		// SettingsSerializer.getPluginSettings().getDarkColor());
		// Calculator nodeColorCalculator = new
		// BasicCalculator("NetworkAnalyzer Node Color Calc", conMapNodeColor,
		// VisualPropertyType.NODE_FILL_COLOR);
		// nodeAppCalc.setCalculator(nodeColorCalculator);
		// }
		// if (attrNodeSize.length() > 0) {
		// // Continuous Mapping - set node size
		// name += "_NodeSize_" + attrNodeSize;
		// visualStyle.getDependency().set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED,
		// false);
		// ContinuousMapping conMapNodeSize = getSizeMapping(attrNodeSize,
		// ObjectMapping.NODE_MAPPING);
		// conMapNodeSize = addBoundaries(conMapNodeSize, attrNodeSize,
		// mapNodeSize, new Double(10.0),
		// new Double(50.0), new Double(100.0));
		// Calculator nodeHeightCalculator = new
		// BasicCalculator("NetworkAnalyzer Node Height Calc", conMapNodeSize,
		// VisualPropertyType.NODE_HEIGHT);
		// Calculator nodeWidthCalculator = new
		// BasicCalculator("NetworkAnalyzer Node Width Calc", conMapNodeSize,
		// VisualPropertyType.NODE_WIDTH);
		// nodeAppCalc.setCalculator(nodeHeightCalculator);
		// nodeAppCalc.setCalculator(nodeWidthCalculator);
		// }
		// if (attrEdgeColor.length() > 0) {
		// // Continuous Mapping - set edge color
		// name += "_EdgeColor_" + attrEdgeColor;
		// ContinuousMapping conMapEdgeColor = getColorMapping(attrEdgeColor,
		// ObjectMapping.EDGE_MAPPING);
		// conMapEdgeColor = addBoundaries(conMapEdgeColor, attrEdgeColor,
		// mapEdgeColor, SettingsSerializer
		// .getPluginSettings().getBrightColor(),
		// SettingsSerializer.getPluginSettings().getMiddleColor(),
		// SettingsSerializer.getPluginSettings().getDarkColor());
		// Calculator edgeColorCalculator = new
		// BasicCalculator("NetworkAnalyzer Edge Color Calc", conMapEdgeColor,
		// VisualPropertyType.EDGE_COLOR);
		// edgeAppCalc.setCalculator(edgeColorCalculator);
		// }
		// if (attrEdgeSize.length() > 0) {
		// // Continuous Mapping - set line width
		// name += "_EdgeSize_" + attrEdgeSize;
		// ContinuousMapping conMapEdgeSize = getSizeMapping(attrEdgeSize,
		// ObjectMapping.EDGE_MAPPING);
		// conMapEdgeSize = addBoundaries(conMapEdgeSize, attrEdgeSize,
		// mapEdgeSize, new Double(1.0), new Double(4.0),
		// new Double(8.0));
		// Calculator edgeSizeCalculator = new
		// BasicCalculator("NetworkAnalyzer Edge Size Calc", conMapEdgeSize,
		// VisualPropertyType.EDGE_LINE_WIDTH);
		// edgeAppCalc.setCalculator(edgeSizeCalculator);
		// }

		return visualStyle;
	}

}
