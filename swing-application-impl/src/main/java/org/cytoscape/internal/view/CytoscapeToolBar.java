package org.cytoscape.internal.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.internal.view.util.CyToolBar;
import org.cytoscape.internal.view.util.MenuScroller;
import org.cytoscape.internal.view.util.ViewUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;

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

/**
 * The main Cytoscape toolbar.
 */
@SuppressWarnings("serial")
public class CytoscapeToolBar extends CyToolBar {
	
	private static final String STOPLIST_FILENAME = "toolbar.stoplist";
	
	private final Set<String> stopList = new HashSet<>();

	private final JPopupMenu prefPopup;
	private final PopupMouseListener popupMouseListener;
	
	private ComponentAdapter componentAdapter;
	
	public CytoscapeToolBar(CyServiceRegistrar serviceRegistrar) {
		super("Cytoscape Tools", HORIZONTAL, serviceRegistrar);
		
		// Add listener that opens the tool bar customization popup
		prefPopup = new JPopupMenu();
		popupMouseListener = new PopupMouseListener();
		addMouseListener(popupMouseListener);
		
		// Read list of hidden components
		readStopList();
	}
	
	@Override
	public void removeNotify() {
		super.removeNotify();
		
		if (componentAdapter != null)
			removeComponentListener(componentAdapter);
	}
	
	/**
	 * If the given Action has an absent or false inToolBar property, return;
	 * otherwise toolBarComponent to addAction( String, Action ) with the value of its
	 * gravity property.
	 */
	@Override
	public AbstractButton addAction(CyAction action) {
		var button = super.addAction(action);
		
		if (button != null && button.isVisible()) {
			if (stopList.contains(action.getName())) {
				button.setVisible(false);
				update();
			}
		}

		return button;
	}
	
	@Override
	public Component add(Component comp) {
		if (stopList.contains(comp.getName()))
			comp.setVisible(false);
		
		return super.add(comp);
	}

	private void resave() {
		var hidden = new ArrayList<String>();
		
		for (var c : getComponents()) {
			if (c instanceof AbstractButton) {
				if (!c.isVisible()) {
					var button = (AbstractButton) c;
					var name = ((CyAction) button.getAction()).getName();
					hidden.add(name);
				}
			}
		}
		
		writeStopList(hidden);
	}
	
