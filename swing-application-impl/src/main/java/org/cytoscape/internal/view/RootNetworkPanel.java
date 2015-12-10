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

import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class RootNetworkPanel extends AbstractNetworkPanel<CyRootNetwork> {

	private ExpandCollapseButton expandCollapseBtn;
	private JLabel networkCountLabel;
	private JPanel subNetListPanel;
	
	private Map<CySubNetwork, SubNetworkPanel> items;
	
	public RootNetworkPanel(final RootNetworkPanelModel model, final CyServiceRegistrar serviceRegistrar) {
		super(model, serviceRegistrar);
	}
	
	public SubNetworkPanel addItem(final CySubNetwork network) {
		if (!getItems().containsKey(network)) {
			final SubNetworkPanelModel model = new SubNetworkPanelModel(network, serviceRegistrar);
			
			final SubNetworkPanel subNetPanel = new SubNetworkPanel(model, serviceRegistrar);
			subNetPanel.setAlignmentX(LEFT_ALIGNMENT);
			
			getSubNetListPanel().add(subNetPanel);
			getItems().put(network, subNetPanel);
			
			updateRootPanel();
		}
		
		return getItems().get(network);
	}
	
	public SubNetworkPanel removeItem(final CySubNetwork network) {
		final SubNetworkPanel subNetPanel = getItems().remove(network);
		
		if (subNetPanel != null)
			getSubNetListPanel().remove(subNetPanel);
		
		return subNetPanel;
	}
	
	public void removeAllItems() {
		getItems().clear();
		getSubNetListPanel().removeAll();
	}
	
	public SubNetworkPanel getItem(final CySubNetwork network) {
		return getItems().get(network);
	}
	
	public Collection<SubNetworkPanel> getAllItems() {
		return new ArrayList<>(getItems().values());
	}
	
	public boolean isEmpty() {
		return getItems().isEmpty();
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
	
	@Override
	public void update() {
		updateRootPanel();
		
		for (SubNetworkPanel snp : getAllItems())
			snp.update();
	}
	
	protected void updateRootPanel() {
		super.update();
		final int netCount = getModel().getSubNetworkCount();
		
		getNetworkCountLabel().setText("" + netCount);
		getNetworkCountLabel().setToolTipText(
				"This collection has " + netCount + " network" + (netCount == 1 ? "" : "s"));
	}
	
	@Override
	protected void init() {
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
	
	public Map<CySubNetwork, SubNetworkPanel> getItems() {
		return items != null ? items : (items = new WeakHashMap<>());
	}
}
