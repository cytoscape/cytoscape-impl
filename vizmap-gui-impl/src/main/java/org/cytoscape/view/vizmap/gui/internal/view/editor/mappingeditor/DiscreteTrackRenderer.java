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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.Range;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.Thumb;


public class DiscreteTrackRenderer<K, V> extends JComponent implements VizMapTrackRenderer {

	private final static long serialVersionUID = 1213748837182053L;

	private static final Dimension MIN_SIZE = new Dimension(200, 100);
	private static final int ICON_SIZE = 32;
	private static final int THUMB_WIDTH = 12;
	private static final int V_PADDING = 20;

	//private int smallIconSize = 20;
	private int trackHeight = 70;
	private int arrowBarYPosition = trackHeight + 50;
	
	private final String title;
	
	private V below;
	private V above;
	private VisualProperty<V> vp;

	private JXMultiThumbSlider<V> slider;

	private final EditorValueRangeTracer tracer;
	private final Map<V, Icon> iconMap;
	
	private final RenderingEngine<CyNetwork> engine;

	/**
	 * 
	 * @param mapping
	 * @param below
	 * @param above
	 * @param tracer
	 * @param engine
	 */
	public DiscreteTrackRenderer(final ContinuousMapping<K, V> mapping,
								 final V below,
								 final V above,
								 final EditorValueRangeTracer tracer,
								 final RenderingEngine<CyNetwork> engine) {
		if (mapping == null)
			throw new NullPointerException("Mapping is null.");
		if (tracer == null)
			throw new NullPointerException("Tracer is null.");
		if (engine == null)
			throw new NullPointerException("Rendering engine is null.");

		this.below = below;
		this.above = above;
		this.tracer = tracer;
		this.engine = engine;

		this.vp = mapping.getVisualProperty();
		final Range<V> rangeObject = vp.getRange();

		this.iconMap = new HashMap<V, Icon>();
		
		if (rangeObject.isDiscrete()) {
			final Set<V> values = ((DiscreteRange<V>) rangeObject).values();
			// create map of icons. Key is V value.
			
			for (V value : values)
				iconMap.put(value, engine.createIcon(vp, value, ICON_SIZE, ICON_SIZE));
		} else {
			// ?
		}
		
		this.title = mapping.getMappingColumnName();
		this.setBackground(BACKGROUND_COLOR);
		this.setMinimumSize(MIN_SIZE);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		paintComponent(g);
	}
	
