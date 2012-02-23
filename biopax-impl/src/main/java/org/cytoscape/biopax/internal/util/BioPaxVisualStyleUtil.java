// $Id: BioPaxVisualStyleUtil.java,v 1.17 2006/08/23 15:21:07 cerami Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.biopax.internal.util;

import static org.cytoscape.biopax.internal.BioPaxMapper.BIOPAX_EDGE_TYPE;
import static org.cytoscape.biopax.internal.BioPaxMapper.BIOPAX_ENTITY_TYPE;

import java.awt.Color;
import java.awt.Paint;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
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
 * @author Igor Rodchenkov (re-factoring using PaxTools API)
 */
public class BioPaxVisualStyleUtil {
	public static final Logger log = LoggerFactory
			.getLogger(BioPaxVisualStyleUtil.class);

	/**
	 * Name of BioPax Visual Style.
	 */
	public static final String BIO_PAX_VISUAL_STYLE = "BioPAX";

	/**
	 * size of physical entity node (default node size width)
	 */
	public static final double BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH = 20;

	// taken from DNodeView

	/**
	 * size of physical entity node (default node size height)
	 */
	public static final double BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT = 20;

	// taken from DNodeView

	/**
	 * size of physical entity node scale - (used to scale post tranlational
	 * modification nodes)
	 */
	public static final double BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE = 3;

	/**
	 * Size of interaction node
	 */
	private static final double BIO_PAX_VISUAL_STYLE_INTERACTION_NODE_SIZE_SCALE = 0.33;

	/**
	 * Size of complex node
	 */
	private static final double BIO_PAX_VISUAL_STYLE_COMPLEX_NODE_SIZE_SCALE = 0.33;

	/**
	 * Default color of nodes
	 */
	private static final Color DEFAULT_NODE_COLOR = new Color(255, 255, 255);

	/**
	 * Node border color
	 */
	private static final Color DEFAULT_NODE_BORDER_COLOR = new Color(0, 102,
			102);

	/**
	 * Complex node color
	 */
	private static final Color COMPLEX_NODE_COLOR = new Color(0, 0, 0);

	/**
	 * Complex node color
	 */
	private static final Color COMPLEX_NODE_BORDER_COLOR = COMPLEX_NODE_COLOR;

	VisualStyle style;

	private final VisualStyleFactory styleFactory;
	private final VisualMappingManager mappingManager;
	private final VisualMappingFunctionFactory discreteFactory;
	private final VisualMappingFunctionFactory passthroughFactory;

	public BioPaxVisualStyleUtil(VisualStyleFactory styleFactory,
			VisualMappingManager mappingManager,
			VisualMappingFunctionFactory discreteMappingFactory,
			VisualMappingFunctionFactory passthroughFactory) {
		this.styleFactory = styleFactory;
		this.mappingManager = mappingManager;
		this.discreteFactory = discreteMappingFactory;
		this.passthroughFactory = passthroughFactory;
	}

