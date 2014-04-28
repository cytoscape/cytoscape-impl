package org.cytoscape.ding.internal.charts.stripe;

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
import org.cytoscape.ding.internal.charts.Orientation;
import org.cytoscape.ding.internal.charts.util.ColorUtil;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/**
 * 
 */
public class StripeChart extends AbstractChartCustomGraphics<StripeLayer> {
	
	public static final String FACTORY_ID = "org.cytoscape.chart.Stripe";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					StripeChart.class.getClassLoader().getResource("images/charts/stripe-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public StripeChart() {
		this("");
	}
	
	public StripeChart(final StripeChart chart) {
		super(chart);
	}
	
	public StripeChart(final String input) {
		super("Stripe Chart", input);
	}

	@Override 
	public List<StripeLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final String labelsColumn = get(LABELS_COLUMN, String.class);
		final List<String> labels = getLabelsFromColumn(network, model, labelsColumn);
		final List<Color> colors = getList(COLORS, Color.class);
		
		final List<Double> values = new ArrayList<Double>();
		for (int i = 0; i < labels.size(); i++) values.add(1.0); // So the bars have the same size
		final Map<String, List<Double>> data = Collections.singletonMap(labelsColumn, values);
		
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		
		final Orientation orientation = get(ORIENTATION, Orientation.class);
		final boolean showLabels = get(SHOW_LABELS, Boolean.class, false);
		
		StripeLayer layer = new StripeLayer(data, labels, showLabels, colors, orientation, bounds);
		
		return Collections.singletonList(layer);
	}

	@Override
	public List<Color> convertInputToColor(final String input, final Map<String, ? extends List<?>> data,
			final boolean normalize) {
		int nColors = 0;
		
		for (final List<?> values : data.values())
			nColors = Math.max(nColors, values.size());

		if (input == null) {
			// give the default: contrasting colors
			return ColorUtil.generateContrastingColors(nColors);
		}

		return ColorUtil.parseColorKeyword(input.trim(), nColors);
	}
	
	@Override
	public Image getRenderedImage() {
		return ICON.getImage();
	}
	
	@Override
	public String getId() {
		return FACTORY_ID;
	}
}
