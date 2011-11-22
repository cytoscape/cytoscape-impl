/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentRenderingEngineEvent;
import org.cytoscape.application.events.SetCurrentRenderingEngineListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.VisualStyleAboutToBeRemovedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleAboutToBeRemovedListener;
import org.cytoscape.view.vizmap.events.VisualStyleAddedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleAddedListener;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.event.SelectedVisualStyleSwitchedEvent;
import org.cytoscape.view.vizmap.gui.event.SelectedVisualStyleSwitchedListener;
import org.cytoscape.view.vizmap.gui.internal.task.ImportDefaultVizmapTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.theme.ColorManager;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;
import com.l2fprod.common.swing.plaf.blue.BlueishButtonUI;

/**
 * New VizMapper UI main panel. Refactored for Cytoscape 3.
 * 
 * This panel consists of 3 panels:
 * <ul>
 * <li>Global Control Panel
 * <li>Default editor panel
 * <li>Visual Mapping Browser
 * </ul>
 * 
 */
public class VizMapperMainPanel extends AbstractVizMapperPanel implements
		VisualStyleAddedListener, VisualStyleAboutToBeRemovedListener,
		PopupMenuListener, NetworkViewAddedListener, NetworkAddedListener,
		CytoPanelComponent, SelectedVisualStyleSwitchedListener, SetCurrentRenderingEngineListener {

	private final static long serialVersionUID = 1202339867854959L;

	private static final Logger logger = LoggerFactory.getLogger(VizMapperMainPanel.class);

	// Title for the tab.
	private static final String TAB_TITLE = "VizMapper\u2122";

	private final DefaultViewMouseListener defaultViewMouseListener;

	/**
	 * Create new instance of VizMapperMainPanel object. GUI layout is handled
	 * by abstract class.
	 * 
	 * @param dab
	 * @param iconMgr
	 * @param colorMgr
	 * @param vmm
	 * @param menuMgr
	 * @param editorFactory
	 */
	public VizMapperMainPanel(final VisualStyleFactory vsFactory,
			DefaultViewEditor defViewEditor, 
			IconManager iconMgr,
			ColorManager colorMgr, 
			VisualMappingManager vmm,
			VizMapperMenuManager menuMgr, 
			EditorManager editorFactory,
			final PropertySheetPanel propertySheetPanel,
			VizMapPropertySheetBuilder vizMapPropertySheetBuilder,
			EditorWindowManager editorWindowManager,
			CyApplicationManager applicationManager, 
			CyEventHelper eventHelper,
			final SelectedVisualStyleManager manager,
			final ImportDefaultVizmapTaskFactory taskFactory,
			final TaskManager<?, ?> tManager) {

		super(vsFactory, defViewEditor, iconMgr, colorMgr, vmm, menuMgr,
				editorFactory, propertySheetPanel, vizMapPropertySheetBuilder,
				editorWindowManager, applicationManager, eventHelper, manager);

		// Initialize all components
		this.defaultViewMouseListener = new DefaultViewMouseListener(defViewEditor, this, manager);
		
		initPanel();
		
		// Load default styles
		tManager.execute(taskFactory);
	}

	private void initPanel() {
		addVisualStyleChangeAction();

		// By default, force to sort property by prop name.
		propertySheetPanel.setSorting(true);
		refreshUI();
	}
	
	
	private void addVisualStyleChangeAction() {
		visualStyleComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				switchSelected();
			}
		});
	}
	
	private void switchSelected() {
		final VisualStyle lastStyle = manager.getCurrentVisualStyle();
		final VisualStyle style = (VisualStyle) visualStyleComboBox
				.getSelectedItem();
		if (style.equals(lastStyle))
			return;

		switchVS(style);
		eventHelper.fireEvent(new SelectedVisualStyleSwitchedEvent(this, lastStyle, style));
		logger.debug("######## Event:  new selected style: " + style);
	}

	private void switchVS(final VisualStyle style) {
		switchVS(style, false);
	}

	protected void switchVS(final VisualStyle style, boolean forceUpdate) {
		// Close editor windows
		editorWindowManager.closeAllEditorWindows();

		vizMapPropertySheetBuilder.setPropertyTable(style);
		updateAttributeList();

		// Apply style to the current network view.
		final CyNetworkView currentView = applicationManager.getCurrentNetworkView();

		if (currentView != null && style.equals(manager.getCurrentVisualStyle()) == false) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					vmm.setVisualStyle((VisualStyle) visualStyleComboBox.getModel().getSelectedItem(), currentView);
					style.apply(currentView);
					// Update view
					currentView.updateView();
				}
			});
		}

		final Dimension newSize = new Dimension(mainSplitPane.getWidth(), mainSplitPane.getDividerLocation());
		// Default image is not available in the buffer. Create a new one.
		updateDefaultImage(style, ((DefaultViewPanel) defViewEditor.getDefaultView(style)).getRenderingEngine(), newSize);
		final Image defImg = defaultImageManager.get(style);
		// Set the default view to the panel.
		setDefaultViewImagePanel(defImg, style);

		// Set the default view to the panel.
		propertySheetPanel.setSorting(true);
	}

	void refreshUI() {
		final List<VisualStyle> visualStyles = new ArrayList<VisualStyle>(
				vmm.getAllVisualStyles());

		// Disable action listeners
		final ActionListener[] li = visualStyleComboBox.getActionListeners();

		for (int i = 0; i < li.length; i++)
			visualStyleComboBox.removeActionListener(li[i]);

		visualStyleComboBox.removeAllItems();

		Component defPanel;

		final Dimension panelSize = defaultViewImagePanel.getSize();

		// TODO: make sortable!
		// Collections.sort(visualStyles);

		for (VisualStyle vs : visualStyles) {
			logger.info("Adding VS: " + vs.getTitle());
			vsComboBoxModel.addElement(vs);
			defPanel = defViewEditor.getDefaultView(vs);
			final RenderingEngine<CyNetwork> engine = ((DefaultViewPanel) defPanel).getRenderingEngine();
			updateDefaultImage(vs, engine, panelSize);
		}

		// Switch back to the original style.
		switchVS(manager.getDefaultStyle());

		// Sync check box and actual lock state
		spcs.firePropertyChange("UPDATE_LOCK", null, true);

		// Restore listeners
		for (int i = 0; i < li.length; i++)
			visualStyleComboBox.addActionListener(li[i]);
	}

	
	void updateDefaultImage(final VisualStyle vs, final RenderingEngine<CyNetwork> engine, final Dimension size) {

		logger.debug("Creating Default Image for new visual style " + vs.getTitle());
		Image image = defaultImageManager.remove(vs);

		if (image != null) {
			image.flush();
			image = null;
		}

		defaultImageManager.put(vs, engine.createImage((int) size.getWidth(), (int) size.getHeight()));
	}

	public void updateAttributeList() {
		vizMapPropertySheetBuilder.setAttrComboBox();
	}

	/**
	 * 
	 * @param defImage
	 */
	void setDefaultViewImagePanel(final Image defImage, final VisualStyle newStyle) {
		if (defImage == null) {
			logger.debug("Default image is null!");
			return;
		}
		
		defaultViewImagePanel.removeAll();

		final JButton defaultImageButton = new JButton();
		defaultImageButton.setUI(new BlueishButtonUI());
		defaultImageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		defaultImageButton.setIcon(new ImageIcon(defImage));
		defaultImageButton.setBackground((Color) newStyle
				.getDefaultValue(MinimalVisualLexicon.NETWORK_BACKGROUND_PAINT));

		defaultViewImagePanel.add(defaultImageButton, BorderLayout.CENTER);
		defaultImageButton.addMouseListener(defaultViewMouseListener);
		
		defaultViewImagePanel.revalidate();
	}


	@Override
	public void popupMenuCanceled(PopupMenuEvent arg0) {
		// TODO: replace this to firePropertyChange
		// disableAllPopup();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}

	/**
	 * Check the selected VPT and enable/disable menu items.
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// disableAllPopup();

		final int selected = propertySheetPanel.getTable().getSelectedRow();

		if (0 > selected) {
			return;
		}

		final Item item = (Item) propertySheetPanel.getTable().getValueAt(
				selected, 0);
		final Property curProp = item.getProperty();

		if (curProp == null)
			return;

		VizMapperProperty prop = ((VizMapperProperty) curProp);

		// FIXME
		// if (prop.getHiddenObject() instanceof VisualProperty
		// && (prop.getDisplayName().contains("Mapping Type") == false)
		// && (prop.getValue() != null)
		// && (prop.getValue().toString().startsWith("Please select") == false))
		// {
		// // Enble delete menu
		// // delete.setEnabled(true);
		// Property[] children = prop.getSubProperties();
		//
		// for (Property p : children) {
		// if ((p.getDisplayName() != null)
		// && p.getDisplayName().contains("Mapping Type")) {
		// if ((p.getValue() == null)
		// || (p.getValue().equals("Discrete Mapping") == false)) {
		// return;
		// }
		// }
		// }
		//
		// VisualProperty type = ((VisualProperty) prop.getHiddenObject());
		//
		// Class dataType = type.getType();
		//
		// // if (dataType == Color.class) {
		// // rainbow1.setEnabled(true);
		// // rainbow2.setEnabled(true);
		// // randomize.setEnabled(true);
		// // brighter.setEnabled(true);
		// // darker.setEnabled(true);
		// // } else if (dataType == Number.class) {
		// // randomize.setEnabled(true);
		// // series.setEnabled(true);
		// // }
		// //
		// // if ((type == VisualProperty.NODE_WIDTH)
		// // || (type == VisualProperty.NODE_HEIGHT)) {
		// // fit.setEnabled(true);
		// // }
		// }

		return;
	}


	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Object getSelectedItem() {
		final JTable table = propertySheetPanel.getTable();

		return table.getModel().getValueAt(table.getSelectedRow(), 0);
	}

	public DefaultViewEditor getDefaultViewEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	public Component getVisualMappingBrowser() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDefaultViewEditor(DefaultViewEditor defViewEditor) {
		// TODO Auto-generated method stub

	}

	public void setDefaultViewPanel(JPanel defViewPanel) {
		// TODO Auto-generated method stub

	}

	public void setVisualMappingBrowser(Component visualMappingBrowser) {
		// TODO Auto-generated method stub

	}

	/**
	 * Update GUI components when new Visual Style is created.
	 */
	@Override
	public void handleEvent(final VisualStyleAddedEvent e) {
		final VisualStyle newStyle = e.getVisualStyleAdded();
		
		if(newStyle == null) {
			logger.warn("New Visual Style is null.");
			return;
		}
		
		// Style already exists
		if(vsComboBoxModel.getIndexOf(newStyle) != -1) {
			logger.info(newStyle.getTitle() + " is already in the combobox.");
			switchVS(newStyle, true);
			return;
		}

		vsComboBoxModel.addElement(newStyle);
		final CyNetworkView currentView = applicationManager.getCurrentNetworkView();

		if (currentView != null)
			vmm.setVisualStyle(newStyle, currentView);

		// Update default panel
		final Component defPanel = defViewEditor.getDefaultView(newStyle);
		final RenderingEngine<CyNetwork> engine = ((DefaultViewPanelImpl) defPanel).getRenderingEngine();
		final Dimension panelSize = defaultViewImagePanel.getSize();

		if (engine != null)
			updateDefaultImage(newStyle, engine, panelSize);
		
		logger.info("New Visual Style registered to combo box: " + newStyle.getTitle());
		// TODO: switch only if it is necessary
		switchVS(newStyle, true);
	}

	@Override
	public void handleEvent(NetworkViewAddedEvent e) {

		final CyNetworkView newView = e.getNetworkView();
		final VisualStyle targetStyle = manager.getCurrentVisualStyle();

		logger.debug("@@@@@@ Network View added. Apply " + targetStyle);

		vmm.setVisualStyle(targetStyle, newView);
		targetStyle.apply(newView);
		newView.updateView();
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		// TODO: is this necessary?
		logger.debug("!!!!!!!!!! Network added. Need to update prop sheet: "
				+ e.getNetwork().getSUID());

	}

	/**
	 * Update panel when removed
	 */
	@Override
	public void handleEvent(VisualStyleAboutToBeRemovedEvent e) {

		final VisualStyle toBeRemoved = e.getVisualStyleToBeRemoved();

		// Update image
		getDefaultImageManager().remove(e.getVisualStyleToBeRemoved());
		vizMapPropertySheetBuilder.removePropertyList(e
				.getVisualStyleToBeRemoved());
		this.visualStyleComboBox.removeItem(toBeRemoved);

		// Switch to the default style
		final VisualStyle defaultStyle = manager.getDefaultStyle();

		switchVS(defaultStyle);
		// Apply to the current view
		final CyNetworkView view = applicationManager.getCurrentNetworkView();
		if (view != null)
			vmm.setVisualStyle(defaultStyle, view);
		eventHelper.fireEvent(new SelectedVisualStyleSwitchedEvent(
				this, toBeRemoved, defaultStyle));
		logger.debug("######## Event:  removed style: " + toBeRemoved);
	}

	@Override
	public String getTitle() {
		return TAB_TITLE;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public void handleEvent(SelectedVisualStyleSwitchedEvent e) {
		final VisualStyle newStyle = e.getNewVisualStyle();
		this.visualStyleComboBox.setSelectedItem(newStyle);
		
		switchVS(newStyle, true);
		
//		final Dimension newSize = new Dimension(mainSplitPane.getWidth(), mainSplitPane.getDividerLocation());
//		// Default image is not available in the buffer. Create a new one.
//		updateDefaultImage(newStyle, ((DefaultViewPanel) defViewEditor.getDefaultView(newStyle)).getRenderingEngine(), newSize);
//		final Image defImg = defaultImageManager.get(newStyle);
//		// Set the default view to the panel.
//		setDefaultViewImagePanel(defImg, newStyle);
	}

	
	@Override
	public void handleEvent(SetCurrentRenderingEngineEvent e) {
		final RenderingEngine<CyNetwork> engine = e.getRenderingEngine();
		final CyNetworkView view = (CyNetworkView) engine.getViewModel();
		final VisualStyle newStyle = vmm.getVisualStyle(view);
		
		this.visualStyleComboBox.setSelectedItem(newStyle);
		visualStyleComboBox.repaint();

		logger.info("$$$$$$$$$$$$ Updating VS Combo Box to: " + newStyle.getTitle());
	}
}
