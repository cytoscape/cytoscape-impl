package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.ICON_COG;
import static org.cytoscape.util.swing.IconManager.ICON_EXTERNAL_LINK_SQUARE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.cytoscape.internal.util.Util;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;

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
public class NetworkViewComparisonPanel extends JPanel {

	public enum Arrangement {
		AUTO("Auto Arrange"),
		GRID("Grid"),
		HORIZONTAL("Side by Side"),
		VERTICAL("Vertical Stack");
		
		private final String name;
		
		Arrangement(final String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public static final int HORIZONTAL = JSplitPane.HORIZONTAL_SPLIT;
	public static final int VERTICAL = JSplitPane.VERTICAL_SPLIT;
	
	private static final float ICON_FONT_SIZE = 22.0f;
	
	private JPanel gridPanel;
	private JPanel comparisonToolBar;
	private JButton detachComparedViewsButton;
	private JButton optionsBtn;
	private final GridViewTogglePanel gridViewTogglePanel;
	
	private final Map<CyNetworkView, ViewPanel> viewPanels = new LinkedHashMap<>();
	private final Map<CyNetworkView, JRootPane> rootPanes = new LinkedHashMap<>();
	
	private CyNetworkView currentNetworkView;
	private Arrangement arrangement = Arrangement.AUTO;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public NetworkViewComparisonPanel(
			final GridViewToggleModel gridViewToggleModel,
			final Set<NetworkViewContainer> containers,
			final CyNetworkView currentNetworkView,
			final CyServiceRegistrar serviceRegistrar
	) {
		if (containers == null || containers.isEmpty())
			throw new IllegalArgumentException("'containers' must not be null or empty.");
		
		this.currentNetworkView = currentNetworkView;
		this.serviceRegistrar = serviceRegistrar;
		
		gridViewTogglePanel = new GridViewTogglePanel(gridViewToggleModel, serviceRegistrar);
		
		for (NetworkViewContainer vc : containers) {
			viewPanels.put(vc.getNetworkView(), new ViewPanel(vc));
			rootPanes.put(vc.getNetworkView(), vc.getRootPane());
		}
		
		init();
	}
	
	public CyNetworkView getCurrentNetworkView() {
		return currentNetworkView;
	}
	
	public void setCurrentNetworkView(final CyNetworkView newValue) {
		if (newValue != currentNetworkView) {
			final CyNetworkView oldValue = currentNetworkView;
			currentNetworkView = newValue;
			update();
			
			firePropertyChange("currentNetworkView", oldValue, newValue);
		}
	}
	
	public Set<CyNetworkView> getAllNetworkViews() {
		final Set<CyNetworkView> set = new LinkedHashSet<>();
		
		for (ViewPanel vp : viewPanels.values())
			set.add(vp.getNetworkView());
		
		return set;
	}
	
	public Set<NetworkViewContainer> getAllContainers() {
		final Set<NetworkViewContainer> set = new LinkedHashSet<>();
		
		for (ViewPanel vp : viewPanels.values())
			set.add(vp.getNetworkViewContainer());
		
		return set;
	}
	
	public NetworkViewContainer getCurrentContainer() {
		for (ViewPanel vp : viewPanels.values()) {
			if (vp.getNetworkView().equals(currentNetworkView))
				return vp.getNetworkViewContainer();
		}
		
		return null;
	}
	
	public NetworkViewContainer getContainer(final CyNetworkView view) {
		final ViewPanel vp = viewPanels.get(view);
		
		return vp != null ? vp.getNetworkViewContainer() : null;
	}
	
	public boolean contains(final CyNetworkView view) {
		for (ViewPanel vp : viewPanels.values()) {
			if (vp.getNetworkView().equals(view))
				return true;
		}
		
		return false;
	}
	
	public void removeView(final CyNetworkView view) {
		if (view != null && viewPanels.containsKey(view)) {
			viewPanels.remove(view);
			rootPanes.remove(view);
			arrangePanels();
		}
	}
	
	public int viewCount() {
		return viewPanels.size();
	}
	
	public Arrangement getArrangement() {
		return arrangement;
	}
	
	public void setArrangement(final Arrangement arrangement) {
		if (this.arrangement != arrangement) {
			this.arrangement = arrangement;
			arrangePanels();
		}
	}
	
	public void update() {
		for (ViewPanel vp : viewPanels.values())
			vp.update();
	}
	
	public void dispose() {
		for (ViewPanel vp : viewPanels.values()) {
			vp.getNetworkViewContainer().setRootPane(rootPanes.get(vp.getNetworkView()));
			vp.getNetworkViewContainer().setComparing(false);
		}
	}
	
	private void init() {
		final Set<CyNetworkView> views = new LinkedHashSet<>();
		
		for (ViewPanel vp : viewPanels.values()) {
			vp.getNetworkViewContainer().setComparing(true);
			views.add(vp.getNetworkView());
		}
		
		setName(createUniqueKey(views));
		
		setLayout(new BorderLayout());
		add(getGridPanel(), BorderLayout.CENTER);
		add(getComparisonToolBar(), BorderLayout.SOUTH);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				requestFocusInWindow();
				
				for (ViewPanel vp : viewPanels.values()) {
					if (vp.isCurrent()) {
						vp.getNetworkViewContainer().getContentPane().requestFocusInWindow();
						break;
					}
				}
				
				arrangePanels();
				update();
			}
			@Override
			public void componentResized(ComponentEvent e) {
				arrangePanels();
			}
		});
		
