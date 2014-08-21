package org.cytoscape.welcome.internal.style;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import java.awt.*;
import java.util.Set;

/**
 * Created by David Welker on 8/20/14
 * Copyright © 2014. All rights reserved.
 */
public class IntActXGMMLVisualStyleBuilder
{
	//Node Fields
	private static final String PREDICTED_GENE_NAME = "Human Readable Label";
	private static final String INTERACTOR_TYPE = "Interactor Type";
	private static final String TAXONOMY = "Taxonomy ID";

	//Edge Fields
	private static final String DETECTION_METHOD_NAME = "Detection Method";
	private static final String PRIMARY_INTERACTION_TYPE = "Primary Interaction Type";

	//private static



	// Default visual style name
	public static final String DEF_VS_NAME = "XGMML PSIMI 25 Style";

	// Presets
	private static final Color NODE_COLOR = Color.WHITE;
	private static final Color NODE_BORDER_COLOR = new Color(180, 180, 180);
	private static final Color NODE_LABEL_COLOR = new Color(50, 50, 50);
	private static final Color EDGE_COLOR = new Color(180, 180, 180);
	private static final Color EDGE_LABEL_COLOR = new Color(50, 50, 50);
	private static final Color EDGE_CROSS_COLOR = new Color(0x1C, 0x86, 0xEE);

