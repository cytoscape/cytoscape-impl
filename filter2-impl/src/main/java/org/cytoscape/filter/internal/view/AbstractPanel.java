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
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

@SuppressWarnings({ "serial", "rawtypes" })
public class AbstractPanel<T extends NamedElement, C extends AbstractPanelController> extends JPanel implements SelectPanelComponent {
	protected IconManager iconManager;
	
	protected C controller;
	protected JComboBox namedElementComboBox;
	protected JPopupMenu menu;
	protected JMenuItem renameMenu;
	protected JMenuItem deleteMenu;
	protected JMenuItem exportMenu;
	protected JMenuItem importMenu;
	protected JLabel optionsLabel;
	protected Component editControlPanel;
	protected JScrollPane scrollPane;
	protected JButton applyButton;
	protected JComponent cancelApplyButton;
	protected JProgressBar progressBar;
	
	public AbstractPanel(final C controller, IconManager iconManager) {
		this.controller = controller;
		this.iconManager = iconManager;
		
		ComboBoxModel model = controller.getElementComboBoxModel();
		namedElementComboBox = new JComboBox(model);
		namedElementComboBox.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleElementSelected(AbstractPanel.this);
			}
		});
		
		renameMenu = new JMenuItem("Rename");
		renameMenu.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleRename(AbstractPanel.this);
			}
		});
		
		deleteMenu = new JMenuItem("Delete");
		deleteMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleDelete();
			}
		});

		exportMenu = new JMenuItem(controller.getExportLabel());
		exportMenu.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleExport(AbstractPanel.this);
			}
		});

		importMenu = new JMenuItem(controller.getImportLabel());
		importMenu.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleImport(AbstractPanel.this);
			}
		});

		menu = new JPopupMenu();
		menu.add(renameMenu);
		menu.add(deleteMenu);
		menu.add(exportMenu);
		menu.add(importMenu);

		optionsLabel = new JLabel(IconManager.ICON_COG);
		Font iconFont = iconManager.getIconFont(17.0f);
		optionsLabel.setFont(iconFont);
		optionsLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				handleShowMenu(event);
			}
		});
		
		applyButton = new JButton("Apply");
		applyButton.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleApplyFilter(AbstractPanel.this);
			}
		});
		
		cancelApplyButton = new JLabel(IconManager.ICON_BAN_CIRCLE);
		cancelApplyButton.setFont(iconFont);
		cancelApplyButton.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void mousePressed(MouseEvent event) {
				controller.handleCancelApply(AbstractPanel.this);
			}
		});
		cancelApplyButton.setEnabled(false);
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(AbstractPanelController.PROGRESS_BAR_MAXIMUM);
	}
	
	@SuppressWarnings("unchecked")
	protected void handleShowMenu(MouseEvent event) {
		ComboBoxModel model = controller.getElementComboBoxModel();
		T selected = (T) model.getSelectedItem();
		renameMenu.setEnabled(selected != null && !selected.isPlaceholder());
		deleteMenu.setEnabled(model.getSize() > 2);
		menu.show(event.getComponent(), event.getX(), event.getY());
	}
	
	private void createEditControlPanel(JButton... buttons) {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
	
		SequentialGroup horizontalGroup = layout.createSequentialGroup();
		ParallelGroup verticalGroup = layout.createParallelGroup();
		for (JButton button : buttons) {
			horizontalGroup.addComponent(button);
			verticalGroup.addComponent(button);
		}
		layout.setHorizontalGroup(horizontalGroup);
		layout.setVerticalGroup(verticalGroup);
		editControlPanel = panel;
	}
	
	protected Component createEditPanel(JButton... buttons) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder());
		
		createEditControlPanel(buttons);
		editControlPanel.setVisible(false);
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		panel.setLayout(new GridBagLayout());
		
		int row = 0;
		panel.add(editControlPanel, new GridBagConstraints(0, row++, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
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
	
	protected JPanel createApplyPanel() {
		JPanel applyPanel = new JPanel();
		applyPanel.setOpaque(false);
		
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new GridBagLayout());
		progressPanel.setOpaque(false);

		progressPanel.add(progressBar, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		progressPanel.add(cancelApplyButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 4, 0, 4), 0, 0));
		
		applyPanel.setLayout(new GridBagLayout());
		applyPanel.add(applyButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		applyPanel.add(progressPanel, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		return applyPanel;
	}
	
	public void setStatus(String status) {
		applyButton.setText(status);
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
}
