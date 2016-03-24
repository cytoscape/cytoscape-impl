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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;

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
public class NetworkViewContainer extends SimpleRootPaneContainer {
	
	private final CyNetworkView networkView;
	private final RenderingEngineFactory<CyNetwork> engineFactory;
	private final RenderingEngine<CyNetwork> renderingEngine;

	private SimpleRootPaneContainer visualizationContainer;
	
	private JPanel toolBar;
	private JButton gridModeButton;
	private JButton detachViewButton;
	private JButton reattachViewButton;
	private JLabel currentLabel;
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
	final JSeparator sep2 = new JSeparator(JSeparator.VERTICAL);
	final JSeparator sep3 = new JSeparator(JSeparator.VERTICAL);
	
    private boolean detached;
    private boolean comparing;
    private boolean current;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewContainer(
			final CyNetworkView networkView,
			final boolean current,
			final RenderingEngineFactory<CyNetwork> engineFactory,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.networkView = networkView;
		this.current = current;
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
		
		if (comparing) {
			this.detached = false;
			
			// Hide Navigator when starting Compare Mode
			if (getBirdsEyeViewPanel().isVisible())
				getBirdsEyeViewButton().doClick();
		}
	}
	
	public boolean isCurrent() {
		return current;
	}
	
	public void setCurrent(final boolean newValue) {
		if (current != newValue) {
			final boolean oldValue = current;
			current = newValue;
			
			updateCurrentLabel();
			firePropertyChange("current", oldValue, newValue);
		}
	}
	
	public void update() {
		getVisualizationContainer().repaint();
		updateTollBar();
		
		if (getBirdsEyeViewPanel().isVisible())
			getBirdsEyeViewPanel().update();
	}
	
	protected void updateTollBar() {
		getGridModeButton().setVisible(!isDetached() && !isComparing());
		getDetachViewButton().setVisible(!isDetached() && !isComparing());
		getReattachViewButton().setVisible(isDetached());
		sep1.setVisible(!isComparing());
		getCurrentLabel().setVisible(isComparing());
		
		final CyNetworkView view = getNetworkView();
		getViewTitleLabel().setText(view != null ? ViewUtil.getTitle(view) : "");
		getViewTitleLabel().setToolTipText(view != null ? ViewUtil.getTitle(view) : null);
		
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
		
		if (isComparing())
			updateCurrentLabel();
		
		updateBirdsEyeButton();
		getToolBar().updateUI();
	}
	
	protected void updateCurrentLabel() {
		getCurrentLabel().setForeground(isCurrent() ? UIManager.getColor("Focus.color") : new Color(0, 0, 0, 0));
		getCurrentLabel().setToolTipText(isCurrent() ? "Current Network" : null);
	}
	
	private void updateBirdsEyeButton() {
		final boolean bevVisible = getBirdsEyeViewPanel().isVisible();
		getBirdsEyeViewButton().setToolTipText((bevVisible ? "Hide" : "Show") + " Navigator (N)");
		getBirdsEyeViewButton().setForeground(UIManager.getColor(bevVisible ? "Focus.color" : "Button.foreground"));
	}
	
	public void dispose() {
		getRootPane().getLayeredPane().removeAll();
		getRootPane().getContentPane().removeAll();
		
		getBirdsEyeViewPanel().dispose();
	}
	
	private void init() {
		setFocusable(true);
		setRequestFocusEnabled(true);
		
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
		
		setKeyBindings(this);
		setKeyBindings(getRootPane());
		
		updateTollBar();
	}
	
	protected RenderingEngine<CyNetwork> getRenderingEngine() {
		return renderingEngine;
	}
	
	protected CyNetworkView getNetworkView() {
		return networkView;
	}
	
	protected SimpleRootPaneContainer getVisualizationContainer() {
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
					.addComponent(getCurrentLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getViewTitleLabel())
					.addComponent(getViewTitleTextField(), 100, 260, 320)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(sep2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getInfoPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getBirdsEyeViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, false)
					.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDetachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getReattachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getCurrentLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getInfoPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep3, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getBirdsEyeViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return toolBar;
	}
	
