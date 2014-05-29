package org.cytoscape.ding.internal.charts.bar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
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
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;


public class BarLayer extends AbstractChartLayer<CategoryDataset> {
	
	private final boolean upAndDown;
	private final boolean stacked;
	private final Orientation orientation;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarLayer(final Map<String/*category*/, List<Double>/*values*/> data,
					final boolean stacked,
					final List<String> itemLabels,
					final List<String> domainLabels,
					final List<String> rangeLabels,
					final boolean showItemLabels,
					final boolean showDomainAxis,
					final boolean showRangeAxis,
					final List<Color> colors,
					final boolean upAndDown,
					final DoubleRange range,
					final Orientation orientation,
					final Rectangle2D bounds) {
        super(data, itemLabels, domainLabels, rangeLabels, showItemLabels, showDomainAxis, showRangeAxis, colors,
        		range, bounds);
        this.upAndDown = upAndDown;
        this.stacked = stacked;
        this.orientation = orientation;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
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
//		final AxisSpace das = new AxisSpace();
//		das.setTop(1.5);
//		das.setRight(0.0);
//		das.setLeft(0.0);
//		das.setBottom(0.5);
//		plot.setFixedDomainAxisSpace(das);
		
		final BasicStroke axisStroke =
				new BasicStroke((float)axisWidth/LINE_WIDTH_FACTOR, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		final CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(showDomainAxis);
        domainAxis.setAxisLineStroke(axisStroke);
        domainAxis.setAxisLinePaint(axisColor);
        domainAxis.setTickMarkStroke(axisStroke);
        domainAxis.setTickMarkPaint(axisColor);
        domainAxis.setTickLabelsVisible(true);
        domainAxis.setTickLabelFont(domainAxis.getTickLabelFont().deriveFont(axisFontSize));
        domainAxis.setTickLabelPaint(axisColor);
        domainAxis.setCategoryMargin(.1);
        
//        if (!showDomainAxis && !showRangeAxis) {
//        	// Prevent bars from being cropped
//	        domainAxis.setLowerMargin(.01);
//	        domainAxis.setUpperMargin(.01);
//        }
        
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(showRangeAxis);
		rangeAxis.setAxisLineStroke(axisStroke);
		rangeAxis.setAxisLinePaint(axisColor);
		rangeAxis.setTickMarkStroke(axisStroke);
		rangeAxis.setTickMarkPaint(axisColor);
		rangeAxis.setTickLabelFont(rangeAxis.getLabelFont().deriveFont(axisFontSize).deriveFont(Font.PLAIN));
		rangeAxis.setTickLabelPaint(axisColor);
		rangeAxis.setLowerMargin(0.1);
		rangeAxis.setUpperMargin(0.1);
		
		// Set axis range		
		if (range != null) {
			rangeAxis.setLowerBound(range.min);
			rangeAxis.setUpperBound(range.max);
		}
		
//		if (!showRangeAxis) {
//			// Prevent bars from being cropped
//	        rangeAxis.setLowerMargin(.01);
//	        rangeAxis.setUpperMargin(.01);
//        }
		
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		
		if (upAndDown && !stacked) {
			final Color up = colors.size() > 0 ? colors.get(0) : Color.LIGHT_GRAY;
			final Color down = colors.size() > 1 ? colors.get(colors.size() - 1) : Color.DARK_GRAY;
			renderer = new UpDownColorBarRenderer(up, down);
			plot.setRenderer(renderer);
		}
		
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setShadowVisible(false);
		renderer.setDrawBarOutline(true);
		renderer.setItemMargin(0.0);
		renderer.setBaseItemLabelGenerator(showItemLabels ? new CustomCategoryItemLabelGenerator(itemLabels) : null);
		renderer.setBaseItemLabelsVisible(showItemLabels);
		renderer.setBaseItemLabelFont(renderer.getBaseItemLabelFont().deriveFont(labelFontSize));
		renderer.setBaseItemLabelPaint(labelColor);
		
		if (!stacked && showItemLabels) {
			double angle = orientation == Orientation.HORIZONTAL ? 0 : -Math.PI/2;
			
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, angle));
			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, angle));
		}
		
		
		final BasicStroke borderStroke =
				new BasicStroke((float)borderWidth/LINE_WIDTH_FACTOR, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		final List<?> keys = dataset.getRowKeys();
		
		for (int i = 0; i < keys.size(); i++) {
			renderer.setSeriesOutlineStroke(i, borderStroke);
			renderer.setSeriesOutlinePaint(i, borderColor);
			
			if (stacked || !upAndDown) {
				Color c = Color.LIGHT_GRAY;
				
				if (colors.size() > i)
					c = colors.get(i);
				
				renderer.setSeriesPaint(i, c);
			}
		}
		
		return chart;
	}

	// ==[ CLASSES ]====================================================================================================
	
	class UpDownColorBarRenderer extends BarRenderer {

		private static final long serialVersionUID = -1827868101222293644L;
		
		private Color upColor;
		private Color downColor;

		UpDownColorBarRenderer(final Color up, final Color down) {
			this.upColor = up;
			this.downColor = down;
		}
		
		@Override
		public Paint getItemPaint(final int row, final int column) {
			final CategoryDataset dataset = getPlot().getDataset();
			final String rowKey = (String) dataset.getRowKey(row);
			final String colKey = (String) dataset.getColumnKey(column);
			final double value = dataset.getValue(rowKey, colKey).doubleValue();
			
			return (value < 0.0) ? downColor : upColor;
		}
	}
}
