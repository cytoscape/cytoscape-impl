package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.internal.view.CytoPanelNameInternal.BOTTOM;
import static org.cytoscape.internal.view.CytoPanelNameInternal.EAST;
import static org.cytoscape.internal.view.CytoPanelNameInternal.SOUTH;
import static org.cytoscape.internal.view.CytoPanelNameInternal.WEST;
import static org.cytoscape.internal.view.CytoPanelStateInternal.DOCK;
import static org.cytoscape.internal.view.CytoPanelStateInternal.FLOAT;
import static org.cytoscape.internal.view.CytoPanelStateInternal.HIDE;
import static org.cytoscape.internal.view.CytoPanelStateInternal.MINIMIZE;
import static org.cytoscape.internal.view.CytoPanelStateInternal.UNDOCK;
import static org.cytoscape.internal.view.CytoPanelUtil.BOTTOM_MIN_HEIGHT;
import static org.cytoscape.internal.view.CytoPanelUtil.BOTTOM_MIN_WIDTH;
import static org.cytoscape.internal.view.CytoPanelUtil.EAST_MIN_HEIGHT;
import static org.cytoscape.internal.view.CytoPanelUtil.EAST_MIN_WIDTH;
import static org.cytoscape.internal.view.CytoPanelUtil.SOUTH_MIN_HEIGHT;
import static org.cytoscape.internal.view.CytoPanelUtil.SOUTH_MIN_WIDTH;
import static org.cytoscape.internal.view.CytoPanelUtil.WEST_MIN_HEIGHT;
import static org.cytoscape.internal.view.CytoPanelUtil.WEST_MIN_WIDTH;
import static org.cytoscape.util.swing.IconManager.ICON_WINDOW_MAXIMIZE;
import static org.cytoscape.util.swing.IconManager.ICON_WINDOW_MINIMIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.SwingPropertyChangeSupport;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.internal.util.IconUtil;
import org.cytoscape.internal.view.util.ViewUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.DropDownMenuButton;
import org.cytoscape.util.swing.IconManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class CytoPanelImpl implements CytoPanel {
	
	public static final String TEXT_DOCK = "Dock";
	public static final String TEXT_UNDOCK = "Undock";
	public static final String TEXT_FLOAT = "Float";
	public static final String TEXT_MINIMIZE = "Minimize";
	public static final String TEXT_HIDE = "Hide";
	
	public static final float STATE_ICON_FONT_SIZE = 11.0f;
	
	private final int NOTIFICATION_STATE_CHANGE = 0;
	private final int NOTIFICATION_COMPONENT_SELECTED = 1;

	private JPanel mainPanel;
	private JPanel titlePanel;
	private DropDownMenuButton titleButton;
	private JButton floatButton;
	private JButton dockButton;
	private JButton undockButton;
	private JButton minimizeButton;
	
	private JPanel cardsPanel;
	private final CardLayout cardLayout = new CardLayout();
	
	private final CytoPanelNameInternal compassDirection;
	private final int trimBarIndex;
	
	private CytoPanelStateInternal state;
	private boolean maximized;
	
	private final List<CytoPanelComponent> cytoPanelComponents = new ArrayList<>();
	private final Map<String, CytoPanelComponent2> componentsById = new HashMap<>();
	
	private final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this, true);

	private final CyServiceRegistrar serviceRegistrar;

	public CytoPanelImpl(
			final CytoPanelNameInternal compassDirection,
			final int trimBarIndex,
			final CytoPanelStateInternal state,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.compassDirection = compassDirection;
		this.trimBarIndex = trimBarIndex;
		this.serviceRegistrar = serviceRegistrar;
		
		update();
		setStateInternal(state);
	}
	
	String getTitle() {
		return compassDirection.getTitle();
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return compassDirection.toCytoPanelName();
	}
	
	public CytoPanelNameInternal getCytoPanelNameInternal() {
		return compassDirection;
	}
	
	@Override
	public CytoPanelState getState() {
		return getCytoPanelComponentCount() > 0 ? state.toCytoPanelState() : CytoPanelState.HIDE;
	}

	@Override
	public void setState(CytoPanelState newState) {
		if (newState == null)
			throw new IllegalArgumentException("CytoPanelState must not be null.");
		
		setStateInternal(CytoPanelStateInternal.valueOf(newState));
	}
	
	public CytoPanelStateInternal getStateInternal() {
		return state;
	}
	
	public void setStateInternal(CytoPanelStateInternal newState) {
		if (newState == null)
			throw new IllegalArgumentException("'newState' must not be null.");
		
		if (newState != state) {
			CytoPanelState oldApiState = getState();
			CytoPanelStateInternal oldState = state;
			state = newState;
			update();
			
			if (pcs.hasListeners("stateInternal"))
				pcs.firePropertyChange("stateInternal", oldState, newState);
			
			if (oldApiState != getState()) // Not all internal states will change the API state!
				notifyListeners(NOTIFICATION_STATE_CHANGE);
		}
	}
	
	public int getTrimBarIndex() {
		return trimBarIndex;
	}

	public boolean insert(CytoPanelComponent cpc, int index) {
		if (indexOfComponent(cpc.getComponent()) >= 0)
			return false;
		
		CytoPanelState oldState = getState();
		cytoPanelComponents.add(index, cpc);
		
		if (cpc instanceof CytoPanelComponent2) {
			final CytoPanelComponent2 comp2 = (CytoPanelComponent2) cpc;
			
			if (comp2.getIdentifier() == null)
				throw new NullPointerException("'CytoPanelComponent2.identifier' must not be null");
			
			componentsById.put(comp2.getIdentifier(), comp2);
		}
		
		checkSizes(cpc.getComponent()); // Check our sizes, and override, if necessary
		getCardsPanel().add(cpc.getComponent(), getIdentifier(cpc));
		
		update();
		
		// For backwards compatibility
		if (oldState != getState())
			notifyListeners(NOTIFICATION_STATE_CHANGE); // The CytoPanelState probably changed from HIDE
		
		return true;
	}
	
	@Override
	public int getCytoPanelComponentCount() {
		return cytoPanelComponents.size();
	}

	@Override
	public Component getSelectedComponent() {
		for (Component c : getCardsPanel().getComponents()) {
		    if (c.isVisible())
		    	return c;
		}
		
		return null;
	}

	@Override
	public Component getComponentAt(int index) {
		CytoPanelComponent cpc = getCytoPanelComponentAt(index);

		return cpc != null ? cpc.getComponent() : null;
	}
	
	public CytoPanelComponent getCytoPanelComponentAt(int index) {
		return index >= 0 && cytoPanelComponents.size() > index ? cytoPanelComponents.get(index) : null;
	}

	@Override
	public int getSelectedIndex() {
		return indexOfComponent(getSelectedComponent());
	}

	@Override
	public int indexOfComponent(Component component) {
		int i = 0;
		
		for (CytoPanelComponent cpc : cytoPanelComponents) {
			if (cpc.getComponent().equals(component))
				return i;
			
			i++;
		}
		
		return -1;
	}
	
	@Override
	public int indexOfComponent(String identifier) {
		final CytoPanelComponent cpc = componentsById.get(identifier);
		
		return cpc != null ? indexOfComponent(cpc.getComponent()) : -1;
	}

	public boolean remove(CytoPanelComponent comp) {
		CytoPanelState oldState = getState();
		boolean removed = cytoPanelComponents.remove(comp);
		getCardsPanel().remove(comp.getComponent());
		
		if (comp instanceof CytoPanelComponent2)
			componentsById.remove(((CytoPanelComponent2)comp).getIdentifier());
		
		if (removed) {
			update();
			
			// For backwards compatibility
			if (oldState != getState())
				notifyListeners(NOTIFICATION_STATE_CHANGE); // The CytoPanelState probably changed to HIDE
		}
		
		return removed;
	}
	
	public List<CytoPanelComponent> getCytoPanelComponents() {
		return new ArrayList<>(cytoPanelComponents);
	}
	
	public void clear() {
		getCardsPanel().removeAll();
		cytoPanelComponents.clear();
		componentsById.clear();
	}

	@Override
	public void setSelectedIndex(int index) {
		CytoPanelComponent cpc = getCytoPanelComponentAt(index);
		setSelectedComponent(cpc);
	}
	
	public void setSelectedComponent(CytoPanelComponent cpc) {
		if (cpc != null) {
			cardLayout.show(getCardsPanel(), getIdentifier(cpc));
			updateTitleButton();
			notifyListeners(NOTIFICATION_COMPONENT_SELECTED);
		}
	}

	@Override
	public Component getThisComponent() {
		return getMainPanel();
	}
	
	public boolean isMaximized() {
		return maximized;
	}
	
	public void setMaximized(boolean maximized) {
		this.maximized = maximized;
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}
	
	/**
	 * Checks to make sure the CytoPanel is within the appropriate dimensions
	 * by overriding the sizes, if necessary
	 */
	private void checkSizes(Component comp) {
		if (compassDirection == WEST)
			comp.setMinimumSize(new Dimension(WEST_MIN_WIDTH, WEST_MIN_HEIGHT));
		else if (compassDirection == SOUTH)
			comp.setMinimumSize(new Dimension(SOUTH_MIN_WIDTH, SOUTH_MIN_HEIGHT));
		else if (compassDirection == EAST)
			comp.setMinimumSize(new Dimension(EAST_MIN_WIDTH, EAST_MIN_HEIGHT));
		else if (compassDirection == BOTTOM)
			comp.setMinimumSize(new Dimension(BOTTOM_MIN_WIDTH, BOTTOM_MIN_HEIGHT));
	}
	
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setBorder(BorderFactory.createEmptyBorder());
			
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(getTitlePanel(), BorderLayout.NORTH);
			mainPanel.add(getCardsPanel(), BorderLayout.CENTER);
		}
		
		return mainPanel;
	}
	
	JPanel getTitlePanel() {
		if (titlePanel == null) {
			titlePanel = new JPanel();
			titlePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));

			GroupLayout layout = new GroupLayout(titlePanel);
			titlePanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);

			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getTitleButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(20, 20, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getFloatButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDockButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getUndockButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getMinimizeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(getTitleButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getFloatButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDockButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getUndockButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getMinimizeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return titlePanel;
	}
	
	private JPanel getCardsPanel() {
		if (cardsPanel == null) {
			cardsPanel = new JPanel(cardLayout);
		}
		
		return cardsPanel;
	}
	
	JButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JButton(ICON_WINDOW_MAXIMIZE);
			floatButton.setToolTipText(TEXT_FLOAT);
			CytoPanelUtil.styleButton(floatButton);
			floatButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(STATE_ICON_FONT_SIZE));
		}
		
		return floatButton;
	}
	
	JButton getUndockButton() {
		if (undockButton == null) {
			undockButton = new JButton(IconUtil.UNPIN);
			undockButton.setToolTipText(TEXT_UNDOCK);
			CytoPanelUtil.styleButton(undockButton);
			undockButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, STATE_ICON_FONT_SIZE));
		}
		
		return undockButton;
	}
	
	JButton getDockButton() {
		if (dockButton == null) {
			dockButton = new JButton(IconUtil.PIN);
			dockButton.setToolTipText(TEXT_DOCK);
			CytoPanelUtil.styleButton(dockButton);
			dockButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, STATE_ICON_FONT_SIZE));
		}
		
		return dockButton;
	}
	
	JButton getMinimizeButton() {
		if (minimizeButton == null) {
			minimizeButton = new JButton(ICON_WINDOW_MINIMIZE);
			minimizeButton.setToolTipText(TEXT_MINIMIZE);
			CytoPanelUtil.styleButton(minimizeButton);
			minimizeButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(STATE_ICON_FONT_SIZE));
		}
		
		return minimizeButton;
	}
	
	DropDownMenuButton getTitleButton() {
		if (titleButton == null) {
			titleButton = new DropDownMenuButton();
			titleButton.setHorizontalAlignment(SwingConstants.LEFT);
			titleButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			titleButton.setContentAreaFilled(false);
			makeSmall(titleButton);
			titleButton.addActionListener(evt -> {
				if (getCytoPanelComponentCount() > 0) {
					JPopupMenu popupMenu = new JPopupMenu();
					int i = 0;
					
					for (CytoPanelComponent cpc : cytoPanelComponents) {
						int index = i++;
						String title = cpc.getTitle();
						Icon icon = cpc.getIcon();
						
						if (icon == null)
							icon = ViewUtil.createDefaultIcon(title, CytoPanelUtil.BUTTON_SIZE,
									serviceRegistrar.getService(IconManager.class));
						else if (icon.getIconHeight() > CytoPanelUtil.BUTTON_SIZE)
							icon = ViewUtil.resizeIcon(icon, CytoPanelUtil.BUTTON_SIZE);
						
						JCheckBoxMenuItem mi = new JCheckBoxMenuItem(title, icon);
						mi.setSelected(getSelectedIndex() == index);
						mi.addActionListener(e -> setSelectedIndex(index));
						popupMenu.add(mi);
					}
					
					popupMenu.show(titleButton, 0, titleButton.getHeight());
					popupMenu.requestFocusInWindow();
				}
			});
			
			updateTitleButton();
		}
		
		return titleButton;
	}

	private void notifyListeners(int notificationType) {
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);

		switch (notificationType) {
			case NOTIFICATION_STATE_CHANGE:
				eventHelper.fireEvent(new CytoPanelStateChangedEvent(this, this, getState()));
				break;
			case NOTIFICATION_COMPONENT_SELECTED:
				int selectedIndex = getSelectedIndex();
				eventHelper.fireEvent(new CytoPanelComponentSelectedEvent(this, this, selectedIndex));
				break;
		}
	}
	
	void update() {
		updateTitleButton();
		
		getFloatButton().setVisible(state != FLOAT);
		getDockButton().setVisible(state != DOCK);
		getUndockButton().setVisible(state != UNDOCK);
		getMinimizeButton().setVisible(state != MINIMIZE);
		
		getThisComponent().setVisible(getCytoPanelComponentCount() > 0 && state != HIDE && state != MINIMIZE);
		getThisComponent().validate();
	}

	private void updateTitleButton() {
		int index = getSelectedIndex();
		CytoPanelComponent cpc = getCytoPanelComponentAt(index);

		String text = cpc != null && cpc.getTitle() != null ? cpc.getTitle().trim() : "";
		
		// Show icon instead, if there's no text
		Icon icon = cpc != null && text.isEmpty() ? cpc.getIcon() : null;
		
		// Make sure the icon is no too big
		if (icon != null && icon.getIconHeight() > CytoPanelUtil.BUTTON_SIZE)
			icon = ViewUtil.resizeIcon(icon, CytoPanelUtil.BUTTON_SIZE);

		getTitleButton().setText(text);
		getTitleButton().setIcon(icon);
		getTitleButton().setToolTipText(text);
		getTitleButton().setEnabled(getCytoPanelComponentCount() > 0);
		getTitleButton().setVisible(getCytoPanelComponentCount() > 0);
	}
	
	private String getIdentifier(CytoPanelComponent cpc) {
		if (cpc instanceof CytoPanelComponent2)
			return ((CytoPanelComponent2) cpc).getIdentifier();
		
		return cpc.getTitle() + "__" + cpc.getClass().getName();
	}
	
	@Override
	public String toString() {
		return compassDirection.getTitle();
	}
}
