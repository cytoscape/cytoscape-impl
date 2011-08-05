package org.cytoscape.cpath2.internal.cytoscape;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.biopax.MapBioPaxToCytoscape;
import org.cytoscape.biopax.util.BioPaxVisualStyleUtil;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.RichVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

/**
 * Binary SIF Visual Style.
 * 
 * @author Ethan Cerami.
 */
public class BinarySifVisualStyleUtil {
	public final static String BINARY_SIF_VISUAL_STYLE = "Binary_SIF_Version_1";
	public final static String BINARY_NETWORK = "BINARY_NETWORK";
	public final static String COMPONENT_OF = "COMPONENT_OF";
	public final static String COMPONENT_IN_SAME = "IN_SAME_COMPONENT";
	public final static String SEQUENTIAL_CATALYSIS = "SEQUENTIAL_CATALYSIS";
	public final static String CONTROLS_STATE_CHANGE = "STATE_CHANGE";
	public final static String CONTROLS_METABOLIC_CHANGE = "METABOLIC_CATALYSIS";
	public final static String PARTICIPATES_CONVERSION = "REACTS_WITH";
	public final static String PARTICIPATES_INTERACTION = "INTERACTS_WITH";
	public final static String CO_CONTROL_INDEPENDENT_SIMILAR = "CO_CONTROL_INDEPENDENT_SIMILAR";
	public final static String CO_CONTROL_INDEPENDENT_ANTI = "CO_CONTROL_INDEPENDENT_ANTI";
	public final static String CO_CONTROL_DEPENDENT_SIMILAR = "CO_CONTROL_DEPENDENT_SIMILAR";
	public final static String CO_CONTROL_DEPENDENT_ANTI = "CO_CONTROL_DEPENDENT_ANTI";
	private final static String COMPLEX = "Complex";
	private static final String INTERACTION = "interaction"; // TODO: Double check this constant

	VisualStyle binarySifStyle;

	private final VisualStyleFactory styleFactory;
	private final VisualMappingManager mappingManager;
	private final VisualMappingFunctionFactory discreteFactory;
	private final VisualMappingFunctionFactory passthroughFactory;

	public BinarySifVisualStyleUtil(VisualStyleFactory styleFactory,
			VisualMappingManager mappingManager,
			VisualMappingFunctionFactory discreteMappingFactory,
			VisualMappingFunctionFactory passthroughFactory) {
		this.styleFactory = styleFactory;
		this.mappingManager = mappingManager;
		this.discreteFactory = discreteMappingFactory;
		this.passthroughFactory = passthroughFactory;
	}

	// /**
	// * Constructor.
	// * If an existing BioPAX Viz Mapper already exists, we use it.
	// * Otherwise, we create a new one.
	// *
	// * @return VisualStyle Object.
	// */
	public VisualStyle getVisualStyle() {
		synchronized (this) {
			if (binarySifStyle == null) {
				binarySifStyle = styleFactory
						.getInstance(BINARY_SIF_VISUAL_STYLE);

				// set node opacity
				binarySifStyle.setDefaultValue(
						RichVisualLexicon.NODE_TRANSPARENCY, 125);
				// unlock node size
				// binarySifStyle.getDependency().set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED,false);

				createNodeShapes(binarySifStyle);
				createNodeColors(binarySifStyle);
				createNodeLabel(binarySifStyle);

				binarySifStyle.setDefaultValue(MinimalVisualLexicon.EDGE_WIDTH,
						4.0);
				createEdgeColor(binarySifStyle);
				createDirectedEdges(binarySifStyle);

				binarySifStyle
						.setDefaultValue(
								MinimalVisualLexicon.NETWORK_BACKGROUND_PAINT,
								Color.WHITE);

				// The visual style must be added to the Global Catalog
				// in order for it to be written out to vizmap.props upon user
				// exit
				mappingManager.addVisualStyle(binarySifStyle);
			}
		}
		return binarySifStyle;
	}

	private void createNodeShapes(VisualStyle style) {
		// Default shape is an ellipse.
		style.setDefaultValue(RichVisualLexicon.NODE_SHAPE,
				NodeShapeVisualProperty.ELLIPSE);

		// Complexes are Hexagons.
		DiscreteMapping<String, NodeShape> function = (DiscreteMapping<String, NodeShape>) discreteFactory
				.createVisualMappingFunction(
						MapBioPaxToCytoscape.BIOPAX_ENTITY_TYPE, String.class,
						RichVisualLexicon.NODE_SHAPE);
		function.putMapValue(COMPLEX, NodeShapeVisualProperty.HEXAGON);
		style.addVisualMappingFunction(function);
	}

