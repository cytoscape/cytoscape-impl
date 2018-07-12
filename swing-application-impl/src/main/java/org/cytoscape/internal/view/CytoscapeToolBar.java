package org.cytoscape.internal.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.JToolTip;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.CyToolTip;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
public class CytoscapeToolBar extends JToolBar {
	
	public static int ICON_WIDTH = 32;
	public static int ICON_HEIGHT = 32;
	private static int BUTTON_BORDER_SIZE = 2;
	
	private Map<CyAction, ActionButton> actionButtonMap;
	private List<Object> orderedList;
	private Map<Object, Float> componentGravity;
	private HashSet<String> stopList = new HashSet<>();

	private CyServiceRegistrar serviceRegistrar;
	
	public CytoscapeToolBar(CyServiceRegistrar serviceRegistrar) {
		super("Cytoscape Tools");
		actionButtonMap = new HashMap<>();
		componentGravity = new HashMap<>();
		orderedList = new ArrayList<>();
		this.serviceRegistrar = serviceRegistrar;
		
		setFloatable(false);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, (new JSeparator()).getForeground()));
		buildPopup();
//		createCustomToolbar();
		readStopList();
	}
	
	private void buildPopup() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}
			private void showPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					final JPopupMenu popup = new JPopupMenu();
					JMenuItem menuItem = new JMenuItem("Show All");
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
					
					for (Component comp : getComponents()) {
						if (comp instanceof JButton) {
							JButton button = (JButton) comp;
							String tip = button.getToolTipText();
							
							if (tip == null || tip.isEmpty())
								continue;
							
							JCheckBoxMenuItem mi = new JCheckBoxMenuItem();
							mi.setText(tip);
							mi.setState(button.isVisible());
							
							Icon icon = button.getIcon();
							
							if (icon instanceof ImageIcon) {
								icon = new ImageIcon(((ImageIcon) icon).getImage()) {
									@Override
									public int getIconWidth() {
										return ICON_WIDTH;
									}
									@Override
									public int getIconHeight() {
										return ICON_HEIGHT;
									}
								};
							}
							
							mi.setIcon(icon);
							mi.addActionListener(ev -> {
								button.setVisible(!button.isVisible());
								resave();
							});
							popup.add(mi);
						}
					}
					
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	private void resave() {
		List<String> hidden = new ArrayList<>();
		
		for (Component comp : getComponents()) {
			if (comp instanceof JButton)
				if (!comp.isVisible()) {
					String butnName = ((CyAction) ((JButton) comp).getAction()).getName();
					hidden.add(butnName);
				}
		}
		
		if (hidden.size() == 0)
			deleteStopList();
		else
			writeStopList(hidden);
	}

	@Override
	public Component add(Component comp) {
		if (stopList.contains(comp.getName()))
			comp.setVisible(false);
		
		return super.add(comp);
	}
	
	/**
	 * If the given Action has an absent or false inToolBar property, return;
	 * otherwise delegate to addAction( String, Action ) with the value of its
	 * gravity property.
	 */
	public boolean addAction(CyAction action) {
		if (!action.isInToolBar()) 
			return false;
	
		// At present we allow an Action to be in this tool bar only once.
		if (actionButtonMap.containsKey(action))
			return false;

		boolean insertSepBefore = false;
		boolean insertSepAfter = false;
		
		if (action instanceof AbstractCyAction) {
			insertSepBefore = ((AbstractCyAction) action).insertToolbarSeparatorBefore();
			insertSepAfter = ((AbstractCyAction) action).insertToolbarSeparatorAfter();
		}

		ActionButton button = new ActionButton(createToolBarButton(action), insertSepBefore, insertSepAfter);
		
		componentGravity.put(button, action.getToolbarGravity());
		actionButtonMap.put(action, button);
		int addIndex = getInsertLocation(action.getToolbarGravity());
		orderedList.add(addIndex, button);
		
		if (stopList.contains(action.getName()))
			button.component.setVisible(false);

		addComponents();

		return true;
	}

	
	private static class ActionButton {
		final JButton component;
		final boolean separatorBefore;
		final boolean separatorAfter;
		
		public ActionButton(JButton button, boolean separatorBefore, boolean separatorAfter) {
			this.component = button;
			this.separatorBefore = separatorBefore;
			this.separatorAfter = separatorAfter;
		}
	}
	
	
	public void showAll() {
		for (Object o : orderedList)
			if (o instanceof Component)
				((Component) o).setVisible(true);
	}

	public void hideAll() {
		for (Object o : orderedList)
			if (o instanceof Component)
				((Component) o).setVisible(false);
	}

	private void addComponents() {
		removeAll();
		
		for (Object o : orderedList) {
			if (o instanceof JButton)
				add((JButton) o);
			else if (o instanceof Float)
				addSeparator();
			else if (o instanceof ToolBarComponent)
				add(((ToolBarComponent) o).getComponent());
			else if (o instanceof ActionButton) {
				ActionButton ab = (ActionButton) o;
				if(ab.separatorBefore)
					addSeparator();
				add(ab.component);
				if(ab.separatorAfter)
					addSeparator();
			}
		}
		
		validate();
	}

	public void addSeparator(float gravity) {
		Float key = new Float(gravity);
		componentGravity.put(key, gravity);
		int addInd = getInsertLocation(gravity);
		orderedList.add(addInd, key);
	}

	private int getInsertLocation(float newGravity) {
		for (int i = 0; i < orderedList.size(); i++) {
			Object item = orderedList.get(i);
			Float gravity = componentGravity.get(item);
			
			if (gravity != null && newGravity < gravity)
				return i;
		}
		
		return orderedList.size();
	}

	/**
	 * If the given Action has an absent or false inToolBar property, return;
	 * otherwise if there's a button for the action, remove it.
	 */
	public boolean removeAction(CyAction action) {
		ActionButton button = actionButtonMap.remove(action);

		if (button == null)
			return false;

		orderedList.remove(button);
		addComponents();

		return true;
	}

	/**
	 * Used by toolbar updater to keep things properly enabled/disabled.
	 */
	Collection<CyAction> getAllToolBarActions() {
		return actionButtonMap.keySet();
	}
	
	public void addToolBarComponent(ToolBarComponent tbc){		
		componentGravity.put(tbc,tbc.getToolBarGravity());
		int addInd = getInsertLocation(tbc.getToolBarGravity());
		orderedList.add(addInd, tbc);
		addComponents();
	}

	public void removeToolBarComponent(ToolBarComponent tbc){
		if (tbc != null){
			this.componentGravity.remove(tbc);
			this.orderedList.remove(tbc);
			this.remove(tbc.getComponent());
			this.repaint();
		}	
	}
	
	public static JButton createToolBarButton(CyAction action) {
		action.updateEnableState();
		
		final JButton button = new JButton(action) {
			@Override
		      public JToolTip createToolTip() {
				return new CyToolTip(
						this,
						action.getName(),
						(String) action.getValue(Action.SHORT_DESCRIPTION),
						action.getToolTipImage() == null ? null : new ImageIcon(action.getToolTipImage())
				);
		      }
		};
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

		Object normalIcon = action.getValue(Action.LARGE_ICON_KEY);
		
		if (normalIcon instanceof ImageIcon) {
			Image normalImage = ((ImageIcon) normalIcon).getImage();
			GrayFilter filter = new GrayFilter(true, 71);
	        ImageProducer prod = new FilteredImageSource(normalImage.getSource(), filter);
	        Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
	        button.setDisabledIcon(new ImageIcon(grayImage));
		}
		
		return button;
	}
	
	private void readStopList() {
		stopList.clear();
		final List<String> lines;
		
		try {
			CyApplicationConfiguration cyApplicationConfiguration = serviceRegistrar
					.getService(CyApplicationConfiguration.class);
			
			if (cyApplicationConfiguration == null)
				return;

			File configDirectory = cyApplicationConfiguration.getConfigurationDirectoryLocation();
			File configFile = null;
			
			if (configDirectory.exists())
				configFile = new File(configDirectory.toPath() + "/toolbar.stoplist");
			
			lines = Files.readAllLines(configFile.toPath(), Charset.defaultCharset());
		} catch (IOException e) {
			// file not found: there's no customization, just return
			return;
		}

		for (String line : lines)
			stopList.add(line.trim());
	}
	
	private void deleteStopList() {
		CyApplicationConfiguration cyApplicationConfiguration = serviceRegistrar
				.getService(CyApplicationConfiguration.class);
		
		if (cyApplicationConfiguration == null) {
			System.err.println("cyApplicationConfiguration not found");
			return;
		}

		File configDirectory = cyApplicationConfiguration.getConfigurationDirectoryLocation();
		File configFile = null;
		
		if (configDirectory.exists()) {
			configFile = new File(configDirectory.toPath() + "/toolbar.stoplist");
			
			if (configFile.exists())
				configFile.delete();
		}
	}

	private void writeStopList(List<String> list) {
		BufferedWriter writer = null;
		
		try {
			CyApplicationConfiguration cyApplicationConfiguration = serviceRegistrar
					.getService(CyApplicationConfiguration.class);
			
			if (cyApplicationConfiguration == null) {
				System.err.println("cyApplicationConfiguration not found");
				return;
			}

			File configDirectory = cyApplicationConfiguration.getConfigurationDirectoryLocation();
			File configFile = null;
			
			if (configDirectory.exists())
				configFile = new File(configDirectory.toPath() + "/toolbar.stoplist");
			
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
	
//		public void createCustomToolbar() {
//			//get the file
//			// this doesn't work: ??  "~/CytoscapeConfiguration/toolbar.custom"
////			String configFilename = "/Users/adamtreister/CytoscapeConfiguration/toolbar.custom";
//			System.out.println("createCustomToolbar leaves early");
//			if (System.currentTimeMillis() > 1) return;
//			
//			List<String> lines;
//			try {
//				CyApplicationConfiguration cyApplicationConfiguration = registrar.getService(CyApplicationConfiguration.class);
//				if (cyApplicationConfiguration == null)
//					System.out.println("cyApplicationConfiguration not found");
//
//				File configDirectory = cyApplicationConfiguration.getConfigurationDirectoryLocation();
//				File configFile = null;
//				if (configDirectory.exists())
//					configFile = new File(configDirectory.toPath()  + "/toolbar.custom");
//				lines = Files.readAllLines(configFile.toPath(), Charset.defaultCharset() );
//			} catch (IOException e) {
//				// file not found: there's no customization, just return
//				System.out.println(e.getMessage());
//				return;
//			}
//			
//			System.out.println("createCustomToolbar");
//			System.out.println("read " + lines.size() + " lines");
//
//			for (CyAction a : getAllToolBarActions())
//				removeAction(a);
//			boolean lastItemWasSeparator = false;
//			for (String line : lines)
//			{
////				System.out.println(line);
//				if (line.trim().isEmpty()) continue;
//				if (line.trim().charAt(0) == '/' && !lastItemWasSeparator)
//				{
//					addSeparator();
//					lastItemWasSeparator = true;
//				}
//				else
//				{
//					CyAction action = parseLine( line);	
//					System.out.println("action = " + action);
//					if (action != null)
//					{	
//						addAction(action);
//						lastItemWasSeparator = false;
//
//					}
//					
//				}
//			}
//		}
//		
//		private CyAction parseLine(String line) {
//			String cmdName = line.substring(0, line.indexOf(' '));
//			String displayName =  getBetween(line, '"','"');
//			String gravity =  getBetween(line, '[',']');
//			double weight = 0;
//			try
//			{
//				weight = Double.parseDouble(gravity);
//			}
//			catch (NumberFormatException ex)
//			{
//				weight = -1;
//			}
//			String iconName = getBetween(line, '{','}');
//			System.out.println("adding button: " + cmdName + " " +  displayName + " " +  gravity + " " + iconName);
//			return lookupAction(cmdName);
//		}
//		
//		private CyAction lookupAction(String className) {
//			Class<?> actionClass;
//			CyAction action = null;
//			
//			try {
//				actionClass = Class.forName(className);
//				action = (CyAction) registrar.getService(actionClass);
//			} catch (Exception e) {	}
//			return action;
//		}
//
//		String getBetween(String src, char start, char end) {
//			int startIdx = src.indexOf(start);
//			int endIdx = src.indexOf(end, startIdx+1);
//			if (startIdx >= 0 && endIdx > startIdx)
//			{
//				String s =  src.substring(startIdx + 1, endIdx);
//				return s.trim();
//		
//			}
//			return "";
//		}
}
