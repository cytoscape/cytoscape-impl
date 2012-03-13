/*
  File: CytoscapeToolBar.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.internal.view;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
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
		actionButtonMap = new HashMap<CyAction,JButton>();
		componentGravity = new HashMap<Object,Float>();
		orderedList = new ArrayList<Object>();
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

		action.updateEnableState();

		JButton button = new JButton(action); 
		button.setBorderPainted(false);
		button.setRolloverEnabled(true);
		button.setText("");
		componentGravity.put(button,action.getToolbarGravity());

		//  If SHORT_DESCRIPTION exists, use this as tool-tip
		String shortDescription = (String) action.getValue(Action.SHORT_DESCRIPTION);
		if (shortDescription != null) 
			button.setToolTipText(shortDescription);

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
}