	private void createNodeColors(VisualStyle style) {
		Color color = new Color(255, 153, 153);
		style.setDefaultValue(MinimalVisualLexicon.NODE_FILL_COLOR, color);

		// Complexes are a Different Color.
		Color lightBlue = new Color(153, 153, 255);
		DiscreteMapping<String, Paint> function = (DiscreteMapping<String, Paint>) discreteFactory
				.createVisualMappingFunction(
						MapBioPaxToCytoscape.BIOPAX_ENTITY_TYPE, String.class,
						RichVisualLexicon.NODE_FILL_COLOR);
		function.putMapValue(COMPLEX, lightBlue);
		style.addVisualMappingFunction(function);
	}

	private void createEdgeColor(VisualStyle style) {
		// create a discrete mapper, for mapping biopax node type
		// to a particular node color
		style.setDefaultValue(MinimalVisualLexicon.EDGE_PAINT, Color.BLACK);
		DiscreteMapping<String, Paint> function = (DiscreteMapping<String, Paint>) discreteFactory
				.createVisualMappingFunction(INTERACTION, String.class,
						RichVisualLexicon.EDGE_PAINT);
		
		function.putMapValue(PARTICIPATES_CONVERSION,
				Color.decode("#ccc1da"));
		function.putMapValue(PARTICIPATES_INTERACTION,
				Color.decode("#7030a0"));
		function.putMapValue(CONTROLS_STATE_CHANGE,
				Color.decode("#0070c0"));
		function.putMapValue(CONTROLS_METABOLIC_CHANGE,
				Color.decode("#00b0f0"));
		function.putMapValue(SEQUENTIAL_CATALYSIS,
				Color.decode("#7f7f7f"));
		function.putMapValue(CO_CONTROL_DEPENDENT_ANTI,
				Color.decode("#ff0000"));
		function.putMapValue(CO_CONTROL_INDEPENDENT_ANTI,
				Color.decode("#fd95a6"));
		function.putMapValue(CO_CONTROL_DEPENDENT_SIMILAR,
				Color.decode("#00b050"));
		function.putMapValue(CO_CONTROL_INDEPENDENT_SIMILAR,
				Color.decode("#92d050"));
		function.putMapValue(COMPONENT_IN_SAME, Color.decode("#ffff00"));
		function.putMapValue(COMPONENT_OF, Color.decode("#ffc000"));
	}

	private static void createDirectedEdges(VisualStyle style) {
		// TODO: Arrow shape isn't part of the RichVisualLexicon yet...
//		DiscreteMapping discreteMapping = new DiscreteMapping(ArrowShape.NONE,
//				Semantics.INTERACTION, ObjectMapping.EDGE_MAPPING);
//
//		discreteMapping.putMapValue(COMPONENT_OF, ArrowShape.ARROW);
//		discreteMapping.putMapValue(CONTROLS_STATE_CHANGE, ArrowShape.ARROW);
//		discreteMapping
//				.putMapValue(CONTROLS_METABOLIC_CHANGE, ArrowShape.ARROW);
//		discreteMapping.putMapValue(SEQUENTIAL_CATALYSIS, ArrowShape.ARROW);
//
//		// create and set edge label calculator in edge appearance calculator
//		Calculator edgeColorCalculator = new BasicCalculator(
//				"Edge Source Arrow Shape", discreteMapping,
//				VisualPropertyType.EDGE_TGTARROW_SHAPE);
//		eac.setCalculator(edgeColorCalculator);
//
//		// set default color
//		eac.getDefaultAppearance().set(
//				cytoscape.visual.VisualPropertyType.EDGE_TGTARROW_SHAPE,
//				ArrowShape.NONE);
	}

	private void createNodeLabel(VisualStyle style) {
		// create pass through mapper for node labels
		style.addVisualMappingFunction(passthroughFactory
				.createVisualMappingFunction(
						BioPaxVisualStyleUtil.BIOPAX_NODE_LABEL, String.class,
						MinimalVisualLexicon.NODE_LABEL));
	}
}
