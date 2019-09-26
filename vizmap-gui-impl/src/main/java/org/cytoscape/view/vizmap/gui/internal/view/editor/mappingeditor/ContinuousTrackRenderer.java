package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.NumberConverter;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.ThumbDataEvent;
import org.jdesktop.swingx.multislider.ThumbDataListener;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

/**
 * Track renderer for Continuous mapping (Number-to-Number mapping)
 */
public class ContinuousTrackRenderer<K extends Number, V extends Number>
		extends JComponent implements VizMapTrackRenderer {
	
	private final static long serialVersionUID = 1202339877100033L;
	
	private final Color VALUE_AREA_COLOR;
	private final Color TRACK_COLOR;
	
	private static final Float UPPER_LIMIT = 2000f;
	
	private static final int THUMB_WIDTH = 12;
	private static final int LEFT_SPACE = 50;
	
	private Map<Integer, Double> valueMap;

	/*
	 * Define Colors used in this diagram.
	 */
	private int trackHeight = 120;
	private int arrowBarPosition = trackHeight + 50;

	/*
	 * Min and Max for the Y-Axis.
	 */
	private float min;
	private float max;
	private boolean clickFlag;
	private boolean dragFlag;
	private Point curPoint;
	private JXMultiThumbSlider<V> slider;
	private CMouseListener listener;
	private Map<Integer, Point> verticesList;
	private int selectedIdx = -1;
	// private Point dragOrigin;
	
	private String title;
	private V below;
	private V above;
	private List<V> values = new ArrayList<>();
	private Polygon valueArea = new Polygon();
	private Point belowSquare;
	private Point aboveSquare;

	private final EditorValueRangeTracer tracer;
	private final VisualStyle style;
	
	private final VisualProperty<V> vp;
	private final ContinuousMapping<K, V> cMapping;
	
	private final Class<V> vpValueType;
	private final Class<K> columnType;

	private final ServicesUtil servicesUtil;
	
	public ContinuousTrackRenderer(
			VisualStyle style,
			ContinuousMapping<K, V> mapping,
			V below,
			V above,
			EditorValueRangeTracer tracer,
			ServicesUtil servicesUtil
	) {
		if (mapping == null)
			throw new IllegalArgumentException("'mapping' must not be null.");
		if (tracer == null)
			throw new IllegalArgumentException("'tracer' must not be null.");
		
		this.below = below;
		this.above = above;
		this.vp = mapping.getVisualProperty();
		this.tracer = tracer;
		this.style = style;
		this.servicesUtil = servicesUtil;

		cMapping = mapping;
		this.columnType = mapping.getMappingColumnType();
		this.vpValueType = mapping.getVisualProperty().getRange().getType();
		
		title = cMapping.getMappingColumnName();
		
		Color c = UIManager.getColor("CyColor.complement(+2)");
		VALUE_AREA_COLOR = new Color(c.getRed(), c.getGreen(), c.getBlue(), 60);
		TRACK_COLOR = UIManager.getColor("CyColor.complement(-1)");
		
		 //TODO: where should I put this property value?
//		 Object propStr =
//		 CytoscapeInit.getProperties().getProperty("vizmapper.cntMapperUpperLimit");
//		
//		 if (propStr != null) {
//		 try {
//		 val = Float.parseFloat(propStr.toString());
//		 } catch (NumberFormatException e) {
//		 val = 2000f;
//		 }
//		
//		 UPPER_LIMIT = val;
//		 } else {
//		 UPPER_LIMIT = 2000f;
//		 }
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		paintComponent(g);
	}

	protected void removeSquare(Integer index) {
		verticesList.remove(index);
	}

	@Override
	protected void paintComponent(Graphics gfx) {
		trackHeight = slider.getHeight() - 100;
		arrowBarPosition = trackHeight + 50;

		// AA on
		Graphics2D g = (Graphics2D) gfx;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double minValue = tracer.getMin(vp);
		double maxValue = tracer.getMax(vp);

		int thumb_width = 12;
		int track_width = slider.getWidth() - thumb_width;
		g.translate(thumb_width / 2, 12);

		// get the list of tumbs
		List<Thumb<V>> stops = slider.getModel().getSortedThumbs();
		int numPoints = stops.size();
		
		// set up the data for the gradient
		float[] fractions = new float[numPoints];
		final Double[] doubleValues = new Double[numPoints];
		int i = 0;

		values.clear();
		values.add(below);
		values.add(above);

		for (Thumb<V> thumb : stops) {
			doubleValues[i] = thumb.getObject().doubleValue();
			fractions[i] = thumb.getPosition();
			values.add(thumb.getObject());
			i++;
		}

		for (V val : values) {
			if (min >= val.floatValue())
				min = val.floatValue();

			if (max <= val.floatValue())
				max = val.floatValue();
		}

		// Draw arrow bar
		g.setStroke(new BasicStroke(1.0f));
		g.setColor(LABEL_COLOR);
		g.drawLine(0, arrowBarPosition, track_width, arrowBarPosition);

		Polygon arrow = new Polygon();
		arrow.addPoint(track_width, arrowBarPosition);
		arrow.addPoint(track_width - 20, arrowBarPosition - 8);
		arrow.addPoint(track_width - 20, arrowBarPosition);
		g.fill(arrow);

		g.setColor(DISABLED_LABEL_COLOR);
		g.drawLine(0, arrowBarPosition, 15, arrowBarPosition - 30);
		g.drawLine(15, arrowBarPosition - 30, 25, arrowBarPosition - 30);

		g.setFont(SMALL_FONT);
		g.drawString("Min=" + minValue, 28, arrowBarPosition - 25);

		g.drawLine(track_width, arrowBarPosition, track_width - 15, arrowBarPosition + 30);
		g.drawLine(track_width - 15, arrowBarPosition + 30, track_width - 25, arrowBarPosition + 30);

		final String maxStr = "Max=" + maxValue;
		int strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), maxStr);
		g.drawString(maxStr, track_width - strWidth - 26, arrowBarPosition + 35);

		g.setFont(DEF_FONT);
		g.setColor(LABEL_COLOR);
		strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), title);
		g.drawString(title, (track_width / 2) - (strWidth / 2), arrowBarPosition + 35);

		/*
		 * If no points, just draw empty box.
		 */
		if (numPoints == 0) {
			g.setColor(BORDER_COLOR);
			g.setStroke(new BasicStroke(1.5f));
			g.drawRect(0, 5, track_width, trackHeight);

			return;
		}

		g.setStroke(new BasicStroke(1.0f));

		/*
		 * Fill background
		 */
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 5, track_width, trackHeight);

		int newX = 0;
		int lastY = 0;

		Point2D p1 = new Point2D.Float(0, 5);
		Point2D p2 = new Point2D.Float(0, 5);

		for (i = 0; i < doubleValues.length; i++) {
			newX = (int) (track_width * (fractions[i] / 100));

			p2.setLocation(newX, 5);

			int newY = (5 + trackHeight) - (int) ((doubleValues[i].floatValue() / max) * trackHeight);

			valueArea.reset();

			g.setColor(VALUE_AREA_COLOR);

			if (i == 0) {
				int h = (5 + trackHeight) - (int) ((below.floatValue() / max) * trackHeight);
				g.fillRect(0, h, newX, (int) ((below.floatValue() / max) * trackHeight));
				g.setColor(TRACK_COLOR);
				g.fillRect(-5, h - 5, 10, 10);
				belowSquare = new Point(0, h);
			} else {
				valueArea.addPoint((int) p1.getX(), lastY);
				valueArea.addPoint(newX, newY);
				valueArea.addPoint(newX, trackHeight + 5);
				valueArea.addPoint((int) p1.getX(), trackHeight + 5);
				g.fill(valueArea);
			}

			for (int j = 0; j < stops.size(); j++) {
				final V tValue = slider.getModel().getThumbAt(j).getObject();
				if (tValue.doubleValue() == doubleValues[i].doubleValue()) {
					Point newPoint = new Point(newX, newY);

					if (verticesList.containsValue(newPoint) == false)
						verticesList.put(j, new Point(newX, newY));

					break;
				}
			}

			lastY = newY;

			g.setColor(LABEL_COLOR);
			g.setStroke(new BasicStroke(1.5f));
			g.setFont(SMALL_FONT);

			int numberWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), doubleValues[i].toString());

			g.setColor(LABEL_COLOR);

			if (fractions[i] < 10) {
				g.drawLine(newX, newY, newX + 15, newY - 35);
				g.drawString(doubleValues[i].toString(), newX + numberWidth, newY - 48);
			} else {
				g.drawLine(newX, newY, newX - 15, newY + 35);
				g.drawString(doubleValues[i].toString(), newX - (numberWidth + 5), newY + 48);
			}

			g.setColor(LABEL_COLOR);
			g.setFont(SMALL_FONT);

			Double curPositionValue = ((Double) (((fractions[i] / 100) * tracer.getRange(vp)) + minValue))
					.doubleValue();
			String valueString = String.format("%.4f", curPositionValue);

			int flipLimit = 90;
			int borderVal = track_width - newX;

			if (((i % 2) == 0) && (flipLimit < borderVal)) {
				g.drawLine(newX, arrowBarPosition, newX + 20, arrowBarPosition - 15);
				g.drawLine(newX + 20, arrowBarPosition - 15, newX + 30, arrowBarPosition - 15);
				g.setColor(LABEL_COLOR);
				g.drawString(valueString, newX + 33, arrowBarPosition - 11);
			} else if (((i % 2) == 1) && (flipLimit < borderVal)) {
				g.drawLine(newX, arrowBarPosition, newX + 20, arrowBarPosition + 15);
				g.drawLine(newX + 20, arrowBarPosition + 15, newX + 30, arrowBarPosition + 15);
				g.setColor(LABEL_COLOR);
				g.drawString(valueString, newX + 33, arrowBarPosition + 19);
			} else if (((i % 2) == 0) && (flipLimit >= borderVal)) {
				g.drawLine(newX, arrowBarPosition, newX - 20, arrowBarPosition - 15);
				g.drawLine(newX - 20, arrowBarPosition - 15, newX - 30, arrowBarPosition - 15);
				g.setColor(LABEL_COLOR);
				g.drawString(valueString, newX - 90, arrowBarPosition - 11);
			} else {
				g.drawLine(newX, arrowBarPosition, newX - 20, arrowBarPosition + 15);
				g.drawLine(newX - 20, arrowBarPosition + 15, newX - 30, arrowBarPosition + 15);
				g.setColor(LABEL_COLOR);
				g.drawString(valueString, newX - 90, arrowBarPosition + 19);
			}

			g.setColor(LABEL_COLOR);
			g.fillOval(newX - 3, arrowBarPosition - 3, 6, 6);

			p1.setLocation(p2);
		}

		p2.setLocation(track_width, 5);

		g.setColor(VALUE_AREA_COLOR);

		int h = (5 + trackHeight) - (int) ((above.floatValue() / max) * trackHeight);
		g.fillRect((int) p1.getX(), h, track_width - (int) p1.getX(), (int) ((above.floatValue() / max) * trackHeight));

		g.setColor(TRACK_COLOR);
		g.fillRect(track_width - 5, h - 5, 10, 10);
		aboveSquare = new Point(track_width, h);

		/*
		 * Finally, draw border line (rectangle)
		 */
		g.setColor(BORDER_COLOR);
		g.setStroke(new BasicStroke(1.5f));
		g.drawRect(0, 5, track_width, trackHeight);

		g.setColor(TRACK_COLOR);
		g.setStroke(new BasicStroke(1.5f));

		for (Integer key : verticesList.keySet()) {
			Point p = verticesList.get(key);
			
			if (clickFlag) {
				int diffX = Math.abs(p.x - (curPoint.x - 6));
				int diffY = Math.abs(p.y - (curPoint.y - 12));

				if ((diffX < 6 && diffY < 6) || key == selectedIdx) {
					g.setColor(FOCUS_COLOR);
					g.setStroke(new BasicStroke(2.5f));
				} else {
					g.setColor(TRACK_COLOR);
					g.setStroke(new BasicStroke(1.5f));
				}
			}

			g.drawRect(p.x - 5, p.y - 5, 10, 10);
		}

		/*
		 * Draw below & above
		 */
		g.translate(-THUMB_WIDTH / 2, -12);
	}


	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JComponent getRendererComponent(JXMultiThumbSlider slider) {
		if (this.slider != slider) {
			if (listener == null)
				listener = new CMouseListener();
			
			slider.addMouseListener(listener);
			slider.addMouseMotionListener(new CMouseMotionListener());
			
			slider.getModel().addThumbDataListener(new ThumbDataListener() {
				@Override
				public void thumbRemoved(ThumbDataEvent evt) {
					if (selectedIdx == evt.getIndex())
						selectedIdx = -1;
				}
				@Override
				public void thumbAdded(ThumbDataEvent evt) {
					// Ignore...
				}
				@Override
				public void positionChanged(ThumbDataEvent evt) {
					// Ignore...
				}
				@Override
				public void valueChanged(ThumbDataEvent evt) {
					// Ignore...
				}
			});
			
			this.slider = slider;
		}
		
		if (verticesList == null)
			verticesList = new HashMap<>();

		if (valueMap == null)
			valueMap = new HashMap<>();

		return this;
	}

	class CMouseMotionListener implements MouseMotionListener {
		
		@Override
		@SuppressWarnings("unchecked")
		public void mouseDragged(MouseEvent e) {
			/*
			 * If user is moving thumbs, update is not necessary!
			 */
			if (e.getY() < THUMB_WIDTH && !dragFlag)
				return;

			dragFlag = true;
			curPoint = e.getPoint();

			// If beyond the bottom line
			if (clickFlag == true && selectedIdx >= 0 && slider.getModel().getThumbCount() > selectedIdx) {
				Thumb<V> selectedThumb = slider.getModel().getThumbAt(selectedIdx);
				V zero = (V) Double.valueOf(0);
				
				if (curPoint.getY() >= (trackHeight + 5)) {
					selectedThumb.setObject(zero);

					return;
				}

				double curY = curPoint.getY();
				V newY = (V) Double.valueOf(((((trackHeight + 5) - curY) * max) / (trackHeight + 5)));

				if (newY.doubleValue() > UPPER_LIMIT)
					newY = (V) UPPER_LIMIT;

				selectedThumb.setObject(newY);

				// updateMax();
				V newVal = newY;
				//cMapping.getPoint(selectedIdx).getRange().equalValue = newVal;

				ContinuousMappingPoint<K, V> point = selectedIdx >= 0 && cMapping.getPointCount() > selectedIdx
						? cMapping.getPoint(selectedIdx)
						: null;
				V lesserVal = point != null ? point.getRange().lesserValue : newVal;
				V greaterVal = point != null ? point.getRange().greaterValue : newVal;

				int numPoints = cMapping.getAllPoints().size();

				// Update Values which are not accessible from UI
				if (numPoints > 1) {
					if (selectedIdx == 0) {
						greaterVal = newVal;
					} else if (selectedIdx == (numPoints - 1)) {
						lesserVal = newVal;
					} else {
						lesserVal = newVal;
						greaterVal = newVal;
					}
				}

				BoundaryRangeValues<V> brv = new BoundaryRangeValues<>(
						NumberConverter.convert(vpValueType, lesserVal),
						NumberConverter.convert(vpValueType, newVal),
						NumberConverter.convert(vpValueType, greaterVal)
				);

				if (point != null)
					point.setRange(brv);
			}

			// dragOrigin = e.getPoint();
			slider.repaint();
		}

		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}
	}

	class CMouseListener extends MouseAdapter {
		
		@Override
		@SuppressWarnings("unchecked")
		public void mouseClicked(MouseEvent e) {
			/*
			 * Show popup dialog to enter new numerical value.
			 */
			if (isPointerInSquare(e) && (e.getClickCount() == 2)) {
				final String val = JOptionPane.showInputDialog(slider, "Please type new value for this pivot.");

				if (val == null)
					return;

				V newVal;

				try {
					newVal = (V) Double.valueOf(val);
				} catch (Exception ne) {
					// Number format error.
					return;
				}

				if (selectedIdx >= 0 && slider.getModel().getThumbCount() > selectedIdx)
					slider.getModel().getThumbAt(selectedIdx).setObject(newVal);

				updateMax();

				ContinuousMappingPoint<K, V> point = selectedIdx >= 0 && cMapping.getPointCount() > selectedIdx
						? cMapping.getPoint(selectedIdx)
						: null;
				V lesserVal = point != null ? point.getRange().lesserValue : newVal;
				V greaterVal = point != null ? point.getRange().greaterValue : newVal;

				// Update Values which are not accessible from UI
				int numPoints = cMapping.getAllPoints().size();
				
				if (numPoints > 1) {
					if (selectedIdx == 0) {
						greaterVal = newVal;
					} else if (selectedIdx == (numPoints - 1)) {
						lesserVal = newVal;
					} else {
						lesserVal = newVal;
						greaterVal = newVal;
					}
				}

				BoundaryRangeValues<V> brv = new BoundaryRangeValues<>(
						NumberConverter.convert(vpValueType, lesserVal),
						NumberConverter.convert(vpValueType, newVal),
						NumberConverter.convert(vpValueType, greaterVal)
				);

				if (point != null)
					point.setRange(brv);

				repaint();
				slider.repaint();
				repaint();
			} else if (e.getClickCount() == 2 && isBelow(e.getPoint())) {
				final String val = JOptionPane.showInputDialog(slider, "Please type new value for BELOW:");

				if (val == null)
					return;

				try {
					below = (V) Double.valueOf(val);
				} catch (Exception ne) {
					// Number format error.
					return;
				}

				V newValue = below;

				BoundaryRangeValues<V> brv;
				BoundaryRangeValues<V> original;

				original = cMapping.getPoint(0).getRange();
				brv = new BoundaryRangeValues<>(
						NumberConverter.convert(vpValueType, newValue),
						NumberConverter.convert(vpValueType, original.equalValue),
						NumberConverter.convert(vpValueType, original.greaterValue)
				);
				cMapping.getPoint(0).setRange(brv);

				slider.repaint();
				repaint();
				firePropertyChange(ContinuousMappingEditorPanel.BELOW_VALUE_CHANGED, null, below);
			} else if ((e.getClickCount() == 2) && (isAbove(e.getPoint()))) {
				final String val = JOptionPane.showInputDialog(slider, "Please type new value for ABOVE:");

				if (val == null)
					return;

				try {
					above = (V) Double.valueOf(val);
				} catch (Exception ne) {
					// Number format error.
					return;
				}

				BoundaryRangeValues<V> brv;
				BoundaryRangeValues<V> original;

				original = cMapping.getPoint(cMapping.getPointCount() - 1).getRange();
				brv = new BoundaryRangeValues<>(
						NumberConverter.convert(vpValueType, original.lesserValue),
						NumberConverter.convert(vpValueType, original.equalValue),
						NumberConverter.convert(vpValueType, above)
				);
				
				cMapping.getPoint(cMapping.getPointCount() - 1).setRange(brv);

				slider.repaint();
				repaint();
				firePropertyChange(ContinuousMappingEditorPanel.ABOVE_VALUE_CHANGED, null, above);
			}
		}

		private boolean isBelow(final Point p) {
			if (belowSquare == null)
				return false;

			int diffY = Math.abs(p.y - 12 - belowSquare.y);
			int diffX = Math.abs(p.x - 6 - belowSquare.x);

			return diffX < 6 && diffY < 6;
		}

		private boolean isAbove(final Point p) {
			if (aboveSquare == null)
				return false;

			int diffY = Math.abs(p.y - 12 - aboveSquare.y);
			int diffX = Math.abs(p.x - 6 - aboveSquare.x);

			return diffX < 6 && diffY < 6;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			curPoint = e.getPoint();
			// dragOrigin = e.getPoint();

			for (Integer key : verticesList.keySet()) {
				Point p = verticesList.get(key);
				int diffY = Math.abs((p.y + 12) - curPoint.y);
				int diffX = Math.abs((p.x + (THUMB_WIDTH / 2)) - curPoint.x);

				if (diffX < 6 && diffY < 6) {
					selectedIdx = key;
					clickFlag = true;
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			clickFlag = false;
			updateMax();

			if (slider.getSelectedThumb() == null)
				slider.repaint();

			dragFlag = false;
			repaint();
		}

		private boolean isPointerInSquare(MouseEvent e) {
			curPoint = e.getPoint();
			// dragOrigin = e.getPoint();

			for (Integer key : verticesList.keySet()) {
				Point p = verticesList.get(key);
				int diffY = Math.abs((p.y + 12) - curPoint.y);
				int diffX = Math.abs((p.x + (THUMB_WIDTH / 2)) - curPoint.x);

				if (diffX < 6 && diffY < 6) {
					selectedIdx = key;

					return true;
				}
			}

			return false;
		}

		private void updateMax() {
			Number val;
			Float curMax = 0f;

			for (Thumb<V> thumb : slider.getModel().getSortedThumbs()) {
				val = thumb.getObject();

				if (val.floatValue() > curMax)
					curMax = val.floatValue();
			}

			max = curMax;
		}
	}
	
	public ImageIcon getTrackGraphicIcon(int iconWidth, int iconHeight) {
		return drawIcon(iconWidth, iconHeight, false);
	}
	
	public ImageIcon getLegend(int iconWidth, int iconHeight) {
		return drawIcon(iconWidth, iconHeight, true);
	}

	private ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail) {
		if (slider == null)
			return null;

		final BufferedImage bi = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = bi.createGraphics();

		// Turn Anti-alias on
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double minValue = tracer.getMin(vp);
		double maxValue = tracer.getMax(vp);
		double valueRange = tracer.getRange(vp);
		/*
		 * Fill background
		 */
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, iconWidth, iconHeight);

		int leftSpace = 10;
		int trackHeight = iconHeight - 9;
		int trackWidth = iconWidth - leftSpace;

		/*
		 * Compute fractions from mapping
		 */
		List<Thumb<V>> stops = slider.getModel().getSortedThumbs();

		int numPoints = stops.size();

		float[] fractions = new float[numPoints];
		Float[] floatProperty = new Float[numPoints];
		int i = 0;

		values.clear();
		values.add(below);
		values.add(above);

		for (Thumb<V> thumb : stops) {
			floatProperty[i] = ((Number) thumb.getObject()).floatValue();
			fractions[i] = thumb.getPosition();
			values.add((V) thumb.getObject());
			i++;
		}

		for (V val : values) {
			if (min >= val.floatValue())
				min = val.floatValue();

			if (max <= val.floatValue())
				max = val.floatValue();
		}

		// Draw min/max
		g.setColor(LABEL_COLOR);
		g.setFont(ICON_FONT);

		int minWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), String.format("%.1f", min));
		int maxWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), String.format("%.1f", max));

		if (detail) {
			leftSpace = LEFT_SPACE;
			trackHeight = iconHeight - 30;
		} else {
			leftSpace = Math.max(minWidth, maxWidth) + 1;
		}

		trackWidth = iconWidth - leftSpace;

		g.drawString(String.format("%.1f", min), 0, trackHeight);
		g.drawString(String.format("%.1f", max), 0, 8);

		/*
		 * If no points, just return empty rectangle.
		 */
		if (numPoints == 0) {
			g.setStroke(new BasicStroke(1.0f));
			g.setColor(LABEL_COLOR);
			g.drawRect(leftSpace, 0, trackWidth - 3, trackHeight);

			return new ImageIcon(bi);
		}

		g.translate(leftSpace, 0);
		g.setStroke(new BasicStroke(1.0f));

		int newX = 0;
		int lastY = 0;

		Point2D p1 = new Point2D.Float(0, 0);
		Point2D p2 = new Point2D.Float(0, 0);

		for (i = 0; i < floatProperty.length; i++) {
			newX = (int) (trackWidth * (fractions[i] / 100)) - 3;

			if (newX < 0) {
				newX = 0;
			}

			p2.setLocation(newX, 0);
			int newY = trackHeight - (int) ((floatProperty[i] / max) * trackHeight);
			valueArea.reset();

			g.setColor(VALUE_AREA_COLOR);

			if (i == 0) {
				int h = trackHeight - (int) ((below.floatValue() / max) * trackHeight);
				g.fillRect(0, h, newX, (int) ((below.floatValue() / max) * trackHeight));
			} else {
				valueArea.addPoint((int) p1.getX(), lastY);
				valueArea.addPoint(newX, newY);
				valueArea.addPoint(newX, trackHeight);
				valueArea.addPoint((int) p1.getX(), trackHeight);
				g.fill(valueArea);
			}

			for (int j = 0; j < stops.size(); j++) {
				if (slider.getModel().getThumbAt(j).getObject() == floatProperty[i]) {
					Point newPoint = new Point(newX, newY);

					if (verticesList.containsValue(newPoint) == false)
						verticesList.put(j, new Point(newX, newY));

					break;
				}
			}

			lastY = newY;
			p1.setLocation(p2);
		}

		p2.setLocation(trackWidth, 0);

		g.setColor(VALUE_AREA_COLOR);

		int h = trackHeight - (int) ((above.floatValue() / max) * trackHeight);
		g.fillRect((int) p1.getX(), h, trackWidth - (int) p1.getX() - 3,
				(int) ((above.floatValue() / max) * trackHeight));

		g.translate(-leftSpace, 0);

		/*
		 * Draw border line (rectangle)
		 */
		g.setColor(BORDER_COLOR);
		g.setStroke(new BasicStroke(1.0f));
		g.drawRect(leftSpace, 0, trackWidth - 3, trackHeight);

		/*
		 * Draw numbers and arrows
		 */
		g.setFont(ICON_FONT);

		final String minStr = String.format("%.2f", minValue);
		final String maxStr = String.format("%.2f", maxValue);
		int strWidth;
		g.setColor(LABEL_COLOR);

		if (detail) {
			String fNum = null;

			for (int j = 0; j < fractions.length; j++) {
				fNum = String.format("%.2f", ((fractions[j] / 100) * valueRange) + minValue);
				strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), fNum);
				g.drawString(fNum, ((fractions[j] / 100) * trackWidth) - (strWidth / 2) + leftSpace, iconHeight - 20);
			}

			g.drawString(minStr, leftSpace, iconHeight);
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
			g.drawLine(leftSpace, iconHeight - 9, ((iconWidth - leftSpace) / 2) - (titleWidth / 2) - 3, iconHeight - 9);
			g.drawLine((iconWidth / 2) + (titleWidth / 2) + 3, iconHeight - 9, iconWidth, iconHeight - 9);

			/*
			 * Draw vertical arrow
			 */
			int panelHeight = iconHeight - 30;

			final Polygon poly = new Polygon();
			int top = 0;

			g.setStroke(new BasicStroke(1.0f));

			int center = (leftSpace / 2) + 6;

			poly.addPoint(center, top);
			poly.addPoint(center - 6, top + 15);
			poly.addPoint(center, top + 15);
			g.fillPolygon(poly);

			g.drawLine(center, top, center, panelHeight);
			g.setColor(LABEL_COLOR);
			g.setFont(SMALL_FONT);

			final String label = vp.getDisplayName();
			final int width = SwingUtilities.computeStringWidth(g.getFontMetrics(), label);
			AffineTransform af = new AffineTransform();
			af.rotate(Math.PI + (Math.PI / 2));
			g.setTransform(af);

			g.setColor(LABEL_COLOR);
			g.drawString(vp.getDisplayName(), (-panelHeight / 2) - (width / 2), (leftSpace / 2) + 5);
		} else {
			g.drawString(minStr, 0, iconHeight);
			strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), maxStr);
			g.drawString(maxStr, iconWidth - strWidth - 2, iconHeight);
		}

		return new ImageIcon(bi);
	}
}
