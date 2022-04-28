package org.cytoscape.io.internal.write.graphics.command;

import java.io.File;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.Tunable;

public class ExportNetworkTunables {

	static final String _CY_NETWORK_VIEW_DESC = "Specifies a network view by name, or by SUID if the prefix ```SUID:``` is used. The keyword ```CURRENT```, or a blank value can also be used to specify the current network view.";
	
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private RenderingEngine<?> re;
	
	public ExportNetworkTunables(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Tunable(
		longDescription = "The path name of the file where the view must be saved to.",
		exampleStringValue = "/Users/johndoe/Downloads/View1.png"
	)
	public File outputFile;
	
	
	public CyNetworkView view = null;
	@Tunable(
		description="Network View to export",
		longDescription=_CY_NETWORK_VIEW_DESC,
		exampleStringValue = "CURRENT"
	)
	public CyNetworkView getView() {
		return view;
	}
	public void setView(CyNetworkView view) {
		this.view = view;
		if (view != null) {
			// Get the rendering engine
			RenderingEngine<?> engine = serviceRegistrar.getService(CyApplicationManager.class).getCurrentRenderingEngine();

			// Now get the rendering engine for this view and use this one if we can
			String engineId = view.getRendererId();
			RenderingEngineManager engineManager = serviceRegistrar.getService(RenderingEngineManager.class);
	
			for (RenderingEngine<?> e : engineManager.getRenderingEngines(view)) {
				if (engineId.equals(e.getRendererId())) {
					engine = e;
					break;
				}
			}
			this.re = engine;
		}
	}
	
	
	
	public RenderingEngine<?> getRenderingEngine() {
		return re;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
}
