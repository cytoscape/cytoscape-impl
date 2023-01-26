package org.cytoscape.ding.impl.cyannotator.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
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
	
	/** The controlPoints of control points */
	private List<ControlPoint> controlPoints = new ArrayList<>();
	/** The current selected control point */
	private ControlPoint selected;
	/** The polygon used for the markers */
	private Polygon poly = new Polygon();
	/** A button to addBtn a control point */
	private JButton addBtn = new JButton("Add");
	/** A button to editBtn a control point */
	private JButton editBtn = new JButton("Edit");
	/** A button to delete a control point */
	private JButton delBtn = new JButton("Delete");
	
	/** The x position of the gradient bar */
	private int x;
	/** The y position of the gradient bar */
	private int y;
	/** The width of the gradient bar */
	private int width;
	/** The height of the gradient bar */
	private int barHeight;
	
	/** The listeners that should be notified of changes to this emitter */
	private List<ActionListener> listeners = new ArrayList<>();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	/**
	 * Create a new editor for gradients
	 */
	public GradientEditor(List<ControlPoint> points, CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		setPoints(points);
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
		
		setPoints(points);
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
		
		var g = (Graphics2D) g1d;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		width = getWidth() - 30;
		x = 10;
		y = 20;
		barHeight = 25;
		
		var lineColor = UIManager.getColor(isEnabled() ? "Label.disabledForeground" : "Separator.foreground");
		
		for (int i = 0; i < controlPoints.size() - 1; i++) {
			var now = controlPoints.get(i);
			var next = controlPoints.get(i+1);
			
			int size = (int) ((next.getPosition() - now.getPosition()) * width);
			g.setPaint(new GradientPaint(x, y, now.getColor(), x + size, y, next.getColor()));
			g.fillRect(x, y, size + 1, barHeight);
			x += size;
		}
		
		g.setColor(lineColor);
		g.drawRect(10, y, width, barHeight - 1);
		
		for (int i = 0; i < controlPoints.size(); i++) {
			var pt = controlPoints.get(i);
			g.translate(10 + (width * pt.getPosition()), y + barHeight);
			g.setColor(pt.getColor());
			g.fillPolygon(poly);
			g.setColor(pt == selected? UIManager.getColor("Focus.color") : lineColor);
			g.setStroke(new BasicStroke(pt == selected ? 1.5f : 1.0f));
			g.drawPolygon(poly);
			
			g.translate(-10 - (width * pt.getPosition()), -y - barHeight);
		}
	}
	
	/**
	 * Add a control point to the gradient
	 * 
	 * @param position The position in the gradient (0 -> 1)
	 * @param color The color at the new control point
	 */
	public void addPoint(float pos, Color col) {
		var point = new ControlPoint(col, pos);
		
		for (int i = 0; i < controlPoints.size() - 1; i++) {
			var now = controlPoints.get(i);
			var next = controlPoints.get(i+1);
			
			if ((now.getPosition() <= pos) && (next.getPosition() >= pos)) {
				controlPoints.add(i+1, point);
				break;
			}
		}
		
		repaint(0);
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
		(controlPoints.get(controlPoints.size()-1)).setColor(col);
		repaint(0);
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
	 * @return The graident position of the control point
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
	
	public float[] getPositions() {
		var positions = new float[controlPoints.size()];
		
		for (int i = 0; i < controlPoints.size(); i++)
			positions[i] = controlPoints.get(i).getPosition();
		
		return positions;
	}
	
	public Color[] getColors() {
		var colors = new Color[controlPoints.size()];
		
		for (int i = 0; i < controlPoints.size(); i++)
			colors[i] = controlPoints.get(i).getColor();
		
		return colors;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void init() {
		setLayout(null);
		setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
		
		addBtn.setBounds(20, 70, 75, 20);
		add(addBtn);
		
		editBtn.setBounds(100, 70, 75, 20);
		editBtn.setEnabled(false);
		add(editBtn);
		
		delBtn.setBounds(180, 70, 75, 20);
		delBtn.setEnabled(false);
		add(delBtn);
		
		addBtn.addActionListener(e -> addPoint());
		delBtn.addActionListener(e -> delPoint());
		editBtn.addActionListener(e -> editPoint());
		
		LookAndFeelUtil.makeSmall(addBtn, editBtn, delBtn);
		
		poly.addPoint(0, 0);
		poly.addPoint(5, 10);
		poly.addPoint(-5, 10);
		
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
	
	private void setPoints(List<ControlPoint> points) {
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
	}
	
	/**
	 * Fire an update to all listeners
	 */
	private void fireUpdate() {
		var event = new ActionEvent(this,0,"");
		
		for (int i=0; i < listeners.size(); i++)
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
		int dx = (int) Math.abs((10+(width * pt.getPosition())) - mx);
		int dy = Math.abs((y+barHeight+7)-my);
		
		if ((dx < 5) && (dy < 7))
			return true;
		
		return false;
	}
	
	/**
	 * Add a new control point
	 */
	private void addPoint() {
		var point = new ControlPoint(Color.WHITE, 0.5f);
		
		for (int i = 0; i < controlPoints.size() - 1; i++) {
			var now = controlPoints.get(i);
			var next = controlPoints.get(i+1);
			
			if ((now.getPosition() <= 0.5f) && (next.getPosition() >=0.5f)) {
				controlPoints.add(i+1,point);
				break;
			}
			
		}
		
		setSelected(point);
		sortPoints();
		repaint(0);
		
		fireUpdate();
	}
	
	/**
	 * Sort the control points based on their position
	 */
	private void sortPoints() {
		var firstPt = controlPoints.get(0);
		var lastPt  = controlPoints.get(controlPoints.size()-1);
		
		var compare = new Comparator<ControlPoint>() {
			@Override
			public int compare(ControlPoint first, ControlPoint second) {
				if (first == firstPt)
					return -1;
				if (second == lastPt)
					return -1;
				
				float a = first.getPosition();
				float b = second.getPosition();
				return (int) ((a-b) * 10000);
			}
		};
		
		Collections.sort(controlPoints, compare);
	}
	
	/**
	 * Edit the currently selected control point
	 */
	private void editPoint() {
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
	 * Select the control point at the specified mouse coordinate
	 * 
	 * @param mx The mouse x coordinate 
	 * @param my The mouse y coordinate
	 */
	private void selectPoint(int mx, int my) {
		if (!isEnabled())
			return;
		
		for (int i = 1; i < controlPoints.size() - 1; i++) {
			if (checkPoint(mx, my, controlPoints.get(i))) {
				setSelected(controlPoints.get(i));
				return;
			}
		}
		if (checkPoint(mx, my, controlPoints.get(0))) {
			setSelected(controlPoints.get(0));
			return;
		}
		if (checkPoint(mx, my, controlPoints.get(controlPoints.size() - 1))) {
			setSelected(controlPoints.get(controlPoints.size()-1));
			return;
		}
		
		setSelected(null);
	}
	
	private void setSelected(ControlPoint selected) {
		this.selected = selected;
		updateButtons();
	}
	
	private void updateButtons() {
		editBtn.setEnabled(selected != null);
		delBtn.setEnabled(selected != null
				&& selected != controlPoints.get(0)
				&& selected != controlPoints.get(controlPoints.size()-1));
	}

	/**
	 * Delete the currently selected point
	 */
	private void delPoint() {
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
		sortPoints();
		repaint(0);
		fireUpdate();
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
		if (controlPoints.indexOf(selected) == controlPoints.size()-1)
			return;
		
		float newPos = (mx - 10) / (float) width;
		newPos = Math.min(1, newPos);
		newPos = Math.max(0, newPos);
		
		selected.setPosition(newPos);
		sortPoints();
		fireUpdate();
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	public class ControlPoint {
		
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
		public int hashCode() {
			int prime = 11;
			int result = 5;
			result = prime * result + ((color == null) ? 0 : color.hashCode());
			result = prime * result + Float.floatToIntBits(position);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ControlPoint other = (ControlPoint) obj;
			if (color == null) {
				if (other.color != null)
					return false;
			} else if (!color.equals(other.color)) {
				return false;
			}
			if (Float.floatToIntBits(position) != Float.floatToIntBits(other.position))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getPosition() + ":" + ColorUtil.toHexString(getColor());
		}
	}
}
