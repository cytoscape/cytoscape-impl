package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;
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
	
	private final int orientation;
	private final NetworkViewContainer container1;
	private final JRootPane rootPane1;
	private final NetworkViewContainer container2;
	private final JRootPane rootPane2;
	
	/**
	 * @param orientation {@link NetworkViewComparisonPanel#HORIZONTAL} or {@link NetworkViewComparisonPanel#VERTICAL}
	 * @param container1
	 * @param container2
	 */
	public NetworkViewComparisonPanel(final int orientation, final NetworkViewContainer container1,
			final NetworkViewContainer container2) {
		if (orientation != JSplitPane.HORIZONTAL_SPLIT && orientation != JSplitPane.VERTICAL_SPLIT)
			throw new IllegalArgumentException("'orientation' must be " + HORIZONTAL + " or " + VERTICAL + ".");
		if (container1 == null)
			throw new IllegalArgumentException("'container1' must not be null.");
		if (container2 == null)
			throw new IllegalArgumentException("'container2' must not be null.");
		if (container1.equals(container2))
			throw new IllegalArgumentException("The view containers must not be the same.");
		
		this.orientation = orientation;
		this.container1 = container1;
		this.rootPane1 = container1.getRootPane();
		this.container2 = container2;
		this.rootPane2 = container2.getRootPane();
		
		container1.setDetached(false);
		container2.setDetached(false);
		
		init();
	}

	public void update() {
		getViewPanel1().update();
		getViewPanel2().update();
	}
	
	public void dispose() {
		getContainer1().setRootPane(rootPane1);
		getContainer2().setRootPane(rootPane2);
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
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				getContainer1().getNetworkView().updateView();
				getContainer2().getNetworkView().updateView();
			}
		});
	}
	
	protected JSplitPane getSplitPane() {
		if (splitPane == null) {
			splitPane = new JSplitPane(orientation, getViewPanel1(), getViewPanel2());
			splitPane.setResizeWeight(0.5);
		}
		
		return splitPane;
	}
	
	private ViewPanel getViewPanel1() {
		if (viewPanel1 == null) {
			viewPanel1 = new ViewPanel(container1);
		}
		
		return viewPanel1;
	}
	
	private ViewPanel getViewPanel2() {
		if (viewPanel2 == null) {
			viewPanel2 = new ViewPanel(container2);
		}
		
		return viewPanel2;
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
	
	private class ViewPanel extends JPanel {
		
		private JPanel titlePanel;
		private JLabel titleLabel;
		
		private final NetworkViewContainer networkViewContainer;

		ViewPanel(final NetworkViewContainer networkViewContainer) {
			this.networkViewContainer = networkViewContainer;
			
			setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
			
			setLayout(new BorderLayout());
			add(getNetworkViewContainer().getRootPane(), BorderLayout.CENTER);
			add(getTitlePanel(), BorderLayout.SOUTH);
		}
		
		NetworkViewContainer getNetworkViewContainer() {
			return networkViewContainer;
		}
		
		CyNetworkView getNetworkView() {
			return getNetworkViewContainer().getNetworkView();
		}
		
		void update() {
			final String title = ViewUtil.getTitle(getNetworkView());
			getTitleLabel().setText(title);
			getTitleLabel().setToolTipText(title);
		}
		
		JPanel getTitlePanel() {
			if (titlePanel == null) {
				titlePanel = new JPanel();
				titlePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, 
						UIManager.getColor("Separator.foreground")));
				
				final GroupLayout layout = new GroupLayout(titlePanel);
				titlePanel.setLayout(layout);
				layout.setAutoCreateContainerGaps(false);
				layout.setAutoCreateGaps(true);
				
				layout.setHorizontalGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGap(0, 0, Short.MAX_VALUE)
						.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(0, 0, Short.MAX_VALUE)
						.addContainerGap()
				);
				layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
						.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				);
			}
			
			return titlePanel;
		}
		
		JLabel getTitleLabel() {
			if (titleLabel == null) {
				titleLabel = new JLabel();
				titleLabel.setFont(titleLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			}
			
			return titleLabel;
		}
	}
}
