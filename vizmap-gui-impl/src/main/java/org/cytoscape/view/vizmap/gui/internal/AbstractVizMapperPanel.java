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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableCellRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.VizMapGUI;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.AttributeComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.theme.ColorManager;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;

import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.swing.plaf.blue.BlueishButtonUI;

/**
 * Skeleton of the VizMapper Main panel GUI.
 * 
 * This class includes methods which sets up GUI components of VizMapper. Actual
 * functions are in the VizMapperMainPanel.
 * 
 */
public abstract class AbstractVizMapperPanel extends JPanel implements VizMapGUI {
	
	// Visual Properties which are not used in mapping now.
	public static final String CATEGORY_UNUSED = "Unused Properties";

	public static final String GRAPHICAL_MAP_VIEW = "Graphical View";

	protected final SelectedVisualStyleManager manager;

	// ///////// Main GUI Components /////////////////

	// Current Visual Style is managed by this object.
	protected JComboBox visualStyleComboBox;
	protected DefaultComboBoxModel vsComboBoxModel;

	// Default View Editor. This is a singleton.
	protected DefaultViewEditor defViewEditor;

	// Property Sheet for Mapping
	protected PropertySheetPanel propertySheetPanel;

	/*
	 * Resources which will be injected through DI Container
	 */

	// Listeners for attribute-related events
	protected AttributeEventsListener nodeAttrListener;
	protected AttributeEventsListener edgeAttrListener;
	protected AttributeEventsListener networkAttrListener;

	protected CyEventHelper eventHelper;

	protected VisualMappingManager vmm;

	protected ColorManager colorMgr;
	protected IconManager iconMgr;
	protected VizMapperMenuManager menuMgr;
	protected EditorManager editorManager;
	protected VizMapperUtil vizMapperUtil;

	/*
	 * Combo Box Editors
	 */
	protected PropertyEditor nodeAttrEditor;
	protected PropertyEditor edgeAttrEditor;
	protected PropertyEditor nodeNumericalAttrEditor;
	protected PropertyEditor edgeNumericalAttrEditor;
	protected PropertyEditor mappingTypeEditor;

	protected VizMapPropertySheetBuilder vizMapPropertySheetBuilder;

	protected Map<VisualStyle, Image> defaultImageManager;

	protected DefaultTableCellRenderer emptyBoxRenderer;
	protected DefaultTableCellRenderer filledBoxRenderer;

	protected PropertyRendererRegistry rendReg;
	protected PropertyEditorRegistry editorReg;

	// protected VizMapEventHandlerManager vizMapEventHandlerManager;

	protected EditorWindowManager editorWindowManager;

	protected CyApplicationManager applicationManager;

	protected AttributeComboBoxPropertyEditor nodeAttributeEditor;
	protected AttributeComboBoxPropertyEditor edgeAttributeEditor;
	protected AttributeComboBoxPropertyEditor networkAttributeEditor;

	protected SwingPropertyChangeSupport spcs;

	protected final VisualStyleFactory vsFactory;
	
	private JToggleButton showAllVPButton;
	private final SetViewModeAction viewModeAction;

	protected static final long serialVersionUID = -6839011300709287662L;

	public AbstractVizMapperPanel(final VisualStyleFactory vsFactory,
			DefaultViewEditor defViewEditor,
			IconManager iconMgr, ColorManager colorMgr,
			VisualMappingManager vmm, VizMapperMenuManager menuMgr,
			EditorManager editorFactory, PropertySheetPanel propertySheetPanel,
			VizMapPropertySheetBuilder vizMapPropertySheetBuilder,
			EditorWindowManager editorWindowManager,
			CyApplicationManager applicationManager, CyEventHelper eventHelper,
			final SelectedVisualStyleManager manager, final SetViewModeAction viewModeAction) {
		
		if(menuMgr == null)
			throw new NullPointerException("Menu manager is missing.");
		
		this.manager = manager;
		this.vsFactory = vsFactory;
		this.defViewEditor = defViewEditor;
		this.iconMgr = iconMgr;
		this.colorMgr = colorMgr;
		this.vmm = vmm;
		this.menuMgr = menuMgr;
		this.editorManager = editorFactory;
		this.propertySheetPanel = propertySheetPanel;
		this.vizMapPropertySheetBuilder = vizMapPropertySheetBuilder;
		this.editorWindowManager = editorWindowManager;
		this.applicationManager = applicationManager;
		this.eventHelper = eventHelper;
		this.viewModeAction = viewModeAction;

		editorReg = new PropertyEditorRegistry();
		rendReg = new PropertyRendererRegistry();
		this.propertySheetPanel.getTable().setEditorFactory(editorReg);
		this.propertySheetPanel.getTable().setRendererFactory(rendReg);


		spcs = new SwingPropertyChangeSupport(this);

		defaultImageManager = new HashMap<VisualStyle, Image>();

		initComponents();
		initDefaultEditors();
		
		// This is a hack: Add extra button to the UI.
		addButtonToPropertySheetPanel();
	}
	
