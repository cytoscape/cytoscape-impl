package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class RootNetworkPanel extends JPanel {

	private ExpandCollapseButton expandCollapseBtn;
	private JLabel nameLabel;
	private JLabel networkCountLabel;
	private JPanel subNetListPanel;
	
	private RootNetworkPanelModel model;
	private final Map<CySubNetwork, SubNetworkPanel> items = new WeakHashMap<>();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public RootNetworkPanel(final RootNetworkPanelModel model, final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		setModel(model);
		init();
	}
	
	public SubNetworkPanel addItem(final CySubNetwork network) {
		if (!items.containsKey(network)) {
			final SubNetworkPanelModel model = new SubNetworkPanelModel(network, serviceRegistrar);
			
			final SubNetworkPanel subNetPanel = new SubNetworkPanel(model, serviceRegistrar);
			subNetPanel.setAlignmentX(LEFT_ALIGNMENT);
			
			getSubNetListPanel().add(subNetPanel);
			items.put(network, subNetPanel);
			
			updateRootPanel();
		}
		
		return items.get(network);
	}
	
	public SubNetworkPanel removeItem(final CySubNetwork network) {
		final SubNetworkPanel subNetPanel = items.remove(network);
		
		if (subNetPanel != null)
			getSubNetListPanel().remove(subNetPanel);
		
		return subNetPanel;
	}
	
	public void removeAllItems() {
		items.clear();
		getSubNetListPanel().removeAll();
	}
	
	public SubNetworkPanel getItem(final CySubNetwork network) {
		return items.get(network);
	}
	
	public Collection<SubNetworkPanel> getAllItems() {
		return new ArrayList<>(items.values());
	}
	
	public boolean isEmpty() {
		return items.isEmpty();
	}
	
	public void expand() {
		if (!isExpanded()) {
			getSubNetListPanel().setVisible(true);
			firePropertyChange("expanded", false, true);
		}
	}
	
	public void collapse() {
		if (isExpanded()) {
			getSubNetListPanel().setVisible(false);
			firePropertyChange("expanded", true, false);
		}
	}
	
	public boolean isExpanded() {
		return getSubNetListPanel().isVisible();
	}
	
	public RootNetworkPanelModel getModel() {
		return model;
	}
	
	public void setModel(final RootNetworkPanelModel newModel) {
		final RootNetworkPanelModel oldModel = model;
		model = newModel;
		update();
        firePropertyChange("model", oldModel, newModel);
	}
	
	public void update() {
		updateRootPanel();
		
		for (SubNetworkPanel snp : getAllItems())
			snp.update();
	}
	
	private void updateRootPanel() {
		getNameLabel().setText(model.getRootNetworkName());
		getNameLabel().setToolTipText(model.getRootNetworkName());
		
		final int netCount = model.getSubNetworkCount();
		
		getNetworkCountLabel().setText("" + netCount);
		getNetworkCountLabel().setToolTipText(
				"This collection has " + netCount + " network" + (netCount == 1 ? "" : "s"));
	}
	
	private void init() {
		setBackground(UIManager.getColor("Table.background"));
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(!LookAndFeelUtil.isAquaLAF());
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(getExpandCollapseBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGap(0, 10, Short.MAX_VALUE)
							.addComponent(getNetworkCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addContainerGap()
					)
					.addComponent(getSubNetListPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(CENTER, true)
						.addComponent(getExpandCollapseBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getNetworkCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(getSubNetListPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}
	
	private ExpandCollapseButton getExpandCollapseBtn() {
		if (expandCollapseBtn == null) {
			expandCollapseBtn = new ExpandCollapseButton(isExpanded(), new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent ae) {
					if (isExpanded())
						collapse();
					else
						expand();
				}
			}, serviceRegistrar);
		}
		
		return expandCollapseBtn;
	}
	
	private JLabel getNameLabel() {
		if (nameLabel == null) {
			nameLabel = new JLabel();
			nameLabel.setFont(nameLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		}
		
		return nameLabel;
	}
	
	private JLabel getNetworkCountLabel() {
		if (networkCountLabel == null) {
			networkCountLabel = new JLabel();
			networkCountLabel.setFont(networkCountLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			networkCountLabel.setHorizontalAlignment(JLabel.RIGHT);
			networkCountLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return networkCountLabel;
	}
	
	private JPanel getSubNetListPanel() {
		if (subNetListPanel == null) {
			subNetListPanel = new JPanel();
			subNetListPanel.setBackground(UIManager.getColor("Table.background"));
			subNetListPanel.setBorder(
					BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("TableHeader.background")));
			subNetListPanel.setVisible(false);
			subNetListPanel.setLayout(new BoxLayout(subNetListPanel, BoxLayout.Y_AXIS));
			
			subNetListPanel.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(final ComponentEvent ce) {
					if (!getExpandCollapseBtn().isSelected())
						getExpandCollapseBtn().setSelected(true);
				}
				@Override
				public void componentHidden(final ComponentEvent ce) {
					if (getExpandCollapseBtn().isSelected())
						getExpandCollapseBtn().setSelected(false);
				}
			});
		}
		
		return subNetListPanel;
	}
}
