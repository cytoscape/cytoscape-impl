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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.JTextComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.filter.internal.filters.AtomicFilter;
import org.cytoscape.filter.internal.filters.CompositeFilter;
import org.cytoscape.filter.internal.filters.CyFilter;
import org.cytoscape.filter.internal.filters.FilterPlugin;
import org.cytoscape.filter.internal.filters.InteractionFilter;
import org.cytoscape.filter.internal.filters.NumericFilter;
import org.cytoscape.filter.internal.filters.Relation;
import org.cytoscape.filter.internal.filters.StringFilter;
import org.cytoscape.filter.internal.filters.TopologyFilter;
import org.cytoscape.filter.internal.filters.util.FilterUtil;
import org.cytoscape.filter.internal.prefuse.data.query.NumberRangeModel;
import org.cytoscape.filter.internal.prefuse.util.ui.JRangeSlider;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.quickfind.util.QuickFindFactory;
import org.cytoscape.filter.internal.quickfind.util.TaskMonitorBase;
import org.cytoscape.filter.internal.widgets.autocomplete.index.GenericIndex;
import org.cytoscape.filter.internal.widgets.autocomplete.index.Hit;
import org.cytoscape.filter.internal.widgets.autocomplete.index.NumberIndex;
import org.cytoscape.filter.internal.widgets.autocomplete.index.TextIndex;
import org.cytoscape.filter.internal.widgets.autocomplete.view.ComboBoxFactory;
import org.cytoscape.filter.internal.widgets.autocomplete.view.TextIndexComboBox;
import org.cytoscape.filter.internal.widgets.slider.JRangeSliderExtended;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.CyNetworkView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilterSettingPanel extends JPanel {
	
	private static final ImageIcon plusIcon = new ImageIcon(
			FilterSettingPanel.class.getResource("/images/plus.gif"));
	private static final ImageIcon minusIcon = new ImageIcon(
			FilterSettingPanel.class.getResource("/images/minus.gif"));
	private static final ImageIcon delIcon = new ImageIcon(FilterSettingPanel.class
			.getResource("/images/delete.png"));

	private CompositeFilter theFilter;
	private FilterMainPanel parentPanel;
	private CyNetwork currentNetwork = null;
	private TopoFilterPanel topoPanel = null;
	private InteractionFilterPanel interactionPanel = null;
	private final Logger logger;
	private final CyApplicationManager applicationManager;
	private final CyEventHelper eventHelper;
	
	public FilterSettingPanel(FilterMainPanel pParent, Object pFilterObj, CyApplicationManager applicationManager, FilterPlugin filterPlugin, CyEventHelper eventHelper) {
		this.applicationManager = applicationManager;
		this.eventHelper = eventHelper;
		
		logger = LoggerFactory.getLogger(getClass());
		theFilter = (CompositeFilter) pFilterObj;
        setName(theFilter.getName());
		parentPanel = pParent;
		initComponents();
		
		initAdvancedSetting();
		
		// Select "node/edge" will be determined automatically through the attribute selected
		lbSelect.setVisible(false);			
		chkEdge.setVisible(false);
		chkNode.setVisible(false);						
		
		initCustomSetting();	
		
		if (pFilterObj instanceof TopologyFilter) {

			java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.weightx = 1.0;

			pnlCustomSettings.removeAll();
			topoPanel = new TopoFilterPanel((TopologyFilter)theFilter, applicationManager, filterPlugin, eventHelper);
			pnlCustomSettings.add(topoPanel, gridBagConstraints);
			//topoPanel.addParentPanelListener(); // Update passFilterCOM when shown
			topoPanel.addParentPanelListener(this); // Update passFilterCOM when shown

			addBlankLabelToCustomPanel();

			// Hide un-used components in AdvancedPanel
			lbRelation.setVisible(false);
			rbtAND.setVisible(false);
			rbtOR.setVisible(false);
			chkEdge.setVisible(false);
			chkNode.setEnabled(false);
			
			this.validate();
		}
		
		if (pFilterObj instanceof InteractionFilter) {

			java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.weightx = 1.0;

			pnlCustomSettings.removeAll();
			interactionPanel = new InteractionFilterPanel((InteractionFilter)theFilter, applicationManager, filterPlugin, eventHelper);
			pnlCustomSettings.add(interactionPanel, gridBagConstraints);
			//interactionPanel.addParentPanelListener(); // Update passFilterCOM when shown
			interactionPanel.addParentPanelListener(this); // Update passFilterCOM when shown

			addBlankLabelToCustomPanel();

			// Hide un-used components in AdvancedPanel
			lbRelation.setVisible(false);
			rbtAND.setVisible(false);
			rbtOR.setVisible(false);
			lbSelect.setVisible(false);			
			chkEdge.setVisible(false);
			chkNode.setVisible(false);
			
			this.validate();
		}
	}
	
	private void initCustomSetting() {
		List<CyFilter> theCustomFilterList = theFilter.getChildren();

		for (int i=0; i <theCustomFilterList.size();i++) {
			addWidgetRow(theCustomFilterList.get(i),i*2);
		}	
		addBlankLabelToCustomPanel();
		
		this.validate();
		this.repaint();
	}
	

	private Class<?> getAttributeDataType(CyNetwork network, String pAttribute, int pType) {
		Collection<? extends CyTableEntry> entries;
		if (pType == QuickFind.INDEX_NODES) {
			entries = network.getNodeList();
		} else if (pType == QuickFind.INDEX_EDGES) {
			entries = network.getEdgeList();
		} else {
			return null;
		}
		
		if (entries.size() == 0) {
			return null;
		}
		
		CyTableEntry entry = entries.iterator().next();
		CyRow row = entry.getCyRow();
		return row.getTable().getColumn(pAttribute).getType();
	}

	
	private JComboBox getTextIndexComboBox(StringFilter pFilter){
		TextIndexComboBox comboBox = null;

		try {		
			// If index doesnot exist, check if there is such attribute or
			
			if (!FilterUtil.hasSuchAttribute(pFilter.getNetwork(), pFilter.getControllingAttribute(), pFilter.getIndexType())) {
				// no such attribute
				JComboBox tmpCombo;
				if (pFilter.getSearchStr() != null) {
					Object[] objList = {pFilter.getSearchStr()};
					tmpCombo = new JComboBox(objList);
				}
				else {
					tmpCombo = new JComboBox();
				}
				tmpCombo.setEnabled(false);
				return tmpCombo;				
			}
			
			//	The attribute exists, create an index
			//final QuickFind quickFind = QuickFindFactory.getGlobalQuickFindInstance();
			//quickFind.reindexNetwork(Cytoscape.getCurrentNetwork(), pFilter.getIndexType(),
			//		pFilter.getControllingAttribute(), new TaskMonitorBase());					
			//TextIndex index_by_thisAttr = (TextIndex) quickFind.getIndex(Cytoscape.getCurrentNetwork());

			pFilter.setIndex(createTextIndex(pFilter));					
			
			comboBox = ComboBoxFactory.createTextIndexComboBox((TextIndex)pFilter.getIndex(), 2.0);				

			//  Set Size of ComboBox Display, based on # of specific chars
			comboBox.setPrototypeDisplayValue("01234567");

			//  Set Max Size of ComboBox to match preferred size
			comboBox.setMaximumSize(comboBox.getPreferredSize());			
			comboBox.setName(pFilter.getControllingAttribute());
			
			if (pFilter.getSearchStr() != null) {
				comboBox.setSelectedItem(pFilter.getSearchStr());							
			}

			ActionListener listener = new UserSelectionListener(comboBox);
			comboBox.addFinalSelectionListener(listener);
						
			final JTextComponent editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
			ComboBoxFocusListener focuslistener = new ComboBoxFocusListener(comboBox);
			editor.addFocusListener(focuslistener);
			
		} catch (Exception e) {
			logger.error("Exception in FilterSettingpanel.getTextIndexComboBox()");
		}

		return comboBox;
	}
	
	
	private JRangeSliderExtended getRangerSlider(NumericFilter pFilter) {
		//System.out.println("Entering FilterSettingPanel.getRangerSlider()...");
		
		NumberIndex theIndex = createNumberIndex(pFilter);

		if (theIndex != null) {
			//System.out.println("theIndex != null");
			pFilter.setIndex(theIndex);
		}
		else {
			//System.out.println("theIndex == null");
		}
		
		NumberRangeModel rangeModel = null;
		if (theIndex == null) {
			rangeModel = new NumberRangeModel(0,0,0,0);			
		}
		else {
			Class<?> dataType = getAttributeDataType(pFilter.getNetwork(), pFilter.getControllingAttribute(), pFilter.getIndexType());
			//Initialize the search values, lowBound and highBound	

			if (pFilter.getLowBound() == null) {
				pFilter.setLowBound(theIndex.getMinimumValue());
			}
			if (pFilter.getHighBound() == null) {
				pFilter.setHighBound(theIndex.getMaximumValue());
			}
			
			if (dataType == Double.class) {
				Double lowB = (Double)pFilter.getLowBound();
				Double highB = (Double)pFilter.getHighBound();
				Double min = (Double)theIndex.getMinimumValue();
				Double max = (Double)theIndex.getMaximumValue();

				rangeModel = new NumberRangeModel(lowB.doubleValue(),highB.doubleValue(),min.doubleValue(),max.doubleValue());				
			}
			else if (dataType == Integer.class) {
				rangeModel = new NumberRangeModel(pFilter.getLowBound(),pFilter.getHighBound(),
						theIndex.getMinimumValue(),theIndex.getMaximumValue());		
			}
		}
		
		JRangeSliderExtended rangeSlider = new JRangeSliderExtended(rangeModel, JRangeSlider.HORIZONTAL,
                JRangeSlider.LEFTRIGHT_TOPBOTTOM);		
		
		rangeSlider.setMinimumSize(new Dimension(100,20));
		rangeSlider.setPreferredSize(new Dimension(100,20));

		RangeSelectionListener rangeSelectionListener = new RangeSelectionListener(rangeSlider);
		rangeSlider.addChangeListener(rangeSelectionListener);
		rangeSlider.setName(pFilter.getControllingAttribute());
		
		RangeSlideMouseAdapter l = new RangeSlideMouseAdapter(); 
		rangeSlider.addMouseListener(l);
		
		MyMouseInputAdapter myMouseInputAdapter = new MyMouseInputAdapter();		
		rangeSlider.addMouseMotionListener(myMouseInputAdapter);

		return rangeSlider;
	}
	
	private class MyMouseInputAdapter extends MouseInputAdapter {
		public void mouseMoved(MouseEvent e) {
			String toolTipText = "";
			JRangeSliderExtended _range = null;
			NumberRangeModel _model = null;
			Object _obj = e.getSource();			
			if (_obj instanceof JRangeSliderExtended) {
				_range = (JRangeSliderExtended) _obj;
				_model = (NumberRangeModel) _range.getModel();
				toolTipText = _model.getLowValue() + " ~ " + _model.getHighValue() + " Double-click to edit"; 
				_range.setToolTipText(toolTipText);
			}
		}
	}
	
	public boolean hasNullIndexChildFilter() {
		List<CyFilter> children = theFilter.getChildren();
		if ((children == null)||(children.size() == 0)) {
			return false;
		}
		for (int i=0; i<children.size(); i++) {
			CyFilter child = children.get(i);
			if (child instanceof AtomicFilter) {
				AtomicFilter aFilter = (AtomicFilter) child;
				if (aFilter.getIndex() == null)
					return true;
			}
		}
	
		return false;
	}
	
	
	//  Refresh indices for widget after network switch or Cytoscape.ATTRIBUTES_CHANGED event is received
	// The method may be triggered by event of NETWORK_VIEW_FOCUSED
	public void refreshIndicesForWidgets(){
		// Check if each widget has associatd index, if not, try to create one
		//System.out.println("FilterSettingpanel:refreshIndicesForWidgets()...\n");
		List<CyFilter> children = theFilter.getChildren();
		if ((children == null)||(children.size() == 0)) {
			return;
		}
		
		CyNetwork network = applicationManager.getCurrentNetwork();
		
		for (int i=0; i<children.size(); i++) {
			CyFilter child = children.get(i);
			if (child instanceof StringFilter) {
				if (pnlCustomSettings.getComponent(i*5+3) instanceof TextIndexComboBox) {
					TextIndexComboBox theBox = (TextIndexComboBox) pnlCustomSettings.getComponent(i*5+3);
					if (network != null) {
						CyNetworkView networkView = applicationManager.getCurrentNetworkView();
						if (networkView != null) {
							TextIndex textIndex = createTextIndex((StringFilter) child);;
							if (textIndex != null) {
								theBox.setTextIndex(textIndex);
								StringFilter aFilter = (StringFilter) child;
								aFilter.setIndex(textIndex);					
							}
						}
					}
				}
			}
			if (child instanceof NumericFilter) {
				if (pnlCustomSettings.getComponent(i*5+3) instanceof JRangeSliderExtended) {
					JRangeSliderExtended theSlider = (JRangeSliderExtended) pnlCustomSettings.getComponent(i*5+3);
					if (network != null) {
						CyNetworkView networkView = applicationManager.getCurrentNetworkView();

						if (networkView != null) {
							NumberIndex numIndex = createNumberIndex((NumericFilter) child);;
							if (numIndex != null) {
								NumberRangeModel rangeModel = (NumberRangeModel) theSlider.getModel();
								rangeModel.setMinValue(numIndex.getMinimumValue());
								rangeModel.setMaxValue(numIndex.getMaximumValue());

								NumericFilter aFilter = (NumericFilter) child;
								aFilter.setIndex(numIndex);					
							}
						}
					}
				}
			}		
		}
	}
	
	/**
	 * Inner class Mouse listener for double click events on rangeSlider.
	 */
	public class RangeSlideMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent pMouseEvent)
		{
			if (pMouseEvent.getClickCount() >= 2)
			{
				Object srcObj = pMouseEvent.getSource();
				if (srcObj instanceof JRangeSliderExtended) {
					JRangeSliderExtended theSlider = (JRangeSliderExtended) srcObj; 
					NumberRangeModel model = (NumberRangeModel) theSlider.getModel();
					
					Vector<String> boundVect = new Vector<String>();
					boundVect.add(model.getLowValue().toString());
					boundVect.add(model.getHighValue().toString());
					boundVect.add(model.getMinValue().toString());
					boundVect.add(model.getMaxValue().toString());
										
					if (model.getLowValue().getClass().getName().equalsIgnoreCase("java.lang.Integer")) {
		            	EditRangeDialog theDialog = new EditRangeDialog(new javax.swing.JFrame(), true, theSlider.getName(), boundVect, "int");
		            	theDialog.setLocation(theSlider.getLocationOnScreen());
		            	theDialog.setVisible(true);
		                //if lowBound < min, set it to min
		                //if highBound > max, set it to max
		            	adjustBoundValues(boundVect, "int");

						model.setValueRange(new Integer(boundVect.elementAt(0)),new Integer(boundVect.elementAt(1)), (Integer) model.getMinValue(), (Integer) model.getMaxValue());						
					}
					else {
		            	EditRangeDialog theDialog = new EditRangeDialog(new javax.swing.JFrame(), true, theSlider.getName(), boundVect, "double");												
		            	theDialog.setLocation(theSlider.getLocationOnScreen());
		            	theDialog.setVisible(true);
		            	adjustBoundValues(boundVect, "double");
		            	model.setValueRange(new Double(boundVect.elementAt(0)),new Double(boundVect.elementAt(1)), (Double) model.getMinValue(), (Double) model.getMaxValue());
					}
					
					//Update the selection on screen
					theFilter.setNetwork(applicationManager.getCurrentNetwork());
					FilterUtil.doSelection(theFilter, applicationManager);
					updateView();
				}
			}
		}
	}

	private void updateView() {
		eventHelper.flushPayloadEvents();
		applicationManager.getCurrentNetworkView().updateView();
	}

	private void adjustBoundValues(Vector<String> pBoundVect, String pDataType){
		if (pDataType.equalsIgnoreCase("int")) {
	    	int lowBound = new Integer(pBoundVect.elementAt(0)).intValue();
	    	int highBound = new Integer(pBoundVect.elementAt(1)).intValue();
	    	int min = new Integer(pBoundVect.elementAt(2)).intValue();
	    	int max = new Integer(pBoundVect.elementAt(3)).intValue();
	    	if (lowBound < min) {
	    		lowBound = min;
	    		pBoundVect.setElementAt(new Integer(lowBound).toString(),0);
	    	}
	    	if (highBound > max) {
	    		highBound = max;
	    		pBoundVect.setElementAt(new Integer(highBound).toString(),1);	    		
	    	}
		}
		else if (pDataType.equalsIgnoreCase("double")) {
	    	double lowBound = new Double(pBoundVect.elementAt(0)).doubleValue();
	    	double highBound = new Double(pBoundVect.elementAt(1)).doubleValue();
	    	double min = new Double(pBoundVect.elementAt(2)).doubleValue();
	    	double max = new Double(pBoundVect.elementAt(3)).doubleValue();
	    	if (lowBound < min) {
	    		lowBound = min;
	    		pBoundVect.setElementAt(new Double(lowBound).toString(),0);
	    	}
	    	if (highBound > max) {
	    		highBound = max;
	    		pBoundVect.setElementAt(new Double(highBound).toString(),1);	    		
	    	}
		}
	}
	
	private AtomicFilter getAtomicFilterFromStr(String pCtrlAttribute, int pIndexType) {
		AtomicFilter retFilter = null;
		
		final QuickFind quickFind = QuickFindFactory.getGlobalQuickFindInstance();
		//quickFind.addNetwork(cyNetwork, new TaskMonitorBase());
		//index_by_UniqueIdentification = (TextIndex) quickFind.getIndex(cyNetwork);
		
		CyNetwork cyNetwork = applicationManager.getCurrentNetwork();
		quickFind.reindexNetwork(cyNetwork, pIndexType, pCtrlAttribute, new TaskMonitorBase());

		Class<?> attributeType = null;
		
		if (pIndexType == QuickFind.INDEX_NODES) {
			if (cyNetwork.getNodeCount() > 0) {
				CyNode node = cyNetwork.getNode(0);
				attributeType =
					node.getCyRow().getTable().getColumn(pCtrlAttribute).getType();
			}
		}
		else if (pIndexType == QuickFind.INDEX_EDGES) {
			if (cyNetwork.getEdgeCount() > 0) {
				CyEdge edge = cyNetwork.getEdge(0);
				attributeType =
					edge.getCyRow().getTable().getColumn(pCtrlAttribute).getType();
			}
		}
		//
		if ((attributeType == Integer.class)
				||(attributeType == Double.class)) {
				retFilter = new NumericFilter();
				retFilter.setControllingAttribute(pCtrlAttribute);
				retFilter.setIndexType(pIndexType);		

				//NumberIndex index_by_thisAttr = (NumberIndex) quickFind.getIndex(Cytoscape.getCurrentNetwork());

				retFilter.setIndex(quickFind.getIndex(cyNetwork));
				
		}
		else if ((attributeType == String.class||(attributeType == List.class||(attributeType == Boolean.class)))) {
				retFilter = new StringFilter();	
				retFilter.setControllingAttribute(pCtrlAttribute);
				retFilter.setIndexType(pIndexType);
				
				//TextIndex index_by_thisAttr = (TextIndex) quickFind.getIndex(Cytoscape.getCurrentNetwork());

				retFilter.setIndex(quickFind.getIndex(cyNetwork));
		}
		else {
				logger.error("AttributeType is not numeric/string/list/boolean!");
		}

		if (retFilter != null) {
			retFilter.setNetwork(cyNetwork);
		}
		return retFilter;
	}
	
	// Update the relation label after user click radio button "AND" or "OR" in AdvancedPanel
	private void updateRelationLabel(){
		Component[] allComponents = pnlCustomSettings.getComponents();
		
    	String relationStr = "AND";
    	if (theFilter.getAdvancedSetting().getRelation()== Relation.OR) {
    		relationStr = "OR";
    	}
    	
    	for (int i=0; i<allComponents.length; i++) {
			if (allComponents[i] instanceof JLabel) {
				JLabel theLabel = (JLabel) allComponents[i]; 
				String labelName = theLabel.getName();
				if ((labelName != null) &&(labelName.equalsIgnoreCase("RelationLabel"))) {
					theLabel.setText(relationStr);
					theLabel.repaint();
				}
			}
		}
	}
	
	//user Clicked CheckBox_Not left-side of the child filter
	private void updateNegationStatus(JCheckBox _chk) {
		int widgetGridY = (new Integer(_chk.getName())).intValue();
		int childIndex =widgetGridY/2;
		
		CyFilter childFilter = theFilter.getChildren().get(childIndex);
		if (childFilter instanceof CompositeFilter) {
			CompositeFilter tmpFilter = (CompositeFilter)childFilter;
			theFilter.setNotTable(tmpFilter, new Boolean(_chk.isSelected()));
		}
		else { // it is an AtomiCFilter
			childFilter.setNegation(_chk.isSelected());				
		}
		//Update the selection on screen
		doSelection();
	}
	
	
	private void addWidgetRow(CyFilter pFilter, int pGridY) {
		
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        if (pGridY > 0) {
        	// add a row to indicate the relationship between the widgets
        	String relationStr = "AND";
        	if (theFilter.getAdvancedSetting().getRelation()== Relation.OR) {
        		relationStr = "OR";
        	}

            //Col 2 ---> Label to indicate relationship between widgets
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = pGridY-1;
            JLabel lbRelation = new JLabel(relationStr);
            lbRelation.setName("RelationLabel");
            pnlCustomSettings.add(lbRelation, gridBagConstraints);        	
        }
    
        // Col 0 -- label with attributeName/Filter
		JLabel theLabel_col0 = new JLabel();

		if (pFilter instanceof AtomicFilter) {
			AtomicFilter atomicFilter = (AtomicFilter) pFilter;
			theLabel_col0.setText(atomicFilter.getControllingAttribute());
		}
		else {
			theLabel_col0.setText("Filter");
		}

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = pGridY;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        //gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        pnlCustomSettings.add(theLabel_col0, gridBagConstraints);
    	
		//Col 1 ---> chk box -- NOT
        final JCheckBox chkNot = new JCheckBox("Not");
        chkNot.setName(Integer.toString(pGridY));
        chkNot.setSelected(pFilter.getNegation());
        chkNot.addActionListener(
        		new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						updateNegationStatus(chkNot);
					}
				}
            );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = pGridY;
        //gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        pnlCustomSettings.add(chkNot, gridBagConstraints);

        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        //Col 2 ---> Widget if atomicFilter
		if (pFilter instanceof StringFilter) {
			JComboBox aBox = getTextIndexComboBox((StringFilter)pFilter);
	        pnlCustomSettings.add(aBox, gridBagConstraints);		
		}
		else if (pFilter instanceof NumericFilter) {
			JRangeSliderExtended theSlider = getRangerSlider((NumericFilter) pFilter);
			pnlCustomSettings.add(theSlider, gridBagConstraints);						
		}
		else {// CompositeFilter
			gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
	        //gridBagConstraints.weightx = 0.0;
			//gridBagConstraints.anchor = java.awt.GridBagConstraints.
	        pnlCustomSettings.add(new JLabel(pFilter.getName()), gridBagConstraints);		
		}
        gridBagConstraints.weightx = 0.0;
		//Col 3 ---> label (a trash can) for delete of the row
        JLabel theDelLabel = new JLabel();

        theDelLabel.setIcon(delIcon);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = pGridY;
        //gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pnlCustomSettings.add(theDelLabel, gridBagConstraints);
		
        theDelLabel.setName(Integer.toString(pGridY));
        theDelLabel.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {         	
                    	removeFilterWidget(e);
                    }
                }
            );
    	
        this.validate();

	}
	
	public void addNewWidget(Object pObj) {
		// The parameter pObj is the object selected from Attribute/Filter combobox
		// It can be either (1) a string with prefix "node."/"edge." (2) a CompositeFilter object 
		if (pObj instanceof CompositeFilter) {
			if (pObj == theFilter) {
				return; // Ignore if try to add self
			}
			
			CompositeFilter theCompositeFilter = (CompositeFilter) pObj;
			addWidgetRow(theCompositeFilter, theFilter.getChildren().size()*2);
			// Update theFilter object	
			theFilter.addChild(theCompositeFilter, new Boolean(false));

			//determine the filter selection type
			if (theCompositeFilter.getAdvancedSetting().isNodeChecked()) {
				theFilter.getAdvancedSetting().setNode(true);				
			}
			if (theCompositeFilter.getAdvancedSetting().isEdgeChecked()) {
				theFilter.getAdvancedSetting().setEdge(true);				
			}
		}
		else { //(pObj instanceof String)
			String tmpObj = (String)pObj;
			String ctrlAttribute = tmpObj.substring(5);

			int indexType = QuickFind.INDEX_NODES;
			if (tmpObj.startsWith("edge.")) {
				indexType = QuickFind.INDEX_EDGES;
				// determine the filter selection type
				theFilter.getAdvancedSetting().setEdge(true);
			}
			else {
				theFilter.getAdvancedSetting().setNode(true);
			}
			AtomicFilter newChildFilter = getAtomicFilterFromStr(ctrlAttribute, indexType);			
			addWidgetRow(newChildFilter, theFilter.getChildren().size()*2);
			// Update theFilter object		
			theFilter.addChild(newChildFilter);
		}
		// Update selection type of the filter setting
		updateSelectionType();
		
		//Update the selection on screen
		doSelection();
	}
	

	private void updateSelectionType() {
		boolean selectNode = false;
		boolean selectEdge = false;
		List<CyFilter> childFilters = theFilter.getChildren();
		for (int i=0; i< childFilters.size(); i++) {
			CyFilter child = childFilters.get(i);
			if (child instanceof AtomicFilter) {
				AtomicFilter tmp = (AtomicFilter) child;
				if (tmp.getIndexType() == QuickFind.INDEX_NODES) {
					selectNode = true;
				}
				if (tmp.getIndexType() == QuickFind.INDEX_EDGES) {
					selectEdge = true;
				}
			}
			else if (child instanceof CompositeFilter) {
				CompositeFilter tmp = (CompositeFilter) child;
				if (tmp.getAdvancedSetting().isNodeChecked()) {
					selectNode = true;
				}
				if (tmp.getAdvancedSetting().isEdgeChecked()) {
					selectEdge = true;
				}
			}
		}//end of for loop
		
		theFilter.getAdvancedSetting().setNode(selectNode);
		theFilter.getAdvancedSetting().setEdge(selectEdge);
	}
	
	// Determine the child index in filter based on the row index of a component 
	// (TextIndexComboBox or RangeSlider) in the customSetting panel
	private int getChildIndexFromComponent(Component pComponent) {
		int childIndex = -1;
		int componentCount = pnlCustomSettings.getComponentCount();
		for (int i = 0; i<componentCount; i++ ) {
			Component theComponent = pnlCustomSettings.getComponent(i);
			if (theComponent == pComponent){
				if (i<5) {
					childIndex =0;
				}
				else {
					childIndex = (i-2)/5;
				}
				break;
			}
		}
		return childIndex;
	}
	
	private void doSelection() {
		// If network size is greater than pre-defined threshold, don't apply theFilter automatically 
		if (FilterUtil.isDynamicFilter(theFilter)) {
			FilterUtil.doSelection(theFilter, applicationManager);
			updateView();
		}		
	}
	
	// remove a GUI widget from the customeSetting panel 
	private void removeFilterWidget(MouseEvent e)
	{
		Object _actionObject = e.getSource();
		
		if (_actionObject instanceof JLabel) {
			JLabel _lbl = (JLabel) _actionObject;
			int widgetGridY = (new Integer(_lbl.getName())).intValue();
			int childIndex =widgetGridY /2;
			
			theFilter.removeChildAt(childIndex);
			hidePopupForRangeSlider();
			pnlCustomSettings.removeAll();			
			initCustomSetting();
		}
		
		updateSelectionType();
		
		doSelection();
	}

	
	private void hidePopupForRangeSlider() {
		if (pnlCustomSettings.getComponentCount() == 0) {
			return;
		}
		
		int cmpCount = pnlCustomSettings.getComponentCount();
		for (int i=0; i< cmpCount; i++) {
			Component theCmp = pnlCustomSettings.getComponent(i);
			if (theCmp instanceof JRangeSliderExtended) {
				JRangeSliderExtended theSlider = (JRangeSliderExtended) theCmp;
				theSlider.resetPopup();
			}
		}
		
	}
	
	// Fix Bug #0001940
	class ComboBoxFocusListener implements FocusListener {
		private TextIndexComboBox comboBox = null;
		public ComboBoxFocusListener (TextIndexComboBox comboBox) {
			this.comboBox = comboBox;
		}
		
		public void focusLost(FocusEvent e) {
			// sync the StringFilter with the UI
			final JTextComponent editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
			String userInput = editor.getText();

			// Determine the row index of the TextIndexCombobox in the customSetting panel
			int widgetIndex = getChildIndexFromComponent(comboBox); 
							
			//Update theFilter Object
			List<CyFilter> theFilterlist = theFilter.getChildren();
			
			StringFilter theStringFilter = (StringFilter) theFilterlist.get(widgetIndex);
			theStringFilter.setSearchStr(userInput);
		}
		public void focusGained(FocusEvent e) {
			// do nothing
		}
	}
	
	
	/**
	 * Listens for Final Selection from User.
	 *
	 * @author Ethan Cerami.
	 */
	class UserSelectionListener implements ActionListener {
		private TextIndexComboBox comboBox;

		/**
		 * Constructor.
		 *
		 * @param comboBox TextIndexComboBox.
		 */
		public UserSelectionListener(TextIndexComboBox comboBox) {
			this.comboBox = comboBox;
		}

		/**
		 * User has made final selection.
		 *
		 * @param e ActionEvent Object.
		 */
		public void actionPerformed(ActionEvent e) {
			//Update the StringFilter after user made a selection in the TextIndexCombobox
						
			//  Get Current User Selection
			Object o = comboBox.getSelectedItem();

			if ((o != null) && o instanceof Hit) {
				Hit hit = (Hit) comboBox.getSelectedItem();

				// Determine the row index of the TextIndexCombobox in the customSetting panel
				int widgetIndex = getChildIndexFromComponent(comboBox); 
								
				//Update theFilter Object
				List<CyFilter> theFilterlist = theFilter.getChildren();
				
				StringFilter theStringFilter = (StringFilter) theFilterlist.get(widgetIndex);
				theStringFilter.setSearchStr(hit.getKeyword());	
			}
			
			//Update the selection on screen
			doSelection();
		}
	}

	
	/**
	 * Action to select a range of nodes.
	 *
	 * @author Ethan Cerami.
	 */
	class RangeSelectionListener implements ChangeListener {
		private JRangeSliderExtended slider;

		/**
		 * Constructor.
		 *
		 * @param slider JRangeSliderExtended Object.
		 */
		public RangeSelectionListener(JRangeSliderExtended slider) {
			this.slider = slider;
		}

		/**
		 * State Change Event.
		 *
		 * @param e ChangeEvent Object.
		 */
		public void stateChanged(ChangeEvent e) {

			//Update theFilter object if the slider is adjusted
			List<CyFilter> theFilterList = theFilter.getChildren();
			
			try {
				NumberRangeModel model = (NumberRangeModel) slider.getModel();
				NumericFilter theNumericFilter = (NumericFilter) theFilterList.get(getChildIndexFromComponent(slider));

				theNumericFilter.setRange((Number)model.getLowValue(), (Number)model.getHighValue());				
			}
			catch (Exception ex) {
				//NullPointerException caught -- the slider is not initialized yet								
				logger.error("FilterSettingPanel.stateChanged():NullPointerException caught -- the slider is not initialized yet");								
			}	

			theFilter.childChanged();
			//Update the selection on screen
			doSelection();
		}
	}
	
	
	private TextIndex createTextIndex(StringFilter pFilter) {
		final QuickFind quickFind = QuickFindFactory.getGlobalQuickFindInstance();
		CyNetwork cyNetwork = applicationManager.getCurrentNetwork();
		quickFind.reindexNetwork(cyNetwork, pFilter.getIndexType(),
				pFilter.getControllingAttribute(), new TaskMonitorBase());					
		return (TextIndex) quickFind.getIndex(cyNetwork);
	}
	
	
	private NumberIndex createNumberIndex(NumericFilter pNumericFilter) {
		final QuickFind quickFind = QuickFindFactory.getGlobalQuickFindInstance();					
		currentNetwork = applicationManager.getCurrentNetwork();

		int indexType = pNumericFilter.getIndexType();
		quickFind.reindexNetwork(currentNetwork, indexType, 
				pNumericFilter.getControllingAttribute(), new TaskMonitorBase());

		GenericIndex currentIndex = quickFind.getIndex(currentNetwork);
		if (currentIndex == null|| !(currentIndex instanceof NumberIndex)) {
			return null;
		}
		return (NumberIndex) currentIndex;
	}
		
	// Add a label to take up the extra space at the custom setting panel
	private void addBlankLabelToCustomPanel(){
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 99;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        pnlCustomSettings.add(new JLabel(), gridBagConstraints);
	}
	
	private void initAdvancedSetting() {
		chkSession.setSelected(theFilter.getAdvancedSetting().isSessionChecked());
		chkGlobal.setSelected(theFilter.getAdvancedSetting().isGlobalChecked());
		chkNode.setSelected(theFilter.getAdvancedSetting().isNodeChecked());
		chkEdge.setSelected(theFilter.getAdvancedSetting().isEdgeChecked());
		chkNegation.setSelected(theFilter.getNegation());
		
		if (theFilter.getAdvancedSetting().getRelation() == Relation.AND) {
			rbtAND.setSelected(true);
			rbtOR.setSelected(false);			
		}
		else { // Relation.OR
			rbtAND.setSelected(false);
			rbtOR.setSelected(true);						
		}
		
		lbAdvancedIcon.setIcon(plusIcon);
		lbAdvancedIcon.addMouseListener( new MouseAdapter() {
			 //Inner class Mouse listener for click on the plus/minus sign.
            public void mouseClicked(MouseEvent e) {         	
    			Object _actionObject = e.getSource();
    			// click on the plus/minus sign to hide/show advancedPanel 
    			if (_actionObject instanceof JLabel) {
    				JLabel _lbl = (JLabel) _actionObject;
    				
    				if (_lbl == lbAdvancedIcon) {
    					if (pnlAdvancedOptions.isVisible()) {
    						pnlAdvancedOptions.setVisible(false);
    				        lbAdvancedIcon.setIcon(plusIcon);
    					}
    					else {
    						pnlAdvancedOptions.setVisible(true);
    				        lbAdvancedIcon.setIcon(minusIcon);
    					}
    				}
    			}
            }
        });
		
		ItemListener l = new MyItemListener();
		chkSession.addItemListener(l);
		chkGlobal.addItemListener(l);
		
		//The following no longer needed
		//chkNode.addItemListener(l);
		//chkEdge.addItemListener(l);
		
		chkSource.addItemListener(l);
		chkTarget.addItemListener(l);
		rbtAND.addItemListener(l);
		rbtOR.addItemListener(l);
		chkNegation.addItemListener(l);
		//By default, the AdvancedPanel is invisible
		pnlAdvancedOptions.setVisible(false);
		
	}
	
	//To sync the filter object with the setting Panel
	public class MyItemListener implements ItemListener{
		
		public void itemStateChanged(ItemEvent e) {
			Object soureObj= e.getSource();
			if (soureObj instanceof javax.swing.JCheckBox) {
				JCheckBox theCheckBox = (JCheckBox) soureObj;
				
				if (theCheckBox == chkSession) {
					theFilter.getAdvancedSetting().setSession(chkSession.isSelected());	
					// filter name has a prefix "global." or "session.", refresh CMB to update
					parentPanel.refreshFilterSelectCMB();
				}
				else if (theCheckBox == chkGlobal)
				{
					theFilter.getAdvancedSetting().setGlobal(chkGlobal.isSelected());										
					parentPanel.refreshFilterSelectCMB();
				}
				/*
				 * "select Node/Edge" will be determined automatically through selection of attribute in attributeComboBox
				else if (theCheckBox == chkNode)
				{
					theFilter.getAdvancedSetting().setNode(chkNode.isSelected());
					parentPanel.refreshAttributeCMB();						
				}
				else if (theCheckBox == chkEdge)
				{
					theFilter.getAdvancedSetting().setEdge(chkEdge.isSelected());	
					parentPanel.refreshAttributeCMB();						
				}
				*/
				else if (theCheckBox == chkSource)
				{
					theFilter.getAdvancedSetting().setSource(chkSource.isSelected());										
					//parentPanel.refreshAttributeCMB();
				}	
				else if (theCheckBox == chkTarget)
				{
					theFilter.getAdvancedSetting().setTarget(chkTarget.isSelected());										
					//parentPanel.refreshAttributeCMB();
				}					
				else if (theCheckBox == chkNegation)
				{
					theFilter.setNegation(chkNegation.isSelected());										
				}	
				//Update the selection on screen
				if ((theCheckBox == chkNegation)) { //||(theCheckBox == chkEdge)||(theCheckBox == chkNode)) {
					theFilter.childChanged();//The setting has changed
					doSelection();										
				}
			}
			if (soureObj instanceof javax.swing.JRadioButton) {
				JRadioButton theRadioButton = (JRadioButton) soureObj;
				
				if (theRadioButton == rbtAND) {
					theFilter.getAdvancedSetting().setRelation(Relation.AND);	
				}
				if (theRadioButton == rbtOR) {
					theFilter.getAdvancedSetting().setRelation(Relation.OR);	
				}
				updateRelationLabel();

				//Update the selection on screen
				//System.out.println("FilterSettingPanel. rbtAND/rbtOR is clicked");	
				theFilter.childChanged();
				doSelection();				
			}
		}
	}
	
	
	/** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        pnlAdvancedSettings = new javax.swing.JPanel();
        pnlAdvancedIcon = new javax.swing.JPanel();
        lbAdvanced = new javax.swing.JLabel();
        lbAdvancedIcon = new javax.swing.JLabel();
        pnlAdvancedOptions = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        chkSession = new javax.swing.JCheckBox();
        chkGlobal = new javax.swing.JCheckBox();
        lbSelect = new javax.swing.JLabel();
        chkNode = new javax.swing.JCheckBox();
        chkEdge = new javax.swing.JCheckBox();
        
        jLabel8 = new javax.swing.JLabel();
        chkSource = new javax.swing.JCheckBox();
        chkTarget = new javax.swing.JCheckBox();
        
        // hide source/target row
        jLabel8.setVisible(false);
        chkSource.setVisible(false);
        chkTarget.setVisible(false);
        
        lbRelation = new javax.swing.JLabel();
        rbtAND = new javax.swing.JRadioButton();
        rbtOR = new javax.swing.JRadioButton();
        lbNegation = new javax.swing.JLabel();
        chkNegation = new javax.swing.JCheckBox();

        pnlCustomSettings = new javax.swing.JPanel();
        //lbSpaceHolder = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        pnlAdvancedSettings.setLayout(new java.awt.GridBagLayout());

        pnlAdvancedIcon.setLayout(new java.awt.GridBagLayout());

        lbAdvanced.setText(" Advanced ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        pnlAdvancedIcon.add(lbAdvanced, gridBagConstraints);

        lbAdvancedIcon.setIcon(plusIcon);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        pnlAdvancedIcon.add(lbAdvancedIcon, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        pnlAdvancedSettings.add(pnlAdvancedIcon, gridBagConstraints);

        pnlAdvancedOptions.setLayout(new java.awt.GridBagLayout());

        pnlAdvancedOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jLabel6.setText("Save");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlAdvancedOptions.add(jLabel6, gridBagConstraints);

        chkSession.setSelected(true);
        chkSession.setText("Session");
        chkSession.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkSession.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        pnlAdvancedOptions.add(chkSession, gridBagConstraints);

        chkGlobal.setText("Global");
        chkGlobal.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkGlobal.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        pnlAdvancedOptions.add(chkGlobal, gridBagConstraints);

        lbSelect.setText("Select");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlAdvancedOptions.add(lbSelect, gridBagConstraints);

        chkNode.setSelected(true);
        chkNode.setText("Node");
        chkNode.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkNode.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pnlAdvancedOptions.add(chkNode, gridBagConstraints);

        chkEdge.setSelected(true);
        chkEdge.setText("Edge");
        chkEdge.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkEdge.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pnlAdvancedOptions.add(chkEdge, gridBagConstraints);
        
        jLabel8.setText("Edge");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlAdvancedOptions.add(jLabel8, gridBagConstraints);


        chkSource.setSelected(true);
        chkSource.setText("Source");
        chkSource.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkSource.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        pnlAdvancedOptions.add(chkSource, gridBagConstraints);

        chkTarget.setSelected(true);
        chkTarget.setText("Target");
        chkTarget.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkTarget.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        pnlAdvancedOptions.add(chkTarget, gridBagConstraints);
        
        
        lbRelation.setText("Relation");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        pnlAdvancedOptions.add(lbRelation, gridBagConstraints);

        rbtAND.setSelected(true);
        rbtAND.setText("AND");
        rbtAND.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbtAND.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        pnlAdvancedOptions.add(rbtAND, gridBagConstraints);

        rbtOR.setText("OR");
        rbtOR.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbtOR.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        pnlAdvancedOptions.add(rbtOR, gridBagConstraints);

        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        pnlAdvancedSettings.add(pnlAdvancedOptions, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(pnlAdvancedSettings, gridBagConstraints);

        
        lbNegation.setText("Negation");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        pnlAdvancedOptions.add(lbNegation, gridBagConstraints);

        chkNegation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkNegation.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        pnlAdvancedOptions.add(chkNegation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        pnlAdvancedSettings.add(pnlAdvancedOptions, gridBagConstraints);

        pnlCustomSettings.setLayout(new java.awt.GridBagLayout());

        pnlCustomSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 0, 2);
        add(new JScrollPane(pnlCustomSettings), gridBagConstraints);
        //add(pnlCustomSettings, gridBagConstraints);

        btnAdd.setText("Add Widgets");
        jPanel1.add(btnAdd);

        btnClose.setText("Close");
        jPanel1.add(btnClose);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        
        //Unomment the following line for test 
        //add(jPanel1, gridBagConstraints);

        
        buttonGroup1.add(rbtAND);
        buttonGroup1.add(rbtOR);
        
    }// </editor-fold>

	
    // Variables declaration - do not modify

    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClose;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chkEdge;
    private javax.swing.JCheckBox chkGlobal;
    private javax.swing.JCheckBox chkNegation;
    private javax.swing.JCheckBox chkNode;
    private javax.swing.JCheckBox chkSession;
    private javax.swing.JCheckBox chkSource;
    private javax.swing.JCheckBox chkTarget;
    private javax.swing.JLabel lbRelation;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel lbSelect;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lbAdvanced;
    private javax.swing.JLabel lbAdvancedIcon;
    //private javax.swing.JLabel lbSpaceHolder;
    private javax.swing.JPanel pnlAdvancedIcon;
    private javax.swing.JPanel pnlAdvancedOptions;
    private javax.swing.JPanel pnlAdvancedSettings;
    private javax.swing.JPanel pnlCustomSettings;
    private javax.swing.JRadioButton rbtAND;
    private javax.swing.JRadioButton rbtOR;
    private javax.swing.JLabel lbNegation;
    // End of variables declaration
        
}
