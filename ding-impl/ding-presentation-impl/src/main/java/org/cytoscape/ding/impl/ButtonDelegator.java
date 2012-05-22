/*
 Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.ding.impl;


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

	final void delegateMouseEvent(MouseEvent e) {
		// single click or release (i.e. no clicks)
		if ( e.getClickCount() <= 1 ) {
			if ( isLeftClick(e) ) {
				if ( e.isControlDown() ) {
					singleLeftControlClick(e);
				} else {
					singleLeftClick(e);
				}
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
	void singleLeftControlClick(MouseEvent e) {};
	void singleMiddleClick(MouseEvent e) {};
	void singleRightClick(MouseEvent e) {}; 
	void doubleLeftClick(MouseEvent e) {}; 

	private boolean isLeftClick(MouseEvent e) {
		boolean b1 = (e.getButton() == MouseEvent.BUTTON1);
		if ( isMacPlatform ) {
			return (!e.isControlDown() && !e.isAltDown() && b1);
		}
		return b1;
	}

	private boolean isRightClick(MouseEvent e) {
		boolean b3 = (e.getButton() == MouseEvent.BUTTON3); 
		if ( !b3 && isMacPlatform ) {
			// meta - left click
			return (e.isControlDown() && !e.isAltDown() && (e.getButton() == MouseEvent.BUTTON1));
		}
		return b3;
	}

	private boolean isMiddleClick(MouseEvent e) {
		boolean b2 = (e.getButton() == MouseEvent.BUTTON2); 
		if ( !b2 && isMacPlatform ) {
			// alt - left click
			return (!e.isControlDown() && e.isAltDown() && (e.getButton() == MouseEvent.BUTTON1));
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
