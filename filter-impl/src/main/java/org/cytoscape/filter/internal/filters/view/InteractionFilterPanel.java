/*
 Copyright (c) 2006, 2007, 2009, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.filter.internal.filters.view;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.filter.internal.filters.model.CompositeFilter;
import org.cytoscape.filter.internal.filters.model.EdgeInteractionFilter;
import org.cytoscape.filter.internal.filters.model.FilterModelLocator;
import org.cytoscape.filter.internal.filters.model.InteractionFilter;
import org.cytoscape.filter.internal.filters.model.NodeInteractionFilter;
import org.cytoscape.filter.internal.filters.util.FilterUtil;
import org.cytoscape.filter.internal.filters.util.WidestStringComboBoxPopupMenuListener;
import org.cytoscape.view.model.CyNetworkView;


@SuppressWarnings("serial")
public class InteractionFilterPanel extends JPanel implements ItemListener{

	private final InteractionFilter theFilter;
	private final CyApplicationManager applicationManager;
	private final CyEventHelper eventHelper;
	private final FilterModelLocator modelLocator;

    /** Creates new form InteractionFilterPanel */
	public InteractionFilterPanel(final InteractionFilter pFilter,
								  final FilterModelLocator modelLocator,
								  final CyApplicationManager applicationManager,
								  final CyEventHelper eventHelper) {
		this.applicationManager = applicationManager;
		this.eventHelper = eventHelper;
		this.modelLocator = modelLocator;
		theFilter = pFilter;
		setName(theFilter.getName());

		initComponents();
		
		if (pFilter instanceof EdgeInteractionFilter) {
			changeLabelText();
		}
        
        buildCMBmodel();

		cmbPassFilter.setRenderer(new FilterRenderer());
		
		//add EventListeners
		chkSource.addItemListener(this);
		chkTarget.addItemListener(this);
		
		cmbPassFilter.addItemListener(this);
				
		//Make sure bits will be calculated for the first time
		pFilter.childChanged();

		// Recovery initial values
        chkSource.setSelected(pFilter.isSourceChecked());        	
        chkTarget.setSelected(pFilter.isTargetChecked());       
        
        //Initialize the passFilter, if it is a new fIlter
        if (theFilter.getPassFilter().getName().equalsIgnoreCase("None")) {			
        	// thePassFilter name == "None", it's a brand new filter
        	// Set the passFilter the first appropriate filter in the comboBox 
        	int index = 0;
        	CompositeFilter curFilter = null;
        	for (int i=0; i< cmbPassFilter.getModel().getSize(); i++) {
        		curFilter = (CompositeFilter)cmbPassFilter.getModel().getElementAt(i);        		
    			if ((theFilter instanceof EdgeInteractionFilter && curFilter.getAdvancedSetting().isNodeChecked())||
    				 theFilter instanceof NodeInteractionFilter && curFilter.getAdvancedSetting().isEdgeChecked()) {
    				index = i;
    				break;    				
    			}
        	}
        	theFilter.setPassFilter((CompositeFilter)cmbPassFilter.getModel().getElementAt(index));
        }
		cmbPassFilter.setSelectedItem(theFilter.getPassFilter());

		MyComponentAdapter cmpAdpt = new MyComponentAdapter();
		addComponentListener(cmpAdpt);
    }
    
    private void changeLabelText() {
    	lbSelectionType.setText("Select Edges");
    	lbNodeType.setText("with a node");
    	lbAboveFilter.setText("which pass the filter");
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
		//CompositeFilter emptyFilter = new CompositeFilter("None");
		Vector<CompositeFilter> allFilters = modelLocator.getFilters();
		
        PassFilterWidestStringComboBoxModel pfwscbm = new PassFilterWidestStringComboBoxModel(new Vector<CompositeFilter>(allFilters));
        cmbPassFilter.setModel(pfwscbm);
        
        if (theFilter.getPassFilter() != null) {
        	cmbPassFilter.setSelectedIndex(0);
			cmbPassFilter.setSelectedItem(theFilter.getPassFilter());
		}
	}
    
    	
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();		
		//System.out.println("Entering InteractionFilterPanel.itemStateChanged() ...");
		Object soureObj= e.getSource();
		
		if (soureObj instanceof JCheckBox) {
			JCheckBox theCheckBox = (JCheckBox) soureObj;
			
			if (theCheckBox == chkSource) {
				theFilter.setSourceChecked(chkSource.isSelected());
			}
			if (theCheckBox == chkTarget) {
				theFilter.setTargetChecked(chkTarget.isSelected());
			}	
		}
		
		if (source instanceof JComboBox) {
			theFilter.setPassFilter((CompositeFilter) cmbPassFilter.getSelectedItem());
		}
		
		theFilter.childChanged();
		
		// If network size is less than pre-defined threshold, apply theFilter automatically 
		if (FilterUtil.isDynamicFilter(theFilter)) {
			FilterUtil.doSelection(theFilter, applicationManager);
			updateView();
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

        lbSelectionType = new javax.swing.JLabel();
        lbNodeType = new javax.swing.JLabel();
        chkSource = new javax.swing.JCheckBox();
        chkTarget = new javax.swing.JCheckBox();
        lbAboveFilter = new javax.swing.JLabel();
        cmbPassFilter = new javax.swing.JComboBox();
        cmbPassFilter.setModel(new PassFilterWidestStringComboBoxModel());
        cmbPassFilter.addPopupMenuListener(new WidestStringComboBoxPopupMenuListener());
        lbPlaceHolder = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        lbSelectionType.setText("Select Nodes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(lbSelectionType, gridBagConstraints);

        lbNodeType.setText("which are the");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(lbNodeType, gridBagConstraints);

        chkSource.setText("Source");
        chkSource.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkSource.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(chkSource, gridBagConstraints);

        chkTarget.setText("Target");
        chkTarget.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkTarget.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(chkTarget, gridBagConstraints);

        lbAboveFilter.setText("of at least one edge which pass the filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(lbAboveFilter, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
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
    private javax.swing.JCheckBox chkSource;
    private javax.swing.JCheckBox chkTarget;
    private javax.swing.JComboBox cmbPassFilter;
    private javax.swing.JLabel lbAboveFilter;
    private javax.swing.JLabel lbNodeType;
    private javax.swing.JLabel lbPlaceHolder;
    private javax.swing.JLabel lbSelectionType;
    // End of variables declaration                   
	
	class FilterRenderer extends JLabel implements ListCellRenderer {
		
		public FilterRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			if (value == null)  {
				setText("");
				return this;
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			//Ignore self
			if (value == theFilter) {
				setText(""); 

				return this;
			}

			//Display related filters only
			CompositeFilter tmpFilter = (CompositeFilter) value;
			
			if ((theFilter instanceof EdgeInteractionFilter && tmpFilter.getAdvancedSetting().isNodeChecked()) ||
					(theFilter instanceof NodeInteractionFilter && tmpFilter.getAdvancedSetting().isEdgeChecked())) {
				setText(tmpFilter.getName());											
			}
			else {
				setText("");
			}

			return this;
		}
	}
}
