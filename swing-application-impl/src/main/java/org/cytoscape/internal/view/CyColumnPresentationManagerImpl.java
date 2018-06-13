package org.cytoscape.internal.view;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cytoscape.application.swing.CyColumnPresentation;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.internal.util.IconUtil;
import org.cytoscape.internal.util.RandomImage;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;

public class CyColumnPresentationManagerImpl implements CyColumnPresentationManager {
	
	private final CyColumnPresentation cytoscapePresentation;
	private final Map<String,CyColumnPresentation> presentations = new HashMap<>();
	
	public CyColumnPresentationManagerImpl(CyServiceRegistrar serviceRegistrar) {
		Font font = serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 15f);
		Color color = new Color(254, 193, 125);
		Icon icon = new TextIcon(IconUtil.CYTOSCAPE_LOGO, font, color, 16, 16);
		cytoscapePresentation = new DefaultPresentation("Cytoscape", icon);
	}
	
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
			return cytoscapePresentation;
		return presentations.computeIfAbsent(namespace.toLowerCase(), DefaultPresentation::new);
	}
	
	
	private static class DefaultPresentation implements CyColumnPresentation {
		
		private final String description;
		private final Icon icon;
		
		public DefaultPresentation(String desc, Icon icon) {
			this.description = desc;
			this.icon = (icon == null) ? new ImageIcon(new RandomImage(16,16,desc.hashCode())) : icon;
		}
		
		public DefaultPresentation(String namespace) {
			this(namespace, null);
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
