package org.cytoscape.filter.internal.filters.view;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.filter.internal.filters.model.CompositeFilter;
import org.cytoscape.filter.internal.filters.model.FilterModelLocator;
import org.cytoscape.filter.internal.filters.model.TopologyFilter;
import org.cytoscape.filter.internal.filters.util.FilterUtil;
import org.cytoscape.filter.internal.filters.util.WidestStringComboBoxPopupMenuListener;
import org.cytoscape.view.model.CyNetworkView;



/**
 * @author Peng
 */
@SuppressWarnings("serial")
public class TopoFilterPanel extends JPanel implements ActionListener, ItemListener {

	private final TopologyFilter theFilter;
	private final CyApplicationManager applicationManager;
	private final CyEventHelper eventHelper;
	private final FilterModelLocator modelLocator;
 
    /** Creates new form TopoFilterPanel 
     * @param eventHelper */
	public TopoFilterPanel(final TopologyFilter pFilter,
						   final FilterModelLocator modelLocator,
						   final CyApplicationManager applicationManager,
						   final CyEventHelper eventHelper) {
		this.modelLocator = modelLocator;
		
		this.applicationManager = applicationManager;
		this.eventHelper = eventHelper;
    	
    	theFilter = pFilter;
        setName(theFilter.getName());
        initComponents();

        buildCMBmodel();

		cmbPassFilter.setRenderer(new FilterRenderer());
		
		//add EventListeners
		tfDistance.addActionListener(this);
		tfMinNeighbors.addActionListener(this);

		MyKeyListener l = new MyKeyListener();
		tfDistance.addKeyListener(l);
		tfMinNeighbors.addKeyListener(l);
		
		cmbPassFilter.addItemListener(this);
				
		//Make sure bits will be calculated for the first time
		pFilter.childChanged();

		// Recovery initial values if any
        tfMinNeighbors.setText(new Integer(pFilter.getMinNeighbors()).toString());        	
        tfDistance.setText(new Integer(pFilter.getDistance()).toString());

		MyComponentAdapter cmpAdpt = new MyComponentAdapter();
		addComponentListener(cmpAdpt);
    }

    /**
     * @deprecated
     * If nothing else calls this, then it's more sensible to just pass the panel
     * to listen to directly.
     */
    public void addParentPanelListener() {
		// Listen to the visible event from FilterSettingPanel
    	// To syn Filters in cmbPassFilter
		MyComponentAdapter cmpAdpt = new MyComponentAdapter();
        Component comp;

        comp = this;
        while (comp != null) {
            if (comp instanceof FilterSettingPanel) {
                comp.addComponentListener(cmpAdpt);
                break;
            }
            else {
                comp = comp.getParent();
            }
        }
    }
    
    public void addParentPanelListener(Component comp) {
		// Listen to the visible event from FilterSettingPanel
    	// To syn Filters in cmbPassFilter
		MyComponentAdapter cmpAdpt = new MyComponentAdapter();
        comp.addComponentListener(cmpAdpt);
    }

    //Each time, the FilterSettingPanel become visible, rebuild the model for the cmbPassFilter
	class MyComponentAdapter extends ComponentAdapter {
		public void componentShown(ComponentEvent e) {
			buildCMBmodel();
		}
	}
	
