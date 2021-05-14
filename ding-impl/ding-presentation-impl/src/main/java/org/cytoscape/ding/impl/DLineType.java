package org.cytoscape.ding.impl;

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

import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.impl.strokes.DashDotStroke;
import org.cytoscape.ding.impl.strokes.DotStroke;
import org.cytoscape.ding.impl.strokes.EqualDashStroke;
import org.cytoscape.ding.impl.strokes.LongDashStroke;
import org.cytoscape.ding.impl.strokes.SolidStroke;
import org.cytoscape.ding.impl.strokes.WidthStroke;
import org.cytoscape.ding.impl.strokes.ZeroStroke;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;

public class DLineType implements LineType {

	private static final DLineType SOLID = new DLineType(
			LineTypeVisualProperty.SOLID.getDisplayName(),
			LineTypeVisualProperty.SOLID.getSerializableString(),
			new SolidStroke(1.0f));
	private static final DLineType LONG_DASH = new DLineType(
			LineTypeVisualProperty.LONG_DASH.getDisplayName(),
			LineTypeVisualProperty.LONG_DASH.getSerializableString(),
			new LongDashStroke(1.0f));
	private static final DLineType DOT = new DLineType(
			LineTypeVisualProperty.DOT.getDisplayName(),
			LineTypeVisualProperty.DOT.getSerializableString(),
			new DotStroke(1.0f));
	private static final DLineType DASH_DOT = new DLineType(
			LineTypeVisualProperty.DASH_DOT.getDisplayName(),
			LineTypeVisualProperty.DASH_DOT.getSerializableString(),
			new DashDotStroke(1.0f));
	private static final DLineType EQUAL_DASH = new DLineType(
			LineTypeVisualProperty.EQUAL_DASH.getDisplayName(),
			LineTypeVisualProperty.EQUAL_DASH.getSerializableString(),
			new EqualDashStroke(1.0f));
	
	private static final Map<LineType, DLineType> DEF_LINE_TYPE_MAP;

	static {
		DEF_LINE_TYPE_MAP = new HashMap<LineType, DLineType>();
		DEF_LINE_TYPE_MAP.put(LineTypeVisualProperty.SOLID, SOLID);
		DEF_LINE_TYPE_MAP.put(LineTypeVisualProperty.LONG_DASH, LONG_DASH);
		DEF_LINE_TYPE_MAP.put(LineTypeVisualProperty.DOT, DOT);
		DEF_LINE_TYPE_MAP.put(LineTypeVisualProperty.DASH_DOT, DASH_DOT);
		DEF_LINE_TYPE_MAP.put(LineTypeVisualProperty.EQUAL_DASH, EQUAL_DASH);
	}

	public static final DLineType getDLineType(final LineType lineType) {
		if(lineType instanceof DLineType)
			return (DLineType) lineType;
		else {
			final DLineType dl = DEF_LINE_TYPE_MAP.get(lineType);
			if(dl == null)
				return DLineType.SOLID;
			else
				return dl;
		}
	}

	
	private final String displayName;
	private final String serializableString;
	
	private final WidthStroke stroke;
	
	public DLineType(final String displayName, final String serializableString,
			final WidthStroke stroke) {
		this.displayName = displayName;
		this.serializableString = serializableString;
		this.stroke = stroke;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
	
	@Override public String toString() {
		return displayName;
	}

	@Override
	public String getSerializableString() {
		return serializableString;
	}

	
	/**
	 * Creates a new stroke of this LineStyle with the specified width.
	 */
	public Stroke getStroke(final float width) {
		if ( width <= 0 )
			return new ZeroStroke(stroke);
		else
			return stroke.newInstanceForWidth( width );
	}

}
