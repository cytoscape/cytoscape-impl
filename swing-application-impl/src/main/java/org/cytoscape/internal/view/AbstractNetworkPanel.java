package org.cytoscape.internal.view;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

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
