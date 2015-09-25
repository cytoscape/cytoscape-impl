package org.cytoscape.filter.internal.filters.composite;

import java.awt.dnd.DropTarget;
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

import org.cytoscape.filter.internal.view.DragHandler;
import org.cytoscape.filter.internal.view.TransformerElementViewModel;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerPanelController;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;

@SuppressWarnings("serial")
public class CompositeTransformerPanel extends JPanel {
	
	private Map<Transformer<CyNetwork, CyIdentifiable>, TransformerElementViewModel<TransformerPanel>> viewModels;
	private GroupLayout layout;
	private final JButton addButton;
	private TransformerPanel parent;
	private TransformerPanelController transformerPanelController;
	private List<Transformer<CyNetwork, CyIdentifiable>> model;
	private final IconManager iconManager;
	private JComponent separator; 
	
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
		
		separator = new CompositeSeparator();
		new DropTarget(separator, new DragHandler<TransformerPanel>(separator, transformerPanelController, parent, null));
		
		ViewUtil.configureFilterView(this);
		setBorder(BorderFactory.createEmptyBorder());

		viewModels = new WeakHashMap<Transformer<CyNetwork,CyIdentifiable>, TransformerElementViewModel<TransformerPanel>>();
		layout = new GroupLayout(this);
		setLayout(layout);

		addButton = createAddChainEntryButton();

		for (Transformer<CyNetwork, CyIdentifiable> transformer : model) {
			JComponent component = transformerPanelController.createView(parent, transformer, 0);
			TransformerElementViewModel<TransformerPanel> viewModel = new TransformerElementViewModel<TransformerPanel>(component, transformerPanelController, parent, iconManager);
			viewModels.put(transformer, viewModel);
		}
	}
	
	JButton createAddChainEntryButton() {
		final JButton button = new JButton(IconManager.ICON_PLUS);
		button.setFont(iconManager.getIconFont(11.0f));
		button.setToolTipText("Add new chain entry...");
		button.putClientProperty("JButton.buttonType", "gradient");
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
						.addGap(4)
						.addGroup(checkBoxGroup)
						.addGap(4)
						.addGroup(viewGroup));
		
		final Group rows = layout.createSequentialGroup();
		
		int separatorHeight = 5;
		viewGroup.addComponent(separator);
		rows.addComponent(separator, separatorHeight, separatorHeight, separatorHeight);

		for (Transformer<CyNetwork, CyIdentifiable> transformer : model) {
			final TransformerElementViewModel<TransformerPanel> viewModel = viewModels.get(transformer);
			if (viewModel.view instanceof CompositeFilterPanel) {
				CompositeFilterPanel<?> panel = (CompositeFilterPanel<?>) viewModel.view;
				panel.updateLayout();
			}
			
			checkBoxGroup.addGroup(layout.createSequentialGroup()
				.addComponent(viewModel.deleteButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
				.addGap(4)
				.addComponent(viewModel.handle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE));
			viewGroup.addComponent(viewModel.view, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					 .addComponent(viewModel.separator);
			
			rows.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
										.addGap(ViewUtil.INTERNAL_VERTICAL_PADDING)
										.addGroup(layout.createParallelGroup()
												.addComponent(viewModel.deleteButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
												.addComponent(viewModel.handle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)))
								.addComponent(viewModel.view, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
			rows.addComponent(viewModel.separator, separatorHeight, separatorHeight, separatorHeight);
		}
		
		columns.addComponent(addButton);
		rows.addGap(ViewUtil.INTERNAL_VERTICAL_PADDING).addComponent(addButton);
		
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
		JComponent component = transformerPanelController.createView(parent, transformer, 0);
		final TransformerElementViewModel<TransformerPanel> viewModel = new TransformerElementViewModel<TransformerPanel>(component, transformerPanelController, parent, iconManager);
		addViewModel(transformer, viewModel);
	}

	public void addViewModel(Transformer<CyNetwork, CyIdentifiable> transformer, TransformerElementViewModel<TransformerPanel> viewModel) {
		model.add(transformer);
		viewModels.put(transformer, viewModel);
	}

	public void removeTransformer(int index) {
		Transformer<CyNetwork, CyIdentifiable> transformer = model.remove(index);
		TransformerElementViewModel<TransformerPanel> model = viewModels.remove(transformer);
		if (model != null && model.view != null) {
			transformerPanelController.unregisterView(model.view);
		}
	}
	
	public List<Transformer<CyNetwork, CyIdentifiable>> getModel() {
		return model;
	}
	
	public TransformerElementViewModel<TransformerPanel> getViewModel(Transformer<CyNetwork, CyIdentifiable> transformer) {
		return viewModels.get(transformer);
	}
	
	public JComponent getSeparator() {
		return separator;
	}
}
