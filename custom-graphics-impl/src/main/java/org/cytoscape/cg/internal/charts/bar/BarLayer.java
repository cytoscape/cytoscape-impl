package org.cytoscape.cg.internal.charts.bar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.List;
import java.util.Map;

import org.cytoscape.cg.internal.charts.AbstractChartLayer;
import org.cytoscape.cg.internal.charts.CustomCategoryItemLabelGenerator;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.cg.internal.charts.bar.BarChart.BarChartType;
import org.cytoscape.cg.internal.charts.util.ColorScale;
import org.cytoscape.cg.internal.util.EqualDashStroke;
import org.cytoscape.cg.model.Orientation;
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
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

public class BarLayer extends AbstractChartLayer<CategoryDataset> {
	
	private final BarChartType type;
	private final boolean showRangeZeroBaseline;
	private final double separation;
	private final Orientation orientation;
	private final boolean singleCategory;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarLayer(
			Map<String/* category */, List<Double>/* values */> data, 
			BarChartType type,
			List<String> itemLabels, 
			List<String> domainLabels, 
			List<String> rangeLabels, 
			boolean showItemLabels,
			boolean showDomainAxis, 
			boolean showRangeAxis, 
			boolean showRangeZeroBaseline, 
			float itemFontSize,
			LabelPosition domainLabelPosition, 
			List<Color> colors, 
			float axisWidth, 
			Color axisColor, 
			float axisFontSize,
			float borderWidth, 
			Color borderColor, 
			double separation, 
			List<Double> range, 
			Orientation orientation
	) {
        super(data, itemLabels, domainLabels, rangeLabels, showItemLabels, showDomainAxis, showRangeAxis, itemFontSize,
        		domainLabelPosition, colors, axisWidth, axisColor, axisFontSize, borderWidth, borderColor, range);
		this.type = type;
		this.showRangeZeroBaseline = showRangeZeroBaseline;
		this.separation = separation;
		this.orientation = orientation;
		singleCategory = data.size() == 1;

		// Range cannot be null
		if (type == BarChartType.HEAT_STRIPS &&
				(this.range == null || this.range.size() < 2 || this.range.get(0) == null || this.range.get(1) == null))
			this.range = calculateRange(data.values(), false);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected CategoryDataset createDataset() {
		var listIsSeries = (singleCategory && type != BarChartType.STACKED);
		
		return createCategoryDataset(data, listIsSeries, domainLabels);
	}
    
	@Override
	protected JFreeChart createChart(CategoryDataset dataset) {
		final PlotOrientation plotOrientation;
		
		if (orientation == null || orientation == Orientation.AUTO) {
			// Auto-orientation, based on the number of bars, usually when rendering table sparklines
			// (1 bar fits better in a table cell when horizontal; besides, two or more horizontal bars per cell
			// could make the visualization of confusing when comparing rows, specially if the table does not show the
			// internal grid lines to better separate the different charts)
			var colCount = dataset.getColumnCount();
			plotOrientation = colCount > 1 ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL;
		} else {
			plotOrientation = orientation == Orientation.HORIZONTAL ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
		}
		
		final JFreeChart chart;
		
		if (type == BarChartType.STACKED)
			chart = ChartFactory.createStackedBarChart(
					null, // chart title
					null, // domain axis label
					null, // range axis label
					dataset, // data
					plotOrientation,
					false, // include legend
					false, // tooltips
					false // urls
			);
		else
			chart = ChartFactory.createBarChart(
					null, // chart title
					null, // domain axis label
					null, // range axis label
					dataset, // data
					plotOrientation,
					false, // include legend
					false, // tooltips
					false // urls
			);
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        
        var plot = (CategoryPlot) chart.getPlot();
		plot.setOutlineVisible(false);
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		plot.setDomainGridlinesVisible(false);
	    plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		
		if (showRangeZeroBaseline) {
			plot.setRangeZeroBaselineVisible(true);
			plot.setRangeZeroBaselinePaint(axisColor);
			plot.setRangeZeroBaselineStroke(new EqualDashStroke(axisWidth));
		}
		
		var axisStroke = new BasicStroke(axisWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		var domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(showDomainAxis);
        domainAxis.setAxisLineStroke(axisStroke);
        domainAxis.setAxisLinePaint(axisColor);
        domainAxis.setTickMarkStroke(axisStroke);
        domainAxis.setTickMarkPaint(axisColor);
        domainAxis.setTickMarksVisible(true);
        domainAxis.setTickLabelsVisible(true);
        domainAxis.setTickLabelFont(domainAxis.getTickLabelFont().deriveFont(axisFontSize));
        domainAxis.setTickLabelPaint(axisColor);
        domainAxis.setCategoryLabelPositions(getCategoryLabelPosition());
        domainAxis.setCategoryMargin((type == BarChartType.STACKED || singleCategory) ? separation : 0.1);
        domainAxis.setLowerMargin(.025);
        domainAxis.setUpperMargin(.025);
        
		var rangeAxis = (NumberAxis) plot.getRangeAxis();
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
		if (range != null && range.size() >= 2 && range.get(0) != null && range.get(1) != null) {
			rangeAxis.setLowerBound(range.get(0));
			rangeAxis.setUpperBound(range.get(1));
		}
		
		if (type != BarChartType.STACKED) {
			if (type == BarChartType.HEAT_STRIPS || type == BarChartType.UP_DOWN) {
				final Color up =   (colors.size() > 0) ? colors.get(0) : Color.LIGHT_GRAY;
				final Color zero = (colors.size() > 2) ? colors.get(1) : Color.BLACK;
				final Color down = (colors.size() > 2) ? colors.get(2) : (colors.size() > 1 ? colors.get(1) : Color.GRAY);
				plot.setRenderer(new UpDownColorBarRenderer(up, zero, down));
			} else if (singleCategory) {
				plot.setRenderer(new SingleCategoryRenderer());
			}
		}
		
		var renderer = (BarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setShadowVisible(false);
		renderer.setDrawBarOutline(true);
		renderer.setBaseItemLabelGenerator(showItemLabels ? new CustomCategoryItemLabelGenerator(itemLabels) : null);
		renderer.setBaseItemLabelsVisible(showItemLabels);
		renderer.setBaseItemLabelFont(renderer.getBaseItemLabelFont().deriveFont(itemFontSize));
		renderer.setBaseItemLabelPaint(labelColor);
		renderer.setItemMargin(separation);
		
		if (type != BarChartType.STACKED && showItemLabels) {
			double angle = orientation == Orientation.HORIZONTAL ? 0 : -Math.PI/2;
			
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, angle));
			renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
					ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, angle));
		}
		
