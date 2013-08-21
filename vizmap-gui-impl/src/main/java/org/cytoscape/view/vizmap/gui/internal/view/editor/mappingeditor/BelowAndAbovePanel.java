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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;

final class BelowAndAbovePanel extends JPanel {

	private final static long serialVersionUID = 1202339876961477L;

	private static final Stroke OUTLINE_STROKE = new BasicStroke(1.0f);

	private final VisualProperty<?> vp;
	private final boolean isBelow;
	private Color boxColor;
	
	private final ContinuousMapping<?, ?> mapping;
	private final ContinuousMappingEditorPanel parentPanel;

	BelowAndAbovePanel(final Color color, final boolean below, final ContinuousMapping<?, ?> mapping,
			final ContinuousMappingEditorPanel<?, ?> parentPanel) {
		this.boxColor = color;
		this.isBelow = below;
		this.mapping = mapping;
		this.vp = mapping.getVisualProperty();
		this.parentPanel = parentPanel;

		if (below)
			this.setToolTipText("Double-click triangle to set below color...");
		else
			this.setToolTipText("Double-click triangle to set above color...");

		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				processDoubleClick(e);
			}
		});
	}

	private void processDoubleClick(MouseEvent e) {
		if (e.getClickCount() == 2) {
			Object newValue = null;

			if (Paint.class.isAssignableFrom(vp.getRange().getType()) == false)
				return;

			newValue = ((GradientEditorPanel)parentPanel).colorEditor.showEditor(this, boxColor);
			
			if (newValue == null)
				return;

			setColor((Color) newValue);

			BoundaryRangeValues brv = null;
			BoundaryRangeValues original;
			ContinuousMappingPoint originalPoint;
			
			final SortedMap<Double, ContinuousMappingPoint> sortedPoints = new TreeMap<Double, ContinuousMappingPoint>();
			
			for (final Object point : mapping.getAllPoints()) {
				ContinuousMappingPoint p = (ContinuousMappingPoint) point;
				final Number val =(Number) p.getValue();
				sortedPoints.put(val.doubleValue(), p);
			}
			
			if (mapping.getPointCount() != 0) {
				if (isBelow) {
					originalPoint = sortedPoints.get(sortedPoints.firstKey());
					original = originalPoint.getRange();
					brv = new BoundaryRangeValues(newValue, original.equalValue, original.greaterValue);
				} else {
					originalPoint = sortedPoints.get(sortedPoints.lastKey());
					original = originalPoint.getRange();
					brv = new BoundaryRangeValues(original.lesserValue, original.equalValue, newValue);					
				}
				originalPoint.setRange(brv);
			}
			
			if (isBelow)
				parentPanel.below = (Color) newValue;
			else
				parentPanel.above = (Color) newValue;

			((GradientEditorPanel) parentPanel).initSlider();
			((GradientEditorPanel) parentPanel).updateView();
		}
	}

	public void setColor(final Color newColor) {
		this.boxColor = newColor;
		this.repaint();
		this.getParent().repaint();
	}

	/**
	 * Draw custom panel component
	 */
	@Override
	public void paintComponent(final Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		final Polygon poly = new Polygon();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setStroke(OUTLINE_STROKE);
		g2d.setColor(boxColor);

		if (isBelow) {
			poly.addPoint(18, 0);
			poly.addPoint(18, 20);
			poly.addPoint(0, 10);
		} else {
			poly.addPoint(0, 0);
			poly.addPoint(0, 20);
			poly.addPoint(18, 10);
		}

		g2d.fillPolygon(poly);
		g2d.setColor(Color.DARK_GRAY);
		g2d.draw(poly);
	}
}
