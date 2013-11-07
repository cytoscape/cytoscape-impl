package org.cytoscape.filter.internal.view;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.filter.internal.composite.CompositeTransformerPanel;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;


@SuppressWarnings("serial")
public class TransformerPanel extends AbstractPanel<TransformerElement, TransformerPanelController> {
	CompositeTransformerPanel root;
	private JComboBox startWithComboBox;
	JButton shiftUpButton;
	JButton shiftDownButton;
	
	public TransformerPanel(final TransformerPanelController controller, IconManager iconManager, TransformerWorker worker) {
		super(controller, iconManager);
		setOpaque(false);

		worker.setView(this);
		
		JPanel applyPanel = createApplyPanel();

		Component editPanel = createEditPanel(createButtons());

		startWithComboBox = new JComboBox(controller.getStartWithComboBoxModel());
		
		JPanel startWithPanel = createStartWithPanel();
		
		setLayout(new GridBagLayout());
		int row = 0;
		add(namedElementComboBox, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(optionsLabel, new GridBagConstraints(1, row++, 1, 1, 1, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));
		add(startWithPanel, new GridBagConstraints(0, row++, 3, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 0, 0), 0, 0));
		add(editPanel, new GridBagConstraints(0, row++, 3, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(applyPanel, new GridBagConstraints(0, row++, 3, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		ComboBoxModel model = controller.getElementComboBoxModel();
		TransformerElement element = (TransformerElement) model.getSelectedItem();
		createView(element.chain);

		controller.synchronize(this);
	}

	private JPanel createStartWithPanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		panel.add(new JLabel("Start with"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(startWithComboBox, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		return panel;
	}

	private void createView(List<Transformer<CyNetwork, CyIdentifiable>> chain) {
		if (chain == null) {
			setRootPanel(null);
			return;
		}
		
		CompositeTransformerPanel panel = new CompositeTransformerPanel(this, controller, chain);
		setRootPanel(panel);
	}

	void setRootPanel(CompositeTransformerPanel panel) {
		root = panel;
		scrollPane.setViewportView(root);
		root.updateLayout();
	}

	private JButton[] createButtons() {
		Font arrowFont = iconManager.getIconFont(16.0f);
		Font iconFont = iconManager.getIconFont(17.0f);
		
		shiftUpButton = new JButton(IconManager.ICON_CARET_UP);
		shiftUpButton.setFont(arrowFont);
		shiftUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleShiftUp(TransformerPanel.this);
			}
		});
		
		shiftDownButton = new JButton(IconManager.ICON_CARET_DOWN);
		shiftDownButton.setFont(arrowFont);
		shiftDownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleShiftDown(TransformerPanel.this);
			}
		});

		JButton deleteButton = new JButton(IconManager.ICON_TRASH);
		deleteButton.setFont(iconFont);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleDelete(TransformerPanel.this);
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleCancel(TransformerPanel.this);
			}
		});
		
		return new JButton[] {
			shiftUpButton,
			shiftDownButton,
			deleteButton,
			cancelButton,
		};
	}

	public CompositeTransformerPanel getRootPanel() {
		return root;
	}
}
