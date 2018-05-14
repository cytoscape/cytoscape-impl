package org.cytoscape.internal.view;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cytoscape.application.swing.CyColumnPresentation;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.internal.util.RandomImage;

public class CyColumnPresentationManagerImpl implements CyColumnPresentationManager {
	
	private static final String ICON_PATH = "/images/logo-light-96.png";

	private final CyColumnPresentation CYTOSCAPE_PRESENTATION = new DefaultPresentation("Cytoscape", ICON_PATH); 

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
		return presentations.computeIfAbsent(namespace.toLowerCase(), DefaultPresentation::new);
	}
	
	
	private static class DefaultPresentation implements CyColumnPresentation {
		
		private final String description;
		private final Icon icon;
		
		public DefaultPresentation(String description, String iconPath) {
			this.description = description;
			this.icon = new ImageIcon(getClass().getResource(iconPath));
		}
		
		public DefaultPresentation(String namespace) {
			this.description = namespace;
			this.icon = new ImageIcon(new RandomImage(16, 16, namespace.hashCode())); // assume namespace.toLowerCase() has already been done
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
