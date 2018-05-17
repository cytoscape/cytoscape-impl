package org.cytoscape.internal.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cytoscape.application.swing.CyColumnPresentation;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.internal.util.RandomImage;
import org.cytoscape.util.swing.TextIcon;

public class CyColumnPresentationManagerImpl implements CyColumnPresentationManager {
	
	private static final String FONT_PATH = "/fonts/cytoscape-3.ttf";

	private final CyColumnPresentation cytoscapePresentation;
	private final Map<String,CyColumnPresentation> presentations = new HashMap<>();
	
	public CyColumnPresentationManagerImpl() {
		CyColumnPresentation defaultPresentation;
		URL url = getClass().getResource(FONT_PATH);
		try(InputStream in = url.openStream()) {
			Font font = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(15f);
			Color color = new Color(254, 193, 125);
			Icon icon = new TextIcon("b", font, color, 16, 16);
			defaultPresentation = new DefaultPresentation("Cytoscape", icon);
		} catch (IOException | FontFormatException e) {
			defaultPresentation = new DefaultPresentation("Cytoscape");
		}
		this.cytoscapePresentation = defaultPresentation;
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
