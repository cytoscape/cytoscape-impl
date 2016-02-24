package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.ICON_EXTERNAL_LINK_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_TH;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

	public static final int HORIZONTAL = JSplitPane.HORIZONTAL_SPLIT;
	public static final int VERTICAL = JSplitPane.VERTICAL_SPLIT;
	
	private JSplitPane splitPane;
	private ViewPanel viewPanel1;
	private ViewPanel viewPanel2;
	
	private JPanel comparisonToolBar;
	private JButton gridModeButton;
	private JButton detachComparedViewsButton;
	
	private final int orientation;
	private final NetworkViewContainer container1;
	private final JRootPane rootPane1;
	private final NetworkViewContainer container2;
	private final JRootPane rootPane2;
	
	private CyNetworkView currentNetworkView;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	/**
	 * @param orientation {@link NetworkViewComparisonPanel#HORIZONTAL} or {@link NetworkViewComparisonPanel#VERTICAL}
	 * @param container1
	 * @param container2
	 */
	public NetworkViewComparisonPanel(
			final int orientation,
			final NetworkViewContainer container1,
			final NetworkViewContainer container2,
			final CyNetworkView currentNetworkView,
			final CyServiceRegistrar serviceRegistrar
	) {
		if (orientation != JSplitPane.HORIZONTAL_SPLIT && orientation != JSplitPane.VERTICAL_SPLIT)
			throw new IllegalArgumentException("'orientation' must be " + HORIZONTAL + " or " + VERTICAL + ".");
		if (container1 == null)
			throw new IllegalArgumentException("'container1' must not be null.");
		if (container2 == null)
			throw new IllegalArgumentException("'container2' must not be null.");
		if (container1.equals(container2))
			throw new IllegalArgumentException("The view containers must not be the same.");
		if (!container1.getNetworkView().equals(currentNetworkView) &&
				!container2.getNetworkView().equals(currentNetworkView))
			throw new IllegalArgumentException("'currentNetworkView' must be in container1 or container2.");
		
		this.orientation = orientation;
		this.container1 = container1;
		this.rootPane1 = container1.getRootPane();
		this.container2 = container2;
		this.rootPane2 = container2.getRootPane();
		this.currentNetworkView = currentNetworkView;
		this.serviceRegistrar = serviceRegistrar;
		
		container1.setComparing(true);
		container2.setComparing(true);
		
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

	public void update() {
		getViewPanel1().update();
		getViewPanel2().update();
	}
	
	public void dispose() {
		getContainer1().setRootPane(rootPane1);
		getContainer2().setRootPane(rootPane2);
		getContainer1().setComparing(false);
		getContainer2().setComparing(false);
	}
	
	public int getOrientation() {
		return orientation;
	}
	
	public NetworkViewContainer getContainer1() {
		return container1;
	}
	
	public NetworkViewContainer getContainer2() {
		return container2;
	}
	
	private void init() {
		setName(createUniqueKey(container1.getNetworkView(), container2.getNetworkView()));
		
		setLayout(new BorderLayout());
		add(getSplitPane(), BorderLayout.CENTER);
		add(getComparisonToolBar(), BorderLayout.SOUTH);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				requestFocusInWindow();
				
				if (getViewPanel1().isCurrent())
					getContainer1().getContentPane().requestFocusInWindow();
				else if (getViewPanel2().isCurrent())
					getContainer2().getContentPane().requestFocusInWindow();
			}
		});
		
		update();
	}
	
	protected JSplitPane getSplitPane() {
		if (splitPane == null) {
			splitPane = new JSplitPane(orientation, getViewPanel1(), getViewPanel2());
			splitPane.setResizeWeight(0.5);
		}
		
		return splitPane;
	}
	
	protected ViewPanel getViewPanel1() {
		if (viewPanel1 == null) {
			viewPanel1 = new ViewPanel(container1);
		}
		
		return viewPanel1;
	}
	
	protected ViewPanel getViewPanel2() {
		if (viewPanel2 == null) {
			viewPanel2 = new ViewPanel(container2);
		}
		
		return viewPanel2;
	}
	
	private JPanel getComparisonToolBar() {
		if (comparisonToolBar == null) {
			comparisonToolBar = new JPanel();
			comparisonToolBar.setName("comparisonToolBar");
			
			final GroupLayout layout = new GroupLayout(comparisonToolBar);
			comparisonToolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDetachComparedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDetachComparedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return comparisonToolBar;
	}
	
	JButton getGridModeButton() {
		if (gridModeButton == null) {
			gridModeButton = new JButton(ICON_TH);
			gridModeButton.setToolTipText("Show Grid (G)");
			styleToolBarButton(gridModeButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return gridModeButton;
	}
	
	JButton getDetachComparedViewsButton() {
		if (detachComparedViewsButton == null) {
			detachComparedViewsButton = new JButton(ICON_EXTERNAL_LINK_SQUARE);
			detachComparedViewsButton.setToolTipText("Detach Both Views");
			styleToolBarButton(detachComparedViewsButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return detachComparedViewsButton;
	}
	
	@Override
	public String toString() {
		return container1 + " :: " + container2;
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
	
	public static String createUniqueKey(final CyNetworkView view1, final CyNetworkView view2) {
		// The name should be unique
		long suid1 = view1.getSUID();
		long suid2 = view2.getSUID();
		
		// The lower SUID value always comes first
		if (suid2 < suid1) {
			long temp = suid1;
			suid1 = suid2;
			suid2 = temp;
		}
		
		return "NetworkViewComparisonPanel_" + suid1 + "::" + suid2;
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
			return this.getNetworkView().equals(getCurrentNetworkView());
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
