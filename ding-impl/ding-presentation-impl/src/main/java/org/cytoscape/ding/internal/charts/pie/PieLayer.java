package org.cytoscape.ding.internal.charts.pie;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

public class PieLayer extends AbstractChartLayer<PieDataset> {
	
	public PieLayer(final Map<String, List<Double>> data,
					final List<String> labels,
					final boolean showLabels,
					final List<Color> colors,
					final Rectangle2D bounds) {
        super(data, labels, null, null, showLabels, false, false, colors, null, bounds);
	}
	
	@Override
	protected PieDataset createDataset() {
		final List<Double> values = data.isEmpty() ? null : data.values().iterator().next();
		return createPieDataset(values, itemLabels);
	}
    
	@Override
	protected JFreeChart createChart(final PieDataset dataset) {
		JFreeChart chart = ChartFactory.createPieChart(
				null, // chart title
				dataset, // data
				false, // include legend
				false, // tooltips
				false); // urls
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBorderPaint(TRANSPARENT_COLOR);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        
		final PiePlot plot = (PiePlot) chart.getPlot();
		plot.setCircular(true);
//		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setOutlineVisible(false);
		plot.setOutlinePaint(TRANSPARENT_COLOR);
		plot.setLabelGenerator(showItemLabels ? new StandardPieSectionLabelGenerator() : null);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setShadowPaint(TRANSPARENT_COLOR);
		plot.setShadowXOffset(0);
		plot.setShadowYOffset(0);
		
		final List<?> keys = dataset.getKeys();
		
		if (colors != null && colors.size() >= keys.size()) {
			for (int i = 0; i < keys.size(); i++) {
				final String k = (String) keys.get(i);
				final Color c = colors.get(i);
				plot.setSectionPaint(k, c);
			}
		}
		
		return chart;
	}
}