	private void addButtonToPropertySheetPanel() {
		showAllVPButton = new JToggleButton();
		showAllVPButton.setText("Show All");
		// This is a hack: get private component and add button.
		Field[] fields = PropertySheetPanel.class.getDeclaredFields();
		showAllVPButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent ae) {
				viewModeAction.menuSelected(null);
				viewModeAction.actionPerformed(null);
			}
		});
		
		showAllVPButton.setUI(new BlueishButtonUI());
		showAllVPButton.setToolTipText("Show all Visual Properties");
		
		for (Field f : fields) {
			if (f.getName().equals("actionPanel")) {
				f.setAccessible(true);
				try {
					JPanel buttonPanel = (JPanel) f.get(propertySheetPanel);
					buttonPanel.add(showAllVPButton);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void initDefaultEditors() {
		nodeNumericalAttrEditor = editorManager
				.getDefaultComboBoxEditor("nodeNumericalAttrEditor");
		edgeNumericalAttrEditor = editorManager
				.getDefaultComboBoxEditor("edgeNumericalAttrEditor");
		mappingTypeEditor = editorManager
				.getDefaultComboBoxEditor("mappingTypeEditor");

	}
	
	private void initializeVisualStyleComboBox() {
		vsComboBoxModel = new DefaultComboBoxModel();
		final VisualStyle defaultVS = manager.getDefaultStyle();
		final Set<VisualStyle> styles = vmm.getAllVisualStyles();
		
		for(VisualStyle style: styles)
			vsComboBoxModel.addElement(style);
		
		visualStyleComboBox = new JComboBox(vsComboBoxModel);
		visualStyleComboBox.setSelectedItem(defaultVS);
	}

	private void initComponents() {
		mainSplitPane = new javax.swing.JSplitPane();
		mainSplitPane.setBorder(BorderFactory.createEmptyBorder());
		listSplitPane = new javax.swing.JSplitPane();
		listSplitPane.setBorder(BorderFactory.createEmptyBorder());

		bottomPanel = new javax.swing.JPanel();

		defaultViewImagePanel = new javax.swing.JPanel();
		propertySheetPanel.setTable(new VizMapPropertySheetTable());

		vsSelectPanel = new javax.swing.JPanel();

		buttonPanel = new javax.swing.JPanel();

		initializeVisualStyleComboBox();

		optionButton = new DropDownMenuButton(new AbstractAction() {
			private final static long serialVersionUID = 1213748836776579L;

			public void actionPerformed(ActionEvent ae) {
				DropDownMenuButton b = (DropDownMenuButton) ae.getSource();
				menuMgr.getMainMenu().show(b, 0, b.getHeight());
			}
		});

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		buttonPanel.setLayout(gridbag);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = GridBagConstraints.REMAINDER;

		addButton = new javax.swing.JButton();

		addButton.setUI(new BlueishButtonUI());

		gridbag.setConstraints(addButton, constraints);
		buttonPanel.add(addButton);

		constraints.gridx = 2;
		constraints.gridy = 0;

		mainSplitPane.setDividerLocation(160);
		mainSplitPane.setDividerSize(4);
		// TODO why do we have to do this?
		mainSplitPane.setSize(new Dimension(100, 160));

		listSplitPane.setDividerLocation(400);
		listSplitPane.setDividerSize(5);
		listSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		
		// Default View Panel
		//defaultViewImagePanel.setMinimumSize(new Dimension(200, 200));
		defaultViewImagePanel.setPreferredSize(new Dimension(mainSplitPane.getWidth(), mainSplitPane
				.getDividerLocation()));
		defaultViewImagePanel.setSize(defaultViewImagePanel.getPreferredSize());
		defaultViewImagePanel.setLayout(new BorderLayout());

		noMapListScrollPane = new javax.swing.JScrollPane();
		noMapListScrollPane.setBorder(javax.swing.BorderFactory
				.createTitledBorder(null, "Unused Visual Properties",
						javax.swing.border.TitledBorder.CENTER,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						new java.awt.Font("SansSerif", 1, 12)));
		noMapListScrollPane
				.setToolTipText("To Create New Mapping, Drag & Drop List Item to Browser.");

		GroupLayout bottomPanelLayout = new GroupLayout(bottomPanel);
		bottomPanel.setLayout(bottomPanelLayout);
		bottomPanelLayout.setHorizontalGroup(bottomPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(noMapListScrollPane, GroupLayout.DEFAULT_SIZE,
						272, Short.MAX_VALUE)
				.addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		bottomPanelLayout.setVerticalGroup(bottomPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						bottomPanelLayout
								.createSequentialGroup()
								.addComponent(buttonPanel,
										GroupLayout.PREFERRED_SIZE, 25,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(noMapListScrollPane,
										GroupLayout.DEFAULT_SIZE, 135,
										Short.MAX_VALUE)));

		listSplitPane.setLeftComponent(mainSplitPane);
		listSplitPane.setRightComponent(bottomPanel);

		mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		defaultViewImagePanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder(null, "Defaults",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						new java.awt.Font("SansSerif", 1, 12),
						java.awt.Color.darkGray));

		mainSplitPane.setLeftComponent(defaultViewImagePanel);

		propertySheetPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder(null, "Visual Mapping Browser",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						new java.awt.Font("SansSerif", 1, 12),
						java.awt.Color.darkGray));

		mainSplitPane.setRightComponent(propertySheetPanel);

		vsSelectPanel
				.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
						"Current Visual Style",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						new java.awt.Font("SansSerif", 1, 12),
						java.awt.Color.darkGray));

		optionButton.setToolTipText("Options...");
		optionButton.setIcon(iconMgr.getIcon("optionIcon"));
		optionButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		optionButton.setComponentPopupMenu(menuMgr.getMainMenu());

		GroupLayout vsSelectPanelLayout = new GroupLayout(vsSelectPanel);
		vsSelectPanel.setLayout(vsSelectPanelLayout);
		vsSelectPanelLayout.setHorizontalGroup(vsSelectPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						vsSelectPanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(visualStyleComboBox, 0, 146,
										Short.MAX_VALUE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(optionButton,
										GroupLayout.PREFERRED_SIZE, 64,
										GroupLayout.PREFERRED_SIZE)
								.addContainerGap()));
		vsSelectPanelLayout.setVerticalGroup(vsSelectPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						vsSelectPanelLayout.createSequentialGroup().addGroup(
								vsSelectPanelLayout
										.createParallelGroup(
												GroupLayout.Alignment.BASELINE)
										.addComponent(visualStyleComboBox,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(optionButton)) // .addContainerGap(
				// GroupLayout.DEFAULT_SIZE,
				// Short.MAX_VALUE)
				));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(vsSelectPanel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(mainSplitPane, GroupLayout.DEFAULT_SIZE, 280,
						Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(vsSelectPanel,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(mainSplitPane,
										GroupLayout.DEFAULT_SIZE, 510,
										Short.MAX_VALUE)));
	} // </editor-fold>

	// Variables declaration - do not modify
	protected JPanel defaultViewImagePanel;
	protected JSplitPane mainSplitPane;
	protected JSplitPane listSplitPane;
	protected DropDownMenuButton optionButton;
	protected JPanel vsSelectPanel;
	protected JScrollPane noMapListScrollPane;
	protected JPanel buttonPanel;
	protected JButton addButton;
	protected JPanel bottomPanel;

	
	public PropertyChangeSupport getPropertyChangeSupport() {
		return this.spcs;
	}

	public Map<VisualStyle, Image> getDefaultImageManager() {
		return this.defaultImageManager;
	}

	public JPanel getDefaultViewPanel() {
		return this.defaultViewImagePanel;
	}
}
