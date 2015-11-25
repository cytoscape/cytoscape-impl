package org.cytoscape.filter.internal.view;

import static javax.swing.GroupLayout.*;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.cytoscape.filter.internal.filters.composite.CompositeTransformerPanel;
import org.cytoscape.filter.internal.work.TransformerWorker;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;


@SuppressWarnings("serial")
public class TransformerPanel extends AbstractPanel<TransformerElement, TransformerPanelController> {
	
	private CompositeTransformerPanel root;
	private JComboBox<FilterElement> startWithComboBox;
	
	@SuppressWarnings("unchecked")
	public TransformerPanel(final TransformerPanelController controller, IconManager iconManager, TransformerWorker worker) {
		super(controller, iconManager);
		setOpaque(!isAquaLAF());

		worker.setView(this);
		
		final JPanel applyPanel = createApplyPanel();
		final Component editPanel = createEditPanel();
		final JLabel startWithLabel = new JLabel("Start with:");
		startWithComboBox = new JComboBox<>(controller.getStartWithComboBoxModel());
		startWithComboBox.setRenderer(ViewUtil.createElipsisRenderer(50));
		final JSeparator sep = new JSeparator();
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(!isAquaLAF());
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(namedElementComboBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(optionsButton, PREFERRED_SIZE, 64, PREFERRED_SIZE)
				)
				.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(startWithLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(startWithComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(editPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(applyPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(namedElementComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(optionsButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(startWithLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(startWithComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(editPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(applyPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		DynamicComboBoxModel<TransformerElement> model = controller.getElementComboBoxModel();
		TransformerElement element = (TransformerElement) model.getSelectedItem();
		createView(element.getChain());

		controller.synchronize(this);
	}

	private void createView(List<Transformer<CyNetwork, CyIdentifiable>> chain) {
		if (chain == null) {
			setRootPanel(null);
			return;
		}
		
		CompositeTransformerPanel panel = new CompositeTransformerPanel(this, controller, chain, iconManager);
		new TransformerElementViewModel<TransformerPanel>(panel, controller, this);
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

	@Override
	public CompositeTransformerPanel getRootPanel() {
		return root;
	}
	
	@Override
	public void reset() {
		setRootPanel(null);
	}
}
