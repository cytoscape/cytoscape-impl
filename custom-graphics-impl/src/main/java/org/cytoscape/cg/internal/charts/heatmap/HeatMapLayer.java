package org.cytoscape.cg.internal.charts.heatmap;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cytoscape.cg.internal.charts.AbstractChartLayer;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.cg.internal.charts.util.ColorScale;
import org.cytoscape.cg.model.Orientation;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleInsets;

public class HeatMapLayer extends AbstractChartLayer<XYZDataset> {
	
	private final Orientation orientation;
	private final String[] xLabels;
	private final String[] yLabels;
	private int maxYSize;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public HeatMapLayer(Map<String/*key*/, List<Double>/*z-values*/> data,
						List<String> itemLabels,
						List<String> domainLabels,
						List<String> rangeLabels,
						boolean showDomainAxis,
						boolean showRangeAxis,
						LabelPosition domainLabelPosition,
						List<Color> colors,
						Color axisColor,
						float axisFontSize,
						List<Double> range,
						Orientation orientation,
						Rectangle2D bounds) {
		super(data, itemLabels, domainLabels, rangeLabels, false, showDomainAxis, showRangeAxis, 0.0f,
				domainLabelPosition, colors, 0.0f, axisColor, axisFontSize, 0.0f, TRANSPARENT_COLOR, range, bounds);
        this.orientation = orientation;
        
        // Range cannot be null
        if (this.range == null)
        	this.range = calculateRange(data.values(), false);
        
        // x/y Labels are mandatory
        xLabels = new String[data.size()];
        int x = 0;
        
        for (var series : data.entrySet()) {
        	var k = series.getKey();
        	var zValues = series.getValue();
			
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
		var dataset = new DefaultXYZDataset();
		int x = 0;
		
		for (var series : data.entrySet()) {
			var k = series.getKey();
			var zValues = series.getValue();
			
			// Must be an array with length 3, containing three arrays of equal length,
			// - the first containing the x-values,
			// - the second containing the y-values
			// - and the third containing the z-values
			// { { x1, x2 }, { y1, y2 }, { z1, z2 } }
			
			var seriesData = new double[3][maxYSize];
			Arrays.fill(seriesData[0], x);
			
			for (int y = 0; y < maxYSize; y++) {
				double z = Double.NaN;
				
				if (zValues.size() > y && zValues.get(y) != null)
					 z = zValues.get(y);
				
				seriesData[1][y] = y;
				seriesData[2][y] = z;
			}
			
			dataset.addSeries(k, seriesData);
			x++;
		}
		
		return dataset;
	}
    
	@Override
	protected JFreeChart createChart(XYZDataset dataset) {
		var plotOrientation = 
				orientation == Orientation.HORIZONTAL ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
		
		var xAxis = new SymbolAxis(null, xLabels);
		xAxis.setVisible(showDomainAxis);
		xAxis.setAxisLineVisible(false);
		xAxis.setTickMarksVisible(false);
		xAxis.setTickLabelFont(xAxis.getLabelFont().deriveFont(axisFontSize));
		xAxis.setTickLabelPaint(axisColor);
		xAxis.setVerticalTickLabels(domainLabelPosition != LabelPosition.STANDARD);
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);

		var yAxis = new SymbolAxis(null, yLabels);
		yAxis.setVisible(showRangeAxis);
		yAxis.setAxisLineVisible(false);
		yAxis.setTickMarksVisible(false);
		yAxis.setTickLabelFont(yAxis.getLabelFont().deriveFont(axisFontSize));
		yAxis.setTickLabelPaint(axisColor);
		yAxis.setLowerMargin(0.0);
		yAxis.setUpperMargin(0.0);
		yAxis.setInverted(true);

		var renderer = new XYBlockRenderer();
		
		if (range != null && range.size() >= 2 && range.get(0) != null && range.get(1) != null) {
			int colorsSize = colors != null ? colors.size() : 0;
			
			var upperColor = colorsSize > 0 ? colors.get(0) : Color.BLUE;
			var zeroColor  = colorsSize > 1 ? colors.get(1) : Color.WHITE;
			var lowerColor = colorsSize > 2 ? colors.get(2) : Color.RED;
			var nanColor   = colorsSize > 3 ? colors.get(3) : Color.GRAY;
			
			var scale = new ColorScale(range.get(0), range.get(1), lowerColor, zeroColor, upperColor, nanColor);
			renderer.setPaintScale(scale);
		}

		var plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setDomainAxis(xAxis);
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
        plot.setOutlineVisible(false);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setInsets(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		plot.setOrientation(plotOrientation);

		var chart = new JFreeChart(null, plot);
		chart.removeLegend();
		chart.setBorderVisible(false);
		chart.setBackgroundPaint(TRANSPARENT_COLOR);
		chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		
		return chart;
	}
}
