package org.cytoscape.cg.model;

import java.util.Collection;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public interface CustomGraphics2Manager {

	static final String GROUP_CHARTS = "Charts";
	static final String GROUP_GRADIENTS = "Gradients";
	static final String GROUP_OTHERS = "Others";
	
	Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> getAllCustomGraphics2Factories();
	
	Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> getCustomGraphics2Factories(Class<? extends CyIdentifiable> targetType, String group);
	
	CyCustomGraphics2Factory<? extends CustomGraphicLayer> getCustomGraphics2Factory(String factoryId);
	
	CyCustomGraphics2Factory<? extends CustomGraphicLayer> getCustomGraphics2Factory(Class<? extends CyCustomGraphics2<? extends CustomGraphicLayer>> cls);
	
	Set<String> getGroups();
}
