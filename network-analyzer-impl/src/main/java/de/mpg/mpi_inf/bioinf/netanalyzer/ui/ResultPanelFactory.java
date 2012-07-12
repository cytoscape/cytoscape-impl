package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

import java.awt.Component;
import java.util.Properties;

import org.cytoscape.service.util.CyServiceRegistrar;

public class ResultPanelFactory {

	private final CyServiceRegistrar registrar;

	public ResultPanelFactory(final CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}

	public ResultPanel registerPanel(final Component panel, final String panelTitle) {
		final ResultPanel resPanel = new ResultPanel(panelTitle);
		resPanel.add(panel);
		registrar.registerAllServices(resPanel, new Properties());
		
		return resPanel;
	}
	
	public void removePanel(ResultPanel panel) {
		if(panel != null) {
			registrar.unregisterAllServices(panel);
		}
	}

}
