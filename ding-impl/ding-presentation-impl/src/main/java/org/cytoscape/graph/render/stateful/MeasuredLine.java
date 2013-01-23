package org.cytoscape.graph.render.stateful;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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


/**
 * A simple class to hold the width and height of a given string in terms
 * of specific fonts, rendering contexts, etc.. 
 */
class MeasuredLine {
	private final String line;
	private final double width;
	private final double height;

	public MeasuredLine(final String line, final double width, final double height) {
		this.line = line;
		this.width = width;
		this.height = height;
	}

	public String getLine() {
		return line;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public String toString() {
		return "'" + line + "'  w:" + width + " h:" + height;
	}
}
