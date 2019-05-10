package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.application.swing.CytoPanelState.DOCK;
import static org.cytoscape.application.swing.CytoPanelState.HIDE;
import static org.cytoscape.internal.view.CytoPanelNameInternal.BOTTOM;
import static org.cytoscape.internal.view.CytoPanelNameInternal.EAST;
import static org.cytoscape.internal.view.CytoPanelNameInternal.SOUTH;
import static org.cytoscape.internal.view.CytoPanelNameInternal.WEST;
import static org.cytoscape.internal.view.CytoPanelUtil.BOTTOM_MIN_HEIGHT;
import static org.cytoscape.internal.view.CytoPanelUtil.BOTTOM_MIN_WIDTH;
import static org.cytoscape.internal.view.CytoPanelUtil.EAST_MIN_HEIGHT;
import static org.cytoscape.internal.view.CytoPanelUtil.EAST_MIN_WIDTH;
import static org.cytoscape.internal.view.CytoPanelUtil.SOUTH_MIN_HEIGHT;
import static org.cytoscape.internal.view.CytoPanelUtil.SOUTH_MIN_WIDTH;
import static org.cytoscape.internal.view.CytoPanelUtil.WEST_MIN_HEIGHT;
import static org.cytoscape.internal.view.CytoPanelUtil.WEST_MIN_WIDTH;
import static org.cytoscape.util.swing.IconManager.ICON_SQUARE_O;
import static org.cytoscape.util.swing.IconManager.ICON_THUMB_TACK;
import static org.cytoscape.util.swing.IconManager.ICON_WINDOW_MINIMIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
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

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedEvent;
import org.cytoscape.event.CyEventHelper;
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
	public static final String TEXT_FLOAT = "Float";
	public static final String TEXT_HIDE = "Minimize";
	public static final String TEXT_REMOVE = "Remove";
	
	/* These are the minimum sizes for our CytoPanels. A CytoPanel can't exceed these values. */
	private final int NOTIFICATION_STATE_CHANGE = 0;
	private final int NOTIFICATION_COMPONENT_SELECTED = 1;

	private JPanel mainPanel;
	private JPanel titlePanel;
	private DropDownMenuButton titleButton;
	private JButton floatButton;
	private JButton minimizeButton;
	
	private JPanel cardsPanel;
	private final CardLayout cardLayout = new CardLayout();
	
	private final CytoPanelNameInternal compassDirection;
	private final int trimBarIndex;
	
	private CytoPanelState cytoPanelState;
	private boolean removed;

	private final List<CytoPanelComponent> cytoPanelComponents = new ArrayList<>();
	private final Map<String, CytoPanelComponent2> componentsById = new HashMap<>();

	private final CyServiceRegistrar serviceRegistrar;

	public CytoPanelImpl(
			final CytoPanelNameInternal compassDirection,
			final int trimBarIndex,
			final CytoPanelState cytoPanelState,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.compassDirection = compassDirection;
		this.trimBarIndex = trimBarIndex;
		this.serviceRegistrar = serviceRegistrar;
		
		update();
		setState(cytoPanelState);
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
	
	public int getTrimBarIndex() {
		return trimBarIndex;
	}

	public void insert(CytoPanelComponent cpc, int index) {
		if (indexOfComponent(cpc.getComponent()) >= 0)
			return;
		
		cytoPanelComponents.add(index, cpc);
		
		if (cpc instanceof CytoPanelComponent2) {
			final CytoPanelComponent2 comp2 = (CytoPanelComponent2) cpc;
			
			if (comp2.getIdentifier() == null)
				throw new NullPointerException("'CytoPanelComponent2.identifier' must not be null");
			
			componentsById.put(comp2.getIdentifier(), comp2);
		}
		
		
		checkSizes(cpc.getComponent()); // Check our sizes, and override, if necessary
		getCardsPanel().add(cpc.getComponent(), getIdentifier(cpc));
		
		updateTitleButton();
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
		CytoPanelComponent cpc = index >= 0 && cytoPanelComponents.size() > index ?
				cytoPanelComponents.get(index) : null;

		return cpc != null ? cpc.getComponent() : null;
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

	public void remove(CytoPanelComponent comp) {
		boolean changed = cytoPanelComponents.remove(comp);
		getCardsPanel().remove(comp.getComponent());
		
		if (comp instanceof CytoPanelComponent2)
			componentsById.remove(((CytoPanelComponent2)comp).getIdentifier());
		
		if (changed)
			updateTitleButton();
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
		CytoPanelComponent cpc = index >= 0 && cytoPanelComponents.size() > index ?
				cytoPanelComponents.get(index) : null;
		
		if (cpc != null) {
			cardLayout.show(getCardsPanel(), getIdentifier(cpc));
			updateTitleButton();
			notifyListeners(NOTIFICATION_COMPONENT_SELECTED);
		}
	}

	@Override
	public void setState(CytoPanelState newState) {
		if (newState == null)
			throw new IllegalArgumentException("CytoPanelState must not be null.");
		
		if (newState != cytoPanelState) {
			cytoPanelState = newState;
			update();
			notifyListeners(NOTIFICATION_STATE_CHANGE);
		}
	}

	@Override
	public CytoPanelState getState() {
		return cytoPanelState;
	}
	
	@Override
	public Component getThisComponent() {
		return getMainPanel();
	}
	
	public boolean isRemoved() {
		return removed;
	}
	
	public void setRemoved(boolean removed) {
		if (removed != this.removed) {
			this.removed = removed;
			update();
		}
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
	
	private JPanel getTitlePanel() {
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
					.addComponent(getMinimizeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(getTitleButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getFloatButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
			floatButton = new JButton(ICON_SQUARE_O);
			floatButton.setToolTipText(TEXT_FLOAT);
			CytoPanelUtil.styleButton(floatButton);
			floatButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(12));
		}
		
		return floatButton;
	}
	
	JButton getMinimizeButton() {
		if (minimizeButton == null) {
			minimizeButton = new JButton(ICON_WINDOW_MINIMIZE);
			minimizeButton.setToolTipText(TEXT_HIDE);
			CytoPanelUtil.styleButton(minimizeButton);
			minimizeButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(11));
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

		// determine what event to fire
		switch (notificationType) {
			case NOTIFICATION_STATE_CHANGE:
				eventHelper.fireEvent(new CytoPanelStateChangedEvent(this, this, cytoPanelState));
				break;
			case NOTIFICATION_COMPONENT_SELECTED:
				int selectedIndex = getSelectedIndex();
				eventHelper.fireEvent(new CytoPanelComponentSelectedEvent(this, this, selectedIndex));
				break;
		}
	}
	
	void update() {
		updateTitleButton();
		getFloatButton().setText(cytoPanelState == DOCK ? ICON_SQUARE_O : ICON_THUMB_TACK);
		getFloatButton().setToolTipText(cytoPanelState == DOCK ? TEXT_FLOAT : TEXT_DOCK);
		
		getThisComponent().setVisible(!isRemoved() && getState() != HIDE);
		getThisComponent().validate();
	}
	
	private void updateTitleButton() {
		int index = getSelectedIndex();
		CytoPanelComponent cpc = index >= 0 && cytoPanelComponents.size() > index ?
				cytoPanelComponents.get(index) : null;

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
}
