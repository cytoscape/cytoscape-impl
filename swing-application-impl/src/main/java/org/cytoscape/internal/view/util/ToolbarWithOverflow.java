package org.cytoscape.internal.view.util;

import static org.cytoscape.util.swing.IconManager.ICON_ANGLE_DOUBLE_DOWN;
import static org.cytoscape.util.swing.IconManager.ICON_ANGLE_DOUBLE_RIGHT;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * ToolbarWithOverflow provides a component which is useful for displaying commonly used
 * actions.  It adds an overflow button when the toolbar becomes too small to show all the
 * available actions.
 * <br>
 * Adapted from the original org.openide.awt.ToolbarWithOverflow--basically, just removed the org.openide.util.Mutex
 * and other NetBeans specific dependencies, besides some minor UI modifications.
 * 
 * @author Th. Oikonomou
 * @since 7.51
 */
@SuppressWarnings("serial")
public class ToolbarWithOverflow extends JToolBar {

	private JButton overflowButton;
	private JPopupMenu popup;
	private JToolBar overflowToolbar;
	
	private boolean displayOverflowOnHover = true;
	
	private final String PROP_DRAGGER = "_toolbar_dragger_"; // NOI18N
	
	private AWTEventListener awtEventListener;
	private ComponentAdapter componentAdapter;
	
	protected final CyServiceRegistrar serviceRegistrar;
	
	/**
	 *  keep track of the overflow popup that is showing, possibly from another overflow button,
	 *  in order to hide it if necessary
	 */
	private static JPopupMenu showingPopup;

	/**
	 * Creates a new tool bar; orientation defaults to <code>HORIZONTAL</code>.
	 */
	public ToolbarWithOverflow(CyServiceRegistrar serviceRegistrar) {
		this(HORIZONTAL, serviceRegistrar);
	}

	/**
	 * Creates a new tool bar with the specified <code>orientation</code>. The
	 * <code>orientation</code> must be either <code>HORIZONTAL</code> or <code>VERTICAL</code>.
	 *
	 * @param orientation the orientation desired
	 */
	public ToolbarWithOverflow(int orientation, CyServiceRegistrar serviceRegistrar) {
		this(null, orientation, serviceRegistrar);
	}

	/**
	 * Creates a new tool bar with the specified <code>name</code>. The name is used
	 * as the title of the undocked tool bar. The default orientation is <code>HORIZONTAL</code>.
	 *
	 * @param name the name of the tool bar
	 */
	public ToolbarWithOverflow(String name, CyServiceRegistrar serviceRegistrar) {
		this(name, HORIZONTAL, serviceRegistrar);
	}

	/**
	 * Creates a new tool bar with a specified <code>name</code> and
	 * <code>orientation</code>. All other constructors call this constructor.
	 * If <code>orientation</code> is an invalid value, an exception will be thrown.
	 *
	 * @param name        the name of the tool bar
	 * @param orientation the initial orientation -- it must be * either <code>HORIZONTAL</code> or <code>VERTICAL</code>
	 * @exception IllegalArgumentException if orientation is neither <code>HORIZONTAL</code> nor <code>VERTICAL</code>
	 */
	public ToolbarWithOverflow(String name, int orientation, CyServiceRegistrar serviceRegistrar) {
		super(name, orientation);
		this.serviceRegistrar = serviceRegistrar;
		
		setupOverflowButton();
		popup = new JPopupMenu();
		popup.setBorderPainted(false);
		popup.setBorder(BorderFactory.createEmptyBorder());
		overflowToolbar = new JToolBar("overflowToolbar", orientation == HORIZONTAL ? VERTICAL : HORIZONTAL);
		overflowToolbar.setFloatable(false);
		overflowToolbar.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"), 1));
	}

