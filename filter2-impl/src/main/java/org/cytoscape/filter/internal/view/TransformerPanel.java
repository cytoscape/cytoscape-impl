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
import javax.swing.JSeparator;

import org.cytoscape.filter.internal.composite.CompositeTransformerPanel;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;


@SuppressWarnings("serial")
public class TransformerPanel extends AbstractPanel<TransformerElement, TransformerPanelController> {
	
	private CompositeTransformerPanel root;
	private JComboBox startWithComboBox;
	private JButton shiftUpButton;
	private JButton shiftDownButton;
	private JButton deleteButton;
	private JButton selectAllButton;
	private JButton deselectAllButton;
	
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
		add(namedElementComboBox, new GridBagConstraints(0, row, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(optionsButton, new GridBagConstraints(2, row++, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));
		
		add(new JSeparator(), new GridBagConstraints(0, row++, 3, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
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
		
		CompositeTransformerPanel panel = new CompositeTransformerPanel(this, controller, chain, iconManager);
		setRootPanel(panel);
	}

	void setRootPanel(CompositeTransformerPanel panel) {
		root = panel;
		scrollPane.setViewportView(root);
		
		if (root == null) {
			return;
		}
		root.updateLayout();
	}

	private JButton[] createButtons() {
		Font arrowFont = iconManager.getIconFont(20.0f);
		Font iconFont = iconManager.getIconFont(22.0f);
		
		shiftUpButton = new JButton(IconManager.ICON_CARET_UP);
		styleToolBarButton(shiftUpButton);
		shiftUpButton.setFont(arrowFont);
		shiftUpButton.setToolTipText("Shift selected chain entries up");
		shiftUpButton.setEnabled(false);
		shiftUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleShiftUp(TransformerPanel.this);
			}
		});
		
		shiftDownButton = new JButton(IconManager.ICON_CARET_DOWN);
		styleToolBarButton(shiftDownButton);
		shiftDownButton.setFont(arrowFont);
		shiftDownButton.setToolTipText("Shift selected chain entries down");
		shiftDownButton.setEnabled(false);
		shiftDownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleShiftDown(TransformerPanel.this);
			}
		});

		deleteButton = new JButton(IconManager.ICON_TRASH);
		styleToolBarButton(deleteButton);
		deleteButton.setFont(iconFont);
		deleteButton.setToolTipText("Delete selected chain entries");
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleDelete(TransformerPanel.this);
			}
		});
		
		selectAllButton = new JButton(IconManager.ICON_CHECK + " " + IconManager.ICON_CHECK);
		selectAllButton.setFont(iconFont.deriveFont(iconFont.getSize()/2.0f));
		selectAllButton.setToolTipText("Select all chain entries");
		selectAllButton.setEnabled(false);
		styleToolBarButton(selectAllButton);
		selectAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleSelectAll(TransformerPanel.this);
			}
		});
		
		deselectAllButton = new JButton(IconManager.ICON_CHECK_EMPTY + " " + IconManager.ICON_CHECK_EMPTY);
		deselectAllButton.setFont(iconFont.deriveFont(iconFont.getSize()/2.0f));
		deselectAllButton.setToolTipText("Deselect all chain entries");
		deselectAllButton.setEnabled(false);
		styleToolBarButton(deselectAllButton);
		deselectAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleDeselectAll(TransformerPanel.this);
			}
		});
		
		return new JButton[] {
			shiftUpButton,
			shiftDownButton,
			selectAllButton,
			deselectAllButton,
			deleteButton,
		};
	}

	public CompositeTransformerPanel getRootPanel() {
		return root;
	}
	
	public JButton getShiftUpButton() {
		return shiftUpButton;
	}
	
	public JButton getShiftDownButton() {
		return shiftDownButton;
	}
	
	public JButton getDeleteButton() {
		return deleteButton;
	}
	
	public JButton getSelectAllButton() {
		return selectAllButton;
	}
	
	public JButton getDeselectAllButton() {
		return deselectAllButton;
	}
}
