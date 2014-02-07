package org.cytoscape.view.vizmap.gui.internal.util;

import java.util.Map;

import org.cytoscape.util.swing.GravityTracker;
import org.cytoscape.work.ServiceProperties;

public final class ServicePropertiesUtil implements ServiceProperties {

	public static final String SERVICE_TYPE = "service.type";
	public static final String MENU_ID = "menu";
	public static final String GRAVITY = "gravity";
	
	public static final String MAIN_MENU = "main";
	public static final String CONTEXT_MENU = "context";

	public static String getServiceType(final Map<?, ?> properties) {
		return getString(properties, SERVICE_TYPE, null);
	}
	
	public static String getTitle(final Map<?, ?> properties) {
		return getString(properties, TITLE, null);
	}
	
	public static double getGravity(final Map<?, ?> properties) {
		double gravity = GravityTracker.USE_ALPHABETIC_ORDER;
		
		try {
			gravity = properties.get(GRAVITY) != null ?
					Double.parseDouble(properties.get(GRAVITY).toString()) : gravity;
		} catch (Exception e) { }
		
		return gravity;
	}
	
	public static boolean getInsertSeparatorBefore(final Map<?, ?> properties) {
		return getBoolean(properties, INSERT_SEPARATOR_BEFORE, false);
	}
	
	public static boolean getInsertSeparatorAfter(final Map<?, ?> properties) {
		return getBoolean(properties, INSERT_SEPARATOR_AFTER, false);
	}
	
	public static boolean getBoolean(final Map<?, ?> properties, final String key, boolean def) {
		boolean b = def;
		
		try {
			b = Boolean.parseBoolean(properties.get(key) != null ? properties.get(key).toString() : "false");
		} catch (Exception e) { }
		
		return b;
	}
	
	public static String getString(final Map<?, ?> properties, final String key, String def) {
		return properties.get(key) != null ? properties.get(key).toString() : def;
	}
	
	private ServicePropertiesUtil() {
		// restrict instantiation
	}
}
