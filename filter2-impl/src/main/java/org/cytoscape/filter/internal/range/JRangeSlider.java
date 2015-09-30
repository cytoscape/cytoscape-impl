package org.cytoscape.filter.internal.range;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * <p>Implements a Swing-based Range slider, which allows the user to enter a
 * range (minimum and maximum) value.</p>
 *
 * @author Ben Bederson
 * @author Jesse Grosjean
 * @author Jon Meyer
 * @author Lance Good
 * @author jeffrey heer
 * @author Christian Lopes
 */
@SuppressWarnings("serial")
public class JRangeSlider extends JComponent implements MouseListener, MouseMotionListener,
                                                        KeyListener, SwingConstants {
	/*
	 * NOTE: This is a modified version of the original class distributed by
	 * Ben Bederson, Jesse Grosjean, and Jon Meyer as part of an HCIL Tech
	 * Report.  It is modified to allow both vertical and horizontal modes.
	 * It also fixes a bug with offset on the buttons. Also fixed a bug with
	 * rendering using (x,y) instead of (0,0) as origin.  Also modified to
	 * render arrows as a series of lines rather than as a GeneralPath.
	 * Also modified to fix rounding errors on toLocal and toScreen.
	 *
	 * With inclusion in prefuse, this class has been further modified to use a
	 * bounded range model, support keyboard commands and more extensize
	 * parameterization of rendering/appearance options. Furthermore, a stub
	 * method has been introduced to allow subclasses to perform custom
	 * rendering within the slider through.
	 */
	final protected static int HPAD = 2; // Horizontal pad when orientation is HORIZONTAL
	final protected static int VPAD = 4; // Vertical pad when orientation is HORIZONTAL
	final protected static int THUMB_SIZE = 14;
	final protected static int TRACK_THICKNESS = 4;
	
	protected BoundedRangeModel model;
	protected int orientation;
	protected int increment = 1;
	protected int minExtent = 0; // min extent, in pixels
	protected ArrayList<ChangeListener> listeners = new ArrayList<>();
	protected ChangeEvent changeEvent;
	protected ChangeListener lstnr;
	protected final Color trackColor1 = UIManager.getColor("Panel.background").darker();
	protected final Color trackColor2 = UIManager.getColor("Panel.background");
	protected final Color rangeColor1 = UIManager.getColor("TextField.background");
	protected final Color rangeColor2 = UIManager.getColor("TextField.selectionBackground");
	protected final Color thumbColor1 = UIManager.getColor("TextField.background");
	protected final Color thumbColor2 = UIManager.getColor("Button.background");
	protected final Color borderColor = UIManager.getColor("Label.disabledForeground");
	protected final Color disabledBorderColor = UIManager.getColor("Label.disabledForeground");
	protected final Color rangeBorderColor = rangeColor2.darker();

	// ------------------------------------------------------------------------

	/**
	 * Create a new range slider with a {@link NumberRangeModel}.
	 *
	 * @param minimum - the minimum value of the range.
	 * @param maximum - the maximum value of the range.
	 * @param lowValue - the current low value shown by the range slider's bar.
	 * @param highValue - the current high value shown by the range slider's bar.
	 * @param orientation - construct a horizontal or vertical slider?
	 */
//	public JRangeSlider(int minimum, int maximum, int lowValue, int highValue, int orientation) {
//		this(new NumberRangeModel(lowValue, highValue, minimum, maximum), orientation);
//	}

	/**
	 * Create a new range slider.
	 *
	 * @param model - a BoundedRangeModel specifying the slider's range
	 * @param orientation - construct a horizontal or vertical slider?
	 */
	public JRangeSlider(BoundedRangeModel model, int orientation) {
		super.setFocusable(true);
		this.model = model;
		this.orientation = orientation;

		if (orientation == VERTICAL)
			setMinimumSize(new Dimension(THUMB_SIZE + 2 * VPAD, 6 * THUMB_SIZE + 2 * HPAD));
		else
			setMinimumSize(new Dimension(6 * THUMB_SIZE + 2 * HPAD, THUMB_SIZE + VPAD * 2));

		setPreferredSize(getMinimumSize());
		
		this.lstnr = createListener();
		model.addChangeListener(lstnr);

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}

	/**
	 * Create a listener to relay change events from the bounded range model.
	 * @return a ChangeListener to relay events from the range model
	 */
	protected ChangeListener createListener() {
		return new RangeSliderChangeListener();
	}

	/**
	 * Listener that fires a change event when it receives  change event from
	 * the slider list model.
	 */
	protected class RangeSliderChangeListener implements ChangeListener {
		
		@Override
		public void stateChanged(ChangeEvent e) {
			fireChangeEvent();
		}
	}

	/**
	 * Returns the current "low" value shown by the range slider's bar. The low
	 * value meets the constraint minimum <= lowValue <= highValue <= maximum.
	 */
	public int getLowValue() {
		return model.getValue();
	}

	/**
	 * Sets the low value shown by this range slider. This causes the range slider to be
	 * repainted and a ChangeEvent to be fired.
	 * @param lowValue the low value to use
	 */
	public void setLowValue(int lowValue) {
		int e = (model.getValue() - lowValue) + model.getExtent();
		model.setRangeProperties(lowValue, e, model.getMinimum(), model.getMaximum(), false);
		model.setValue(lowValue);
	}

	/**
	 * Returns the current "high" value shown by the range slider's bar. The high
	 * value meets the constraint minimum <= lowValue <= highValue <= maximum.
	 */
	public int getHighValue() {
		return model.getValue() + model.getExtent();
	}

	/**
	 * Sets the high value shown by this range slider. This causes the range slider to be
	 * repainted and a ChangeEvent to be fired.
	 * @param highValue the high value to use
	 */
	public void setHighValue(int highValue) {
		model.setExtent(highValue - model.getValue());
	}

	/**
	 * Set the slider range span.
	 * @param lowValue the low value of the slider range
	 * @param highValue the high value of the slider range
	 */
	public void setRange(int lowValue, int highValue) {
		model.setRangeProperties(lowValue, highValue - lowValue, model.getMinimum(),
		                         model.getMaximum(), false);
	}

	/**
	 * Gets the minimum possible value for either the low value or the high value.
	 * @return the minimum possible range value
	 */
	public int getMinimum() {
		return model.getMinimum();
	}

	/**
	 * Sets the minimum possible value for either the low value or the high value.
	 * @param minimum the minimum possible range value
	 */
	public void setMinimum(int minimum) {
		model.setMinimum(minimum);
	}

	/**
	 * Gets the maximum possible value for either the low value or the high value.
	 * @return the maximum possible range value
	 */
	public int getMaximum() {
		return model.getMaximum();
	}

	/**
	 * Sets the maximum possible value for either the low value or the high value.
	 * @param maximum the maximum possible range value
	 */
	public void setMaximum(int maximum) {
		model.setMaximum(maximum);
	}

	/**
	 * Sets the minimum extent (difference between low and high values).
	 * This method <strong>does not</strong> change the current state of the
	 * model, but can affect all subsequent interaction.
	 * @param minExtent the minimum extent allowed in subsequent interaction
	 */
	public void setMinExtent(int minExtent) {
		this.minExtent = minExtent;
	}

	/**
	 * Get the BoundedRangeModel backing this slider.
	 * @return the slider's range model
	 */
	public BoundedRangeModel getModel() {
		return model;
	}

	/**
	 * Set the BoundedRangeModel backing this slider.
	 * @param brm the slider range model to use
	 */
	public void setModel(BoundedRangeModel brm) {
		model.removeChangeListener(lstnr);
		model = brm;
		model.addChangeListener(lstnr);
		repaint();
	}

	/**
	 * Registers a listener for ChangeEvents.
	 * @param cl the ChangeListener to add
	 */
	public void addChangeListener(ChangeListener cl) {
		if (!listeners.contains(cl))
			listeners.add(cl);
	}

	/**
	 * Removes a listener for ChangeEvents.
	 * @param cl the ChangeListener to remove
	 */
	public void removeChangeListener(ChangeListener cl) {
		listeners.remove(cl);
	}

	/**
	 * Fire a change event to all listeners.
	 */
	protected void fireChangeEvent() {
		repaint();

		if (changeEvent == null)
			changeEvent = new ChangeEvent(this);

		Iterator<ChangeListener> iter = listeners.iterator();

		while (iter.hasNext())
			iter.next().stateChanged(changeEvent);
	}

	// ------------------------------------------------------------------------
	// Rendering

	/**
	 * Override this method to perform custom painting of the slider trough.
	 * @param g a Graphics2D context for rendering
	 * @param width the width of the slider trough
	 * @param height the height of the slider trough
	 */
	protected void customPaint(Graphics2D g, int width, int height) {
		// does nothing in this class
		// subclasses can override to perform custom painting
	}

	@Override
	public void paintComponent(Graphics g) {
		final boolean vertical = orientation == VERTICAL;
		
		// Working bounds (excludes padding)
		Rectangle bounds = getBounds();
		final int width = (int) Math.round(bounds.getWidth() - (2 * (vertical ? VPAD : HPAD)));
		final int height = (int) Math.round(bounds.getHeight() - (2 * (vertical ? HPAD : VPAD)));

		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int x = trackX();
		int y = trackY();
		int w, h;
		
		// Draw track
		if (vertical) {
			w = TRACK_THICKNESS;
			h = height - THUMB_SIZE;
		} else {
			w = width - THUMB_SIZE;
			h = TRACK_THICKNESS;
		}
		
		paintTrack(
				g2,
				trackColor1,
				trackColor2,
				isEnabled() ? borderColor : disabledBorderColor,
				x, y, w, h
		);

		customPaint(g2, width, height);
		
		// Draw range backgrounds
		int min = toScreen(getLowValue());
		int max = toScreen(getHighValue());
		
		if (vertical) {
			y = min;
			h = max - y;
		} else {
			x = min;
			w = max - x;
		}
		
		paintTrack(
				g2,
				rangeColor1,
				isEnabled() ? rangeColor2 : trackColor2,
				isEnabled() ? rangeBorderColor : disabledBorderColor,
				x, y, w, h
		);
		
		// Draw thumbs
		w = h = THUMB_SIZE;
		
		if (vertical) {
			x = (int) Math.round((bounds.getWidth() - THUMB_SIZE) / 2.0);
			y = y - THUMB_SIZE;
		} else {
			x = x - THUMB_SIZE;
			y = (int) Math.round((bounds.getHeight() - THUMB_SIZE) / 2.0);
		}
		
		paintThumb(
				g2,
				isEnabled() ? thumbColor1 : thumbColor2,
				thumbColor2,
				x, y, w, h
		);
		
		if (vertical)
			y = max;
		else
			x = max;
		
		paintThumb(
				g2,
				isEnabled() ? thumbColor1 : thumbColor2,
				thumbColor2,
				x, y, w, h
		);
	}
	
	private int trackX() {
		return orientation == VERTICAL ?
				(int) Math.round((getBounds().getWidth() - TRACK_THICKNESS) / 2.0) :
				(int) Math.round(HPAD + THUMB_SIZE / 2.0);
	}
	
	private int trackY() {
		return orientation == VERTICAL ?
				(int) Math.round(HPAD + THUMB_SIZE / 2.0) :
				(int) Math.round((getBounds().getHeight() - TRACK_THICKNESS) / 2.0);
	}
	
	private void paintTrack(final Graphics g, final Color color1, final Color color2, final Color borderColor,
			int x, int y, int w, int h) {
		final Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		final Point p1 = orientation == VERTICAL ? new Point(x, y) : new Point(x, y);
		final Point p2 = orientation == VERTICAL ? new Point(x + w, y) : new Point(x, y + h);
		final Paint p = new GradientPaint(p1, color1, p2, color2);
		
		final int arc = 4;
		
		// Background
		g2.setPaint(p);
		g2.fillRoundRect(x, y, w, h, arc, arc);
		// Border
		g2.setColor(borderColor);
		g2.setStroke(new BasicStroke(1));
		g2.drawRoundRect(x, y, w, h, arc, arc);
		
		g2.dispose();
	}
	
	private void paintThumb(final Graphics g, final Color color1, final Color color2, int x, int y, int w, int h) {
		final Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		final Point p1 = new Point(x, y);
		final Point p2 = new Point(x, y + h);
		final Paint p = new GradientPaint(p1, color1, p2, color2);
		
		// Background
		g2.setPaint(p);
		g2.fillOval(x, y, w, h);
		// Border
		g2.setColor(isEnabled() ? borderColor : disabledBorderColor);
		g2.setStroke(new BasicStroke(1));
		g2.drawOval(x, y, w, h);
		g2.dispose();
	}

	/**
	 * Converts from screen coordinates to a range value.
	 */
	protected int toLocal(int xOrY) {
		final Dimension d = getSize();
		final int min = getMinimum();
		final int max = getMaximum();
		double length = orientation == VERTICAL ? d.height : d.width;
		double scale = (length - (2 * HPAD) - THUMB_SIZE) / (double) (max - min);

		return (int) (((xOrY - THUMB_SIZE) / scale) + min + 0.5);
	}

	/**
	 * Converts from a range value to screen coordinates.
	 */
	protected int toScreen(int xOrY) {
		final Dimension d = getSize();
		final int min = getMinimum();
		final int max = getMaximum();
		double length = orientation == VERTICAL ? d.height : d.width;
		double scale = (length - (2 * HPAD) - (2 * THUMB_SIZE)) / (double) (max - min);

		return (int) (HPAD + THUMB_SIZE + ((xOrY - min) * scale));
	}
	
	// ------------------------------------------------------------------------
	// Event Handling
	static final int PICK_NONE = 0;
	static final int PICK_THUMB_1 = 1; // Left or Top Thumb
	static final int PICK_RANGE = 2; // Range Track
	static final int PICK_THUMB_2 = 3; // Right or Bottom Thumb
	int pick;
	int pickOffsetLow;
	int pickOffsetHigh;
	int mouse;

	private int pickHandle(int xOrY) {
		int min = toScreen(getLowValue());
		int max = toScreen(getHighValue());
		int pick = PICK_NONE;

		if (xOrY >= (min - THUMB_SIZE) && xOrY <= min)
			pick = PICK_THUMB_1;
		else if (xOrY > min && xOrY < max)
			pick = PICK_RANGE;
		else if (xOrY >= max && xOrY <= (max + THUMB_SIZE))
			pick = PICK_THUMB_2;

		return pick;
	}

	private void offset(int dxOrDy) {
		model.setValue(model.getValue() + dxOrDy);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!isEnabled())
			return;
		
		if (orientation == VERTICAL) {
			pick = pickHandle(e.getY());
			pickOffsetLow = e.getY() - toScreen(getLowValue());
			pickOffsetHigh = e.getY() - toScreen(getHighValue());
			mouse = e.getY();
		} else {
			pick = pickHandle(e.getX());
			pickOffsetLow = e.getX() - toScreen(getLowValue());
			pickOffsetHigh = e.getX() - toScreen(getHighValue());
			mouse = e.getX();
		}

		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!isEnabled())
			return;
		
		requestFocus();
		int value = (orientation == VERTICAL) ? e.getY() : e.getX();

		int minimum = getMinimum();
		int maximum = getMaximum();
		int lowValue = getLowValue();
		int highValue = getHighValue();

		switch (pick) {
			case PICK_THUMB_1:

				int low = toLocal(value - pickOffsetLow);

				if (low < minimum)
					low = minimum;
				if (low > maximum)
					low = maximum;
				if (low > (highValue - minExtent))
					low = highValue - minExtent;

				setLowValue(low);
				break;

			case PICK_THUMB_2:

				int high = toLocal(value - pickOffsetHigh);

				if (high < minimum)
					high = minimum;
				if (high > maximum)
					high = maximum;
				if (high < (lowValue + minExtent))
					high = lowValue + minExtent;

				setHighValue(high);
				break;

			case PICK_RANGE:

				int dxOrDy = toLocal(value - pickOffsetLow) - lowValue;

				if ((dxOrDy < 0) && ((lowValue + dxOrDy) < minimum))
					dxOrDy = minimum - lowValue;
				if ((dxOrDy > 0) && ((highValue + dxOrDy) > maximum))
					dxOrDy = maximum - highValue;
				if (dxOrDy != 0)
					offset(dxOrDy);

				break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (isEnabled()) {
			pick = PICK_NONE;
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!isEnabled())
			return;
		
		if (orientation == VERTICAL) {
			switch (pickHandle(e.getY())) {
				case PICK_THUMB_1:
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;

				case PICK_THUMB_2:
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;

				case PICK_RANGE:
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;

				case PICK_NONE:
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;
			}
		} else {
			switch (pickHandle(e.getX())) {
				case PICK_THUMB_1:
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;

				case PICK_THUMB_2:
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;

				case PICK_RANGE:
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;

				case PICK_NONE:
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	private void grow(int increment) {
		model.setRangeProperties(model.getValue() - increment, model.getExtent() + (2 * increment),
		                         model.getMinimum(), model.getMaximum(), false);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!isEnabled())
			return;
		
		int kc = e.getKeyCode();
		boolean v = (orientation == VERTICAL);
		boolean d = (kc == KeyEvent.VK_DOWN);
		boolean u = (kc == KeyEvent.VK_UP);
		boolean l = (kc == KeyEvent.VK_LEFT);
		boolean r = (kc == KeyEvent.VK_RIGHT);

		int minimum = getMinimum();
		int maximum = getMaximum();
		int lowValue = getLowValue();
		int highValue = getHighValue();

		if ((v && r) || (!v && u)) {
			if (((lowValue - increment) >= minimum) && ((highValue + increment) <= maximum)) {
				grow(increment);
			}
		} else if ((v && l) || (!v && d)) {
			if ((highValue - lowValue) >= (2 * increment)) {
				grow(-1 * increment);
			}
		} else if ((v && d) || (!v && l)) {
			if ((lowValue - increment) >= minimum) {
				offset(-increment);
			}
		} else if ((v && u) || (!v && r)) {
			if ((highValue + increment) <= maximum) {
				offset(increment);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	
//	public static void main(String... s) {
//		JFrame frame = new JFrame();
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.getContentPane().setLayout(new BorderLayout());
//		
//		final JRangeSlider jrs = new JRangeSlider(0, 100, 50, 75, HORIZONTAL);
////		jrs.setEnabled(false);
//
//		frame.getContentPane().add(jrs);
//
//		frame.pack();
//		frame.setVisible(true);
//	}
}