		setKeyBindings(this);
		
		arrangePanels();
		update();
	}
	
	private JPanel getGridPanel() {
		if (gridPanel == null) {
			gridPanel = new JPanel();
		}
		
		return gridPanel;
	}
	
	private JPanel getComparisonToolBar() {
		if (comparisonToolBar == null) {
			comparisonToolBar = new JPanel();
			comparisonToolBar.setName("comparisonToolBar");
			comparisonToolBar.setBorder(
					BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			
			final JSeparator sep1 = new JSeparator(JSeparator.VERTICAL);
			final JSeparator sep2 = new JSeparator(JSeparator.VERTICAL);
			final JSeparator sep3 = new JSeparator(JSeparator.VERTICAL);
			
			final GroupLayout layout = new GroupLayout(comparisonToolBar);
			comparisonToolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(gridViewTogglePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sep1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getDetachComparedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sep2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(sep3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(gridViewTogglePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getDetachComparedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(sep3, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return comparisonToolBar;
	}
	
	JButton getDetachComparedViewsButton() {
		if (detachComparedViewsButton == null) {
			detachComparedViewsButton = new JButton(ICON_EXTERNAL_LINK_SQUARE);
			detachComparedViewsButton.setToolTipText("Detach All Views");
			styleToolBarButton(detachComparedViewsButton,
					serviceRegistrar.getService(IconManager.class).getIconFont(ICON_FONT_SIZE));
		}
		
		return detachComparedViewsButton;
	}
	
	private JButton getOptionsButton() {
		if (optionsBtn == null) {
			optionsBtn = new JButton(ICON_COG);
			optionsBtn.setToolTipText("Options...");
			styleToolBarButton(optionsBtn,
					serviceRegistrar.getService(IconManager.class).getIconFont(ICON_FONT_SIZE * 4/5));
			
			optionsBtn.addActionListener((ActionEvent e) -> {
				getOptionsMenu().show(optionsBtn, 0, optionsBtn.getHeight());
			});
		}
		
		return optionsBtn;
	}
	
	private JPopupMenu getOptionsMenu() {
		final JPopupMenu menu = new JPopupMenu();
		
		for (Arrangement arrangement : Arrangement.values()) {
			final JMenuItem mi = new JCheckBoxMenuItem(arrangement.toString());
			mi.addActionListener((ActionEvent e) -> {
				setArrangement(arrangement);
			});
			mi.setSelected(this.arrangement == arrangement);
			menu.add(mi);
		}
		
		return menu;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + (getName() == null ? 0 : getName().hashCode());
		
		return result;
	}

	/**
	 * Two NetworkViewComparisonPanels are equal if they have the same network views, no matter their positions
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		NetworkViewComparisonPanel other = (NetworkViewComparisonPanel) obj;
		String name = getName();
		
		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName())) {
			return false;
		}
		
		return true;
	}
	
	public static String createUniqueKey(final Collection<CyNetworkView> views) {
		// Sort the views by SUID first
		final List<CyNetworkView> list = new ArrayList<>(views);
		Collections.sort(list, (CyNetworkView o1, CyNetworkView o2) -> {
			return o1.getSUID().compareTo(o2.getSUID());
		});
		
		return "NetworkViewComparisonPanel_" + list.hashCode();
	}
	
	private void arrangePanels() {
		getGridPanel().removeAll();
		
		final Dimension size = getGridPanel().getSize();
		
		if (size == null || size.width <= 0 || size.height <= 0)
			return;
		
		if (!viewPanels.isEmpty()) {
			final int n = viewPanels.size();
			
			// Starts with Horizontal Arrangement
			int rows = 1;
			int cols = n;
			
			switch (arrangement) {
				case AUTO:
					double w = size.getWidth() / cols;
					double h = size.getHeight() / rows;
					
					final int BAR_HEIGHT = 25; // The height of the view container's status bar, approximately
					double ratio = Util.squarenessRatio(w, h - BAR_HEIGHT); // cell width:height ratio
					
					// Pick the pair the gives us the most desirable cell width:height ratio
					for (int i = 1; i < n; i++) {
						int c = n - i;
						
						int r = (int) Math.ceil((double) n / c);
						w = size.getWidth() / c;
						h = size.getHeight() / r;
						double newRatio = Util.squarenessRatio(w, h - BAR_HEIGHT);
						
						// Is this ratio more squared than the previous one
						if (newRatio < ratio) {
							ratio = newRatio;
							rows = r;
							cols = c;
						}
					}
					
					break;
					
				case GRID:
					if (viewPanels.size() == 2) {
						boolean portrait = size.width >= size.height; 
						rows = portrait ? 1 : 2;
						cols = portrait ? 2 : 1;
					} else {
						int sqrt = (int) Math.ceil(Math.sqrt(viewPanels.size()));
						cols = sqrt;
						rows = sqrt;
					}
					
					break;
					
				case VERTICAL:
					rows = n;
					cols = 1;
					
					break;
					
				default:
			}
			
			getGridPanel().setLayout(new GridLayout(rows, cols));
			
			for (ViewPanel vp : viewPanels.values())
				getGridPanel().add(vp);
		}
		
		getGridPanel().updateUI();
	}
	
	private void setKeyBindings(final JComponent comp) {
		final ActionMap actionMap = comp.getActionMap();
		final InputMap inputMap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), KeyAction.VK_N);
		actionMap.put(KeyAction.VK_N, new KeyAction(KeyAction.VK_N));
	}
	
	private class KeyAction extends AbstractAction {

		final static String VK_N = "VK_N";
		
		KeyAction(final String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			
			if (focusOwner instanceof JTextComponent || focusOwner instanceof JTable ||
					!NetworkViewComparisonPanel.this.isVisible())
				return; // We don't want to steal the key event from these components
			
			final String cmd = e.getActionCommand();
			
			if (cmd.equals(VK_N)) {
				// Toggle current view's Navigator (bird's eye view) visibility
				final NetworkViewContainer vc = getCurrentContainer();
				
				if (vc != null)
					vc.getBirdsEyeViewButton().doClick();
			}
		}
	}
	
	protected class ViewPanel extends JPanel {
		
		private final NetworkViewContainer networkViewContainer;

		ViewPanel(final NetworkViewContainer networkViewContainer) {
			this.networkViewContainer = networkViewContainer;
			
			setLayout(new BorderLayout());
			add(getNetworkViewContainer().getRootPane(), BorderLayout.CENTER);
			
			updateBorder();
			
			networkViewContainer.getContentPane().addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					setCurrentNetworkView(ViewPanel.this.getNetworkView());
				}
			});
		}
		
		boolean isCurrent() {
			return networkViewContainer.isCurrent();
		}
		
		NetworkViewContainer getNetworkViewContainer() {
			return networkViewContainer;
		}
		
		CyNetworkView getNetworkView() {
			return getNetworkViewContainer().getNetworkView();
		}
		
		void update() {
			updateBorder();
			getNetworkViewContainer().update();
		}
		
		private void updateBorder() {
			setBorder(BorderFactory.createLineBorder(
					UIManager.getColor(isCurrent() ? "Focus.color" : "Separator.foreground")));
		}
	}
}
