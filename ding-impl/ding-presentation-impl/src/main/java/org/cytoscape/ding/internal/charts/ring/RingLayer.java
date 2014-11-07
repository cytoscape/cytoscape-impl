package org.cytoscape.ding.internal.charts.ring;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
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

import org.cytoscape.ding.customgraphics.Rotation;
import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.CustomPieSectionLabelGenerator;
import org.cytoscape.ding.internal.charts.LabelPosition;
import org.cytoscape.ding.internal.charts.pie.PieLayer;
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
    
	public RingLayer(final Map<String, List<Double>> data,
					 final List<String> labels,
					 final boolean showLabels,
					 final List<Color> colors,
					 final float borderWidth,
					 final Color borderColor,
					 final double startAngle,
					 final double hole,
					 final Rotation rotation,
					 final Rectangle2D bounds) {
        super(data, labels, null, null, showLabels, false, false, LabelPosition.STANDARD, colors, 0.0f,
        		TRANSPARENT_COLOR, borderWidth, borderColor, null, bounds);
        this.startAngle = startAngle;
        this.hole = hole;
        this.rotation = rotation;
        this.labels = new HashMap<String, String>();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void draw(final Graphics2D g, final Shape shape, final CyNetworkView networkView, 
			final View<? extends CyIdentifiable> view) {
		getChart(); // Make sure charts have been created
		
		if (chartList.size() == 1) {
			super.draw(g, shape, networkView, view);
		} else {
	        for (final JFreeChart chart : chartList) {
	        	chart.draw(g, bounds);
	        }
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected PieDataset createDataset() {
		datasetList = new ArrayList<PieDataset>();
		
		for (final String category : data.keySet()) {
			final List<Double> values = data.get(category);
			final PieDataset ds = createPieDataset(values);
			
			if (ds != null)
				datasetList.add(ds);
		}
		
		if (showItemLabels && itemLabels != null && datasetList.size() == 1) {
			final List<?> keys = datasetList.get(0).getKeys();
			
			for (int i = 0; i < keys.size(); i++) {
				final String k = (String) keys.get(i);
				final String label = itemLabels.size() > i ? itemLabels.get(i) : null;
				labels.put(k, label);
			}
        }
		
		return datasetList.isEmpty() ? null : datasetList.get(0);
	}
	
	@Override
	protected JFreeChart getChart() {
		if (datasetList == null || chartList == null) {
			createDataset();
			final int total = datasetList.size();
			chartList = new ArrayList<JFreeChart>(total);
			
			if (total > 0) {
				int count = 0;
				
				for (final PieDataset ds : datasetList) {
					double sectionDepth = ((1.0 - hole) / (double)total) * (total - count);
					final JFreeChart chart = createChart(ds, sectionDepth, PieLayer.INTERIOR_GAP);
					chartList.add(chart);
					count++;
				}
			} else {
				// Just to show the "no data" text
				final JFreeChart chart = createChart(createPieDataset(Collections.EMPTY_LIST), 1.0, 0.0);
				chartList.add(chart);
			}
		}
		
		return chartList == null || chartList.isEmpty() ? null : chartList.get(0);
	}
    
	@Override
	protected JFreeChart createChart(final PieDataset dataset) {
		return createChart(dataset, 0, 0);
	}
	
	@Override
	protected BufferedImage createImage(final Rectangle2D r) {
		getChart(); // Make sure charts have been created
		
		if (chartList.size() == 1)
			return super.createImage(r);
		
		final Rectangle nr = validateBounds(r);
        final BufferedImage img = new BufferedImage(nr.width, nr.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics g = img.getGraphics();
        
        // Get a buffered image of each chart converting the background color to transparent
        for (final JFreeChart chart : chartList) {
        	final Image transpImg = transformColorToTransparency(
        			chart.createBufferedImage(nr.width, nr.height), TRANSPARENT_COLOR, TRANSPARENT_COLOR);
        	g.drawImage(transpImg, 0, 0, null);
        }
        
        return img;
	}
	
    private JFreeChart createChart(final PieDataset dataset, final double sectionDepth, final double interiorGap) {
    	JFreeChart chart = ChartFactory.createRingChart(
				null, // chart title
				dataset, // data
				false, // include legend
				false, // tooltips
				false); // urls
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        
		final RingPlot plot = (RingPlot) chart.getPlot();
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
		plot.setLabelFont(plot.getLabelFont().deriveFont(1.0f));
		
		final BasicStroke stroke = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		plot.setSeparatorStroke(stroke);
		plot.setSeparatorPaint(borderWidth > 0 ? borderColor : TRANSPARENT_COLOR);
		
		final List<?> keys = dataset.getKeys();
		
		for (int i = 0; i < keys.size(); i++) {
			final String k = (String) keys.get(i);
			final Color c = colors.size() > i ? colors.get(i) : DEFAULT_ITEM_BG_COLOR;
			plot.setSectionPaint(k, c);
			plot.setSectionOutlinePaint(k, borderWidth > 0 ? borderColor : TRANSPARENT_COLOR);
			plot.setSectionOutlineStroke(k, stroke);
		}
		
		return chart;
	}

    // if pixel color is between c1 and c2 (inclusive), make it transparent
    private static Image transformColorToTransparency(BufferedImage image, Color c1, Color c2) {
        // Primitive test, just an example
        final int r1 = c1.getRed();
        final int g1 = c1.getGreen();
        final int b1 = c1.getBlue();
        final int r2 = c2.getRed();
        final int g2 = c2.getGreen();
        final int b2 = c2.getBlue();
        
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
