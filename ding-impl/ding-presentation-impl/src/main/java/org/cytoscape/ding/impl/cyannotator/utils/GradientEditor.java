package org.cytoscape.ding.impl.cyannotator.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.cytoscape.ding.internal.util.ColorUtil;
import org.cytoscape.ding.internal.util.MathUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
 * A generic editor for configuring a multiple point varying gradient.
 * Adapted from: 
 * https://www.assembla.com/code/ascent_game/git/node/blob/Slick/tools/org/newdawn/slick/tools/peditor/GradientEditor.java?raw=1&rev=c1b50b1c68bcb1d1eea464c7e507154ecf51e748
 */
@SuppressWarnings("serial")
public class GradientEditor extends JPanel {
	
	private static final int HPAD = 4;
	private static final int VPAD = 2;
	private static final int BORDER_WIDTH = 2;
	private static final int MARKER_SIZE = 12;
	
	private static final float[] MAIN_POSITIONS = { .1f, .2f, .3f, .4f, .5f, .6f, .7f, .8f, .9f };
	
	private Color color1 = UIManager.getColor("CyComponent.borderColor");
	private Color color2 = UIManager.getColor("Table.background");
	private Color selColor = UIManager.getColor("Focus.color");
	
	private Stroke defStroke = new BasicStroke(1);
	private Stroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 1 }, 0);
	
	/** The controlPoints of control points */
	private List<ControlPoint> controlPoints = new ArrayList<>();
	/** The current selected control point */
	private ControlPoint selected;
	
	/** The shape used for the movable markers */
	private Polygon marker = new Polygon();
	
	/** The x position of the gradient bar */
	private int x;
	/** The y position of the gradient bar */
	private int y;
	/** The width of the gradient bar */
	private int w;
	/** The height of the gradient bar */
	private int h;
	
	/** The listeners that should be notified of changes to this emitter */
	private List<ActionListener> listeners = new ArrayList<>();
	
	private boolean mouseDragging;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	/**
	 * Create a new editor for gradients
	 */
	public GradientEditor(List<ControlPoint> points, CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		setPoints(points, false);
		init();
	}
	
	public GradientEditor(float[] positions, Color[] colors, CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		var points = new ArrayList<ControlPoint>();
		
		if (positions != null) {
			for (int i = 0; i < positions.length; i++) {
				float pos = positions[i];
				Color c = null;
					
				if (colors != null && colors.length > i)
					c = colors[i];
				
				points.add(new ControlPoint((c != null ? c : Color.WHITE), pos));
			}
		}
		
		setPoints(points, false);
		init();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		var components = getComponents();
		
		for (int i = 0; i < components.length; i++)
			components[i].setEnabled(enabled);
		
		repaint(0);
	}
	
	/**
	 * Add a listener that will be notified on change of this editor
	 * 
	 * @param listener The listener to be notified on change of this editor
	 */
	public void addActionListener(ActionListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Remove a listener from this editor. It will no longer be notified
	 * 
	 * @param listener The listener to be removed
	 */
	public void removeActionListener(ActionListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Add a control point to the gradient.
	 * 
	 * @param pos The position in the gradient (0 -> 1)
	 * @param color The color at the new control point
	 */
	public void addPoint(float pos, Color col) {
		addPoint(new ControlPoint(col, pos));
	}
	
	/**
	 * Delete the currently selected point
	 */
	public void deletePoint() {
		if (!isEnabled())
			return;
		if (selected == null)
			return;
		if (controlPoints.indexOf(selected) == 0)
			return;
		if (controlPoints.indexOf(selected) == controlPoints.size()-1)
			return;
		
		controlPoints.remove(selected);
		setSelected(null);
		repaint(0);
		fireUpdate();
	}
	
	/**
	 * Set the starting color
	 * 
	 * @param color The color at the start of the gradient
	 */
	public void setStart(Color col) {
		(controlPoints.get(0)).setColor(col);
		repaint(0);
	}

	/**
	 * Set the ending colour
	 * 
	 * @param color The color at the end of the gradient
	 */
	public void setEnd(Color col) {
		(controlPoints.get(controlPoints.size() - 1)).setColor(col);
		repaint(0);
	}
	
	/**
	 * Add a new control point
	 */
	public void addPoint() {
		var point = new ControlPoint(Color.WHITE, 0.5f);
		addPoint(point);
		
		var oldSelected = this.selected;
		this.selected = point;
		
		repaint(0);
		
		fireUpdate();
		firePropertyChange("selected", oldSelected, selected);
	}
	
	/**
	 * Edit the currently selected control point
	 */
	public void editPoint() {
		if (selected == null)
			return;
		
		var chooserFactory = serviceRegistrar.getService(CyColorPaletteChooserFactory.class);
		var chooser = chooserFactory.getColorPaletteChooser(BrewerType.ANY, false);
		var col = chooser.showDialog(this, "Colors", null, selected.getColor(), 8);
		
		if (col != null) {
			selected.setColor(col);
			repaint(0);
			fireUpdate();
		}
	}
	
	/**
	 * Remove all the control points from the gradient editor (this does
	 * not include start and end points)
	 */
	public void clearPoints() {
		for (int i = 1; i < controlPoints.size() - 1; i++) {
			controlPoints.remove(1);
		}
		
		repaint(0);
		fireUpdate();
	}
	
	/**
	 * Get the number of control points in the gradient
	 * 
	 * @return The number of control points in the gradient
	 */
	public int getControlPointCount() {
		return controlPoints.size();
	}
	
	/**
	 * Get the graident position of the control point at the specified
	 * index.
	 *  
	 * @param index The index of the control point
	 * @return The gradient position of the control point
	 */
	public float getPointPos(int index) {
		return controlPoints.get(index).getPosition();
	}

	/**
	 * Get the color of the control point at the specified
	 * index.
	 *  
	 * @param index The index of the control point
	 * @return The color of the control point
	 */
	public Color getColor(int index) {
		return controlPoints.get(index).getColor();
	}
	
	public List<ControlPoint> getControlPoints() {
		return new ArrayList<ControlPoint>(controlPoints);
	}
	
	/**
	 * @return Distinct position values (duplicates are removed).
	 */
	public float[] getPositions() {
		// We need to remove duplicates or MultipleGradientPaint throws this exception:
		// "java.lang.IllegalArgumentException: Keyframe fractions must be increasing"
		var positions = new ArrayList<Float>();
		int size = controlPoints.size();
		
		for (int i = 0; i < size; i++) {
			var cp = controlPoints.get(i);
			float pos = cp.getPosition();
			
			if (i < size - 1) {
				if (pos == controlPoints.get(i + 1).getPosition())
					continue;
			}
			
			positions.add(pos);
		}
		
		var res = new float[positions.size()];
		int i = 0;
		
		for (var pos : positions)
		  res[i++] = pos;
		
		return res;
	}
	
	/**
	 * @return Colors from the distinct positions.
	 */
	public Color[] getColors() {
		var colors = new ArrayList<Color>();
		int size = controlPoints.size();
		
		for (int i = 0; i < size; i++) {
			var cp = controlPoints.get(i);
			float pos = cp.getPosition();
			
			if (i < size - 1) {
				if (pos == controlPoints.get(i + 1).getPosition())
					continue;
			}
			
			colors.add(cp.getColor());
		}
		
		return colors.toArray(new Color[colors.size()]);
	}
	
	public void setPoints(List<ControlPoint> points) {
		setPoints(points, true);
	}
	
	public ControlPoint getSelected() {
		return selected;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init() {
		setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
		setMinimumSize(new Dimension(100, 50));
		setPreferredSize(new Dimension(200, 50));
		
		setFocusable(true);
		setKeyBindings();
		
		marker.addPoint(0, 0);
		marker.addPoint(MARKER_SIZE / 2, MARKER_SIZE);
		marker.addPoint(-MARKER_SIZE / 2, MARKER_SIZE);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectPoint(e.getX(), e.getY());
				repaint(0);
				
				if (e.getClickCount() == 2)
					editPoint();
			}
			@Override
			public void mouseReleased(MouseEvent evt) {
				mouseDragging = false;
				repaint();
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				mouseDragging = true;
				movePoint(e);
				repaint(0);
			}
		});
	}
	
	private void addPoint(ControlPoint point) {
		float pos = point.getPosition();
		
		for (int i = 0; i < controlPoints.size(); i++) {
			var now = controlPoints.get(i);
			var next = i < controlPoints.size() - 1 ? controlPoints.get(i + 1) : null;

			if ((pos >= now.getPosition()) && (next == null || pos < next.getPosition())) {
				int newIdx = next == null ? i : i + 1; // Do not replace the end point! 
				
				controlPoints.add(newIdx, point);
				break;
			}
		}
		
		repaint(0);
	}
	
	private void setPoints(List<ControlPoint> points, boolean update) {
		setSelected(null);
		controlPoints.clear();
		
		if (points == null || points.isEmpty()) {
			controlPoints.add(new ControlPoint(Color.WHITE, 0));
			controlPoints.add(new ControlPoint(Color.BLACK, 1));
		} else {
			for (int i = 0; i < points.size() - 1; i++) {
				if (i == 0) {
					if (points.get(i) != null)
						controlPoints.add(new ControlPoint(points.get(i).getColor(), 0)); // start
					else
						controlPoints.add(new ControlPoint(Color.WHITE, 0)); // start
					
					if (points.get(points.size() - 1) != null)
						controlPoints.add(new ControlPoint(points.get(points.size() - 1).getColor(), 1)); // end
					else
						controlPoints.add(new ControlPoint(Color.BLACK, 1));
				} else {
					var cp = points.get(i);
					
					if (cp != null)
						addPoint(cp.getPosition(), cp.getColor());
				}
			}
		}
		
		if (update) {
			repaint(0);
			fireUpdate();
		}
	}
	
	/**
	 * Fire an update to all listeners
	 */
	private void fireUpdate() {
		var event = new ActionEvent(this, 0, "");

		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).actionPerformed(event);
	}
	
	/**
	 * Check if there is a control point at the specified mouse location
	 * 
	 * @param mx The mouse x coordinate 
	 * @param my The mouse y coordinate
	 * @param pt The point to check against
	 * @return True if the mouse point coincides with the control point
	 */
	private boolean checkPoint(int mx, int my, ControlPoint pt) {
		int shift = 0; // for the first/last points
		
		if (pt == controlPoints.get(0))
			shift = -BORDER_WIDTH;
		else if (pt == controlPoints.get(controlPoints.size() - 1))
			shift = BORDER_WIDTH;
		
		int dx = (int) Math.abs((MARKER_SIZE + shift + (w * pt.getPosition())) - mx);
		int dy = Math.abs((y + h + BORDER_WIDTH / 2 + MARKER_SIZE / 2) - my);

		if ((dx < MARKER_SIZE / 2) && (dy < BORDER_WIDTH / 2 + MARKER_SIZE / 2))
			return true;

		return false;
	}
	
