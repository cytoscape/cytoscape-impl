package org.cytoscape.work.internal.task;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.RoundRectangle2D;
import java.awt.RenderingHints;

public class RoundedProgressBar extends JComponent {
	protected static final float CORNER_RADIUS = 5.2f;
	protected static final float HEIGHT = 6.0f;
	protected static final Color FG_COLOR = new Color(0x499e55);
	protected static final Color BK_COLOR = new Color(0xd3d3d3);

	public RoundedProgressBar() {
		super.setMinimumSize(new Dimension(50, (int) HEIGHT));
		super.setMaximumSize(new Dimension(10000, (int) HEIGHT));
		super.setPreferredSize(new Dimension(250, (int) HEIGHT));
	}

	float value = 0.0f;

	public void setValue(final float value) {
		if (value > 1.0f)
			this.value = 1.0f;
		else if (value < 0.0f)
			this.value = 0.0f;
		else
			this.value = value;
		super.repaint();
	}

	public float getValue() {
		return value;
	}

	Insets insets = new Insets(0, 0, 0, 0);
	RoundRectangle2D.Float bkRect = new RoundRectangle2D.Float();
	RoundRectangle2D.Float fgRect = new RoundRectangle2D.Float();

	public void paintComponent(Graphics g) {
		insets = super.getInsets(insets);
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float x = insets.left;
		final float y = insets.top;
		final float w = getEffectiveWidth() - insets.right - insets.left;
		final float h = HEIGHT;

		bkRect.setRoundRect(x, y, w, h, CORNER_RADIUS, CORNER_RADIUS);
		g2d.setColor(BK_COLOR);
		g2d.fill(bkRect);

		fgRect.setRoundRect(x, y, w * value, h, CORNER_RADIUS, CORNER_RADIUS);
		g2d.setColor(FG_COLOR);
		g2d.fill(fgRect);
	}

	private int getEffectiveWidth() {
		final int w = super.getWidth();
		final int maxW = (int) super.getMaximumSize().getWidth();
		final int minW = (int) super.getMinimumSize().getWidth();

		if (w > maxW)
			return maxW;
		else if (w < minW)
			return minW;
		else
			return w;
	}
}