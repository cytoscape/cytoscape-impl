package org.cytoscape.task.internal.quickstart.subnetworkbuilder;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

import static org.cytoscape.task.internal.quickstart.subnetworkbuilder.CreateSubnetworkFromSearchTask.*;

/**
 * Builder for Visual Style
 * 
 */
public class VisualStyleBuilder {

    // Default Style
    private static final Color BACKGROUND_COLOR = new Color(0xE8, 0xE8, 0xE8);

    private static final Color NODE_COLOR = new Color(0x26, 0x26, 0x26);
    private static final Color NODE_LABEL_COLOR = new Color(0xF9, 0xF9, 0xF9);
    private static final Color EDGE_COLOR = new Color(0x6E, 0x7B, 0x8B);

    private static final Double EDGE_WIDTH = 1d;
    private static final Double NODE_WIDTH = 30d;
    private static final Double NODE_HEIGHT = 30d;
    private static final Color EDGE_LABEL_COLOR = NODE_LABEL_COLOR;

    private static final int NODE_LABEL_SIZE_REGULAR = 12;
    private static final int NODE_LABEL_SIZE_LARGE = 32;

    private static final Double NODE_BORDER_WIDTH = 10d;
    private static final Color NODE_BORDER_COLOR = new Color(0x69, 0x69, 0x69);

    private static final Double NODE_WIDTH_TARGET = 85d;
    private static final Double NODE_HEIGHT_TARGET = 85d;
    private static final Double NODE_WIDTH_BOTH = 100d;
    private static final Double NODE_HEIGHT_BOTH = 100d;

    private static final Color NODE_COLOR_DISEASE = new Color(0xFF, 0x63, 0x47);
    private static final Color NODE_BORDER_COLOR_DISEASE = NODE_COLOR_DISEASE.darker();
    private static final Color NODE_COLOR_QUERY = new Color(0xEE, 0x76, 0x00);
    private static final Color NODE_BORDER_COLOR_QUERY = NODE_COLOR_QUERY.darker();
    private static final Color NODE_COLOR_BOTH = new Color(0x6B, 0x8E, 0x23);
    private static final Color NODE_BORDER_COLOR_BOTH = NODE_COLOR_BOTH.darker();

    private final VisualStyleFactory vsFactory;
    private final VisualMappingFunctionFactory discFactory;
    private final VisualMappingFunctionFactory ptFactory;

    public VisualStyleBuilder(final VisualStyleFactory vsFactory, final VisualMappingFunctionFactory discFactory,
	    final VisualMappingFunctionFactory ptFactory) {
	this.vsFactory = vsFactory;
	this.discFactory = discFactory;
	this.ptFactory = ptFactory;
    }

    public VisualStyle buildStyle(final String vsName) {
	final VisualStyle newStyle = vsFactory.createVisualStyle(vsName);

	newStyle.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, BACKGROUND_COLOR);
	newStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
	newStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, NODE_LABEL_COLOR);
	newStyle.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, NODE_WIDTH);
	newStyle.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, NODE_HEIGHT);

	newStyle.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);

	newStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, NODE_LABEL_SIZE_REGULAR);

	newStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, NODE_BORDER_WIDTH);
	newStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, NODE_BORDER_COLOR);

	newStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, EDGE_WIDTH);
	newStyle.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, EDGE_COLOR);
	newStyle.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, EDGE_LABEL_COLOR);

	// Node Color mapping
	VisualMappingFunction<String, Paint> nodeColorMapping = discFactory.createVisualMappingFunction(
		QUERY_GENE_ATTR_NAME, String.class, null, BasicVisualLexicon.NODE_FILL_COLOR);

	if (nodeColorMapping instanceof DiscreteMapping) {
	    ((DiscreteMapping<String, Paint>) nodeColorMapping).putMapValue("disease", NODE_COLOR_DISEASE);
	    ((DiscreteMapping<String, Paint>) nodeColorMapping).putMapValue("query and disease", NODE_COLOR_BOTH);
	    ((DiscreteMapping<String, Paint>) nodeColorMapping).putMapValue("query", NODE_COLOR_QUERY);
	}
	newStyle.addVisualMappingFunction(nodeColorMapping);

	// Border color mapping
	VisualMappingFunction<String, Paint> nodeBorderColorMapping = discFactory.createVisualMappingFunction(
		QUERY_GENE_ATTR_NAME, String.class, null, BasicVisualLexicon.NODE_BORDER_PAINT);

	((DiscreteMapping<String, Paint>) nodeBorderColorMapping).putMapValue("disease", NODE_BORDER_COLOR_DISEASE);
	((DiscreteMapping<String, Paint>) nodeBorderColorMapping).putMapValue("query and disease", NODE_BORDER_COLOR_BOTH);
	((DiscreteMapping<String, Paint>) nodeBorderColorMapping).putMapValue("query", NODE_BORDER_COLOR_QUERY);

	newStyle.addVisualMappingFunction(nodeBorderColorMapping);

	// Node Label Size mapping
	VisualMappingFunction<String, Integer> nodeLabelSizeMapping = discFactory.createVisualMappingFunction(
		QUERY_GENE_ATTR_NAME, String.class, null, BasicVisualLexicon.NODE_LABEL_FONT_SIZE);

	((DiscreteMapping<String, Integer>) nodeLabelSizeMapping).putMapValue("disease", NODE_LABEL_SIZE_LARGE);
	((DiscreteMapping<String, Integer>) nodeLabelSizeMapping).putMapValue("query and disease",
		NODE_LABEL_SIZE_LARGE);
	((DiscreteMapping<String, Integer>) nodeLabelSizeMapping).putMapValue("query", NODE_LABEL_SIZE_LARGE);

	newStyle.addVisualMappingFunction(nodeLabelSizeMapping);

	// Node Width & Height mapping
	final VisualMappingFunction<String, Double> nodeWidthMapping = discFactory.createVisualMappingFunction(
		QUERY_GENE_ATTR_NAME, String.class, null, BasicVisualLexicon.NODE_WIDTH);
	final VisualMappingFunction<String, Double> nodeHeightMapping = discFactory.createVisualMappingFunction(
		QUERY_GENE_ATTR_NAME, String.class, null, BasicVisualLexicon.NODE_HEIGHT);

	((DiscreteMapping<String, Double>) nodeWidthMapping).putMapValue("disease", NODE_WIDTH_TARGET);
	((DiscreteMapping<String, Double>) nodeWidthMapping).putMapValue("query and disease", NODE_WIDTH_BOTH);
	((DiscreteMapping<String, Double>) nodeWidthMapping).putMapValue("query", NODE_WIDTH_TARGET);
	((DiscreteMapping<String, Double>) nodeHeightMapping).putMapValue("disease", NODE_HEIGHT_TARGET);
	((DiscreteMapping<String, Double>) nodeHeightMapping).putMapValue("query and disease", NODE_HEIGHT_BOTH);
	((DiscreteMapping<String, Double>) nodeHeightMapping).putMapValue("query", NODE_HEIGHT_TARGET);

	newStyle.addVisualMappingFunction(nodeWidthMapping);
	newStyle.addVisualMappingFunction(nodeHeightMapping);

	// Label Mapping.
	final VisualMappingFunction<String, String> nodeLabelMapping = ptFactory.createVisualMappingFunction(
		CyTableEntry.NAME, String.class, null, BasicVisualLexicon.NODE_LABEL);
	newStyle.addVisualMappingFunction(nodeLabelMapping);

	return newStyle;
    }

}
