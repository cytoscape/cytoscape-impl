package org.cytoscape.ding.internal.charts.stripe;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.CustomCategoryItemLabelGenerator;
import org.cytoscape.ding.internal.charts.Orientation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;


public class StripeLayer extends AbstractChartLayer<CategoryDataset> {
	
	private final Orientation orientation;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public StripeLayer(final Map<String/*category*/, List<Double>/*values*/> data,
					   final List<String> itemLabels,
					   final boolean showItemLabels,
					   final List<Color> colors,
					   final Orientation orientation,
					   final Rectangle2D bounds) {
        super(data, itemLabels, null, null, showItemLabels, false, false, colors, null, bounds);
        this.orientation = orientation;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
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
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setOutlineVisible(false);
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		plot.setDomainGridlinesVisible(false);
	    plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		
		final CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(false);
        domainAxis.setAxisLineVisible(false);
	    domainAxis.setLowerMargin(0.0);
	    domainAxis.setUpperMargin(0.0);
        
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(false);
		rangeAxis.setLowerMargin(0.0);
		rangeAxis.setUpperMargin(0.0);
		
		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setBaseItemLabelGenerator(showItemLabels ? new CustomCategoryItemLabelGenerator(itemLabels) : null);
		renderer.setBaseItemLabelsVisible(showItemLabels);
		renderer.setBaseItemLabelFont(renderer.getBaseItemLabelFont().deriveFont(labelFontSize));
		renderer.setBaseItemLabelPaint(labelColor);
		renderer.setShadowVisible(false);
		renderer.setDrawBarOutline(true);
		renderer.setItemMargin(0.0);
		
		final BasicStroke stroke =
				new BasicStroke((float)borderWidth/LINE_WIDTH_FACTOR, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		final List<?> keys = dataset.getRowKeys();
		
		for (int i = 0; i < keys.size(); i++) {
			renderer.setSeriesOutlineStroke(i, stroke);
			renderer.setSeriesOutlinePaint(i, borderColor);
			
			if (colors != null && colors.size() >= keys.size())
				renderer.setSeriesPaint(i, colors.get(i));
		}
		
		return chart;
	}
}
