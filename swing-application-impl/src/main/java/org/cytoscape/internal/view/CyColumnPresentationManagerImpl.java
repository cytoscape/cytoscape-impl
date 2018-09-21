package org.cytoscape.internal.view;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.application.swing.CyColumnPresentation;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.internal.util.IconUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;

public class CyColumnPresentationManagerImpl implements CyColumnPresentationManager {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private final CyColumnPresentation cytoscapePresentation;
	private final Map<String,CyColumnPresentation> presentations = new HashMap<>();
	
	
	public CyColumnPresentationManagerImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		cytoscapePresentation = getCytoscapePresentation(serviceRegistrar);
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
	
	
	// initialize built-in cytoscape presentation
	private CyColumnPresentation getCytoscapePresentation(CyServiceRegistrar serviceRegistrar) {
		IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		Font font = iconManager.getIconFont(IconUtil.CY_FONT_NAME, 15f);
		Color color = new Color(254, 193, 125);
		Icon icon = new TextIcon(IconUtil.CYTOSCAPE_LOGO, font, color, 16, 16);
		return new DefaultPresentation("Cytoscape", icon);
	}
	
	
	private Icon createDefaultIcon(String namespace) {
		String letter = " ";
		namespace = namespace.trim();
		if(!namespace.isEmpty())
			letter = namespace.substring(0, 1).toUpperCase();
		
		Color iconColor = getDefaultIconColor(namespace);
		Color textColor = getContrastingColor(iconColor);
		
		Font iconFont = serviceRegistrar.getService(IconManager.class).getIconFont(16f);
		Font textFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
		
		return new TextIcon(
				new String[] {IconManager.ICON_CIRCLE, letter},
				new Font[]   {iconFont,  textFont},
				new Color[]  {iconColor, textColor},
				16, 16);
	}
	
	
	private static Color getDefaultIconColor(String namespace) {
		// http://colorbrewer2.org/#type=qualitative&scheme=Paired&n=12
		int index = Math.abs(namespace.toLowerCase().hashCode() % 12);
		switch(index) {
			default:
			case 0:  return new Color(166,206,227);
			case 1:  return new Color(31,120,180);
			case 2:  return new Color(178,223,138);
			case 3:  return new Color(51,160,44);
			case 4:  return new Color(251,154,153);
			case 5:  return new Color(227,26,28);
			case 6:  return new Color(253,191,111);
			case 7:  return new Color(255,127,0);
			case 8:  return new Color(202,178,214);
			case 9:  return new Color(106,61,154);
			case 10: return new Color(255,255,153);
			case 11: return new Color(177,89,40);
		}
	}
	
	
	// copy-pasted from org.cytoscape.ding.internal.util.ColorUtil.getContrastingColor(Color)
	private static Color getContrastingColor(Color color) {
		int d = 0;
		// Counting the perceptive luminance - human eye favors green color...
		final double a = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

		if (a < 0.5)
			d = 0; // bright colors - black font
		else
			d = 255; // dark colors - white font

		return new Color(d, d, d);
	}
	
	
	private class DefaultPresentation implements CyColumnPresentation {
		
		private final String namespace;
		private final Icon icon;
		
		public DefaultPresentation(String namespace, Icon icon) {
			this.namespace = namespace;
			this.icon = icon == null ? createDefaultIcon(namespace) : icon;
		}
		
		public DefaultPresentation(String namespace) {
			this(namespace, null);
		}
		
		@Override
		public String getNamespaceDescription() {
			return namespace;
		}
		
		@Override
		public Icon getNamespaceIcon() {
			return icon;
		}
	}


}
