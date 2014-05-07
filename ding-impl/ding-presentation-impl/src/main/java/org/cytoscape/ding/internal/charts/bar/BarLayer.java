package org.cytoscape.ding.internal.charts.bar;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.CustomCategoryItemLabelGenerator;
import org.cytoscape.ding.internal.charts.Orientation;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.TextAnchor;


public class BarLayer extends AbstractChartLayer<CategoryDataset> {
	
	private final boolean stacked;
	private final Orientation orientation;

	public BarLayer(final Map<String/*category*/, List<Double>/*values*/> data,
					final boolean stacked,
					final List<String> itemLabels,
					final List<String> domainLabels,
					final List<String> rangeLabels,
					final boolean showItemLabels,
					final boolean showDomainAxis,
					final boolean showRangeAxis,
					final List<Color> colors,
					final DoubleRange range,
					final Orientation orientation,
					final Rectangle2D bounds) {
        super(data, itemLabels, domainLabels, rangeLabels, showItemLabels, showDomainAxis, showRangeAxis, colors,
        		range, bounds);
        this.stacked = stacked;
        this.orientation = orientation;
	}
	
	@Override
	protected CategoryDataset createDataset() {
		return createCategoryDataset(data, false, domainLabels);
	}
    
	@Override
	protected JFreeChart createChart(final CategoryDataset dataset) {
		final PlotOrientation plotOrientation = 
				orientation == Orientation.HORIZONTAL ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
		final JFreeChart chart;
		
		if (stacked)
			chart = ChartFactory.createStackedBarChart(
					null, // chart title
					null, // domain axis label
					null, // range axis label
					dataset, // data
					plotOrientation,
					false, // include legend
					false, // tooltips
					false); // urls
		else
			chart = ChartFactory.createBarChart(
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
        domainAxis.setVisible(showDomainAxis);
        domainAxis.setAxisLineVisible(showDomainAxis);
        domainAxis.setTickMarksVisible(true);
        domainAxis.setTickLabelsVisible(true);
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
		
//		if (!showRangeAxis) {
//			// Prevent bars from being cropped
//	        rangeAxis.setLowerMargin(.5);
//	        rangeAxis.setUpperMargin(.5);
//        }
		
		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setBaseItemLabelPaint(domainAxis.getLabelPaint());
		renderer.setShadowVisible(false);
		renderer.setDrawBarOutline(true);
		renderer.setItemMargin(0.0);
		renderer.setBaseItemLabelGenerator(showItemLabels ? new CustomCategoryItemLabelGenerator(itemLabels) : null);
		renderer.setBaseItemLabelsVisible(showItemLabels);
		
		if (!stacked && showItemLabels) {
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, -Math.PI/2));
			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, -Math.PI/2));
		}
		
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
