package org.cytoscape.work.internal.task;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class RoundedProgressBar extends JComponent {
	
	protected static final float CORNER_RADIUS = 5.2f;
	protected static final float HEIGHT = 6.0f;
	protected static final float INDET_BAR_WIDTH = 100.0f;
	protected static final int INDET_UPDATE_MS = 50;
	protected static final double INDET_UPDATE_INCREMENT = 0.015;
	
	boolean indet;
	Timer indetUpdate;
	double indetPosition;
	float progress;
	
	Insets insets = new Insets(0, 0, 0, 0);
	RoundRectangle2D.Float bkRect = new RoundRectangle2D.Float();
	RoundRectangle2D.Float fgRect = new RoundRectangle2D.Float();
	
	final Color bgColor = UIManager.getColor("Separator.foreground");
	final Color fgColor = LookAndFeelUtil.GO_COLOR;

	public RoundedProgressBar() {
		final int h = (int) Math.ceil(HEIGHT);
		super.setMinimumSize(new Dimension(50, h));
		super.setMaximumSize(new Dimension(10000, h));
		super.setPreferredSize(new Dimension(250, h));
	}

	public void setProgress(final float progress) {
		indet = false;
		if (indetUpdate != null) {
			indetUpdate.stop();
			indetUpdate = null;
		}
		if (progress > 1.0f)
			this.progress = 1.0f;
		else if (progress < 0.0f)
			this.progress = 0.0f;
		else
			this.progress = progress;
		super.repaint();
	}

	public void setIndeterminate() {
		if (indet)
			return;
		indet = true;
		indetUpdate = new Timer(INDET_UPDATE_MS, new IndetUpdate());
		indetUpdate.start();
	}

	protected static double indetPositionFunc(double t) {
		//return 0.5 * (Math.cos(Math.PI * (2.0 * t + 1.0)) + 1);
		if (t > 0.5) {
			return -2.0 * (t - 1.0);
		} else {
			return 2.0 * t;
		}
	}

	public Float getProgress() {
		return (indet ? null : progress);
	}

	@Override
	public void paintComponent(Graphics g) {
		insets = super.getInsets(insets);
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float x = insets.left;
		final float y = insets.top;
		final float w = getEffectiveWidth() - insets.right - insets.left;
		final float h = HEIGHT;

		bkRect.setRoundRect(x, y, w, h, CORNER_RADIUS, CORNER_RADIUS);
		g2d.setColor(bgColor);
		g2d.fill(bkRect);

		if (indet) {
			final float nw = (INDET_BAR_WIDTH * 2.0 > w) ? (w * 0.2f) : INDET_BAR_WIDTH;
			final float nx = (float) (x + (w - nw) * indetPositionFunc(indetPosition));
			fgRect.setRoundRect(nx, y, nw, h, CORNER_RADIUS, CORNER_RADIUS);
			g2d.setColor(fgColor);
		} else {
			fgRect.setRoundRect(x, y, w * progress, h, CORNER_RADIUS, CORNER_RADIUS);
			g2d.setColor(fgColor);
		}
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

	class IndetUpdate implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			indetPosition += INDET_UPDATE_INCREMENT;
			if (indetPosition > 1.0)
				indetPosition = 0.0;
			repaint();
		}
	}
}
