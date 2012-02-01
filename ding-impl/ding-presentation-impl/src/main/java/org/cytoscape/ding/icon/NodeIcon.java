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
import java.awt.Stroke;
import java.awt.geom.AffineTransform;


/**
 * Icon for node shapes.
 *
 */
public class NodeIcon extends VisualPropertyIcon<Shape> {
	
	private final static long serialVersionUID = 1202339876280466L;
	
	private static final Stroke BASIC_STROKE = new BasicStroke(2.0f);

	private Shape newShape;
	private Graphics2D g2d;

	
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
		af.setToScale(xRatio, yRatio);
		newShape = af.createTransformedShape(value);
	}


	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {		
		g2d = (Graphics2D) g;

		// AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.translate(leftPad, 0);
		g2d.setColor(color);
		g2d.setStroke(BASIC_STROKE);
		g2d.draw(newShape);
		g2d.translate(-leftPad, 0);
	}

	@Override
	public NodeIcon clone() {
		final NodeIcon cloned = new NodeIcon(value, width, height, name);
		return cloned;
	}
}
