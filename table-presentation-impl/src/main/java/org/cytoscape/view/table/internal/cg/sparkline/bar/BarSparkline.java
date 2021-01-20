package org.cytoscape.view.table.internal.cg.sparkline.bar;

import java.awt.Color;
import java.awt.Image;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.table.internal.cg.Orientation;
import org.cytoscape.view.table.internal.cg.sparkline.AbstractSparkline;
import org.cytoscape.view.table.internal.cg.sparkline.CellSparkline;
import org.cytoscape.view.table.internal.util.ColorScale;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class BarSparkline extends AbstractSparkline<CategoryDataset> implements CellSparkline {

	public static final String FACTORY_ID = "org.cytoscape.BarSparkline";
	public static final String DISPLAY_NAME = "Bar Sparkline";
	
	public static final String TYPE = "cy_type";
	
	public static enum BarSparklineType { GROUPED, STACKED, HEAT_STRIPS, UP_DOWN };
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					BarSparkline.class.getClassLoader().getResource("images/sparklines/bar-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private BarSparklineType type;
	private Orientation orientation;
	private final double separation = 0.05;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarSparkline(Map<String, Object> properties, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, properties, serviceRegistrar);
	}
	
	public BarSparkline(BarSparkline chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, serviceRegistrar);
	}
	
	public BarSparkline(String input, CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, input, serviceRegistrar);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public Image getRenderedImage() {
		return ICON.getImage();
	}
	
	@Override
	public String getId() {
		return FACTORY_ID;
	}
	
	@Override
	public Class<?> getSettingType(String key) {
		if (key.equalsIgnoreCase(TYPE)) return BarSparklineType.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	public void addJsonDeserializers(SimpleModule module) {
		super.addJsonDeserializers(module);
		module.addDeserializer(BarSparklineType.class, new BarSparklineTypeJsonDeserializer());
	}
	
	@Override
	public void update() {
		super.update();
		
		type = get(TYPE, BarSparklineType.class, BarSparklineType.GROUPED);
		orientation = get(ORIENTATION, Orientation.class);
	}

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected CategoryDataset createDataset(CyRow row) {
		data = getData(row);
		
		colors = getColors(data);
		double size = 32;
		bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		
		if (type == BarSparklineType.HEAT_STRIPS &&
				(range == null || range.size() < 2 || range.get(0) == null || range.get(1) == null))
			range = calculateRange(data.values(), false);
		
		return createCategoryDataset(data);
	}
    
	@Override
	protected JFreeChart createChart(CategoryDataset dataset) {System.out.println(orientation);
		final PlotOrientation plotOrientation;
		
		if (orientation != null) {
			plotOrientation = orientation == Orientation.HORIZONTAL ? PlotOrientation.HORIZONTAL
					: PlotOrientation.VERTICAL;
		} else {
			// Auto-orientation, based on the number of bars
			// (1 bar fits better in a table cell when horizontal; besides, two or more horizontal bars per cell
			// could make the visualization of confusing when comparing rows, specially if the table does not show the
			// internal grid lines to better separate the different charts)
			var colCount = dataset.getColumnCount();
			plotOrientation = colCount > 1 ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL;
		}
		
		final JFreeChart chart;
		
		if (type == BarSparklineType.STACKED)
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
					false // urls
			);
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        
        var plot = (CategoryPlot) chart.getPlot();
		plot.setOutlineVisible(false);
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		plot.setDomainGridlinesVisible(false);
	    plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		
		if (type != BarSparklineType.STACKED) {
			if (type == BarSparklineType.HEAT_STRIPS || type == BarSparklineType.UP_DOWN) {
				Color up   = (colors.size() > 0) ? colors.get(0) : Color.LIGHT_GRAY;
				Color zero = (colors.size() > 2) ? colors.get(1) : Color.BLACK;
				Color down = (colors.size() > 2) ? colors.get(2) : (colors.size() > 1 ? colors.get(1) : Color.GRAY);
				plot.setRenderer(new UpDownColorBarRenderer(up, zero, down));
			} else {
				plot.setRenderer(new SingleCategoryRenderer());
			}
		}
		
		var domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(false);
        domainAxis.setCategoryMargin(separation);
        
        var rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(false);
		
		// Set axis range
		if (range != null && range.size() >= 2 && range.get(0) != null && range.get(1) != null) {
			rangeAxis.setLowerBound(range.get(0));
			rangeAxis.setUpperBound(range.get(1));
		}
				
		var renderer = (BarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setShadowVisible(false);
		renderer.setDrawBarOutline(false);
		renderer.setBaseItemLabelGenerator(null);
		renderer.setBaseItemLabelsVisible(false);
		renderer.setItemMargin(separation);
		
		var keys = dataset.getRowKeys();
		
		for (int i = 0; i < keys.size(); i++) {
			if (type != BarSparklineType.UP_DOWN && type != BarSparklineType.HEAT_STRIPS) {
				var c = DEFAULT_ITEM_BG_COLOR;
				
				if (colors != null && colors.size() > i)
					c = colors.get(i);
				
				renderer.setSeriesPaint(i, c);
			}
		}
		
		return chart;
	}

	// ==[ CLASSES ]====================================================================================================
	
	@SuppressWarnings("serial")
	private class UpDownColorBarRenderer extends BarRenderer {

		private Color upColor;
		private Color zeroColor;
		private Color downColor;

		UpDownColorBarRenderer(Color up, Color zero, Color down) {
			this.upColor = up;
			this.zeroColor = zero;
			this.downColor = down;
		}
		
		@Override
		public Paint getItemPaint(int row, int column) {
			CategoryDataset dataset = getPlot().getDataset();
			String rowKey = (String) dataset.getRowKey(row);
			String colKey = (String) dataset.getColumnKey(column);
			double value = dataset.getValue(rowKey, colKey).doubleValue();
			
			if (type == BarSparklineType.HEAT_STRIPS) {
				if (Double.isNaN(value))
					return zeroColor;
				
				return ColorScale.getPaint(value, range.get(0), range.get(1), downColor, zeroColor, upColor);
			}
			
			return value < 0.0 ? downColor : upColor;
		}
	}
	
	@SuppressWarnings("serial")
	private class SingleCategoryRenderer extends BarRenderer {

		@Override
        public Paint getItemPaint(int row, int column) {
            return (colors != null && colors.size() > column) ? colors.get(column) : DEFAULT_ITEM_BG_COLOR;
        }
    }
}
