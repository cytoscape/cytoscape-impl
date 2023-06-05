package org.cytoscape.internal.view.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.slf4j.Logger;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public final class ViewUtil {

	public static enum NetworksSortMode {
		/** To sort by the original creation positions */
		CREATION,
		/** To sort alphabetically (ASC) by the network name */
		NAME;
		
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	public static final int DIVIDER_SIZE = 5;
	
	public static final String CY_PROPERTY_NAME = "(cyPropertyName=cytoscape3.props)";
	public static final String SHOW_NODE_EDGE_COUNT_KEY = "showNodeEdgeCount";
	public static final String SHOW_NETWORK_PROVENANCE_HIERARCHY_KEY = "showNetworkProvenanceHierarchy";
	public static final String DEFAULT_PROVIDER_PROP_KEY = "networkSearch.defaultProvider";
	
	public static final String PARENT_NETWORK_COLUMN = "__parentNetwork.SUID";
	
	public static final Border DESELECTED_TOGLLE_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);
	public static final Border SELECTED_TOGLLE_BORDER = DESELECTED_TOGLLE_BORDER;
	
	private static final String DEF_ICON_REGEX = "([A-Z]?[0-9a-z]+)|([A-Z]+)"; // Finds camelCase and PascalCase groups
	private static final Pattern DEF_ICON_PATTERN = Pattern.compile(DEF_ICON_REGEX);
	
	public static String getName(CyNetwork network) {
		var name = "";
		
		try {
			name = network.getRow(network).get(CyNetwork.NAME, String.class);
		} catch (Exception e) {
		}
		
		if (name == null || name.trim().isEmpty())
			name = "? (SUID: " + network.getSUID() + ")";
		
		return name;
	}
	
	public static String getTitle(CyNetworkView view) {
		var title = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		
		if (title == null || title.trim().isEmpty())
			title = getName(view.getModel());
		
		return title;
	}
	
	public static int getHiddenNodeCount(CyNetworkView view) {
		int count = 0;
		
		if (view != null) {
			for (var nv : view.getNodeViews()) {
				if (nv.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE) == Boolean.FALSE)
					count++;
			}
		}
		
		return count;
	}
	
	public static int getHiddenEdgeCount(CyNetworkView view) {
		int count = 0;
		
		if (view != null) {
			for (var ev : view.getEdgeViews()) {
				if (ev.getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE) == Boolean.FALSE)
					count++;
			}
		}
		
		return count;
	}
	
	public static String createUniqueKey(CyNetworkView view) {
		return "__CyNetworkView_" + view.getSUID();
	}
	
	public static String createUniqueKey(CyNetwork net) {
		return "__CyNetwork_" + (net != null ? net.getSUID() : "null");
	}
	
	public static CySubNetwork getParent(CySubNetwork net, CyServiceRegistrar serviceRegistrar) {
		var hiddenTable = net.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		var row = hiddenTable != null ? hiddenTable.getRow(net.getSUID()) : null;
		var suid = row != null ? row.get(PARENT_NETWORK_COLUMN, Long.class) : null;
		
		if (suid != null) {
			var parent = serviceRegistrar.getService(CyNetworkManager.class).getNetwork(suid);
			
			if (parent instanceof CySubNetwork)
				return (CySubNetwork) parent;
		}
		
		return null;
	}
	
	public static List<CySubNetwork> getSessionNetworks(CyServiceRegistrar serviceRegistrar) {
		var netMgr = serviceRegistrar.getService(CyNetworkManager.class);
		var sortedNetworks = new ArrayList<CySubNetwork>();
		
		for (var n : netMgr.getNetworkSet()) {
			if (n instanceof CySubNetwork && netMgr.networkExists(n.getSUID()))
				sortedNetworks.add((CySubNetwork) n);
		}
		
		return sortedNetworks;
	}
	
	public static void sortNetworksByCreationPos(List<? extends CyNetwork> networks, Map<Long, Integer> netPos) {
		Collections.sort(networks, new Comparator<CyNetwork>() {
			@Override
			public int compare(CyNetwork n1, CyNetwork n2) {
				Integer o1 = netPos.get(n1.getSUID());
				Integer o2 = netPos.get(n2.getSUID());
				if (o1 == null) o1 = -1;
				if (o2 == null) o2 = -1;
				
				return o1.compareTo(o2);
			}
		});
	}
	
	public static void sortNetworksByName(List<? extends CyNetwork> networks) {
		var collator = Collator.getInstance();
		
		Collections.sort(networks, new Comparator<CyNetwork>() {
			@Override
			public int compare(CyNetwork n1, CyNetwork n2) {
				var name1 = getName(n1);
				var name2 = getName(n2);
				
				return collator.compare(name1, name2);
			}
		});
	}
	
	public static void styleToolBarButton(AbstractButton btn) {
		styleToolBarButton(btn, null, true);
	}
	
	public static void styleToolBarButton(AbstractButton btn, boolean addPadding) {
		styleToolBarButton(btn, null, addPadding);
	}
	
	public static void styleToolBarButton(AbstractButton btn, Font font) {
		styleToolBarButton(btn, font, true);
	}
	
	public static void styleToolBarButton(AbstractButton btn, Font font, boolean addPadding) {
		int hPad = addPadding ? 5 : 0;
		int vPad = addPadding ? 4 : 0;
		styleToolBarButton(btn, font, hPad, vPad);
	}
	
	public static void styleToolBarButton(AbstractButton btn, Font font, int hPad, int vPad) {
		if (font != null)
			btn.setFont(font);
		
		// Decrease the padding, because it will have a border
//		if (btn instanceof JToggleButton) {
//			hPad = Math.max(0, hPad - 4);
//			vPad = Math.max(0, vPad - 4);
//		}
		
		btn.setFocusPainted(false);
		btn.setFocusable(false);
		btn.setBorder(BorderFactory.createEmptyBorder());
		btn.setContentAreaFilled(false);
		btn.setOpaque(true);
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setVerticalTextPosition(SwingConstants.TOP);
		
		if (hPad > 0 || vPad > 0) {
			var d = btn.getPreferredSize();
			d = new Dimension(d.width + 2 * hPad, d.height + 2 * vPad);
			btn.setPreferredSize(d);
			btn.setMinimumSize(d);
			btn.setMaximumSize(d);
			btn.setSize(d);
		}
		
		if (btn instanceof JToggleButton) {
			btn.addItemListener(evt -> updateToolBarStyle((JToggleButton) btn));
			updateToolBarStyle((JToggleButton) btn);
		}
	}
	
	public static void updateToolBarStyle(JToggleButton btn) {
		updateToolBarStyle(btn, true);
	}
	
	public static void updateToolBarStyle(JToggleButton btn, boolean showSelectionBorder) {
		var defBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		
		if (btn.isEnabled()) {
			var selBorder = showSelectionBorder ?
					BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager.getColor("CyToggleButton[Selected].borderColor")),
							BorderFactory.createEmptyBorder(1, 1, 1, 1))
					: defBorder;
			
			btn.setBorder(btn.isSelected() ? selBorder : defBorder);
			btn.setBackground(
					btn.isSelected() ?
					UIManager.getColor("CyToggleButton[Selected].background") :
					UIManager.getColor("CyToggleButton.background"));
			btn.setForeground(
					btn.isSelected() ?
					UIManager.getColor("CyToggleButton[Selected].foreground") :
					UIManager.getColor("CyToggleButton.foreground"));
		} else {
			btn.setBorder(defBorder);
			btn.setForeground(UIManager.getColor("ToggleButton.disabledForeground"));
			btn.setBackground(UIManager.getColor("CyToggleButton.unselectedBackground"));
		}
	}
	
	public static JSeparator createToolBarSeparator() {
		var sep = new ToolBarSeparator(JSeparator.VERTICAL);
		sep.setForeground(UIManager.getColor("Separator.foreground"));
		
		return sep;
	}
	
	public static Icon resizeIcon(Icon icon, int maxHeight) {
		int height = icon.getIconHeight(), width = icon.getIconWidth();

		if (height <= maxHeight)
			return icon;

		int newHeight = maxHeight;
		int newWidth = Math.round(width * (newHeight / (float) height));
		
		final Icon resizedIcon;
		
		if (icon instanceof TextIcon) {
			var ti = (TextIcon) icon;
			resizedIcon = new TextIcon(ti, newWidth, newHeight);
		} else {
			var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			var g = img.createGraphics();
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
	
			var resizedImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
			resizedIcon = new ImageIcon(resizedImage);
		}
		
		return resizedIcon;
	}
	
	public static String getViewProperty(String key, CyServiceRegistrar serviceRegistrar) {
		return getViewProperty(key, null, serviceRegistrar);
	}
	
	public static String removeHtmlTags(String text) {
		if(text == null)
			return null;
		
		var label = new JLabel(text);
		var htmlProperty = label.getClientProperty("html");
		
		if(htmlProperty instanceof View view) {
			var doc = view.getDocument();
            if(doc instanceof StyledDocument) {
                try {
					return doc.getText(0, doc.getLength());
				} catch (BadLocationException e) { }
            }
		}
		
		return text;
	}
	
	@SuppressWarnings("unchecked")
	public static String getViewProperty(String key, String defaultValue, CyServiceRegistrar serviceRegistrar) {
		CyProperty<Properties> cyProps = serviceRegistrar.getService(CyProperty.class, CY_PROPERTY_NAME);

		return cyProps.getProperties().getProperty(key, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public static void setViewProperty(String key, String value, CyServiceRegistrar serviceRegistrar) {
		CyProperty<Properties> cyProps = serviceRegistrar.getService(CyProperty.class, CY_PROPERTY_NAME);
		cyProps.getProperties().setProperty(key, value);
	}
	
	public static Rectangle getEffectiveScreenArea(GraphicsConfiguration gc) {
		var bounds = gc.getBounds();
		var screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
		var rect = new Rectangle();
		rect.x = bounds.x + screenInsets.left;
		rect.y = bounds.y + screenInsets.top;
		rect.height = bounds.height - screenInsets.top - screenInsets.bottom;
		rect.width = bounds.width - screenInsets.left - screenInsets.right;
		
		return rect;
	}
	
	public static Window getWindowAncestor(ActionEvent evt, CySwingApplication swingApplication) {
		Window window = null;
		
		if (evt.getSource() instanceof JMenuItem) {
			if (swingApplication.getJMenuBar() != null)
				window = SwingUtilities.getWindowAncestor(swingApplication.getJMenuBar());
		} else if (evt.getSource() instanceof Component) {
			window = SwingUtilities.getWindowAncestor((Component) evt.getSource());
		}
		
		if (window == null)
			window = swingApplication.getJFrame();
		
		return window;
	}
	
	public static void recursiveDo(Component component, Consumer<JComponent> c) {
		if (component instanceof JComponent)
			c.accept((JComponent) component);
		
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents())
				recursiveDo(child, c);
		}
	}
	
	@SuppressWarnings("serial")
	public static void makeSmall(JComponent... components) {
		if (components == null || components.length == 0)
			return;

		for (var c : components) {
			if (LookAndFeelUtil.isAquaLAF()) {
				c.putClientProperty("JComponent.sizeVariant", "small");
			} else {
				if (c.getFont() != null)
					c.setFont(c.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			}

			if (c instanceof JList) {
				((JList<?>) c).setCellRenderer(new DefaultListCellRenderer() {
					@Override
					public Component getListCellRendererComponent(JList<?> list, Object value, int index,
							boolean isSelected, boolean cellHasFocus) {
						super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
						setFont(getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));

						return this;
					}
				});
			} else if (c instanceof JMenuItem) {
				c.setFont(c.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			}
		}
	}
	
	public static boolean hasVisibleOwnedWindows(Window window) {
		var ownedWindows = window != null ? window.getOwnedWindows() : null;
		
		if (ownedWindows == null || ownedWindows.length == 0)
			return false;
		
		for (Window w : ownedWindows) {
			if (w.isVisible())
				return true;
		}
	
		return false;
	}
	
	/**
	 * Utility method that invokes the code in Runnable.run on the AWT Event Dispatch Thread.
	 * @param runnable
	 */
	public static void invokeOnEDT(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}
	
	public static void invokeOnEDTAndWait(Runnable runnable) {
		invokeOnEDTAndWait(runnable, null);
	}
	
	public static void invokeOnEDTAndWait(Runnable runnable, Logger logger) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (Exception e) {
				if (logger != null)
					logger.error("Unexpected error", e);
				else
					e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return true if Aqua LAF and system property to "use Screen Menu Bar" is "true".
	 */
	public static boolean isScreenMenuBar() {
		return LookAndFeelUtil.isAquaLAF() && "true".equals(System.getProperty("apple.laf.useScreenMenuBar"));
	}
	
	public static Icon createDefaultIcon(String title, int size, IconManager iconManager) {
		var text = createDefaultIconText(title);
		var iconColor = getDefaultIconColor(title);
		var textColor = getContrastingColor(iconColor);
		var shape = text.length() > 1 ? IconManager.ICON_SQUARE : IconManager.ICON_CIRCLE;

		var iconFont = iconManager.getIconFont(size * 1.125f);
		int fontSize = (int) Math.round(size / (text.length() > 1 ? 1.6 : 1.3));
		var textFont = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
		
		return new TextIcon(
				new String[] { shape, text },
				new Font[]   { iconFont,  textFont },
				new Color[]  { iconColor, textColor },
				size, size
		);
	}

	// copy-pasted from org.cytoscape.ding.internal.util.ColorUtil.getContrastingColor(Color)
	public static Color getContrastingColor(Color color) {
		int d = 0;
		// Counting the perceptive luminance - human eye favors green color...
		double a = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

		if (a < 0.5)
			d = 0; // bright colors - black font
		else
			d = 255; // dark colors - white font

		return new Color(d, d, d);
	}
	
	static String createDefaultIconText(String title) {
		var text = "";
		title = title.trim();

		if (!title.isEmpty()) {
			Matcher matcher = DEF_ICON_PATTERN.matcher(title);

			DONE:
			while (matcher.find()) {
				for (int i = 1; i <= matcher.groupCount(); i++) {
					var s = matcher.group(i);
					s = s != null ? s.trim() : "";

					if (!s.isEmpty())
						text += s.substring(0, 1);
					
					if (text.length() == 2)
						break DONE;
				}
			}
			
			if (text.isEmpty())
				text = title.substring(0, 1);
		}

		return text.isEmpty() ? text = " " : text;
	}
	
	private static Color getDefaultIconColor(String text) {
		// http://colorbrewer2.org/#type=qualitative&scheme=Set1&n=8 (RED excluded!)
		int index = Math.abs(text.toLowerCase().hashCode() % 7);
		
		switch (index) {
			default:
			case 0:  return new Color(55, 126, 184);
			case 1:  return new Color(77, 175, 74);
			case 2:  return new Color(152, 78, 163);
			case 3:  return new Color(255, 127, 0);
			case 4:  return new Color(255, 255, 51);
			case 5:  return new Color(166, 86, 40);
			case 6:  return new Color(247, 129, 191);
		}
	}
	
	private ViewUtil() {
	}
	
	@SuppressWarnings("serial")
	private static class ToolBarSeparator extends JSeparator {

		ToolBarSeparator(int orientation) {
			super(orientation);
		}
		
		@Override
		public void paint(Graphics g) {
			var s = getSize();

			if (getOrientation() == JSeparator.VERTICAL) {
				g.setColor(getForeground());
				g.drawLine(0, 0, 0, s.height);
			} else {
				g.setColor(getForeground());
				g.drawLine(0, 0, s.width, 0);
			}
		}
		
		@Override
		public Dimension getPreferredSize() {
			if (getOrientation() == JSeparator.VERTICAL)
				return new Dimension(1, 0);
			else
				return new Dimension(0, 1);
		}
	}
}
