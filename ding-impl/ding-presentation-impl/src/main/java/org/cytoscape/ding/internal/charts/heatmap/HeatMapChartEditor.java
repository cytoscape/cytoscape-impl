package org.cytoscape.ding.internal.charts.heatmap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.charts.ColorScheme;
import org.cytoscape.ding.internal.charts.ColorSchemeEditor;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class HeatMapChartEditor extends AbstractChartEditor<HeatMapChart> {

	private static final long serialVersionUID = -8463795233540323840L;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public HeatMapChartEditor(final HeatMapChart chart, final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		super(chart, Number.class, false, 10, true, false, false, true, true, true, appMgr, iconMgr, colIdFactory);
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
	
	// ==[ CLASSES ]====================================================================================================
	
	private class HeatMapColorSchemeEditor extends ColorSchemeEditor<HeatMapChart> {

		private static final long serialVersionUID = -1978465682553210535L;

		public HeatMapColorSchemeEditor(final HeatMapChart chart, final ColorScheme[] colorSchemes, final CyNetwork network,
				final IconManager iconMgr) {
			super(chart, colorSchemes, false, network, iconMgr);
		}

		@Override
		protected int getTotal() {
			return total = 2;
		}
	}
}
