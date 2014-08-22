package org.cytoscape.ding.internal.charts.heatmap;

import static org.cytoscape.ding.customgraphics.ColorScheme.CUSTOM;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.customgraphics.ColorScheme;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.charts.ColorSchemeEditor;
import org.cytoscape.ding.internal.charts.util.ColorGradient;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class HeatMapChartEditor extends AbstractChartEditor<HeatMapChart> {

	private static final long serialVersionUID = -8463795233540323840L;

	private static final ColorScheme[] UP_ZERO_DOWN_COLOR_SCHEMES;
	
	static {
		final List<ColorScheme> upZeroDownSchemeList = new ArrayList<ColorScheme>();
		
		for (final ColorGradient cg : ColorGradient.values()) {
			if (cg.getColors().size() == 3)
				upZeroDownSchemeList.add(new ColorScheme(cg));
		}
		
		upZeroDownSchemeList.add(CUSTOM);
		
		UP_ZERO_DOWN_COLOR_SCHEMES = upZeroDownSchemeList.toArray(new ColorScheme[upZeroDownSchemeList.size()]);
	}
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public HeatMapChartEditor(final HeatMapChart chart, final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		super(chart, Number.class, false, 10, true, true, false, true, true, true, appMgr, iconMgr, colIdFactory);
		
		getBorderPnl().setVisible(false);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================

	@Override
	protected ColorSchemeEditor<HeatMapChart> getColorSchemeEditor() {
		if (colorSchemeEditor == null) {
			colorSchemeEditor = new HeatMapColorSchemeEditor(chart, getColorSchemes(), appMgr.getCurrentNetwork(),
					iconMgr);
		}
		
		return colorSchemeEditor;
	}
	
	@Override
	protected ColorScheme[] getColorSchemes() {
		return UP_ZERO_DOWN_COLOR_SCHEMES;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class HeatMapColorSchemeEditor extends ColorSchemeEditor<HeatMapChart> {

		private static final long serialVersionUID = -1978465682553210535L;

		public HeatMapColorSchemeEditor(final HeatMapChart chart, final ColorScheme[] colorSchemes, final CyNetwork network,
				final IconManager iconMgr) {
			super(chart, colorSchemes, false, network, iconMgr);
		}

		@Override
		protected int getTotal() {
			return total = 4;
		}
		
		@Override
		protected void style(final ColorPanel cp, final int index) {
			super.style(cp, index);
			
			cp.setFont(iconMgr.getIconFont(11));
			String label = "";
			String toolTip = null;
			
			if (index == 0) {
				label = IconManager.ICON_ARROW_UP;
				toolTip = "Upper Bound";
			} else if (index == 1) {
				toolTip = "Zero";
			} else if (index == 2) {
				label = IconManager.ICON_ARROW_DOWN;
				toolTip = "Lower Bound";
			} else if (index == 3) {
				label = IconManager.ICON_BAN_CIRCLE;
				toolTip = "Not Available";
			}
			
			cp.setText(label);
			cp.setToolTipText(toolTip);
		}
	}
}