	/**
	 * Constructor. If an existing BioPAX Viz Mapper already exists, we use it.
	 * Otherwise, we create a new one.
	 * 
	 * @return VisualStyle Object.
	 */
	public VisualStyle getBioPaxVisualStyle() {
		// If the BioPAX Visual Style already exists, use this one instead.
		// The user may have tweaked the out-of-the box mapping, and we don't
		// want to over-ride these tweaks.
		synchronized (this) {
			if (style == null) {
				style = styleFactory.createVisualStyle(BIO_PAX_VISUAL_STYLE);
	
				// style.getDependency().set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED,false);
	
				createNodeSize(style);
				createNodeLabel(style);
				createNodeColor(style);
				createNodeBorderColor(style);
				createTargetArrows(style);
				createNodeShape(style);
				
				mappingManager.addVisualStyle(style);
			}
		}
		return style;
	}

	
	private void createNodeShape(VisualStyle style) {
		style.setDefaultValue(BasicVisualLexicon.NODE_SHAPE,
				NodeShapeVisualProperty.ELLIPSE);

		// create a discrete mapper, for mapping a biopax type to a shape
		DiscreteMapping<String, NodeShape> shape = (DiscreteMapping<String, NodeShape>) discreteFactory
				.createVisualMappingFunction(
						BIOPAX_ENTITY_TYPE, String.class, null,
						BasicVisualLexicon.NODE_SHAPE);

		// map all physical entities to circles
		for (Class<? extends BioPAXElement> claz : BioPaxUtil.getSubclassNames(PhysicalEntity.class)) 
		{
			String name = claz.getSimpleName();
			shape.putMapValue(name, NodeShapeVisualProperty.ELLIPSE);
		}

		// hack for phosphorylated proteins
		shape.putMapValue(BioPaxUtil.PROTEIN_PHOSPHORYLATED,
				NodeShapeVisualProperty.ELLIPSE);

		// map all interactions
		// - control to triangles
		// - others to square
		for (Class<?> c : BioPaxUtil.getSubclassNames(Interaction.class)) {
			String entityName = c.getSimpleName();
			if (Control.class.isAssignableFrom(c)) {
				shape.putMapValue(entityName,
						NodeShapeVisualProperty.TRIANGLE);
			} else {
				shape.putMapValue(entityName,
						NodeShapeVisualProperty.RECTANGLE);
			}
		}
		
		style.addVisualMappingFunction(shape);
	}

	
	private void createNodeSize(VisualStyle style) {
		// create a discrete mapper, for mapping biopax node type
		// to a particular node size.
		DiscreteMapping<String, Double> width = (DiscreteMapping<String, Double>) discreteFactory
				.createVisualMappingFunction(
						BIOPAX_ENTITY_TYPE, String.class, null,
						BasicVisualLexicon.NODE_WIDTH);
		DiscreteMapping<String, Double> height = (DiscreteMapping<String, Double>) discreteFactory
				.createVisualMappingFunction(
						BIOPAX_ENTITY_TYPE, String.class, null, 
						BasicVisualLexicon.NODE_HEIGHT);

		// map all interactions to required size
		for (Class c : BioPaxUtil.getSubclassNames(Interaction.class)) {
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

		/*
		 * // hack for phosphorylated proteins - make them large so label fits
		 * within node // commented out by Ethan Cerami, November 15, 2006
		 * discreteMappingWidth.putMapValue(BioPaxUtil.PROTEIN_PHOSPHORYLATED,
		 * new Double(BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH
		 * BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE));
		 * discreteMappingHeight.putMapValue(BioPaxUtil.PROTEIN_PHOSPHORYLATED,
		 * new Double(BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT
		 * BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE));
		 */

		// create and set node height calculator in node appearance calculator
		style.setDefaultValue(BasicVisualLexicon.NODE_WIDTH,
				BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH);
		style.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT,
				BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT);

		style.addVisualMappingFunction(width);
		style.addVisualMappingFunction(height);
	}

	
	private void createNodeLabel(VisualStyle style) {
		// create pass through mapper for node labels
		style.addVisualMappingFunction(passthroughFactory
				.createVisualMappingFunction(CyNode.NAME, String.class, null,
						BasicVisualLexicon.NODE_LABEL));
	}

	
	private void createNodeColor(VisualStyle style) {
		style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR,
				DEFAULT_NODE_COLOR);

		// create a discrete mapper, for mapping biopax node type
		// to a particular node color
		DiscreteMapping<String, Paint> color = (DiscreteMapping<String, Paint>) discreteFactory
				.createVisualMappingFunction(
						BIOPAX_ENTITY_TYPE, String.class, null, 
						BasicVisualLexicon.NODE_FILL_COLOR);

		// map all complex to black
		color.putMapValue("Complex", COMPLEX_NODE_COLOR);
		style.addVisualMappingFunction(color);
	}

	
	private void createNodeBorderColor(VisualStyle style) {
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT,
				DEFAULT_NODE_BORDER_COLOR);

		// create a discrete mapper, for mapping biopax node type
		// to a particular node color
		DiscreteMapping<String, Paint> function = (DiscreteMapping<String, Paint>) discreteFactory
				.createVisualMappingFunction(
						BIOPAX_ENTITY_TYPE, String.class, null,
						BasicVisualLexicon.NODE_BORDER_PAINT);

		// map all complex to black
		function.putMapValue("Complex", COMPLEX_NODE_BORDER_COLOR);
		style.addVisualMappingFunction(function);
	}

	
	private void createTargetArrows(VisualStyle style) {
		
		DiscreteMapping<String, ArrowShape> tgtArrowShape = 
			(DiscreteMapping<String, ArrowShape>) discreteFactory
				.createVisualMappingFunction(
						BIOPAX_EDGE_TYPE, String.class, null,
						BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);

		tgtArrowShape.putMapValue("right", ArrowShapeVisualProperty.DELTA);
		tgtArrowShape.putMapValue("controlled", ArrowShapeVisualProperty.DELTA);
		tgtArrowShape.putMapValue("cofactor", ArrowShapeVisualProperty.DELTA);
		tgtArrowShape.putMapValue("contains", ArrowShapeVisualProperty.CIRCLE);

		// Inhibition Edges
		for (ControlType controlType : ControlType.values()) {
			if (controlType.toString().startsWith("I")) {
				tgtArrowShape.putMapValue(controlType.toString(),
						ArrowShapeVisualProperty.T);
			}
		}

		// Activation Edges
		for (ControlType controlType : ControlType.values()) {
			if (controlType.toString().startsWith("A")) {
				tgtArrowShape.putMapValue(controlType.toString(),
						ArrowShapeVisualProperty.DELTA);
			}
		}

// old piece of code...
//		Calculator edgeTargetArrowCalculator = new BasicCalculator(
//				"BioPAX Target Arrows" + VERSION_POST_FIX, tgtArrowShape,
//				VisualPropertyType.EDGE_TGTARROW_SHAPE);
//		eac.setCalculator(edgeTargetArrowCalculator);
		
		style.addVisualMappingFunction(tgtArrowShape);
	}

}
