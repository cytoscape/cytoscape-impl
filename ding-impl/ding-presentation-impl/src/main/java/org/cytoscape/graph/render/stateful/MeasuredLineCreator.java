/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.graph.render.stateful;


import java.awt.Font;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

import java.util.List;
import java.util.ArrayList;

import org.cytoscape.graph.render.immed.GraphGraphics;


/**
 * A class that takes a String and a number of rendering parameters and then 
 * splits the text into MeasuredLines based on newline characters and whether 
 * line length is otherwise greater than the specified label width limit.
 */
class MeasuredLineCreator {
	private double maxLineWidth;
	private double totalHeight;

	private final double labelWidth;
	private final String rawLine;
	private final String[] rawLines;
	private final FontRenderContext frc;
	private final Font font; 
	private final double fontScaleFactor;
	private final boolean textAsShape;
	private final List<MeasuredLine> measuredLines;
	
	public MeasuredLineCreator(final String rawLine, final Font font, 
	                            final FontRenderContext frc, final double fontScaleFactor, 
	                            final boolean textAsShape, final double labelWidth) {
		this.rawLine = rawLine;
		this.font = font;
		this.frc = frc;
		this.fontScaleFactor = fontScaleFactor;
		this.textAsShape = textAsShape;
		this.labelWidth = labelWidth;
		this.rawLines = rawLine.split("\n"); 
		this.measuredLines = new ArrayList<MeasuredLine>();

		calculateRawBounds(); 
		createMeasuredLines();
	}

	/** 
	 * Does a first pass at calculating the bounds of all lines. For short strings
	 * (i.e. the norm) this is sufficient calculation.
	 */
	private void calculateRawBounds() {
		maxLineWidth = 0;
		totalHeight = 0;
		for ( String line : rawLines ) { 
			final Rectangle2D bounds = calcBounds(line);
			updateBounds(bounds.getWidth()*fontScaleFactor,bounds.getHeight()*fontScaleFactor);
		}
	}

	/**
	 * Simply updates the maxLineWidth and totalHeight.  We do it in a separate method
	 * to make sure we do it consistently.
	 */
	private void updateBounds(final double newWidth, final double newHeight ) {
		maxLineWidth = Math.max( maxLineWidth, newWidth );
		totalHeight += newHeight;
	}

	/**
	 * Calculates the bounds of a single string.
	 */
	private Rectangle2D calcBounds(final String s) {
		final Rectangle2D bounds;

		if (textAsShape) {
			final char[] charBuff = s.toCharArray(); 
			final GlyphVector glyphV = font.layoutGlyphVector( frc, charBuff, 0,
			                                charBuff.length, Font.LAYOUT_NO_LIMIT_CONTEXT);
			bounds = glyphV.getLogicalBounds();
		} else {
			bounds = font.getStringBounds(s, frc);
		}

		return bounds;
	}

	/**
	 * Splits the raw lines according to how many lines are present and if any of 
	 * the lines are too long.  Recalculates the maxLineWidth and totalHeight based
	 * on the new lines created.
	 */
	private void createMeasuredLines() {

		// There's only one line and it's short, i.e. what usually happens.
		if ( rawLines.length == 1 && labelWidth > maxLineWidth ) {
			measuredLines.add(new MeasuredLine(rawLines[0],maxLineWidth,totalHeight));
			return;
		}

		// There are multiple and/or longer-than-allowed lines.   
		// Process each of them. Also update overall widths and heights 
		// as those may change. 
		totalHeight = 0;
		maxLineWidth = 0;
	
		for ( String line : rawLines ) {
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
				wordWidth = bounds.getWidth()*fontScaleFactor;
				wordHeight = bounds.getHeight()*fontScaleFactor;

				// If the current line width plus the new word
				// width is >= than the label width save the line
				if (currentWidth + wordWidth >= labelWidth) {
					// only write the string if something is there
					if ( currentWidth > 0 ) {
						measuredLines.add( new MeasuredLine(currentLine.toString(),
					   	                                    currentWidth,wordHeight) );
						updateBounds(currentWidth,wordHeight);
						currentLine.delete(0,currentLine.length());
					}

					// if the word itself is >= the label width,
					// make the word itself a new line
					if ( wordWidth >= labelWidth ) {
						measuredLines.add(new MeasuredLine(word,wordWidth,wordHeight) );
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
				measuredLines.add( new MeasuredLine(currentLine.toString(),
			                                        currentWidth, wordHeight) );
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
	public List<MeasuredLine> getMeasuredLines() {
		return measuredLines;
	}
}
