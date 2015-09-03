package org.cytoscape.internal.layout.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.properties.TunablePropertySerializer;
import org.cytoscape.work.properties.TunablePropertySerializerFactory;
import org.cytoscape.work.swing.PanelTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayoutSettingsManager {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	
	private final CyServiceRegistrar serviceRegistrar;
	private final TunablePropertySerializerFactory serializerFactory;
	
	private final Map<String,CyProperty<Properties>> registeredPropertyServices = new HashMap<>();
	
	
	public LayoutSettingsManager(CyServiceRegistrar serviceRegistrar, TunablePropertySerializerFactory serializerFactory) {
		this.serviceRegistrar = serviceRegistrar;
		this.serializerFactory = serializerFactory;
	}
	

	public void addLayout(final CyLayoutAlgorithm layout, Map<?,?> props) {
		restoreLayoutContext(layout);
    }
    
    public void removeLayout(final CyLayoutAlgorithm layout, Map<?,?> props) {
    	// Do nothing
    }
	    
    
    private void restoreLayoutContext(CyLayoutAlgorithm layout) {
    	try {
			Object layoutContext = layout.getDefaultLayoutContext();
	        CyProperty<Properties> cyProperty = getPropertyService(layout);
			Properties propsBefore = cyProperty.getProperties();
	        if(!propsBefore.isEmpty()) {
	            // use the Properties to restore the values of the Tunable fields
	        	TunablePropertySerializer serializer = serializerFactory.createSerializer();
	            serializer.setTunables(layoutContext, propsBefore);
	        }
    	} catch (Exception e) {
    		logger.error("Error restoring layout settings for '" + layout.getName() + "'", e);
    	}
	}
    
    public void saveLayoutContext(PanelTaskManager taskMgr, CyLayoutAlgorithm layout) {
    	try {
	    	Object layoutContext = layout.getDefaultLayoutContext();
	    	taskMgr.validateAndApplyTunables(layoutContext);
	    	
	    	TunablePropertySerializer serializer = serializerFactory.createSerializer();
	    	Properties layoutProps = serializer.toProperties(layoutContext);
	    	
	    	// No need to save empty props
	    	if(!layoutProps.isEmpty()) {
	        	CyProperty<Properties> cyProperty = getPropertyService(layout);
		        cyProperty.getProperties().clear();
		        cyProperty.getProperties().putAll(layoutProps);	
	    	}
    	} catch (Exception e) {
    		logger.error("Error saving layout settings for '" + layout.getName() + "'", e);
    	}
	}
	
	
	private synchronized CyProperty<Properties> getPropertyService(CyLayoutAlgorithm layout) {
		CyProperty<Properties> service = registeredPropertyServices.get(layout.getName());
		if(service == null) {
			service = PropsReader.forLayout(layout);
			Properties serviceProps = new Properties();
			serviceProps.setProperty("cyPropertyName", service.getName());
			serviceRegistrar.registerAllServices(service, serviceProps);
			registeredPropertyServices.put(layout.getName(), service);
		}
		return service;
	}
	
	
	private static class PropsReader extends AbstractConfigDirPropsReader {
        public PropsReader(String name, String fileName) {
            super(name, fileName, SavePolicy.CONFIG_DIR);
        }
        public static PropsReader forLayout(CyLayoutAlgorithm layout) {
        	String name = "layout." + layout.getName();
        	return new PropsReader(name, name + ".props");
        }
    }
}
