package org.cytoscape.util.internal.color;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteType;

public abstract class AbstractPalette implements Palette {
	protected String name;
	protected int size;
	protected PaletteType type;
	protected boolean colorBlindSafe;

	AbstractPalette(String name, int size, PaletteType type, boolean cbs) {
		this.name = name;
		this.size = size;
		this.type = type;
		this.colorBlindSafe = cbs;
	}

	@Override
	public String getName() { return name; }

	@Override
	public Object getIdentifier() { return name; }

	@Override
	public PaletteType getType() { return type; }

	@Override
	public boolean isColorBlindSafe() { return colorBlindSafe; }

	@Override
	public int size() { return size; }

	public abstract Color[] getColors();
	public abstract Color[] getColors(int nColors);
}
