package org.cytoscape.internal.view;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.ToolBarComponent;

/**
 * Implementation of Toolbar on the Cytoscape Desktop applicaiton.
 */
public class CytoscapeToolBar extends JToolBar {
	
	private final static long serialVersionUID = 1202339868655256L;
	
	private Map<CyAction, JButton> actionButtonMap; 
	private List<Object> orderedList;
	private Map<Object, Float> componentGravity;

	/**
	 * Default constructor delegates to the superclass void constructor and then
	 * calls {@link #initializeCytoscapeToolBar()}.
	 */
	public CytoscapeToolBar() {
		super("Cytoscape Tools");
		actionButtonMap = new HashMap<>();
		componentGravity = new HashMap<>();
		orderedList = new ArrayList<>();
		
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, (new JSeparator()).getForeground()));
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
		if ( actionButtonMap.containsKey( action ) )
			return false;

		final JButton button = createToolBarButton(action);

		componentGravity.put(button,action.getToolbarGravity());
		actionButtonMap.put(action, button);
		int addInd = getInsertLocation(action.getToolbarGravity());
		orderedList.add(addInd, button);

		addComponents();

		return true;
	}

	private void addComponents() {
		removeAll();
		for ( Object o : orderedList) {
			if ( o instanceof JButton ) {
				add((JButton)o);
			} else if ( o instanceof Float ) {
				addSeparator();
			}
			else if (o instanceof ToolBarComponent){
				add(((ToolBarComponent)o).getComponent());
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
		for ( int i = 0; i < orderedList.size(); i++ ) {
			Object item = orderedList.get(i);
			Float gravity = componentGravity.get(item);
			if ( gravity != null && newGravity < gravity ) {
				return i;
			}
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
		button.setBorderPainted(false);
		button.setRolloverEnabled(true);
		button.setHideActionText(true);

		//  If SHORT_DESCRIPTION exists, use this as tool-tip
		final String shortDescription = (String) action.getValue(Action.SHORT_DESCRIPTION);
		
		if (shortDescription != null) 
			button.setToolTipText(shortDescription);
		
		return button;
	}
}
