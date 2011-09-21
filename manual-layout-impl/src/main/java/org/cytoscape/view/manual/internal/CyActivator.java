



package org.cytoscape.view.manual.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;

import org.cytoscape.view.manual.internal.rotate.RotatePanelAction;
import org.cytoscape.view.manual.internal.scale.ScalePanelAction;
import org.cytoscape.view.manual.internal.control.ControlPanel;
import org.cytoscape.view.manual.internal.scale.ScalePanel;
import org.cytoscape.view.manual.internal.control.ControlPanelAction;
import org.cytoscape.view.manual.internal.rotate.RotatePanel;

import org.cytoscape.application.swing.CytoPanelComponent;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CySwingApplication cySwingApplicationServiceRef = getService(bc,CySwingApplication.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		
		ControlPanel controlPanel = new ControlPanel(cyApplicationManagerServiceRef);
		RotatePanel rotatePanel = new RotatePanel(cyApplicationManagerServiceRef);
		ScalePanel scalePanel = new ScalePanel(cyApplicationManagerServiceRef);
		ControlPanelAction controlPanelAction = new ControlPanelAction(controlPanel,cySwingApplicationServiceRef,cyApplicationManagerServiceRef);
		RotatePanelAction rotatePanelAction = new RotatePanelAction(rotatePanel,cySwingApplicationServiceRef,cyApplicationManagerServiceRef);
		ScalePanelAction scalePanelAction = new ScalePanelAction(scalePanel,cySwingApplicationServiceRef,cyApplicationManagerServiceRef);
		
		registerAllServices(bc,controlPanelAction, new Properties());
		registerAllServices(bc,scalePanelAction, new Properties());
		registerAllServices(bc,rotatePanelAction, new Properties());
		registerService(bc,controlPanel,CytoPanelComponent.class, new Properties());
		registerService(bc,scalePanel,CytoPanelComponent.class, new Properties());
		registerService(bc,rotatePanel,CytoPanelComponent.class, new Properties());
	}
}

