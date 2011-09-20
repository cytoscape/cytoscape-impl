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
package org.cytoscape.ding.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import javax.swing.SwingUtilities;

/**
 * Icon generator for Stroke objects.
 *
 */
public class StrokeIcon extends VisualPropertyIcon<Stroke> {
	private final static long serialVersionUID = 1202339875918391L;

	private Graphics2D g2d;

	// If not null, this message will be shown over the icon.
	private String superimposedText = null;
	private Font textFont = null;
	private Color textColor = null;

	/**
	 * Creates a new LineTypeIcon object.
	 *
	 * @param stroke DOCUMENT ME!
	 * @param width DOCUMENT ME!
	 * @param height DOCUMENT ME!
	 * @param name DOCUMENT ME!
	 * @param color DOCUMENT ME!
	 */
	public StrokeIcon(final Stroke stroke, int width, int height, String name) {
		super(stroke, width, height, name);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param c DOCUMENT ME!
	 * @param g DOCUMENT ME!
	 * @param x DOCUMENT ME!
	 * @param y DOCUMENT ME!
	 */
	@Override public void paintIcon(Component c, Graphics g, int x, int y) {
		g2d = (Graphics2D) g;
		g2d.setColor(color);
		// AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.translate(leftPad, bottomPad);
		g2d.setStroke(value);
		g2d.draw(new Line2D.Double(20, (height + 20) / 2, width*2, (height + 20) / 2));

		/*
		 * Superimpose text if text object is not empty.
		 */
		if (superimposedText != null) {

			if (textColor == null) {
				g2d.setColor(Color.DARK_GRAY);
			} else
				g2d.setColor(textColor);

			if (textFont == null) {
				g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
			} else {
				g2d.setFont(textFont);
			}

			g2d.drawString(superimposedText, 20, (height + 40) / 2);
		}

		g2d.translate(-leftPad, -bottomPad);
		g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param text DOCUMENT ME!
	 */
	public void setText(final String text) {
		this.superimposedText = text;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param font DOCUMENT ME!
	 */
	public void setTextFont(final Font font) {
		this.textFont = font;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param color DOCUMENT ME!
	 */
	public void setTextColor(final Color color) {
		this.textColor = color;
	}
}
