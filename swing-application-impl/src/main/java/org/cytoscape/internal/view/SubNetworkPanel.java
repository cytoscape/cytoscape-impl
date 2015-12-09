package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT_SQUARE;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class SubNetworkPanel extends JPanel {
	
	private JLabel viewIconLabel;
	private JLabel nameLabel;
	
	private SubNetworkPanelModel model;
	private volatile boolean selected;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public SubNetworkPanel(final SubNetworkPanelModel model, final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		setModel(model);
		init();
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
	
	public SubNetworkPanelModel getModel() {
		return model;
	}

	public void setModel(final SubNetworkPanelModel newModel) {
		final SubNetworkPanelModel oldModel = model;
		model = newModel;
		update();
        firePropertyChange("model", oldModel, newModel);
	}

	public void update() {
		final int viewCount = model.getViewCount();
		getViewIconLabel().setText(viewCount == 0 ? ICON_SHARE_ALT : ICON_SHARE_ALT_SQUARE);
		getViewIconLabel().setForeground(
				UIManager.getColor(viewCount == 0 ? "Label.disabledForeground" : "Label.foreground"));
		getViewIconLabel().setToolTipText((viewCount > 0 ? viewCount : "No") + " view" + (viewCount == 1 ? "" : "s"));
		
		getNameLabel().setText(model.getNetworkName());
		getNameLabel().setToolTipText(model.getNetworkName());
		
		updateSelection();
	}
	
	@Override
	public Dimension getMaximumSize() {
		final Dimension size = getPreferredSize();
	    size.width = Short.MAX_VALUE;
	    
	    return size;
	}
	
	private void init() {
		setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(!LookAndFeelUtil.isAquaLAF());
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(getViewIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addContainerGap()
		);
		layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getViewIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}
	
	private JLabel getViewIconLabel() {
		if (viewIconLabel == null) {
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			
			viewIconLabel = new JLabel();
			viewIconLabel.setFont(iconManager.getIconFont(16.0f));
		}
		
		return viewIconLabel;
	}
	
	private JLabel getNameLabel() {
		if (nameLabel == null) {
			nameLabel = new JLabel();
			nameLabel.setFont(nameLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		}
		
		return nameLabel;
	}
	
	private void updateSelection() {
		setBackground(UIManager.getColor(selected ? "Table.selectionBackground" : "Table.background"));
	}
}
