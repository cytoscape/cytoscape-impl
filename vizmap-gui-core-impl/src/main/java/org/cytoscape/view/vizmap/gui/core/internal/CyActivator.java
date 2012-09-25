package org.cytoscape.view.vizmap.gui.core.internal;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.vizmap.gui.core.internal.cellrenderer.ContinuousMappingCellRendererFactoryImpl;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		ContinuousMappingCellRendererFactory continuousMappingCellRendererFactoryImpl = new ContinuousMappingCellRendererFactoryImpl();
		registerService(context, continuousMappingCellRendererFactoryImpl, ContinuousMappingCellRendererFactory.class, new Properties());
	}

}
