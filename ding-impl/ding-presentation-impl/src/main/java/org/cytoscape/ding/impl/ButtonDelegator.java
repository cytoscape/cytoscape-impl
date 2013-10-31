package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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


import java.awt.event.MouseEvent;

/**
 * The purpose of this class is to capture the button processing logic in a single
 * place so that it can be used consistently for different mouse events. This
 * class also captures the logic for converting one-button Mac MouseEvents into 
 * expected method calls.  To handle a particular type of button click, just 
 * extend this class and implement the method that best describes the click you're 
 * interested in.
 */
class ButtonDelegator {

	private static final String MAC_OS_ID = "mac";
	private final boolean isMacPlatform;

	ButtonDelegator() {
		String os = System.getProperty("os.name");
		isMacPlatform = os.regionMatches(true, 0, MAC_OS_ID, 0, MAC_OS_ID.length());
	}

	void delegateMouseEvent(MouseEvent e) {
		// single click or release (i.e. no clicks)
		if ( e.getClickCount() <= 1 ) {
			if ( isLeftClick(e) ) {
				singleLeftClick(e);
			} else if ( isMiddleClick(e) ) {
				singleMiddleClick(e);
			} else if ( isRightClick(e) ) {
				singleRightClick(e);
			}
		// double click
		} else if ( e.getClickCount() == 2 ) {
			if ( isLeftClick(e) ) {
				doubleLeftClick(e);
			}
		}
	}

	void singleLeftClick(MouseEvent e) {}; 
	void singleMiddleClick(MouseEvent e) {};
	void singleRightClick(MouseEvent e) {}; 
	void doubleLeftClick(MouseEvent e) {}; 

	private boolean isLeftClick(MouseEvent e) {
		boolean b1 = (e.getButton() == MouseEvent.BUTTON1);
		if ( isMacPlatform ) {
			return (!e.isControlDown() && !e.isMetaDown() && b1);
		}
		return b1;
	}

	private boolean isRightClick(MouseEvent e) {
		boolean b3 = (e.getButton() == MouseEvent.BUTTON3); 
		if ( !b3 && isMacPlatform ) {
			// control - right click
			return (e.isControlDown() && !e.isMetaDown() && (e.getButton() == MouseEvent.BUTTON1));
		}
		return b3;
	}

	private boolean isMiddleClick(MouseEvent e) {
		boolean b2 = (e.getButton() == MouseEvent.BUTTON2); 
		if ( !b2 && isMacPlatform ) {
			// meta - left click
			return (!e.isControlDown() && e.isMetaDown() && (e.getButton() == MouseEvent.BUTTON1));
		}
		return b2;
	}

    private String getMouseEventString(MouseEvent e) {
        return "button (x " + e.getClickCount() + "): " + e.getButton() + " ["
        + (e.isControlDown() ? "ctrl " : "")
        + (e.isShiftDown() ? "shft " : "")
        + (e.isAltDown() ? "alt " : "")
        + (e.isMetaDown() ? "meta " : "")
        + (e.isAltGraphDown() ? "grph " : "")
        + "{"
        + MouseEvent.getMouseModifiersText(e.getModifiers())
        + "}"
        + (Integer.toBinaryString(e.getModifiers()) )
        + " "
        + (Integer.toBinaryString(e.getModifiersEx()) )
        + "]";
    }
}
