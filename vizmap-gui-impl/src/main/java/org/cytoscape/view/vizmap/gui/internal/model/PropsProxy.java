package org.cytoscape.view.vizmap.gui.internal.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;
import org.cytoscape.view.vizmap.gui.internal.util.NotificationNames;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.puremvc.java.multicore.patterns.proxy.Proxy;

public class PropsProxy extends Proxy implements PropertyUpdatedListener {

	public static final String NAME = "VizMapperPropsProxy";
	public static final String DEF_VISUAL_PROPS_TOKEN = "defaultVisualProperties";
	
	private final PropsReader propsReader;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public PropsProxy(final ServicesUtil servicesUtil) {
		super(NAME);
		
		propsReader = new PropsReader();
		Properties props = new Properties();
		props.setProperty("cyPropertyName", PropsReader.NAME);
		servicesUtil.registerAllServices(propsReader, props);
		
		setData(propsReader.getProperties());
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public Properties getProperties() {
		return propsReader.getProperties();
	}
	
	public Set<String> getDefaultVisualProperties(final Class<? extends CyIdentifiable> targetDataType) {
		final String key = DEF_VISUAL_PROPS_TOKEN + "." + toPropsToken(targetDataType);
		final Properties props = getProperties();
		final String[] val = props.getProperty(key, "").split(",");
		
		return new HashSet<String>(Arrays.asList(val));
	}
	
	public void setDefaultVisualProperties(final Class<? extends CyIdentifiable> targetDataType,
										   final Set<String> idSet) {
		if (targetDataType != null && idSet != null) {
			final String key = DEF_VISUAL_PROPS_TOKEN + "." + toPropsToken(targetDataType);
			String value = "";
			
			if (!idSet.isEmpty()) {
				final StringBuilder sb = new StringBuilder();
				final Iterator<String> iter = idSet.iterator();
				
			    while (iter.hasNext()) {
			        sb.append(iter.next());
			        if (iter.hasNext()) sb.append(",");
			    }
			    
			    value = sb.toString();
			}
			
			final Properties props = propsReader.getProperties();
			props.put(key, value);
			
			setData(props);
		}
	}
	
	@Override
	public void handleEvent(final PropertyUpdatedEvent e) {
		final CyProperty<?> cyProp = e.getSource();
		
		if (cyProp != null && PropsReader.NAME.equals(cyProp.getName())) {
			setData(cyProp.getProperties());
			sendNotification(NotificationNames.VIZMAP_PROPS_CHANGED, getProperties());
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private static String toPropsToken(final Class<? extends CyIdentifiable> targetDataType) {
		return targetDataType.getSimpleName().toLowerCase().replace("cy", "");
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private static class PropsReader extends AbstractConfigDirPropsReader {

		static final String NAME = "vizmapper";
		static final String FILE_NAME = "vizmapper.props";
		
		public PropsReader() {
			super(NAME, FILE_NAME, SavePolicy.CONFIG_DIR);
		}
	}
}