	@Override
	protected void paintComponent(Graphics gfx) {
		// Turn AA on
		final Graphics2D g = (Graphics2D) gfx;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		trackHeight = slider.getHeight() - 100;
		arrowBarYPosition = trackHeight + 50;

		final int trackWidth = slider.getWidth() - THUMB_WIDTH;
		g.translate(THUMB_WIDTH / 2, 12);

		final double minValue = tracer.getMin(vp);
		final double maxValue = tracer.getMax(vp);
		final double valueRange = tracer.getRange(vp);
		final List<Thumb<V>> stops = slider.getModel().getSortedThumbs();
		final int numPoints = stops.size();

		// set up the data for the gradient
		final float[] fractions = new float[numPoints];
		final Object[] objectValues = new Object[numPoints];
		int i = 0;
		
		for (Thumb<V> thumb : stops) {
			objectValues[i] = thumb.getObject();
			fractions[i] = thumb.getPosition();
			i++;
		}

		// Draw arrow
		g.setStroke(STROKE1);
		g.setColor(BORDER_COLOR);
		g.drawLine(0, arrowBarYPosition, trackWidth, arrowBarYPosition);

		final Polygon arrow = new Polygon();
		arrow.addPoint(trackWidth, arrowBarYPosition);
		arrow.addPoint(trackWidth - 20, arrowBarYPosition - 8);
		arrow.addPoint(trackWidth - 20, arrowBarYPosition);
		g.fill(arrow);

		g.setColor(LABEL_COLOR);
		g.drawLine(0, arrowBarYPosition, 15, arrowBarYPosition - 30);
		g.drawLine(15, arrowBarYPosition - 30, 25, arrowBarYPosition - 30);

		g.setFont(SMALL_FONT);
		g.drawString("Min=" + minValue, 28, arrowBarYPosition - 25);

		g.drawLine(trackWidth, arrowBarYPosition, trackWidth - 15, arrowBarYPosition + 30);
		g.drawLine(trackWidth - 15, arrowBarYPosition + 30, trackWidth - 25, arrowBarYPosition + 30);

		final String maxStr = "Max=" + maxValue;
		int strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), maxStr);
		g.drawString(maxStr, trackWidth - strWidth - 26, arrowBarYPosition + 35);

		g.setColor(LABEL_COLOR);
		strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), title);
		g.drawString(title, (trackWidth / 2) - (strWidth / 2), arrowBarYPosition + 35);

		if (numPoints == 0) {
			g.setColor(BORDER_COLOR);
			g.setStroke(new BasicStroke(1.5f));
			g.drawRect(0, 5, trackWidth, trackHeight);

			return;
		}
		
		g.setStroke(STROKE1);

		// Fill background
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 5, trackWidth, trackHeight);

		final Point2D p1 = new Point2D.Float(0, 5);
		final Point2D p2 = new Point2D.Float(0, 5);

		int iconLocX;
		int iconLocY;

		// Draw Icons
		for (i = 0; i < stops.size(); i++) {
			int newX = (int) (trackWidth * (fractions[i] / 100));

			p2.setLocation(newX, 5);
			g.setColor(LABEL_COLOR);
			g.setStroke(STROKE1);

			g.drawLine(newX, 5, newX, trackHeight + 4);

			g.setColor(LABEL_COLOR);
			g.setFont(SMALL_FONT);

			final Float curPositionValue = ((Number) (((fractions[i] / 100) * valueRange) + minValue)).floatValue();
			final String valueString = String.format("%.5f", curPositionValue);

			int flipLimit = 90;
			int borderVal = trackWidth - newX;

			if (((i % 2) == 0) && (flipLimit < borderVal)) {
				g.drawLine(newX, arrowBarYPosition, newX + 20, arrowBarYPosition - 15);
				g.drawLine(newX + 20, arrowBarYPosition - 15, newX + 30, arrowBarYPosition - 15);
				g.setColor(LABEL_COLOR);
				g.drawString(valueString, newX + 33, arrowBarYPosition - 11);
			} else if (((i % 2) == 1) && (flipLimit < borderVal)) {
				g.drawLine(newX, arrowBarYPosition, newX + 20, arrowBarYPosition + 15);
				g.drawLine(newX + 20, arrowBarYPosition + 15, newX + 30, arrowBarYPosition + 15);
				g.setColor(LABEL_COLOR);
				g.drawString(valueString, newX + 33, arrowBarYPosition + 19);
			} else if (((i % 2) == 0) && (flipLimit >= borderVal)) {
				g.drawLine(newX, arrowBarYPosition, newX - 20, arrowBarYPosition - 15);
				g.drawLine(newX - 20, arrowBarYPosition - 15, newX - 30, arrowBarYPosition - 15);
				g.setColor(LABEL_COLOR);
				g.drawString(valueString, newX - 90, arrowBarYPosition - 11);
			} else {
				g.drawLine(newX, arrowBarYPosition, newX - 20, arrowBarYPosition + 15);
				g.drawLine(newX - 20, arrowBarYPosition + 15, newX - 30, arrowBarYPosition + 15);
				g.setColor(LABEL_COLOR);
				g.drawString(valueString, newX - 90, arrowBarYPosition + 19);
			}

			g.setColor(LABEL_COLOR);
			g.fillOval(newX - 3, arrowBarYPosition - 3, 6, 6);

			iconLocX = (int) (p2.getX() - ((p2.getX() - p1.getX()) / 2 + ICON_SIZE / 2));
			iconLocY = (int) (trackHeight / 2 - ICON_SIZE / 2 + p2.getY());

			if (i == 0)
				drawIcon(below, g, iconLocX, iconLocY);
			else
				drawIcon((V) objectValues[i], g, iconLocX, iconLocY);

			p1.setLocation(p2);
		}

		// Draw last region (above region)
		p2.setLocation(trackWidth, 5);

		iconLocX = (int) (p2.getX() - ((p2.getX() - p1.getX()) / 2 + ICON_SIZE / 2));
		iconLocY = (int) (trackHeight / 2 - ICON_SIZE / 2 + p2.getY());

		drawIcon(above, g, iconLocX, iconLocY);
		
		/*
		 * Finally, draw border line (rectangle)
		 */
		g.setColor(BORDER_COLOR);
		g.setStroke(new BasicStroke(1.5f));
		g.drawRect(0, 5, trackWidth, trackHeight);

		g.translate(-THUMB_WIDTH / 2, -12);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JComponent getRendererComponent(JXMultiThumbSlider slider) {
		this.slider = slider;
		return this;
	}

	protected List<V> getRanges() {
		List<V> range = new ArrayList<V>();
		return range;
	}

	public String getToolTipForCurrentLocation(int x, int y) {
		int oldX = 0;
		int newX;

		final List<Thumb<V>> stops = slider.getModel().getSortedThumbs();

		int i = 1;

		for (Thumb<V> thumb : stops) {
			newX = (int) (slider.getWidth() * (thumb.getPosition() / 100));

			if ((oldX <= x) && (x <= newX) && (V_PADDING < y) && (y < (V_PADDING + trackHeight)))
				return "This is region " + i;

			i++;
			oldX = newX + 1;
		}

		if ((oldX <= x) && (x <= slider.getWidth()) && (V_PADDING < y) && (y < (V_PADDING + trackHeight)))
			return "Last Area: " + oldX + " - " + slider.getWidth() + " (x, y) = " + x + ", " + y;

		return null;
	}


	/*
	 * Get region id.
	 * 
	 * +------------------------------------------- | 0 | 1 | 2 | ...
	 * +-------------------------------------------
	 */
	protected int getRangeID(int x, int y) {
		int oldX = 0;
		int newX;

		final List<Thumb<V>> stops = slider.getModel().getSortedThumbs();
		Thumb<V> thumb;
		int i;

		for (i = 0; i < stops.size(); i++) {
			thumb = stops.get(i);
			newX = (int) (slider.getWidth() * (thumb.getPosition() / 100));

			if ((oldX <= x) && (x <= newX) && (V_PADDING < y) && (y < (V_PADDING + trackHeight)))
				return i;

			oldX = newX + 1;
		}

		if ((oldX <= x) && (x <= slider.getWidth()) && (V_PADDING < y) && (y < (V_PADDING + trackHeight)))
			return i;

		// Invalid range
		return -1;
	}

	/**
	 * Draw icon object based on the given data type.
	 */
	private void drawIcon(final V key, final Graphics2D g, final int x, final int y) {
		if (key == null)
			return;
		
		Icon icon = iconMap.get(key);
		
		if (icon == null)
			icon = engine.createIcon(vp, key, ICON_SIZE, ICON_SIZE);
		
		g.setColor(LABEL_COLOR);
		icon.paintIcon(this, g, x, y);	
	}

	public ImageIcon getTrackGraphicIcon(int iconWidth, int iconHeight) {
		return createIcon(iconWidth, iconHeight, false);
	}

	public ImageIcon getLegend(int iconWidth, int iconHeight) {
		return createIcon(iconWidth, iconHeight, true);
	}

	private ImageIcon createIcon(int iconWidth, int iconHeight, final boolean detail) {
		if (slider == null)
			return null;

		final BufferedImage bi = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = bi.createGraphics();

		// Turn AA on.
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Fill background
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, iconWidth, iconHeight);

		double minValue = tracer.getMin(vp);
		double maxValue = tracer.getMax(vp);
		double valueRange = tracer.getRange(vp);

		int track_width = iconWidth;
		int trackHeight;
		
		if (detail) {
			trackHeight = iconHeight - 30;
			//smallIconSize = (int) (trackHeight * 0.5);
		} else {
			trackHeight = iconHeight - 8;
		}

		// get the list of tumbs
		List<Thumb<V>> stops = slider.getModel().getSortedThumbs();
		int numPoints = stops.size();

		// set up the data for the gradient
		float[] fractions = new float[numPoints];
		Object[] objectValues = new Object[numPoints];

		/*
		 * Find min, max, and ranges
		 */
		int i = 0;

		for (Thumb<V> thumb : stops) {
			objectValues[i] = thumb.getObject();
			fractions[i] = thumb.getPosition();
			i++;
		}

		/*
		 * If no points, just draw empty box.
		 */
		if (numPoints == 0) {
			g.setColor(BORDER_COLOR);
			g.setStroke(new BasicStroke(1.0f));
			g.drawRect(0, 0, track_width - 3, trackHeight);

			return new ImageIcon(bi);
		}

		int newX;

		Point2D p1 = new Point2D.Float(0, 5);
		Point2D p2 = new Point2D.Float(0, 5);

		int iconLocX;
		int iconLocY;

		/*
		 * Draw separators and icons
		 */
		for (i = 0; i < stops.size(); i++) {
			newX = (int) (track_width * (fractions[i] / 100));

			p2.setLocation(newX, 0);
			iconLocX = newX - (((newX - (int) p1.getX()) / 2) + (ICON_SIZE / 2));
			iconLocY = ((trackHeight) / 2) - (ICON_SIZE / 2);
			
			if (i == 0)
				drawIcon(below, g, iconLocX, iconLocY);
			else
				drawIcon((V) objectValues[i], g, iconLocX, iconLocY);

			g.setColor(BORDER_COLOR);
			g.setStroke(STROKE1);
			g.drawLine(newX, 0, newX, trackHeight);

			p1.setLocation(p2);
		}

		/*
		 * Draw last region (above region)
		 */
		p2.setLocation(track_width, 0);

		iconLocX = track_width - (((track_width - (int) p1.getX()) / 2) + (ICON_SIZE / 2));
		iconLocY = ((trackHeight) / 2) - (ICON_SIZE / 2);
		drawIcon(above, g, iconLocX, iconLocY);

		/*
		 * Finally, draw border line (rectangle)
		 */
		g.setColor(BORDER_COLOR);
		g.setStroke(new BasicStroke(1.0f));
		g.drawRect(0, 0, track_width - 3, trackHeight);

		g.setFont(ICON_FONT);

		final String minStr = String.format("%.2f", minValue);
		final String maxStr = String.format("%.2f", maxValue);
		int strWidth;
		g.setColor(LABEL_COLOR);
		
		if (detail) {
			String fNum;

			for (int j = 0; j < fractions.length; j++) {
				fNum = String.format("%.2f", ((fractions[j] / 100) * valueRange) + minValue);
				strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), fNum);
				g.drawString(fNum, (fractions[j] / 100) * iconWidth - strWidth / 2, iconHeight - 20);
			}

			g.drawString(minStr, 0, iconHeight);
			strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), maxStr);
			g.drawString(maxStr, iconWidth - strWidth - 2, iconHeight);

			g.setFont(TITLE_FONT);

			final int titleWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), title);
			g.setColor(LABEL_COLOR);
			g.drawString(title, (iconWidth / 2) - (titleWidth / 2), iconHeight - 5);
			Polygon p = new Polygon();
			p.addPoint(iconWidth, iconHeight - 9);
			p.addPoint(iconWidth - 15, iconHeight - 15);
			p.addPoint(iconWidth - 15, iconHeight - 9);
			g.fillPolygon(p);
			g.drawLine(0, iconHeight - 9, (iconWidth / 2) - (titleWidth / 2) - 3, iconHeight - 9);
			g.drawLine((iconWidth / 2) + (titleWidth / 2) + 3, iconHeight - 9, iconWidth, iconHeight - 9);
		} else {
			g.drawString(minStr, 0, iconHeight);
			strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), maxStr);
			g.drawString(maxStr, iconWidth - strWidth - 2, iconHeight);
		}

		return new ImageIcon(bi);
	}

	public Double getSelectedThumbValue() {
		final double minValue = tracer.getMin(vp);
		final double valueRange = tracer.getRange(vp);

		final float position = slider.getModel().getThumbAt(slider.getSelectedIndex()).getPosition();

		return ((position / 100) * valueRange) + minValue;
	}
}