	private ComponentListener getComponentListener() {
		if (componentAdapter == null) {
			componentAdapter = new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					maybeAddOverflow();
				}
			};
		}
		
		return componentAdapter;
	}

	private AWTEventListener getAWTEventListener() {
		if (awtEventListener == null) {
			awtEventListener = new AWTEventListener() {
				@Override
				public void eventDispatched(AWTEvent evt) {
					var me = (MouseEvent) evt;
					
					if (isVisible() && !isShowing() && popup.isShowing()) {
						showingPopup = null;
						popup.setVisible(false);
						return;
					}
					
					if (evt.getSource() == popup) {
						if (popup.isShowing() && me.getID() == MouseEvent.MOUSE_EXITED) {
							int minX = popup.getLocationOnScreen().x;
							int maxX = popup.getLocationOnScreen().x + popup.getWidth();
							int minY = popup.getLocationOnScreen().y;
							int maxY = popup.getLocationOnScreen().y + popup.getHeight();
							
							if (me.getXOnScreen() < minX || me.getXOnScreen() >= maxX || me.getYOnScreen() < minY
									|| me.getYOnScreen() >= maxY) {
								showingPopup = null;
								popup.setVisible(false);
							}
						}
					} else {
						if (popup.isShowing() && overflowButton.isShowing()
								&& (me.getID() == MouseEvent.MOUSE_MOVED || me.getID() == MouseEvent.MOUSE_EXITED)) {
							int minX = overflowButton.getLocationOnScreen().x;
							int maxX = getOrientation() == HORIZONTAL ? minX + popup.getWidth()
									: minX + overflowButton.getWidth() + popup.getWidth();
							int minY = overflowButton.getLocationOnScreen().y;
							int maxY = getOrientation() == HORIZONTAL
									? minY + overflowButton.getHeight() + popup.getHeight()
									: minY + popup.getHeight();
							
							if (me.getXOnScreen() < minX || me.getYOnScreen() < minY || me.getXOnScreen() > maxX
									|| me.getYOnScreen() > maxY) {
								showingPopup = null;
								popup.setVisible(false);
							}
						}
					}
				}
			};
		}
		
		return awtEventListener;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		
		addComponentListener(getComponentListener());
		Toolkit.getDefaultToolkit().addAWTEventListener(getAWTEventListener(),
				AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		
		if (componentAdapter != null)
			removeComponentListener(componentAdapter);
		
		if (awtEventListener != null)
			Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
	}

	/**
	 * Returns whether the overflow should be displayed on hover or not. The default value is <code>true</code>.
	 *
	 * @return <code>true</code> if overflow is displayed on hover;
	 *         <code>false</code> otherwise
	 */
	public boolean isDisplayOverflowOnHover() {
		return displayOverflowOnHover;
	}

	/**
	 * Sets whether the overflow should be displayed on hover or not. The default value is <code>true</code>.
	 *
	 * @param displayOverflowOnHover if <code>true</code>, the overflow will be
	 *                               displayed on hover; <code>false</code>
	 *                               otherwise
	 */
	public void setDisplayOverflowOnHover(boolean displayOverflowOnHover) {
		this.displayOverflowOnHover = displayOverflowOnHover;
		setupOverflowButton();
	}

	@Override
	public Dimension getPreferredSize() {
		var comps = getAllComponents();
		var insets = getInsets();
		int width = null == insets ? 0 : insets.left + insets.right;
		int height = null == insets ? 0 : insets.top + insets.bottom;
		
		for (int i = 0; i < comps.length; i++) {
			var c = comps[i];
			
			if (!c.isVisible())
				continue;
			
			width += getOrientation() == HORIZONTAL ? c.getPreferredSize().width : c.getPreferredSize().height;
			height = Math.max(height,
					(getOrientation() == HORIZONTAL
							? (c.getPreferredSize().height + (insets == null ? 0 : insets.top + insets.bottom))
							: (c.getPreferredSize().width) + (insets == null ? 0 : insets.left + insets.right)));
		}
			
		if (overflowToolbar.getComponentCount() > 0)
			width += getOrientation() == HORIZONTAL ? overflowButton.getPreferredSize().width
					: overflowButton.getPreferredSize().height;
		
		var dim = getOrientation() == HORIZONTAL ? new Dimension(width, height) : new Dimension(height, width);
		
		return dim;
	}

	@Override
	public void setOrientation(int o) {
		super.setOrientation(o);
		
		if (serviceRegistrar != null) // Have we been initialized yet?
			setupOverflowButton();
	}

	@Override
	public void removeAll() {
		super.removeAll();
		overflowToolbar.removeAll();
	}

	@Override
	public void validate() {
		int visibleButtons = computeVisibleButtons();
		
		if (visibleButtons == -1)
			handleOverflowRemoval();
		else
			handleOverflowAddittion(visibleButtons);
		
		super.validate();
	}

	private void setupOverflowButton() {
		var iconManager = serviceRegistrar.getService(IconManager.class);
		var iconText = getOrientation() == HORIZONTAL ? ICON_ANGLE_DOUBLE_DOWN : ICON_ANGLE_DOUBLE_RIGHT;
		var icon = new TextIcon(iconText, iconManager.getIconFont(16.0f), 24, 24);
		
		overflowButton = new JButton(icon);
		overflowButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

		overflowButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (popup.isShowing()) {
					showingPopup = null;
					popup.setVisible(false);
				} else {
					displayOverflow();
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				if (showingPopup != null && showingPopup != popup) {
					showingPopup.setVisible(false);
					showingPopup = null;
				}

				if (displayOverflowOnHover)
					displayOverflow();
			}
		});
	}

	private void displayOverflow() {
		if (!overflowButton.isShowing())
			return;
		
		int x = getOrientation() == HORIZONTAL ? overflowButton.getLocationOnScreen().x
				: overflowButton.getLocationOnScreen().x + overflowButton.getWidth();
		int y = getOrientation() == HORIZONTAL ? overflowButton.getLocationOnScreen().y + overflowButton.getHeight()
				: overflowButton.getLocationOnScreen().y;
		popup.setLocation(x, y);
		showingPopup = popup;
		popup.setVisible(true);
	}

	/**
	 * Determines if an overflow button should be added to or removed from the toolbar.
	 */
	private void maybeAddOverflow() {
		validate();
		repaint();
	}

	private int computeVisibleButtons() {
		if (isShowing()) {
			int w = getOrientation() == HORIZONTAL ? overflowButton.getIcon().getIconWidth() + 4
					: getWidth() - getInsets().left - getInsets().right;
			int h = getOrientation() == HORIZONTAL ? getHeight() - getInsets().top - getInsets().bottom
					: overflowButton.getIcon().getIconHeight() + 4;
			overflowButton.setMaximumSize(new Dimension(w, h));
			overflowButton.setMinimumSize(new Dimension(w, h));
			overflowButton.setPreferredSize(new Dimension(w, h));
		}
		
		var comps = getAllComponents();
		int sizeSoFar = 0;
		int maxSize = getOrientation() == HORIZONTAL ? getWidth() : getHeight();
		int overflowButtonSize = getOrientation() == HORIZONTAL ? overflowButton.getPreferredSize().width
				: overflowButton.getPreferredSize().height;
		int showingButtons = 0; // all that return true from isVisible()
		int visibleButtons = 0; // all visible that fit into the given space (maxSize)
		var insets = getInsets();
		
		if (null != insets)
			sizeSoFar = getOrientation() == HORIZONTAL ? insets.left + insets.right : insets.top + insets.bottom;
		
		for (int i = 0; i < comps.length; i++) {
			var c = comps[i];
			
			if (!c.isVisible())
				continue;
			
			if (showingButtons == visibleButtons) {
				int size = getOrientation() == HORIZONTAL ? c.getPreferredSize().width : c.getPreferredSize().height;
				
				if (sizeSoFar + size <= maxSize) {
					sizeSoFar += size;
					visibleButtons++;
				}
			}
			
			showingButtons++;
		}
		
		if (visibleButtons < showingButtons && visibleButtons > 0 && sizeSoFar + overflowButtonSize > maxSize)
			visibleButtons--; // overflow button needed but would not have enough space, remove one more button
		
		if (visibleButtons == 0 && comps.length > 0 && comps[0] instanceof JComponent
				&& Boolean.TRUE.equals(((JComponent) comps[0]).getClientProperty(PROP_DRAGGER)))
			visibleButtons = 1; // always include the dragger if present
		
		if (visibleButtons == showingButtons)
			visibleButtons = -1;
		
		return visibleButtons;
	}

	private void handleOverflowAddittion(int visibleButtons) {
		var comps = getAllComponents();
		removeAll();
		overflowToolbar.setOrientation(getOrientation() == HORIZONTAL ? VERTICAL : HORIZONTAL);
		popup.removeAll();

		for (var c : comps) {
			if (visibleButtons > 0) {
				add(c);

				if (c.isVisible())
					visibleButtons--;
			} else {
				overflowToolbar.add(c);
			}
		}
		
		popup.add(overflowToolbar);
		add(overflowButton);
	}

	private void handleOverflowRemoval() {
		if (overflowToolbar.getComponents().length > 0) {
			remove(overflowButton);
			
			for (var c : overflowToolbar.getComponents())
				add(c);
			
			overflowToolbar.removeAll();
			popup.removeAll();
		}
	}

	private Component[] getAllComponents() {
		final Component[] toolbarComps;
		var overflowComps = overflowToolbar.getComponents();
		
		if (overflowComps.length == 0) {
			toolbarComps = getComponents();
		} else {
			if (getComponentCount() > 0) {
				toolbarComps = new Component[getComponents().length - 1];
				System.arraycopy(getComponents(), 0, toolbarComps, 0, toolbarComps.length);
			} else {
				toolbarComps = new Component[0];
			}
		}
		
		var comps = new Component[toolbarComps.length + overflowComps.length];
		System.arraycopy(toolbarComps, 0, comps, 0, toolbarComps.length);
		System.arraycopy(overflowComps, 0, comps, toolbarComps.length, overflowComps.length);
		
		return comps;
	}
}