//	/**
//	 * Sort the control points based on their position
//	 */
//	private void sortPoints() {
//		var firstPt = controlPoints.get(0);
//		var lastPt = controlPoints.get(controlPoints.size() - 1);
//
//		var compare = new Comparator<ControlPoint>() {
//			@Override
//			public int compare(ControlPoint first, ControlPoint second) {
//				if (first == firstPt)
//					return -1;
//				if (second == lastPt)
//					return -1;
//				
//				float a = first.getPosition();
//				float b = second.getPosition();
//				return (int) ((a-b) * 10000);
//			}
//		};
//		
//		Collections.sort(controlPoints, compare);
//	}
	
	/**
	 * Select the control point at the specified mouse coordinate
	 * 
	 * @param mx The mouse x coordinate 
	 * @param my The mouse y coordinate
	 */
	private void selectPoint(int mx, int my) {
		if (!isEnabled())
			return;
		
		// Check all points, except the start and end ones, first
		for (int i = controlPoints.size() - 2; i > 0; i--) {
			if (checkPoint(mx, my, controlPoints.get(i))) {
				setSelected(controlPoints.get(i));
				requestFocusInWindow();
				
				return;
			}
		}
		// Now we can check the first and last points
		if (checkPoint(mx, my, controlPoints.get(0))) {
			setSelected(controlPoints.get(0));
			requestFocusInWindow();
			
			return;
		}
		if (checkPoint(mx, my, controlPoints.get(controlPoints.size() - 1))) {
			setSelected(controlPoints.get(controlPoints.size() - 1));
			requestFocusInWindow();
			
			return;
		}
		
		setSelected(null);
	}
	
	private void setSelected(ControlPoint selected) {
		if (this.selected != selected) {
			var oldValue = this.selected;
			this.selected = selected;
			firePropertyChange("selected", oldValue, selected);
		}
	}
	
	/**
	 * Move the current point to the specified mouse location
	 */
	private void movePoint(MouseEvent e) {
		if (!isEnabled())
			return;
		if (selected == null)
			return;
		if (controlPoints.indexOf(selected) == 0)
			return;
		if (controlPoints.indexOf(selected) == controlPoints.size() - 1)
			return;
		
		float newPos = (e.getX() - 10) / (float) w;
		newPos = Math.min(1, newPos);
		newPos = Math.max(0, newPos);
		
		// Snap to the nearest main position if holding the SHIFT key
		if (e.isShiftDown())
			newPos = MathUtil.findNearestNumber(MAIN_POSITIONS, newPos);
		
		selected.setPosition(newPos);
		controlPoints.remove(selected);
		addPoint(selected);
		
		fireUpdate();
	}
	
	private void setKeyBindings() {
		var actionMap = this.getActionMap();
		var inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), KeyAction.VK_DELETE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), KeyAction.VK_BACK_SPACE);
		
		actionMap.put(KeyAction.VK_DELETE, new KeyAction(KeyAction.VK_DELETE));
		actionMap.put(KeyAction.VK_BACK_SPACE, new KeyAction(KeyAction.VK_BACK_SPACE));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		var g2 = (Graphics2D) g.create();
		
		var insets = getInsets();
		int bw = BORDER_WIDTH / 2; // The width of each border (we draw 2 borders, an internal and an external one)
		
		// Draw compound border first, but draw it as two fill rectangles
		x = HPAD + MARKER_SIZE / 2;
		y = VPAD;
		w = getWidth()  - insets.left - insets.right  - 2 * HPAD - MARKER_SIZE;
		h = getHeight() - insets.top  - insets.bottom - 2 * VPAD - MARKER_SIZE;
		
		// External Border
		g2.setColor(color1);
		g2.setStroke(new BasicStroke(bw));
		g2.drawRect(x, y, w, h);
		// Internal Border
		x += bw;
		y += bw;
		w -= 2 * bw;
		h -= 2 * bw;
		g2.setColor(color2);
		g2.fillRect(x, y, w, h);
		
		// Linear Gradient
		x += bw;
		y += bw;
		w -= 2 * bw;
		h -= 2 * bw;
		
