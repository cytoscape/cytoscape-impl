package org.cytoscape.internal.view;

import static org.cytoscape.internal.view.CytoPanelNameInternal.EAST;
import static org.cytoscape.internal.view.CytoPanelNameInternal.SOUTH_WEST;
import static org.cytoscape.internal.view.CytoPanelUtil.EAST_MAX_WIDTH;
import static org.cytoscape.internal.view.CytoPanelUtil.EAST_MIN_WIDTH;
import static org.cytoscape.internal.view.CytoPanelUtil.WEST_MIN_HEIGHT;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

import org.cytoscape.application.swing.CytoPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
 * The BiModalJSplitPane class extends JSplitPane to provide two modes:
 * <UL>
 * <LI>MODE_SHOW_SPLIT:  The split in the split pane appears as it normally
 *                       would, and the user can resize the split pane as
 *                       needed.
 * <LI>MODE_HIDE_SPLIT:  The split in the split pane is hidden, and the
 *                       user cannot resize the split pane.
 * </UL>
 *
 * BIModalJSplitPane also implements the CytoPanelContainer interface.
 *
 * @author Ethan Cerami, Ben Gross
 */
@SuppressWarnings("serial")
public class BiModalJSplitPane extends JSplitPane {
	
	private int defaultDividerSize;
	private int dividerLocation;
	private boolean dividerManuallyMoved;
	private boolean updatingDivider;
	
	private final CytoPanelNameInternal compassDirection;
	
	private static final Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");

	/**
	 * @param orientation    JSplitPane Orientation.
	 *                       JSplitPane.HORIZONTAL_SPLIT or
	 *                       JSplitPane.VERTICAL_SPLIT.
	 * @param leftComponent  Left/Top Component.
	 * @param rightComponent Right/Bottom Component.
	 */
	public BiModalJSplitPane(
			CytoPanelNameInternal compassDirection,
			int orientation,
			Component
			leftComponent,
			Component rightComponent
	) {
		super(orientation, leftComponent, rightComponent);

		this.compassDirection = compassDirection;
		
		// Remove the border
		setBorder(BorderFactory.createEmptyBorder());
		setOneTouchExpandable(false);

		// Store the default divider size
		defaultDividerSize = getDividerSize();

		update();
		
		addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
			if (!updatingDivider)
				dividerManuallyMoved = true;
		});
	}

	public void addCytoPanel(CytoPanelImpl cytoPanel) {
		switch (cytoPanel.getCytoPanelNameInternal()) {
			case WEST:
				setLeftComponent(cytoPanel.getThisComponent());
				break;
			case EAST:
				setRightComponent(cytoPanel.getThisComponent());
				break;
			case SOUTH:
			case SOUTH_WEST:
			case BOTTOM:
				setBottomComponent(cytoPanel.getThisComponent());
				break;
		}

		updatingDivider = true;
		
		try {	
			// Hack to set divider size back to what it should be
			setDividerSize(defaultDividerSize);
	
			// Hack to set divider location back to what it was
			if (dividerLocation != -1)
				setDividerLocation(dividerLocation);
		} finally {
			updatingDivider = false;
		}
	}
	
	public void removeCytoPanel(CytoPanel cytoPanel) {
		remove(cytoPanel.getThisComponent());

		updatingDivider = true;
		
		try {
			// Hack to set divider size back to what it should be
			setDividerSize(0);
	
			// Hack to set divider location back to what it was
			if (dividerLocation != -1)
				setDividerLocation(dividerLocation);
		} finally {
			updatingDivider = false;
		}
	}

	@Override
	public void addNotify() {
		super.addNotify();
		
		getParent().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (getParent() != null)
					dividerLocation = -1;
			}
		});
	}
	
	public void update() {
		Component lc = getLeftComponent();
		Component rc = getRightComponent();
		
		if (lc != null && lc.isVisible() && rc != null && rc.isVisible()) {
			showSplit();
		} else {
			hideSplit();
		}
	}
	
	/**
	 * Move the divider according to the panels' preferred and minimum sizes.
	 */
	public void updateDividerLocation() {
		Component c1 = getTopComponent();
		Component c2 = getBottomComponent();
		
		if (c1 == null || c2 == null || !c1.isVisible() || !c2.isVisible())
			return;
		// Do not move the divider when it's been hidden
		if (getDividerLocation() == -1)
			return;
		// Do not move the divider when user has repositioned it manually
		if (dividerManuallyMoved)
			return;
		// If the panel is 0x0, it's probably not created, yet
		if (c1.getSize() == null || (c1.getSize().width == 0 && c1.getSize().height == 0))
			return;
		
		updatingDivider = true;
		
		try {
			final int w = getSize().width; // Total widht
			final int h = getSize().height; // Total height
			final int divSize = getDividerSize();
			
			if (compassDirection == SOUTH_WEST) {
				if (c1 != null && c2 != null && c2.isVisible()) {
					// TOP panel's preferred height
					int h1 = Math.max(c1.getPreferredSize().height, WEST_MIN_HEIGHT);
					
					// BOTTOM panel's height--we try to allow the bottom panel to reach its preferred height...
					int h2 = c2.getPreferredSize().height + divSize;
					// ...but we also want to do the same for the top panel, which has the priority here
					h2 = Math.min(h2, h - h1);
					
					// However we should do everything to respect the bottom panel's minimum height...
					if (c2.getMinimumSize() != null)
						h2 = Math.max(h2, c2.getMinimumSize().height + divSize);
					
					// ...unless it's bigger than half of the total height (the bottom panel cannot abuse it!)
					h2 = Math.min(h2, (h - divSize) / 2);
					
					// This is the final height of the top component
					h1 = h - h2;
					setDividerLocation(h1);
				}
			} else if (compassDirection == EAST) {
				int w1 = c1.getPreferredSize().width;
				w1 = Math.min(w1, EAST_MAX_WIDTH);
				w1 = Math.max(w1, EAST_MIN_WIDTH);
	
				setDividerLocation(w - getInsets().right - getInsets().left - divSize - w1);
			} 
		} catch (Exception e) {
			logger.error("Unable to update Split Pane's divider location", e);
		} finally {
			updatingDivider = false;
		}
		// TODO: What's the right thing to do with SOUTH?
	}
	
	private void showSplit() {
		updatingDivider = true;
		
		try {
			setDividerSize(defaultDividerSize);
	
			if (dividerLocation != -1)
				setDividerLocation(dividerLocation);
	
			resetToPreferredSizes();
			validateParent();
		} finally {
			updatingDivider = false;
		}
	}

	private void hideSplit() {
		updatingDivider = true;
		
		try {
			setDividerSize(0);
			dividerLocation = getDividerLocation();
			setDividerLocation(-1);
			resetToPreferredSizes();
			validateParent();
		} finally {
			updatingDivider = false;
		}
	}

	private void validateParent() {
		Container container = getParent();

		if (container != null)
			container.validate();
	}
}
