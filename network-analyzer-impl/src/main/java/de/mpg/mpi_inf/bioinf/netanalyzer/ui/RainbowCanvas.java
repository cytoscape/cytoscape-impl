package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Rainbow control displaying the gradual pairwise transitions between three colors.
 * 
 * @author Yassen Assenov
 * @author Nadezhda Doncheva
 */
public class RainbowCanvas extends Canvas {

	/**
	 * Initializes a new instance of <code>RainbowCanvas</code> with a breakpoint of <code>0.5</code>.
	 * 
	 * @param aColors
	 *            Three-dimensional array of colors to be used.
	 * 
	 * @see #RainbowCanvas(Color[], double)
	 */
	public RainbowCanvas(Color[] aColors) {
		this(aColors, 0.5);
	}

	/**
	 * Initializes a new instance of <code>RainbowCanvas</code>.
	 * 
	 * @param aColors
	 *            Three-dimensional array of colors to be used.
	 * @param aBreakpoint
	 *            A relative location of the second color in the rainbow. This must be a number between 0 and
	 *            1.
	 * 
	 * @throws IllegalArgumentException
	 *             If at least one of the following does <b>not</b> hold:
	 *             <ul>
	 *             <li><code>aBreakpoint</code> is a value in the range <code>[0, 1]</code>.</li>
	 *             <li><code>aColors</code> contains exactly three non-<code>null</code> elements.</li>
	 *             </ul>
	 * @throws NullPointerException
	 *             If <code>aColors</code> is <code>null</code>.
	 */
	public RainbowCanvas(Color[] aColors, double aBreakpoint) {
		if (aColors.length != 3 || aBreakpoint < 0 || aBreakpoint > 1) {
			throw new IllegalArgumentException();
		}
		if (aColors[0] == null || aColors[1] == null || aColors[2] == null) {
			throw new IllegalArgumentException();
		}
		colors = aColors;
		breakpoint = aBreakpoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Canvas#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		final Dimension size = getSize();

		final int breakX = (int) Math.round(breakpoint * size.width);
		final int rightX = size.width - breakX;
		int rc, gc, bc;
		for (int x = 0; x < size.width; ++x) {
			if (x < breakX) {
				rc = ((breakX - x) * colors[0].getRed() + x * colors[1].getRed()) / breakX;
				gc = ((breakX - x) * colors[0].getGreen() + x * colors[1].getGreen()) / breakX;
				bc = ((breakX - x) * colors[0].getBlue() + x * colors[1].getBlue()) / breakX;
			} else {
				rc = ((size.width - x) * colors[1].getRed() + (x - breakX) * colors[2].getRed()) / rightX;
				gc = ((size.width - x) * colors[1].getGreen() + (x - breakX) * colors[2].getGreen()) / rightX;
				bc = ((size.width - x) * colors[1].getBlue() + (x - breakX) * colors[2].getBlue()) / rightX;
			}
			g.setColor(new Color(rc, gc, bc));
			g.drawLine(x, 0, x, size.height - 1);
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1514313656921206969L;

	/**
	 * Colors to be used in the rainbow.
	 */
	private Color[] colors;

	/**
	 * Relative position of the second color in the rainbow.
	 */
	private double breakpoint;
}
