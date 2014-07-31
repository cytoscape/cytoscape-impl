package org.cytoscape.ding.internal.charts.heatmap;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.cytoscape.ding.internal.charts.util.ColorScale;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleInsets;


public class HeatMapLayer extends AbstractChartLayer<XYZDataset> {
	
	private final String[] xLabels;
	private final String[] yLabels;
	private int maxYSize;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public HeatMapLayer(final Map<String/*key*/, List<Double>/*z-values*/> data,
						final List<String> itemLabels,
						final List<String> domainLabels,
						final List<String> rangeLabels,
						final boolean showDomainAxis,
						final boolean showRangeAxis,
						final List<Color> colors,
						final Color axisColor,
						final DoubleRange range,
						final Rectangle2D bounds) {
        super(data, itemLabels, domainLabels, rangeLabels, false, showDomainAxis, showRangeAxis, colors,
        		0.0, axisColor, 0.0, TRANSPARENT_COLOR, range, bounds);
        
        // Range cannot be null
        if (this.range == null)
        	this.range = calculateRange(data.values(), false);
        
        // x/y Labels are mandatory
        xLabels = new String[data.size()];
        int x = 0;
        
        for (final Entry<String, List<Double>> series : data.entrySet()) {
        	final String k = series.getKey();
			final List<Double> zValues = series.getValue();
			
			maxYSize = Math.max(maxYSize, zValues.size());
			xLabels[x] = (domainLabels != null && domainLabels.size() > x) ? domainLabels.get(x) : k;
			x++;
        }
        
        yLabels = new String[maxYSize];
        
        for (int i = 0; i < maxYSize; i++) {
        	yLabels[i] = (rangeLabels != null && rangeLabels.size() > i) ? rangeLabels.get(i) : ""+(i+1);
        }
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected XYZDataset createDataset() {
		final DefaultXYZDataset dataset = new DefaultXYZDataset();
		int x = 0;
		
		for (final Entry<String, List<Double>> series : data.entrySet()) {
			final String k = series.getKey();
			final List<Double> zValues = series.getValue();
			
			// Must be an array with length 3, containing three arrays of equal length,
			// - the first containing the x-values,
			// - the second containing the y-values
			// - and the third containing the z-values
			// { { x1, x2 }, { y1, y2 }, { z1, z2 } }
			
			final double[][] seriesData = new double[3][maxYSize];
			Arrays.fill(seriesData[0], x);
			
			for (int y = 0; y < maxYSize; y++) {
				final double z = zValues.size() > y ? zValues.get(y) : range.min;
				seriesData[1][y] = y;
				seriesData[2][y] = z;
			}
			
			dataset.addSeries(k, seriesData);
			x++;
		}
		
		return dataset;
	}
    
	@Override
	protected JFreeChart createChart(final XYZDataset dataset) {
		final SymbolAxis xAxis = new SymbolAxis(null, xLabels);
		xAxis.setVisible(showDomainAxis);
		xAxis.setAxisLineVisible(false);
		xAxis.setTickMarksVisible(false);
		xAxis.setTickLabelFont(xAxis.getLabelFont().deriveFont(axisFontSize));
		xAxis.setTickLabelPaint(axisColor);
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);

		final SymbolAxis yAxis = new SymbolAxis(null, yLabels);
		yAxis.setVisible(showRangeAxis);
		yAxis.setAxisLineVisible(false);
		yAxis.setTickMarksVisible(false);
		yAxis.setTickLabelFont(yAxis.getLabelFont().deriveFont(axisFontSize));
		yAxis.setTickLabelPaint(axisColor);
		yAxis.setLowerMargin(0.0);
		yAxis.setUpperMargin(0.0);
		yAxis.setInverted(true);

		final XYBlockRenderer renderer = new XYBlockRenderer();
		
		if (range != null) {
			final Color color1 = colors != null && colors.size() > 0 ? colors.get(0) : Color.BLACK;
			final Color color2 = colors != null && colors.size() > 1 ? colors.get(1) : Color.WHITE;
			final ColorScale scale = new ColorScale(range.min, range.max, color1, color2);
			renderer.setPaintScale(scale);
		}

		final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setDomainAxis(xAxis);
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);
        plot.setOutlineVisible(false);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setInsets(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));

		final JFreeChart chart = new JFreeChart(null, plot);
		chart.removeLegend();
		chart.setBorderVisible(false);
		chart.setBackgroundPaint(TRANSPARENT_COLOR);
		chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		
		return chart;
	}
}
