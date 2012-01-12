/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.ding;


import java.awt.Shape;
import java.util.Hashtable;
import java.util.Map;

import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.view.presentation.property.values.ArrowShape;


/**
 * Defines arrow shapes.<br>
 * This replaces constants defined in Arrow.java.
 *
 *
 */
public enum DArrowShape {
	NONE("None", "NONE", GraphGraphics.ARROW_NONE),
	DIAMOND("Diamond", "DIAMOND", GraphGraphics.ARROW_DIAMOND),
	DELTA("Delta", "DELTA", GraphGraphics.ARROW_DELTA),
	ARROW("Arrow", "ARROW", GraphGraphics.ARROW_ARROWHEAD),
	T("T", "T", GraphGraphics.ARROW_TEE),
	CIRCLE("Circle", "CIRCLE", GraphGraphics.ARROW_DISC),
	HALF_TOP("Half Top", "HALF_TOP", GraphGraphics.ARROW_HALF_TOP),
	HALF_BOTTOM("Half Top", "HALF_BOTTOM", GraphGraphics.ARROW_HALF_BOTTOM);
	

	private final String displayName;
	private final String serializableString;
	private final byte rendererTypeID;
	
	private static final Map<Byte, Shape> ARROW_SHAPES;
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
	
	
	private DArrowShape(final String displayName, final String serializableString, final byte rendererTypeID) {
		this.displayName = displayName;
		this.rendererTypeID = rendererTypeID;
		this.serializableString = serializableString;
	}

	/**
	 * Returns arrow type ID used in renderer code.
	 *
	 * @return
	 */
	public byte getRendererTypeID() {
		return rendererTypeID;
	}


	/**
	 * Returns human-readable name of this object.  This will be used in labels.
	 *
	 * @return DOCUMENT ME!
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	public Shape getShape() {
		return ARROW_SHAPES.get((Byte) rendererTypeID);
	}

	/**
	 *
	 * @param text
	 * @return
	 */
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
			if (shape.serializableString.equals(serializedString))
				return shape;
		}

		// if we can't match anything, just return NONE.
		return NONE;
	}
}
