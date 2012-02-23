/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.view.vizmap.gui.internal;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditor;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.event.SelectedVisualStyleSwitchedEvent;
import org.cytoscape.view.vizmap.gui.event.SelectedVisualStyleSwitchedListener;
import org.cytoscape.view.vizmap.gui.internal.event.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

/**
 * Creates a new Mapping from GUI
 */
public final class VizMapPropertySheetMouseAdapter extends MouseAdapter
		implements SelectedVisualStyleSwitchedListener {
	
	private static final Logger logger = LoggerFactory.getLogger(VizMapPropertySheetMouseAdapter.class);
	
	private VizMapPropertySheetBuilder vizMapPropertySheetBuilder;
	private PropertySheetPanel propertySheetPanel;
	private EditorManager editorManager;
	
	private final VizMapperMenuManager menuManager;

	private VisualStyle selectedStyle;
	
	
	private final PropertyEditor nodeAttributeEditor;
	private final PropertyEditor edgeAttributeEditor;
	private final PropertyEditor networkAttributeEditor;
	
	private VisualMappingFunction<?, ?> currentMapping;
	

	/**
	 * Creates a new VizMapPropertySheetMouseAdapter object.
	 * 
	 * @param sheetBuilder
	 *            DOCUMENT ME!
	 * @param propertySheetPanel
	 *            DOCUMENT ME!
	 * @param editorWindowManager
	 *            DOCUMENT ME!
	 */
	public VizMapPropertySheetMouseAdapter(final VizMapperMenuManager menuManager,
			VizMapPropertySheetBuilder sheetBuilder,
			PropertySheetPanel propertySheetPanel, VisualStyle selectedStyle, EditorManager editorManager) {
		
		if(menuManager == null)
			throw new NullPointerException("VizMapperMenuManager is null.");

		this.vizMapPropertySheetBuilder = sheetBuilder;
		this.propertySheetPanel = propertySheetPanel;
		this.selectedStyle = selectedStyle;
		this.editorManager = editorManager;
		this.menuManager = menuManager;
		
		this.nodeAttributeEditor = editorManager.getDataTableComboBoxEditor(CyNode.class);
		this.edgeAttributeEditor = editorManager.getDataTableComboBoxEditor(CyEdge.class);
		this.networkAttributeEditor = editorManager.getDataTableComboBoxEditor(CyNetwork.class);
		
	}


	@Override public void mouseClicked(MouseEvent e) {
		
		int selected = propertySheetPanel.getTable().getSelectedRow();
		/*
		 * Adjust height if it's an legend icon.
		 */
		vizMapPropertySheetBuilder.updateTableView();
		
		if(SwingUtilities.isRightMouseButton(e)) {
			this.handleContextMenuEvent(e);
		} else if (SwingUtilities.isLeftMouseButton(e) && (0 <= selected)) {
			final Item item = (Item) propertySheetPanel.getTable().getValueAt(selected, 0);
			final VizMapperProperty<?, ?, ?> curProp = (VizMapperProperty<?, ?, ?>) item.getProperty();

			if (curProp == null)
				return;
			
			logger.debug("Got prop: " + curProp.getDisplayName());

			final CellType cellType = curProp.getCellType();
			if ((e.getClickCount() == 2) && cellType.equals(CellType.UNUSED)) {
				
				// Create new mapping from unused Visual Property.
				curProp.setEditable(true);

				final VisualProperty<?> vp = (VisualProperty<?>) curProp.getKey();
				propertySheetPanel.removeProperty(curProp);
				
				logger.debug("VP removed: " + vp.getDisplayName());

				final VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory> newProp 
					= new VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory>(CellType.VISUAL_PROPERTY_TYPE, vp, String.class);
				final VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, ?>> mapProp 
					= new VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, ?>>(CellType.MAPPING_TYPE, "Mapping Type", VisualMappingFunctionFactory.class);

				newProp.setDisplayName(vp.getDisplayName());
				newProp.setValue("Please select a value!");

				if (vp.getTargetDataType().equals(CyNode.class)) {
					newProp.setCategory(BasicVisualLexicon.NODE.getDisplayName());
					((PropertyEditorRegistry) propertySheetPanel.getTable().getEditorFactory()).registerEditor(newProp, nodeAttributeEditor);
					
					logger.debug("This is node prop: " + vp.getDisplayName());
				} else if (vp.getTargetDataType().equals(CyEdge.class)){
					newProp.setCategory(BasicVisualLexicon.EDGE.getDisplayName());
					((PropertyEditorRegistry) propertySheetPanel.getTable().getEditorFactory()).registerEditor(newProp, edgeAttributeEditor);
					logger.debug("This is edge prop: " + vp.getDisplayName());
				} else {
					// Network prop
					logger.debug("This is network prop: " + vp.getDisplayName());
				}

				mapProp.setDisplayName("Mapping Type");				
				
				((PropertyEditorRegistry) propertySheetPanel.getTable().getEditorFactory()).registerEditor(mapProp, editorManager.getMappingFunctionSelector());
				
				newProp.addSubProperty(mapProp);
				mapProp.setParentProperty(newProp);
				propertySheetPanel.addProperty(0, newProp);

				vizMapPropertySheetBuilder.expandLastSelectedItem(vp
						.getDisplayName());

				propertySheetPanel.getTable().scrollRectToVisible(new Rectangle(0, 0, 10, 10));
				
				propertySheetPanel.repaint();

				return;
				
			} else if ((e.getClickCount() == 1) && (cellType.equals(CellType.DISCRETE))) {
				/*
				 * Single left-click
				 */
				final VisualMappingFunction<?, ?> mapping = (VisualMappingFunction<?, ?>) curProp.getInternalValue();
				if(mapping == null)
					return;

				final VisualMappingFunction<?, ?> selectedMapping = selectedStyle
						.getVisualMappingFunction(mapping.getVisualProperty());
				logger.debug("==========Target Mapping = " + selectedMapping);
				logger.debug("==========Target Key = " + curProp.getDisplayName());
				logger.debug("==========Target Val = " + curProp.getValue());
			}
		}
	}
	
	private void handleContextMenuEvent(MouseEvent e) {
		final JPopupMenu contextMenu = menuManager.getContextMenu();
		final Component parent = (Component) e.getSource();
		contextMenu.show(parent, e.getX(), e.getY());
	}

	public void handleEvent(SelectedVisualStyleSwitchedEvent e) {
		this.selectedStyle = e.getNewVisualStyle();
	}
}
