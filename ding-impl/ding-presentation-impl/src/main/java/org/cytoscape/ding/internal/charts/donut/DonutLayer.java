package org.cytoscape.ding.internal.charts.donut;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.RingPlot;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

public class DonutLayer extends AbstractChartLayer<PieDataset> {
	
	private List<PieDataset> datasetList;
    private List<JFreeChart> chartList;
    
    /** Where to begin the first circle, as a proportion of the entire node */
    private final double hole = 0.2;
	
	public DonutLayer(final Map<String, List<Double>> data,
					 final List<String> labels,
					 final boolean showLabels,
					 final List<Color> colors,
					 final Rectangle2D bounds) {
        super(data, labels, null, null, showLabels, false, false, colors, null, bounds);
	}
	
	@Override
	protected PieDataset createDataset() {
		datasetList = new ArrayList<PieDataset>();
		
		for (final String category : data.keySet()) {
			final List<Double> values = data.get(category);
			final PieDataset ds = createPieDataset(values, itemLabels);
			
			if (ds != null)
				datasetList.add(ds);
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
					
					System.out.println("\tsectionDepth="+sectionDepth);
					final JFreeChart chart = createChart(ds, sectionDepth, 0.0);
					chartList.add(chart);
					count++;
				}
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
        chart.setBorderPaint(TRANSPARENT_COLOR);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        
		final RingPlot plot = (RingPlot) chart.getPlot();
		plot.setCircular(true);
//		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setOutlineVisible(false);
		plot.setOutlinePaint(TRANSPARENT_COLOR);
		plot.setLabelGenerator(showItemLabels ? new StandardPieSectionLabelGenerator() : null);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setShadowPaint(TRANSPARENT_COLOR);
		plot.setShadowXOffset(0);
		plot.setShadowYOffset(0);
		plot.setInnerSeparatorExtension(0);
	    plot.setOuterSeparatorExtension(0);
	    plot.setSectionDepth(sectionDepth);
		plot.setInteriorGap(interiorGap);
		
		final List<?> keys = dataset.getKeys();
		
		if (colors != null && colors.size() >= keys.size()) {
			for (int i = 0; i < keys.size(); i++) {
				final String k = (String) keys.get(i);
				final Color c = colors.get(i);
				plot.setSectionPaint(k, c);
			}
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
