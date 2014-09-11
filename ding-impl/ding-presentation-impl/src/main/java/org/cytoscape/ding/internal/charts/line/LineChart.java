package org.cytoscape.ding.internal.charts.line;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.ding.internal.charts.AbstractChart;
import org.cytoscape.ding.internal.charts.DoubleRange;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

/**
 *
 */
public class LineChart extends AbstractChart<LineLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.LineChart";
	public static final String DISPLAY_NAME = "Line Chart";
	
	
	public static final String LINE_WIDTH = "lineWidth";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					LineChart.class.getClassLoader().getResource("images/charts/line-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LineChart(final Map<String, Object> properties, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, properties, colIdFactory);
	}
	
	public LineChart(final LineChart chart, final CyColumnIdentifierFactory colIdFactory) {
		super(chart, colIdFactory);
	}
	
	public LineChart(final String input, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, input, colIdFactory);
	}

	@Override 
	public List<LineLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final List<String> itemLabels = getItemLabels(network, model);
		final List<String> domainLabels =
				getLabelsFromColumn(network, model, get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class));
		final List<String> rangeLabels =
				getLabelsFromColumn(network, model, get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class));
		final boolean global = get(GLOBAL_RANGE, Boolean.class, true);
		final DoubleRange range = global ? get(RANGE, DoubleRange.class) : null;
		
		final Map<String, List<Double>> data = getData(network, model);
		
		final List<Color> colors = getColors(data);
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		
		final boolean showItemLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		final boolean showDomainAxis = get(SHOW_DOMAIN_AXIS, Boolean.class, false);
		final boolean showRangeAxis = get(SHOW_RANGE_AXIS, Boolean.class, false);
		final float axisWidth = get(AXIS_WIDTH, Float.class, 0.25f);
		final Color axisColor = get(AXIS_COLOR, Color.class, Color.DARK_GRAY);
		final float lineWidth = get(LINE_WIDTH, Float.class, 1.0f);
		
		LineLayer layer = new LineLayer(data, itemLabels, domainLabels, rangeLabels, showItemLabels, 
				showDomainAxis, showRangeAxis, colors, axisWidth, axisColor, range, lineWidth, bounds);
		
		return Collections.singletonList(layer);
	}

	@Override
	public Image getRenderedImage() {
		return ICON.getImage();
	}
	
	@Override
	public Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(LINE_WIDTH)) return Float.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	public String getId() {
		return FACTORY_ID;
	}
}
