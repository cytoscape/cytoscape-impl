package org.cytoscape.filter.internal.view;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.cytoscape.filter.internal.filters.composite.CompositeTransformerPanel;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;


@SuppressWarnings("serial")
public class TransformerPanel extends AbstractPanel<TransformerElement, TransformerPanelController> {
	
	private CompositeTransformerPanel root;
	private JComboBox startWithComboBox;
	
	public TransformerPanel(final TransformerPanelController controller, IconManager iconManager, TransformerWorker worker) {
		super(controller, iconManager);
		setOpaque(!isAquaLAF());

		worker.setView(this);
		
		JPanel applyPanel = createApplyPanel();

		Component editPanel = createEditPanel();

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
		panel.setOpaque(!isAquaLAF());
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
		new TransformerElementViewModel<TransformerPanel>(panel, controller, this, iconManager);
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

	public CompositeTransformerPanel getRootPanel() {
		return root;
	}
	
	@Override
	public void reset() {
		setRootPanel(null);
	}
}
