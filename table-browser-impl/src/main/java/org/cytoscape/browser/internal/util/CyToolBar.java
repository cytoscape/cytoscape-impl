package org.cytoscape.browser.internal.util;

import static org.cytoscape.util.swing.IconManager.ICON_ANGLE_DOUBLE_DOWN;
import static org.cytoscape.util.swing.IconManager.ICON_ANGLE_DOUBLE_RIGHT;
import static org.cytoscape.util.swing.LookAndFeelUtil.isWinLAF;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.FilteredImageSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.CyToolTip;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * Implementation of toolbar on the Cytoscape Desktop application that creates buttons for registered
 * toolbar {@link CyAction}s and {@link ToolBarComponent}s.
 * <br>
 * It also adds an overflow button when the toolbar becomes too small to show all the available actions.
 * This feature was adapted from org.openide.awt.ToolbarWithOverflow, used by NetBeans.
 */
@SuppressWarnings("serial")
public class CyToolBar extends JToolBar {
	
	private static int BUTTON_BORDER_SIZE = 2;
	private static int DEF_ICON_WIDTH = 32;
	private static int DEF_ICON_HEIGHT = 32;
	
	protected final int iconWidth;
	protected final int iconHeight;
	
	protected final List<ToolBarItem> orderedItems = new ArrayList<>();
	protected final Map<ToolBarComponent, ToolBarItem> toolBarComponentMap = new HashMap<>();
	protected final Map<CyAction, ToolBarItem> actionMap = new HashMap<>();

	protected JButton overflowButton;
	protected final JPopupMenu overflowPopup;
	protected final JToolBar overflowToolBar;
	
	/** So the popup can be hidden when clicking the overflow button again */
	private long lastTimeOverflowPopupClosed;
	
	private boolean isAdjusting;
	
	private AWTEventListener awtEventListener;
	private ComponentAdapter componentAdapter;
	
	protected final CyServiceRegistrar serviceRegistrar;
	
	public CyToolBar(String name, int orientation, CyServiceRegistrar serviceRegistrar) {
		this(name, orientation, DEF_ICON_WIDTH, DEF_ICON_HEIGHT, serviceRegistrar);
	}
	
