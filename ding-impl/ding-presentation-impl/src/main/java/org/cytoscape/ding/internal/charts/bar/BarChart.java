package org.cytoscape.ding.internal.charts.bar;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.ding.internal.charts.AbstractChartCustomGraphics;
import org.cytoscape.ding.internal.charts.Orientation;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

/**
 * 
 */
public class BarChart extends AbstractChartCustomGraphics<BarLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.BarChart";
	public static final String DISPLAY_NAME = "Bar Chart";
	
	public static final String TYPE = "type";
	public static final String SEPARATION = "separation";
	
	public static final double MAX_SEPARATION = 0.5;
	
	public static enum BarChartType { GROUPED, STACKED, HEAT_STRIPS };
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					BarChart.class.getClassLoader().getResource("images/charts/bar-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarChart(final Map<String, Object> properties, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, properties, colIdFactory);
	}
	
	public BarChart(final BarChart chart, final CyColumnIdentifierFactory colIdFactory) {
		super(chart, colIdFactory);
	}
	
	public BarChart(final String input, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, input, colIdFactory);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override 
	public List<BarLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
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
		
		final BarChartType type = get(TYPE, BarChartType.class, BarChartType.GROUPED);
		final Orientation orientation = get(ORIENTATION, Orientation.class);
		final boolean showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		final boolean showDomainAxis = get(SHOW_DOMAIN_AXIS, Boolean.class, false);
		final boolean showRangeAxis = get(SHOW_RANGE_AXIS, Boolean.class, false);
		final String colorScheme = get(COLOR_SCHEME, String.class, "");
		final boolean upAndDown = ColorUtil.UP_DOWN.equalsIgnoreCase(colorScheme);
		
		double separation = get(SEPARATION, Double.class, 0.0);
		separation = (separation > MAX_SEPARATION) ? MAX_SEPARATION : (separation < 0.0 ? 0.0 : separation);
		
		final BarLayer layer = new BarLayer(data, type, itemLabels, domainLabels, rangeLabels, showLabels,
				showDomainAxis, showRangeAxis, colors, upAndDown, separation, range, orientation, bounds);
		
		return Collections.singletonList(layer);
	}

	@Override
	public Image getRenderedImage() {
		return ICON.getImage();
	}
	
	@Override
	public String getId() {
		return FACTORY_ID;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(TYPE)) return BarChartType.class;
		if (key.equalsIgnoreCase(SEPARATION)) return Double.class;
		
		return super.getSettingType(key);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected <S> S parseValue(final String key, Object value, final Class<S> type) {
		try {
			if (!type.isAssignableFrom(value.getClass())) {
				if (type == BarChartType.class) {
					value = BarChartType.valueOf(value.toString().toUpperCase());
				} else {
					value = super.parseValue(key, value, type);
				}
			}
			
			return (S) value;
		} catch (Exception e) {
			return null;
		}
	}
}
