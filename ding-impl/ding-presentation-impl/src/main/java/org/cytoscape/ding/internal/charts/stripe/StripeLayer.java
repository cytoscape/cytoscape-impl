package org.cytoscape.ding.internal.charts.stripe;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.Orientation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;


public class StripeLayer extends AbstractChartLayer<CategoryDataset> {
	
	private final Orientation orientation;

	public StripeLayer(final Map<String/*category*/, List<Double>/*values*/> data,
					   final List<String> itemLabels,
					   final boolean showItemLabels,
					   final List<Color> colors,
					   final Orientation orientation,
					   final Rectangle2D bounds) {
        super(data, itemLabels, null, null, showItemLabels, false, false, colors, null, bounds);
        this.orientation = orientation;
	}
	
	@Override
	protected CategoryDataset createDataset() {
		return createCategoryDataset(data, false, itemLabels);
	}
    
	@Override
	protected JFreeChart createChart(final CategoryDataset dataset) {
		// The actual bar orientation is inverted here, because we will use a stacked bar chart
		final PlotOrientation plotOrientation = 
				orientation == Orientation.HORIZONTAL ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL;
		// Use stacked bar chart so itemLabels are automatically centered inside the bars.
		final JFreeChart chart = ChartFactory.createStackedBarChart(
				null, // chart title
				null, // domain axis label
				null, // range axis label
				dataset, // data
				plotOrientation,
				false, // include legend
				false, // tooltips
				false); // urls
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBorderPaint(TRANSPARENT_COLOR);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setOutlineVisible(false);
		plot.setOutlinePaint(TRANSPARENT_COLOR);
		plot.setDomainGridlinePaint(TRANSPARENT_COLOR);
		plot.setDomainGridlinesVisible(false);
	    plot.setRangeGridlinePaint(TRANSPARENT_COLOR);
	    plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		
		final CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(false);
        domainAxis.setAxisLineVisible(false);
//        domainAxis.setCategoryMargin(.1);
        // Prevent bars from being cropped
//	    domainAxis.setLowerMargin(.01);
//	    domainAxis.setUpperMargin(.01);
        
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(false);
		
		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setBaseItemLabelGenerator(showItemLabels ? new StandardCategoryItemLabelGenerator() : null);
		renderer.setBaseItemLabelsVisible(showItemLabels);
		renderer.setBaseItemLabelPaint(domainAxis.getLabelPaint());
		renderer.setShadowVisible(false);
		renderer.setDrawBarOutline(true);
		renderer.setItemMargin(0.0);
		
		final List<?> keys = dataset.getRowKeys();
		
		if (colors != null && colors.size() >= keys.size()) {
			for (int i = 0; i < keys.size(); i++) {
				final Color c = colors.get(i);
				renderer.setSeriesFillPaint(i, c);
			}
		}
		
		return chart;
	}
}
