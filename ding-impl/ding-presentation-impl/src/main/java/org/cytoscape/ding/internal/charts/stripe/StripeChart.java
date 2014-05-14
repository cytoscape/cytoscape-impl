package org.cytoscape.ding.internal.charts.stripe;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.ding.internal.charts.AbstractChartCustomGraphics;
import org.cytoscape.ding.internal.charts.Orientation;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/**
 * 
 */
public class StripeChart extends AbstractChartCustomGraphics<StripeLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.chart.Stripe";
	public static final String DISPLAY_NAME = "Stripe Chart";
	
	public static final String DISTINCT_VALUES = "distinctvalues";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					StripeChart.class.getClassLoader().getResource("images/charts/stripe-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public StripeChart(final Map<String, Object> properties) {
		super(DISPLAY_NAME, properties);
	}
	
	public StripeChart(final StripeChart chart) {
		super(chart);
	}
	
	public StripeChart(final String input) {
		super(DISPLAY_NAME, input);
	}

	@Override 
	public List<StripeLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final List<String> dataColumns = new ArrayList<String>(getList(DATA_COLUMNS, String.class));
		final List<String> distinctValues = getList(DISTINCT_VALUES, String.class);
		final List<Color> colors = getList(COLORS, Color.class);
		final Orientation orientation = get(ORIENTATION, Orientation.class);
		final List<String> labels = Collections.emptyList();
		
		final String columnName = dataColumns.isEmpty() ? null : dataColumns.get(0);
		final Map<String, List<Double>> data;
		
		if (columnName != null) {
			final Set<Object> rowValues = getDistinctValuesFromRow(network, model, columnName);
			
			// Create dummy data
			final List<Double> values = new ArrayList<Double>();
			
			for (int i = 0; i < distinctValues.size(); i++) {
				// 1: Has the value/color at this index
				// 0: Does not have that value
				final double v = rowValues.contains(distinctValues.get(i)) ? 1.0 : 0.0;
				values.add(v);
			}
			
			data = Collections.singletonMap(columnName, values);
		} else {
			data = Collections.emptyMap();
		}
		
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		
		StripeLayer layer = new StripeLayer(data, labels, false, colors, orientation, bounds);
		
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
	
	@Override
	protected Class<?> getSettingType(final String key) {
		if (key.equals(DISTINCT_VALUES)) return List.class;
		return super.getSettingType(key);
	}
	
	@Override
	protected Class<?> getSettingListType(final String key) {
		if (key.equals(DISTINCT_VALUES)) return Object.class;
		return super.getSettingListType(key);
	}
	
	public static Set<Object> getDistinctValuesFromRow(final CyNetwork network, final CyIdentifiable model,
			final String columnName) {
		final Set<Object> values = new LinkedHashSet<Object>();
		final CyRow row = network.getRow(model);
		
		if (row == null)
			return values;

		final CyTable table = row.getTable();
		final CyColumn column = table.getColumn(columnName);
		
		if (column != null && column.getType() == List.class) {
			final List<?> list = row.getList(columnName, column.getListElementType());
			
			if (list != null)
				values.addAll(list);
		}

		return values;
	}
}
