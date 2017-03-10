package org.cytoscape.ding.internal.charts.heatmap;

import static org.cytoscape.ding.customgraphics.ColorScheme.CUSTOM;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.customgraphics.ColorScheme;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.charts.ColorSchemeEditor;
import org.cytoscape.ding.internal.charts.LabelPosition;
import org.cytoscape.ding.internal.charts.util.ColorGradient;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class HeatMapChartEditor extends AbstractChartEditor<HeatMapChart> {

	private static final long serialVersionUID = -8463795233540323840L;

	private static final ColorScheme[] UP_ZERO_DOWN_COLOR_SCHEMES;
	
	static {
		final List<ColorScheme> upZeroDownSchemeList = new ArrayList<>();
		
		for (final ColorGradient cg : ColorGradient.values()) {
			if (cg.getColors().size() == 3)
				upZeroDownSchemeList.add(new ColorScheme(cg));
		}
		
		upZeroDownSchemeList.add(CUSTOM);
		
		UP_ZERO_DOWN_COLOR_SCHEMES = upZeroDownSchemeList.toArray(new ColorScheme[upZeroDownSchemeList.size()]);
	}
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public HeatMapChartEditor(final HeatMapChart chart, final CyServiceRegistrar serviceRegistrar) {
		super(chart, Number.class, false, true, true, false, true, true, true, false, serviceRegistrar);
		
		getBorderPnl().setVisible(false);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================

	@Override
	public JComboBox<LabelPosition> getDomainLabelPositionCmb() {
		if (domainLabelPositionCmb == null) {
			final JComboBox<LabelPosition> cmb = super.getDomainLabelPositionCmb();
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
				label = IconManager.ICON_BAN;
				toolTip = "Not Available";
			}
			
			cp.setText(label);
			cp.setToolTipText(toolTip);
		}
	}
}
