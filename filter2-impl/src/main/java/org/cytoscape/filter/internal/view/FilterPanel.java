package org.cytoscape.filter.internal.view;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.cytoscape.filter.internal.composite.CompositeFilterPanel;
import org.cytoscape.filter.internal.view.FilterPanelController.FilterElement;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

@SuppressWarnings("serial")
public class FilterPanel extends JPanel {
	private Component editControlPanel;
	private CompositeFilterPanel root;
	private JButton outdentButton;
	private FilterPanelController controller;
	private JComboBox filterComboBox;
	private Component selectionPanel;
	private JPopupMenu menu;
	private DynamicComboBoxModel<FilterElement> filterComboBoxModel;
	private JMenuItem renameMenu;
	private JMenuItem deleteMenu;
	private JMenuItem exportMenu;
	private JMenuItem importMenu;
	private JScrollPane scrollPane;
	private IconManager iconManager;
	private JLabel statusLabel;

	public FilterPanel(final FilterPanelController controller, IconManager iconManager, ViewUpdater viewUpdater) {
		this.controller = controller;
		this.iconManager = iconManager;
		
		viewUpdater.setView(this);
		
		filterComboBoxModel = controller.getFilterComboBoxModel();
		
		createSelectionPanel();

		renameMenu = new JMenuItem("Rename");
		renameMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleRename(FilterPanel.this);
			}
		});
		
		deleteMenu = new JMenuItem("Delete");
		deleteMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleDelete();
			}
		});

		exportMenu = new JMenuItem("Export filters...");
		exportMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleExport(FilterPanel.this);
			}
		});

		importMenu = new JMenuItem("Import filters...");
		importMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleImport(FilterPanel.this);
			}
		});

		menu = new JPopupMenu();
		menu.add(renameMenu);
		menu.add(deleteMenu);
		menu.add(exportMenu);
		menu.add(importMenu);

		JLabel optionsLabel = new JLabel(IconManager.ICON_COG);
		Font iconFont = iconManager.getIconFont(17.0f);
		optionsLabel.setFont(iconFont);
		optionsLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				handleShowMenu(event);
			}
		});
		
		statusLabel = new JLabel("");
		
		setLayout(new GridBagLayout());
		int row = 0;
		add(selectionPanel, new GridBagConstraints(0, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(statusLabel, new GridBagConstraints(1, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));
		add(optionsLabel, new GridBagConstraints(2, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));
		
		Component editPanel = createEditPanel();
		add(editPanel, new GridBagConstraints(0, row++, 3, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		FilterElement element = (FilterElement) filterComboBox.getSelectedItem();
		createView(element.filter);
	}
	
	private void createView(CompositeFilter<CyNetwork, CyIdentifiable> filter) {
		if (filter == null) {
			setRootPanel(null);
			return;
		}
		
		// We're passing in a CompositeFilter so we can assume we're getting
		// back a CompositeFilterPanel.
		CompositeFilterPanel panel = (CompositeFilterPanel) controller.createView(this, filter, 0);
		setRootPanel(panel);
	}

	protected void handleShowMenu(MouseEvent event) {
		ComboBoxModel model = filterComboBox.getModel();
		FilterElement selected = (FilterElement) filterComboBox.getSelectedItem();
		renameMenu.setEnabled(selected != null && selected.filter != null);
		deleteMenu.setEnabled(model.getSize() > 2);
		menu.show(event.getComponent(), event.getX(), event.getY());
	}

	private Component createEditPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder());
		
		createEditControlPanel();
		editControlPanel.setVisible(false);
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		panel.setLayout(new GridBagLayout());
		
		int row = 0;
		panel.add(editControlPanel, new GridBagConstraints(0, row++, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(scrollPane, new GridBagConstraints(0, row++, 1, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		return panel;
	}

	private void createSelectionPanel() {
		filterComboBox = new JComboBox(filterComboBoxModel);
		filterComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleFilterSelected(filterComboBox, FilterPanel.this);
			}
		});
		selectionPanel = filterComboBox;
	}

	void createEditControlPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
	
		Font arrowFont = iconManager.getIconFont(11.0f);
		Font iconFont = iconManager.getIconFont(17.0f);

		outdentButton = new JButton(IconManager.ICON_STEP_BACKWARD);
		outdentButton.setFont(arrowFont);
		outdentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleOutdent(FilterPanel.this);
			}
		});
		
		JButton indentButton = new JButton(IconManager.ICON_STEP_FORWARD);
		indentButton.setFont(arrowFont);
		indentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleIndent(FilterPanel.this);
			}
		});
		
		JButton deleteButton = new JButton(IconManager.ICON_TRASH);
		deleteButton.setFont(iconFont);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleDelete(FilterPanel.this);
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleCancel(FilterPanel.this);
			}
		});
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
										.addComponent(outdentButton)
										.addComponent(indentButton)
										.addComponent(deleteButton)
										.addComponent(cancelButton));
		layout.setVerticalGroup(layout.createParallelGroup()
									  .addComponent(outdentButton)
									  .addComponent(indentButton)
									  .addComponent(deleteButton)
									  .addComponent(cancelButton));
		
		editControlPanel = panel;
	}
	
	CompositeFilterPanel getRootPanel() {
		return root;
	}

	public void setRootPanel(CompositeFilterPanel panel) {
		root = panel;
		scrollPane.setViewportView(root);
		root.updateLayout();
	}
	
	public JButton getOutdentButton() {
		return outdentButton;
	}

	public Component getEditPanel() {
		return editControlPanel;
	}

	public JComboBox getFilterComboBox() {
		return filterComboBox;
	}
	
	public void setStatus(String status) {
		statusLabel.setText(status);
	}
}
