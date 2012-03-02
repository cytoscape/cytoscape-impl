/*
  Copyright (c) 2006, 2007, 2009, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.filter.internal.filters.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.filter.internal.filters.event.FiltersChangedEvent;
import org.cytoscape.filter.internal.filters.event.FiltersChangedListener;
import org.cytoscape.filter.internal.filters.model.CompositeFilter;
import org.cytoscape.filter.internal.filters.model.EdgeInteractionFilter;
import org.cytoscape.filter.internal.filters.model.FilterModelLocator;
import org.cytoscape.filter.internal.filters.model.InteractionFilter;
import org.cytoscape.filter.internal.filters.model.NodeInteractionFilter;
import org.cytoscape.filter.internal.filters.model.TopologyFilter;
import org.cytoscape.filter.internal.filters.util.FilterUtil;
import org.cytoscape.filter.internal.filters.util.SelectUtil;
import org.cytoscape.filter.internal.filters.util.WidestStringComboBoxModel;
import org.cytoscape.filter.internal.filters.util.WidestStringComboBoxPopupMenuListener;
import org.cytoscape.filter.internal.filters.util.WidestStringProvider;
import org.cytoscape.filter.internal.quickfind.util.CyAttributesUtil;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsCreatedEvent;
import org.cytoscape.model.events.RowsCreatedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.util.swing.DropDownMenuButton;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.TaskManager;


@SuppressWarnings("serial")
public class FilterMainPanel extends JPanel implements ActionListener,
						       ItemListener, SetCurrentNetworkViewListener, NetworkAddedListener,
						       NetworkAboutToBeDestroyedListener, SessionLoadedListener, RowsSetListener,
						       RowsCreatedListener, NetworkViewAddedListener, FiltersChangedListener {
	
	// String constants used for separator entries in the attribute combobox
	private static final String filtersSeparator = "-- Filters --";
	private static final String attributesSeperator = "-- Attributes --";
	
	private final QuickFind quickFind;

	private static JPopupMenu optionMenu;

	private static JMenuItem newFilterMenuItem;
	private static JMenuItem newTopologyFilterMenuItem;
	private static JMenuItem newNodeInteractionFilterMenuItem;
	private static JMenuItem newEdgeInteractionFilterMenuItem;
	
	private static JMenuItem renameFilterMenuItem;

	private static JMenuItem deleteFilterMenuItem;

	private static JMenuItem duplicateFilterMenuItem;

	private DropDownMenuButton optionButton;
	
	private FilterSettingPanel currentFilterSettingPanel;
	private HashMap<CompositeFilter,FilterSettingPanel> filter2SettingPanelMap = new HashMap<CompositeFilter,FilterSettingPanel>();

	/*
	 * Icons used in this panel.
	 */
	private final ImageIcon optionIcon = new ImageIcon(getClass().getResource("/images/properties.png"));
	private final ImageIcon delIcon = new ImageIcon(getClass().getResource("/images/delete.png"));
	private final ImageIcon addIcon = new ImageIcon(getClass().getResource("/images/add.png"));
	private final ImageIcon renameIcon = new ImageIcon(getClass().getResource("/images/rename.png"));
	private final ImageIcon duplicateIcon = new ImageIcon(getClass().getResource("/images/duplicate.png"));

	private final FilterModelLocator modelLocator;
	private final CyApplicationManager applicationManager;
	private final CyNetworkManager networkManager;
	private final CyEventHelper eventHelper;
	private final TaskManager taskManager;
	
	public FilterMainPanel(final QuickFind quickFind, final FilterModelLocator modelLocator,
						   final CyApplicationManager applicationManager,
	                       final CyNetworkManager networkManager,
	                       final CyEventHelper eventHelper,
	                       final TaskManager taskManager) {
		this.modelLocator       = modelLocator;
		this.applicationManager = applicationManager;
		this.networkManager     = networkManager;
		this.eventHelper        = eventHelper;
		this.taskManager        = taskManager;
		this.quickFind = quickFind;
		
		modelLocator.addListener(this);

		//Initialize the option menu with menuItems
		setupOptionMenu();

		optionButton = new DropDownMenuButton(new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					DropDownMenuButton b = (DropDownMenuButton) ae.getSource();
					optionMenu.show(b, 0, b.getHeight());
				}
			});

		optionButton.setToolTipText("Options...");
		optionButton.setIcon(optionIcon);
		optionButton.setMargin(new Insets(2, 2, 2, 2));
		optionButton.setComponentPopupMenu(optionMenu);

		//Initialize the UI components 
		initComponents();

		this.btnSelectAll.setEnabled(false);
		this.btnDeSelect.setEnabled(false);

		// reduce the text font to fit three buttons within visible window
		this.btnSelectAll.setFont(new Font("Tahoma", 0, 9));
		this.btnDeSelect.setFont(new Font("Tahoma", 0, 9));
		this.btnApplyFilter.setFont(new Font("Tahoma", 0, 9));
		//
		String[][] data = {{"","",""}};
		String[] col = {"Network","Nodes","Edges"};
		DefaultTableModel model = new DefaultTableModel(data,col);

		tblFeedBack.setModel(model);

		addEventListeners();
	
		//btnApplyFilter.setVisible(false);
		
		//Update the status of interactionMenuItems if this panel become visible
		MyComponentAdapter cmpAdpt = new MyComponentAdapter();
		addComponentListener(cmpAdpt);
	}

	@Override
	public void handleEvent(FiltersChangedEvent event) {
		updateCMBFilters();
		refreshAttributeCMB();
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		// Handle selection events
		if (this.applicationManager.getCurrentNetworkView() == null){
			return;
		}
		
		boolean isSelection = true;
		for (RowSetRecord change : e.getPayloadCollection()) {
			if (!change.getColumn().equals(CyNetwork.SELECTED)) {
				isSelection = false;
				break;
			}
		}
		if (isSelection) {
			updateFeedbackTableModel();
			return;
		}
		
		handleAttributesChanged();	
		updateFeedbackTableModel();
	}
	
	@Override
	public void handleEvent(RowsCreatedEvent e) {
		handleAttributesChanged();
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		updateFeedbackTableModel();
	}
	
	public void handleNetworkFocused(final CyNetworkView view) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (view == null) {
					return;
				}
				
				// If FilterPanel is not selected, do nothing
				if (cmbFilters.getSelectedItem() == null) {
					return;
				}
							
				//Refresh indices for UI widgets after network switch			
				CompositeFilter selectedFilter = (CompositeFilter) cmbFilters.getSelectedItem();
				selectedFilter.setNetwork(view.getModel());
				FilterSettingPanel theSettingPanel= filter2SettingPanelMap.get(selectedFilter);
				
				if (theSettingPanel != null) {
					theSettingPanel.refreshIndicesForWidgets();
					updateFeedbackTableModel();
				}
			}
		});
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		handleNetworkFocused(e.getNetworkView());
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		CyNetwork network = e.getNetwork();
		if (!networkManager.networkExists(network.getSUID())) {
			return;
		}
		
		enableForNetwork();
		updateFeedbackTableModel();
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		CyNetwork network = e.getNetwork();
		if (!networkManager.networkExists(network.getSUID())) {
			return;
		}

		enableForNetwork();
		updateFeedbackTableModel();
	}
	
	@Override
	public void handleEvent(final NetworkViewAddedEvent e) {
		if (!isShowing())
			return;

		updateIndex();
	}

	public void updateFeedbackTableModel(){		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CyNetwork cyNetwork = applicationManager.getCurrentNetwork();
				CyNetworkView view = applicationManager.getCurrentNetworkView();
				RenderingEngine<CyNetwork> engine = applicationManager.getCurrentRenderingEngine();
				if (cyNetwork == null || view == null || engine == null) {
					return;
				}
		
				//VisualLexicon lexicon = engine.getVisualLexicon();
				//String title = VisualPropertyUtil.get(lexicon, view, "NETWORK_TITLE", MinimalVisualLexicon.NETWORK, String.class);
				tblFeedBack.getModel().setValueAt(cyNetwork.getRow(cyNetwork).get("name", String.class), 0, 0);

				String nodeStr = "" + cyNetwork.getNodeCount() + "(" + cyNetwork.getDefaultNodeTable().countMatchingRows(CyNetwork.SELECTED,true) + ")";
				tblFeedBack.getModel().setValueAt(nodeStr, 0, 1);

				String edgeStr = "" + cyNetwork.getEdgeCount() + "(" + cyNetwork.getDefaultEdgeTable().countMatchingRows(CyNetwork.SELECTED,true) + ")";
				tblFeedBack.getModel().setValueAt(edgeStr, 0, 2);				
		}});
	}
	
	/**
	 * Enable select/deselect buttons if the current network exists and is not null.
	 */
	public void enableForNetwork() {
		CyNetwork n = applicationManager.getCurrentNetwork();

		if ( n == null ) {
			this.btnSelectAll.setEnabled(false);
			this.btnDeSelect.setEnabled(false);
		} else {
			this.btnSelectAll.setEnabled(true);
			this.btnDeSelect.setEnabled(true);	
		}
	}
	
	public void handlePanelSelected() {
		updateIndex();
	}
	
	public void refreshFilterSelectCMB() {
		ComboBoxModel cbm;

		// Whatever change caused the refresh may have altered the longest display String
		// so need to have the model recalculate it.
		cbm = cmbFilters.getModel();
		if (cbm instanceof WidestStringProvider) {
			((WidestStringProvider)cbm).resetWidest();
		}

		this.cmbFilters.repaint();
	}
	
	private void handleAttributesChanged() {
		SwingUtilities.invokeLater(new Runnable() {
				@Override
					public void run() {
					refreshAttributeCMB();
					replaceFilterSettingPanel((CompositeFilter)cmbFilters.getSelectedItem());
				
					FilterSettingPanel theSettingPanel= filter2SettingPanelMap.get(cmbFilters.getSelectedItem());
					if (theSettingPanel != null) {
						theSettingPanel.refreshIndicesForWidgets();
					}
					updateFeedbackTableModel();
				}
			});
	}
	
	private void refreshAttributeCMB() {
		updateCMBAttributes();
		cmbAttributes.repaint();
	}
		
	/**
	 * Get the list of attribute names for either "node" or "edge". The attribute names will be
	 * prefixed either with "node." or "edge.". Those attributes whose data type is neither
	 * "String" nor "numeric" will be excluded
	 */
	private List<Object> getCyAttributesList(CyNetwork network, String pType) {
		Vector<String> attributeList = new Vector<String>();
		Collection<? extends CyTableEntry> entries = null;
		
		if (pType.equalsIgnoreCase("node")) {
			entries = network.getNodeList();
		} else if (pType.equalsIgnoreCase("edge")){
			entries = network.getEdgeList();
		} else {
			return Collections.emptyList();
		}
		
		if (entries.size() == 0) {
			return Collections.emptyList();
		}
		
		CyTableEntry tableEntry = entries.iterator().next();
		final Collection<CyColumn> columns = network.getRow(tableEntry).getTable().getColumns();
		
		for (final CyColumn column : columns) {
			//  Show all attributes, with type of String or Number
			Class<?> type = column.getType();
			
			//  only show user visible attributes,with type = Number/String/List
			if ((type == Integer.class)||(type == Double.class)||(type == Boolean.class)||(type == String.class)||(type == List.class)) {
				attributeList.add(pType + "." + column.getName());
			}
		
			//  Alphabetical sort
			Collections.sort(attributeList);
		}

		// type conversion
		Vector<Object> retList = new Vector<Object>();

		for (int i=0; i<attributeList.size(); i++) {
			retList.add(attributeList.elementAt(i));
		}
		
		return retList;
	}
	
	/*
	 * Hide the visible filterSettingPanel, if any, and show the new FilterSettingPanel for
	 * the given filter.
	 */
	private void replaceFilterSettingPanel(CompositeFilter pNewFilter) {
		if (pNewFilter == null) {
			pnlFilterDefinition.setVisible(false);
			lbPlaceHolder_pnlFilterDefinition.setVisible(true);	
			return;
		}
		
		FilterSettingPanel next;
		next = filter2SettingPanelMap.get(pNewFilter);

		// When the next panel exists and is the same as the current one,
		// we can exit now and avoid hiding and showing the same panel.
		if ((next != null) && (next == currentFilterSettingPanel)) {
			return;
		}
        
		//Hide the existing FilterSettingPanel, if any
		if (currentFilterSettingPanel != null) {
			currentFilterSettingPanel.setVisible(false);
		}

		currentFilterSettingPanel = next;
		
		if (currentFilterSettingPanel == null || currentFilterSettingPanel.hasNullIndexChildFilter()) {
			currentFilterSettingPanel = new FilterSettingPanel(quickFind, this, pNewFilter, modelLocator, applicationManager,
					eventHelper);
			//Update the HashMap
			filter2SettingPanelMap.put(pNewFilter, currentFilterSettingPanel);			
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(7, 0, 0, 0);
			
		if (pNewFilter instanceof TopologyFilter) {
			lbAttribute.setVisible(false);
			btnAddFilterWidget.setVisible(false);
			cmbAttributes.setVisible(false);	
			pnlFilterDefinition.setBorder(BorderFactory
						      .createTitledBorder("Topology Filter Definition"));
		} else if (pNewFilter instanceof InteractionFilter) {
			lbAttribute.setVisible(false);
			btnAddFilterWidget.setVisible(false);
			cmbAttributes.setVisible(false);	
			pnlFilterDefinition.setBorder(BorderFactory
						      .createTitledBorder("Interaction Filter Definition"));				
		} else {
			lbAttribute.setVisible(true);
			btnAddFilterWidget.setVisible(true);
			cmbAttributes.setVisible(true);								
			pnlFilterDefinition.setBorder(BorderFactory
						      .createTitledBorder("Filter Definition"));

		}
		
		pnlFilterDefinition.add(currentFilterSettingPanel, gridBagConstraints);
		pnlFilterDefinition.setVisible(true);
		currentFilterSettingPanel.setVisible(true);
		lbPlaceHolder_pnlFilterDefinition.setVisible(false); 				
			
		this.repaint();
	}
	
	private void addEventListeners() {
		btnApplyFilter.addActionListener(this);

		btnAddFilterWidget.addActionListener(this);

		newFilterMenuItem.addActionListener(this);
		newTopologyFilterMenuItem.addActionListener(this);
		newNodeInteractionFilterMenuItem.addActionListener(this);
		newEdgeInteractionFilterMenuItem.addActionListener(this);

		deleteFilterMenuItem.addActionListener(this);
		renameFilterMenuItem.addActionListener(this);
		duplicateFilterMenuItem.addActionListener(this);

		cmbFilters.addItemListener(this);
		cmbAttributes.addItemListener(this);
		
		btnSelectAll.addActionListener(this);
		btnDeSelect.addActionListener(this);
	}

	private void initCMBFilters(){
		Vector<CompositeFilter> allFilters = modelLocator.getFilters();
		ComboBoxModel cbm = new FilterSelectWidestStringComboBoxModel(allFilters);
		cmbFilters.setModel(cbm);
		cmbFilters.setRenderer(new FilterRenderer());
		
		if (allFilters.size() == 0) {
			this.btnApplyFilter.setEnabled(false);
			this.btnAddFilterWidget.setEnabled(false);
		}
		
		for (CompositeFilter cf : allFilters) {
			filter2SettingPanelMap.put(cf, null);
		}

		// Force the first filter in the model to be selected, so that it's panel will be shown
		if (cbm.getSize() > 0) {
			cmbFilters.setSelectedIndex(0);
		}

		replaceFilterSettingPanel((CompositeFilter)cmbFilters.getSelectedItem());
	}

	private void updateIndex() {
		final CyNetworkView currentView = applicationManager.getCurrentNetworkView();
		
		if (currentView == null)
			return;

		final CyNetwork network = currentView.getModel();
		taskManager.execute(new FilterIndexingTaskFactory(quickFind, network));

		updateCMBAttributes();
	}
	
	private void updateCMBFilters() {
		Vector<CompositeFilter> filters = modelLocator.getFilters();
		
		DefaultComboBoxModel cbm = (DefaultComboBoxModel) cmbFilters.getModel();
		cbm.removeAllElements();
		
		if (filters != null) {
			for (CompositeFilter cf : filters) {
				cbm.addElement(cf);
			}
		}
		
		if (filters == null || filters.size() == 0) {
			replaceFilterSettingPanel(null);
		}
	}
	
	/*
	 * Update the attribute list in the attribute combobox based on the settings in the 
	 * current selected filter
	 */
	private void updateCMBAttributes() {
		DefaultComboBoxModel cbm;

		cbm = ((DefaultComboBoxModel)cmbAttributes.getModel());
		cbm.removeAllElements();

		cbm.addElement(attributesSeperator);
		CompositeFilter selectedFilter = (CompositeFilter)cmbFilters.getSelectedItem();

		if (selectedFilter == null) {
			return;
		}

		CyNetwork network = selectedFilter.getNetwork();
		if (network == null) {
			return;
		}
		
		List<Object> av;

		av = getCyAttributesList(network, "node");
		for (int i = 0; i < av.size(); i++) {
			cbm.addElement(av.get(i));
		}

		av = getCyAttributesList(network, "edge");
		for (int i = 0; i < av.size(); i++) {
			cbm.addElement(av.get(i));
		}
	}
	
	/**
	 * Setup menu items.
	 * 
	 */
	private void setupOptionMenu() {
		/*
		 * Option Menu
		 */
		newFilterMenuItem = new JMenuItem("Create new filter...");
		newFilterMenuItem.setIcon(addIcon);
		
		newTopologyFilterMenuItem = new JMenuItem("Create new topology filter...");
		newTopologyFilterMenuItem.setIcon(addIcon);

		newNodeInteractionFilterMenuItem = new JMenuItem("Create new NodeInteraction filter...");
		newNodeInteractionFilterMenuItem.setIcon(addIcon);
		newNodeInteractionFilterMenuItem.setEnabled(false);

		newEdgeInteractionFilterMenuItem = new JMenuItem("Create new EdgeInteraction filter...");
		newEdgeInteractionFilterMenuItem.setIcon(addIcon);
		newEdgeInteractionFilterMenuItem.setEnabled(false);
		
		deleteFilterMenuItem = new JMenuItem("Delete filter...");
		deleteFilterMenuItem.setIcon(delIcon);

		renameFilterMenuItem = new JMenuItem("Rename filter...");
		renameFilterMenuItem.setIcon(renameIcon);

		duplicateFilterMenuItem = new JMenuItem("Copy existing filter...");
		duplicateFilterMenuItem.setIcon(duplicateIcon);
		// Hide copy icon for now, we may need it in the future
		duplicateFilterMenuItem.setVisible(false);

		optionMenu = new JPopupMenu();
		optionMenu.add(newFilterMenuItem);
		optionMenu.add(newTopologyFilterMenuItem);
		optionMenu.add(newNodeInteractionFilterMenuItem);
		optionMenu.add(newEdgeInteractionFilterMenuItem);
		optionMenu.add(deleteFilterMenuItem);
		optionMenu.add(renameFilterMenuItem);
		optionMenu.add(duplicateFilterMenuItem);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {
		GridBagConstraints gridBagConstraints;

		pnlCurrentFilter = new JPanel();
		cmbFilters = new JComboBox();
		cmbFilters.addPopupMenuListener(new WidestStringComboBoxPopupMenuListener());
		// optionButton = new javax.swing.JButton();
		pnlFilterDefinition = new JPanel();
        
		WidestStringComboBoxModel wscbm = new AttributeSelectWidestStringComboBoxModel();
		cmbAttributes = new JComboBox(wscbm);
		cmbAttributes.addPopupMenuListener(new WidestStringComboBoxPopupMenuListener());

		btnAddFilterWidget = new JButton();
		lbAttribute = new JLabel();
		lbPlaceHolder = new JLabel();
		pnlButton = new JPanel();
		btnApplyFilter = new JButton();
		lbPlaceHolder_pnlFilterDefinition = new JLabel();

		pnlFeedBack = new JPanel();
		tblFeedBack = new JTable();
		
		btnSelectAll = new JButton();
		btnDeSelect = new JButton();
		pnlScroll = new JScrollPane();
        
		setLayout(new GridBagLayout());

		pnlCurrentFilter.setLayout(new GridBagLayout());

		pnlCurrentFilter.setBorder(BorderFactory.createTitledBorder("Current Filter"));
		// cmbFilters.setModel(new DefaultComboBoxModel(new String[] { "My First filter", "My second Filter" }));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.insets = new Insets(5, 10, 5, 10);
		pnlCurrentFilter.add(cmbFilters, gridBagConstraints);

		optionButton.setText("Option");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 0, 0, 5);
		pnlCurrentFilter.add(optionButton, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.ipady = 4;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		add(pnlCurrentFilter, gridBagConstraints);

		pnlFilterDefinition.setLayout(new GridBagLayout());

		pnlFilterDefinition.setBorder(BorderFactory.createTitledBorder("Filter Definition"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(10, 10, 0, 10);
		pnlFilterDefinition.add(cmbAttributes, gridBagConstraints);

		btnAddFilterWidget.setText("Add");
		btnAddFilterWidget.setEnabled(false);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(10, 0, 0, 5);
		pnlFilterDefinition.add(btnAddFilterWidget, gridBagConstraints);

		lbAttribute.setText("Attribute/Filter");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(10, 5, 0, 0);
		pnlFilterDefinition.add(lbAttribute, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		pnlFilterDefinition.add(lbPlaceHolder, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(pnlFilterDefinition, gridBagConstraints);

		///
		pnlButton.setLayout(new FlowLayout());
		
		btnApplyFilter.setText("Apply Filter");
		pnlButton.add(btnApplyFilter);

		btnSelectAll.setText("Select All");
		pnlButton.add(btnSelectAll);

		btnDeSelect.setText("Deselect All");
		pnlButton.add(btnDeSelect);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(10, 0, 10, 0);
		add(pnlButton, gridBagConstraints);

		// lbPlaceHolder_pnlFilterDefinition.setText("jLabel1");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(lbPlaceHolder_pnlFilterDefinition, gridBagConstraints);

		// feedback panel
		pnlFeedBack.setLayout(new GridBagLayout());
		pnlFeedBack.setBorder(BorderFactory.createTitledBorder(""));
		pnlFeedBack.setMinimumSize(new Dimension(pnlFeedBack.getWidth(),52));
		//pnlFeedBack.setMinimumSize(new java.awt.Dimension(300,52));
        
		pnlScroll.setViewportView(tblFeedBack);

		//tblFeedBack.setAutoCreateColumnsFromModel(true);
		//tblFeedBack.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
		tblFeedBack.setEnabled(false);
		tblFeedBack.setFocusable(false);
		//tblFeedBack.setRequestFocusEnabled(false);
		//tblFeedBack.setRowSelectionAllowed(false);
		//tblFeedBack.setTableHeader(null);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH; //.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		//gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 1);
		pnlFeedBack.add(pnlScroll, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(1, 1, 2, 1);

		add(pnlFeedBack, gridBagConstraints);
		// Set customized renderer for attributes/filter combobox
		cmbAttributes.setRenderer(new AttributeFilterRenderer());
		
		initCMBFilters();
	}// </editor-fold>

	// Variables declaration - do not modify
	private JButton btnAddFilterWidget;

	private JButton btnApplyFilter;

	private JComboBox cmbAttributes;

	private JComboBox cmbFilters;

	private JLabel lbAttribute;

	private JLabel lbPlaceHolder;

	private JLabel lbPlaceHolder_pnlFilterDefinition;

	// private javax.swing.JButton optionButton;
	private JPanel pnlButton;

	private JPanel pnlCurrentFilter;

	private JPanel pnlFilterDefinition;

	private JPanel pnlFeedBack;
	private JTable tblFeedBack;
    
	private JButton btnDeSelect;
	private JButton btnSelectAll;
	private JScrollPane pnlScroll;
	// End of variables declaration
	
	public JComboBox getCMBAttributes() {
		return 	cmbAttributes;
	}
	
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		
		if (source instanceof JComboBox) {
			JComboBox cmb = (JComboBox) source;
			
			if (cmb == cmbFilters) {
				CompositeFilter selectedFilter = (CompositeFilter)cmbFilters.getSelectedItem();
				if (selectedFilter == null) {
					this.btnApplyFilter.setEnabled(false);
					this.btnAddFilterWidget.setEnabled(false);
					return;
				} else {
					this.btnAddFilterWidget.setEnabled(true);
					this.btnApplyFilter.setEnabled(true);					
				}
				
				CyNetwork cyNetwork = applicationManager.getCurrentNetwork();
				selectedFilter.setNetwork(cyNetwork);
				
				replaceFilterSettingPanel(selectedFilter);

				if (cyNetwork != null) {
					SelectUtil.unselectAllNodes(cyNetwork);						
				}

				if (cmbFilters.getSelectedItem() instanceof TopologyFilter || cmbFilters.getSelectedItem() instanceof InteractionFilter) {
					// do not apply TopologyFilter or InteractionFilter automatically
					return;
				}	
								
				// If network size is greater than pre-defined threshold, don't apply it automatically 
				if (FilterUtil.isDynamicFilter(selectedFilter)) {
					FilterUtil.doSelection(selectedFilter, applicationManager);
				}
				
				updateView();
				refreshAttributeCMB();
			} else if (cmb == cmbAttributes) {
				Object selectObject = cmbAttributes.getSelectedItem();
				
				if (selectObject != null) {
					String selectItem = selectObject.toString();
					
					// Disable the Add button if "--Attribute--" or "-- Filter ---" is selected
					if (selectItem.equalsIgnoreCase(filtersSeparator) ||selectItem.equalsIgnoreCase(attributesSeperator)) {
						btnAddFilterWidget.setEnabled(false);
					} else {
						btnAddFilterWidget.setEnabled(true);
					}
				}
			}
		}	
	}

	public void actionPerformed(ActionEvent e) {
		Object _actionObject = e.getSource();

		// handle Button events
		if (_actionObject instanceof JButton) {
			JButton _btn = (JButton) _actionObject;

			if (_btn == btnApplyFilter) {
				CompositeFilter theFilterToApply = (CompositeFilter) cmbFilters.getSelectedItem();
				final CyNetwork currentNetwork = applicationManager.getCurrentNetwork();
				if (currentNetwork == null)
					return;
				theFilterToApply.setNetwork(currentNetwork);
				FilterUtil.doSelection(theFilterToApply, applicationManager);
			}
			if (_btn == btnAddFilterWidget) {
				//btnAddFilterWidget is clicked!
				CompositeFilter selectedFilter = (CompositeFilter) cmbFilters.getSelectedItem();
				FilterSettingPanel theSettingPanel = filter2SettingPanelMap.get(selectedFilter);

				if (cmbAttributes.getSelectedItem() instanceof String) {
					String selectItem = (String) cmbAttributes.getSelectedItem();
					if (selectItem.equalsIgnoreCase(filtersSeparator) ||selectItem.equalsIgnoreCase(attributesSeperator)) {
						return;
					}
				}
				
				String attributeType = cmbAttributes.getSelectedItem().toString().substring(0,4);// "node" or "edge"
				String attributeName = cmbAttributes.getSelectedItem().toString().substring(5);

				if(CyAttributesUtil.isNullAttribute(applicationManager.getCurrentNetwork(), attributeType, attributeName)){
					JOptionPane.showMessageDialog(this, "All the values for this attribute are NULL!", "Can not create filter", JOptionPane.ERROR_MESSAGE); 
				}
				else {
					theSettingPanel.addNewWidget(cmbAttributes.getSelectedItem());					
				}
			}
			if (_btn == btnSelectAll){
				CyNetwork cyNetwork = applicationManager.getCurrentNetwork();
				SelectUtil.selectAllNodes(cyNetwork);
				SelectUtil.selectAllEdges(cyNetwork);
			}
			if (_btn == btnDeSelect){
				CyNetwork cyNetwork = applicationManager.getCurrentNetwork();
				SelectUtil.unselectAllNodes(cyNetwork);
				SelectUtil.unselectAllEdges(cyNetwork);
			}
			
		} // JButton event
		
		if (_actionObject instanceof JMenuItem) {
			CyNetwork cyNetwork = applicationManager.getCurrentNetwork();
			JMenuItem _menuItem = (JMenuItem) _actionObject;
			
			if (_menuItem == newFilterMenuItem || _menuItem == newTopologyFilterMenuItem 
			    || _menuItem == newNodeInteractionFilterMenuItem || _menuItem == newEdgeInteractionFilterMenuItem) {
				String filterType = "Composite";
				
				if (_menuItem == newTopologyFilterMenuItem) {
					filterType = "Topology";
					if (cyNetwork != null) {
						SelectUtil.unselectAllNodes(cyNetwork);
					}
				}
				if (_menuItem == newNodeInteractionFilterMenuItem) {
					filterType = "NodeInteraction";
					if (cyNetwork != null) {
						SelectUtil.unselectAllNodes(cyNetwork);
					}
				}
				if (_menuItem == newEdgeInteractionFilterMenuItem) {
					filterType = "EdgeInteraction";
					if (cyNetwork != null) {
						SelectUtil.unselectAllNodes(cyNetwork);
					}
				}
				
				String newFilterName = "";
				
				while (true) {
					newFilterName = JOptionPane.showInputDialog(
										    this, "New filter name", "New Filter Name",
										    JOptionPane.INFORMATION_MESSAGE);

					if (newFilterName == null) { // user clicked "cancel"
						break;
					}
					
					if (newFilterName.trim().equals("")) {
						Object[] options = { "OK" };
						JOptionPane.showOptionDialog(this,
									     "Filter name is empty!", "Warning",
									     JOptionPane.DEFAULT_OPTION,
									     JOptionPane.WARNING_MESSAGE, null, options,
									     options[0]);
						continue;
					}
					
					if (org.cytoscape.filter.internal.filters.util.FilterUtil
					    .isFilterNameDuplicated(modelLocator.getFilters(),  newFilterName)) {
						Object[] options = { "OK" };
						JOptionPane.showOptionDialog(this,
									     "Filter name already existed!", "Warning",
									     JOptionPane.DEFAULT_OPTION,
									     JOptionPane.WARNING_MESSAGE, null, options,
									     options[0]);
						continue;
					}
					break;
				}// while loop

				if ((newFilterName != null)
				    && (!newFilterName.trim().equals(""))) {
					createNewFilter(newFilterName, filterType);
					
					//pcs.firePropertyChange("NEW_FILTER_CREATED", "", "");
					
					//					// TODO: Port this
					//					if (FilterPlugin.shouldFireFilterEvent) {
					//						PropertyChangeEvent evt = new PropertyChangeEvent(this, "NEW_FILTER_CREATED", null, null);
					//						Cytoscape.getPropertyChangeSupport().firePropertyChange(evt);						
					//					}
				}
			} else if (_menuItem == deleteFilterMenuItem) {
				CompositeFilter theSelectedFilter = (CompositeFilter)cmbFilters.getSelectedItem();	
				if (theSelectedFilter == null) {
					return;
				}

				Object[] options = { "YES", "CANCEL" };
				int userChoice = JOptionPane.showOptionDialog(this,
									      "Are you sure you want to delete " + theSelectedFilter.getName()
									      + "?", "Warning", JOptionPane.DEFAULT_OPTION,
									      JOptionPane.WARNING_MESSAGE, null, options, options[0]);

				if (userChoice == 1) { // user clicked CANCEL
					return;
				}
				deleteFilter(theSelectedFilter);
				//				// TODO: Port this?  No one listens for these events
				//				if (FilterPlugin.shouldFireFilterEvent) {
				//					PropertyChangeEvent evt = new PropertyChangeEvent(this, "FILTER_DELETED", null, null);
				//					Cytoscape.getPropertyChangeSupport().firePropertyChange(evt);
				//				}
			} else if (_menuItem == renameFilterMenuItem) {
				CompositeFilter theSelectedFilter = (CompositeFilter)cmbFilters.getSelectedItem();				
				if (theSelectedFilter == null) {
					return;
				}
				renameFilter();
				//				// TODO: Port this?  No one listens for these events
				//				if (FilterPlugin.shouldFireFilterEvent) {
				//					PropertyChangeEvent evt = new PropertyChangeEvent(this, "FILTER_RENAMED", null, null);
				//					Cytoscape.getPropertyChangeSupport().firePropertyChange(evt);
				//				}
			} else if (_menuItem == duplicateFilterMenuItem) {
				CompositeFilter theSelectedFilter = (CompositeFilter)cmbFilters.getSelectedItem();				
				if (theSelectedFilter == null) {
					return;
				}
				duplicateFilter();
				//				// TODO: Port this?  No one listens for these events
				//				if (FilterPlugin.shouldFireFilterEvent) {
				//					PropertyChangeEvent evt = new PropertyChangeEvent(this, "FILTER_DUPLICATED", null, null);
				//					Cytoscape.getPropertyChangeSupport().firePropertyChange(evt);
				//				}
			}
		} // JMenuItem event
		
		updateInteractionMenuItemStatus();
		updateView();
	}
	
	private void updateView() {
		eventHelper.flushPayloadEvents();
		final CyNetworkView currentView = applicationManager.getCurrentNetworkView();
		
		if (currentView != null)
			currentView.updateView();
	}

	private void updateInteractionMenuItemStatus() {
		Vector<CompositeFilter> allFilters = modelLocator.getFilters(); //filterPlugin.getAllFilterVect();
		
		//Disable interactionMenuItem if there is no other filters to depend on
		if (allFilters == null || allFilters.size() == 0) {
			newNodeInteractionFilterMenuItem.setEnabled(false);
			newEdgeInteractionFilterMenuItem.setEnabled(false);
			return;
		}
		
		// Set newEdgeInteractionFilterMenuItem on only if there are at least one Node Filter
		if (hasNodeFilter(allFilters)) {
			newEdgeInteractionFilterMenuItem.setEnabled(true);	
		} else {
			newEdgeInteractionFilterMenuItem.setEnabled(false);
		}

		// Set newNodeInteractionFilterMenuItem on only if there are at least one  Edge Filter
		if (hasEdgeFilter(allFilters)) {
			newNodeInteractionFilterMenuItem.setEnabled(true);
		} else {
			newNodeInteractionFilterMenuItem.setEnabled(false);
		}
	}
	
	// Check if there are any NodeFilter in the AllFilterVect
	private boolean hasNodeFilter(Vector<CompositeFilter> pAllFilterVect) {
		boolean selectNode = false;

		for (CompositeFilter curFilter : pAllFilterVect) {
			if (curFilter.getAdvancedSetting().isNodeChecked()) {
				selectNode = true;
			}			
		}

		return selectNode;
	}

	// Check if there are any NodeFilter in the AllFilterVect
	private boolean hasEdgeFilter(Vector<CompositeFilter> pAllFilterVect) {
		boolean selectEdge = false;

		for (CompositeFilter curFilter : pAllFilterVect) {
			if (curFilter.getAdvancedSetting().isEdgeChecked()) {
				selectEdge = true;
			}			
		}

		return selectEdge;
	}
	
	//Each time, the FilterMainPanel become visible, update the status of InteractionMaenuItems
	class MyComponentAdapter extends ComponentAdapter {
		public void componentShown(ComponentEvent e) {
			updateInteractionMenuItemStatus();
		}
	}

	private void duplicateFilter(){
		CompositeFilter theFilter = (CompositeFilter)cmbFilters.getSelectedItem();
	
		String tmpName = "Copy of " + theFilter.getName();
		String newFilterName = null;
		
		while (true) {
			Vector<String> nameVect = new Vector<String>();
			nameVect.add(tmpName);
			
			EditNameDialog theDialog = new EditNameDialog("Copy Filter", "Please enter a new Filter name:", nameVect, 300,170);
			theDialog.setLocationRelativeTo(this);
			theDialog.setVisible(true);
			
			newFilterName = nameVect.elementAt(0);

			if ((newFilterName == null)) { // cancel buton is clicked
				return;
			}
			
			if (FilterUtil.isFilterNameDuplicated(modelLocator.getFilters(), newFilterName)) {
				Object[] options = { "OK" };
				JOptionPane.showOptionDialog(this,
							     "Filter name already existed!", "Warning",
							     JOptionPane.DEFAULT_OPTION,
							     JOptionPane.WARNING_MESSAGE, null, options,
							     options[0]);
				continue;
			}

			break;
		}// while loop

		CompositeFilter newFilter = (CompositeFilter) theFilter.clone(); 
		newFilter.setName(newFilterName);
		
		Vector<CompositeFilter> allFilters = modelLocator.getFilters();
		allFilters.add(newFilter);
		FilterSettingPanel newFilterSettingPanel = new FilterSettingPanel(quickFind, this, newFilter, modelLocator,
				applicationManager, eventHelper);
		filter2SettingPanelMap.put(newFilter, newFilterSettingPanel);
		
		// set the new filter in the combobox selected
		cmbFilters.setSelectedItem(newFilter);
	}	
	
	
	private void renameFilter(){
		CompositeFilter theFilter = (CompositeFilter)cmbFilters.getSelectedItem();
		String oldFilterName = theFilter.getName();
		String newFilterName = "";
		
		while (true) {
			Vector<String> nameVect = new Vector<String>();
			nameVect.add(oldFilterName);
			
			EditNameDialog theDialog = new EditNameDialog("Edit Filter Name", "Please enter a new Filter name:", nameVect, 300,170);
			theDialog.setLocationRelativeTo(this);
			theDialog.setVisible(true);
			
			newFilterName = nameVect.elementAt(0);

			if ((newFilterName == null) || newFilterName.trim().equals("") 
			    ||newFilterName.equals(oldFilterName)) {
				return;
			}
			
			if (FilterUtil.isFilterNameDuplicated(modelLocator.getFilters(), newFilterName)) {
				Object[] options = { "OK" };
				JOptionPane.showOptionDialog(this,
							     "Filter name already existed!", "Warning",
							     JOptionPane.DEFAULT_OPTION,
							     JOptionPane.WARNING_MESSAGE, null, options,
							     options[0]);
				continue;
			}

			break;
		}// while loop

		theFilter.setName(newFilterName);
		cmbFilters.setSelectedItem(theFilter);
		refreshFilterSelectCMB();
	}
		
	private void deleteFilter(CompositeFilter pFilter) {
		filter2SettingPanelMap.remove(pFilter);
		cmbFilters.removeItem(pFilter);
		modelLocator.removeFilter(pFilter);
		
		this.validate();
		this.repaint();
	}

	private void createNewFilter(String pFilterName, String pFilterType) {
		// Create an empty filter, add it to the current filter list
		CompositeFilter newFilter = null;
		
		if (pFilterType.equalsIgnoreCase("Topology")) {
			newFilter =  new TopologyFilter(applicationManager);
			newFilter.getAdvancedSetting().setEdge(false);
			newFilter.setName(pFilterName);			
		} else if (pFilterType.equalsIgnoreCase("NodeInteraction")) {
			newFilter =  new NodeInteractionFilter(applicationManager);
			//newFilter.getAdvancedSetting().setEdge(false);
			newFilter.setName(pFilterName);			
		} else if (pFilterType.equalsIgnoreCase("EdgeInteraction")) {
			newFilter =  new EdgeInteractionFilter(applicationManager);
			//newFilter.getAdvancedSetting().setEdge(false);
			newFilter.setName(pFilterName);			
		} else {
			newFilter = new CompositeFilter(pFilterName);
		}
		
		newFilter.setNetwork(applicationManager.getCurrentNetwork());
		modelLocator.addFilter(newFilter);
		
		FilterSettingPanel newFilterSettingPanel = new FilterSettingPanel(quickFind, this, newFilter, modelLocator,
				applicationManager, eventHelper);
		filter2SettingPanelMap.put(newFilter, newFilterSettingPanel);

		// set the new filter in the combobox selected
		cmbFilters.setSelectedItem(newFilter);

		if (pFilterType.equalsIgnoreCase("Composite")) {
			updateCMBAttributes();
		}
	}

	class AttributeFilterRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = -9137647911856857211L;

		public AttributeFilterRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
							      int index, boolean isSelected, boolean cellHasFocus) {
			if (value != null) {
				if (value instanceof String) {
					setText((String)value);
				}
				else if (value instanceof CompositeFilter) {
					CompositeFilter theFilter = (CompositeFilter) value;
					setText(theFilter.getName());
				}
			}
			else { // value == null
				setText("");
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			return this;
		}
	}// AttributeRenderer

	class FilterRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = -2396393425644165756L;

		public FilterRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
							      int index, boolean isSelected, boolean cellHasFocus) {
			if (value != null) {
				CompositeFilter theFilter = (CompositeFilter) value;
				setText(theFilter.getLabel());
			} else { // value == null
				setText(""); 
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			return this;
		}
	}// FilterRenderer

	class FilterSelectWidestStringComboBoxModel extends WidestStringComboBoxModel {
		private static final long serialVersionUID = -1311538859326314189L;

		public FilterSelectWidestStringComboBoxModel() {
			super();
		}

		public FilterSelectWidestStringComboBoxModel(Object[] items) {
			super(items);
		}

		public FilterSelectWidestStringComboBoxModel(Vector<?> v) {
			super(v);
		}

		@Override
			protected String getLabel(Object anObject) {
			return (anObject != null) ? ((CompositeFilter)anObject).getLabel() : "";
		}
	}

	class AttributeSelectWidestStringComboBoxModel extends WidestStringComboBoxModel {
		private static final long serialVersionUID = -7287008568486671513L;

		public AttributeSelectWidestStringComboBoxModel() {
			super();
		}

		public AttributeSelectWidestStringComboBoxModel(Object[] items) {
			super(items);
		}

		public AttributeSelectWidestStringComboBoxModel(Vector<?> v) {
			super(v);
		}

		@Override
		protected String getLabel(Object anObject) {
			String rv = "";

			if (anObject != null) {
				if (anObject instanceof CompositeFilter) {
					rv = ((CompositeFilter)anObject).getLabel();
				} else {
					rv = anObject.toString();
				}
			}

			return rv;
		}
	}
}
