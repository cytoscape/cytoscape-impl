package org.cytoscape.ding.internal.charts.heatmap;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;


public class HeatMapLayer extends AbstractChartLayer<XYZDataset> {
	
	private final boolean showCategoryAxis;
	private final boolean showRangeAxis;
	
    final String[] xAxisLabels = {"a", "b", "c", "d", "e"}; //TODO: generalize

	public HeatMapLayer(final Map<String/*key*/, List<Double>/*z-values*/> data,
						final List<String> labels,
						final boolean showLabels,
						final List<Color> colors,
						final DoubleRange range,
						final boolean showCategoryAxis,
						final boolean showRangeAxis,
						final Rectangle2D bounds) {
        super(data, labels, showLabels, colors, range, bounds);
        this.showCategoryAxis = showCategoryAxis;
        this.showRangeAxis = showRangeAxis;
        
        if (range == null)
        	this.range = calculateRange(data.values(), false);
	}
	
	@Override
	protected XYZDataset createDataset() {
		final DefaultXYZDataset dataset = new DefaultXYZDataset();
		int maxSize = 0;
		
		for (final List<Double> values : data.values())
			maxSize = Math.max(maxSize, values.size());
		
		int x = 0;
		
		for (final Entry<String, List<Double>> series : data.entrySet()) {
			final String k = series.getKey();
			final List<Double> zValues = series.getValue();
			
			// Must be an array with length 3, containing three arrays of equal length,
			// - the first containing the x-values,
			// - the second containing the y-values
			// - and the third containing the z-values
			// { { x1, x2 }, { y1, y2 }, { z1, z2 } }
			
			final double[][] seriesData = new double[3][maxSize];
			Arrays.fill(seriesData[0], x);
			
			for (int y = 0; y < maxSize; y++) {
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
		final SymbolAxis xAxis = new SymbolAxis(null, xAxisLabels);
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);

		final SymbolAxis yAxis = new SymbolAxis(null, labels.toArray(new String[labels.size()]));
		yAxis.setLowerMargin(0.0);
		yAxis.setUpperMargin(0.0);
		yAxis.setInverted(true);

		final XYBlockRenderer renderer = new XYBlockRenderer();
		final PaintScale scale = new GrayPaintScale(range.min, range.max);
		renderer.setPaintScale(scale);

		final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setDomainAxis(1, xAxis);
        plot.setDomainAxisLocation(1, AxisLocation.TOP_OR_LEFT);
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(Color.WHITE);

		final JFreeChart chart = new JFreeChart(null, plot);
		chart.removeLegend();
		chart.setBackgroundPaint(TRANSPARENT_COLOR);
		
		return chart;
	}
	
//	static class SimpleColorScale implements PaintScale {
//		
//		private static final Color[] colors = {
//			new Color(36, 35, 105),
//			new Color(0, 6, 252),
//			new Color(0, 134, 250),
//			new Color(9, 251, 242),
//			new Color(135, 252, 112),
//			new Color(254, 241, 3),
//			new Color(255, 117, 0),
//			new Color(244, 0, 1),
//			new Color(104, 21, 21), };
//
//		private int lowerBound = 0;
//		private int upperBound = 100;
//
//		public SimpleColorScale(int lowerBound, int upperBound) {
//			this.lowerBound = lowerBound;
//			this.upperBound = upperBound;
//		}
//
//		@Override
//		public double getLowerBound() {
//			return lowerBound;
//		}
//
//		@Override
//		public double getUpperBound() {
//			return upperBound;
//		}
//
//		@Override
//		public Paint getPaint(double v) {
//			double divisor = (upperBound - lowerBound) / colors.length;
//			int index = (int) ((v - lowerBound) / divisor);
//			
//			if (index < 0)
//				return colors[0];
//			
//			if (index >= colors.length)
//				return colors[colors.length - 1];
//			
//			return colors[index];
//		}
//	}
}
