package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.ICON_CHECK_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_CROSSHAIRS;
import static org.cytoscape.util.swing.IconManager.ICON_EXTERNAL_LINK_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_EYE_SLASH;
import static org.cytoscape.util.swing.IconManager.ICON_TH;
import static org.cytoscape.util.swing.IconManager.ICON_THUMB_TACK;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;

@SuppressWarnings("serial")
public class NetworkViewContainer extends SimpleRootPaneContainer {
	
	private final CyNetworkView networkView;
	private final RenderingEngineFactory<CyNetwork> engineFactory;
	private final RenderingEngine<CyNetwork> renderingEngine;

	private SimpleRootPaneContainer visualizationContainer;
	
	private JPanel toolBar;
	private JButton gridModeButton;
	private JButton detachViewButton;
	private JButton reattachViewButton;
	private JLabel viewTitleLabel;
	private JTextField viewTitleTextField;
	private JPanel infoPanel;
	private JLabel selectionIconLabel;
	private JLabel hiddenIconLabel;
	private JLabel selectionLabel;
	private JLabel hiddenLabel;
	private JButton birdsEyeViewButton;
	private BirdsEyeViewPanel birdsEyeViewPanel;
	
	final JSeparator sep1 = new JSeparator(JSeparator.VERTICAL);
	
    private boolean detached;
    private boolean comparing;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewContainer(
			final CyNetworkView networkView,
			final RenderingEngineFactory<CyNetwork> engineFactory,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.networkView = networkView;
		this.engineFactory = engineFactory;
		this.serviceRegistrar = serviceRegistrar;
		
		setName(ViewUtil.createUniqueKey(networkView));
		init();
		
		renderingEngine = engineFactory.createRenderingEngine(getVisualizationContainer(), networkView);
	}
	
	public boolean isDetached() {
		return detached;
	}
	
	public void setDetached(boolean detached) {
		this.detached = detached;
		
		if (detached)
			this.comparing = false;
	}
	
	public boolean isComparing() {
		return comparing;
	}
	
	public void setComparing(boolean comparing) {
		this.comparing = comparing;
		
		if (comparing)
			this.detached = false;
	}
	
	public void update() {
		updateTollBar();
		
		if (getBirdsEyeViewPanel().isVisible())
			getBirdsEyeViewPanel().update();
	}
	
	protected void updateTollBar() {
		getGridModeButton().setVisible(!isDetached() && !isComparing());
		getDetachViewButton().setVisible(!isDetached() && !isComparing());
		getReattachViewButton().setVisible(isDetached());
		sep1.setVisible(!isComparing());
		
		final CyNetworkView view = getNetworkView();
		getViewTitleLabel().setText(view != null ? ViewUtil.getTitle(view) : "");
		
		if (getInfoPanel().isVisible()) {
			// Selected nodes/edges info
			final int sn = view.getModel().getDefaultNodeTable().countMatchingRows(CyNetwork.SELECTED, Boolean.TRUE);
			final int se = view.getModel().getDefaultEdgeTable().countMatchingRows(CyNetwork.SELECTED, Boolean.TRUE);
			getSelectionLabel().setText(sn + " - " + se);
			
			final String sTooltip = createInfoToolTipText(sn, se, "selected");
			getSelectionIconLabel().setToolTipText(sTooltip);
			getSelectionLabel().setToolTipText(sTooltip);
			
			getSelectionIconLabel().setForeground(
					sn > 0 || se > 0 ? LookAndFeelUtil.getInfoColor() : UIManager.getColor("Label.disabledForeground"));
			
			// Hidden nodes/edges info
			final int hn = ViewUtil.getHiddenNodeCount(view);
			final int he = ViewUtil.getHiddenEdgeCount(view);
			getHiddenLabel().setText(hn + " - " + he);
			
			final String hTooltip = createInfoToolTipText(hn, he, "hidden");
			getHiddenIconLabel().setToolTipText(hTooltip);
			getHiddenLabel().setToolTipText(hTooltip);
			
			getHiddenIconLabel().setForeground(
					hn > 0 || he > 0 ? LookAndFeelUtil.getWarnColor() : UIManager.getColor("Label.disabledForeground"));
		}
		
		updateBirdsEyeButton();
		getToolBar().updateUI();
	}

