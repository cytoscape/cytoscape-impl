package org.cytoscape.cg.internal.charts.pie;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.cg.internal.charts.AbstractChartLayer;
import org.cytoscape.cg.internal.charts.CustomPieSectionLabelGenerator;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.cg.model.Rotation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;

public class PieLayer extends AbstractChartLayer<PieDataset> {
	
	/** Just to prevent the circle's border from being cropped */
	public static final double INTERIOR_GAP = 0.004;
	
	private final Map<String, String> labels;
	private final double startAngle;
	private final Rotation rotation;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public PieLayer(
			Map<String, List<Double>> data, 
			List<String> itemLabels, 
			boolean showLabels, 
			float itemFontSize,
			List<Color> colors, 
			float borderWidth, 
			Color borderColor, 
			double startAngle, 
			Rotation rotation
	) {
		super(data, itemLabels, null, null, showLabels, false, false, itemFontSize, LabelPosition.STANDARD, colors,
				0.0f, TRANSPARENT_COLOR, 0.0f, borderWidth, borderColor, null);
        this.startAngle = startAngle;
        this.rotation = rotation;
        this.labels = new HashMap<String, String>();
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected PieDataset createDataset() {
		var values = data.isEmpty() ? null : data.values().iterator().next();
		var dataset = createPieDataset(values);
		
		if (showItemLabels && itemLabels != null) {
			var keys = dataset.getKeys();
			
			for (int i = 0; i < keys.size(); i++) {
				var k = (String) keys.get(i);
				var label = itemLabels.size() > i ? itemLabels.get(i) : null;
				labels.put(k, label);
			}
        }
		
		return dataset;
	}
    
	@Override
	protected JFreeChart createChart(PieDataset dataset) {
		JFreeChart chart = ChartFactory.createPieChart(
				null, // chart title
				dataset, // data
				false, // include legend
				false, // tooltips
				false // urls
		);
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        
        var plot = (PiePlot) chart.getPlot();
		plot.setCircular(true);
		plot.setStartAngle(startAngle);
		plot.setDirection(rotation == Rotation.ANTICLOCKWISE ?
				org.jfree.util.Rotation.ANTICLOCKWISE : org.jfree.util.Rotation.CLOCKWISE);
		plot.setOutlineVisible(false);
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		plot.setInteriorGap(INTERIOR_GAP);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setShadowPaint(TRANSPARENT_COLOR);
		plot.setShadowXOffset(0.0);
		plot.setShadowYOffset(0.0);
		plot.setLabelGenerator(showItemLabels ? new CustomPieSectionLabelGenerator(labels) : null);
		plot.setSimpleLabels(true);
		plot.setLabelFont(plot.getLabelFont().deriveFont(itemFontSize));
		plot.setLabelBackgroundPaint(TRANSPARENT_COLOR);
		plot.setLabelOutlinePaint(TRANSPARENT_COLOR);
		plot.setLabelShadowPaint(TRANSPARENT_COLOR);
		plot.setLabelPaint(labelColor);
		
		var stroke = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		var keys = dataset.getKeys();
		
		for (int i = 0; i < keys.size(); i++) {
			var k = (String) keys.get(i);
			var c = colors.size() > i ? colors.get(i) : DEFAULT_ITEM_BG_COLOR;
			plot.setSectionPaint(k, c);
			plot.setSectionOutlinePaint(k, borderWidth > 0 ? borderColor : TRANSPARENT_COLOR);
			plot.setSectionOutlineStroke(k, stroke);
		}
		
		return chart;
	}
}
