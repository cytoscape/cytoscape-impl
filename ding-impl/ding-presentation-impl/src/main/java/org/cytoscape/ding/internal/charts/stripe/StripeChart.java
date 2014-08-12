package org.cytoscape.ding.internal.charts.stripe;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.cytoscape.ding.customgraphics.Orientation;
import org.cytoscape.ding.internal.charts.AbstractChart;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

/**
 * 
 */
public class StripeChart extends AbstractChart<StripeLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.StripeChart";
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

	public StripeChart(final Map<String, Object> properties, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, properties, colIdFactory);
	}
	
	public StripeChart(final StripeChart chart, final CyColumnIdentifierFactory colIdFactory) {
		super(chart, colIdFactory);
	}
	
	public StripeChart(final String input, final CyColumnIdentifierFactory colIdFactory) {
		super(DISPLAY_NAME, input, colIdFactory);
	}

	@Override 
	public List<StripeLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final Orientation orientation = get(ORIENTATION, Orientation.class);
		final List<String> labels = Collections.emptyList();
		
		final Map<String, List<Double>> data = getData(network, model);
		
		final List<Color> colors = getColors(data);
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		
		final float borderWidth = get(BORDER_WIDTH, Float.class, 0.25f);
		final Color borderColor = get(BORDER_COLOR, Color.class, Color.DARK_GRAY);
		
		final StripeLayer layer = new StripeLayer(data, labels, false, colors, borderWidth, borderColor,
				orientation, bounds);
		
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
	protected Map<String, List<Double>> getData(final CyNetwork network, final CyIdentifiable model) {
		final Map<String, List<Double>> data = new HashMap<String, List<Double>>();
		
		final List<CyColumnIdentifier> dataColumns = 
				new ArrayList<CyColumnIdentifier>(getList(DATA_COLUMNS, CyColumnIdentifier.class));
		final CyColumnIdentifier columnId = dataColumns.isEmpty() ? null : dataColumns.get(0);
		
		if (columnId != null) {
			final Set<Object> rowValues = getDistinctValuesFromRow(network, model, columnId);
			
			if (!rowValues.isEmpty()) {
				// Create dummy data
				final List<Double> values = new ArrayList<Double>();
				final List<String> distinctValues = getList(DISTINCT_VALUES, String.class);
				
				for (int i = 0; i < distinctValues.size(); i++) {
					// 1: Has the value/color at this index
					// 0: Does not have that value
					final double v = rowValues.contains(distinctValues.get(i)) ? 1.0 : 0.0;
					values.add(v);
				}
				
				data.put(columnId.getColumnName(), values);
			}
		}
		
		if (data.isEmpty()) {
			final List<Color> colors = getList(COLORS, Color.class);
			
			if (colors != null && !colors.isEmpty()) {
				final List<Double> values = new ArrayList<Double>();
				
				for (final Color c : colors)
					values.add(1.0);
				
				data.put("Values", values);
			}
		}
		
		return data;
	}
	
	@Override
	protected Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(DISTINCT_VALUES)) return List.class;
		return super.getSettingType(key);
	}
	
	@Override
	protected Class<?> getSettingListType(final String key) {
		if (key.equalsIgnoreCase(DISTINCT_VALUES)) return Object.class;
		return super.getSettingListType(key);
	}
	
	public static Set<Object> getDistinctValuesFromRow(final CyNetwork network, final CyIdentifiable model,
			final CyColumnIdentifier columnId) {
		final Set<Object> values = new LinkedHashSet<Object>();
		final CyRow row = network.getRow(model);
		
		if (row == null)
			return values;

		final CyTable table = row.getTable();
		final CyColumn column = table.getColumn(columnId.getColumnName());
		
		if (column != null && column.getType() == List.class) {
			final List<?> list = row.getList(columnId.getColumnName(), column.getListElementType());
			
			if (list != null)
				values.addAll(list);
		}

		return values;
	}
}
