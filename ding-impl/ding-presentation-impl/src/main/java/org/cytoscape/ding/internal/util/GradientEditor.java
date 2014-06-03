package org.cytoscape.ding.internal.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
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
import javax.swing.JColorChooser;
import javax.swing.JPanel;

import org.cytoscape.ding.internal.charts.ControlPoint;

/**
 * A generic editor for configuring a multiple point varying gradient.
 * Adapted from: 
 * https://www.assembla.com/code/ascent_game/git/node/blob/Slick/tools/org/newdawn/slick/tools/peditor/GradientEditor.java?raw=1&rev=c1b50b1c68bcb1d1eea464c7e507154ecf51e748
 *
 * @author kevin
 */
public class GradientEditor extends JPanel {
	
	private static final long serialVersionUID = -760260103076698312L;
	
	/** The controlPoints of control points */
	private List<ControlPoint> controlPoints = new ArrayList<ControlPoint>();
	/** The current selected control point */
	private ControlPoint selected;
	/** The polygon used for the markers */
	private Polygon poly = new Polygon();
	/** A button to add a control point */
	private JButton add = new JButton("Add");
	/** A button to edit a control point */
	private JButton edit = new JButton("Edit");
	/** A button to delete a control point */
	private JButton del = new JButton("Del");
	
	/** The x position of the gradient bar */
	private int x;
	/** The y position of the gradient bar */
	private int y;
	/** The width of the gradient bar */
	private int width;
	/** The height of the gradient bar */
	private int barHeight;
	
