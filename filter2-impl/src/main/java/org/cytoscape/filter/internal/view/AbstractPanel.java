package org.cytoscape.filter.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.cytoscape.filter.TransformerContainer;
import org.cytoscape.filter.internal.work.AbstractWorker.ApplyAction;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Filters 2 Impl (filter2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

@SuppressWarnings({ "serial", "rawtypes" })
public abstract class AbstractPanel<T extends NamedElement, C extends AbstractPanelController> 
	extends JPanel implements SelectPanelComponent, TransformerContainer<CyNetwork, CyIdentifiable> {
	
	protected C controller;
	protected JComboBox namedElementComboBox;
	protected JButton optionsButton;
	protected Component editControlPanel;
	protected JScrollPane scrollPane;
	protected JButton applyButton;
	protected JButton cancelApplyButton;
	protected JProgressBar progressBar;
	protected JRadioButton selectButton;
	protected JRadioButton filterButton;

	protected JLabel statusLabel;
	
	protected final CyServiceRegistrar serviceRegistrar;
	
	@SuppressWarnings("unchecked")
	public AbstractPanel(final C controller, final CyServiceRegistrar serviceRegistrar) {
		this.controller = controller;
		this.serviceRegistrar = serviceRegistrar;
		
		ComboBoxModel model = controller.getElementComboBoxModel();
		namedElementComboBox = new JComboBox(model);
		namedElementComboBox.setRenderer(ViewUtil.createElipsisRenderer(50));
		namedElementComboBox.addActionListener(e -> controller.handleElementSelected(AbstractPanel.this));
		
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		
		optionsButton = new JButton(IconManager.ICON_BARS);
		optionsButton.setFont(iconManager.getIconFont(12.0f));
		optionsButton.setToolTipText("Options...");
		optionsButton.addActionListener(this::handleShowMenu);
		
		applyButton = new JButton("Apply");
		applyButton.addActionListener(e -> controller.handleApplyFilter(AbstractPanel.this));
		
		cancelApplyButton = new JButton(IconManager.ICON_BAN);
		cancelApplyButton.addActionListener(e -> controller.handleCancelApply(AbstractPanel.this));
		cancelApplyButton.setFont(iconManager.getIconFont(17.0f));
		cancelApplyButton.setEnabled(false);
		cancelApplyButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		cancelApplyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(cancelApplyButton.isEnabled()) {
					cancelApplyButton.setForeground(UIManager.getColor("Focus.color"));
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if(cancelApplyButton.isEnabled()) {
					cancelApplyButton.setForeground(UIManager.getColor("Button.foreground"));
				}
			}
		});
		
		statusLabel = new JLabel(" ");
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(AbstractPanelController.PROGRESS_BAR_MAXIMUM);
		
		selectButton = new JRadioButton("select");
		filterButton = new JRadioButton("show");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(selectButton);
		buttonGroup.add(filterButton);
		selectButton.setSelected(true);
		selectButton.addActionListener(e -> {
			controller.setApplyAction(ApplyAction.SELECT);
			applyButton.doClick();
		});
		filterButton.addActionListener(e -> {
			controller.setApplyAction(ApplyAction.SHOW);
			applyButton.doClick();
		});
		
		LookAndFeelUtil.makeSmall(statusLabel, selectButton, filterButton);
	}
	
	@SuppressWarnings("unchecked")
	private void handleShowMenu(ActionEvent event) {
		JMenuItem createMenu = new JMenuItem(controller.getCreateMenuLabel());
		createMenu.addActionListener(e -> controller.createNewElement(AbstractPanel.this));
		
		JMenuItem renameMenu = new JMenuItem(controller.getRenameMenuLabel());
		renameMenu.addActionListener(e -> controller.handleRename());
		
		JMenuItem copyMenu = new JMenuItem(controller.getCopyMenuLabel());
		copyMenu.addActionListener(e -> controller.handleCopy(AbstractPanel.this));
		
		JMenuItem deleteMenu = new JMenuItem(controller.getDeleteMenuLabel());
		deleteMenu.addActionListener(e -> controller.handleDelete());

		JMenuItem exportMenu = new JMenuItem(controller.getExportLabel());
		exportMenu.addActionListener(e -> controller.handleExport());

		JMenuItem importMenu = new JMenuItem(controller.getImportLabel());
		importMenu.addActionListener(e -> controller.handleImport(AbstractPanel.this));
		
		JPopupMenu menu = new JPopupMenu();
		menu.add(createMenu);
		menu.add(renameMenu);
		menu.add(copyMenu);
		menu.add(deleteMenu);
		menu.add(exportMenu);
		menu.add(importMenu);
		
		ComboBoxModel model = controller.getElementComboBoxModel();
		T selected = (T) model.getSelectedItem();
		renameMenu.setEnabled(selected != null);
		copyMenu.setEnabled(selected != null);
		deleteMenu.setEnabled(model.getSize() > 1);
		
		Component c = (Component) event.getSource();
		menu.show(c, 0, c.getHeight());
	}
	
	protected Component createEditPanel() {
		scrollPane = new JScrollPane();
		
		final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		int row = 0;
		panel.add(scrollPane, new GridBagConstraints(0, row++, 1, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		return panel;
	}
	
	@Override
	public Component getComponent() {
		return this;
	}
	
	@Override
	public Component getEditPanel() {
		return editControlPanel;
	}
	
	protected JPanel createApplyPanel(boolean showButton) {
		final JPanel panel = new JPanel();
		panel.setOpaque(!isAquaLAF());
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!isAquaLAF());
		
		if(showButton) {
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(selectButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(filterButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createSequentialGroup()
							.addComponent(applyButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(progressBar, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(cancelApplyButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(statusLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
							.addComponent(selectButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(filterButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)	
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(applyButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(progressBar, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(cancelApplyButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(statusLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		} else {
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(applyButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(progressBar, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(cancelApplyButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(statusLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(applyButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(progressBar, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(cancelApplyButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(statusLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return panel;
	}
	
	public void setStatus(String status) {
		if (status == null || status.isEmpty()) {
			status = " ";
		}
		statusLabel.setText(status);
	}

	public JComponent getApplyButton() {
		return applyButton;
	}

	public JComponent getCancelApplyButton() {
		return cancelApplyButton;
	}
	
	@Override
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public C getController() {
		return controller;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addNamedTransformer(NamedTransformer<CyNetwork, CyIdentifiable> transformer) {
		controller.addNamedTransformer(this, transformer, true);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public NamedTransformer<CyNetwork, CyIdentifiable> getNamedTransformer(String name) {
		if(name == null)
			return null;
		NamedTransformer[] namedTransformers = controller.getNamedTransformers();
		if(namedTransformers == null)
			return null;
		for(NamedTransformer<CyNetwork, CyIdentifiable> transformer : namedTransformers) {
			if(name.equals(transformer.getName())) {
				return transformer;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<NamedTransformer<CyNetwork,CyIdentifiable>> getNamedTransformers() {
		NamedTransformer<CyNetwork, CyIdentifiable>[] namedTransformers = controller.getNamedTransformers();
		return Arrays.asList(namedTransformers);
	}
	
	@Override 
	public boolean removeNamedTransformer(String name) {
		if(name == null)
			return false;
		return controller.handleDelete(name);
	}
	
	protected void styleToolBarButton(JButton btn) {
		btn.setBorderPainted(false);
		btn.setContentAreaFilled(false);
		btn.setOpaque(false);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
	}
}