	private void buildCMBmodel() {
        // Create an empty filter, add to the top of the filter list in the combobox
		CompositeFilter emptyFilter = new CompositeFilter("None");

		Vector<CompositeFilter> tmpVect = new Vector<CompositeFilter>();
		tmpVect.add(emptyFilter);
		Vector<CompositeFilter> allFilters = modelLocator.getFilters();
		tmpVect.addAll(allFilters);
		
        PassFilterWidestStringComboBoxModel pfwscbm = new PassFilterWidestStringComboBoxModel(tmpVect);
        cmbPassFilter.setModel(pfwscbm);
        
        if (theFilter.getPassFilter() != null) {
        	cmbPassFilter.setSelectedIndex(0);			
			cmbPassFilter.setSelectedItem(theFilter.getPassFilter());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object _actionObject = e.getSource();
		
		// handle Button events
		if (_actionObject instanceof JTextField) {
			JTextField _tfObj = (JTextField) _actionObject;
			if (_tfObj == tfMinNeighbors) {
				int _neighbors = (new Integer(tfMinNeighbors.getText())).intValue();
				theFilter.setMinNeighbors(_neighbors);
			}
			else if (_tfObj == tfDistance) {
				int _distance = (new Integer(tfDistance.getText())).intValue();
				theFilter.setDistance(_distance);				
			}
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		
		//System.out.println("Entering TopoFilterPanel.itemStateChanged() ...");
		
		if (source instanceof JComboBox) {
			theFilter.setPassFilter((CompositeFilter) cmbPassFilter.getSelectedItem());
			theFilter.childChanged();

			// If network size is less than pre-defined threshold, apply theFilter automatically 
			if (FilterUtil.isDynamicFilter(theFilter)) {
				FilterUtil.doSelection(theFilter, applicationManager);
				updateView();
			}
		}
	}
	
	private void updateView() {
		eventHelper.flushPayloadEvents();
		final CyNetworkView currentView = applicationManager.getCurrentNetworkView();
		
		if (currentView != null)
			currentView.updateView();
	}

	
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lbSelectNodeEdge = new javax.swing.JLabel();
        lbWithAtLeast = new javax.swing.JLabel();
        tfMinNeighbors = new javax.swing.JTextField();
        lbNeighbors = new javax.swing.JLabel();
        lbWithinDistance = new javax.swing.JLabel();
        tfDistance = new javax.swing.JTextField();
        lbThatPassTheFilter = new javax.swing.JLabel();
        cmbPassFilter = new javax.swing.JComboBox();
        cmbPassFilter.setModel(new PassFilterWidestStringComboBoxModel());
        cmbPassFilter.addPopupMenuListener(new WidestStringComboBoxPopupMenuListener());
        lbPlaceHolder = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        lbSelectNodeEdge.setText("Select nodes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        add(lbSelectNodeEdge, gridBagConstraints);

        lbWithAtLeast.setText("with at least");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        add(lbWithAtLeast, gridBagConstraints);

        tfMinNeighbors.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        add(tfMinNeighbors, gridBagConstraints);

        lbNeighbors.setText("neighbors");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        add(lbNeighbors, gridBagConstraints);

        lbWithinDistance.setText("within distance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        add(lbWithinDistance, gridBagConstraints);

        tfDistance.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        add(tfDistance, gridBagConstraints);

        lbThatPassTheFilter.setText("and the nodes pass the filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(lbThatPassTheFilter, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(cmbPassFilter, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(lbPlaceHolder, gridBagConstraints);

    }// </editor-fold>
    
    // Variables declaration - do not modify
    private javax.swing.JComboBox cmbPassFilter;
    private javax.swing.JLabel lbNeighbors;
    private javax.swing.JLabel lbPlaceHolder;
    private javax.swing.JLabel lbSelectNodeEdge;
    private javax.swing.JLabel lbThatPassTheFilter;
    private javax.swing.JLabel lbWithAtLeast;
    private javax.swing.JLabel lbWithinDistance;
    private javax.swing.JTextField tfDistance;
    private javax.swing.JTextField tfMinNeighbors;
    // End of variables declaration
	
    class FilterRenderer extends JLabel implements ListCellRenderer {
        public FilterRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                if (value == theFilter) {
                    setText("");
                }
                else {
                    CompositeFilter tmpFilter = (CompositeFilter) value;
                    setText(tmpFilter.getName());
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
    }

	class MyKeyListener extends KeyAdapter {

		public void keyReleased(KeyEvent e) {
			Object _actionObject = e.getSource();

			if (tfMinNeighbors.getText().trim().equalsIgnoreCase("")
					|| tfDistance.getText().trim().equalsIgnoreCase("")) {
				return;
			}
			if (_actionObject instanceof JTextField) {
				JTextField _tfObj = (JTextField) _actionObject;
				if (_tfObj == tfMinNeighbors) {

					// Validate the data
					// try {
					// Integer.parseInt(tfMinNeighbors.getText());
					// }
					// catch (NumberFormatException nfe) {
					// JOptionPane.showMessageDialog((Component)e.getSource(),
					// "Invalid values", "Warning", JOptionPane.ERROR_MESSAGE);
					// return;
					// }

					int _neighbors = (new Integer(tfMinNeighbors.getText())).intValue();
					theFilter.setMinNeighbors(_neighbors);
				} else if (_tfObj == tfDistance) {
					int _distance = (new Integer(tfDistance.getText())).intValue();
					theFilter.setDistance(_distance);
				}
			}
		}
	}
}
