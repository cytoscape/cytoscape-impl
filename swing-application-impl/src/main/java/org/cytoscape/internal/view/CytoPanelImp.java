package org.cytoscape.internal.view;

import static org.cytoscape.internal.view.CytoPanelUtil.BUTTON_SIZE;
import static org.cytoscape.util.swing.IconManager.ICON_CARET_DOWN;
import static org.cytoscape.util.swing.IconManager.ICON_REMOVE;
import static org.cytoscape.util.swing.IconManager.ICON_SQUARE_O;
import static org.cytoscape.util.swing.IconManager.ICON_THUMB_TACK;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class CytoPanelImp extends JPanel implements CytoPanel, ChangeListener {
	
	private final static Logger logger = LoggerFactory.getLogger(CytoPanelImp.class);

	
	private static final int FLOAT_PANEL_SCALE_FACTOR = 2;

	private static final String TOOL_TIP_SWITCH = "Switch To...";
	private static final String TOOL_TIP_FLOAT = "Float Window";
	private static final String TOOL_TIP_DOCK = "Dock Window";
	private static final String TOOL_TIP_CLOSE = "Close Window";
	
	/**
	 * These are the minimum sizes for our CytoPanels.  A CytoPanel can't exceed these values.
	 */
	private static final int WEST_MIN_WIDTH = 100;
	private static final int WEST_MAX_WIDTH = 400;
	private static final int WEST_MIN_HEIGHT = 500;
	private static final int SOUTH_MIN_WIDTH = 500;
	private static final int SOUTH_MIN_HEIGHT = 50;
	
	private static final int EAST_MIN_WIDTH = 100;
	private static final int EAST_MAX_WIDTH = 1500;
	private static final int EAST_MIN_HEIGHT = 100;
	
	/**
	 * Notification state change.
	 */
	private final int NOTIFICATION_STATE_CHANGE = 0;

	/**
	 * Notification component selected.
	 */
	private final int NOTIFICATION_COMPONENT_SELECTED = 1;

	/**
	 * Notification component added.
	 */
	private final int NOTIFICATION_COMPONENT_ADDED = 2;

	/**
	 * Notification component removed.
	 */
	private final int NOTIFICATION_COMPONENT_REMOVED = 3;

	/**
	 * Reference to CytoPanelContainer we live in.
	 */
	private CytoPanelContainer cytoPanelContainer;

	/**
	 * External window used to hold the floating CytoPanel.
	 */
	private JFrame externalWindow;

	private JTabbedPane tabbedPane;
	private JLabel floatLabel;
	private JButton switchCompButton;
	private JButton floatButton;
	private JButton closeButton;
	
	private final JFrame parent;
	private final CytoPanelName compassDirection;
	private final int tabPlacement;
	
	private CytoPanelState cytoPanelState;

	private final Map<String, CytoPanelComponent2> componentsById;

	private final CyServiceRegistrar serviceRegistrar;

	public CytoPanelImp(
			final CytoPanelName compassDirection,
			final int tabPlacement,
			final CytoPanelState cytoPanelState,
			final CySwingApplication cySwingApp,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.parent = cySwingApp.getJFrame();
		this.compassDirection = compassDirection;
		this.tabPlacement = tabPlacement;
		this.serviceRegistrar = serviceRegistrar;
		
		componentsById = new HashMap<>();
		
		init();
		setState(cytoPanelState); // to hidden by default 
	}

	public void setCytoPanelContainer(CytoPanelContainer cytoPanelContainer) {
		this.cytoPanelContainer = cytoPanelContainer;
	}

	private String getTitle() {
		return compassDirection.getTitle();
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return compassDirection;
	}

	public void add(final CytoPanelComponent comp) {
		if (comp instanceof CytoPanelComponent2) {
			final CytoPanelComponent2 comp2 = (CytoPanelComponent2) comp;
			
			if (comp2.getIdentifier() == null)
				throw new NullPointerException("'CytoPanelComponent2.identifier' must not be null");
			
			componentsById.put(comp2.getIdentifier(), comp2);
		}
		
		// Check our sizes, and override, if necessary
		checkSizes(comp.getComponent());
		// add tab to JTabbedPane
		getTabbedPane().addTab(comp.getTitle(), comp.getIcon(), comp.getComponent());
		// send out a notification
		notifyListeners(NOTIFICATION_COMPONENT_ADDED);
	}

	@Override
	public int getCytoPanelComponentCount() {
		return getTabbedPane().getTabCount();
	}

	@Override
	public Component getSelectedComponent() {
		return getTabbedPane().getSelectedComponent();
	}

	@Override
	public Component getComponentAt(int index) {
		return getTabbedPane().getComponentAt(index);
	}

	@Override
	public int getSelectedIndex() {
		return getTabbedPane().getSelectedIndex();
	}

	@Override
	public int indexOfComponent(Component component) {
		return getTabbedPane().indexOfComponent(component);
	}
	
	@Override
	public int indexOfComponent(String identifier) {
		final CytoPanelComponent cpComp = componentsById.get(identifier);
		
		return cpComp != null ? indexOfComponent(cpComp.getComponent()) : -1;
	}

	@Override
	public void remove(Component component) {
		getTabbedPane().remove(component);
		notifyListeners(NOTIFICATION_COMPONENT_REMOVED);
	}

	public void remove(CytoPanelComponent comp) {
		getTabbedPane().remove(comp.getComponent());
		
		if (comp instanceof CytoPanelComponent2)
			componentsById.remove(((CytoPanelComponent2)comp).getIdentifier());
	}

	@Override
	public void remove(int index) {
		getTabbedPane().remove(index);
		notifyListeners(NOTIFICATION_COMPONENT_REMOVED);
	}

	@Override
	public void removeAll() {
		getTabbedPane().removeAll();
		componentsById.clear();
		notifyListeners(NOTIFICATION_COMPONENT_REMOVED);
	}

	@Override
	public void setSelectedIndex(final int index) {
		if (getTabbedPane().getTabCount() <= index)
			return;
		
		getTabbedPane().setSelectedIndex(index);
		resizeSelectedComponent();
		// do not have to sent out notification - the tabbedPane will let us know.
	}

	@Override
	public void setState(CytoPanelState cytoPanelState) {
		boolean success = false;

		// 'switch' on the state
		if (cytoPanelState == CytoPanelState.HIDE) {
			hideCytoPanel(cytoPanelState);
			success = true;
		} else if (cytoPanelState == CytoPanelState.FLOAT) {
			FloatCytoPanel();
			success = true;
		} else if (cytoPanelState == CytoPanelState.DOCK) {
			DockCytoPanel();
			success = true;
		}

		if (!success)
			throw new IllegalArgumentException("Illegal Argument:  " + cytoPanelState
			                                   + ".  is unknown.  Please see CytoPanelState class.");

		this.cytoPanelState = cytoPanelState;
		notifyListeners(NOTIFICATION_STATE_CHANGE);
	}

	@Override
	public CytoPanelState getState() {
		return cytoPanelState;
	}
	
	@Override
	public Component getThisComponent() {
		return this;
	}

	/**
	 * Our implementation of the ChangeListener interface, to determine when new tab has been selected
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		updateSwitchCompButton();
		resizeSelectedComponent();
		notifyListeners(NOTIFICATION_COMPONENT_SELECTED);
	}
	
	public void addComponentToSouth(Component pComponent) {
		add(pComponent, BorderLayout.SOUTH);
	}

	public void removeComponentAtSouth(Component pComponent) {
		remove(pComponent);
	}

	private void showCytoPanel(CytoPanelState cytoPanelState) {
		setVisible(true);

		//  if our parent is a BiModalSplitPane, show the split
		Container parent = this.getParent();

		if (parent instanceof BiModalJSplitPane) {
			BiModalJSplitPane biModalSplitPane = (BiModalJSplitPane) parent;
			biModalSplitPane.setMode(cytoPanelState, BiModalJSplitPane.MODE_SHOW_SPLIT);
		}
	}

	private void hideCytoPanel(CytoPanelState cytoPanelState) {
		if (isFloating())
			DockCytoPanel();

		setVisible(false);

		// if our Parent Container is a BiModalSplitPane, hide the split
		Container parent = this.getParent();

		if (parent instanceof BiModalJSplitPane) {
			BiModalJSplitPane biModalSplitPane = (BiModalJSplitPane) parent;
			biModalSplitPane.setMode(cytoPanelState, BiModalJSplitPane.MODE_HIDE_SPLIT);
		}
	}
	
	/**
	 * Checks to make sure the CytoPanel is within the appropriate dimensions
	 * by overriding the sizes, if necessary
	 */
	private void checkSizes(Component comp) {
		if (compassDirection == CytoPanelName.WEST) {
			comp.setMinimumSize(new Dimension(WEST_MIN_WIDTH, WEST_MIN_HEIGHT));
		} else if (compassDirection == CytoPanelName.SOUTH) {
			comp.setMinimumSize(new Dimension(SOUTH_MIN_WIDTH, SOUTH_MIN_HEIGHT));
		} else if (compassDirection == CytoPanelName.EAST) {
			comp.setMinimumSize(new Dimension(EAST_MIN_WIDTH, EAST_MIN_HEIGHT));
		}
	}
	
	/**
	 * Size the divider to the currently selected panel's preferred Size
	 */
	private void resizeSelectedComponent() {
		/* 
		 * Set default resize behavior based on the currently selected panel's preferredSize setting
		 */
		Component panel = getTabbedPane().getSelectedComponent();
		// Make sure we're not being notified that we've deleted the last panel
		int width = 0;
		
		if (panel != null && cytoPanelContainer instanceof JSplitPane) {
			JSplitPane jsp = (JSplitPane)cytoPanelContainer;
			// if the panel is 0x0, it's probably not created, yet
			if (panel.getSize().width == 0 && panel.getSize().height == 0)
				return;

			if (panel.getPreferredSize() != null) {
				width = panel.getPreferredSize().width;
			}
			
			if (compassDirection == CytoPanelName.WEST) {
				if (width > WEST_MAX_WIDTH)
					width = WEST_MAX_WIDTH;
				else if (width < WEST_MIN_WIDTH)
					width = WEST_MIN_WIDTH;
				jsp.setDividerLocation(width+jsp.getInsets().left+jsp.getInsets().right);
			} else if (compassDirection == CytoPanelName.EAST) {
				if (width > EAST_MAX_WIDTH)
					width = EAST_MAX_WIDTH;
				else if (width < EAST_MIN_WIDTH)
					width = EAST_MIN_WIDTH;
				
				jsp.setDividerLocation(jsp.getSize().width
				                       -jsp.getInsets().right
				                       -jsp.getInsets().left
				                       -jsp.getDividerSize()
				                       -width);
			}
			// TODO: What's the right thing to do with SOUTH?
		}
	}

	private void init() {
		initLabel();
		initButtons();

		// add label and button components to yet another panel, 
		// so we can layout properly
		final JPanel floatDockPanel = new JPanel();
		final BoxLayout boxLayout = new BoxLayout(floatDockPanel, BoxLayout.X_AXIS);
		floatDockPanel.setLayout(boxLayout);
		
		floatDockPanel.add(Box.createHorizontalStrut(8));
		floatDockPanel.add(floatLabel);
		floatDockPanel.add(Box.createHorizontalGlue());
		floatDockPanel.add(switchCompButton);
		floatDockPanel.add(Box.createHorizontalStrut(8));
		floatDockPanel.add(floatButton);
		floatDockPanel.add(closeButton);
		floatDockPanel.add(Box.createHorizontalStrut(8));

		// set preferred size - we can use float or dock icon dimensions - they are the same
		final FontMetrics fm = floatLabel.getFontMetrics(floatLabel.getFont());
		floatDockPanel.setMinimumSize(new Dimension((fm.stringWidth(getTitle()) + BUTTON_SIZE)
				* FLOAT_PANEL_SCALE_FACTOR, BUTTON_SIZE));
		floatDockPanel.setPreferredSize(new Dimension((fm.stringWidth(getTitle()) + BUTTON_SIZE)
				* FLOAT_PANEL_SCALE_FACTOR, BUTTON_SIZE + 2));

		// use the border layout for this CytoPanel
		setLayout(new BorderLayout());
		add(floatDockPanel, BorderLayout.NORTH);
		add(getTabbedPane(), BorderLayout.CENTER);
	}
	
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane(tabPlacement);
			tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			tabbedPane.addChangeListener(this);
		}
		
		return tabbedPane;
	}

	private void initLabel() {
		floatLabel = new JLabel(getTitle());
		floatLabel.setFont(floatLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		floatLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
	}

	private void initButtons() {
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		
		switchCompButton = new JButton(ICON_CARET_DOWN);
		switchCompButton.setToolTipText(TOOL_TIP_SWITCH);
		CytoPanelUtil.styleButton(switchCompButton);
		switchCompButton.setFont(iconManager.getIconFont(14));
		switchCompButton.setSelected(true);
		updateSwitchCompButton();
		
		switchCompButton.addActionListener((ActionEvent e) -> {
			if (getTabbedPane().getTabCount() == 0)
				return;

			final JPopupMenu popupMenu = new JPopupMenu();
			final int count = getTabbedPane().getTabCount();
			
			for (int i = 0; i < count; i++) {
				final int idx = i;
				final String title = getTabbedPane().getTitleAt(idx);
				final Icon icon = getTabbedPane().getIconAt(idx);
				final JCheckBoxMenuItem mi = new JCheckBoxMenuItem(title, icon);
				mi.setEnabled(getTabbedPane().isEnabledAt(idx));
				mi.setSelected(getTabbedPane().getSelectedIndex() == idx);
				mi.addActionListener((ActionEvent evt) -> {
					setSelectedIndex(idx);
				});
				popupMenu.add(mi);
			}
			
			popupMenu.show(switchCompButton, 0, switchCompButton.getHeight());
			popupMenu.requestFocusInWindow();
		});
		
		floatButton = new JButton(ICON_SQUARE_O);
		floatButton.setToolTipText(TOOL_TIP_FLOAT);
		CytoPanelUtil.styleButton(floatButton);
		floatButton.setFont(iconManager.getIconFont(12));
		floatButton.setSelected(true);
		
		floatButton.addActionListener((ActionEvent e) -> {
			if (isFloating())
				DockCytoPanel();
			else
				FloatCytoPanel();
			
			notifyListeners(NOTIFICATION_STATE_CHANGE);
		});
		
		closeButton = new JButton(ICON_REMOVE);
		closeButton.setToolTipText(TOOL_TIP_CLOSE);
		CytoPanelUtil.styleButton(closeButton);
		closeButton.setFont(iconManager.getIconFont(13));
		closeButton.setSelected(true);
		
		closeButton.addActionListener((ActionEvent e) -> {
			setState(CytoPanelState.HIDE);
			notifyListeners(NOTIFICATION_STATE_CHANGE);
		});
	}

	private void FloatCytoPanel() {
		// show ourselves
		showCytoPanel(CytoPanelState.FLOAT);

		if (!isFloating()) {
			// new window to place this CytoPanel
			externalWindow = new JFrame(parent.getGraphicsConfiguration());
			
			// add listener to handle when window is closed
			externalWindow.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					DockCytoPanel();
					notifyListeners(NOTIFICATION_STATE_CHANGE);
				}
			});

			//  Add CytoPanel to the New External Window
			externalWindow.getContentPane().add(this, BorderLayout.CENTER);
			
			final Dimension windowSize = this.getSize();
			externalWindow.setSize(windowSize);
			externalWindow.validate();

			// set proper title of window
			externalWindow.setTitle(getTitle());

			// set proper button icon/text
			floatButton.setText(ICON_THUMB_TACK);
			floatButton.setToolTipText(TOOL_TIP_DOCK);

			// set float label text
			floatLabel.setText("");
			
			// set location of external window
			setLocationOfExternalWindow(externalWindow);
			// lets show it
			externalWindow.setVisible(true);

			// set our new state
			cytoPanelState = CytoPanelState.FLOAT;

			// re-layout
			validate();

			// SOUTH_WEST is used for manualLayout, it is nested in WEST
			if (compassDirection == CytoPanelName.SOUTH_WEST) {
				try {
					this.getParent().getParent().validate();
				} catch (Exception e) {
				}
			}
		}
	}

	private void DockCytoPanel() {
		// show ourselves
		showCytoPanel(CytoPanelState.DOCK);

		if (isFloating()) {
			// remove cytopanel from external view
			externalWindow.remove(this);

			// add this cytopanel back to cytopanel container
			if (cytoPanelContainer == null)
				logger.warn("cytoPanelContainer reference has not been set.");

			cytoPanelContainer.insertCytoPanel(this, compassDirection);

			// dispose of the external window
			externalWindow.dispose();
			externalWindow = null;

			// set proper button icon/text
			floatButton.setText("\uF096");
			floatButton.setToolTipText(TOOL_TIP_FLOAT);

			// set float label text
			floatLabel.setText(getTitle());

			// set our new state
			this.cytoPanelState = CytoPanelState.DOCK;

			// re-layout
			this.validate();

			// SOUTH_WEST is used for manualLayout, it is nested in WEST
			if (compassDirection == CytoPanelName.SOUTH_WEST) {
				try {
					this.getParent().getParent().validate();
				} catch (Exception e) {
				}
			}
		}
	}

	private boolean isFloating() {
		return (cytoPanelState == CytoPanelState.FLOAT);
	}

	private void setLocationOfExternalWindow(Window externalWindow) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenDimension = tk.getScreenSize();

		//  Get Absolute Location and Bounds, relative to Screen
		Rectangle containerBounds = cytoPanelContainer.getBounds();
		containerBounds.setLocation(cytoPanelContainer.getLocationOnScreen());

		Point p = CytoPanelUtil.getLocationOfExternalWindow(screenDimension, containerBounds,
		                                                   externalWindow.getSize(),
		                                                   compassDirection, false);

		externalWindow.setLocation(p);
		externalWindow.setVisible(true);
	}

	private void notifyListeners(int notificationType) {
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);

		// determine what event to fire
		switch (notificationType) {
			case NOTIFICATION_STATE_CHANGE:
				eventHelper.fireEvent(new CytoPanelStateChangedEvent(this, this, cytoPanelState));
				break;
	
			case NOTIFICATION_COMPONENT_SELECTED:
				int selectedIndex = getTabbedPane().getSelectedIndex();
				eventHelper.fireEvent(new CytoPanelComponentSelectedEvent(this, this, selectedIndex));
				break;
	
			case NOTIFICATION_COMPONENT_ADDED:
				break;
	
			case NOTIFICATION_COMPONENT_REMOVED:
				break;
		}
	}
	
	private void updateSwitchCompButton() {
		switchCompButton.setEnabled(getTabbedPane().getTabCount() > 0);
	}
}