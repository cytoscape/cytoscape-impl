package org.cytoscape.cg.internal.charts.heatmap;

import static org.cytoscape.cg.model.ColorScheme.CUSTOM;

import java.util.ArrayList;

import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.cg.internal.charts.AbstractChartEditor;
import org.cytoscape.cg.internal.charts.ColorSchemeEditor;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.cg.internal.charts.util.ColorGradient;
import org.cytoscape.cg.model.ColorScheme;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;

@SuppressWarnings("serial")
public class HeatMapChartEditor extends AbstractChartEditor<HeatMapChart> {

	private static final ColorScheme[] UP_ZERO_DOWN_COLOR_SCHEMES;
	
	static {
		var upZeroDownSchemeList = new ArrayList<>();
		
		for (var cg : ColorGradient.values()) {
			if (cg.getColors().size() == 3)
				upZeroDownSchemeList.add(new ColorScheme(cg));
		}
		
		upZeroDownSchemeList.add(CUSTOM);
		
		UP_ZERO_DOWN_COLOR_SCHEMES = upZeroDownSchemeList.toArray(new ColorScheme[upZeroDownSchemeList.size()]);
	}
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public HeatMapChartEditor(HeatMapChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, Number.class, false, true, true, false, true, true, true, false, serviceRegistrar);
		
		getBorderPnl().setVisible(false);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================

	@Override
	public JComboBox<LabelPosition> getDomainLabelPositionCmb() {
		if (domainLabelPositionCmb == null) {
			var cmb = super.getDomainLabelPositionCmb();
			// These options don't work with this chart
			cmb.removeItem(LabelPosition.DOWN_45);
			cmb.removeItem(LabelPosition.UP_45);
			cmb.removeItem(LabelPosition.UP_90);
		}
		
		return domainLabelPositionCmb;
	}
	
	@Override
	protected ColorSchemeEditor<HeatMapChart> getColorSchemeEditor() {
		if (colorSchemeEditor == null) {
			colorSchemeEditor = new HeatMapColorSchemeEditor(
					chart,
					getColorSchemes(),
					serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork(),
					serviceRegistrar.getService(IconManager.class)
			);
		}
		
		return colorSchemeEditor;
	}
	
	@Override
	protected ColorScheme[] getColorSchemes() {
		return UP_ZERO_DOWN_COLOR_SCHEMES;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class HeatMapColorSchemeEditor extends ColorSchemeEditor<HeatMapChart> {

		public HeatMapColorSchemeEditor(HeatMapChart chart, ColorScheme[] colorSchemes, CyNetwork network,
				IconManager iconMgr) {
			super(chart, colorSchemes, false, network, iconMgr);
		}

		@Override
		protected int getTotal() {
			return total = 4;
		}

		@Override
		protected void style(ColorPanel cp, int index) {
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
				label = IconManager.ICON_BAN;
				toolTip = "Not Available";
			}
			
			cp.setText(label);
			cp.setToolTipText(toolTip);
		}
	}
}
