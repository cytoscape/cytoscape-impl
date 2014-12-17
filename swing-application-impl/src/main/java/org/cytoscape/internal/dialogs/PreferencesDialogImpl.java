package org.cytoscape.internal.dialogs;

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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class PreferencesDialogImpl extends JDialog implements ItemListener, ActionListener, ListSelectionListener {
	
	private final static long serialVersionUID = 1202339873396288L;
	private static final Logger logger = LoggerFactory.getLogger(PreferencesDialogImpl.class);
	private final CyEventHelper  eventHelper;
	
	private Map<String, Properties> propMap = new HashMap<String,Properties>();
	private Map<String, CyProperty> cyPropMap;
	private Map<String, Boolean> itemChangedMap = new HashMap<String, Boolean>();

	private JComboBox cmbPropCategories = new JComboBox();	
	private JScrollPane propsTablePane = new JScrollPane();
	private JTable prefsTable = new JTable();
	private JButton addPropBtn = new JButton("Add");
	private JButton deletePropBtn = new JButton("Delete");
	private JButton modifyPropBtn = new JButton("Modify");
	private JButton closeButton = new JButton("Close");
	
	/**
	 * Creates a new PreferencesDialog object.
	 *
	 * @param owner  DOCUMENT ME!
	 */
	public PreferencesDialogImpl(Frame owner, CyEventHelper eh, 
			Map<String, Properties> propMap, Map<String, CyProperty> cyPropMap) {
		super(owner);
		
		this.propMap = propMap;
		this.cyPropMap = cyPropMap;
		this.eventHelper = eh;
		
		for(String key: propMap.keySet())
			itemChangedMap.put(key, false);
		
		try {
			initGUI();
			addListeners();

			//
			modifyPropBtn.setEnabled(false);
			deletePropBtn.setEnabled(false);
			

			initTable();
			initCMB();

			updateTable();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		setTitle("Cytoscape Preferences Editor");
		pack();
		// set location relative to owner/parent
		setLocationRelativeTo(owner);
		setModalityType(DEFAULT_MODALITY_TYPE);
		setResizable(false);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {	
		updateTable();
		
	}

	private void initCMB() {
		Object[] keys = this.propMap.keySet().toArray();
		DefaultComboBoxModel cmbModel = new DefaultComboBoxModel(keys);

		this.cmbPropCategories.setModel(cmbModel);
		
		String key = SimpleCyProperty.CORE_PROPRERTY_NAME;
		
		int index =0;
		for (int i=0; i<keys.length; i++){
			if (keys[i].toString().equalsIgnoreCase(key)){
				index =i;
				break;
			}
		}
		
		this.cmbPropCategories.setSelectedIndex(index);
	}

	private void initTable() {
	
		DefaultTableColumnModel cm = new DefaultTableColumnModel();
		
		for (int i = 0; i < PreferenceTableModel.columnHeader.length; i++) {
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
			renderer.setHorizontalAlignment(PreferenceTableModel.alignment[i]);

			TableColumn Column = new TableColumn(i, PreferenceTableModel.columnWidth[i], renderer,
			                                     null);
			Column.setIdentifier(PreferenceTableModel.columnHeader[i]);
			
			cm.addColumn(Column);
		}

		prefsTable.setColumnModel(cm);
		prefsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	
	private void updateTable(){
		
		if (this.cmbPropCategories.getSelectedItem() == null){
			return;
		}
				
		String selectedPropertyName = this.cmbPropCategories.getSelectedItem().toString();

		Properties prop = this.propMap.get(selectedPropertyName);
		
		PreferenceTableModel m = new PreferenceTableModel(prop);
		prefsTable.setModel(m);
	}
	

	// Handle action event from the buttons
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		String selectedPropertyName = this.cmbPropCategories.getSelectedItem().toString();

		if (obj instanceof JButton){
			JButton btn = (JButton) obj;
			if (btn == this.closeButton){
				for(String key: itemChangedMap.keySet()){
					if (itemChangedMap.get(key)){
						PropertyUpdatedEvent event = new PropertyUpdatedEvent(this.cyPropMap.get(key));
						eventHelper.fireEvent(event );
						itemChangedMap.put(key, false);
					}
				}
				this.dispose();
			}
			else if (btn == this.deletePropBtn){			
				int[] selectedIndices = this.prefsTable.getSelectedRows();
				for (int i = selectedIndices.length-1; i >= 0; i--) {					
					String name = new String((String) (this.prefsTable.getModel().getValueAt(selectedIndices[i], 0)));
					PreferenceTableModel m = (PreferenceTableModel)this.prefsTable.getModel();
					m.deleteProperty(name);
					itemChangedMap.put(selectedPropertyName, true);
				}
			}
			else if (btn == this.modifyPropBtn){				
				int[] selectedIndices = this.prefsTable.getSelectedRows();
				for (int i = selectedIndices.length-1; i >= 0; i--) {					
					String name = new String((String) (this.prefsTable.getModel().getValueAt(selectedIndices[i], 0)));
					String value = new String((String) (this.prefsTable.getModel().getValueAt(selectedIndices[i], 1)));
					
					PreferenceTableModel m = (PreferenceTableModel)this.prefsTable.getModel();
					PreferenceValueDialog pd = new PreferenceValueDialog(this, name,  value, m,
						                                                     "Modify value...");
					if (pd.itemChanged)
						itemChangedMap.put(selectedPropertyName, true);
				}
			}
			else if (btn == this.addPropBtn){
				String key = JOptionPane.showInputDialog(addPropBtn, "Enter property name:",
                       "Add Property", JOptionPane.QUESTION_MESSAGE);

				if (key != null) {
					String value = JOptionPane.showInputDialog(addPropBtn,
	                              "Enter value for property " + key + ":",
	                              "Add Property Value",
	                              JOptionPane.QUESTION_MESSAGE);
	
					if (value != null) {
						String[] vals = { key, value };
						PreferenceTableModel m = (PreferenceTableModel)this.prefsTable.getModel();
						
						m.addProperty(vals);
						itemChangedMap.put(selectedPropertyName, true);
					}
				}
			}
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e){
	
		if (this.prefsTable.getSelectedRowCount() == 0){
			this.modifyPropBtn.setEnabled(false);
			this.deletePropBtn.setEnabled(false);
		}
		else {
			this.modifyPropBtn.setEnabled(true);
			this.deletePropBtn.setEnabled(true);
		}
	}

	private void addListeners() {
		addPropBtn.addActionListener(this);
		modifyPropBtn.addActionListener(this);
		deletePropBtn.addActionListener(this);
		closeButton.addActionListener(this);

		cmbPropCategories.addItemListener(this);
		prefsTable.getSelectionModel().addListSelectionListener(this);
	}
    
	private void initGUI() throws Exception {
		propsTablePane.getViewport().add(prefsTable, null);
		prefsTable.setPreferredScrollableViewportSize(new Dimension(400, 200));

		final JPanel propsTablePanel = new JPanel();
		propsTablePanel.setBorder(LookAndFeelUtil.createTitledBorder("Properties"));
		
		{
			final GroupLayout layout = new GroupLayout(propsTablePanel);
			propsTablePanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(cmbPropCategories, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(propsTablePane, DEFAULT_SIZE, DEFAULT_SIZE, 460)
					.addGroup(Alignment.CENTER, layout.createSequentialGroup()
							.addComponent(addPropBtn)
							.addComponent(modifyPropBtn)
							.addComponent(deletePropBtn)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(cmbPropCategories, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(propsTablePane, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
							.addComponent(addPropBtn)
							.addComponent(modifyPropBtn)
							.addComponent(deletePropBtn)
					)
			);
		}
		
		final JPanel outerPanel = new JPanel();
		
		{
			final GroupLayout layout = new GroupLayout(outerPanel);
			outerPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.TRAILING, true)
					.addComponent(propsTablePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(closeButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(propsTablePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(closeButton)
			);
		}
		
		this.getContentPane().add(outerPanel, BorderLayout.CENTER);
	}
}
