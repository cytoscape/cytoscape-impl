package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cytoscape.view.model.VisualProperty;


/**
 * DOCUMENT ME!
 *
 * @author $author$
  */
public class YValueLegendPanel extends JPanel {
	private final static long serialVersionUID = 1202339877453677L;
	private VisualProperty<?> type;

	/**
	 * Creates a new IconPanel object.
	 *
	 * @param type DOCUMENT ME!
	 */
	public YValueLegendPanel(VisualProperty<?> type) {
		this.type = type;
		this.setPreferredSize(new Dimension());
	}

	@Override
	public void paintComponent(Graphics g) {
		clear(g);

		Graphics2D g2d = (Graphics2D) g;

		//this.setPreferredSize(new Dimension(strW + 6, 1));
		int panelHeight = this.getHeight() - 30;

		Polygon poly = new Polygon();
		int top = 10;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setStroke(new BasicStroke(1.0f));

		int center = (this.getWidth() / 2) + 4;

		poly.addPoint(center, top);
		poly.addPoint(center - 6, top + 15);
		poly.addPoint(center, top + 15);
		g.fillPolygon(poly);

		g2d.drawLine(center, top, center, panelHeight);
		g2d.setColor(Color.DARK_GRAY);
		g2d.setFont(new Font("SansSerif", Font.BOLD, 10));

		final String label = type.getDisplayName();
		final int width = SwingUtilities.computeStringWidth(g2d.getFontMetrics(), label);
		AffineTransform af = new AffineTransform();
		af.rotate(Math.PI + (Math.PI / 2));
		g2d.setTransform(af);

		g2d.setColor(Color.black);
		g2d.drawString(type.getDisplayName(), (-this.getHeight() / 2) - (width / 2),
		               (this.getWidth() / 2) + 5);
	}

	// super.paintComponent clears offscreen pixmap,
	// since we're using double buffering by default.
	protected void clear(Graphics g) {
		super.paintComponent(g);
	}
}
