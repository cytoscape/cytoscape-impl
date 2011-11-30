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
import java.awt.geom.Rectangle2D;


/**
 * Icon for arrow shape.
 *
 * @version 0.5
 * @since Cytoscape 2.5
 * @author kono
 */
public class ArrowIcon extends VisualPropertyIcon<Shape> {
	private final static long serialVersionUID = 1202339877462891L;
	private static final Stroke EDGE_STROKE = new BasicStroke(6.0f, BasicStroke.CAP_SQUARE,
	                                                          BasicStroke.JOIN_MITER);
	private static final Stroke EDGE_STROKE_SMALL = new BasicStroke(4.0f, BasicStroke.CAP_SQUARE,
	                                                                BasicStroke.JOIN_MITER);
	protected Graphics2D g2d;
	private static final int DEF_L_PAD = 15;


	/**
	 * Creates a new ArrowIcon object.
	 *
	 * @param shape DOCUMENT ME!
	 * @param width DOCUMENT ME!
	 * @param height DOCUMENT ME!
	 * @param name DOCUMENT ME!
	 * @param color DOCUMENT ME!
	 */
	public ArrowIcon(Shape shape, int width, int height, String name) {
		super(shape, width, height, name);
	}

	/**
	 * Draw icon using Java2D.
	 *
	 * @param c DOCUMENT ME!
	 * @param g DOCUMENT ME!
	 * @param x DOCUMENT ME!
	 * @param y DOCUMENT ME!
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g2d = (Graphics2D) g;

		// Turn AA on
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(color);

		g2d.translate(leftPad, bottomPad);

		/*
		 * If shape is not defined, treat as no-head.
		 */
		if (value == null) {
			if ((width < 20) || (height < 20)) {
				g2d.translate(-leftPad, -bottomPad);
				g2d.setStroke(EDGE_STROKE_SMALL);
				g2d.drawLine(3, c.getHeight()/2,
			             width/2 +10, c.getHeight()/2);
				return;
			} else {
//				g2d.setStroke(EDGE_STROKE);
//				g2d.drawLine(DEF_L_PAD, (height + 20) / 2,
//			             (int) (c.getWidth()*0.3), (height + 20) / 2);
			}
			g2d.translate(-leftPad, -bottomPad);
			return;
		}

		final AffineTransform af = new AffineTransform();

		g2d.setStroke(new BasicStroke(2.0f));

		final Rectangle2D bound = value.getBounds2D();
		final double minx = bound.getMinX();
		final double miny = bound.getMinY();

		Shape newShape = value;

		/*
		 * Adjust position if it is NOT in first quadrant.
		 */
		if (minx < 0) {
			af.setToTranslation(Math.abs(minx), 0);
			newShape = af.createTransformedShape(newShape);
		}

		if (miny < 0) {
			af.setToTranslation(0, Math.abs(miny));
			newShape = af.createTransformedShape(newShape);
		}

		final double shapeWidth = newShape.getBounds2D().getWidth();
		final double shapeHeight = newShape.getBounds2D().getHeight();

		final double originalXYRatio = shapeWidth / shapeHeight;

		final double xRatio = (width / 3) / shapeWidth;
		final double yRatio = height / shapeHeight;
		af.setToScale(xRatio * originalXYRatio, yRatio);
		newShape = af.createTransformedShape(newShape);

		af.setToTranslation((width * 0.8) - newShape.getBounds2D().getCenterX(),
		                    ((height + 20) / 2) - newShape.getBounds2D().getCenterY());
		newShape = af.createTransformedShape(newShape);

		g2d.fill(newShape);

		/*
		 * Finally, draw an edge (line) to the arrow head.
		 */
		if ((width < 20) || (height < 20)) {
//			g2d.setStroke(EDGE_STROKE_SMALL);
//			
//			
//			
//			g2d.drawLine(3, c.getHeight()/2,
//		             width/2, c.getHeight()/2);
		} else {
			g2d.setStroke(EDGE_STROKE);
			g2d.drawLine(DEF_L_PAD, (height + 20) / 2,
		             (int) (newShape.getBounds2D().getCenterX()) - 2, (height + 20) / 2);
		}
		
		g2d.translate(-leftPad, -bottomPad);
		
		if ((width < 20) || (height < 20)) {
			g2d.setStroke(EDGE_STROKE_SMALL);
			g2d.drawLine(3, c.getHeight()/2,
		             width/2 +10, c.getHeight()/2);
		}
	}
}
