package org.cytoscape.filter.internal.composite;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.cytoscape.filter.internal.view.TransformerElementViewModel;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerPanelController;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

@SuppressWarnings("serial")
public class CompositeTransformerPanel extends JPanel {
	private Map<Transformer<CyNetwork, CyIdentifiable>, TransformerElementViewModel<TransformerPanel>> viewModels;
	private GroupLayout layout;
	private final JComboBox addComboBox;
	private TransformerPanel parent;
	private TransformerPanelController transformerPanelController;
	private List<Transformer<CyNetwork, CyIdentifiable>> model;
	
	public CompositeTransformerPanel(TransformerPanel parent, TransformerPanelController transformerPanelController, List<Transformer<CyNetwork, CyIdentifiable>> model) {
		this(parent, transformerPanelController, new Controller(), model);
	}
	
	CompositeTransformerPanel(TransformerPanel parent, TransformerPanelController transformerPanelController, final Controller controller, List<Transformer<CyNetwork, CyIdentifiable>> model) {
		this.parent = parent;
		this.transformerPanelController = transformerPanelController;
		this.model = model;
		
		ViewUtil.configureFilterView(this);
		setBorder(BorderFactory.createEmptyBorder());

		viewModels = new WeakHashMap<Transformer<CyNetwork,CyIdentifiable>, TransformerElementViewModel<TransformerPanel>>();
		layout = new GroupLayout(this);
		setLayout(layout);

		addComboBox = createChainComboBox(transformerPanelController.createChainComboBoxModel());

		for (Transformer<CyNetwork, CyIdentifiable> transformer : model) {
			JComponent component = transformerPanelController.createView(parent, transformer);
			TransformerElementViewModel<TransformerPanel> viewModel = new TransformerElementViewModel<TransformerPanel>(component, transformerPanelController, parent);
			viewModels.put(transformer, viewModel);
		}
	}
	
	JComboBox createChainComboBox(ComboBoxModel model) {
		final JComboBox comboBox = new JComboBox(model);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				transformerPanelController.handleAddTransformer(comboBox, CompositeTransformerPanel.this);
			}
		});
		return comboBox;
	}

	public void updateLayout() {
		removeAll();

		Group columns = layout.createParallelGroup(Alignment.LEADING, true);
		Group rows = layout.createSequentialGroup();
		
		ParallelGroup checkBoxGroup = layout.createParallelGroup(Alignment.LEADING);
		ParallelGroup viewGroup = layout.createParallelGroup(Alignment.LEADING);
		columns.addGroup(layout.createSequentialGroup()
							   .addGroup(checkBoxGroup)
							   .addGroup(viewGroup));
		
		for (Transformer<CyNetwork, CyIdentifiable> transformer : model) {
			TransformerElementViewModel<TransformerPanel> viewModel = viewModels.get(transformer);
			checkBoxGroup.addComponent(viewModel.checkBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE);
			viewGroup.addComponent(viewModel.view, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
			
			rows.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(viewModel.checkBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
								.addComponent(viewModel.view, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
		}
		
		viewGroup.addComponent(addComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
		rows.addComponent(addComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
		
		layout.setHorizontalGroup(columns);
		layout.setVerticalGroup(rows);
	}

	static class Controller {
		List<Transformer<CyNetwork, CyIdentifiable>> model;
		
		Controller() {
			model = new ArrayList<Transformer<CyNetwork,CyIdentifiable>>();
		}
	}

	public void addTransformer(Transformer<CyNetwork, CyIdentifiable> transformer) {
		JComponent component = transformerPanelController.createView(parent, transformer);
		final TransformerElementViewModel<TransformerPanel> viewModel = new TransformerElementViewModel<TransformerPanel>(component, transformerPanelController, parent);
		addViewModel(transformer, viewModel);
	}

	public void addViewModel(Transformer<CyNetwork, CyIdentifiable> transformer, TransformerElementViewModel<TransformerPanel> viewModel) {
		model.add(transformer);
		viewModels.put(transformer, viewModel);
	}

	public void deselectAll() {
		for (TransformerElementViewModel<TransformerPanel> viewModel : viewModels.values()) {
			if (viewModel.checkBox.isSelected()) {
				viewModel.checkBox.setSelected(false);
				viewModel.view.setBackground(Color.white);
			}
		}
	}

	public void deleteSelected() {
		int index = 0;
		while (index < model.size()) {
			TransformerElementViewModel<TransformerPanel> viewModel = viewModels.get(model.get(index));
			if (viewModel.checkBox.isSelected()) {
				removeTransformer(index--);
			}
			index++;
		}
	}

	private void removeTransformer(int index) {
		Transformer<CyNetwork, CyIdentifiable> transformer = model.remove(index);
		viewModels.remove(transformer);
	}
	
	public List<Transformer<CyNetwork, CyIdentifiable>> getModel() {
		return model;
	}
	
	public TransformerElementViewModel<TransformerPanel> getViewModel(Transformer<CyNetwork, CyIdentifiable> transformer) {
		return viewModels.get(transformer);
	}
}
