/*
  File: BiModalJSplitPane.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.internal.view;


import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.internal.view.CytoPanelContainer;


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
public class BiModalJSplitPane extends JSplitPane implements CytoPanelContainer {
	private final static long serialVersionUID = 1202339868270146L;
	/**
	 * Reference application frame.
	 */
	private JFrame frame;

	/**
	 * Available modes of the BiModalJSplitPane.
	 */
	public static final int MODE_SHOW_SPLIT = 1;

	/**
	 *
	 */
	public static final int MODE_HIDE_SPLIT = 2;

	/**
	 * Property listener modes.
	 */
	public static final String MODE_PROPERTY = "MODE_PROPERTY";

	/**
	 * The current mode.
	 */
	private int currentMode;

	/**
	 * The default divider size.
	 */
	private int defaultDividerSize;

	/**
	 * The saved divider location.
	 */
	private int dividerLocation;

	/**
	 * Constructor.
	 *
	 * @param orientation    JSplitPane Orientation.
	 *                       JSplitPane.HORIZONTAL_SPLIT or
	 *                       JSplitPane.VERTICAL_SPLIT.
	 * @param initialMode    Initial Mode.
	 *                       MODE_SHOW_SPLIT or
	 *                       MODE_HIDE_SPLIT.
	 * @param leftComponent  Left/Top Component.
	 * @param rightComponent Right/Bottom Component.
	 */
	public BiModalJSplitPane(JFrame f, int orientation, int initialMode, Component leftComponent,
	                         Component rightComponent) {
		super(orientation, leftComponent, rightComponent);

		// init some member vars
		currentMode = initialMode;
		frame = f;

		// add component listener to get resize events
		addComponentListener();

		//  remove the border
		setBorder(null);
		setOneTouchExpandable(false);

		//  store the default divider size
		defaultDividerSize = this.getDividerSize();

		//  hide split
		if (initialMode == MODE_HIDE_SPLIT) {
			this.setDividerSize(0);
		}
	}

	/**
	 * Inserts CytoPanel at desired compass direction.
	 *
	 * @param cytoPanel        CytoPanel reference.
	 * @param compassDirection CytoPanelName enum value
	 */
	public void insertCytoPanel(final CytoPanel cytoPanel, final CytoPanelName compassDirection) {
		boolean success = false;

		switch (compassDirection) {
		case SOUTH:
			this.setBottomComponent(cytoPanel.getThisComponent());
			success = true;
			break;
		case EAST:
			this.setRightComponent(cytoPanel.getThisComponent());
			success = true;
			break;
		case WEST:
			this.setLeftComponent(cytoPanel.getThisComponent());
			success = true;
			break;
		case SOUTH_WEST:
			this.setBottomComponent(cytoPanel.getThisComponent());
			success = true;
			break;
		}

		// houston we have a problem
		if (!success) {
			// made it here, houston, we have a problem
			throw new IllegalArgumentException("Illegal Argument:  " + compassDirection
			                                   + ".  Must be one of:  CytoPanelName.{SOUTH,EAST,WEST,SOUTH_WEST}.");
		}

		// hack to set divider size back to what it should be
		setDividerSize(defaultDividerSize);

		// hack to set divider location back to what it was
		if (dividerLocation != -1) {
			setDividerLocation(dividerLocation);
		}
	}

	/**
	 * Gets the location of the applications mainframe.
	 *
	 * @return Point object.
	 */
	public Point getLocationOnScreen() {
		return frame.getLocationOnScreen();
	}

	/**
	 * Gets the bounds of the applications mainframe.
	 *
	 * @return Rectangle Object.
	 */
	public Rectangle getBounds() {
		return frame.getBounds();
	}

	/**
	 * Sets the BiModalJSplitframe mode.
	 *
	 * @param newMode MODE_SHOW_SPLIT or MODE_HIDE_SPLIT.
	 */
	public void setMode(CytoPanelState cytoPanelState, int newMode) {
		//  check args
		if ((newMode != MODE_SHOW_SPLIT) && (newMode != MODE_HIDE_SPLIT)) {
			throw new IllegalArgumentException("Illegal Argument:  " + newMode
			                                   + ".  Must be one of:  MODE_SHOW_SPLIT or "
			                                   + " MODE_HIDE_SPLIT.");
		}

		int oldMode = currentMode;

		//  only process if the mode has changed
		if (newMode != currentMode) {
			if (newMode == MODE_HIDE_SPLIT) {
				hideSplit();
			} else if (newMode == MODE_SHOW_SPLIT) {
				showSplit();
			}

			this.currentMode = newMode;

			//  fire a property change
			this.firePropertyChange(MODE_PROPERTY, oldMode, newMode);
		}

		// hack to make sure divider is zero when we go from dock to float
		// and divider location is set properly when we go back to dock
		if (cytoPanelState == cytoPanelState.FLOAT) {
			setDividerSize(0);
			dividerLocation = getDividerLocation();
		}
	}

	/**
	 * Gets the current mode.
	 *
	 * @return MODE_SHOW_SPLIT or MODE_HIDE_SPLIT.
	 */
	public int getMode() {
		return currentMode;
	}

	/**
	 * Shows the split.
	 */
	private void showSplit() {
		setDividerSize(defaultDividerSize);

		if (dividerLocation != -1) {
			setDividerLocation(dividerLocation);
		}

		resetToPreferredSizes();
		validateParent();
	}

	/**
	 * Hides the split.
	 */
	private void hideSplit() {
		setDividerSize(0);
		dividerLocation = getDividerLocation();
		resetToPreferredSizes();
		validateParent();
	}

	/**
	 * Validates the parent container.
	 */
	private void validateParent() {
		Container container = this.getParent();

		if (container != null) {
			container.validate();
		}
	}

	/**
	 * Add a component listener to the app frame to get windows resize events.
	 */
	private void addComponentListener() {
		frame.addComponentListener(new ComponentAdapter() {
				/**
				 * Frame is resized.
				 *
				 * @param e Component Event.
				 */
				public void componentResized(ComponentEvent e) {
					dividerLocation = -1;
				}
			});
	}
}
