package org.cytoscape.work.internal.tunables;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.DefaultListModel;
import javax.xml.ws.handler.MessageContext.Scope;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.util.ListMultipleSelection;


/**
 * Handler for the type <i>ListMultipleSelection</i> of <code>Tunable</code>
 *
 * @author pasteur
 *
 * @param <T> type of items the List contains
 */
public class ListMultipleHandler<T> extends AbstractGUITunableHandler implements ListSelectionListener {
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
			panel = null;
			itemsContainerList = null;
			return;
		}

		final Border padding = BorderFactory.createEmptyBorder(5, 10, 5, 10);
		panel = new JPanel();
		BorderLayout layout = new BorderLayout();
		panel.setLayout(layout);
		final JTextArea jta = new JTextArea(getDescription());
		jta.setPreferredSize(new Dimension(200, 50));
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		jta.setBorder(padding);
		panel.add(jta, BorderLayout.PAGE_START);
		jta.setBackground(null);
		jta.setEditable(false);

		//put the items in a list
		itemsContainerList = new JList(listModel);//new JList(listMultipleSelection.getPossibleValues().toArray());
		for ( T value : getMultipleSelection().getPossibleValues() ) 
			listModel.addElement(value);
		
		itemsContainerList.setFont(new Font("sansserif",Font.PLAIN,11));
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
		scrollpane.setBorder(padding);
		panel.add(scrollpane, BorderLayout.CENTER);
	}

	
	@Override
	public void update(){
		
		boolean reloadSelection = false;
		
		//If the list of elements has changed, remove old elements and add new ones
		if(!Arrays.equals(listModel.toArray(),getMultipleSelection().getPossibleValues().toArray()))
		{
			listModel.removeAllElements();
			reloadSelection = true;
			for ( T value : getMultipleSelection().getPossibleValues() ) 
				listModel.addElement(value);
		}
		else
		{
			//if the list is the same but the selection has changed, remove all selections and select new ones
			if(!Arrays.equals(itemsContainerList.getSelectedValues(),getMultipleSelection().getSelectedValues().toArray()))
				reloadSelection = true;
		}
		if(reloadSelection )
		{
			// selected items
			final List<T> selectedVals = getMultipleSelection().getSelectedValues();
			final List<T> allValues = getMultipleSelection().getPossibleValues();
			
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
}
