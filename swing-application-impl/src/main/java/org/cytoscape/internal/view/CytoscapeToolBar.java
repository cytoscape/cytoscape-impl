package org.cytoscape.internal.view;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
public class CytoscapeToolBar extends JToolBar {
	
	private final static long serialVersionUID = 1202339868655256L;
	
	private Map<CyAction, JButton> actionButtonMap; 
//	private List<CyAction> actionList; 
	private List<Object> orderedList;
	private Map<Object, Float> componentGravity;
	private  CyServiceRegistrar registrar;

	
	/**
	 * new constructor passes CyServiceRegistrar in 
	 */
	public CytoscapeToolBar(final CyServiceRegistrar serviceRegistrar) {
		this();
		registrar = serviceRegistrar;
//		createCustomToolbar();
		readStopList();
	}
	/**
	 * Default constructor delegates to the superclass void constructor and then
	 * calls {@link #initializeCytoscapeToolBar()}.
	 */
	private CytoscapeToolBar() {
		super("Cytoscape Tools");
		
		actionButtonMap = new HashMap<CyAction, JButton>();
//		actionList = new ArrayList<CyAction>();
		componentGravity = new HashMap<Object, Float>();
		orderedList = new ArrayList<Object>();
		
		setFloatable(false);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, (new JSeparator()).getForeground()));
		buildPopup();
	}

	private void buildPopup() {
		addMouseListener(new MouseAdapter() {
			 
	            @Override  public void mousePressed(MouseEvent e) {   showPopup(e);  }
	            @Override  public void mouseReleased(MouseEvent e) {  showPopup(e); }
	            private void showPopup(MouseEvent e)
            	{
            		if (e.isPopupTrigger()) 
            		{
            	        final JPopupMenu popup = new JPopupMenu();
            	        JMenuItem menuItem = new JMenuItem("Show All");
            	        popup.add(menuItem);
            	        menuItem.addActionListener(ev2 -> { showAll(); resave();	} );
            	        menuItem = new JMenuItem("Hide All");
            	        popup.add(menuItem);
            	        popup.add(new JSeparator());
            	        menuItem.addActionListener(ev2 -> { hideAll(); resave();	} );
            	        for (Component comp : getComponents())
	                    {
            				if (comp instanceof JButton)
            				{
    							JButton button  = (JButton)comp; 
    							String tip =  button.getToolTipText();
            					if (tip == null || tip.isEmpty()) continue;
	            				JCheckBoxMenuItem checktem = new JCheckBoxMenuItem();
	            				checktem.setText(tip);
	            				checktem.setState(button.isVisible());
	            				checktem.setIcon(button.getIcon()); 
		                        popup.add(checktem);
		                        checktem.addActionListener(ev -> {  button.setVisible(!button.isVisible()); resave();	} );
               				}
            				                     }
	                    popup.show(e.getComponent(), e.getX(), e.getY());
            		}
            	}
	        });		
	}
	private void resave() {
		List<String> hidden = new ArrayList<String>();
        for (Component comp : getComponents())
        {
            if (comp instanceof JButton)
				if (!comp.isVisible())
				{
					String butnName = ((CyAction)((JButton)comp).getAction()).getName();
					hidden.add(butnName);
//					System.out.println("Hide " + butnName);
				
				}
        }
        if (hidden.size() > 0)
        	writeStopList(hidden);
	}
	@Override public Component add(Component comp)
	{
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
		
//		System.out.println("addAction: " + action.getName());
		
		if (!action.isInToolBar()) 
			return false;

	
		// At present we allow an Action to be in this tool bar only once.
		if ( actionButtonMap.containsKey( action ) )
			return false;
		
		boolean insertSepBefore = false;
		boolean insertSepAfter = false;
		if (action instanceof AbstractCyAction) {
			insertSepBefore = ((AbstractCyAction)action).insertToolbarSeparatorBefore();
			insertSepAfter = ((AbstractCyAction)action).insertToolbarSeparatorAfter();
		}

		final JButton button = createToolBarButton(action);
		if (insertSepBefore)
			addSeparator(action.getToolbarGravity()-.0001f);
		if (insertSepAfter)
			addSeparator(action.getToolbarGravity()+.0001f);

		componentGravity.put(button,action.getToolbarGravity());
		actionButtonMap.put(action, button);
//		actionList.add(action);
		int addIndex = getInsertLocation(action.getToolbarGravity());
		orderedList.add(addIndex, button);
		if (stopList.contains(action.getName())) 
			button.setVisible(false);

		addComponents();

		return true;
	}

	public void showAll()
	{
		for ( Object o : orderedList) 
			if (o instanceof Component)
				((Component)o).setVisible(true);
	}
	
	public void hideAll()
	{
		for ( Object o : orderedList) 
			if (o instanceof Component)
				((Component)o).setVisible(false);
	}
	
	private void addComponents() {
		removeAll();
		for ( Object o : orderedList) 
		{
			if ( o instanceof JButton ) 				add((JButton)o);
			else if ( o instanceof Float ) 				addSeparator();
			else if (o instanceof ToolBarComponent)
				add(((ToolBarComponent)o).getComponent());
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
		for ( int i = 0; i < orderedList.size(); i++ ) {
			Object item = orderedList.get(i);
			Float gravity = componentGravity.get(item);
			if ( gravity != null && newGravity < gravity ) 
				return i;
		}
		return orderedList.size();
	}

	/**
	 * If the given Action has an absent or false inToolBar property, return;
	 * otherwise if there's a button for the action, remove it.
	 */
	public boolean removeAction(CyAction action) {
		JButton button = actionButtonMap.remove(action);

		if (button == null) {
			return false;
		}

		orderedList.remove(button);
		remove(button);

		return true;
	}

	// use by toolbar updater to keep things properly enabled/disabled
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
		
		final JButton button = new JButton(action); 
//		System.out.println("create button: " + action.getName());
		button.setText(action.getName());
		button.setBorderPainted(false);
		button.setRolloverEnabled(true);
		button.setHideActionText(true);

		//  If SHORT_DESCRIPTION exists, use this as tool-tip
		final String shortDescription = (String) action.getValue(Action.SHORT_DESCRIPTION);
		
		if (shortDescription != null) 
			button.setToolTipText(shortDescription);
		
		return button;
	}
	private HashSet<String> 	stopList = new HashSet<String>();

	//--------------------------------------
	private void readStopList()
	{
		stopList.clear();
		List<String> lines;
		try {
			CyApplicationConfiguration cyApplicationConfiguration = registrar.getService(CyApplicationConfiguration.class);
			if (cyApplicationConfiguration == null)
				System.out.println("cyApplicationConfiguration not found");

			if (cyApplicationConfiguration == null) return;
			File configDirectory = cyApplicationConfiguration.getConfigurationDirectoryLocation();
			File configFile = null;
			if (configDirectory.exists())
				configFile = new File(configDirectory.toPath()  + "/toolbar.stoplist");
			lines = Files.readAllLines(configFile.toPath(), Charset.defaultCharset() );
		} catch (IOException e) {
			// file not found: there's no customization, just return
			System.out.println("IOException: " + e.getMessage());
			return;
		}
				
		for (String line : lines)
			stopList.add(line.trim());
	}
	//------------------------
	private void writeStopList(List<String> list)
	{
		BufferedWriter writer = null;
		try {
			CyApplicationConfiguration cyApplicationConfiguration = registrar.getService(CyApplicationConfiguration.class);
			if (cyApplicationConfiguration == null)
				System.out.println("cyApplicationConfiguration not found");

			if (cyApplicationConfiguration == null) return;
			File configDirectory = cyApplicationConfiguration.getConfigurationDirectoryLocation();
			File configFile = null;
			if (configDirectory.exists())
				configFile = new File(configDirectory.toPath()  + "/toolbar.stoplist");
			writer = new BufferedWriter(new FileWriter(configFile));
			for (String line : list)
				if (line != null)
					writer.write(line + "\n");
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}finally {
	          if ( writer != null ) {
	        	  try {
					writer.close();
				} catch (IOException e) {	e.printStackTrace();	}
	            }
		}
				
	}
	//------------------------
//		public void createCustomToolbar()
//		{
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
//		
//		private CyAction parseLine(String line)
//		{
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
//		String getBetween(String src, char start, char end)
//		{
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