	JButton getGridModeButton() {
		if (gridModeButton == null) {
			gridModeButton = new JButton(ICON_TH);
			gridModeButton.setToolTipText("Show Grid (G)");
			styleToolBarButton(gridModeButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return gridModeButton;
	}
	
	JButton getDetachViewButton() {
		if (detachViewButton == null) {
			detachViewButton = new JButton(ICON_EXTERNAL_LINK_SQUARE);
			detachViewButton.setToolTipText("Detach View");
			styleToolBarButton(detachViewButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return detachViewButton;
	}
	
	JButton getReattachViewButton() {
		if (reattachViewButton == null) {
			reattachViewButton = new JButton(ICON_THUMB_TACK);
			reattachViewButton.setToolTipText("Reattach View");
			styleToolBarButton(reattachViewButton, serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
		}
		
		return reattachViewButton;
	}
	
	JLabel getCurrentLabel() {
		if (currentLabel == null) {
			currentLabel = new JLabel(IconManager.ICON_CIRCLE); // Just to get the preferred size with the icon font
			currentLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(10.0f));
			currentLabel.setMinimumSize(currentLabel.getPreferredSize());
			currentLabel.setMaximumSize(currentLabel.getPreferredSize());
			currentLabel.setSize(currentLabel.getPreferredSize());
			currentLabel.setForeground(UIManager.getColor("Focus.color"));
		}
		
		return currentLabel;
	}
	
	JLabel getViewTitleLabel() {
		if (viewTitleLabel == null) {
			viewTitleLabel = new JLabel();
//			viewTitleLabel.setToolTipText("Click to change the title...");
			viewTitleLabel.setFont(viewTitleLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			viewTitleLabel.setMinimumSize(new Dimension(viewTitleLabel.getPreferredSize().width,
					getViewTitleTextField().getPreferredSize().height));
// TODO Uncomment when multiple views support is enabled
//			viewTitleLabel.addMouseListener(new MouseAdapter() {
//				@Override
//				public void mouseClicked(MouseEvent e) {
//					showViewTitleEditor();
//				}
//			});
//			viewTitleLabel.setCursor(new Cursor(Cursor.TEXT_CURSOR));
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
			
			styleToolBarButton(birdsEyeViewButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f),
					false);
			
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
	
	BirdsEyeViewPanel getBirdsEyeViewPanel() {
		if (birdsEyeViewPanel == null) {
			birdsEyeViewPanel = new BirdsEyeViewPanel(getNetworkView(), serviceRegistrar);
			birdsEyeViewPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(1, 1, 0, 0, UIManager.getColor("Table.background")),
					BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager.getColor("Focus.color"))
			));
			birdsEyeViewPanel.setFocusable(true);
			birdsEyeViewPanel.setRequestFocusEnabled(true);
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
	
	private void setKeyBindings(final JComponent comp) {
		final ActionMap actionMap = comp.getActionMap();
		final InputMap inputMap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), KeyAction.VK_G);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), KeyAction.VK_N);
		
		actionMap.put(KeyAction.VK_G, new KeyAction(KeyAction.VK_G));
		actionMap.put(KeyAction.VK_N, new KeyAction(KeyAction.VK_N));
	}
	
	private class KeyAction extends AbstractAction {

		final static String VK_G = "VK_G";
		final static String VK_N = "VK_N";
		
		KeyAction(final String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			
			if (focusOwner instanceof JTextComponent || focusOwner instanceof JTable ||
					!NetworkViewContainer.this.getContentPane().isVisible())
				return; // We don't want to steal the key event from these components
			
			final String cmd = e.getActionCommand();
			
			if (cmd.equals(VK_G)) {
				getGridModeButton().doClick();
			} else if (cmd.equals(VK_N)) {
				// Toggle Navigator (bird's eye view) visibility state
				getBirdsEyeViewButton().doClick();
			}
		}
	}
}
