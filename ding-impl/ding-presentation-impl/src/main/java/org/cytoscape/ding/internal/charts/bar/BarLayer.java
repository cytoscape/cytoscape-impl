package org.cytoscape.ding.internal.charts.bar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.customgraphics.Orientation;
import org.cytoscape.ding.impl.strokes.EqualDashStroke;
import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.CustomCategoryItemLabelGenerator;
import org.cytoscape.ding.internal.charts.LabelPosition;
import org.cytoscape.ding.internal.charts.bar.BarChart.BarChartType;
import org.cytoscape.ding.internal.charts.util.ColorScale;
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

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class BarLayer extends AbstractChartLayer<CategoryDataset> {
	
	private final BarChartType type;
	private final boolean showRangeZeroBaseline;
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
					final boolean showRangeZeroBaseline,
					final float itemFontSize,
					final LabelPosition domainLabelPosition,
					final List<Color> colors,
					final float axisWidth,
					final Color axisColor,
					final float axisFontSize,
					final float borderWidth,
					final Color borderColor,
					final double separation,
					final List<Double> range,
					final Orientation orientation,
					final Rectangle2D bounds) {
        super(data, itemLabels, domainLabels, rangeLabels, showItemLabels, showDomainAxis, showRangeAxis, itemFontSize,
        		domainLabelPosition, colors, axisWidth, axisColor, axisFontSize, borderWidth, borderColor, range,
        		bounds);
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
		final boolean listIsSeries = (singleCategory && type != BarChartType.STACKED);
		
		return createCategoryDataset(data, listIsSeries, domainLabels);
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
		
		if (showRangeZeroBaseline) {
			plot.setRangeZeroBaselineVisible(true);
			plot.setRangeZeroBaselinePaint(axisColor);
			plot.setRangeZeroBaselineStroke(new EqualDashStroke(axisWidth));
		}
		
		final BasicStroke axisStroke = new BasicStroke(axisWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		final CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
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
		
		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
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
		
		final BasicStroke borderStroke = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		final List<?> keys = dataset.getRowKeys();
		
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
			
			if (type == BarChartType.HEAT_STRIPS) {
				if (Double.isNaN(value))
					return zeroColor;
				
				return ColorScale.getPaint(value, range.get(0), range.get(1), downColor, zeroColor, upColor);
			}
			
			return value < 0.0 ? downColor : upColor;
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
