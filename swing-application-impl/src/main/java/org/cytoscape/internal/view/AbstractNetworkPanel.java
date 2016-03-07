package org.cytoscape.internal.view;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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
public abstract class AbstractNetworkPanel<T extends CyNetwork> extends JPanel {

	private JLabel nameLabel;
	
	private boolean selected;
	private AbstractNetworkPanelModel<T> model;
	
	protected final CyServiceRegistrar serviceRegistrar;

	protected AbstractNetworkPanel(final AbstractNetworkPanelModel<T> model, final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		setModel(model);
		init();
	}

	public AbstractNetworkPanelModel<T> getModel() {
		return model;
	}

	public void setModel(final AbstractNetworkPanelModel<T> newModel) {
		if (newModel == null)
			throw new IllegalArgumentException("'newModel' must not be null.");
		
		final AbstractNetworkPanelModel<T> oldModel = model;
		model = newModel;
		update();
        firePropertyChange("model", oldModel, newModel);
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean newValue) {
		if (selected != newValue) {
			selected = newValue;
			updateSelection();
			firePropertyChange("selected", !newValue, newValue);
		}
	}
	
	public void update() {
		updateSelection();
		updateNameLabel();
	}
	
	protected JLabel getNameLabel() {
		if (nameLabel == null) {
			nameLabel = new JLabel();
			nameLabel.setFont(nameLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		}
		
		return nameLabel;
	}
	
	protected void updateSelection() {
		setBackground(UIManager.getColor(selected ? "Table.selectionBackground" : "Table.background"));
	}
	
	protected void updateNameLabel() {
		getNameLabel().setText(model.getNetworkName());
		getNameLabel().setToolTipText(model.getNetworkName());
	}
	
	protected abstract void init();
	
	@Override
	public String toString() {
		return getNameLabel().getText();
	}
}
