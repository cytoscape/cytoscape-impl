package org.cytoscape.ding.impl.cyannotator.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.ding.internal.util.ColorUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 * A generic editor for configuring a multiple point varying gradient.
 * Adapted from: 
 * https://www.assembla.com/code/ascent_game/git/node/blob/Slick/tools/org/newdawn/slick/tools/peditor/GradientEditor.java?raw=1&rev=c1b50b1c68bcb1d1eea464c7e507154ecf51e748
 */
@SuppressWarnings("serial")
public class GradientEditor extends JPanel {
	
	private static int HPAD = 4;
	private static int VPAD = 2;
	private static int BORDER_WIDTH = 2;
	private static int MARKER_SIZE = 12;
	
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
	
	@Override
	public void paintComponent(Graphics g1d) {
		super.paintComponent(g1d);
		
		var g = (Graphics2D) g1d.create();
		
		var insets = getInsets();
		int bw = BORDER_WIDTH / 2; // The width of each border (we draw 2 borders, an internal and an external one)
		
		// Draw compound border first, but draw it as two fill rectangles
		x = HPAD + MARKER_SIZE / 2;
		y = VPAD;
		w = getWidth()  - insets.left - insets.right  - 2 * HPAD - MARKER_SIZE;
		h = getHeight() - insets.top  - insets.bottom - 2 * VPAD - MARKER_SIZE;
		
		var extBC = UIManager.getColor("CyComponent.borderColor");
		var intBC = UIManager.getColor("Table.background");
		
		// External Border
		g.setColor(extBC);
		g.setStroke(new BasicStroke(bw));
		g.drawRect(x, y, w, h);
		// Internal Border
		x += bw;
		y += bw;
		w -= 2 * bw;
		h -= 2 * bw;
		g.setColor(intBC);
		g.fillRect(x, y, w, h);
		
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
		g.setPaint(paint);
		g.fillRect(x, y, w, h);
		
		// Control Point Markers (start/end first)
		paintMarker(g, controlPoints.get(0));
		paintMarker(g, controlPoints.get(controlPoints.size() - 1));
		
		for (int i = 1; i < controlPoints.size() - 1; i++) {
			paintMarker(g, controlPoints.get(i));
		}
		
		g.dispose();
	}

	private void paintMarker(Graphics g1d, ControlPoint pt) {
		var g = (Graphics2D) g1d.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		float py = y + h;
		float px = MARKER_SIZE + (w * pt.getPosition());
		
		int bw = BORDER_WIDTH / 2;
		var borderColor = UIManager.getColor(pt == selected ? "Focus.color" : "CyComponent.borderColor");
		var stroke = new BasicStroke(pt == selected ? bw * 1.5f : bw);
		
		var firstPt = controlPoints.get(0);
		var lastPt = controlPoints.get(controlPoints.size() - 1);
		
		if (pt == firstPt || pt == lastPt) {
			if (pt == firstPt)
				g.translate(px - BORDER_WIDTH, py);
			else
				g.translate(px + BORDER_WIDTH, py);
			
			g.setColor(pt.getColor());
			g.fillOval(-MARKER_SIZE / 2, 0, MARKER_SIZE, MARKER_SIZE);
			
			g.setColor(borderColor);
			g.setStroke(stroke);
			g.drawOval(-MARKER_SIZE / 2, 0, MARKER_SIZE, MARKER_SIZE);
		} else {
			
			g.translate(px, py);
			
			g.setColor(pt.getColor());
			g.fillPolygon(marker);
			
			g.setColor(borderColor);
			g.setStroke(stroke);
			g.drawPolygon(marker);
		}
		
		g.dispose();
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
		});
		
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				movePoint(e.getX(), e.getY());
				repaint(0);
			}
			@Override
			public void mouseMoved(MouseEvent e) {
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
				return;
			}
		}
		// Now we can check the first and last points
		if (checkPoint(mx, my, controlPoints.get(0))) {
			setSelected(controlPoints.get(0));
			return;
		}
		if (checkPoint(mx, my, controlPoints.get(controlPoints.size() - 1))) {
			setSelected(controlPoints.get(controlPoints.size() - 1));
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
	 * 
	 * @param mx The x coordinate of the mouse
	 * @param my The y coordinate of teh mouse 
	 */
	private void movePoint(int mx, int my) {
		if (!isEnabled())
			return;
		if (selected == null)
			return;
		if (controlPoints.indexOf(selected) == 0)
			return;
		if (controlPoints.indexOf(selected) == controlPoints.size() - 1)
			return;
		
		float newPos = (mx - 10) / (float) w;
		newPos = Math.min(1, newPos);
		newPos = Math.max(0, newPos);
		
		selected.setPosition(newPos);
		controlPoints.remove(selected);
		addPoint(selected);
		
		fireUpdate();
	}
	
	// ==[ CLASSES ]====================================================================================================
	
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
