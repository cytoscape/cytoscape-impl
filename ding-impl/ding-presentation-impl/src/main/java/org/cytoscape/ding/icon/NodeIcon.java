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

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;


/**
 * Icon for node shapes.
 *
 */
public class NodeIcon extends VisualPropertyIcon<Shape> {
	
	private final static long serialVersionUID = 1202339876280466L;

	private Shape newShape;
	
	private Graphics2D g2d;


	/**
	 * Creates a new NodeShapeIcon object.
	 *
	 * @param shape DOCUMENT ME!
	 * @param width DOCUMENT ME!
	 * @param height DOCUMENT ME!
	 * @param name DOCUMENT ME!
	 * @param color DOCUMENT ME!
	 */
	public NodeIcon(Shape shape, int width, int height, String name) {
		super(shape, width, height, name);

		adjustShape();
	}


	private void adjustShape() {
		final double shapeWidth = value.getBounds2D().getWidth();
		final double shapeHeight = value.getBounds2D().getHeight();

		final double xRatio = width / shapeWidth;
		final double yRatio = height / shapeHeight;

		final AffineTransform af = new AffineTransform();

		final Rectangle2D bound = value.getBounds2D();
		final double minx = bound.getMinX();
		final double miny = bound.getMinY();

		if (minx < 0) {
			af.setToTranslation(Math.abs(minx), 0);
			newShape = af.createTransformedShape(value);
		}

		if (miny < 0) {
			af.setToTranslation(0, Math.abs(miny));
			newShape = af.createTransformedShape(value);
		}

		af.setToScale(xRatio, yRatio);
		newShape = af.createTransformedShape(value);
	}

	/**
	 * Draw icon using Java2D.
	 *
	 * @param c DOCUMENT ME!
	 * @param g DOCUMENT ME!
	 * @param x DOCUMENT ME!
	 * @param y DOCUMENT ME!
	 */
	@Override public void paintIcon(Component c, Graphics g, int x, int y) {		
		g2d = (Graphics2D) g;

		final AffineTransform af = new AffineTransform();

		// AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.translate(0, bottomPad);

		newShape = value;

		af.setToTranslation(leftPad, (c.getHeight() - newShape.getBounds2D().getHeight()) / 2);
		newShape = af.createTransformedShape(newShape);

		g2d.setColor(color);
		g2d.setStroke(new BasicStroke(2.0f));
		g2d.draw(newShape);

		g2d.translate(0, -bottomPad);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public NodeIcon clone() {
		final NodeIcon cloned = new NodeIcon(value, width, height, name);

		return cloned;
	}
}