	private void updateBirdsEyeButton() {
		final boolean bevVisible = getBirdsEyeViewPanel().isVisible();
		getBirdsEyeViewButton().setToolTipText((bevVisible ? "Hide" : "Show") + " Navigator");
		getBirdsEyeViewButton().setForeground(UIManager.getColor(bevVisible ? "Focus.color" : "Button.foreground"));
	}
	
	public void dispose() {
		getRootPane().getLayeredPane().removeAll();
		getRootPane().getContentPane().removeAll();
		
		getBirdsEyeViewPanel().dispose();
	}
	
	private void init() {
		final JPanel glassPane = new JPanel();
		
		{
			final GroupLayout layout = new GroupLayout(glassPane);
			glassPane.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getBirdsEyeViewPanel(), 10, 200, 200)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getBirdsEyeViewPanel(), 10, 200, 200)
					.addGap(getToolBar().getPreferredSize().height)
			);
		}
		
		getRootPane().setGlassPane(glassPane);
		glassPane.setOpaque(false);
		glassPane.setVisible(true);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getVisualizationContainer(), BorderLayout.CENTER);
		getContentPane().add(getToolBar(), BorderLayout.SOUTH);
	}
	
	protected RenderingEngine<CyNetwork> getRenderingEngine() {
		return renderingEngine;
	}
	
	protected CyNetworkView getNetworkView() {
		return networkView;
	}
	
	private SimpleRootPaneContainer getVisualizationContainer() {
		if (visualizationContainer == null) {
			visualizationContainer = new SimpleRootPaneContainer();
		}
		
		return visualizationContainer;
	}
	
	JPanel getToolBar() {
		if (toolBar == null) {
			toolBar = new JPanel();
			toolBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			
			final GroupLayout layout = new GroupLayout(toolBar);
			toolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getDetachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getReattachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleTextField(), 100, 260, 320)
					.addGap(0, 10, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getInfoPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getBirdsEyeViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, false)
					.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDetachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getReattachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getViewTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getInfoPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getBirdsEyeViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return toolBar;
	}
	
	JButton getGridModeButton() {
		if (gridModeButton == null) {
			gridModeButton = new JButton(ICON_TH);
			gridModeButton.setToolTipText("Show Thumbnails");
			styleToolBarButton(gridModeButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return gridModeButton;
	}
	
	JButton getDetachViewButton() {
		if (detachViewButton == null) {
			detachViewButton = new JButton(ICON_EXTERNAL_LINK_SQUARE);
			detachViewButton.setToolTipText("Detach Network View");
			styleToolBarButton(detachViewButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return detachViewButton;
	}
	
	JButton getReattachViewButton() {
		if (reattachViewButton == null) {
			reattachViewButton = new JButton(ICON_THUMB_TACK);
			reattachViewButton.setToolTipText("Reattach Network View");
			styleToolBarButton(reattachViewButton, serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
		}
		
		return reattachViewButton;
	}
	
	JLabel getViewTitleLabel() {
		if (viewTitleLabel == null) {
			viewTitleLabel = new JLabel();
			viewTitleLabel.setToolTipText("Click to change the title...");
			viewTitleLabel.setFont(viewTitleLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			viewTitleLabel.setMinimumSize(new Dimension(viewTitleLabel.getPreferredSize().width,
					getViewTitleTextField().getPreferredSize().height));
			viewTitleLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					showViewTitleEditor();
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					viewTitleLabel.setForeground(UIManager.getColor("Focus.color"));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					viewTitleLabel.setForeground(UIManager.getColor("Label.foreground"));
				}
			});
		}
		
		return viewTitleLabel;
	}
	
	JTextField getViewTitleTextField() {
		if (viewTitleTextField == null) {
			viewTitleTextField = new JTextField();
			viewTitleTextField.putClientProperty("JComponent.sizeVariant", "mini"); // Aqua (Mac OS X) only
			viewTitleTextField.setVisible(false);
		}
		
		return viewTitleTextField;
	}
	
	private JPanel getInfoPanel() {
		if (infoPanel == null) {
			infoPanel = new JPanel();
			infoPanel.setBorder(LookAndFeelUtil.createPanelBorder());
			
			if (LookAndFeelUtil.isAquaLAF())
				infoPanel.setOpaque(false);
			
			final JSeparator sep = new JSeparator(JSeparator.VERTICAL);
			
			final GroupLayout layout = new GroupLayout(infoPanel);
			infoPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getSelectionIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getHiddenIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getHiddenLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, false)
					.addComponent(getSelectionIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getHiddenIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getHiddenLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return infoPanel;
	}
	
	private JLabel getSelectionIconLabel() {
		if (selectionIconLabel == null) {
			selectionIconLabel = new JLabel(ICON_CHECK_SQUARE);
			selectionIconLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(12.0f));
		}
		
		return selectionIconLabel;
	}
	
	private JLabel getHiddenIconLabel() {
		if (hiddenIconLabel == null) {
			hiddenIconLabel = new JLabel(ICON_EYE_SLASH);
			hiddenIconLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(16.0f));
		}
		
		return hiddenIconLabel;
	}
	
	private JLabel getSelectionLabel() {
		if (selectionLabel == null) {
			selectionLabel = new JLabel();
			selectionLabel.setFont(selectionLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			selectionLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return selectionLabel;
	}
	
	private JLabel getHiddenLabel() {
		if (hiddenLabel == null) {
			hiddenLabel = new JLabel();
			hiddenLabel.setFont(selectionLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			hiddenLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return hiddenLabel;
	}
	
	private JButton getBirdsEyeViewButton() {
		if (birdsEyeViewButton == null) {
			birdsEyeViewButton = new JButton(ICON_CROSSHAIRS);
			styleToolBarButton(birdsEyeViewButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
			
			birdsEyeViewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getBirdsEyeViewPanel().setVisible(!getBirdsEyeViewPanel().isVisible());
					updateBirdsEyeButton();
					
					if (getBirdsEyeViewPanel().isVisible())
						getBirdsEyeViewPanel().update();
				}
			});
		}
		
		return birdsEyeViewButton;
	}
	
	private BirdsEyeViewPanel getBirdsEyeViewPanel() {
		if (birdsEyeViewPanel == null) {
			birdsEyeViewPanel = new BirdsEyeViewPanel(getNetworkView(), serviceRegistrar);
			birdsEyeViewPanel.setBorder(
					BorderFactory.createMatteBorder(1, 1, 0, 0, UIManager.getColor("Separator.foreground")));
			birdsEyeViewPanel.setVisible(true);
		}
		
		return birdsEyeViewPanel;
	}
	
	private void showViewTitleEditor() {
		getViewTitleTextField().setText(getViewTitleLabel().getText());
		getViewTitleLabel().setVisible(false);
		getViewTitleTextField().setVisible(true);
		getViewTitleTextField().requestFocusInWindow();
	}
	
	@Override
	public String toString() {
		return networkView.toString();
	}
	
	private static String createInfoToolTipText(final int nodes, final int edges, final String adjective) {
		String tooltip = "<html>";
		
		if (nodes > 0 || edges > 0) {
			if (nodes > 0)
				tooltip += ( "<b>" + nodes + "</b> " + adjective + " node" + (nodes > 1 ? "s" : "") );
			if (edges > 0)
				tooltip += (
						(nodes > 0 ? "<br>" : "") + 
						"<b>" + edges + "</b> " + adjective + " edge" + (edges > 1 ? "s" : "")
				);
		} else {
			tooltip += "No " + adjective + " nodes or edges";
		}
		
		tooltip += "</html>";
		return tooltip;
	}
}
