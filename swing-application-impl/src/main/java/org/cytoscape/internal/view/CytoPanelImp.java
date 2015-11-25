package org.cytoscape.internal.view;

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

import static org.cytoscape.util.swing.IconManager.ICON_SQUARE_O;
import static org.cytoscape.util.swing.IconManager.ICON_THUMB_TACK;
import static org.cytoscape.util.swing.IconManager.ICON_REMOVE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import org.cytoscape.util.swing.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CytoPanel class extends JPanel to provide the following functionality:
 * <UL>
 * <LI> Floating/Docking of Panel.
 * <UL>
 *
 * CytoPanel also implements CytoPanel interface.
 *
 * @author Ethan Cerami, Benjamin Gross
 */
public class CytoPanelImp extends JPanel implements CytoPanel, ChangeListener {
	
	private final static long serialVersionUID = 1202339868245830L;
	
	private final static Logger logger = LoggerFactory.getLogger(CytoPanelImp.class);

	/**
	 * These are the minimum sizes for our CytoPanels.  A CytoPanel can't exceed these
	 * values.
	 */
	private static final int WEST_MIN_WIDTH = 100;
	private static final int WEST_MAX_WIDTH = 400;
	private static final int WEST_MIN_HEIGHT = 500;
	private static final int SOUTH_MIN_WIDTH = 500;
	private static final int SOUTH_MIN_HEIGHT = 50;
	
	private static final int EAST_MIN_WIDTH = 100;
	private static final int EAST_MAX_WIDTH = 1500;
	private static final int EAST_MIN_HEIGHT = 100;
	private static final int EAST_MAX_HEIGHT = 600;
	
	private static final int BUTTON_SIZE = 18;
	
	/**
	 * The JTabbedPane we hide.
	 */
	private JTabbedPane tabbedPane;

	/**
	 * Our state.
	 */
	private CytoPanelState cytoPanelState;

	/**
	 * Our compass direction.
	 */
	private CytoPanelName compassDirection;

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
	private JDialog externalWindow;

	/**
	 * The label which contains the tab title - not sure if its needed.
	 */
	private JLabel floatLabel;

	/**
	 * The float/dock button.
	 */
	private JButton floatButton;
	
	private JButton closeButton;

	/**
	 * The float/dock button.
	 */
	private static final int FLOAT_PANEL_SCALE_FACTOR = 2;

	/* the following constants should probably move into common constants class */

	//The float button tool tip.
	private static final String TOOL_TIP_FLOAT = "Float Window";

	// The dock button tool tip.
	private static final String TOOL_TIP_DOCK = "Dock Window";
	
	// The dock button tool tip.
	private static final String TOOL_TIP_CLOSE = "Close Window";

	private final CyEventHelper cyEventHelper;
	private final IconManager iconManager;
	private final JFrame parent;
	
	private final Map<String, CytoPanelComponent2> componentsById;

	/**
	 * Constructor.
	 *
	 * @param compassDirection  Compass direction of this CytoPanel.
	 * @param tabPlacement      Tab placement of this CytoPanel.
	 * @param cytoPanelState    The starting CytoPanel state.
	 */
	public CytoPanelImp(final CytoPanelName compassDirection,
						final int tabPlacement,
						final CytoPanelState cytoPanelState,
						final CyEventHelper eh,
						final CySwingApplication cySwingApp,
						final IconManager iconManager) {
		this.cyEventHelper = eh;
		this.parent = cySwingApp.getJFrame();
		componentsById = new HashMap<String, CytoPanelComponent2>();
		
		// setup our tabbed pane
		tabbedPane = new JTabbedPane(tabPlacement);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.addChangeListener(this);

		this.compassDirection = compassDirection;
		this.iconManager = iconManager;

		// construct our panel
		constructPanel();

		// to hidden by default 
		setState(cytoPanelState);
	}