	/** The listeners that should be notified of changes to this emitter */
	private List<ActionListener> listeners = new ArrayList<ActionListener>();
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	/**
	 * Create a new editor for gradients
	 */
	public GradientEditor() {
		setLayout(null);
		
		add.setBounds(20, 70, 75, 20);
		add(add);
		edit.setBounds(100, 70, 75, 20);
		add(edit);
		del.setBounds(180, 70, 75, 20);
		add(del);
		
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addPoint();
			}
		});
		del.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				delPoint();
			}
		});
		edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editPoint();
			}
		});
		
		controlPoints.add(new ControlPoint(Color.WHITE, 0));
		controlPoints.add(new ControlPoint(Color.BLACK, 1));
		
		poly.addPoint(0, 0);
		poly.addPoint(5, 10);
		poly.addPoint(-5, 10);
		
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectPoint(e.getX(), e.getY());
				repaint(0);
				
				if (e.getClickCount() == 2) {
					editPoint();
				}
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
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
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		Component[] components = getComponents();
		
		for (int i = 0; i < components.length; i++)
			components[i].setEnabled(enabled);
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
		
		Graphics2D g = (Graphics2D) g1d;
		width = getWidth() - 30;
		x = 10;
		y = 20;
		barHeight = 25;
		
		for (int i = 0; i < controlPoints.size() - 1; i++) {
			ControlPoint now = controlPoints.get(i);
			ControlPoint next = controlPoints.get(i+1);
			
			int size = (int) ((next.position - now.position) * width);
			g.setPaint(new GradientPaint(x,y,now.color,x+size,y,next.color));
			g.fillRect(x, y, size + 1, barHeight);
			x += size;
		}
		
		g.setColor(Color.BLACK);
		g.drawRect(10, y, width, barHeight - 1);
		
		for (int i = 0; i < controlPoints.size(); i++) {
			ControlPoint pt = controlPoints.get(i);
			g.translate(10+(width * pt.position),y+barHeight);
			g.setColor(pt.color);
			g.fillPolygon(poly);
			g.setColor(Color.BLACK);
			g.drawPolygon(poly);
			
			if (pt == selected)
				g.drawLine(-5, 12, 5, 12);
			
			g.translate(-10-(width * pt.position),-y-barHeight);
		}
	}
	
	/**
	 * Add a control point to the gradient
	 * 
	 * @param position The position in the gradient (0 -> 1)
	 * @param color The color at the new control point
	 */
	public void addPoint(float pos, Color col) {
		ControlPoint point = new ControlPoint(col, pos);
		
		for (int i = 0; i < controlPoints.size() - 1; i++) {
			ControlPoint now = controlPoints.get(i);
			ControlPoint next = controlPoints.get(i+1);
			
			if ((now.position <= 0.5f) && (next.position >=0.5f)) {
				controlPoints.add(i+1,point);
				break;
			}
		}
		
		repaint(0);
	}
	
	/**
	 * Set the starting colour
	 * 
	 * @param color The color at the start of the gradient
	 */
	public void setStart(Color col) {
		(controlPoints.get(0)).color = col;
		repaint(0);
	}

	/**
	 * Set the ending colour
	 * 
	 * @param color The color at the end of the gradient
	 */
	public void setEnd(Color col) {
		(controlPoints.get(controlPoints.size()-1)).color = col;
		repaint(0);
	}
	
	public Color getStart() {
		return controlPoints.size() > 0 ? controlPoints.get(0).color : null;
	}
	
	public Color getEnd() {
		return controlPoints.size() > 0 ? controlPoints.get(controlPoints.size()-1).color : null;
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
		return controlPoints.get(index).position;
	}

	/**
	 * Get the color of the control point at the specified
	 * index.
	 *  
	 * @param index The index of the control point
	 * @return The color of the control point
	 */
	public Color getColor(int index) {
		return controlPoints.get(index).color;
	}
	
	public List<ControlPoint> getControlPoints() {
		return new ArrayList<ControlPoint>(controlPoints);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	/**
	 * Fire an update to all listeners
	 */
	private void fireUpdate() {
		ActionEvent event = new ActionEvent(this,0,"");
		
		for (int i=0; i < listeners.size(); i++)
			listeners.get(i).actionPerformed(event);
	}
	
	/**
	 * Check if there is a control point at the specified mouse location
	 * 
	 * @param mx The mouse x coordinate 
	 * @param my The mouse y coordinate
	 * @param pt The point to check agianst
	 * @return True if the mouse point conincides with the control point
	 */
	private boolean checkPoint(int mx, int my, ControlPoint pt) {
		int dx = (int) Math.abs((10+(width * pt.position)) - mx);
		int dy = Math.abs((y+barHeight+7)-my);
		
		if ((dx < 5) && (dy < 7))
			return true;
		
		return false;
	}
	
	/**
	 * Add a new control point
	 */
	private void addPoint() {
		ControlPoint point = new ControlPoint(Color.WHITE, 0.5f);
		
		for (int i = 0; i < controlPoints.size() - 1; i++) {
			ControlPoint now = controlPoints.get(i);
			ControlPoint next = controlPoints.get(i+1);
			
			if ((now.position <= 0.5f) && (next.position >=0.5f)) {
				controlPoints.add(i+1,point);
				break;
			}
			
		}
		selected = point;
		sortPoints();
		repaint(0);
		
		fireUpdate();
	}
	
	/**
	 * Sort the control points based on their position
	 */
	private void sortPoints() {
		final ControlPoint firstPt = controlPoints.get(0);
		final ControlPoint lastPt  = controlPoints.get(controlPoints.size()-1);
		
		final Comparator<ControlPoint> compare = new Comparator<ControlPoint>() {
			public int compare(ControlPoint first, ControlPoint second) {
				if (first == firstPt)
					return -1;
				if (second == lastPt)
					return -1;
				
				float a = first.position;
				float b = second.position;
				return (int) ((a-b) * 10000);
			}
		};
		
		Collections.sort(controlPoints, compare);
	}
	
	/**
	 * Edit the currently selected control point
	 *
	 */
	private void editPoint() {
		if (selected == null)
			return;
		
		Color col = JColorChooser.showDialog(this, "Select Color", selected.color);
		
		if (col != null) {
			selected.color = col;
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
		if (!isEnabled()) {
			return;
		}
		
		for (int i=1;i<controlPoints.size()-1;i++) {
			if (checkPoint(mx,my,controlPoints.get(i))) {
				selected = controlPoints.get(i);
				return;
			}
		}
		if (checkPoint(mx,my,controlPoints.get(0))) {
			selected = controlPoints.get(0);
			return;
		}
		if (checkPoint(mx,my,controlPoints.get(controlPoints.size()-1))) {
			selected = controlPoints.get(controlPoints.size()-1);
			return;
		}
		
		selected = null;
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
		
		selected.position = newPos;
		sortPoints();
		fireUpdate();
	}
}