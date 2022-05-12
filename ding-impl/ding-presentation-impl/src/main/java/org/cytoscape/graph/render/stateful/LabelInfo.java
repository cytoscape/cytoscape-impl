package org.cytoscape.graph.render.stateful;

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


import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;


/**
 * A class that takes a String and a number of rendering parameters and then 
 * splits the text into MeasuredLines based on newline characters and whether 
 * line length is otherwise greater than the specified label width limit.
 */
public class LabelInfo {
	
	private double maxLineWidth;
	private double totalHeight;
	private final double labelWidth;
	private final FontRenderContext frc;
	private final Font font; 
	private final boolean textAsShape;
	private final List<LabelLineInfo> measuredLines;
	
	public LabelInfo(String rawLine, Font font, FontRenderContext frc, boolean textAsShape, double labelWidth) {
		this.font = font;
		this.frc = frc;
		this.textAsShape = textAsShape;
		this.labelWidth = labelWidth;
		this.measuredLines = new ArrayList<LabelLineInfo>();

		String[] lines = rawLine.split("\n", -1);
		calculateRawBounds(lines); 
		createMeasuredLines(lines);
	}

	public Font getFont() {
		return font;
	}
	
	public FontRenderContext getFontRenderContext() {
		return frc;
	}
	
	/** 
	 * Does a first pass at calculating the bounds of all lines. For short strings
	 * (i.e. the norm) this is sufficient calculation.
	 */
	private void calculateRawBounds(String[] lines) {
		maxLineWidth = 0;
		totalHeight = 0;
		for(String line : lines) { 
			Rectangle2D bounds = calcBounds(line);
			updateBounds(bounds.getWidth(),bounds.getHeight());
		}
	}

	/**
	 * Simply updates the maxLineWidth and totalHeight.  We do it in a separate method
	 * to make sure we do it consistently.
	 */
	private void updateBounds(double newWidth, double newHeight) {
		maxLineWidth = Math.max(maxLineWidth, newWidth);
		totalHeight += newHeight;
	}

	/**
	 * Calculates the bounds of a single string.
	 */
	private Rectangle2D calcBounds(final String s) {
		if(textAsShape) {
			char[] charBuff = s.toCharArray(); 
			GlyphVector glyphV = font.layoutGlyphVector(frc, charBuff, 0, charBuff.length, Font.LAYOUT_NO_LIMIT_CONTEXT);
			return glyphV.getLogicalBounds();
		} else {
			return font.getStringBounds(s, frc);
		}
	}

	/**
	 * Splits the raw lines according to how many lines are present and if any of 
	 * the lines are too long.  Recalculates the maxLineWidth and totalHeight based
	 * on the new lines created.
	 */
	private void createMeasuredLines(String[] lines) {
		// There's only one line and it's short, i.e. what usually happens.
		if(lines.length == 1 && labelWidth > maxLineWidth) {
			measuredLines.add(new LabelLineInfo(this, lines[0], maxLineWidth, totalHeight));
			return;
		}

		// There are multiple and/or longer-than-allowed lines.   
		// Process each of them. Also update overall widths and heights 
		// as those may change. 
		totalHeight = 0;
		maxLineWidth = 0;
	
		for(String line : lines) {
			double currentWidth = 0;
			double wordWidth = 0; 
			double wordHeight = 0; 
			StringBuilder currentLine = new StringBuilder();

			// Split each line based on the space char and then build up
			// new lines by concatenating the words together into a new
			// new line that is within the specified length. 
			String[] words = line.split(" ");
			for (String w : words) {
				String word = w + " ";	
				Rectangle2D bounds = calcBounds(word);
				wordWidth = bounds.getWidth();
				wordHeight = bounds.getHeight();

				// If the current line width plus the new word
				// width is >= than the label width save the line
				if (currentWidth + wordWidth >= labelWidth) {
					// only write the string if something is there
					if ( currentWidth > 0 ) {
						measuredLines.add(new LabelLineInfo(this, currentLine.toString(), currentWidth,wordHeight));
						updateBounds(currentWidth,wordHeight);
						currentLine.delete(0,currentLine.length());
					}

					// if the word itself is >= the label width,
					// make the word itself a new line
					if ( wordWidth >= labelWidth ) {
						measuredLines.add(new LabelLineInfo(this, word,wordWidth,wordHeight) );
						updateBounds(wordWidth,wordHeight);
						currentWidth = 0;

					// otherwise use the word as the beginning of a new line
					} else {
						currentLine.append(word);
						currentWidth = wordWidth;
					}

				// otherwise append the word to the line
				} else {
				currentLine.append(word);
					currentWidth += wordWidth;
				}
			}

			// add the last line if there's anything there
			if ( currentWidth > 0 ) {
				measuredLines.add(new LabelLineInfo(this, currentLine.toString(), currentWidth, wordHeight));
				updateBounds(currentWidth,wordHeight);
			}
		}
	}

	/**
	 * @return the maximum line width among the lines found in the input text.
	 */
	public double getMaxLineWidth() {
		return maxLineWidth;
	}

	/**
	 * @return the total combined height of all of the lines found in the input text.
	 */
	public double getTotalHeight() {
		return totalHeight;
	}

	/**
	 * @return a list of MeasuredLine objects created from the input text.
	 */
	public List<LabelLineInfo> getMeasuredLines() {
		return measuredLines;
	}
}
