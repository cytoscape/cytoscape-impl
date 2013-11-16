package org.cytoscape.filter.internal.view;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.cytoscape.filter.internal.composite.CompositeFilterPanel;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

@SuppressWarnings("serial")
public class FilterPanel extends AbstractPanel<FilterElement, FilterPanelController> {
	private CompositeFilterPanel root;
	private JButton outdentButton;
	private JCheckBox applyAutomaticallyCheckBox;

	public FilterPanel(final FilterPanelController controller, IconManager iconManager, final FilterWorker worker) {
		super(controller, iconManager);
		setOpaque(false);
		
		worker.setView(this);
		
		applyAutomaticallyCheckBox = new JCheckBox("Apply Automatically"); 
		applyAutomaticallyCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.setInteractive(applyAutomaticallyCheckBox.isSelected(), FilterPanel.this);
			}
		});
		
		JPanel applyPanel = createApplyPanel();
		
		setLayout(new GridBagLayout());
		int row = 0;
		add(namedElementComboBox, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(optionsLabel, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));

		add(applyAutomaticallyCheckBox, new GridBagConstraints(0, row++, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		Component editPanel = createEditPanel(createButtons());
		add(editPanel, new GridBagConstraints(0, row++, 2, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		add(applyPanel, new GridBagConstraints(0, row++, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		
		ComboBoxModel model = controller.getElementComboBoxModel();
		FilterElement element = (FilterElement) model.getSelectedItem();
		createView(element.filter);
		
		controller.synchronize(this);
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

	JButton[] createButtons() {
		Font arrowFont = iconManager.getIconFont(16.0f);
		Font iconFont = iconManager.getIconFont(17.0f);

		outdentButton = new JButton(IconManager.ICON_CARET_LEFT);
		outdentButton.setFont(arrowFont);
		outdentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleOutdent(FilterPanel.this);
			}
		});
		
		JButton indentButton = new JButton(IconManager.ICON_CARET_RIGHT);
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
		
		return new JButton[] {
			outdentButton,
			indentButton,
			deleteButton,
			cancelButton,
		};
	}
	
	CompositeFilterPanel getRootPanel() {
		return root;
	}

	public void setRootPanel(CompositeFilterPanel panel) {
		root = panel;
		scrollPane.setViewportView(root);
		
		if (root == null) {
			return;
		}
		root.updateLayout();
	}
	
	public JButton getOutdentButton() {
		return outdentButton;
	}

	public Component getEditPanel() {
		return editControlPanel;
	}

	public JComboBox getFilterComboBox() {
		return namedElementComboBox;
	}
	
	public JCheckBox getApplyAutomaticallyCheckBox() {
		return applyAutomaticallyCheckBox;
	}
}
