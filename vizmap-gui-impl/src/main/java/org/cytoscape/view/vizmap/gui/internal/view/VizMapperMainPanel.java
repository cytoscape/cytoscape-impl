package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.VizMapGUI;
import org.cytoscape.view.vizmap.gui.internal.ColumnSpec;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VisualStylePanelProvider.VisualStyleDropDownButton;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * VizMapper UI main panel.
 */
@SuppressWarnings("serial")
public class VizMapperMainPanel extends JPanel implements VizMapGUI, DefaultViewPanel, DefaultViewEditor,
														  CytoPanelComponent2, VisualPropertySheetContainer {

	private static final String TITLE = "Style";
	private static final String ID = "org.cytoscape.Style";
	
	private TextIcon icon;
	private final ServicesUtil servicesUtil;
	
	private VisualStylePanelProvider visualStylePanel;
	private ColumnStylePicker columnStylePanel;
	private PropertySheetPanel propertySheetPanel;
	
	public VizMapperMainPanel(ServicesUtil servicesUtil) {
		this.servicesUtil = Objects.requireNonNull(servicesUtil, "'servicesUtil' must not be null");
		init();
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
	
	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public Icon getIcon() {
		if (icon == null)
			icon = new TextIcon(IconManager.ICON_PAINT_BRUSH,
					servicesUtil.get(IconManager.class).getIconFont(14.0f), 16, 16);
		
		return icon;
	}

	/**
	 * Dummy panel that is used to prevent NullPointerExceptions for clients of the
	 * deprecated method {@link #getDefaultViewPanel()}.
	 */
	private JPanel defViewPanel = new JPanel();
	
	@Override
	@Deprecated
	public JPanel getDefaultViewPanel() {
		return defViewPanel;
	}
	
	@Override
	@Deprecated
	public DefaultViewEditor getDefaultViewEditor() {
		return this;
	}
	
	@Override
	@Deprecated
	public RenderingEngine<CyNetwork> getRenderingEngine() {
		return getStylesBtn().getRenderingEngine(getSelectedVisualStyle());
	}
	
	VisualStylePanelProvider getStylesPanelProvider() {
		if (visualStylePanel == null) {
			visualStylePanel = new VisualStylePanelProvider(servicesUtil);
		}
		
		return visualStylePanel;
	}

	ColumnStylePicker getColumnStylePnl() {
		if (columnStylePanel == null) {
			columnStylePanel = new ColumnStylePicker(servicesUtil);
		}
		
		return columnStylePanel;
	}

	PropertySheetPanel getPropertiesPnl() {
		if (propertySheetPanel == null) {
			propertySheetPanel = new PropertySheetPanel(servicesUtil);
		}
		
		return propertySheetPanel;
	}

	/**
	 * @return The correspondent JPanel which was used to create the rendering engine that then generates
	 * the preview image of the visual style in the Current Style selector.
	 * This JPanel is never displayed in the UI, though.
	 */
	@Override
	@Deprecated
	public Component getDefaultView(VisualStyle vs) {
		return getStylesPanelProvider().getDefaultView(vs);
	}

	@Override
	@Deprecated
	public void showEditor(Component parent) {
		// Doesn't do anything anymore, since it has been deprecated.
	}
	
	VisualStyleDropDownButton getStylesBtn() {
		return getStylesPanelProvider().getStylesBtn();
	}
	
	
	public VisualStyle getSelectedVisualStyle() {
		return getStylesPanelProvider().getSelectedVisualStyle();
	}
	
	public void setSelectedVisualStyle(VisualStyle style) {
		getStylesPanelProvider().setSelectedVisualStyle(style);
	}
	
	public void removeContextMenuItem(JMenuItem menuItem) {
		getPropertiesPnl().removeContextMenuItem(menuItem);
	}
	
	public void updateVisualStyles(SortedSet<VisualStyle> styles, VisualStyle selectedStyle) {
		getStylesPanelProvider().updateVisualStyles(styles, selectedStyle);
	}
	
	@Override
	public VisualPropertySheet getVisualPropertySheet(Class<? extends CyIdentifiable> targetDataType) {
		return getPropertiesPnl().getVisualPropertySheet(targetDataType);
	}
	
	@Override
	public VisualPropertySheet getSelectedVisualPropertySheet() {
		return getPropertiesPnl().getSelectedVisualPropertySheet();
	}
	
	@Override
	public JPopupMenu getContextMenu() {
		return getPropertiesPnl().getContextMenu();
	}
	
	@Override
	public Set<VisualPropertySheet> getVisualPropertySheets() {
		return getPropertiesPnl().getVisualPropertySheets();
	}
	
	@Override
	public void addVisualPropertySheet(VisualPropertySheet sheet) {
		getPropertiesPnl().addVisualPropertySheet(sheet);
	}
	
	@Override
	public void setSelectedVisualPropertySheet(VisualPropertySheet sheet) {
		getPropertiesPnl().setSelectedVisualPropertySheet(sheet);
	}
			
	@Override
	public JMenu getMapValueGeneratorsSubMenu() {
		return getPropertiesPnl().getMapValueGeneratorsSubMenu();
	}
	
	public void addOption(JMenuItem menuItem, double gravity, boolean insertSeparatorBefore, boolean insertSeparatorAfter) {
		getStylesPanelProvider().getOptionsBtn().addOption(menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
	}
	
	public void removeOption(JMenuItem menuItem) {
		getStylesPanelProvider().getOptionsBtn().removeOption(menuItem);
	}
	
	public void addTableOption(JMenuItem menuItem, double gravity, boolean insertSeparatorBefore, boolean insertSeparatorAfter) {
//		getColumnStylePnl().getOptionsBtn().addOption(menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
	}
	
	public void removeTableOption(JMenuItem menuItem) {
//		getColumnStylePnl().getOptionsBtn().removeOption(menuItem);
	}


	public void addContextMenuItem(JMenuItem menuItem, double gravity, boolean insertSeparatorBefore, boolean insertSeparatorAfter) {
		getPropertiesPnl().addContextMenuItem(menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
	}
	
	public void updateColumns(List<ColumnSpec> columns, ColumnSpec selectedCol) {
		getColumnStylePnl().updateColumns(columns, selectedCol);
	}
	
	private void init() {
		setMinimumSize(new Dimension(260, 240));
		setPreferredSize(new Dimension(400, 385));
		setOpaque(!isAquaLAF());
		
		var topPanel = new JPanel(new BorderLayout());
		
		topPanel.add(getStylesPanelProvider().getComponent(), BorderLayout.CENTER);
		
		getPropertiesPnl().getPropertiesPn().addChangeListener(e -> {
			var sheet = getPropertiesPnl().getSelectedVisualPropertySheet();
			
			if (sheet == null)
				return;
			
			var type = sheet.getModel().getLexiconType();
			topPanel.removeAll();
			
			topPanel.add(getStylesPanelProvider().getComponent(), BorderLayout.NORTH);
			
			if (CyColumn.class.equals(type) || CyTable.class.equals(type))
				topPanel.add(getColumnStylePnl().getComponent(), BorderLayout.CENTER);
				
			topPanel.revalidate();
			topPanel.repaint();
	    });
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(topPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getPropertiesPnl().getComponent(), DEFAULT_SIZE, 280, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(topPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getPropertiesPnl().getComponent(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);
	}
}
