package org.cytoscape.cg.internal.charts.ring;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.cg.internal.charts.AbstractChartLayer;
import org.cytoscape.cg.internal.charts.CustomPieSectionLabelGenerator;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.cg.internal.charts.pie.PieLayer;
import org.cytoscape.cg.model.Rotation;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.RingPlot;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;

public class RingLayer extends AbstractChartLayer<PieDataset> {
	
	private List<PieDataset> datasetList;
    private List<JFreeChart> chartList;
    
    private final Map<String, String> labels;
    /** The angle in degrees to start the pie. 0 points east, 90 points south, etc. */
    private final double startAngle;
    /** Where to begin the first circle, as a proportion of the entire node */
    private final double hole;
    private final Rotation rotation;
	
    // ==[ CONSTRUCTORS ]===============================================================================================
    
	public RingLayer(
			Map<String, List<Double>> data, 
			List<String> labels, 
			boolean showLabels,
			float itemFontSize, 
			List<Color> colors, 
			float borderWidth, 
			Color borderColor,
			double startAngle, 
			double hole, 
			Rotation rotation
	) {
        super(data, labels, null, null, showLabels, false, false, itemFontSize, LabelPosition.STANDARD, colors, 0.0f,
        		TRANSPARENT_COLOR, 0.0f, borderWidth, borderColor, null);
        this.startAngle = startAngle;
        this.hole = hole;
        this.rotation = rotation;
        this.labels = new HashMap<String, String>();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void draw(Graphics2D g, Shape shape, CyNetworkView networkView, View<? extends CyIdentifiable> view) {
		getChart(); // Make sure charts have been created
		
		if (chartList.size() == 1) {
			super.draw(g, shape, networkView, view);
		} else {
	        for (var chart : chartList)
	        	chart.draw(g, bounds);
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected PieDataset createDataset() {
		datasetList = new ArrayList<PieDataset>();
		
		for (var category : data.keySet()) {
			var values = data.get(category);
			var ds = createPieDataset(values);
			
			if (ds != null)
				datasetList.add(ds);
		}
		
		if (showItemLabels && itemLabels != null && datasetList.size() == 1) {
			var keys = datasetList.get(0).getKeys();
			
			for (int i = 0; i < keys.size(); i++) {
				var k = (String) keys.get(i);
				var label = itemLabels.size() > i ? itemLabels.get(i) : null;
				labels.put(k, label);
			}
        }
		
		return datasetList.isEmpty() ? null : datasetList.get(0);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected JFreeChart getChart() {
		if (datasetList == null || chartList == null) {
			createDataset();
			int total = datasetList.size();
			chartList = new ArrayList<JFreeChart>(total);
			
			if (total > 0) {
				int count = 0;
				
				for (var ds : datasetList) {
					double sectionDepth = ((1.0 - hole) / (double)total) * (total - count);
					var chart = createChart(ds, sectionDepth, PieLayer.INTERIOR_GAP);
					chartList.add(chart);
					count++;
				}
			} else {
				// Just to show the "no data" text
				var chart = createChart(createPieDataset(Collections.EMPTY_LIST), 1.0, 0.0);
				chartList.add(chart);
			}
		}
		
		return chartList == null || chartList.isEmpty() ? null : chartList.get(0);
	}
    
	@Override
	protected JFreeChart createChart(PieDataset dataset) {
		return createChart(dataset, 0, 0);
	}
	
	@Override
	protected BufferedImage createImage(Rectangle2D r) {
		getChart(); // Make sure charts have been created
		
		if (chartList.size() == 1)
			return super.createImage(r);
		
		var nr = validateBounds(r);
		var img = new BufferedImage(nr.width, nr.height, BufferedImage.TYPE_INT_ARGB);
		var g = img.getGraphics();
        
        // Get a buffered image of each chart converting the background color to transparent
        for (var chart : chartList) {
        	var transpImg = transformColorToTransparency(
        			chart.createBufferedImage(nr.width, nr.height), TRANSPARENT_COLOR, TRANSPARENT_COLOR);
        	g.drawImage(transpImg, 0, 0, null);
        }
        
        return img;
	}
	
    private JFreeChart createChart(PieDataset dataset, double sectionDepth, double interiorGap) {
    	JFreeChart chart = ChartFactory.createRingChart(
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
        
        var plot = (RingPlot) chart.getPlot();
		plot.setCircular(true);
		plot.setStartAngle(startAngle);
		plot.setDirection(rotation == Rotation.ANTICLOCKWISE ?
				org.jfree.util.Rotation.ANTICLOCKWISE : org.jfree.util.Rotation.CLOCKWISE);
		plot.setOutlineVisible(false);
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setShadowPaint(TRANSPARENT_COLOR);
		plot.setShadowXOffset(0);
		plot.setShadowYOffset(0);
		plot.setInnerSeparatorExtension(0);
	    plot.setOuterSeparatorExtension(0);
	    plot.setSectionDepth(sectionDepth);
		plot.setInteriorGap(interiorGap);
		// Labels don't look good if it has multiple rings, so only show them when there's only one ring
		plot.setLabelGenerator(
				showItemLabels && datasetList.size() == 1 ? new CustomPieSectionLabelGenerator(labels) : null);
		plot.setSimpleLabels(true);
		plot.setLabelBackgroundPaint(TRANSPARENT_COLOR);
		plot.setLabelOutlinePaint(TRANSPARENT_COLOR);
		plot.setLabelShadowPaint(TRANSPARENT_COLOR);
		plot.setLabelFont(plot.getLabelFont().deriveFont(itemFontSize));
		
		var stroke = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		plot.setSeparatorStroke(stroke);
		plot.setSeparatorPaint(borderWidth > 0 ? borderColor : TRANSPARENT_COLOR);
		
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

    // if pixel color is between c1 and c2 (inclusive), make it transparent
    private static Image transformColorToTransparency(BufferedImage image, Color c1, Color c2) {
        // Primitive test, just an example
        int r1 = c1.getRed();
        int g1 = c1.getGreen();
        int b1 = c1.getBlue();
        int r2 = c2.getRed();
        int g2 = c2.getGreen();
        int b2 = c2.getBlue();
        
        ImageFilter filter = new RGBImageFilter() {
            @Override
            public final int filterRGB(int x, int y, int rgb) {
                int r = (rgb & 0x00ff0000) >> 16;
                int g = (rgb & 0x0000ff00) >> 8;
                int b = rgb & 0x000000ff;
                if (r >= r1 && r <= r2
                        && g >= g1 && g <= g2
                        && b >= b1 && b <= b2) {
                    // Set fully transparent but keep color
                    return rgb & 0x00FFFFFF; //alpha of 0 = transparent
                }
                return rgb;
            }
        };

        ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
        
        return Toolkit.getDefaultToolkit().createImage(ip);
    }
}
