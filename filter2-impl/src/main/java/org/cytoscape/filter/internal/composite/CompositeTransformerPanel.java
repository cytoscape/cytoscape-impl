package org.cytoscape.filter.internal.composite;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.cytoscape.filter.internal.view.IconManager;
import org.cytoscape.filter.internal.view.TransformerElementViewModel;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerPanelController;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

@SuppressWarnings("serial")
public class CompositeTransformerPanel extends JPanel {
	
	public static final Color SELECTED_BACKGROUND_COLOR = new Color(222, 234, 252);
	public static final Color UNSELECTED_BACKGROUND_COLOR = Color.WHITE;
	
	private Map<Transformer<CyNetwork, CyIdentifiable>, TransformerElementViewModel<TransformerPanel>> viewModels;
	private GroupLayout layout;
	private final JButton addButton;
	private TransformerPanel parent;
	private TransformerPanelController transformerPanelController;
	private List<Transformer<CyNetwork, CyIdentifiable>> model;
	private final IconManager iconManager;
	
	public CompositeTransformerPanel(TransformerPanel parent, TransformerPanelController transformerPanelController, 
			List<Transformer<CyNetwork, CyIdentifiable>> model, IconManager iconManager) {
		this(parent, transformerPanelController, new Controller(), model, iconManager);
	}
	
	CompositeTransformerPanel(TransformerPanel parent, TransformerPanelController transformerPanelController,
			final Controller controller, List<Transformer<CyNetwork, CyIdentifiable>> model, IconManager iconManager) {
		this.parent = parent;
		this.transformerPanelController = transformerPanelController;
		this.model = model;
		this.iconManager = iconManager;
		
		ViewUtil.configureFilterView(this);
		setBorder(BorderFactory.createEmptyBorder());

		viewModels = new WeakHashMap<Transformer<CyNetwork,CyIdentifiable>, TransformerElementViewModel<TransformerPanel>>();
		layout = new GroupLayout(this);
		setLayout(layout);

		addButton = createAddChainEntryButton();

		for (Transformer<CyNetwork, CyIdentifiable> transformer : model) {
			JComponent component = transformerPanelController.createView(parent, transformer);
			TransformerElementViewModel<TransformerPanel> viewModel = new TransformerElementViewModel<TransformerPanel>(component, transformerPanelController, parent);
			viewModels.put(transformer, viewModel);
		}
	}
	
	JButton createAddChainEntryButton() {
		final JButton button = new JButton(IconManager.ICON_PLUS);
		button.setFont(iconManager.getIconFont(12.0f));
		button.setToolTipText("Add new chain entry...");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JPopupMenu menu = transformerPanelController.createAddChainEntryMenu(CompositeTransformerPanel.this, parent);
				menu.show(button, 0, button.getHeight());
			}
		});
		return button;
	}

	public void updateLayout() {
		removeAll();

		final ParallelGroup checkBoxGroup = layout.createParallelGroup(Alignment.LEADING);
		final ParallelGroup viewGroup = layout.createParallelGroup(Alignment.LEADING);
		
		final Group columns = layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addGroup(checkBoxGroup)
						.addGroup(viewGroup));
		final Group rows = layout.createSequentialGroup();
		
		for (Transformer<CyNetwork, CyIdentifiable> transformer : model) {
			final TransformerElementViewModel<TransformerPanel> viewModel = viewModels.get(transformer);
			
			checkBoxGroup.addComponent(viewModel.checkBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE);
			viewGroup.addComponent(viewModel.view, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
			
			rows.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(viewModel.checkBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
								.addComponent(viewModel.view, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
		}
		
		columns.addComponent(addButton);
		rows.addComponent(addButton);
		
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

	public void selectAll() {
		for (TransformerElementViewModel<TransformerPanel> viewModel : viewModels.values()) {
			if (!viewModel.checkBox.isSelected()) {
				viewModel.checkBox.setSelected(true);
				viewModel.view.setBackground(SELECTED_BACKGROUND_COLOR);
			}
		}
	}
	
	public void deselectAll() {
		for (TransformerElementViewModel<TransformerPanel> viewModel : viewModels.values()) {
			if (viewModel.checkBox.isSelected()) {
				viewModel.checkBox.setSelected(false);
				viewModel.view.setBackground(UNSELECTED_BACKGROUND_COLOR);
			}
		}
	}
	
	public int countSelected() {
		int count = 0;
		for (TransformerElementViewModel<TransformerPanel> viewModel : viewModels.values()) {
			if (viewModel.checkBox.isSelected()) {
				count++;
			}
		}
		return count;
	}
	
	public int countUnselected() {
		int count = 0;
		for (TransformerElementViewModel<TransformerPanel> viewModel : viewModels.values()) {
			if (!viewModel.checkBox.isSelected()) {
				count++;
			}
		}
		return count;
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
