package org.cytoscape.cg.internal.charts;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.cytoscape.cg.internal.paint.TexturePaintFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public abstract class AbstractChartLayer<T extends Dataset> implements Cy2DGraphicLayer {

	public static final int MAX_IMG_RESOLUTION = 3145728;
	public static final Color TRANSPARENT_COLOR = new Color(0x00, 0x00, 0x00, 0);
	
	/** Divisor which should be applied to chart lines so they have the same thickness as Cytoscape lines */
	public static final Color DEFAULT_ITEM_BG_COLOR = Color.LIGHT_GRAY;
	
	/** Category ID -> list of values */
	protected final Map<String, List<Double>> data;
	protected final List<String> itemLabels;
	protected final List<String> domainLabels;
	protected final List<String> rangeLabels;
	protected final boolean showItemLabels;
	protected final boolean showDomainAxis;
	protected final boolean showRangeAxis;
	protected final LabelPosition domainLabelPosition;
	protected final List<Color> colors;
	protected final float borderWidth;
	protected final Color borderColor;
	protected Color labelColor = Color.DARK_GRAY;
	protected float itemFontSize;
	protected float axisWidth;
	protected Color axisColor;
	protected float axisFontSize;
	protected List<Double> range;
	
	protected Rectangle2D bounds;
	protected Rectangle2D scaledBounds;
	
	private JFreeChart chart;
	protected BufferedImage img;
	protected TexturePaint paint;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	protected AbstractChartLayer(
			Map<String, List<Double>> data,
			List<String> itemLabels,
			List<String> domainLabels,
			List<String> rangeLabels,
			boolean showItemLabels,
			boolean showDomainAxis,
			boolean showRangeAxis,
			float itemFontSize,
			LabelPosition domainLabelPosition,
			List<Color> colors,
			float axisWidth,
			Color axisColor,
			float axisFontSize,
			float borderWidth,
			Color borderColor,
			List<Double> range,
			Rectangle2D bounds
	) {
		this.data = data;
		this.itemLabels = itemLabels;
		this.domainLabels = domainLabels;
		this.rangeLabels = rangeLabels;
		this.showItemLabels = showItemLabels;
		this.showDomainAxis = showDomainAxis;
		this.showRangeAxis = showRangeAxis;
		this.itemFontSize = itemFontSize;
		this.domainLabelPosition = domainLabelPosition;
		this.colors = colors;
		this.axisWidth = axisWidth;
		this.axisColor = axisColor;
		this.axisFontSize = axisFontSize;
		this.borderWidth = borderWidth;
		this.borderColor = borderColor;
		this.bounds = scaledBounds = bounds;
		this.range = range;
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public Rectangle2D getBounds2D() {
		return bounds;
	}

	@Override
	public CustomGraphicLayer transform(AffineTransform xform) {
		bounds = xform.createTransformedShape(bounds).getBounds2D();
		
		return this;
	}

	@Override
	public void draw(Graphics2D g, Shape shape, CyNetworkView networkView, View<? extends CyIdentifiable> view) {
		draw(g);
	}
	
	@Override
	public void draw(Graphics2D g, CyTableView tableView, CyColumn column, CyRow row) {
		draw(g);
	}
	
	@Override
	public TexturePaint getPaint(Rectangle2D r) {
		// If the bounds are the same as before, there is no need to recreate the "same" image again
		if (img == null || paint == null || !r.equals(scaledBounds)) {
			// Recreate and cache Image and TexturePaint
			img = createImage(r);
			paint = new TexturePaintFactory(img).getPaint(
					new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight()));
		}
		
		scaledBounds = r;
		
		return paint;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	protected void draw(Graphics2D g) {
		var g2 = (Graphics2D) g.create();
		
		// Give JFreeChart a larger area to draw into, so the proportions of the chart elements looks better
		double scale = 2.0;
		var newBounds = new Rectangle2D.Double(
				bounds.getX() * scale,
				bounds.getY() * scale,
				bounds.getWidth() * scale,
				bounds.getHeight() * scale
		);
		// Of course, we also have to ask Graphics2D to apply the inverse transformation
		double invScale = 1.0 / scale;
		g2.scale(invScale, invScale);

		// Check to see if we have a current alpha composite
		var comp = g2.getComposite();
		
		if (comp instanceof AlphaComposite) {
			float alpha = ((AlphaComposite) comp).getAlpha();
			var fc = getChart();
			var plot = fc.getPlot();
			plot.setForegroundAlpha(alpha);
			fc.draw(g2, newBounds);
		} else {
			getChart().draw(g2, newBounds);
		}

		g2.dispose();
	}
	
	protected JFreeChart getChart() {
		if (chart == null) {
			T dataset = createDataset();
			chart = createChart(dataset);
		}
		
		return chart;
	}

	protected BufferedImage createImage(Rectangle2D r) {
		Rectangle nr = validateBounds(r);
        
        return getChart().createBufferedImage(nr.width, nr.height, BufferedImage.TYPE_INT_ARGB, null);
	}
	
	protected Rectangle validateBounds(Rectangle2D r) {
		double minScale = 1;
		double minSize = 140;
		
		if ((r.getWidth() < minSize || r.getHeight() < minSize) && r.getWidth() > 4 && r.getHeight() > 4)
			minScale = minSize / Math.min(r.getWidth(), r.getHeight()); // Or the plot is not drawn/centered correctly;
		
		int w = (int) Math.round(r.getWidth() * minScale);
		int h = (int) Math.round(r.getHeight() * minScale);
		
		// width * height needs to be less than Integer.MAX_VALUE,
		// but let's limit it to a much smaller resolution, to avoid performance issues and OutOfMemoryErrors
		double resolution = (double)w * (double)h;
		
		if (resolution > MAX_IMG_RESOLUTION) {
			// (f*w)*(f*h) = MAX_IMG_RESOLUTION
			double f = Math.sqrt(MAX_IMG_RESOLUTION / resolution); // new scale
			// The new scale may generate more images that are more pixelated, but that's fine,
			// because either the node size or the zoom level is exaggerated.
			w = (int) Math.round(w*f);
			h = (int) Math.round(h*f);
		}
		
		if (w <= 0) w = 1;
		if (h <= 0) h = 1;
		
		return new Rectangle(w, h);
	}
	
	protected CategoryLabelPositions getCategoryLabelPosition() {
		if (domainLabelPosition == LabelPosition.DOWN_45) return CategoryLabelPositions.DOWN_45;
		if (domainLabelPosition == LabelPosition.DOWN_90) return CategoryLabelPositions.DOWN_90;
		if (domainLabelPosition == LabelPosition.UP_45) return CategoryLabelPositions.UP_45;
		if (domainLabelPosition == LabelPosition.UP_90) return CategoryLabelPositions.UP_90;
		
		return CategoryLabelPositions.STANDARD;
	}
	
	protected abstract T createDataset();
	
	protected abstract JFreeChart createChart(T dataset);
	
	public static CategoryDataset createCategoryDataset(
			Map<String, List<Double>> data,
			boolean listIsSeries,
			List<String> labels
	) {
		var dataset = new DefaultCategoryDataset();
		
		if (listIsSeries && (labels == null || labels.isEmpty())) {
			int size = 0;
			
			for (var values : data.values())
				size = Math.max(size, values.size());
			
			labels = createDefaultLabels(size);
		}
			
		int count = 0;
		
		for (var category : data.keySet()) {
			var values = data.get(category);
			
			for (int i = 0; i < values.size(); i++) {
				Double v = values.get(i);
				String k = "#" + (i+1); // row key
				
				if (listIsSeries) {
					if (labels != null && labels.size() > i)
						k = labels.get(i);
					
					dataset.addValue(v, category, k);
				} else {
					if (labels != null && labels.size() > count)
						category = labels.get(count); // Use label for category name
					
					dataset.addValue(v, k, category);
				}
			}
			
			count++;
		}
		
		return dataset;
	}
	
	// TODO minimumslice: The minimum size of a slice to be considered. All slices smaller than this are grouped together in a single "other" slice
	public static PieDataset createPieDataset(List<Double> values) {
		var dataset = new DefaultPieDataset();
		
		if (values != null) {
			for (int i = 0; i < values.size(); i++) {
				Double v = values.get(i);
				String k = "#" + (i+1);
				dataset.setValue(k, v);
			}
		}
		
		return dataset;
	}
	
	public static List<String> createDefaultLabels(int size) {
		var labels = new ArrayList<String>(size);
		
		for (int i = 0; i < size; i++)
			labels.add("#" + (i+1));
		
		return labels;
	}
	
	public static List<Double> calculateRange(Collection<List<Double>> lists, boolean stacked) {
		var range = new ArrayList<Double>();
		
		if (lists != null && !lists.isEmpty()) {
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			
			for (var values : lists) {
				double sum = 0;
				
				if (values != null) {
					for (double v : values) {
						if (Double.isNaN(v))
							continue;
						
						if (stacked) {
							sum += v;
						} else {
							min = Math.min(min, v);
							max = Math.max(max, v);
						}
					}
					
					if (stacked) {
						min = Math.min(min, sum);
						max = Math.max(max, sum);
					}
				}
				
				if (min != Double.POSITIVE_INFINITY && max != Double.NEGATIVE_INFINITY) {
					range.add(min);
					range.add(max);
				}
			}
		}
		
		return range;
	}
}