	/**
	 * Sets CytoPanelContainer interface reference.
	 *
	 * @param cytoPanelContainer Reference to CytoPanelContainer
	 */
	public void setCytoPanelContainer(CytoPanelContainer cytoPanelContainer) {
		// set our cytoPanelContainerReference
		this.cytoPanelContainer = cytoPanelContainer;
	}

	/**
	 * Returns the proper title based on our compass direction.
	 *
	 * @return A title string
	 */
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
		tabbedPane.addTab(comp.getTitle(), comp.getIcon(), comp.getComponent());
		// send out a notification
		notifyListeners(NOTIFICATION_COMPONENT_ADDED);
	}

	/**
	 * Returns the number of components in the CytoPanel.
	 *
	 * @return int Number of components.
	 */
	@Override
	public int getCytoPanelComponentCount() {
		// return the number of tabs in the JTabbedPane.
		return tabbedPane.getTabCount();
	}

	/**
	 * Returns the currently selected component.
	 *
	 * @return component Currently selected Component reference.
	 */
	@Override
	public Component getSelectedComponent() {
		// get currently selected component in the JTabbedPane.
		return tabbedPane.getSelectedComponent();
	}

	/**
	 * Returns the component at index.
	 *
	 * @return component at the given index.
	 */
	@Override
	public Component getComponentAt(int index) {
		return tabbedPane.getComponentAt(index);
	}

	/**
	 * Returns the currently selected index.
	 *
	 * @return index Currently selected index.
	 */
	@Override
	public int getSelectedIndex() {
		// get currently selected component in the JTabbedPane.
		return tabbedPane.getSelectedIndex();
	}

	/**
	 * Returns the index for the specified component.
	 *
	 * @param component Component reference.
	 * @return int      Index of the Component or -1 if not found.
	 */
	@Override
	public int indexOfComponent(Component component) {
		// get the index from JTabbedPane
		return tabbedPane.indexOfComponent(component);
	}
	
	@Override
	public int indexOfComponent(String identifier) {
		final CytoPanelComponent cpComp = componentsById.get(identifier);
		
		return cpComp != null ? indexOfComponent(cpComp.getComponent()) : -1;
	}

	/**
	 * Removes specified component from the CytoPanel.
	 *
	 * @param component Component reference.
	 */
	@Override
	public void remove(Component component) {
		// remove tab from JTabbedPane (component)
		tabbedPane.remove(component);

		// send out a notification
		notifyListeners(NOTIFICATION_COMPONENT_REMOVED);
	}

	public void remove(CytoPanelComponent comp) {
		tabbedPane.remove(comp.getComponent());
		
		if (comp instanceof CytoPanelComponent2)
			componentsById.remove(((CytoPanelComponent2)comp).getIdentifier());
	}

	/**
	 * Removes the component from the CytoPanel at the specified index.
	 *
	 * @param index Component index.
	 */
	@Override
	public void remove(int index) {
		// remove tab from JTabbedPane (index)
		tabbedPane.remove(index);

		// send out a notification
		notifyListeners(NOTIFICATION_COMPONENT_REMOVED);
	}

	/**
	 * Removes all the components from the CytoPanel.
	 */
	@Override
	public void removeAll() {
		// remove all tabs and components from JTabbedPane
		tabbedPane.removeAll();
		componentsById.clear();

		// send out a notification
		notifyListeners(NOTIFICATION_COMPONENT_REMOVED);
	}

	/**
	 * Sets the selected index on the CytoPanel.
	 *
	 * @param index The desired index.
	 */
	@Override
	public void setSelectedIndex(final int index) {
		// set selected index
		if(tabbedPane.getTabCount()<=index)
			return;
		
		tabbedPane.setSelectedIndex(index);
		resizeSelectedComponent();
		// do not have to sent out notification - the tabbedPane will let us know.
	}

	/**
	 * Sets the state of the CytoPanel.
	 *
	 * @param cytoPanelState A CytoPanelState.
	 */
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

		// houston we have a problem
		if (!success) {
			// made it here, houston, we have a problem
			throw new IllegalArgumentException("Illegal Argument:  " + cytoPanelState
			                                   + ".  is unknown.  Please see CytoPanelState class.");
		}

		// set our new state
		this.cytoPanelState = cytoPanelState;

		// let our listeners know
		notifyListeners(NOTIFICATION_STATE_CHANGE);
	}

	/**
	 * Gets the state of the CytoPanel.
	 *
	 * @return A CytoPanelState.
	 */
	@Override
	public CytoPanelState getState() {
		return cytoPanelState;
	}

	/**
	 * Our implementation of the ChangeListener interface,
	 * to determine when new tab has been selected
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		// Handle the resize
		resizeSelectedComponent();
		
		// let our listeners know
		notifyListeners(NOTIFICATION_COMPONENT_SELECTED);
	}

	/**
	 * Shows the CytoPanel.
	 */
	private void showCytoPanel(CytoPanelState cytoPanelState) {
		// make ourselves visible
		setVisible(true);

		//  if our parent is a BiModalSplitPane, show the split
		Container parent = this.getParent();

		if (parent instanceof BiModalJSplitPane) {
			BiModalJSplitPane biModalSplitPane = (BiModalJSplitPane) parent;
			biModalSplitPane.setMode(cytoPanelState, BiModalJSplitPane.MODE_SHOW_SPLIT);
		}
	}

	/**
	 * Hides the CytoPanel.
	 */
	private void hideCytoPanel(CytoPanelState cytoPanelState) {
		// dock ourselves
		if (isFloating()) {
			DockCytoPanel();
		}

		// hide ourselves
		setVisible(false);

		//  if our Parent Container is a BiModalSplitPane, hide the split
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
		 * Set default resize behavior based on the currently
		 * selected panel's preferredSize setting
		 */
		Component panel = tabbedPane.getSelectedComponent();
		// Make sure we're not being notified that we've deleted
		// the last panel
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

	/**
	 * Constructs this CytoPanel.
	 */
	private void constructPanel() {
		// init our components
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
		floatDockPanel.add(floatButton);
		floatDockPanel.add(closeButton);
		floatDockPanel.add(Box.createHorizontalStrut(8));

		// set preferred size - we can use float or dock icon dimensions - they are the same
		final FontMetrics fm = floatLabel.getFontMetrics(floatLabel.getFont());
		floatDockPanel.setMinimumSize(new Dimension((fm.stringWidth(getTitle()) + BUTTON_SIZE)
				* FLOAT_PANEL_SCALE_FACTOR, BUTTON_SIZE));
		floatDockPanel.setPreferredSize(new Dimension((fm.stringWidth(getTitle()) + BUTTON_SIZE)
				* FLOAT_PANEL_SCALE_FACTOR, BUTTON_SIZE + 10));

		// use the border layout for this CytoPanel
		setLayout(new BorderLayout());
		add(floatDockPanel, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
	}

	/**
	 * Add a component to the CytoPanel just below the TabbedPane.
	 *
	 * @param pComponent    the component to be added.
	 */
	public void addComponentToSouth(Component pComponent) {
		add(pComponent, BorderLayout.SOUTH);
	}

	/**
	 * Remove a component from the CytoPanel just below the TabbedPane.
	 *
	 * @param pComponent  the component to be removed.
	 */
	public void removeComponentAtSouth(Component pComponent) {
		remove(pComponent);
	}

	/**
	 * Initializes the label.
	 */
	private void initLabel() {
		floatLabel = new JLabel(getTitle());
		floatLabel.setFont(floatLabel.getFont().deriveFont(12.0f));
		floatLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
	}

	/**
	 * Initializes the button.
	 */
	private void initButtons() {
		// Create Float / Dock Button
		floatButton = new JButton(ICON_SQUARE_O);
		floatButton.setToolTipText(TOOL_TIP_FLOAT);
		styleButton(floatButton);
		floatButton.setFont(iconManager.getIconFont(12));
		floatButton.setSelected(true);
		
		// Create close button
		closeButton = new JButton(ICON_REMOVE);
		closeButton.setToolTipText(TOOL_TIP_CLOSE);
		styleButton(closeButton);
		closeButton.setFont(iconManager.getIconFont(13));
		closeButton.setSelected(true);
		
		floatButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isFloating())
					DockCytoPanel();
				else
					FloatCytoPanel();
				
				notifyListeners(NOTIFICATION_STATE_CHANGE);
			}
		});
		
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setState(CytoPanelState.HIDE);
				notifyListeners(NOTIFICATION_STATE_CHANGE);
			}
		});
	}

	/**
	 * Float cytoPanel
	 */
	private void FloatCytoPanel() {
		// show ourselves
		showCytoPanel(CytoPanelState.FLOAT);

		if (!isFloating()) {
			// new window to place this CytoPanel
			externalWindow = new JDialog(parent);
			
			// add listener to handle when window is closed
			addWindowListener();

			//  Add CytoPanel to the New External Window
			Container contentPane = externalWindow.getContentPane();
			contentPane.add(this, BorderLayout.CENTER);
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
			this.cytoPanelState = CytoPanelState.FLOAT;

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

	/**
	 * Dock cytoPanel
	 */
	private void DockCytoPanel() {
		// show ourselves
		showCytoPanel(CytoPanelState.DOCK);

		if (isFloating()) {
			// remove cytopanel from external view
			externalWindow.remove(this);

			// add this cytopanel back to cytopanel container
			if (cytoPanelContainer == null) {
				logger.warn("cytoPanelContainer reference has not been set.");
			}

			cytoPanelContainer.insertCytoPanel(this, compassDirection);

			// dispose of the external window
			externalWindow.dispose();

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

	/**
	 * Are we floating ?
	 */
	private boolean isFloating() {
		return (cytoPanelState == CytoPanelState.FLOAT);
	}

	/**
	 * Adds the listener to the floating window.
	 */
	private void addWindowListener() {
		externalWindow.addWindowListener(new WindowAdapter() {
				/**
				 * Window is Closing.
				 *
				 * @param e Window Event.
				 */
				public void windowClosing(WindowEvent e) {
					DockCytoPanel();
					notifyListeners(NOTIFICATION_STATE_CHANGE);
				}
			});
	}

	/**
	 * Sets the Location of the External Window.
	 *
	 * @param externalWindow ExternalWindow Object.
	 */
	private void setLocationOfExternalWindow(JDialog externalWindow) {
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

	/**
	 * Code to notify our listeners of some particular event.
	 *
	 * @param notificationType What type of notification to perform.
	 */
	private void notifyListeners(int notificationType) {
			// determine what event to fire
			switch (notificationType) {
				case NOTIFICATION_STATE_CHANGE:
					cyEventHelper.fireEvent(new CytoPanelStateChangedEvent(this, this, cytoPanelState));
					break;

				case NOTIFICATION_COMPONENT_SELECTED:
					int selectedIndex = tabbedPane.getSelectedIndex();
					cyEventHelper.fireEvent(new CytoPanelComponentSelectedEvent(this,this,selectedIndex));
					break;

				case NOTIFICATION_COMPONENT_ADDED:
					break;

				case NOTIFICATION_COMPONENT_REMOVED:
					break;
			}
	}

	
	@Override
	public Component getThisComponent() {
		return this;
	}
	
	private static void styleButton(final AbstractButton btn) {
		btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		btn.setContentAreaFilled(false);
		btn.setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
		btn.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
		btn.setSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
		btn.setRolloverEnabled(false);
		btn.setFocusPainted(false);
		btn.setFocusable(false);
		btn.setContentAreaFilled(false);
	}
}