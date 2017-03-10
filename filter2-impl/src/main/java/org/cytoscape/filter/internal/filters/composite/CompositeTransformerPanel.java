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

import org.cytoscape.filter.internal.view.CompositePanelComponent;
import org.cytoscape.filter.internal.view.DragHandler;
import org.cytoscape.filter.internal.view.TransformerElementViewModel;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerPanelController;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.ValidatableTransformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;

/*
 * #%L
 * Cytoscape Filters 2 Impl (filter2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

@SuppressWarnings("serial")
public class CompositeTransformerPanel extends JPanel implements CompositePanelComponent {
	
	private Map<Transformer<CyNetwork, CyIdentifiable>, TransformerElementViewModel<TransformerPanel>> viewModels;
	private GroupLayout layout;
	private final JButton addButton;
	private TransformerPanel parent;
	private TransformerPanelController transformerPanelController;
	private List<Transformer<CyNetwork, CyIdentifiable>> model;
	private JComponent separator;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public CompositeTransformerPanel(TransformerPanel parent, TransformerPanelController transformerPanelController,
			List<Transformer<CyNetwork, CyIdentifiable>> model, final CyServiceRegistrar serviceRegistrar) {
		this(parent, transformerPanelController, new Controller(), model, serviceRegistrar);
	}
	
	CompositeTransformerPanel(TransformerPanel parent, TransformerPanelController transformerPanelController,
			final Controller controller, List<Transformer<CyNetwork, CyIdentifiable>> model,
			final CyServiceRegistrar serviceRegistrar) {
		this.parent = parent;
		this.transformerPanelController = transformerPanelController;
		this.model = model;
		this.serviceRegistrar = serviceRegistrar;
		
		separator = new CompositeSeparator();
		new DropTarget(separator, new DragHandler<>(separator, transformerPanelController, parent, null));
		
		ViewUtil.configureFilterView(this);
		setBorder(BorderFactory.createEmptyBorder());

		viewModels = new WeakHashMap<>();
		layout = new GroupLayout(this);
		setLayout(layout);

		addButton = createAddChainEntryButton();

		for (Transformer<CyNetwork, CyIdentifiable> transformer : model) {
			TransformerElementViewModel<TransformerPanel> viewModel = createViewModel(transformer);
			viewModels.put(transformer, viewModel);
		}
	}
	
	private TransformerElementViewModel<TransformerPanel> createViewModel(Transformer<CyNetwork,CyIdentifiable> transformer) {
		JComponent component = transformerPanelController.createView(parent, transformer, 0);
		TransformerElementViewModel<TransformerPanel> viewModel = new TransformerElementViewModel<>(component, transformerPanelController, parent);
		if(transformer instanceof ValidatableTransformer) {
			transformerPanelController.getValidationManager().register((ValidatableTransformer<CyNetwork,CyIdentifiable>)transformer, viewModel);
		}
		return viewModel;
	}
	
	
	JButton createAddChainEntryButton() {
		final JButton button = new JButton(IconManager.ICON_PLUS);
		button.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(11.0f));
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

	@Override
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
			
			checkBoxGroup.addGroup(
					layout.createParallelGroup()
					.addGroup(
						layout.createSequentialGroup()
						.addComponent(viewModel.deleteButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
						.addGap(4)
						.addComponent(viewModel.handle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
					.addGap(4)
					.addComponent(viewModel.warnIcon, Alignment.CENTER));
			
			viewGroup.addComponent(viewModel.view, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					 .addComponent(viewModel.separator);
			
			rows.addGroup(layout.createParallelGroup(Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
							.addGap(ViewUtil.INTERNAL_VERTICAL_PADDING)
							.addGroup(
								layout.createSequentialGroup().addGroup(
									layout.createParallelGroup()
									.addComponent(viewModel.deleteButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
									.addComponent(viewModel.handle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(4)
								.addComponent(viewModel.warnIcon)))
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
		TransformerElementViewModel<TransformerPanel> viewModel = createViewModel(transformer);
		addViewModel(transformer, viewModel);
	}
	
	
	public void addViewModel(Transformer<CyNetwork, CyIdentifiable> transformer, TransformerElementViewModel<TransformerPanel> viewModel) {
		model.add(transformer);
		viewModels.put(transformer, viewModel);
	}

	@Override
	public void removeTransformer(int index, boolean unregister) {
		Transformer<CyNetwork, CyIdentifiable> transformer = model.remove(index);
		TransformerElementViewModel<TransformerPanel> model = viewModels.remove(transformer);
		// always unregister
		if (model != null && model.view != null) {
			transformerPanelController.unregisterView(model.view);
		}
		
		if(transformer instanceof ValidatableTransformer) {
			transformerPanelController.getValidationManager().unregister((ValidatableTransformer<CyNetwork,CyIdentifiable>)transformer);
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

	@Override
	public int getTransformerCount() {
		return model.size();
	}

	@Override
	public Transformer<CyNetwork, CyIdentifiable> getTransformerAt(int index) {
		return model.get(index);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}
	
	
}
