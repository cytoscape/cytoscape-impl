package org.cytoscape.biopax.internal.util;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
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

import static org.cytoscape.biopax.internal.BioPaxMapper.BIOPAX_ENTITY_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.cytoscape.biopax.internal.BioPaxMapper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates an "out-of-the-box" default Visual Mapper for rendering BioPAX
 * networks.
 * 
 * @author Ethan Cerami
 * @author Igor Rodchenkov (re-factoring using PaxTools API, migrating to Cytoscape 3.x)
 */
public class VisualStyleUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(VisualStyleUtil.class);
	
	/**
	 * size of physical entity node (default node size width)
	 */
	public static final double BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH = 20;

	/**
	 * size of physical entity node (default node size height)
	 */
	public static final double BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT = 20;

	/**
	 * size of physical entity node scale - (used to scale post tranlational
	 * modification nodes)
	 */
	public static final double BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE = 3;


	private static final String BIO_PAX_VISUAL_STYLE = "BioPAX";
	private static final String BINARY_SIF_VISUAL_STYLE = "BioPAX_SIF";
	
	private static final double BIO_PAX_VISUAL_STYLE_INTERACTION_NODE_SIZE_SCALE = 0.67;
	private static final double BIO_PAX_VISUAL_STYLE_COMPLEX_NODE_SIZE_SCALE = 0.67;
	private static final Color DEFAULT_NODE_COLOR = new Color(255, 255, 255);
	private static final Color DEFAULT_NODE_BORDER_COLOR = new Color(0, 102, 102);
	private static final Color COMPLEX_NODE_COLOR = DEFAULT_NODE_COLOR; //new Color(0, 0, 0);
	private static final Color COMPLEX_NODE_BORDER_COLOR = DEFAULT_NODE_BORDER_COLOR; //COMPLEX_NODE_COLOR;
	
	private static final String COMPONENT_OF = "COMPONENT_OF";
	private static final String COMPONENT_IN_SAME = "IN_SAME_COMPONENT";
	private static final String SEQUENTIAL_CATALYSIS = "SEQUENTIAL_CATALYSIS";
	private static final String CONTROLS_STATE_CHANGE = "STATE_CHANGE";
	private static final String CONTROLS_METABOLIC_CHANGE = "METABOLIC_CATALYSIS";
	private static final String PARTICIPATES_CONVERSION = "REACTS_WITH";
	private static final String PARTICIPATES_INTERACTION = "INTERACTS_WITH";
	private static final String CO_CONTROL = "CO_CONTROL";	
	//edge attr. name created by the core SIF reader
	private static final String INTERACTION = "interaction"; 
	
	
	private final VisualStyleFactory styleFactory;
	private final VisualMappingManager mappingManager;
	private final VisualMappingFunctionFactory discreteFactory;
	private final VisualMappingFunctionFactory passthroughFactory;
	
	
	private VisualStyle simpleBiopaxStyle;
	private VisualStyle binarySifStyle;

	//TODO use customPhosGraphics? (not migrated from Cy 2.x)
	// custom node images (phosphorylation)	
	private static BufferedImage[] customPhosGraphics = null;	
	
	static {
		try {
			BufferedImage phosNode = javax.imageio.ImageIO.read
                    (BioPaxMapper.class.getResource("phos-node.jpg"));
			BufferedImage phosNodeSelectedTop = javax.imageio.ImageIO.read
                    (BioPaxMapper.class.getResource("phos-node-selected-top.jpg"));
			BufferedImage phosNodeSelectedRight = javax.imageio.ImageIO.read
                    (BioPaxMapper.class.getResource("phos-node-selected-right.jpg"));
			BufferedImage phosNodeSelectedBottom = javax.imageio.ImageIO.read
                    (BioPaxMapper.class.getResource("phos-node-selected-bottom.jpg"));
			BufferedImage phosNodeSelectedLeft = javax.imageio.ImageIO.read
                    (BioPaxMapper.class.getResource("phos-node-selected-left.jpg"));
			customPhosGraphics = new BufferedImage[] {
					phosNode,
			        phosNodeSelectedTop,
			        phosNodeSelectedRight,
			        phosNodeSelectedBottom,
			        phosNodeSelectedLeft
			};			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param styleFactory
	 * @param mappingManager
	 * @param discreteMappingFactory
	 * @param passthroughFactory
	 */
	public VisualStyleUtil(VisualStyleFactory styleFactory,
			VisualMappingManager mappingManager,
			VisualMappingFunctionFactory discreteMappingFactory,
			VisualMappingFunctionFactory passthroughFactory) {
		this.styleFactory = styleFactory;
		this.mappingManager = mappingManager;
		this.discreteFactory = discreteMappingFactory;
		this.passthroughFactory = passthroughFactory;
	}

	
	/**
	 * If an existing BioPAX style found, use it;
	 * otherwise, create a new one.
	 * 
	 * @return VisualStyle Object.
	 */
	public synchronized VisualStyle getBioPaxVisualStyle() {
		// If the BioPAX Visual Style already exists, use this one instead.
		// The user may have tweaked the out-of-the box mapping, and we don't
		// want to over-ride these tweaks.
		if (simpleBiopaxStyle == null) {
			simpleBiopaxStyle = styleFactory.createVisualStyle(BIO_PAX_VISUAL_STYLE);

			// unlock node size, color
			for(VisualPropertyDependency<?> vpd : simpleBiopaxStyle.getAllVisualPropertyDependencies()) {
				if(vpd.getIdString().equals("nodeSizeLocked")) {
					vpd.setDependency(false);
				} else if(vpd.getIdString().equals("arrowColorMatchesEdge")) {
					vpd.setDependency(true);
				}
			}

			// Node size
			// create a discrete mapper, for mapping biopax node type
			// to a particular node size.
			DiscreteMapping<String, Double> width = (DiscreteMapping<String, Double>) discreteFactory
					.createVisualMappingFunction(BIOPAX_ENTITY_TYPE, String.class, NODE_WIDTH);
			DiscreteMapping<String, Double> height = (DiscreteMapping<String, Double>) discreteFactory
					.createVisualMappingFunction(BIOPAX_ENTITY_TYPE, String.class, NODE_HEIGHT);
			// map all interactions to required size
			for (Class c : BioPaxMapper.getSubclassNames(Interaction.class)) {
				String entityName = c.getSimpleName();
				width.putMapValue(entityName,
						new Double(BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH
								* BIO_PAX_VISUAL_STYLE_INTERACTION_NODE_SIZE_SCALE));
				height.putMapValue(entityName,
						new Double(BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT
								* BIO_PAX_VISUAL_STYLE_INTERACTION_NODE_SIZE_SCALE));
			}

			// map all complex to required size
			String entityName = "Complex";//c.getSimpleName();
			width.putMapValue(entityName,
				new Double(BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH
					* BIO_PAX_VISUAL_STYLE_COMPLEX_NODE_SIZE_SCALE));
			height.putMapValue(entityName,
				new Double(BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT
					* BIO_PAX_VISUAL_STYLE_COMPLEX_NODE_SIZE_SCALE));
			// create and set node height calculator in node appearance calculator
			simpleBiopaxStyle.setDefaultValue(NODE_WIDTH,
					BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH);
			simpleBiopaxStyle.setDefaultValue(NODE_HEIGHT,
					BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT);
			simpleBiopaxStyle.addVisualMappingFunction(width);
			simpleBiopaxStyle.addVisualMappingFunction(height);
			
			// Node label
			// create pass through mapper for node labels
			simpleBiopaxStyle.addVisualMappingFunction(passthroughFactory
					.createVisualMappingFunction(CyNetwork.NAME, String.class,
							NODE_LABEL));
			
			// Node color
			simpleBiopaxStyle.setDefaultValue(NODE_FILL_COLOR, DEFAULT_NODE_COLOR);
			// create a discrete mapper, for mapping biopax node type
			// to a particular node color
			DiscreteMapping<String, Paint> color = (DiscreteMapping<String, Paint>) discreteFactory
					.createVisualMappingFunction(BIOPAX_ENTITY_TYPE, String.class, NODE_FILL_COLOR);
			// map all complex to black
			color.putMapValue("Complex", COMPLEX_NODE_COLOR);
			simpleBiopaxStyle.addVisualMappingFunction(color);
			
			// Node border color
			simpleBiopaxStyle.setDefaultValue(NODE_BORDER_PAINT, DEFAULT_NODE_BORDER_COLOR);
			// create a discrete mapper, for mapping biopax node type
			// to a particular node color
			DiscreteMapping<String, Paint> paintFunction = (DiscreteMapping<String, Paint>) discreteFactory
					.createVisualMappingFunction(BIOPAX_ENTITY_TYPE, String.class, NODE_BORDER_PAINT);
			// map all complex to black
			paintFunction.putMapValue("Complex", COMPLEX_NODE_BORDER_COLOR);
			simpleBiopaxStyle.addVisualMappingFunction(paintFunction);
			
			// Target arrows
			DiscreteMapping<String, ArrowShape> tgtArrowShape = 
					(DiscreteMapping<String, ArrowShape>) discreteFactory
						.createVisualMappingFunction(INTERACTION, String.class, EDGE_TARGET_ARROW_SHAPE);
			tgtArrowShape.putMapValue("right", ArrowShapeVisualProperty.DELTA);
			tgtArrowShape.putMapValue("controlled", ArrowShapeVisualProperty.DELTA);
			tgtArrowShape.putMapValue("cofactor", ArrowShapeVisualProperty.DELTA);
			tgtArrowShape.putMapValue("contains", ArrowShapeVisualProperty.CIRCLE);
			// Inhibition Edges
			for (ControlType controlType : ControlType.values()) {
				if (controlType.toString().startsWith("I"))
					tgtArrowShape.putMapValue(controlType.toString(),
							ArrowShapeVisualProperty.T);
			}
			// Activation Edges
			for (ControlType controlType : ControlType.values()) {
				if (controlType.toString().startsWith("A"))
					tgtArrowShape.putMapValue(controlType.toString(),
							ArrowShapeVisualProperty.DELTA);
			}
			simpleBiopaxStyle.addVisualMappingFunction(tgtArrowShape);
			
			// Node shape
			simpleBiopaxStyle.setDefaultValue(NODE_SHAPE,
					NodeShapeVisualProperty.ELLIPSE);
			// create a discrete mapper, for mapping a biopax type to a shape
			DiscreteMapping<String, NodeShape> shape = (DiscreteMapping<String, NodeShape>) discreteFactory
					.createVisualMappingFunction(BIOPAX_ENTITY_TYPE, String.class, NODE_SHAPE);
			// map all physical entities to circles
			for (Class<? extends BioPAXElement> claz : BioPaxMapper.getSubclassNames(PhysicalEntity.class)) 
			{
				String name = claz.getSimpleName();
				shape.putMapValue(name, NodeShapeVisualProperty.ELLIPSE);
			}
			// use a different shape for Complex nodes
			shape.putMapValue("Complex", NodeShapeVisualProperty.DIAMOND);
			// hack for phosphorylated proteins
			shape.putMapValue(BioPaxMapper.PROTEIN_PHOSPHORYLATED,
					NodeShapeVisualProperty.ELLIPSE);

			// map all interactions: control to triangles, others to square
			for (Class<?> c : BioPaxMapper.getSubclassNames(Interaction.class)) {
				if (Control.class.isAssignableFrom(c))
					shape.putMapValue(c.getSimpleName(), NodeShapeVisualProperty.TRIANGLE);
				else 
					shape.putMapValue(c.getSimpleName(), NodeShapeVisualProperty.RECTANGLE);
			}
			
			simpleBiopaxStyle.addVisualMappingFunction(shape);
			mappingManager.addVisualStyle(simpleBiopaxStyle);
		}
		
		return simpleBiopaxStyle;
	}

	
	 /**
	 * If the existing Binary SIF style found, use it;
	 * otherwise, create a new style.
	 *
	 * @return VisualStyle Object.
	 */
	public synchronized VisualStyle getBinarySifVisualStyle() {
		for(VisualStyle vs : mappingManager.getAllVisualStyles()) {
			if(BINARY_SIF_VISUAL_STYLE.equals(vs.getTitle()))
				binarySifStyle = vs;
		}

		if (binarySifStyle == null) {
			binarySifStyle = styleFactory.createVisualStyle(BINARY_SIF_VISUAL_STYLE);

			// set node opacity
			binarySifStyle.setDefaultValue(NODE_TRANSPARENCY, 125);
			// unlock node size, color
			for(VisualPropertyDependency<?> vpd : binarySifStyle.getAllVisualPropertyDependencies()) {
				if(vpd.getIdString().equals("nodeSizeLocked")) {
					vpd.setDependency(false);
				} else if(vpd.getIdString().equals("arrowColorMatchesEdge")) {
					vpd.setDependency(true);
				}
			}

			//Node shape
			// Default shape is an ellipse.
			binarySifStyle.setDefaultValue(NODE_SHAPE,
					NodeShapeVisualProperty.ELLIPSE);
			// Complexes are Hexagons.
			DiscreteMapping<String, NodeShape> shapeFunction = (DiscreteMapping<String, NodeShape>) discreteFactory
					.createVisualMappingFunction(BioPaxMapper.BIOPAX_ENTITY_TYPE, String.class, NODE_SHAPE);
			shapeFunction.putMapValue("Complex", NodeShapeVisualProperty.HEXAGON);
			shapeFunction.putMapValue("(generic)", NodeShapeVisualProperty.OCTAGON);
			binarySifStyle.addVisualMappingFunction(shapeFunction);

			// Node color
			Color color = new Color(255, 153, 153);
			binarySifStyle.setDefaultValue(NODE_FILL_COLOR, color);
			// Complexes are a Different Color.
			Color lightBlue = new Color(153, 153, 255);
			DiscreteMapping<String, Paint> paintFunction = (DiscreteMapping<String, Paint>) discreteFactory
					.createVisualMappingFunction(BioPaxMapper.BIOPAX_ENTITY_TYPE, String.class, NODE_FILL_COLOR);
			paintFunction.putMapValue("Complex", lightBlue);
			paintFunction.putMapValue("(generic)", lightBlue);
			binarySifStyle.addVisualMappingFunction(paintFunction);

			// Node label
			// create pass through mapper for node labels	
			binarySifStyle.addVisualMappingFunction(passthroughFactory
					.createVisualMappingFunction(CyNetwork.NAME, String.class, NODE_LABEL));

			binarySifStyle.setDefaultValue(EDGE_WIDTH, 4.0);

			// Edge color
			// create a discrete mapper, for mapping edge type to a particular edge color
			binarySifStyle.setDefaultValue(EDGE_PAINT, Color.BLACK);
			paintFunction = (DiscreteMapping<String, Paint>) discreteFactory
					.createVisualMappingFunction(INTERACTION, String.class, EDGE_PAINT);
			paintFunction.putMapValue(PARTICIPATES_CONVERSION, Color.decode("#ccc1da"));
			paintFunction.putMapValue(PARTICIPATES_INTERACTION, Color.decode("#7030a0"));
			paintFunction.putMapValue(CONTROLS_STATE_CHANGE, Color.decode("#0070c0"));
			paintFunction.putMapValue(CONTROLS_METABOLIC_CHANGE, Color.decode("#00b0f0"));
			paintFunction.putMapValue(SEQUENTIAL_CATALYSIS, Color.decode("#7f7f7f"));
			paintFunction.putMapValue(CO_CONTROL, Color.decode("#ff0000"));
			paintFunction.putMapValue(COMPONENT_IN_SAME, Color.decode("#ffff00"));
			paintFunction.putMapValue(COMPONENT_OF, Color.decode("#ffc000"));
			binarySifStyle.addVisualMappingFunction(paintFunction);

			//Edge direction
			DiscreteMapping<String, ArrowShape> discreteMapping = 
					(DiscreteMapping<String, ArrowShape>) discreteFactory
					.createVisualMappingFunction(INTERACTION, String.class, EDGE_TARGET_ARROW_SHAPE);
			discreteMapping.putMapValue(COMPONENT_OF, ArrowShapeVisualProperty.ARROW);
			discreteMapping.putMapValue(CONTROLS_STATE_CHANGE, ArrowShapeVisualProperty.ARROW);
			discreteMapping.putMapValue(CONTROLS_METABOLIC_CHANGE, ArrowShapeVisualProperty.ARROW);
			discreteMapping.putMapValue(SEQUENTIAL_CATALYSIS, ArrowShapeVisualProperty.ARROW);
			binarySifStyle.addVisualMappingFunction(discreteMapping);

			binarySifStyle.setDefaultValue(NETWORK_BACKGROUND_PAINT, Color.WHITE);

			// The visual style must be added to the Global Catalog
			// in order for it to be written out to vizmap.props upon user exit
			mappingManager.addVisualStyle(binarySifStyle);
		}
		
		return binarySifStyle;
	}	
	
	/**
	 * Based on given arguments, determines proper rectangle coordinates
	 * used to render custom node shape.
	 */
	private static Rectangle2D getCustomShapeRect(BufferedImage image, int modificationCount) {
		// our scale factor
		double scale = .1;
		final double[] startX = {
					0, (BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH * BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE) / 2,
		            0, (-1 * BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH * BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE) / 2
		};

		final double[] startY = {
					(-1 * BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT * BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE) / 2, 0, 
		            (BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT * BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE) / 2, 0
		};

		// create and return rect
		return new java.awt.geom.Rectangle2D
				.Double(startX[modificationCount] + ((-1 * (image.getWidth() / 2)) * scale),
		                startY[modificationCount] + ((-1 * (image.getHeight() / 2)) * scale),
		                (double) image.getWidth() * scale,
		                (double) image.getHeight() * scale
		        );
	}
}
