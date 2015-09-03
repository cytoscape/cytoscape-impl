package org.cytoscape.opencl.cycl;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.osgi.framework.BundleContext;

public class CyCLSettingsTaskFactory extends AbstractTaskFactory
{
	private CyProperty<Properties> properties;
	
	public CyCLSettingsTaskFactory(CyProperty<Properties> properties) 
	{
		this.properties = properties;
	}
	
	@Override
	public TaskIterator createTaskIterator()
	{
		return new TaskIterator(2, new CyCLSettingsTask(properties));
	}	
}