	public CyToolBar(String name, int orientation, int iconWidth, int iconHeight, CyServiceRegistrar serviceRegistrar) {
		super(name);
		
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
		this.serviceRegistrar = serviceRegistrar;
		
		setFloatable(false);
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")),
				BorderFactory.createEmptyBorder(0, 10, 0, 10)
		));
		
		// Initialize overflow feature
		overflowPopup = new JPopupMenu();
		overflowPopup.setBorderPainted(false);
		overflowPopup.setBorder(BorderFactory.createEmptyBorder());
		
		overflowPopup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
				lastTimeOverflowPopupClosed = System.currentTimeMillis();
			}
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
				// Ignore...
			}
			@Override
			public void popupMenuCanceled(PopupMenuEvent evt) {
				// Ignore...
			}
		});
		
		setupOverflowButton();
		
		overflowToolBar = new JToolBar("overflowToolBar", getOrientation() == HORIZONTAL ? VERTICAL : HORIZONTAL);
		overflowToolBar.setFloatable(false);
		overflowToolBar.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"), 1));
	}
	
	public int getIconWidth() {
		return iconWidth;
	}
	
	public int getIconHeight() {
		return iconHeight;
	}
	
	@Override
	public void addSeparator() {
		// Hide previous duplicated separator, if there is one
		int n = getComponentCount();
		
		if (n > 1) {
			var previous = getComponent(n - 1);
			
			if (previous instanceof JSeparator)
				previous.setVisible(false);
		}
		
		super.addSeparator();
	}
	
	@Override
	public void removeAll() {
		super.removeAll();
		overflowToolBar.removeAll();
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		
		addComponentListener(getComponentListener());
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		
		if (componentAdapter != null)
			removeComponentListener(componentAdapter);
		
		if (awtEventListener != null)
			Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
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
			
		if (overflowToolBar.getComponentCount() > 0)
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
	public void validate() {
		if (!isAdjusting) {
			var visibleComps = new LinkedList<Component>();
			var totalVisible = computeVisibleComponents(visibleComps);
	
			if (totalVisible == -1)
				handleOverflowRemoval();
			else
				handleOverflowAddittion(visibleComps);
		}

		super.validate();
	}
	
	/**
	 * If the given Action has an absent or false inToolBar property, return;
	 * otherwise toolBarComponent to addAction( String, Action ) with the value of its
	 * gravity property.
	 */
	public AbstractButton addAction(CyAction action) {
		// At present we allow an Action to be in this tool bar only once.
		if (actionMap.containsKey(action))
			return null;

		boolean sepBefore = false;
		boolean sepAfter = false;
		
		if (action instanceof AbstractCyAction) {
			sepBefore = ((AbstractCyAction) action).insertToolbarSeparatorBefore();
			sepAfter = ((AbstractCyAction) action).insertToolbarSeparatorAfter();
		}
		
		float gravity = action.getToolbarGravity();

		var button = createToolBarButton(action, iconWidth, iconHeight);
		var item = new ToolBarItem(button, gravity, sepBefore, sepAfter);
		
		actionMap.put(action, item);
		int index = indexOf(gravity);
		orderedItems.add(index, item);
		
		update();

		return button;
	}
	
	/**
	 * If the given Action has an absent or false inToolBar property, return;
	 * otherwise if there's a button for the action, remove it.
	 */
	public boolean removeAction(CyAction action) {
		var item = actionMap.remove(action);

		if (item == null)
			return false;

		orderedItems.remove(item);
		update();

		return true;
	}
	
	public void addToolBarComponent(ToolBarComponent tbc, Map<?, ?> props) {
		if (toolBarComponentMap.containsKey(tbc))
			return;
		
		boolean sepBefore = "true".equals(props.get(INSERT_SEPARATOR_BEFORE));
		boolean sepAfter = "true".equals(props.get(INSERT_SEPARATOR_AFTER));
		int index = indexOf(tbc.getToolBarGravity());
		
		var item = new ToolBarItem(tbc, sepBefore, sepAfter);
		
		toolBarComponentMap.put(tbc, item);
		orderedItems.add(index, item);
		
		update();
	}
	
	public boolean removeToolBarComponent(ToolBarComponent tbc) {
		var item = toolBarComponentMap.remove(tbc);
		
		if (item == null)
			return false;
		
		orderedItems.remove(item);
		update();
		
		return true;
	}
	
	public Component getComponent(CyAction action) {
		var item = actionMap.get(action);
		
		return item != null ? item.getComponent() : null;
	}

	public void showAll() {
		isAdjusting = true;

		try {
			for (var item : orderedItems) {
				var c = item.getComponent();

				if (c != null)
					c.setVisible(true);
			}
		} finally {
			isAdjusting = false;
			validate();
		}

		updateSeparators(this);
	}

	public void hideAll() {
		isAdjusting = true;

		try {
			for (var item : orderedItems) {
				var c = item.getComponent();

				if (c != null && isOverflowAllowed(c))
					c.setVisible(false);
			}
		} finally {
			isAdjusting = false;
			validate();
		}

		updateSeparators(this);
	}

	public void addSeparator(float gravity) {
		int index = indexOf(gravity);
		orderedItems.add(index, new SeparatorToolBarItem(gravity));
	}
	
	public void addSpacer(float gravity) {
		int index = indexOf(gravity);
		orderedItems.add(index, new SpacerToolBarItem(gravity));
	}

	/**
	 * Used by toolbar updater to keep things properly enabled/disabled.
	 */
	public Collection<CyAction> getAllToolBarActions() {
		return new LinkedHashSet<>(actionMap.keySet());
	}
	
	public Collection<ToolBarComponent> getAllToolBarComponents() {
		return new LinkedHashSet<>(toolBarComponentMap.keySet());
	}
	
	protected void update() {
		isAdjusting = true;
		
		try {
			// Remove and add everything again
			removeAll();
			
			for (var item : orderedItems) {
				var c = item.getComponent();
				
				if (c != null)
					add(this, c, item.isSeparatorBefore(), item.isSeparatorAfter());
				else if (item instanceof SeparatorToolBarItem)
					addSeparator();
				else if (item instanceof SpacerToolBarItem)
					addSpacer();
			}
			
			// Hide duplicate separators
			updateSeparators(this);
		} finally {
			isAdjusting = false;
			validate();
		}
	}

	protected void add(JToolBar toolBar, Component c, boolean sepBefore, boolean sepAfter) {
		if (sepBefore)
			toolBar.addSeparator();
		
		toolBar.add(c);
		
		if (sepAfter)
			toolBar.addSeparator();
	}
	
	private void addSpacer() {
		add(getOrientation() == HORIZONTAL ? Box.createHorizontalGlue() : Box.createVerticalGlue());
	}

	protected int indexOf(float newGravity) {
		for (int i = 0; i < orderedItems.size(); i++) {
			var item = orderedItems.get(i);
			float gravity = item.getGravity();
			
			if (gravity >= 0 && newGravity < gravity)
				return i;
		}
		
		return orderedItems.size();
	}
	
	protected void updateSeparators(JToolBar toolBar) {
		if (toolBar == this)
			isAdjusting = true;
			
		// Pretend we start with a separator, because we don't want any separator as the first component
		boolean lastIsSep = true;
		boolean changed = false;
		
		try {
			for (var c : toolBar.getComponents()) {
				if (c instanceof JSeparator) {
					var oldVisible = c.isVisible();
					c.setVisible(!lastIsSep);
					
					if (oldVisible != c.isVisible())
						changed = true;
				}
				
				if (c.isVisible())
					lastIsSep = c instanceof JSeparator;
			}
		} finally {
			if (toolBar == this) {
				isAdjusting = false;

				if (changed)
					toolBar.validate();
			}
		}
	}
	
	private ComponentListener getComponentListener() {
		if (componentAdapter == null) {
			componentAdapter = new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent evt) {
					maybeAddOverflow();
					
					if (overflowPopup != null && overflowPopup.isShowing())
						overflowPopup.setVisible(false);
				}
			};
		}
		
		return componentAdapter;
	}

	private void setupOverflowButton() {
		if (overflowPopup != null && overflowPopup.isShowing())
			overflowPopup.setVisible(false);
		
		var iconManager = serviceRegistrar.getService(IconManager.class);
		var iconText = getOrientation() == HORIZONTAL ? ICON_ANGLE_DOUBLE_DOWN : ICON_ANGLE_DOUBLE_RIGHT;
		var icon = new TextIcon(iconText, iconManager.getIconFont(16.0f), 24, 24);

		overflowButton = new JButton(icon);
		overflowButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		overflowButton.setFocusable(false);
		overflowButton.setFocusPainted(false);
		overflowButton.setContentAreaFilled(false);

		overflowButton.addActionListener(evt -> {
			if ((System.currentTimeMillis() - lastTimeOverflowPopupClosed) > 250)
				displayOverflow();
		});
	}
		
	private void displayOverflow() {
		if (!overflowButton.isShowing())
			return;
		
		if (overflowPopup.isVisible()) {
			int x = getOrientation() == HORIZONTAL
					? overflowButton.getLocationOnScreen().x
					: overflowButton.getLocationOnScreen().x + overflowButton.getWidth();
			int y = getOrientation() == HORIZONTAL
					? overflowButton.getLocationOnScreen().y + overflowButton.getHeight()
					: overflowButton.getLocationOnScreen().y;
			
			overflowPopup.setLocation(x, y);
		} else {
			int x = getOrientation() == HORIZONTAL ? 0 : overflowButton.getWidth();
			int y = getOrientation() == HORIZONTAL ? overflowButton.getHeight() : 0;
			
			overflowPopup.show(overflowButton, x, y);
		}
	}

	/**
	 * Determines if an overflow button should be added to or removed from the toolbar.
	 */
	private void maybeAddOverflow() {
		validate();
		repaint();
	}

	private int computeVisibleComponents(LinkedList<Component> visibleComps) {
		updateSeparators(this);
		
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
		int totalShowing = 0; // all that return true from isVisible()
		int totalVisible = 0; // all visible that fit into the given space (maxSize)
		var insets = getInsets();
		
		if (insets != null)
			sizeSoFar = getOrientation() == HORIZONTAL ? insets.left + insets.right : insets.top + insets.bottom;
		
		for (var c : comps) {
			if (!c.isVisible())
				continue;
			
			var compSize = c.getPreferredSize();
			int size = getOrientation() == HORIZONTAL ? compSize.width : compSize.height;
			
			if (!isOverflowAllowed(c) || (totalShowing == totalVisible && sizeSoFar + size <= maxSize)) {
				visibleComps.add(c);
				totalVisible++;
				sizeSoFar += size;
			}
			
			totalShowing++;
		}
		
		if (totalVisible < totalShowing) {
			// Overflow button needed but would not have enough space, remove one more item
			var iter = visibleComps.descendingIterator();
			
			while (iter.hasNext() && totalVisible > 0 && sizeSoFar + overflowButtonSize > maxSize) {
			    var c = iter.next();
			    
			    if (isOverflowAllowed(c)) {
			    	var compSize = c.getPreferredSize();
					int size = getOrientation() == HORIZONTAL ? compSize.width : compSize.height;
					sizeSoFar -= size;
			    	
			    	iter.remove();
			    	totalVisible--;
			    }
			}
		}
		
		if (totalVisible == totalShowing)
			totalVisible = -1;
		
		return totalVisible;
	}

	private void handleOverflowAddittion(List<Component> visibleComps) {
		isAdjusting = true;
		
		try {
			removeAll();
			overflowToolBar.setOrientation(getOrientation() == HORIZONTAL ? VERTICAL : HORIZONTAL);
			overflowPopup.removeAll();
	
			for (var item : orderedItems) {
				if (item instanceof SpacerToolBarItem)
					continue;
				
				if (item instanceof SeparatorToolBarItem) {
					addSeparator();
					continue;
				}
				
				var c = item.getComponent();
				
				if (c == null)
					continue;
				
				if (!c.isVisible() || visibleComps.contains(c))
					add(this, c, item.isSeparatorBefore(), item.isSeparatorAfter());
				else
					add(overflowToolBar, c, item.isSeparatorBefore(), item.isSeparatorAfter());
			}
			
			overflowPopup.add(overflowToolBar);
			add(overflowButton);
			
			validate();
			updateSeparators(overflowToolBar);
		} finally {
			isAdjusting = false;
		}
	}

	private void handleOverflowRemoval() {
		var comps = overflowToolBar.getComponents();
		
		if (comps.length > 0) {
			remove(overflowButton);
			overflowToolBar.removeAll();
			overflowPopup.removeAll();
			
			update();
		}
	}

	private Component[] getAllComponents() {
		final Component[] toolbarComps;
		var overflowComps = overflowToolBar.getComponents();
		
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
	
	private boolean isOverflowAllowed(Component comp) {
		// We only want to overflow buttons, not more complex components, such as a search text field, for instance
		return comp instanceof AbstractButton || comp instanceof JSeparator;
	}
	
	public static AbstractButton createToolBarButton(CyAction action, int iconWidth, int iconHeight) {
		action.updateEnableState();
		final AbstractButton button;
		
		if (action.useToggleButton()) {
			var selectedStroke = new BasicStroke(1.0f);
			
			button = new JToggleButton(action) {
				@Override
				public JToolTip createToolTip() {
					return CyToolBar.createToolTip(this, action);
				}
				@Override
				public void paint(Graphics g) {
					if (isSelected()) {
						var g2 = (Graphics2D) g.create();
						// Fill
						g2.setColor(UIManager.getColor("CyToggleButton[Selected].background"));
						g2.fillRect(
								BUTTON_BORDER_SIZE,
								BUTTON_BORDER_SIZE,
								getWidth() - 2 * BUTTON_BORDER_SIZE,
								getHeight() - 2 * BUTTON_BORDER_SIZE
						);
						// Border
						g2.setStroke(selectedStroke);
						g2.setColor(UIManager.getColor("CyToggleButton[Selected].borderColor"));
						g2.drawRect(
							BUTTON_BORDER_SIZE,
							BUTTON_BORDER_SIZE,
							getWidth() - 2 * BUTTON_BORDER_SIZE,
							getHeight() - 2 * BUTTON_BORDER_SIZE
						);
						g2.dispose();
					}
					super.paint(g);
				}
			};
		} else {
			button = new JButton(action) {
				@Override
				public JToolTip createToolTip() {
					return CyToolBar.createToolTip(this, action);
				}
			};
		}
		
		button.setText(action.getName());
		button.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER_SIZE, BUTTON_BORDER_SIZE, BUTTON_BORDER_SIZE,
				BUTTON_BORDER_SIZE));
		button.setRolloverEnabled(isWinLAF());
		button.setFocusable(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setHideActionText(true);
		
		var dim = new Dimension(iconWidth + 2 * BUTTON_BORDER_SIZE, iconHeight + 2 * BUTTON_BORDER_SIZE);
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.setMaximumSize(dim);
		button.setSize(dim);

		var iconObj = action.getValue(Action.LARGE_ICON_KEY);
		
		if (iconObj instanceof Icon) {
			var icon = (Icon) iconObj;
			
			if (icon.getIconWidth() > iconWidth || icon.getIconHeight() > iconHeight) {
				icon = IconManager.resizeIcon(icon, Math.min(iconWidth, iconHeight));
				button.setIcon(icon);
			}
			
			if (icon instanceof ImageIcon) {
				var img = ((ImageIcon) icon).getImage();
				var filter = new GrayFilter(true, 71);
				var prod = new FilteredImageSource(img.getSource(), filter);
				var grayImage = Toolkit.getDefaultToolkit().createImage(prod);
		        button.setDisabledIcon(new ImageIcon(grayImage));
			}
		}
		
		return button;
	}
	
	private static JToolTip createToolTip(JComponent comp, CyAction action) {
		return new CyToolTip(comp,
				(String) action.getValue(Action.SHORT_DESCRIPTION),
				(String) action.getValue(Action.LONG_DESCRIPTION),
				action.getToolTipImage() == null ? null : new ImageIcon(action.getToolTipImage()));
	}
	
	protected static class ToolBarItem {
		
		private final ToolBarComponent toolBarComponent;
		private final Component component;
		private final boolean separatorBefore;
		private final boolean separatorAfter;
		private float gravity = -1.0f;
		
		public ToolBarItem(
				Component comp,
				float gravity,
				boolean separatorBefore,
				boolean separatorAfter
		) {
			this.toolBarComponent = null;
			this.component = comp;
			this.gravity = gravity;
			this.separatorBefore = separatorBefore;
			this.separatorAfter = separatorAfter;
		}
		
		public ToolBarItem(ToolBarComponent toolBarComponent, boolean separatorBefore, boolean separatorAfter) {
			this.toolBarComponent = toolBarComponent;
			this.component = null;
			this.separatorBefore = separatorBefore;
			this.separatorAfter = separatorAfter;
		}
		
		public ToolBarComponent getToolBarComponent() {
			return toolBarComponent;
		}
		
		public Component getComponent() {
			return toolBarComponent != null ? toolBarComponent.getComponent() : component;
		}
		
		public float getGravity() {
			return toolBarComponent != null ? toolBarComponent.getToolBarGravity() : gravity;
		}
		
		public boolean isSeparatorBefore() {
			return separatorBefore;
		}
		
		public boolean isSeparatorAfter() {
			return separatorAfter;
		}
	}
	
	protected static final class SeparatorToolBarItem extends ToolBarItem {
		
		public SeparatorToolBarItem(float gravity) {
			super(null, gravity, false, false);
		}
	}
	
	protected static final class SpacerToolBarItem extends ToolBarItem {
		
		public SpacerToolBarItem(float gravity) {
			super(null, gravity, false, false);
		}
	}
}
