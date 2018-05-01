package org.cytoscape.internal.view;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cytoscape.application.swing.CyColumnPresentation;
import org.cytoscape.application.swing.CyColumnPresentationManager;

public class CyColumnPresentationManagerImpl implements CyColumnPresentationManager {
	
	private static final String ICON_PATH = "/images/logo-light-96.png";
	private static final String GREY_ICON_PATH = "/images/logo-light-grey-96.png";

	private final CyColumnPresentation CYTOSCAPE_PRESENTATION = new DefaultPresentation(ICON_PATH, "Cytoscape"); 
	
	private final Map<String,CyColumnPresentation> presentations = new HashMap<>();
	

	public void addPresentation(CyColumnPresentation presentation, Map<String,String> props) {
		String namespace = props.get(CyColumnPresentation.NAMESPACE);
		if(namespace != null) {
			presentations.put(namespace.toLowerCase(), presentation);
		}
	}
	
	public void removePresentation(CyColumnPresentation presentation, Map<String,String> props) {
		presentations.values().remove(presentation);
	}
	
	@Override
	public CyColumnPresentation getColumnPresentation(String namespace) {
		if(namespace == null)
			return CYTOSCAPE_PRESENTATION;
		return presentations.computeIfAbsent(namespace.toLowerCase(), k -> new DefaultPresentation(GREY_ICON_PATH, namespace));
	}
	
	
	private static class DefaultPresentation implements CyColumnPresentation {
		
		private Icon icon;
		private String description;
		
		public DefaultPresentation(String iconPath, String description) {
			this.description = description;
			URL url = getClass().getResource(iconPath);
			this.icon = new ImageIcon(url);
		}
		
		@Override
		public String getNamespaceDescription() {
			return description;
		}
		
		@Override
		public Icon getNamespaceIcon() {
			return icon;
		}
	}

}
