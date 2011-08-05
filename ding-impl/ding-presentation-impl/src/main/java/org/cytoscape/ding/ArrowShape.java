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


import java.util.Hashtable;
import java.util.Map;

import org.cytoscape.graph.render.immed.GraphGraphics;


/**
 * Defines arrow shapes.<br>
 * This replaces constants defined in Arrow.java.
 *
 *
 */
public enum ArrowShape {
	NONE("No Arrow", GraphGraphics.ARROW_NONE),
	DIAMOND("Diamond", GraphGraphics.ARROW_DIAMOND),
	DELTA("Delta", GraphGraphics.ARROW_DELTA),
	ARROW("Arrow", GraphGraphics.ARROW_ARROWHEAD),
	T("T", GraphGraphics.ARROW_TEE),
	CIRCLE("Circle", GraphGraphics.ARROW_DISC),
	HALF_TOP("Half Top", GraphGraphics.ARROW_HALF_TOP),
	HALF_BOTTOM("Half Top", GraphGraphics.ARROW_HALF_BOTTOM);
	

	private final String displayName;
	private final byte rendererTypeID;
	
	/** old_key -> ArrowShape */
	private static final Map<String, ArrowShape> legacyShapes = new Hashtable<String, ArrowShape>();

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
	}
	
	private ArrowShape(final String displayName, final byte rendererTypeID) {
		this.displayName = displayName;
		this.rendererTypeID = rendererTypeID;
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

	/**
	 *
	 * @param text
	 * @return
	 */
	public static ArrowShape parseArrowText(final String text) {
		ArrowShape shape = null;
		
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
					for (ArrowShape val : values())  {
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

	/**
	 * DOCUMENT ME!
	 *
	 * @param rendererTypeID
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static ArrowShape getArrowShape(final int rendererTypeID) {
		// first try for an exact match
		for (ArrowShape shape : values()) {
			if (shape.getRendererTypeID() == rendererTypeID)
				return shape;
		}

		// if we can't match anything, just return NONE.
		return NONE;
	}

//	/**
//	 *  DOCUMENT ME!
//	 *
//	 * @return  DOCUMENT ME!
//	 */
//	public Shape getShape() {
//		return arrowShapes.get(ginyType);
//	}

//	/**
//	 *  DOCUMENT ME!
//	 *
//	 * @param size DOCUMENT ME!
//	 *
//	 * @return  DOCUMENT ME!
//	 */
//	public static Map<Object, Icon> getIconSet() {
//		Map<Object, Icon> arrowShapeIcons = new HashMap<Object, Icon>();
//
//		for (ArrowShape shape : values()) {
//			ArrowIcon icon = new ArrowIcon(arrowShapes.get(shape.getGinyArrow()), 
//			                               VisualPropertyIcon.DEFAULT_ICON_SIZE, 
//			                               VisualPropertyIcon.DEFAULT_ICON_SIZE, 
//										   shape.getName());
//			arrowShapeIcons.put(shape, icon);
//		}
//
//		return arrowShapeIcons;
//	}
}
