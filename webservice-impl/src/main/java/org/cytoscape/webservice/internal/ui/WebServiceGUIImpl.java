package org.cytoscape.webservice.internal.ui;

import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.webservice.swing.WebServiceGUI;

public class WebServiceGUIImpl implements WebServiceGUI {
	Map<Class<?>, Window> clientWindowsByType;
	
	public WebServiceGUIImpl() {
		clientWindowsByType = new HashMap<Class<?>, Window>();
	}
	
	public void addClient(Class<?> webServiceClientType, Window window) {
		clientWindowsByType.put(webServiceClientType, window);
	}
	
	@Override
	public Window getWindow(Class<?> webServiceClientType) {
		return clientWindowsByType.get(webServiceClientType);
	}
}
