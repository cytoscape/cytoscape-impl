package org.cytoscape.filter.internal.view;

import static javax.swing.GroupLayout.*;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
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
import javax.swing.JScrollPane;

import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings({ "serial", "rawtypes" })
public abstract class AbstractPanel<T extends NamedElement, C extends AbstractPanelController> extends JPanel implements SelectPanelComponent {
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
	
	@SuppressWarnings("unchecked")
	public AbstractPanel(final C controller, IconManager iconManager) {
		this.controller = controller;
		this.iconManager = iconManager;
		
		ComboBoxModel model = controller.getElementComboBoxModel();
		namedElementComboBox = new JComboBox(model);
		namedElementComboBox.setRenderer(ViewUtil.createElipsisRenderer(50));
		namedElementComboBox.addActionListener(e -> controller.handleElementSelected(AbstractPanel.this));
		
		createMenu = new JMenuItem(controller.getCreateMenuLabel());
		createMenu.addActionListener(e -> controller.createNewElement(AbstractPanel.this));
		
		renameMenu = new JMenuItem(controller.getRenameMenuLabel());
		renameMenu.addActionListener(e -> controller.handleRename(AbstractPanel.this));
		
		deleteMenu = new JMenuItem(controller.getDeleteMenuLabel());
		deleteMenu.addActionListener(e -> controller.handleDelete());

		exportMenu = new JMenuItem(controller.getExportLabel());
		exportMenu.addActionListener(e -> controller.handleExport(AbstractPanel.this));

		importMenu = new JMenuItem(controller.getImportLabel());
		importMenu.addActionListener(e -> controller.handleImport(AbstractPanel.this));

		menu = new JPopupMenu();
		menu.add(renameMenu);
		menu.add(deleteMenu);
		menu.add(createMenu);
		menu.add(exportMenu);
		menu.add(importMenu);

		optionsButton = new JButton(IconManager.ICON_CARET_DOWN);
		optionsButton.setFont(iconManager.getIconFont(11.0f));
		optionsButton.setToolTipText("Options...");
		optionsButton.addActionListener(this::handleShowMenu);
		
		applyButton = new JButton("Apply");
		applyButton.addActionListener(e -> controller.handleApplyFilter(AbstractPanel.this));
		
		cancelApplyButton = new JLabel(IconManager.ICON_BAN);
		cancelApplyButton.setFont(iconManager.getIconFont(17.0f));
		cancelApplyButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				controller.handleCancelApply(AbstractPanel.this);
			}
		});
		cancelApplyButton.setEnabled(false);
		cancelApplyButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		statusLabel = new JLabel(" ");
		statusLabel.setFont(statusLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		
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
	
	protected JPanel createApplyPanel() {
		final JPanel panel = new JPanel();
		panel.setOpaque(!isAquaLAF());
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!isAquaLAF());
		
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
	
	protected void styleToolBarButton(JButton btn) {
		btn.setBorderPainted(false);
		btn.setContentAreaFilled(false);
		btn.setOpaque(false);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
	}
}
