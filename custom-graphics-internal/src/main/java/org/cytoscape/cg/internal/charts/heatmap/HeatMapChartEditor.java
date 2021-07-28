package org.cytoscape.cg.internal.charts.heatmap;

import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.cg.internal.charts.AbstractChartEditor;
import org.cytoscape.cg.internal.charts.ColorSchemeEditor;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.PaletteType;
import org.cytoscape.util.swing.IconManager;

@SuppressWarnings("serial")
public class HeatMapChartEditor extends AbstractChartEditor<HeatMapChart> {

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
					getDefaultPaletteType(),
					getDefaultPaletteName(),
					serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork(),
					serviceRegistrar
			);
		}
		
		return colorSchemeEditor;
	}
	
	@Override
	protected PaletteType getDefaultPaletteType() {
		return BrewerType.DIVERGING;
	}
	
	@Override
	protected String getDefaultPaletteName() {
		return "Red-Yellow-Blue";
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class HeatMapColorSchemeEditor extends ColorSchemeEditor<HeatMapChart> {

		public HeatMapColorSchemeEditor(
				HeatMapChart chart,
				PaletteType paletteType,
				String defaultPaletteName,
				CyNetwork network,
				CyServiceRegistrar serviceRegistrar
		) {
			super(chart, false, paletteType, defaultPaletteName, network, serviceRegistrar);
		}

		/**
		 * Though the total number of colors we need is 4, we should choose from 3-color palettes (upper, zero, lower).
		 * The fourth color just means "not available" and will be a simple gray.
		 */
		@Override
		protected int getPaletteSize() {
			return 3;
		}
		
		@Override
		protected int getTotal() {
			return total = 4;
		}

		@Override
		protected void style(ColorPanel cp, int index) {
			super.style(cp, index);
			
			cp.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(11));
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