	private void readStopList() {
		stopList.clear();
		List<String> lines = null;
		
		try {
			var applicationConfig = serviceRegistrar.getService(CyApplicationConfiguration.class);
			
			if (applicationConfig == null)
				return;

			File configDirectory = applicationConfig.getConfigurationDirectoryLocation();
			File configFile = null;
			
			if (configDirectory.exists()) {
				configFile = new File(configDirectory.toPath() + "/" + STOPLIST_FILENAME);
				
				if (configFile.exists())
					lines = Files.readAllLines(configFile.toPath(), Charset.defaultCharset());
			}
			
			if (lines == null) {
				// Copy from our bundle resources file
				try (var is = getClass().getResourceAsStream("/" + STOPLIST_FILENAME)) {
					if (is != null)
						lines = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset())).lines()
								.collect(Collectors.toList());
				}
			}
		} catch (IOException e) {
			// file not found: there's no customization, just return
			return;
		}

		if (lines != null) {
			for (String line : lines)
				stopList.add(line.trim());
		}
	}
	
	private void writeStopList(List<String> list) {
		BufferedWriter writer = null;
		
		try {
			var applicationConfig = serviceRegistrar.getService(CyApplicationConfiguration.class);
			
			if (applicationConfig == null) {
				System.err.println("cyApplicationConfiguration not found");
				return;
			}

			File configDirectory = applicationConfig.getConfigurationDirectoryLocation();
			File configFile = null;
			
			if (configDirectory.exists())
				configFile = new File(configDirectory.toPath() + "/" + STOPLIST_FILENAME);
			
			writer = new BufferedWriter(new FileWriter(configFile));
			
			for (var line : list) {
				if (line != null)
					writer.write(line + "\n");
			}
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class PopupMouseListener extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent evt) {
			showPopup(evt);
		}
		
		@Override
		public void mouseReleased(MouseEvent evt) {
			showPopup(evt);
		}
		
		private void showPopup(MouseEvent evt) {
			if (!evt.isPopupTrigger())
				return;
			
			var source = evt.getComponent();
			
			// Do not show this popup on top of the overflow popup!
			if (source != null && overflowToolBar.getComponentIndex(source) >= 0)
				return;
			
			prefPopup.removeAll();
			
			var menuItem = new JMenuItem("Show All");
			prefPopup.add(menuItem);
			menuItem.addActionListener(e -> {
				showAll();
				resave();
			});
			
			menuItem = new JMenuItem("Hide All");
			prefPopup.add(menuItem);
			prefPopup.addSeparator();
			menuItem.addActionListener(e -> {
				hideAll();
				resave();
			});
			
			for (var item : orderedItems) {
				var comp = item.getComponent();
				
				if (comp instanceof AbstractButton) {
					var button = (AbstractButton) comp;
					String tip = button.getToolTipText();
					
					if (tip == null || tip.isEmpty())
						continue;
					
					var mi = new JCheckBoxMenuItem();
					mi.setText(tip);
					mi.setState(button.isVisible());
					
					var icon = button.getIcon();
					
					if (icon instanceof ImageIcon) {
						if (icon.getIconWidth() > iconWidth || icon.getIconHeight() > iconHeight)
							icon = IconManager.resizeIcon(icon, Math.min(iconWidth, iconHeight));
						
						int originalWidth = icon.getIconWidth();
						
						icon = new ImageIcon(((ImageIcon) icon).getImage()) {
							@Override
							public int getIconWidth() { // To align the menu texts
								return iconWidth;
							}
							@Override
							public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
								// Center the icon horizontally
								g.translate((getIconWidth() - originalWidth) / 2, 0);
								super.paintIcon(c, g, x, y);
							}
						};
					}
					
					mi.setIcon(icon);
					mi.setPreferredSize(new Dimension(
							mi.getPreferredSize().width,
							Math.max(iconWidth, mi.getPreferredSize().height)
					));
					mi.addActionListener(ev -> {
						button.setVisible(!button.isVisible());
						updateSeparators(CytoscapeToolBar.this);
						resave();
					});
					prefPopup.add(mi);
				}
			}
			
			// Calculate max number of visible menu items before scrolling
			var window = SwingUtilities.getWindowAncestor(CytoscapeToolBar.this);
			
			if (window != null) {
				var gc = window.getGraphicsConfiguration();
				int sh = ViewUtil.getEffectiveScreenArea(gc).height;
				int ph = prefPopup.getPreferredSize().height;
				
				if (ph > sh) {
					int h = 0;
					
					// Creates another MenuScroller to get the size of the added scroll buttons
					var tmpScroller = MenuScroller.setScrollerFor(new JPopupMenu(), 1);
					h += tmpScroller.getUpItem().getPreferredSize().height;
					h += tmpScroller.getDownItem().getPreferredSize().height;
					tmpScroller.dispose();
					int sepIdx = -1;
					
					for (int count = 0; count <  prefPopup.getComponentCount(); count++) {
						var comp = prefPopup.getComponent(count);
						
						if (comp instanceof JSeparator)
							sepIdx = count;
						else
							h += comp.getPreferredSize().height;
						
						if (h > sh) {
							if (sepIdx >= 0)
								prefPopup.remove(sepIdx);
							
							MenuScroller.setScrollerFor(
									prefPopup,
									Math.max(1, count - 2/*make sure it fits*/ - 2/*ignore 'Show/Hide All'*/),
									125,
									2, // (always show 'Show/Hide All' items on top)
									0
							);
							break;
						}
					}
				}
			}
			
			prefPopup.show(source, evt.getX(), evt.getY());
		}
	}
}
