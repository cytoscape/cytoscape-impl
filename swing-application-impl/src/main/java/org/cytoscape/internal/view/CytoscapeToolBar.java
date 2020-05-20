package org.cytoscape.internal.view;

import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.internal.view.util.MenuScroller;
import org.cytoscape.internal.view.util.ToolbarWithOverflow;
import org.cytoscape.internal.view.util.ViewUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.CyToolTip;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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
 * Implementation of Toolbar on the Cytoscape Desktop application.
 */
@SuppressWarnings("serial")
public class CytoscapeToolBar extends ToolbarWithOverflow {
	
	public static int ICON_WIDTH = 32;
	public static int ICON_HEIGHT = 32;
	private static int BUTTON_BORDER_SIZE = 2;
	
	private static final String STOPLIST_FILENAME = "toolbar.stoplist";
	
	private List<ToolBarItem> orderedItems;
	private Map<CyAction, ToolBarItem> actionMap;
	private HashSet<String> stopList = new HashSet<>();

	private final PopupMouseListener popupMouseListener;

	public CytoscapeToolBar(CyServiceRegistrar serviceRegistrar) {
		super("Cytoscape Tools", serviceRegistrar);
		
		actionMap = new HashMap<>();
		orderedItems = new ArrayList<>();
		
		setFloatable(false);
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")),
				BorderFactory.createEmptyBorder(0, 10, 0, 10)
		));
		
		// Add listener that opens the tool bar customization popup
		popupMouseListener = new PopupMouseListener();
		addMouseListener(popupMouseListener);
		
		readStopList();
	}
	
	@Override
	public Component add(Component comp) {
		if (stopList.contains(comp.getName()))
			comp.setVisible(false);
		
		return super.add(comp);
	}
	
	/**
	 * If the given Action has an absent or false inToolBar property, return;
	 * otherwise toolBarComponent to addAction( String, Action ) with the value of its
	 * gravity property.
	 */
	public boolean addAction(CyAction action) {
		if (!action.isInToolBar()) 
			return false;
	
		// At present we allow an Action to be in this tool bar only once.
		if (actionMap.containsKey(action))
			return false;

		boolean sepBefore = false;
		boolean sepAfter = false;
		
		if (action instanceof AbstractCyAction) {
			sepBefore = ((AbstractCyAction) action).insertToolbarSeparatorBefore();
			sepAfter = ((AbstractCyAction) action).insertToolbarSeparatorAfter();
		}

		var button = createToolBarButton(action);
		button.addMouseListener(popupMouseListener);
		
		float gravity = action.getToolbarGravity();
		
		var tbc = new ToolBarItem(button, gravity, sepBefore, sepAfter);
		
		actionMap.put(action, tbc);
		int index = getInsertLocation(gravity);
		orderedItems.add(index, tbc);
		
		if (stopList.contains(action.getName()))
			tbc.getComponent().setVisible(false);

		update();

		return true;
	}
	
	public void addToolBarComponent(ToolBarComponent tbc, Map<?, ?> props) {
		boolean sepBefore = "true".equals(props.get(INSERT_SEPARATOR_BEFORE));
		boolean sepAfter = "true".equals(props.get(INSERT_SEPARATOR_AFTER));
		int index = getInsertLocation(tbc.getToolBarGravity());
		
		orderedItems.add(index, new ToolBarItem(tbc, sepBefore, sepAfter));
		
		update();
	}
	
	public void removeToolBarComponent(ToolBarComponent tbc) {
		if (tbc != null) {
			var iter = orderedItems.iterator();
			
			while (iter.hasNext()) {
				var item = iter.next();
				
				if (tbc.equals(item.getToolBarComponent()))
					iter.remove();
			}
			
			var c = tbc.getComponent();
			
			if (c != null) {
				remove(c);
				repaint();
			}
		}
	}

	public void showAll() {
		for (var item : orderedItems) {
			var c = item.getComponent();
			
			if (c != null)
				c.setVisible(true);
		}
		
		updateSeparators();
	}

	public void hideAll() {
		for (var item : orderedItems) {
			var c = item.getComponent();
			
			if (c != null)
				c.setVisible(false);
		}
		
		updateSeparators();
	}

	/**
	 * If the given Action has an absent or false inToolBar property, return;
	 * otherwise if there's a button for the action, remove it.
	 */
	public boolean removeAction(CyAction action) {
		var item = actionMap.remove(action);

		if (item == null)
			return false;

		orderedItems.remove(item);
		update();

		return true;
	}

	/**
	 * Used by toolbar updater to keep things properly enabled/disabled.
	 */
	Collection<CyAction> getAllToolBarActions() {
		return actionMap.keySet();
	}
	
	public static AbstractButton createToolBarButton(CyAction action) {
		action.updateEnableState();
		final AbstractButton button;
		
		if (action.useToggleButton()) {
			button = new JToggleButton(action) {
				@Override
				public JToolTip createToolTip() {
					return CytoscapeToolBar.createToolTip(this, action);
				}
				@Override
				public void paint(Graphics g) {
					if (isSelected()) {
						var g2 = (Graphics2D) g.create();
						g2.setColor(UIManager.getColor("CyToggleButton[Selected].background"));
						g2.fillRect(
								BUTTON_BORDER_SIZE,
								BUTTON_BORDER_SIZE,
								getWidth() - 2 * BUTTON_BORDER_SIZE,
								getHeight() - 2 * BUTTON_BORDER_SIZE
						);
						g2.dispose();
					}
					super.paint(g);
				}
			};
		} else {
			button = new JButton(action) {
				@Override
				public JToolTip createToolTip() {
					return CytoscapeToolBar.createToolTip(this, action);
				}
			};
		}
		
		button.setText(action.getName());
		button.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_SIZE, BUTTON_BORDER_SIZE, BUTTON_BORDER_SIZE,
				BUTTON_BORDER_SIZE));
		button.setRolloverEnabled(LookAndFeelUtil.isWinLAF());
		button.setFocusable(false);
		button.setFocusPainted(false);
		button.setHideActionText(true);
		
		Dimension dim = new Dimension(ICON_WIDTH + 2 * BUTTON_BORDER_SIZE, ICON_HEIGHT + 2 * BUTTON_BORDER_SIZE);
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.setMaximumSize(dim);

		Object iconObj = action.getValue(Action.LARGE_ICON_KEY);
		
		if (iconObj instanceof Icon) {
			Icon icon = (Icon) iconObj;
			
			if (icon.getIconWidth() > ICON_WIDTH || icon.getIconHeight() > ICON_HEIGHT) {
				icon = IconManager.resizeIcon(icon, Math.min(ICON_WIDTH, ICON_HEIGHT));
				button.setIcon(icon);
			}
			
			if (icon instanceof ImageIcon) {
				Image img = ((ImageIcon) icon).getImage();
				GrayFilter filter = new GrayFilter(true, 71);
		        ImageProducer prod = new FilteredImageSource(img.getSource(), filter);
		        Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
		        button.setDisabledIcon(new ImageIcon(grayImage));
			}
		}
		
		return button;
	}
	
	private static JToolTip createToolTip(JComponent comp, CyAction action) {
		return new CyToolTip(comp,
				(String) action.getValue(Action.SHORT_DESCRIPTION),
				(String) action.getValue(Action.LONG_DESCRIPTION),
				action.getToolTipImage() == null ? null : new ImageIcon(action.getToolTipImage()));
	}
	
	private void update() {
		// Remove and add everything again
		removeAll();
		
		for (var item : orderedItems) {
			var c = item.getComponent();
			
			if (c != null) {
				ToolBarItem ab = (ToolBarItem) item;
				
				if (ab.isSeparatorBefore())
					addSeparator();
				
				add(c);
				
				if (ab.isSeparatorAfter())
					addSeparator();
			} else if (item.isSeparator()) {
				addSeparator();
			}
		}
		
		// Hide duplicate separators
		updateSeparators();
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
	
	private int getInsertLocation(float newGravity) {
		for (int i = 0; i < orderedItems.size(); i++) {
			var item = orderedItems.get(i);
			float gravity = item.getGravity();
			
			if (gravity >= 0 && newGravity < gravity)
				return i;
		}
		
		return orderedItems.size();
	}
	
	void addSeparator(float gravity) {
		int index = getInsertLocation(gravity);
		orderedItems.add(index, new ToolBarItem(gravity));
	}
	
	private void updateSeparators() {
		// Pretend we start with a separator, because we don't want any separator as the first component
		boolean lastIsSep = true;
		
		for (Component c : getComponents()) {
			if (c instanceof JSeparator)
				c.setVisible(!lastIsSep);
			
			if (c.isVisible())
				lastIsSep = c instanceof JSeparator;
		}
		
		validate();
	}
	
	private void readStopList() {
		stopList.clear();
		List<String> lines = null;
		
		try {
			CyApplicationConfiguration cyApplicationConfiguration = serviceRegistrar
					.getService(CyApplicationConfiguration.class);
			
			if (cyApplicationConfiguration == null)
				return;

			File configDirectory = cyApplicationConfiguration.getConfigurationDirectoryLocation();
			File configFile = null;
			
			if (configDirectory.exists()) {
				configFile = new File(configDirectory.toPath() + "/" + STOPLIST_FILENAME);
				
				if (configFile.exists())
					lines = Files.readAllLines(configFile.toPath(), Charset.defaultCharset());
			}
			
			if (lines == null) {
				// Copy from our bundle resources file
				try (InputStream is = getClass().getResourceAsStream("/" + STOPLIST_FILENAME)) {
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
			
			for (String line : list) {
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
		public void mousePressed(MouseEvent e) {
			showPopup(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}
		
		private void showPopup(MouseEvent e) {
			if (!e.isPopupTrigger())
				return;
			
			var popup = new JPopupMenu();
			
			var menuItem = new JMenuItem("Show All");
			popup.add(menuItem);
			menuItem.addActionListener(ev -> {
				showAll();
				resave();
			});
			
			menuItem = new JMenuItem("Hide All");
			popup.add(menuItem);
			popup.addSeparator();
			menuItem.addActionListener(ev -> {
				hideAll();
				resave();
			});
			
			for (var comp : getComponents()) {
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
						if (icon.getIconWidth() > ICON_WIDTH || icon.getIconHeight() > ICON_HEIGHT)
							icon = IconManager.resizeIcon(icon, Math.min(ICON_WIDTH, ICON_HEIGHT));
						
						int originalWidth = icon.getIconWidth();
						
						icon = new ImageIcon(((ImageIcon) icon).getImage()) {
							@Override
							public int getIconWidth() { // To align the menu texts
								return ICON_WIDTH;
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
							Math.max(ICON_WIDTH, mi.getPreferredSize().height)
					));
					mi.addActionListener(ev -> {
						button.setVisible(!button.isVisible());
						updateSeparators();
						resave();
					});
					popup.add(mi);
				}
			}
			
			// Calculate max number of visible menu items before scrolling
			var window = SwingUtilities.getWindowAncestor(CytoscapeToolBar.this);
			
			if (window != null) {
				var gc = window.getGraphicsConfiguration();
				int sh = ViewUtil.getEffectiveScreenArea(gc).height;
				int ph = popup.getPreferredSize().height;
				
				if (ph > sh) {
					int h = 0;
					
					// Creates another MenuScroller to get the size of the added scroll buttons
					MenuScroller tmpScroller = MenuScroller.setScrollerFor(new JPopupMenu(), 1);
					h += tmpScroller.getUpItem().getPreferredSize().height;
					h += tmpScroller.getDownItem().getPreferredSize().height;
					tmpScroller.dispose();
					int sepIdx = -1;
					
					for (int count = 0; count <  popup.getComponentCount(); count++) {
						Component comp = popup.getComponent(count);
						
						if (comp instanceof JSeparator)
							sepIdx = count;
						else
							h += comp.getPreferredSize().height;
						
						if (h > sh) {
							if (sepIdx >= 0)
								popup.remove(sepIdx);
							
							MenuScroller.setScrollerFor(
									popup,
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
			
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	private static class ToolBarItem {
		
		private final ToolBarComponent toolBarComponent;
		private final Component component;
		private final boolean separator;
		private final boolean separatorBefore;
		private final boolean separatorAfter;
		private float gravity = -1.0f;
		
		public ToolBarItem(
				Component comp,
				float gravity,
				boolean separatorBefore,
				boolean separatorAfter
		) {
			this.toolBarComponent = null;
			this.component = comp;
			this.separator = false;
			this.gravity = gravity;
			this.separatorBefore = separatorBefore;
			this.separatorAfter = separatorAfter;
		}
		
		public ToolBarItem(ToolBarComponent toolBarComponent, boolean separatorBefore, boolean separatorAfter) {
			this.toolBarComponent = toolBarComponent;
			this.component = null;
			this.separator = false;
			this.separatorBefore = separatorBefore;
			this.separatorAfter = separatorAfter;
		}
		
		/**
		 * Use this constructor to create an object that represents a separator.
		 */
		public ToolBarItem(float gravity) {
			this.toolBarComponent = null;
			this.component = null;
			this.separator = true;
			this.gravity = gravity;
			this.separatorBefore = false;
			this.separatorAfter = false;
		}

		public ToolBarComponent getToolBarComponent() {
			return toolBarComponent;
		}
		
		public Component getComponent() {
			return toolBarComponent != null ? toolBarComponent.getComponent() : component;
		}
		
		public float getGravity() {
			return toolBarComponent != null ? toolBarComponent.getToolBarGravity() : gravity;
		}
		
		public boolean isSeparatorBefore() {
			return separatorBefore;
		}
		
		public boolean isSeparatorAfter() {
			return separatorAfter;
		}
		
		private boolean isSeparator() {
			return separator;
		}
	}
}
