package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.ThumbRenderer;

/**
 * Custom Renderer for slider thumbs
 * 
 */
public final class TriangleThumbRenderer extends JComponent implements ThumbRenderer {

	private final static long serialVersionUID = 1202339877445372L;

	private static final Color SELECTED_COLOR = Color.red;
	private static final Color DEFAULT_COLOR = Color.DARK_GRAY;
	private static final Color BACKGROUND_COLOR = Color.white;

	private static final Stroke DEF_STROKE = new BasicStroke(1.0f);
	private static final Stroke SELECTED_STROKE = new BasicStroke(2.0f);

	private boolean selected;
	
	public TriangleThumbRenderer() {
		super();
		setBackground(BACKGROUND_COLOR);
	}

	@Override
	protected void paintComponent(Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (selected) {
			final Polygon outline = new Polygon();
			outline.addPoint(0, 0);
			outline.addPoint(0, 4);
			outline.addPoint(4, 9);
			outline.addPoint(8, 4);
			outline.addPoint(8, 0);
			g.fillPolygon(outline);
			g.setColor(SELECTED_COLOR);
			g2d.setStroke(SELECTED_STROKE);
			g.drawPolygon(outline);
		} else {
			final Polygon thumb = new Polygon();
			thumb.addPoint(0, 0);
			thumb.addPoint(10, 0);
			thumb.addPoint(5, 10);
			g.fillPolygon(thumb);

			final Polygon outline = new Polygon();
			outline.addPoint(0, 0);
			outline.addPoint(9, 0);
			outline.addPoint(5, 9);
			g.setColor(Color.DARK_GRAY);
			g2d.setStroke(DEF_STROKE);
			g.drawPolygon(outline);
		}
	}

	@Override
	public JComponent getThumbRendererComponent(@SuppressWarnings("rawtypes") JXMultiThumbSlider slider, int index,
			boolean selected) {
		this.selected = selected;

		final Object obj = slider.getModel().getThumbAt(index).getObject();
		if (obj.getClass() == Color.class)
			this.setForeground((Color) obj);
		else {
			if (selected)
				this.setForeground(SELECTED_COLOR);
			else
				this.setForeground(DEFAULT_COLOR);
		}
		return this;
	}
}
