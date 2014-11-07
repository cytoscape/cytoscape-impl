package org.cytoscape.ding.customgraphics;

import java.util.Collection;
import java.util.Set;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public interface CyCustomGraphics2Manager {

	static final String GROUP_CHARTS = "Charts";
	static final String GROUP_GRADIENTS = "Gradients";
	static final String GROUP_OTHERS = "Others";
	
	Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> getAllCyCustomGraphics2Factories();
	
	Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> getCyCustomGraphics2Factories(String group);
	
	CyCustomGraphics2Factory<? extends CustomGraphicLayer> getCyCustomGraphics2Factory(String factoryId);
	
	CyCustomGraphics2Factory<? extends CustomGraphicLayer> getCyCustomGraphics2Factory(Class<? extends CyCustomGraphics2<? extends CustomGraphicLayer>> cls);
	
	Set<String> getGroups();
}
