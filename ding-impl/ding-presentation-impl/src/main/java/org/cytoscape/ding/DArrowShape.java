package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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


import java.awt.Shape;
import java.util.Hashtable;
import java.util.Map;

import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;


/**
 * Defines arrow shapes.<br>
 * This replaces constants defined in Arrow.java.
 */
public enum DArrowShape {
	NONE("None", "NONE", ArrowShapeVisualProperty.NONE),
	DIAMOND("Diamond", "DIAMOND", ArrowShapeVisualProperty.DIAMOND),
	OPEN_DIAMOND("Open Diamond", "OPEN_DIAMOND", ArrowShapeVisualProperty.OPEN_DIAMOND),
	DELTA("Delta", "DELTA", ArrowShapeVisualProperty.DELTA),
	OPEN_DELTA("Open Delta", "OPEN_DELTA", ArrowShapeVisualProperty.OPEN_DELTA),
	CROSS_DELTA("Cross Delta", "CROSS_DELTA", ArrowShapeVisualProperty.CROSS_DELTA),
	CROSS_OPEN_DELTA("Cross Open Delta", "CROSS_OPEN_DELTA", ArrowShapeVisualProperty.CROSS_OPEN_DELTA),
	ARROW("Arrow", "ARROW", ArrowShapeVisualProperty.ARROW),
	T("T", "T", ArrowShapeVisualProperty.T),
	CIRCLE("Circle", "CIRCLE", ArrowShapeVisualProperty.CIRCLE),
	OPEN_CIRCLE("Open Circle", "OPEN_CIRCLE", ArrowShapeVisualProperty.OPEN_CIRCLE),
	HALF_CIRCLE("Half Circle", "HALF_CIRCLE", ArrowShapeVisualProperty.HALF_CIRCLE),
	OPEN_HALF_CIRCLE("Open Half Circle", "OPEN_HALF_CIRCLE", ArrowShapeVisualProperty.OPEN_HALF_CIRCLE),
	SQUARE("Square", "SQUARE", ArrowShapeVisualProperty.SQUARE),
	OPEN_SQUARE("Open Square", "OPEN_SQUARE", ArrowShapeVisualProperty.OPEN_SQUARE),
	HALF_TOP("Half Top", "HALF_TOP", ArrowShapeVisualProperty.HALF_TOP),
	HALF_BOTTOM("Half Top", "HALF_BOTTOM", ArrowShapeVisualProperty.HALF_BOTTOM),
	DELTA_SHORT_1("Delta Short 1", "DELTA_SHORT_1", ArrowShapeVisualProperty.DELTA_SHORT_1),
	DELTA_SHORT_2("Delta Short 2", "DELTA_SHORT_2", ArrowShapeVisualProperty.DELTA_SHORT_2),
	ARROW_SHORT("Arrow Short", "ARROW_SHORT", ArrowShapeVisualProperty.ARROW_SHORT),
	DIAMOND_SHORT_1("Diamond Short 1", "DIAMOND_SHORT_1", ArrowShapeVisualProperty.DIAMOND_SHORT_1),
	DIAMOND_SHORT_2("Diamond Short 2", "DIAMOND_SHORT_2", ArrowShapeVisualProperty.DIAMOND_SHORT_2);
	

	private final String displayName;
	private final String serializableString;
	private final ArrowShape presentationShape;
	
	private static final Map<ArrowShape, Shape> ARROW_SHAPES;
	
	/** old_key -> ArrowShape */
	private static final Map<String, DArrowShape> legacyShapes = new Hashtable<String, DArrowShape>();
	static {
		// We have to support Cytoscape 2.8 XGMML shapes!
		legacyShapes.put("0", NONE);
		legacyShapes.put("3", DELTA);
		legacyShapes.put("6", ARROW);
		legacyShapes.put("9", DIAMOND);
		legacyShapes.put("12", CIRCLE);
		legacyShapes.put("15", T);
		legacyShapes.put("16", HALF_TOP);
		legacyShapes.put("17", HALF_BOTTOM);
		ARROW_SHAPES = GraphGraphics.getArrowShapes();
	}
	
	private DArrowShape(final String displayName, final String serializableString, final ArrowShape presentationShape) {
		this.displayName = displayName;
		this.serializableString = serializableString;
		this.presentationShape = presentationShape;
	}

	/**
	 * Returns arrow shape definition.
	 */
	public ArrowShape getPresentationShape() {
		return presentationShape;
	}

	/**
	 * Returns human-readable name of this object.  This will be used in labels.
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	public Shape getShape() {
		return ARROW_SHAPES.get(presentationShape);
	}

	public static DArrowShape parseArrowText(final String text) {
		DArrowShape shape = null;
		
		if (text != null) {
			String key = text.trim().toUpperCase();
			
			try {
				shape = valueOf(key);
			} catch (IllegalArgumentException e) {
				// brilliant flow control--this isn't a problem, we just don't match
				
				// maybe it is an old 2.x key
				shape = legacyShapes.get(key);
				
				if (shape == null) {
					// if string doesn't match, then try other possible GINY names 
					for (DArrowShape val : values())  {
						if (val.displayName.equalsIgnoreCase(text)) {
							shape = val;
							break;
						}
					}
				}
			}
		}
		
		if (shape == null) shape = NONE;

		return shape;
	}

	public static DArrowShape getArrowShape(final ArrowShape arrowShape) {
		final String serializedString = arrowShape.getSerializableString();
		// first try for an exact match
		for (DArrowShape shape : values()) {
			if (easyStringCompart(shape.serializableString,serializedString))
				return shape;
		}

		// if we can't match anything, just return NONE.
		return NONE;
	}

	// remove spaces, underscores and case sensitivity
	private static boolean easyStringCompart(String a, String b) {
		if (a==null) return b==null;
		if (b==null) return false;
		String likeA = a.replace(" ", "").replace("_", "");
		String likeB = b.replace(" ", "").replace("_", "");
		return likeA.equalsIgnoreCase(likeB);
	}
}
