package org.cytoscape.search.internal.ui.debug;

import java.util.Properties;

import javax.swing.Icon;

import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.property.CyProperty;
import org.cytoscape.search.internal.progress.ProgressMonitor;
import org.cytoscape.search.internal.progress.ProgressViewer;
import org.cytoscape.service.util.CyServiceRegistrar;

public class DebugSearchProgressPanel implements CytoPanelComponent2, ProgressViewer {

	public static final String ID = "org.cytoscape.search.internal.ui.DebugSearchProgressPanel";
	
	
	private final ProgressPanel progressPanel;
	
	public DebugSearchProgressPanel() {
		progressPanel = new ProgressPanel(false);
	}
	
	
	public static boolean showDebugPanel(CyServiceRegistrar registrar) {
		@SuppressWarnings("unchecked")
		CyProperty<Properties> cyProp = registrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		return cyProp != null && "true".equalsIgnoreCase(cyProp.getProperties().getProperty("showDebugPanel")); // Same property used by the ding debug panel
	}
	
	
	@Override
	public ProgressMonitor addProgress(String title) {
		return progressPanel.addProgress(title);
	}
	
	@Override
	public ProgressPanel getComponent() {
		return progressPanel;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public String getTitle() {
		return "Search Progress";
	}

	@Override
	public Icon getIcon() {
		return null;
	}
	
	@Override
	public String getIdentifier() {
		return ID;
	}

}
