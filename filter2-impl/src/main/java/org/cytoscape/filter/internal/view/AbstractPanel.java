package org.cytoscape.filter.internal.view;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
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
	protected JMenuItem createMenu;
	protected JMenuItem renameMenu;
	protected JMenuItem deleteMenu;
	protected JMenuItem exportMenu;
	protected JMenuItem importMenu;
	protected JButton optionsButton;
	protected Component editControlPanel;
	protected JScrollPane scrollPane;
	protected JButton applyButton;
	protected JComponent cancelApplyButton;
	protected JProgressBar progressBar;

	protected JLabel statusLabel;
	
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
		
		createMenu = new JMenuItem(controller.getCreateMenuLabel());
		createMenu.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.createNewElement(AbstractPanel.this);
			}
		});
		
		renameMenu = new JMenuItem(controller.getRenameMenuLabel());
		renameMenu.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleRename(AbstractPanel.this);
			}
		});
		
		deleteMenu = new JMenuItem(controller.getDeleteMenuLabel());
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
		menu.add(createMenu);
		menu.add(exportMenu);
		menu.add(importMenu);

		optionsButton = new JButton(IconManager.ICON_CARET_DOWN);
		optionsButton.setFont(iconManager.getIconFont(11.0f));
		optionsButton.setToolTipText("Options...");
		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
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
		cancelApplyButton.setFont(iconManager.getIconFont(17.0f));
		cancelApplyButton.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void mousePressed(MouseEvent event) {
				controller.handleCancelApply(AbstractPanel.this);
			}
		});
		cancelApplyButton.setEnabled(false);
		
		statusLabel = new JLabel(" ");
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(AbstractPanelController.PROGRESS_BAR_MAXIMUM);
	}
	
	@SuppressWarnings("unchecked")
	protected void handleShowMenu(ActionEvent event) {
		ComboBoxModel model = controller.getElementComboBoxModel();
		T selected = (T) model.getSelectedItem();
		renameMenu.setEnabled(selected != null);
		deleteMenu.setEnabled(model.getSize() > 1);
		Component c = (Component) event.getSource();
		menu.show(c, 0, c.getHeight());
	}
	
	private void createEditControlPanel(JButton... buttons) {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
		panel.setLayout(layout);
	
		for (JButton button : buttons) {
			panel.add(Box.createHorizontalStrut(5));
			panel.add(button);
			panel.add(Box.createHorizontalStrut(5));
		}
		
		editControlPanel = panel;
	}
	
	protected Component createEditPanel(JButton... buttons) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder());
		
		createEditControlPanel(buttons);
		
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
		applyPanel.add(statusLabel, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 0, 4), 0, 0));
		return applyPanel;
	}
	
	public void setStatus(String status) {
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
	
	protected void styleToolBarButton(JButton btn) {
		btn.setBorderPainted(false);
		btn.setContentAreaFilled(false);
		btn.setOpaque(false);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
	}
}
