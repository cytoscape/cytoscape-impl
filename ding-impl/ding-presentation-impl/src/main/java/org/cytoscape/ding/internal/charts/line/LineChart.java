package org.cytoscape.ding.internal.charts.line;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.ding.internal.charts.AbstractChartCustomGraphics;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/**
 *
 */
public class LineChart extends AbstractChartCustomGraphics<LineLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.chart.Line";
	public static final String DISPLAY_NAME = "Line Chart";
	
	
	public static final String LINE_WIDTH = "linewidth";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					LineChart.class.getClassLoader().getResource("images/charts/line-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LineChart(final Map<String, Object> properties) {
		super(DISPLAY_NAME, properties);
	}
	
	public LineChart(final LineChart chart) {
		super(chart);
	}
	
	public LineChart(final String input) {
		super(DISPLAY_NAME, input);
	}

	@Override 
	public List<LineLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final List<String> dataColumns = new ArrayList<String>(getList(DATA_COLUMNS, String.class));
		final List<Color> colors = getList(COLORS, Color.class);
		final List<String> itemLabels = getLabelsFromColumn(network, model, get(ITEM_LABELS_COLUMN, String.class));
		final List<String> domainLabels = getLabelsFromColumn(network, model, get(DOMAIN_LABELS_COLUMN, String.class));
		final List<String> rangeLabels = getLabelsFromColumn(network, model, get(RANGE_LABELS_COLUMN, String.class));
		final boolean global = get(GLOBAL_RANGE, Boolean.class, true);
		final DoubleRange range = global ? get(RANGE, DoubleRange.class) : null;
		
		final Map<String, List<Double>> data = getDataFromColumns(network, model, dataColumns);
		
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		
		final boolean showItemLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		final boolean showDomainAxis = get(SHOW_DOMAIN_AXIS, Boolean.class, false);
		final boolean showRangeAxis = get(SHOW_RANGE_AXIS, Boolean.class, false);
		final int lineWidth = get(LINE_WIDTH, Integer.class, 2);
		
		LineLayer layer = new LineLayer(data, itemLabels, domainLabels, rangeLabels, showItemLabels, 
				showDomainAxis, showRangeAxis, colors, range, lineWidth, bounds);
		
		return Collections.singletonList(layer);
	}

	@Override
	public Image getRenderedImage() {
		return ICON.getImage();
	}
	
	@Override
	protected Class<?> getSettingType(final String key) {
		if (key.equals(LINE_WIDTH)) return Integer.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	public String getId() {
		return FACTORY_ID;
	}
}
