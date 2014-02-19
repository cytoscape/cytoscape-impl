package org.cytoscape.work.internal.tunables;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.DefaultListModel;
import javax.xml.ws.handler.MessageContext.Scope;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.util.ListChangeListener;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSelection;


/**
 * Handler for the type <i>ListMultipleSelection</i> of <code>Tunable</code>
 *
 * @author pasteur
 *
 * @param <T> type of items the List contains
 */
public class ListMultipleHandler<T> extends AbstractGUITunableHandler 
                                            implements ListSelectionListener, ListChangeListener<T> {
	private JList itemsContainerList;
	private DefaultListModel listModel;
	private ListMultipleSelection<T> listMultipleSelection;

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>ListMultipleSelection</code> type
	 *
	 * creates a list to collect all the <code>T</code> items and displays it in the GUI through a JScrollPane
	 * Informations about the list and its contents are also displayed
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	@SuppressWarnings("unchecked")
	public ListMultipleHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public ListMultipleHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}
	
	private ListMultipleSelection<T> getMultipleSelection() {
		try {
			return (ListMultipleSelection<T>)getValue();
		} catch(final Exception e) {
			throw new NullPointerException("bad ListMultipleSelection object");	
		}
	}

	private void init() {
		
		listMultipleSelection = getMultipleSelection();
		listModel = new DefaultListModel();

		//create GUI
		if ( listMultipleSelection.getPossibleValues().isEmpty() ) {
			panel = new JPanel();
			itemsContainerList = null;
			return;
		}

		panel = new JPanel();
		BorderLayout layout = new BorderLayout();
		panel.setLayout(layout);
		String description = getDescription();
		if (description != null && description.length() > 80) {
			// Use JTextArea for long descriptions
			final JTextArea jta = new JTextArea(description);

			jta.setLineWrap(true);
			jta.setWrapStyleWord(true);
			panel.add(jta, BorderLayout.PAGE_START);
			jta.setBackground(panel.getBackground());
			jta.setEditable(false);
		} else if (description != null && description.length() > 0) {
			// Otherwise, use JLabel
			final JLabel jLabel = new JLabel(description);
			jLabel.setFont(GUIDefaults.LABEL_FONT);
			panel.add(jLabel, BorderLayout.PAGE_START);
		}

		//put the items in a list
		itemsContainerList = new JList(listModel);//new JList(listMultipleSelection.getPossibleValues().toArray());
		for ( T value : getMultipleSelection().getPossibleValues() ) 
			listModel.addElement(value);
		
		itemsContainerList.setFont(GUIDefaults.TEXT_FONT);
		itemsContainerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		itemsContainerList.addListSelectionListener(this);
		
		// selected items
		final List<T> selectedVals = listMultipleSelection.getSelectedValues();
		final List<T> allValues = listMultipleSelection.getPossibleValues();
		
		final int[] selectedIdx = new int[selectedVals.size()];
		int index = 0;
		for(T selected: selectedVals) {
			for(int i = 0; i<allValues.size(); i++) {
				if(itemsContainerList.getModel().getElementAt(i).equals(selected)) {
					selectedIdx[index] = i;
					index++;
				}
			}
		}
		itemsContainerList.setSelectedIndices(selectedIdx);
		
		//use a JscrollPane to visualize the items
		
		final JScrollPane scrollpane = new JScrollPane(itemsContainerList);
		scrollpane.setAutoscrolls(true);
		scrollpane.setOpaque(false);
		scrollpane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		panel.add(scrollpane, BorderLayout.CENTER);

		// Set the tooltip.  Note that at this point, we're setting
		// the tooltip on the entire panel.  This may or may not be
		// the right thing to do.
		if (getTooltip() != null && getTooltip().length() > 0) {
			final ToolTipManager tipManager = ToolTipManager.sharedInstance();
			tipManager.setInitialDelay(1);
			tipManager.setDismissDelay(7500);
			panel.setToolTipText(getTooltip());
		}
	}

	
	@Override
	public void update(){
		
		boolean reloadSelection = false;
		
		listMultipleSelection = getMultipleSelection();
		
		//If the list of elements has changed, remove old elements and add new ones
		if(!Arrays.equals(listModel.toArray(),listMultipleSelection.getPossibleValues().toArray()))
		{
			listModel.removeAllElements();
			reloadSelection = true;
			for ( T value : listMultipleSelection.getPossibleValues() ) 
				listModel.addElement(value);
		}
		else
		{
			//if the list is the same but the selection has changed, remove all selections and select new ones
			if(!Arrays.equals(itemsContainerList.getSelectedValues(),listMultipleSelection.getSelectedValues().toArray()))
				reloadSelection = true;
		}
		if(reloadSelection )
		{
			// selected items
			final List<T> selectedVals = listMultipleSelection.getSelectedValues();
			final List<T> allValues = listMultipleSelection.getPossibleValues();
			
			final int[] selectedIdx = new int[selectedVals.size()];
			int index = 0;
			for(T selected: selectedVals) {
				for(int i = 0; i<allValues.size(); i++) {
					if(itemsContainerList.getModel().getElementAt(i).equals(selected)) {
						selectedIdx[index] = i;
						index++;
					}
				}
			}
			itemsContainerList.removeSelectionInterval(0, allValues.size()-1);
			itemsContainerList.setSelectedIndices(selectedIdx);
		}
	}
	
	/**
	 * set the items that are currently selected in the <code>itemsContainerList</code> as the selected items in <code>listMultipleSelection</code>
	 */
	public void handle() {
		if ( itemsContainerList == null )
			return;

		List selectedItems = Arrays.asList(itemsContainerList.getSelectedValues());
		if (!selectedItems.isEmpty()) {
			getMultipleSelection().setSelectedValues(selectedItems);
		}
		
		try {
			setValue(getMultipleSelection());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * returns a string representation of all the selected items of <code>listMultipleSelection</code>
	 */
	public String getState() {
		if ( itemsContainerList == null )
			return "";

		final List<T> selection = getMultipleSelection().getSelectedValues();
		return selection == null ? "" : selection.toString();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		handle();
	}

	@Override
	public void selectionChanged(ListSelection<T> source) {
		update();
	}

	@Override
	public void listChanged(ListSelection<T> source) {
		update();
	}
}