	private static Font NODE_LABEL_FONT;
	static {
		NODE_LABEL_FONT = new Font("HelveticaNeue-UltraLight", Font.PLAIN, 10);
		if (NODE_LABEL_FONT == null)
			NODE_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);
	}

	// Color presets for some model organisms
	private static final Color COLOR_HUMAN = new Color(0x43, 0x6E, 0xEE);
	private static final Color COLOR_MOUSE = new Color(0xEE, 0x76, 0x21);
	private static final Color COLOR_RAT = new Color(0xFF, 0xA5, 0x00);
	private static final Color COLOR_FLY = new Color(0x93, 0x70, 0xDB);
	private static final Color COLOR_WORM = new Color(0x6B, 0x8E, 0x23);
	private static final Color COLOR_YEAST = new Color(0x9C, 0x9C, 0x9C);
	private static final Color COLOR_ECOLI = new Color(0xB0, 0xE2, 0xFF);
	private static final Color COLOR_ARABIDOPSIS = new Color(0xFF, 0xF5, 0xEE);

	private static final Color COLOR_DNA = Color.DARK_GRAY;
	private static final Color COLOR_MOLECULE = Color.DARK_GRAY;

	private final VisualStyleFactory vsFactory;

	private final VisualMappingFunctionFactory discreteMappingFactory;
	private final VisualMappingFunctionFactory passthroughMappingFactory;

	public IntActXGMMLVisualStyleBuilder(final VisualStyleFactory vsFactory,
									 final VisualMappingFunctionFactory discreteMappingFactory,
									 final VisualMappingFunctionFactory passthroughMappingFactory) {
		this.vsFactory = vsFactory;
		this.discreteMappingFactory = discreteMappingFactory;
		this.passthroughMappingFactory = passthroughMappingFactory;
	}

	public VisualStyle getVisualStyle() {

		final VisualStyle defStyle = vsFactory.createVisualStyle(DEF_VS_NAME);
		final Set<VisualPropertyDependency<?>> deps = defStyle.getAllVisualPropertyDependencies();
		// Disable add deps
		for(VisualPropertyDependency<?> dep: deps) {
			dep.setDependency(false);
		}

		// Network VP
		final Color backGroundColor = Color.white;
		defStyle.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, backGroundColor);

		// Label Mappings
		final PassthroughMapping<String, String> labelPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(PREDICTED_GENE_NAME, String.class,
						BasicVisualLexicon.NODE_LABEL);
		defStyle.addVisualMappingFunction(labelPassthrough);

		final PassthroughMapping<String, String> edgeLabelPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(DETECTION_METHOD_NAME, String.class,
						BasicVisualLexicon.EDGE_LABEL);
		defStyle.addVisualMappingFunction(edgeLabelPassthrough);

		final PassthroughMapping<String, String> nodeTooltipPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(INTERACTOR_TYPE, String.class,
						BasicVisualLexicon.NODE_TOOLTIP);
		defStyle.addVisualMappingFunction(nodeTooltipPassthrough);

		final PassthroughMapping<String, String> edgeTooltipPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(PRIMARY_INTERACTION_TYPE, String.class,
						BasicVisualLexicon.EDGE_TOOLTIP);
		defStyle.addVisualMappingFunction(edgeTooltipPassthrough);

		// Node View Defaults
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, NODE_BORDER_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, NODE_LABEL_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_FACE, NODE_LABEL_FONT);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, 230);

		defStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 2.0d);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, 240);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 65d);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 24d);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 220);

		// Edge View Defaults
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 180);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 2.0d);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, EDGE_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 80);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, 8);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, EDGE_LABEL_COLOR);

		// Node Color Mapping based on species name
		final DiscreteMapping<String, Paint> nodeColorMapping = (DiscreteMapping<String, Paint>) discreteMappingFactory
				.createVisualMappingFunction(TAXONOMY, String.class, BasicVisualLexicon.NODE_FILL_COLOR);

		nodeColorMapping.putMapValue("9606", COLOR_HUMAN);
		nodeColorMapping.putMapValue("10090", COLOR_MOUSE);
		nodeColorMapping.putMapValue("10116", COLOR_RAT);
		nodeColorMapping.putMapValue("7227", COLOR_FLY);
		nodeColorMapping.putMapValue("10116", COLOR_WORM);
		nodeColorMapping.putMapValue("4932", COLOR_YEAST);
		nodeColorMapping.putMapValue("83333", COLOR_ECOLI);
		nodeColorMapping.putMapValue("3702", COLOR_ARABIDOPSIS);

		defStyle.addVisualMappingFunction(nodeColorMapping);

		// Size Mapping
		final DiscreteMapping<String, Double> nodeWidthMapping = (DiscreteMapping<String, Double>) discreteMappingFactory
				.createVisualMappingFunction(INTERACTOR_TYPE, String.class, BasicVisualLexicon.NODE_WIDTH);
		nodeWidthMapping .putMapValue("protein", 65d);

		nodeWidthMapping .putMapValue("gene", 30d);

		nodeWidthMapping .putMapValue("small molecule", 20d);

		nodeWidthMapping .putMapValue("nucleic acid", 10d);
		nodeWidthMapping .putMapValue("deoxyribonucleic acid", 10d);
		nodeWidthMapping .putMapValue("double stranded deoxyribonucleic acid", 10d);
		nodeWidthMapping .putMapValue("single stranded deoxyribonucleic acid", 10d);
		nodeWidthMapping .putMapValue("ribonucleic acid", 10d);

		nodeWidthMapping .putMapValue("complex", 150d);
		nodeWidthMapping .putMapValue("protein complex", 150d);
		nodeWidthMapping .putMapValue("complex composition", 150d);
		nodeWidthMapping .putMapValue("protein dna complex", 150d);
		nodeWidthMapping .putMapValue("ribonucleoprotein complex", 150d);

		defStyle.addVisualMappingFunction(nodeWidthMapping);

		final DiscreteMapping<String, Double> nodeHeightMapping = (DiscreteMapping<String, Double>) discreteMappingFactory
				.createVisualMappingFunction(INTERACTOR_TYPE, String.class, BasicVisualLexicon.NODE_HEIGHT);
		nodeHeightMapping .putMapValue("protein", 24d);

		nodeHeightMapping .putMapValue("gene", 30d);

		nodeHeightMapping .putMapValue("small molecule", 20d);

		nodeHeightMapping .putMapValue("nucleic acid", 60d);
		nodeHeightMapping .putMapValue("deoxyribonucleic acid", 60d);
		nodeHeightMapping .putMapValue("double stranded deoxyribonucleic acid", 60d);
		nodeHeightMapping .putMapValue("single stranded deoxyribonucleic acid", 60d);
		nodeHeightMapping .putMapValue("ribonucleic acid", 60d);

		nodeHeightMapping .putMapValue("complex", 150d);
		nodeHeightMapping .putMapValue("protein complex", 150d);
		nodeHeightMapping .putMapValue("complex composition", 150d);
		nodeHeightMapping .putMapValue("protein dna complex", 150d);
		nodeHeightMapping .putMapValue("ribonucleoprotein complex", 150d);

		defStyle.addVisualMappingFunction(nodeHeightMapping);


		final DiscreteMapping<String, NodeShape> nodeShapeMapping = (DiscreteMapping<String, NodeShape>) discreteMappingFactory
				.createVisualMappingFunction(INTERACTOR_TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
		nodeShapeMapping.putMapValue("protein", NodeShapeVisualProperty.ROUND_RECTANGLE);

		nodeShapeMapping.putMapValue("gene",NodeShapeVisualProperty.DIAMOND);

		nodeShapeMapping.putMapValue("small molecule", NodeShapeVisualProperty.ELLIPSE);

		nodeShapeMapping.putMapValue("nucleic acid", NodeShapeVisualProperty.PARALLELOGRAM);
		nodeShapeMapping.putMapValue("deoxyribonucleic acid", NodeShapeVisualProperty.PARALLELOGRAM);
		nodeShapeMapping.putMapValue("double stranded deoxyribonucleic acid", NodeShapeVisualProperty.PARALLELOGRAM);
		nodeShapeMapping.putMapValue("single stranded deoxyribonucleic acid", NodeShapeVisualProperty.PARALLELOGRAM);
		nodeShapeMapping.putMapValue("ribonucleic acid", NodeShapeVisualProperty.PARALLELOGRAM);

		nodeShapeMapping.putMapValue("complex", NodeShapeVisualProperty.OCTAGON);
		nodeShapeMapping.putMapValue("protein complex", NodeShapeVisualProperty.OCTAGON);
		nodeShapeMapping.putMapValue("complex composition", NodeShapeVisualProperty.OCTAGON);
		nodeShapeMapping.putMapValue("protein dna complex", NodeShapeVisualProperty.OCTAGON);
		nodeShapeMapping.putMapValue("ribonucleoprotein complex", NodeShapeVisualProperty.OCTAGON);

		defStyle.addVisualMappingFunction(nodeShapeMapping);

		final DiscreteMapping<String, Paint> nodeBorderColorMapping = (DiscreteMapping<String, Paint>) discreteMappingFactory
				.createVisualMappingFunction(INTERACTOR_TYPE, String.class, BasicVisualLexicon.NODE_BORDER_PAINT);
		nodeBorderColorMapping.putMapValue("small molecule", COLOR_MOLECULE);
		nodeBorderColorMapping.putMapValue("gene", Color.DARK_GRAY);
		defStyle.addVisualMappingFunction(nodeBorderColorMapping);

		final DiscreteMapping<String, Double> nodeBorderWidthMapping = (DiscreteMapping<String, Double>) discreteMappingFactory
				.createVisualMappingFunction(INTERACTOR_TYPE, String.class, BasicVisualLexicon.NODE_BORDER_WIDTH);
		nodeBorderWidthMapping.putMapValue("small molecule", 10d);
		nodeBorderWidthMapping.putMapValue("gene", 4d);
		defStyle.addVisualMappingFunction(nodeBorderWidthMapping);


		final DiscreteMapping<String, Double> edgeWidthMapping = (DiscreteMapping<String, Double>) discreteMappingFactory
				.createVisualMappingFunction(PRIMARY_INTERACTION_TYPE, String.class, BasicVisualLexicon.EDGE_WIDTH);
		edgeWidthMapping.putMapValue("colocalization", 3d);
		edgeWidthMapping.putMapValue("predicted interaction", 1d);
		defStyle.addVisualMappingFunction(edgeWidthMapping);

		final DiscreteMapping<String, LineType> edgeLineTypeMapping = (DiscreteMapping<String, LineType>) discreteMappingFactory
				.createVisualMappingFunction(PRIMARY_INTERACTION_TYPE, String.class, BasicVisualLexicon.EDGE_LINE_TYPE);

		edgeLineTypeMapping.putMapValue("colocalization", LineTypeVisualProperty.LONG_DASH);

		edgeLineTypeMapping.putMapValue("asynthetic", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("enhancement", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("epistasis", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("genetic interaction", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("maximal epistasis", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("minimal epistasis", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("mutual enhancement", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("mutual over-suppression", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("mutual suppression", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("mutual suppression (complete)", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("mutual suppression (partial)", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("negative genetic interaction", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("neutral epistasis", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("neutral genetic interaction", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("noninteractive", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("opposing epistasis", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("opposing epistasis", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("over-suppression", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("over-suppression-enhancement", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("phenotype bias", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("positive epistasis", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("positive genetic interaction", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("qualitative epistasis", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("quantitative epistasis", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("suppression", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("suppression (complete)", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("suppression (partial)", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("suppression-enhancement", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("synthetic", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("unilateral enhancement", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("unilateral over-suppression", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("unilateral suppression", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("unilateral suppression (complete)", LineTypeVisualProperty.DOT);
		edgeLineTypeMapping.putMapValue("unilateral suppression (partial)", LineTypeVisualProperty.DOT);

		edgeLineTypeMapping.putMapValue("predicted interaction", LineTypeVisualProperty.LONG_DASH);
		defStyle.addVisualMappingFunction(edgeLineTypeMapping);

		return defStyle;
	}
}
