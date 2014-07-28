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
import org.cytoscape.ding.internal.charts.bar.BarChart.BarChartType;
import org.cytoscape.ding.internal.util.MathUtil;
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
	
	private final boolean upAndDown;
	private final BarChartType type;
	private final double separation;
	private final Orientation orientation;
	private final boolean singleCategory;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarLayer(final Map<String/*category*/, List<Double>/*values*/> data,
					final BarChartType type,
					final List<String> itemLabels,
					final List<String> domainLabels,
					final List<String> rangeLabels,
					final boolean showItemLabels,
					final boolean showDomainAxis,
					final boolean showRangeAxis,
					final List<Color> colors,
					final boolean upAndDown,
					final double separation,
					final DoubleRange range,
					final Orientation orientation,
					final Rectangle2D bounds) {
        super(data, itemLabels, domainLabels, rangeLabels, showItemLabels, showDomainAxis, showRangeAxis, colors,
        		range, bounds);
		this.upAndDown = upAndDown;
		this.type = type;
		this.separation = separation;
		this.orientation = orientation;
		singleCategory = data.size() == 1;

		if (type == BarChartType.HEAT_STRIPS && this.range == null) // Range cannot be null
			this.range = calculateRange(data.values(), false);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected CategoryDataset createDataset() {
		final boolean listIsSeries = (singleCategory && type != BarChartType.STACKED);
		final List<String> labels = listIsSeries ? itemLabels : domainLabels;
		
		return createCategoryDataset(data, listIsSeries, labels);
	}
    
	@Override
	protected JFreeChart createChart(final CategoryDataset dataset) {
		final PlotOrientation plotOrientation = 
				orientation == Orientation.HORIZONTAL ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
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
		
		// Show item labels and there is only one category?
        final boolean showItemLabelsAsDomain = showItemLabels && singleCategory;
		
		final CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(showDomainAxis || showItemLabelsAsDomain);
        domainAxis.setAxisLineVisible(showDomainAxis && !showItemLabelsAsDomain);
        domainAxis.setAxisLineStroke(axisStroke);
        domainAxis.setAxisLinePaint(axisColor);
        domainAxis.setTickMarkStroke(axisStroke);
        domainAxis.setTickMarkPaint(axisColor);
        domainAxis.setTickMarksVisible(showDomainAxis && !showItemLabelsAsDomain);
        domainAxis.setTickLabelsVisible(true);
        domainAxis.setTickLabelFont(domainAxis.getTickLabelFont().deriveFont(axisFontSize));
        domainAxis.setTickLabelPaint(axisColor);
        domainAxis.setCategoryMargin((type == BarChartType.STACKED || singleCategory) ? separation : 0.1);
        
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
		
		if (type != BarChartType.STACKED) {
			if (type == BarChartType.HEAT_STRIPS || upAndDown) {
				final Color up =   (colors.size() > 0) ? colors.get(0) : Color.LIGHT_GRAY;
				final Color zero = (colors.size() > 2) ? colors.get(1) : Color.BLACK;
				final Color down = (colors.size() > 2) ? colors.get(2) : (colors.size() > 1 ? colors.get(1) : Color.GRAY);
				plot.setRenderer(new UpDownColorBarRenderer(up, zero, down));
			} else if (singleCategory) {
				plot.setRenderer(new SingleCategoryRenderer());
			}
		}
		
		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setShadowVisible(false);
		renderer.setDrawBarOutline(true);
		renderer.setBaseItemLabelGenerator(
				(showItemLabels && !showItemLabelsAsDomain) ?
				new CustomCategoryItemLabelGenerator(itemLabels) : null);
		renderer.setBaseItemLabelsVisible(showItemLabels);
		renderer.setBaseItemLabelFont(renderer.getBaseItemLabelFont().deriveFont(labelFontSize));
		renderer.setBaseItemLabelPaint(labelColor);
		renderer.setItemMargin(separation);
		
		if (type != BarChartType.STACKED && showItemLabels) {
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
			
			if (type == BarChartType.STACKED || !upAndDown) {
				Color c = DEFAULT_ITEM_BG_COLOR;
				
				if (colors != null && colors.size() > i)
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
		private Color zeroColor;
		private Color downColor;

		UpDownColorBarRenderer(final Color up, final Color zero, Color down) {
			this.upColor = up;
			this.zeroColor = zero;
			this.downColor = down;
		}
		
		@Override
		public Paint getItemPaint(final int row, final int column) {
			final CategoryDataset dataset = getPlot().getDataset();
			final String rowKey = (String) dataset.getRowKey(row);
			final String colKey = (String) dataset.getColumnKey(column);
			final double value = dataset.getValue(rowKey, colKey).doubleValue();
			final Color color = value < 0.0 ? downColor : upColor;
			
			if (type == BarChartType.HEAT_STRIPS) {
				// Linearly interpolate the value
				final double f = value < 0.0 ?
						MathUtil.invLinearInterp(value, range.min, 0) : MathUtil.invLinearInterp(value, 0, range.max);
				final double t = value < 0.0 ?
						MathUtil.linearInterp(f, 0.0, 1.0) : MathUtil.linearInterp(f, 1.0, 0.0);
				
				return org.jdesktop.swingx.color.ColorUtil.interpolate(zeroColor, color, (float)t);
			}
			
			return color;
		}
	}
	
	class SingleCategoryRenderer extends BarRenderer {

		private static final long serialVersionUID = 1138264028943798008L;

		@Override
        public Paint getItemPaint(final int row, final int column) {
            return (colors != null && colors.size() > column) ? colors.get(column) : DEFAULT_ITEM_BG_COLOR;
        }
    }
}
