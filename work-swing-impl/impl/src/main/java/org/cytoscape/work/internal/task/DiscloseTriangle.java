package org.cytoscape.work.internal.task;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.Shape;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.List;
import java.util.ArrayList;

/**
 * A component that displays a triangle that, when clicked, discloses additional information.
 */
class DiscloseTriangle extends JComponent {
	protected static final float WIDTH = 11.0f;
	protected static final Color COLOR = new Color(0x999999);
	protected static final Color PRESSED_COLOR = new Color(0x666666);
	protected static final Shape CLOSED_TRIANGLE = closedTriangle();
	protected static final Shape OPENED_TRIANGLE = openedTriangle();

	protected static Shape closedTriangle() {
		final GeneralPath p = new GeneralPath();
		p.moveTo(0.0f, 0.0f);
		p.lineTo(WIDTH, WIDTH * 0.5f);
		p.lineTo(0.0f, WIDTH);
		p.closePath();
		return p;
	} 

	protected static Shape openedTriangle() {
		final GeneralPath p = new GeneralPath();
		p.moveTo(0.0f, 0.0f);
		p.lineTo(WIDTH, 0.0f);
		p.lineTo(WIDTH * 0.5f, WIDTH);
		p.closePath();
		return p;
	} 

	protected boolean opened = false;
	protected boolean pressed = false;

	List<ActionListener> listeners = new ArrayList<ActionListener>();

	public DiscloseTriangle() {
		final Dimension d = new Dimension((int) WIDTH, (int) WIDTH);
		super.setMinimumSize(d);
		super.setMaximumSize(d);
		super.setPreferredSize(d);

		super.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				pressed = true;
				repaint();
			}

			public void mouseReleased(MouseEvent e) {
				pressed = false;
				repaint();
			}

			public void mouseClicked(MouseEvent e) {
				opened = !opened;
				repaint();

				ActionEvent event = new ActionEvent(DiscloseTriangle.this, 0, "clicked");
				for (final ActionListener l : listeners)
					l.actionPerformed(event);
			}
		});
	}

	Insets insets = new Insets(0, 0, 0, 0); // this is a member so that a new Insets instance is not allocated every invocation of paintComponent

	public void paintComponent(Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		insets = super.getInsets(insets);
		g2d.translate(insets.left, insets.top);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(pressed ? PRESSED_COLOR : COLOR);
		g2d.fill(opened ? OPENED_TRIANGLE : CLOSED_TRIANGLE);
	}

	public boolean isOpen() {
		return opened;
	}

	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}
}