//		int px = x;
//		
//		for (int i = 0; i < controlPoints.size() - 1; i++) {
//			var now = controlPoints.get(i);
//			var next = controlPoints.get(i + 1);
//
//			int size = (int) ((next.getPosition() - now.getPosition()) * w);
//			g.setPaint(new GradientPaint(px, y, now.getColor(), px + size, y, next.getColor()));
//			g.fillRect(px, y, size + bw, h);
//			px += size;
//		}
		var paint = new LinearGradientPaint(
				new Point2D.Double(x, y),
				new Point2D.Double(x + w, y),
				getPositions(),
				getColors()
		);
		g2.setPaint(paint);
		g2.fillRect(x, y, w, h);
		
		if (mouseDragging) {
			// Draw a temporary dashed line to show where the center of the gradient is
			float mpx = x - bw / 2 + w * 0.5f;
			
			g2.translate(mpx, y);
			g2.setStroke(dashedStroke);
			
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawLine(0, 0, 0, h);
			
			g2.setStroke(defStroke);
			g2.translate(-mpx, -y);
		}
		
		// Control Point Markers (start/end first)
		paintMarker(g2, controlPoints.get(0));
		paintMarker(g2, controlPoints.get(controlPoints.size() - 1));
		
		for (int i = 1; i < controlPoints.size() - 1; i++) {
			paintMarker(g2, controlPoints.get(i));
		}
		
		g2.dispose();
	}

	private void paintMarker(Graphics g, ControlPoint pt) {
		var g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		float py = y + h;
		float px = MARKER_SIZE + (w * pt.getPosition());
		
		int bw = BORDER_WIDTH / 2;
		var borderColor = pt == selected ? selColor : color1;
		var stroke = new BasicStroke(pt == selected ? bw * 1.5f : bw);
		
		var firstPt = controlPoints.get(0);
		var lastPt = controlPoints.get(controlPoints.size() - 1);
		
		if (pt == firstPt || pt == lastPt) {
			if (pt == firstPt)
				g2.translate(px - BORDER_WIDTH, py);
			else
				g2.translate(px + BORDER_WIDTH, py);
			
			g2.setColor(pt.getColor());
			g2.fillOval(-MARKER_SIZE / 2, 0, MARKER_SIZE, MARKER_SIZE);
			
			g2.setColor(borderColor);
			g2.setStroke(stroke);
			g2.drawOval(-MARKER_SIZE / 2, 0, MARKER_SIZE, MARKER_SIZE);
		} else {
			
			g2.translate(px, py);
			
			g2.setColor(pt.getColor());
			g2.fillPolygon(marker);
			
			g2.setColor(borderColor);
			g2.setStroke(stroke);
			g2.drawPolygon(marker);
		}
		
		g2.dispose();
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class KeyAction extends AbstractAction {

		final static String VK_DELETE = "VK_DELETE";
		final static String VK_BACK_SPACE = "VK_BACK_SPACE";
		
		KeyAction(String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			var cmd = evt.getActionCommand();
			
			if (isEnabled()) {
				if (cmd.equals(VK_DELETE) || cmd.equals(VK_BACK_SPACE)) {
					if (selected != null)
						deletePoint();
				}
			}
		}
	}
	
	public static class ControlPoint {
		
		/** The color at this control point */
		private Color color;
		/** The position of this control point (0.0 -> 1.0) */
		private float position;
		
		public ControlPoint() {
			this(Color.WHITE, 0.0f);
		}
		
		/**
		 * Create a new control point
		 * 
		 * @param color The color at this control point
		 * @param position The position of this control point (0 -> 1)
		 */
		public ControlPoint(Color color, float position) {
			if (color == null)
				throw new IllegalArgumentException("'color' must not be null.");
			
			this.setColor(color);
			this.setPosition(position);
		}
		
		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public float getPosition() {
			return position;
		}

		public void setPosition(float position) {
			this.position = position;
		}
		
		@Override
		public String toString() {
			return getPosition() + ":" + ColorUtil.toHexString(getColor());
		}
	}
}
