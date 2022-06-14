package org.cytoscape.view.vizmap.gui.internal.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

import org.cytoscape.application.CyUserLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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
	private int oldDividerLocation = -1;
	private boolean dividerManuallyMoved;
	private boolean updatingDivider;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	/**
	 * @param orientation    JSplitPane Orientation.
	 *                       JSplitPane.HORIZONTAL_SPLIT or
	 *                       JSplitPane.VERTICAL_SPLIT.
	 * @param leftComponent  Left/Top Component.
	 * @param rightComponent Right/Bottom Component.
	 */
	public BiModalJSplitPane(
			int orientation,
			Component leftComponent,
			Component rightComponent
	) {
		super(orientation, leftComponent, rightComponent);

		// Remove the border
		setBorder(BorderFactory.createEmptyBorder());
		setOneTouchExpandable(false);

		// Store the default divider size
		defaultDividerSize = getDividerSize();

		update();
		
		addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
			if (isBothComponentsVisible())
				oldDividerLocation = getDividerLocation();
			
			if (!updatingDivider)
				dividerManuallyMoved = true;
		});
	}


	@Override
	public void addNotify() {
		super.addNotify();
		
		getParent().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (getParent() != null)
					oldDividerLocation = -1;
			}
		});
	}
	
	public void update() {
		if (isBothComponentsVisible()) {
			if (dividerSize <= 0 || getDividerLocation() < 0)
				showSplit();
		} else {
			if (dividerSize > 0 || getDividerLocation() >= 0)
				hideSplit();
		}
		
		Component lc = getLeftComponent();
		Component rc = getRightComponent();
		boolean visible = (lc != null && lc.isVisible()) || (rc != null && rc.isVisible());
		
		if (visible != isVisible())
			setVisible(visible);
		
		// Recursively do the same with the parent split pane
		Container parent = getParent();
		
		if (parent instanceof BiModalJSplitPane)
			((BiModalJSplitPane) parent).update();
	}
	
	
	private void showSplit() {
		updatingDivider = true;
		
		try {
			setDividerSize(defaultDividerSize);
	
			if (oldDividerLocation != -1)
				setDividerLocation(oldDividerLocation);
	
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
			
			if (isBothComponentsVisible())
				oldDividerLocation = getDividerLocation();
			
			setDividerLocation(-1);
			resetToPreferredSizes();
			validateParent();
		} finally {
			updatingDivider = false;
		}
	}
	
	private boolean isBothComponentsVisible() {
		Component lc = getLeftComponent();
		Component rc = getRightComponent();
		
		return lc != null && lc.isVisible() && rc != null && rc.isVisible();
	}

	private void validateParent() {
		Container container = getParent();

		if (container != null)
			container.validate();
	}
}
