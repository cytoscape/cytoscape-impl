package org.cytoscape.ding.internal.charts.bar;

import javax.swing.Icon;

import org.cytoscape.ding.internal.charts.ViewUtils;
import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;

public class BarChartFactory implements CyChartFactory<BarLayer> {
	
	@Override
	public CyChart<BarLayer> getInstance(final String input) {
		return new BarChart(input);
	}

	@Override
	public CyChart<BarLayer> getInstance(final CyChart<BarLayer> chart) {
		return new BarChart((BarChart)chart);
	}
	
	@Override
	public CyChart<BarLayer> getInstance() {
		return new BarChart();
	}

	@Override
	public String getId() {
		return BarChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyChart<BarLayer>> getSupportedClass() {
		return BarChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Bar";
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return ViewUtils.resizeIcon(BarChart.ICON, width, height);
		
//		// Create RGB image with transparency channel
//		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		
//		final int pad = Math.min(Math.max(1, width / 10), Math.max(1, height/10));
//		width -= 2 * pad; // new width after paddings
//		height -= 2 * pad; // new height after paddings
//		final int axisWidth = Math.min(1, Math.round(width / 20));
//		final int bars = 3; // number of bars
//		final int barHeight = (int) Math.round(0.8f * (height - axisWidth) / bars); // all bars occupy 80% of the available height
//		final int barGap = (int) Math.round(0.2f * (height - axisWidth) / (bars - 1));
//		final int x1 = pad;
//		final int x2 = width + x1;
//		final int y1 = pad;
//		final int y2 = height + y1;		
//
//		// Create new graphics and set anti-aliasing hint
//		final Graphics2D g = (Graphics2D) img.getGraphics().create();
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		
//		// Draw bars
//		final float[] barScales = new float[]{ 1.0f, 0.5f, 0.75f };
//		final Color[] barColors = new Color[]{ Color.DARK_GRAY, Color.GRAY, Color.DARK_GRAY };
//		
//		for (int i = 0; i < bars; i++) {
//			final int gap = i * (barGap + barHeight);
//			final int barWidth = (int) Math.round(barScales[i] * (x2-x1));
//			g.setColor(barColors[i]);
//			g.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
//			g.fillRect(x1 + axisWidth, y1 + gap + axisWidth, barWidth, barHeight);
//		}
//		
//		// Draw Y-axis
//		g.setColor(Color.DARK_GRAY);
//		g.setStroke(new BasicStroke(axisWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
//		g.drawLine(x1, y1, x1, y2);
//		
//		// Dispose
//		g.dispose();
//		
//		return new ImageIcon(img);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
