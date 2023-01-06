package org.cytoscape.cycl.internal;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.cycl.CyCLFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator
{
	@Override
	public void start(BundleContext context) throws Exception
	{
		// Start CyCL in separate thread
		final ExecutorService execService = Executors.newSingleThreadExecutor();
		execService.submit(()-> {
			try {
				//System.out.println("Preferred device = " + preferredDevice);

				CyApplicationConfiguration applicationConfig = getService(context, CyApplicationConfiguration.class);
				CyProperty<Properties> cyPropertyServiceRef = getService(context, CyProperty.class, "(cyPropertyName=cytoscape3.props)");

				CyCL.initialize(applicationConfig, cyPropertyServiceRef);

				CyCLFactory service = new CyCLFactoryImpl();

        if (CyCL.getDevices().size() == 0) {
				  System.out.println("No devices found");
        } else {
          System.out.println("Top device after init = " + CyCL.getDevices().get(0).getName());
        }

				Properties properties = new Properties();
				registerService(context, service, CyCLFactory.class, properties);

				if (service.isInitialized() && CyCL.getDevices().size() > 0)
				{
					CyCLSettingsTaskFactory settingsTaskFactory = new CyCLSettingsTaskFactory(cyPropertyServiceRef);

					Properties settingsTaskFactoryProps = new Properties();
					settingsTaskFactoryProps.setProperty(PREFERRED_MENU, "Edit.Preferences");
					settingsTaskFactoryProps.setProperty(MENU_GRAVITY, "5.0");
					settingsTaskFactoryProps.setProperty(TITLE, "OpenCL Settings...");
					registerService(context, settingsTaskFactory, TaskFactory.class, settingsTaskFactoryProps);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});


	}
}
