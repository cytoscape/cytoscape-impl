package org.cytoscape.internal.layout.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class LayoutSettingsManager {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private final Map<String,CyProperty<Properties>> registeredPropertyServices = new HashMap<>();
	
	private ExecutorService executorService;
	
	public LayoutSettingsManager(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		this.executorService = Executors.newCachedThreadPool(); // consumes no resources after all layouts have been registered
	}
	
	public void addLayout(final CyLayoutAlgorithm layout, Map<?,?> props) {
		executorService.execute(() -> {
			restoreLayoutContext(layout);
		}); 
    }
    
    public void removeLayout(final CyLayoutAlgorithm layout, Map<?,?> props) {
    	// Do nothing
    }
    
    private void restoreLayoutContext(CyLayoutAlgorithm layout) {
    	try {
			Object layoutContext = layout.getDefaultLayoutContext();
	        CyProperty<Properties> cyProperty = getPropertyService(layout);
			Properties propsBefore = cyProperty.getProperties();
	        
			if (!propsBefore.isEmpty()) {
	            // use the Properties to restore the values of the Tunable fields
	        	final TunablePropertySerializerFactory serializerFactory =
	        			serviceRegistrar.getService(TunablePropertySerializerFactory.class);
	        	final TunablePropertySerializer serializer = serializerFactory.createSerializer();
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
	    	
	    	final TunablePropertySerializerFactory serializerFactory =
        			serviceRegistrar.getService(TunablePropertySerializerFactory.class);
	    	final TunablePropertySerializer serializer = serializerFactory.createSerializer();
	    	final Properties layoutProps = serializer.toProperties(layoutContext);
	    	
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
