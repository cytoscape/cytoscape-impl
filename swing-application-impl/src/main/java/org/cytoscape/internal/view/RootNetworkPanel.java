package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class RootNetworkPanel extends AbstractNetworkPanel<CyRootNetwork> {

	protected static final String PARENT_NETWORK_COLUMN = "__parentNetwork.SUID";
	
	private ExpandCollapseButton expandCollapseBtn;
	private JLabel networkCountLabel;
	private JLabel nodesLabel;
	private JLabel edgesLabel;
	private JPanel headerPanel;
	private JPanel subNetInfoHeaderPanel;
	private JPanel subNetListPanel;
	
	private Map<CySubNetwork, SubNetworkPanel> items;
	private boolean showNodeEdgeCount;
	
	public RootNetworkPanel(final RootNetworkPanelModel model, final boolean showNodeEdgeCount,
			final CyServiceRegistrar serviceRegistrar) {
		super(model, serviceRegistrar);
		this.showNodeEdgeCount = showNodeEdgeCount;
	}
	
	public SubNetworkPanel addItem(final CySubNetwork network) {
		if (!getItems().containsKey(network)) {
			final SubNetworkPanelModel model = new SubNetworkPanelModel(network, serviceRegistrar);
			
			final SubNetworkPanel subNetPanel = new SubNetworkPanel(model, serviceRegistrar);
			subNetPanel.setAlignmentX(LEFT_ALIGNMENT);
			
			getSubNetListPanel().add(subNetPanel);
			getItems().put(network, subNetPanel);
			
			updateRootPanel();
			updateCountInfo();
		}
		
		return getItems().get(network);
	}
	
	public SubNetworkPanel removeItem(final CySubNetwork network) {
		final SubNetworkPanel subNetPanel = getItems().remove(network);
		
		if (subNetPanel != null) {
			getSubNetListPanel().remove(subNetPanel);
			updateRootPanel();
			updateCountInfo();
		}
		
		return subNetPanel;
	}
	
	public void removeAllItems() {
		getItems().clear();
		getSubNetListPanel().removeAll();
	}
	
	public SubNetworkPanel getItem(final CySubNetwork network) {
		return getItems().get(network);
	}
	
	public List<SubNetworkPanel> getAllItems() {
		return new ArrayList<>(getItems().values());
	}
	
	public boolean isEmpty() {
		return getItems().isEmpty();
	}
	
	public void expand() {
		if (!isExpanded()) {
			getSubNetInfoHeaderPanel().setVisible(true);
			getSubNetListPanel().setVisible(true);
			firePropertyChange("expanded", false, true);
		}
	}
	
	public void collapse() {
		if (isExpanded()) {
			getSubNetInfoHeaderPanel().setVisible(false);
			getSubNetListPanel().setVisible(false);
			firePropertyChange("expanded", true, false);
		}
	}
	
	public boolean isExpanded() {
		return getSubNetListPanel().isVisible();
	}
	
	public void setShowNodeEdgeCount(final boolean show) {
		showNodeEdgeCount = show;
		updateCountInfo();
	}
	
	@Override
	public void update() {
		updateRootPanel();
		
		for (SubNetworkPanel snp : getItems().values()) {
			int depth = getDepth(snp.getModel().getNetwork());
			snp.setDepth(depth);
			snp.update();
		}
		
		updateCountInfo();
	}
	
	protected void updateRootPanel() {
		super.update();
		final int netCount = getItems().values().size();
		
		getNetworkCountLabel().setText("" + netCount);
		getNetworkCountLabel().setToolTipText(
				"This collection has " + netCount + " network" + (netCount == 1 ? "" : "s"));
	}
	
	protected void updateItemsDepth() {
		for (SubNetworkPanel snp : getItems().values()) {
			int depth = getDepth(snp.getModel().getNetwork());
			System.out.println(snp.getModel().getNetwork() +  " >> " + depth);
			snp.setDepth(depth);
		}
	}
	
	protected void updateCountInfo() {
		getSubNetInfoHeaderPanel().setVisible(showNodeEdgeCount);
		
		int nodeLabelWidth = getNodesLabel().getPreferredSize().width;
		int edgeLabelWidth = getEdgesLabel().getPreferredSize().width;
		
		for (SubNetworkPanel snp : getItems().values()) {
			snp.getNodeCountLabel().setVisible(showNodeEdgeCount);
			snp.getEdgeCountLabel().setVisible(showNodeEdgeCount);
			
			if (showNodeEdgeCount) {
				// Update node/edge count label text
				snp.updateCountLabels();
				// Get max label width
				nodeLabelWidth = Math.max(nodeLabelWidth, snp.getNodeCountLabel().getPreferredSize().width);
				edgeLabelWidth = Math.max(edgeLabelWidth, snp.getEdgeCountLabel().getPreferredSize().width);
			}
		}
		
		if (!showNodeEdgeCount)
			return;
		
		// Apply max width values to all labels so they align properly
		final Dimension ntd = new Dimension(nodeLabelWidth, getNodesLabel().getPreferredSize().height); // node title
		final Dimension etd = new Dimension(edgeLabelWidth, getEdgesLabel().getPreferredSize().height); // edge title
		getNodesLabel().setPreferredSize(ntd);
		getEdgesLabel().setPreferredSize(etd);
		
		for (SubNetworkPanel snp : getItems().values()) {
			final Dimension nd = new Dimension(nodeLabelWidth, snp.getNodeCountLabel().getPreferredSize().height);
			final Dimension ed = new Dimension(edgeLabelWidth, snp.getEdgeCountLabel().getPreferredSize().height);
			snp.getNodeCountLabel().setPreferredSize(nd);
			snp.getEdgeCountLabel().setPreferredSize(ed);
		}
	}
	
	@Override
	protected void updateSelection() {
		final Color c = UIManager.getColor(isSelected() ? "Table.selectionBackground" : "Table.background");
		getHeaderPanel().setBackground(c);
		getSubNetInfoHeaderPanel().setBackground(c);
	}
	
	@Override
	protected void init() {
		setBackground(UIManager.getColor("Table.background"));
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(getHeaderPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getSubNetListPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getHeaderPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getSubNetListPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
	
	protected JLabel getNetworkCountLabel() {
		if (networkCountLabel == null) {
			networkCountLabel = new JLabel();
			networkCountLabel.setFont(networkCountLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			networkCountLabel.setHorizontalAlignment(JLabel.RIGHT);
			networkCountLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return networkCountLabel;
	}
	
	protected JLabel getNodesLabel() {
		if (nodesLabel == null) {
			nodesLabel = new JLabel("Nodes");
			nodesLabel.setFont(nodesLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()).deriveFont(Font.BOLD));
			nodesLabel.setHorizontalAlignment(JLabel.RIGHT);
			nodesLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return nodesLabel;
	}
	
	protected JLabel getEdgesLabel() {
		if (edgesLabel == null) {
			edgesLabel = new JLabel("Edges");
			edgesLabel.setFont(edgesLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()).deriveFont(Font.BOLD));
			edgesLabel.setHorizontalAlignment(JLabel.RIGHT);
			edgesLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return edgesLabel;
	}
	
	protected JPanel getHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = new JPanel();
			headerPanel.setBackground(UIManager.getColor("Table.background"));
			
			final GroupLayout layout = new GroupLayout(headerPanel);
			headerPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getExpandCollapseBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGap(0, 10, Short.MAX_VALUE)
							.addComponent(getNetworkCountLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addContainerGap()
					)
					.addComponent(getSubNetInfoHeaderPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, true)
							.addComponent(getExpandCollapseBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getNetworkCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getSubNetInfoHeaderPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return headerPanel;
	}
	
	private JPanel getSubNetInfoHeaderPanel() {
		if (subNetInfoHeaderPanel == null) {
			subNetInfoHeaderPanel = new JPanel();
			subNetInfoHeaderPanel.setBackground(UIManager.getColor("Table.background"));
			
			final GroupLayout layout = new GroupLayout(subNetInfoHeaderPanel);
			subNetInfoHeaderPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getNodesLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getEdgesLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getNodesLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getEdgesLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return subNetInfoHeaderPanel;
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
	
	private Map<CySubNetwork, SubNetworkPanel> getItems() {
		return items != null ? items : (items = new LinkedHashMap<>());
	}
	
	private int getDepth(final CySubNetwork net) {
		int depth = -1;
		CySubNetwork parent = net;
		
		do {
			parent = getParent(parent);
			depth++;
		} while (parent != null);
		
		return depth;
	}
	
	private CySubNetwork getParent(final CySubNetwork net) {
		final CyTable hiddenTable = net.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		final Long suid = hiddenTable.getRow(net.getSUID()).get(PARENT_NETWORK_COLUMN, Long.class);
		
		if (suid != null) {
			final CyNetwork parent = serviceRegistrar.getService(CyNetworkManager.class).getNetwork(suid);
			
			if (parent instanceof CySubNetwork)
				return (CySubNetwork) parent;
		}
		
		return null;
	}
}
