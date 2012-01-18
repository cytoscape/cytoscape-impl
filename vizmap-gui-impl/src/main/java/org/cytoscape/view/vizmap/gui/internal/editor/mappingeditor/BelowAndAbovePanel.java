package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;


final class BelowAndAbovePanel extends JPanel {
	
	private final static long serialVersionUID = 1202339876961477L;
	private static final Stroke OUTLINE_STROKE = new BasicStroke(1.0f);
	
	private final VisualProperty<?> vp;
	private final boolean isBelow;

	private Color boxColor;
	
	private final ContinuousMapping<?, ?> mapping;
	private final GradientEditorPanel parentPanel;

	
	BelowAndAbovePanel(final ContinuousMappingEditorPanel parentPanel, final Color color, final boolean below, final ContinuousMapping<?, ?> mapping) {
		this.boxColor = color;
		this.isBelow = below;
		this.mapping = mapping;
		this.parentPanel = (GradientEditorPanel) parentPanel;
		this.vp = mapping.getVisualProperty();

		if (below)
			this.setToolTipText("Double-click triangle to set below color...");
		else
			this.setToolTipText("Double-click triangle to set above color...");

		this.addMouseListener(new MouseEventHandler(this));
	}


	public void setColor(Color newColor) {
		this.boxColor = newColor;
		this.repaint();
		this.getParent().repaint();
	}

	/**
	 * Draw custom panel component
	 */
	@Override
	public void paintComponent(Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		final Polygon poly = new Polygon();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setStroke(OUTLINE_STROKE);
		g2d.setColor(boxColor);

		if (isBelow) {
			poly.addPoint(18, 0);
			poly.addPoint(18, 20);
			poly.addPoint(0, 10);
		} else {
			poly.addPoint(0, 0);
			poly.addPoint(0, 20);
			poly.addPoint(18, 10);
			
		}

		g2d.fillPolygon(poly);
		g2d.setColor(Color.DARK_GRAY);
		g2d.draw(poly);
	}

	private final class MouseEventHandler extends MouseAdapter {
		private BelowAndAbovePanel caller;

		public MouseEventHandler(BelowAndAbovePanel c) {
			this.caller = c;
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				Object newValue = null;
				
				if (Paint.class.isAssignableFrom(vp.getRange().getType()) == false)
					return;

				newValue = parentPanel.colorEditor.showEditor(null, boxColor);
				if (newValue == null)
					return;
				
				caller.setColor((Color) newValue);
				

				BoundaryRangeValues brv;
				BoundaryRangeValues original;
				ContinuousMappingPoint<?, ?> originalPoint;
				
				if (isBelow) {
					originalPoint = mapping.getPoint(0);
					original = originalPoint.getRange();
					brv = new BoundaryRangeValues(newValue, original.equalValue, original.greaterValue);
				} else {
					originalPoint = mapping.getPoint(mapping.getPointCount() - 1);
					original = originalPoint.getRange();
					brv = new BoundaryRangeValues(original.lesserValue, original.equalValue, newValue);
				}
				originalPoint.setRange(brv);
				
				if (isBelow)
					parentPanel.below = (Color) newValue;
				else
					parentPanel.above = (Color) newValue;

				parentPanel.initSlider();
				parentPanel.updateView();
				
			}
		}
	}
}
