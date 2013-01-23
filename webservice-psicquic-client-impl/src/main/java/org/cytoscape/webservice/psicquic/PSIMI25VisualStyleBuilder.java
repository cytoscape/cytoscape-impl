package org.cytoscape.webservice.psicquic;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.webservice.psicquic.mapper.InteractionClusterMapper;

public class PSIMI25VisualStyleBuilder {

	// Default visual style name
	public static final String DEF_VS_NAME = "PSIMI 25 Style";

	// Presets
	private static final Color NODE_COLOR = Color.WHITE;
	private static final Color NODE_BORDER_COLOR = new Color(180, 180, 180);
	private static final Color NODE_LABEL_COLOR = new Color(100, 100, 100);
	private static final Color EDGE_COLOR = new Color(180, 180, 180);
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

	private final VisualStyleFactory vsFactory;

	private final VisualMappingFunctionFactory discreteMappingFactory;
	private final VisualMappingFunctionFactory passthroughMappingFactory;

	public PSIMI25VisualStyleBuilder(final VisualStyleFactory vsFactory,
			final VisualMappingFunctionFactory discreteMappingFactory,
			final VisualMappingFunctionFactory passthroughMappingFactory) {
		this.vsFactory = vsFactory;
		this.discreteMappingFactory = discreteMappingFactory;
		this.passthroughMappingFactory = passthroughMappingFactory;
	}

	public VisualStyle getVisualStyle() {

		final VisualStyle defStyle = vsFactory.createVisualStyle(DEF_VS_NAME);

		// Network VP
		final Color backGroundColor = Color.white;
		defStyle.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, backGroundColor);

		// Node Label Mapping
		final PassthroughMapping<String, String> labelPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(InteractionClusterMapper.PREDICTED_GENE_NAME, String.class,
						BasicVisualLexicon.NODE_LABEL);
		defStyle.addVisualMappingFunction(labelPassthrough);

		// Node View Defaults
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, NODE_BORDER_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, NODE_LABEL_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_FACE, NODE_LABEL_FONT);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, 210);

		defStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 1.0d);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, 150);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 30d);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 30d);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_SIZE, 30d);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 190);

		// Edge View Defaults
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 80);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 2.0d);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, EDGE_COLOR);

		// Node Color Mapping based on species name
		final DiscreteMapping<String, Paint> nodeColorMapping = (DiscreteMapping<String, Paint>) discreteMappingFactory
				.createVisualMappingFunction("taxonomy", String.class, BasicVisualLexicon.NODE_FILL_COLOR);

		nodeColorMapping.putMapValue("9606", COLOR_HUMAN);
		nodeColorMapping.putMapValue("10090", COLOR_MOUSE);
		nodeColorMapping.putMapValue("10116", COLOR_RAT);
		nodeColorMapping.putMapValue("7227", COLOR_FLY);
		nodeColorMapping.putMapValue("10116", COLOR_WORM);
		nodeColorMapping.putMapValue("4932", COLOR_YEAST);
		nodeColorMapping.putMapValue("83333", COLOR_ECOLI);
		nodeColorMapping.putMapValue("3702", COLOR_ARABIDOPSIS);

		defStyle.addVisualMappingFunction(nodeColorMapping);

		// Edge Color Mapping.  
		final DiscreteMapping<Boolean, Paint> edgeColorMapping = (DiscreteMapping<Boolean, Paint>) discreteMappingFactory
				.createVisualMappingFunction(InteractionClusterMapper.CROSS_SPECIES_EDGE, Boolean.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		edgeColorMapping.putMapValue(Boolean.TRUE, EDGE_CROSS_COLOR);
		defStyle.addVisualMappingFunction(edgeColorMapping);
		
		final DiscreteMapping<Boolean, LineType> edgeLineTypeMapping = (DiscreteMapping<Boolean, LineType>) discreteMappingFactory
				.createVisualMappingFunction(InteractionClusterMapper.CROSS_SPECIES_EDGE, Boolean.class, BasicVisualLexicon.EDGE_LINE_TYPE);
		edgeLineTypeMapping.putMapValue(Boolean.TRUE, LineTypeVisualProperty.DOT);
		defStyle.addVisualMappingFunction(edgeLineTypeMapping);

		return defStyle;
	}
}
