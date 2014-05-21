package org.cytoscape.ding.internal.charts.box;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.Orientation;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.RectangleInsets;


public class BoxLayer extends AbstractChartLayer<BoxAndWhiskerCategoryDataset> {
	
	private final Orientation orientation;

	@SuppressWarnings("unchecked")
	public BoxLayer(final Map<String/*series*/, List<Double>/*values*/> data,
					final boolean showDomainAxis,
					final boolean showRangeAxis,
					final List<Color> colors,
					final DoubleRange range,
					final Orientation orientation,
					final Rectangle2D bounds) {
        super(data, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
        		false, showDomainAxis, showRangeAxis, colors, range, bounds);
        this.orientation = orientation;
	}
	
	@Override
	protected BoxAndWhiskerCategoryDataset createDataset() {
		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		
		for (String series : data.keySet()) {
			final List<Double> values = data.get(series);
			dataset.add(values, series, "1"); // switch series and category name so labels are displayed for series
		}
		
		return dataset;
	}
    
	@Override
	protected JFreeChart createChart(final BoxAndWhiskerCategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
					null, // chart title
					null, // domain axis label
					null, // range axis label
					dataset, // data
					false); // include legend
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setOutlineVisible(false);
		plot.setInsets(new RectangleInsets(2.0, 2.0, 1.0, 2.0));
		plot.setDomainGridlinesVisible(false);
	    plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		
		final PlotOrientation plotOrientation = 
				orientation == Orientation.HORIZONTAL ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
		plot.setOrientation(plotOrientation);
		
		final CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(showDomainAxis);
        domainAxis.setAxisLineVisible(showDomainAxis);
        domainAxis.setTickMarksVisible(false);
        domainAxis.setTickLabelsVisible(false);
        domainAxis.setCategoryMargin(.1);
        
        if (!showDomainAxis && !showRangeAxis) {
        	// Prevent bars from being cropped
	        domainAxis.setLowerMargin(.01);
	        domainAxis.setUpperMargin(.01);
        }
        
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(showRangeAxis);
        
		// Set axis range		
		if (range != null) {
			rangeAxis.setLowerBound(range.min);
			rangeAxis.setUpperBound(range.max);
		}
		
		final BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer) plot.getRenderer();
		renderer.setFillBox(true);
		renderer.setMeanVisible(false);
		renderer.setBaseItemLabelsVisible(false); // Box chart does not support item labels, anyway
		
		final List<?> keys = dataset.getRowKeys();
		
		if (colors != null && colors.size() >= keys.size()) {
			for (int i = 0; i < keys.size(); i++) {
				final Color c = colors.get(i);
				renderer.setSeriesPaint(i, c);
			}
		}
		
		return chart;
	}
}
