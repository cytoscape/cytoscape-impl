package org.cytoscape.view.vizmap.gui.internal.view;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

/**
 * Button with drop down menu.
 */
public class DropDownMenuButton extends JButton {
	
	private final static long serialVersionUID = 1202339868695691L;
	
	private final static int TEXT_ICON_GAP = 6;
	
	private final Icon buttonIcon = new MenuArrowIcon();
	private JPopupMenu popupMenu;
	private final boolean showMenuArrowIcon;
	private final ActionListener defaultActionListener;
	protected boolean isShowingPopup;
	protected boolean showPopup;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public DropDownMenuButton() {
		this(null, true);
	}
	
	public DropDownMenuButton(final boolean showMenuArrowIcon) {
		this(null, showMenuArrowIcon);
	}
	
	public DropDownMenuButton(final JPopupMenu popupMenu) {
		this(popupMenu, true);
	}
	
	public DropDownMenuButton(final JPopupMenu popupMenu, final boolean showMenuArrowIcon) {
		super("");
		this.showMenuArrowIcon = showMenuArrowIcon;
		
		defaultActionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JPopupMenu pm = getPopupMenu();
				
				if (pm != null) {
//					if (showPopup) {
						pm.show(DropDownMenuButton.this, 0, DropDownMenuButton.this.getHeight());
						pm.requestFocusInWindow();
//					} else {
//						showPopup = true;
//					}
				}
			}
		};
		// FIXME
		// Workaround to toggle the popup menu visibility
		// (if it's visible and the button is clicked again, the menu should disappear)
		// See http://stackoverflow.com/questions/2421914/showing-hiding-a-jpopupmenu-from-a-jbutton-focuslistener-not-working
//		addMouseListener(new MouseAdapter() {
//	        @Override
//	        public void mousePressed(final MouseEvent e) {
//	            // This is called before the menu loses focus.
//	            // If the menu is already visible, just set showPopup to false, so that the actionPerformed
//	            // does not show the menu again.
//	            if (isShowingPopup)
//	                showPopup = false;
//	        }
//
//	        @Override
//	        public void mouseReleased(final MouseEvent e) {
//	            showPopup = true;
//	        }
//	    });
		
		setPopupMenu(popupMenu);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public void setPopupMenu(final JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
		
		if (popupMenu != null) {// FIXME
//			popupMenu.addFocusListener(new FocusListener() {
//		        @Override
//		        public void focusLost(final FocusEvent e) {
//		            System.out.println("LOST FOCUS");
//		            isShowingPopup = false;
//		        }
//		        @Override
//		        public void focusGained(final FocusEvent e) {
//		            System.out.println("GAINED FOCUS");
//		            isShowingPopup = true;
//		        }
//		    });
			
			addActionListener(defaultActionListener);
		} else {
			removeActionListener(defaultActionListener);
		}
		
		updateEnabled();
	}
	
	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension d = super.getPreferredSize();
		
		if (showMenuArrowIcon) // So the arrow icon doesn't overlap the text
			d.width += buttonIcon.getIconWidth() + TEXT_ICON_GAP;
		
		return d;
	}
	
	@Override
	public void setEnabled(final boolean b) {
		super.setEnabled(b);
		setForeground(UIManager.getColor(b ? "Button.foreground" : "Label.disabledForeground"/*to change the icon color*/));
	}
	
	@Override
	public void addActionListener(final ActionListener l) {
		super.addActionListener(l);
		updateEnabled();
	}

	@Override
	public void removeActionListener(final ActionListener l) {
		super.removeActionListener(l);
		updateEnabled();
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);

		if (showMenuArrowIcon) {
			final Dimension size = getSize();
			final Insets ins = getInsets();
			int x = size.width - buttonIcon.getIconWidth() - ins.right;
			int y = ins.top + ((size.height - ins.top - ins.bottom - buttonIcon.getIconHeight()) / 2);
			buttonIcon.paintIcon(this, g, x, y);
		}
	}

	// ==[ PRIVATE METHODS ]============================================================================================
	
	private synchronized void updateEnabled() {
		final ActionListener[] actionListeners = getActionListeners();
		setEnabled((actionListeners != null && actionListeners.length > 0) || 
				   (popupMenu != null && popupMenu.getSubElements().length > 0));
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	class MenuArrowIcon implements Icon {
		
		@Override
		public void paintIcon(final Component c, final Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(c.getForeground());
			g2.translate(x, y);
			g2.drawLine(2, 3, 6, 3);
			g2.drawLine(3, 4, 5, 4);
			g2.drawLine(4, 5, 4, 5);
			g2.translate(-x, -y);
		}

		@Override
		public int getIconWidth() {
			return 9;
		}

		@Override
		public int getIconHeight() {
			return 9;
		}
	}
}
