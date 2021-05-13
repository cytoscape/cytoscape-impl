package org.cytoscape.graph.render.stateful;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.GlyphVector;

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


/**
 * A simple class to hold the width and height of a given string in terms
 * of specific fonts, rendering contexts, etc.. 
 */
public class LabelLineInfo {
	
	private static char[] charBuff = new char[20];
	
	private final LabelInfo parent;
	private final String text;
	private final double width;
	private final double height;

	private Shape shape;
	private GlyphVector glyphVector;
	
	public LabelLineInfo(LabelInfo parent, String text, double width, double height) {
		this.parent = parent;
		this.text = text;
		this.width = width;
		this.height = height;
	}

	private GlyphVector createGlyphVector() {
		if (text.length() > charBuff.length) {
			charBuff = new char[Math.max(charBuff.length * 2, text.length())];
		}
		text.getChars(0, text.length(), charBuff, 0);
		return parent.getFont().layoutGlyphVector(parent.getFontRenderContext(), charBuff, 0, text.length(), Font.LAYOUT_NO_LIMIT_CONTEXT);
	}
	
	public GlyphVector getGlyphVector() {
		if(glyphVector == null) {
			glyphVector = createGlyphVector();
		}
		return glyphVector;
	}
	
	public Shape getShape() {
		if(shape == null) {
			GlyphVector glyphVector = getGlyphVector();
			shape = glyphVector.getOutline();
		}
		return shape;
	}
	
	public Font getFont() {
		return parent.getFont();
	}
	
	public String getText() {
		return text;
	}
	
	public String getLine() {
		return text;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public String toString() {
		return "'" + text + "'  w:" + width + " h:" + height;
	}
}