		var borderStroke = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		var keys = dataset.getRowKeys();
		
		for (int i = 0; i < keys.size(); i++) {
			renderer.setSeriesOutlineStroke(i, borderStroke);
			renderer.setSeriesOutlinePaint(i, borderWidth > 0 ? borderColor : TRANSPARENT_COLOR);
			
			if (type != BarChartType.UP_DOWN && type != BarChartType.HEAT_STRIPS) {
				Color c = DEFAULT_ITEM_BG_COLOR;
				
				if (colors != null && colors.size() > i)
					c = colors.get(i);
				
				renderer.setSeriesPaint(i, c);
			}
		}
		
		return chart;
	}

	// ==[ CLASSES ]====================================================================================================
	
	@SuppressWarnings("serial")
	class UpDownColorBarRenderer extends BarRenderer {

		private Color upColor;
		private Color zeroColor;
		private Color downColor;

		UpDownColorBarRenderer(Color up, Color zero, Color down) {
			this.upColor = up;
			this.zeroColor = zero;
			this.downColor = down;
		}
		
		@Override
		public Paint getItemPaint(int row, int column) {
			var dataset = getPlot().getDataset();
			var rowKey = (String) dataset.getRowKey(row);
			var colKey = (String) dataset.getColumnKey(column);
			var value = dataset.getValue(rowKey, colKey).doubleValue();
			
			if (type == BarChartType.HEAT_STRIPS) {
				if (Double.isNaN(value))
					return zeroColor;
				
				return ColorScale.getPaint(value, range.get(0), range.get(1), downColor, zeroColor, upColor);
			}
			
			return value < 0.0 ? downColor : upColor;
		}
	}
	
	@SuppressWarnings("serial")
	class SingleCategoryRenderer extends BarRenderer {

		@Override
        public Paint getItemPaint(int row, int column) {
            return (colors != null && colors.size() > column) ? colors.get(column) : DEFAULT_ITEM_BG_COLOR;
        }
    }
}
