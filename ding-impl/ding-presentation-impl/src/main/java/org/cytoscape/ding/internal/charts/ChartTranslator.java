package org.cytoscape.ding.internal.charts;

import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartFactory;
import org.cytoscape.view.presentation.charts.CyChartFactoryManager;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;

public class ChartTranslator implements ValueTranslator<String, CyChart> {

	private final CyChartFactoryManager cfMgr;
	
	public ChartTranslator(final CyChartFactoryManager cfMgr) {
		this.cfMgr = cfMgr;
	}

	@Override
	public CyChart translate(final String inputValue) {
		for (CyChartFactory<?> factory: cfMgr.getAllCyChartFactories()) {
			if (factory.getId() != null && inputValue.startsWith(factory.getId() + ":")) {
				final CyChart<?> chart = factory.getInstance(inputValue.substring(factory.getId().length() + 1));
				
				if (chart != null)
					return chart;
			}
		}
		
		return null;
	}

	@Override
	public Class<CyChart> getTranslatedValueType() {
		return CyChart.class;
	}
}
