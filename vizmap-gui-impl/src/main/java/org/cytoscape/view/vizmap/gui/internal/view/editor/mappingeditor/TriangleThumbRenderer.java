package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.ThumbRenderer;

/**
 * Custom Renderer for slider thumbs
 * 
 */
public final class TriangleThumbRenderer extends JComponent implements ThumbRenderer {

	private final static long serialVersionUID = 1202339877445372L;

	private final Color FOCUS_COLOR = UIManager.getColor("Focus.color");
	private final Color SELECTION_COLOR = UIManager.getColor("Table.focusCellBackground");
	private final Color DEFAULT_COLOR = UIManager.getColor("Label.foreground");;
	private final Color BACKGROUND_COLOR = UIManager.getColor("Table.background");

	private static final int STROKE_WIDTH = 1;
	
	// Keep the last selected thumb.
	private boolean selected;
	private int selectedIndex;
	private int currentIndex;

	public TriangleThumbRenderer() {
		setBackground(BACKGROUND_COLOR);
	}

	@Override
	protected void paintComponent(Graphics g) {
		var color = g.getColor();
		
		var g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int w = getWidth();
		int h = getHeight();
		
		if (selected || selectedIndex == currentIndex)
			g2d.setColor(SELECTION_COLOR);
		else
			g2d.setColor(DEFAULT_COLOR);
		
		// Outer triangle (border)
		var p1 = new Polygon();
		p1.addPoint(0, 0);
		p1.addPoint(w, 0);
		p1.addPoint(w / 2, h);
		g2d.fillPolygon(p1);

		// Inner triangle (fill color)
		var p2 = new Polygon();
		p2.addPoint(2 * STROKE_WIDTH, STROKE_WIDTH);
		p2.addPoint(w - 2 * STROKE_WIDTH, STROKE_WIDTH);
		p2.addPoint(w / 2, h - 2 * STROKE_WIDTH);
		g2d.setColor(color);
		g2d.fillPolygon(p2);
		
		g2d.dispose();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public JComponent getThumbRendererComponent(JXMultiThumbSlider slider, int index, boolean selected) {
		// Update state
		this.selected = selected;
		this.selectedIndex = slider.getSelectedIndex();
		this.currentIndex = index;

		final Object currentValue = slider.getModel().getThumbAt(index).getObject();

		if (currentValue.getClass() == Color.class) {
			setForeground((Color) currentValue);
		} else {
			if (selected || selectedIndex == currentIndex)
				setForeground(FOCUS_COLOR);
			else
				setForeground(DEFAULT_COLOR);
		}
		
		return this;
	}
}
