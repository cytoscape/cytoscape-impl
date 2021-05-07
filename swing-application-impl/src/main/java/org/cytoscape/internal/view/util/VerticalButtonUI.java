package org.cytoscape.internal.view.util;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

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
 * Modified from: http://tech.chitgoks.com/2009/11/15/rotate-jbutton-to-vertical/
 */
public class VerticalButtonUI extends BasicButtonUI {

	private static Rectangle iconRect = new Rectangle();
	private static Rectangle textRect = new Rectangle();
	private static Rectangle viewRect = new Rectangle();
	private static Insets viewInsets = new Insets(0, 0, 0, 0);

	protected boolean clockwise;

	public VerticalButtonUI() {
		this(false);
	}
	
	public VerticalButtonUI(boolean clockwise) {
		this.clockwise = clockwise;
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension dim = super.getPreferredSize(c);
		return new Dimension(dim.height, dim.width);
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		AbstractButton b = (AbstractButton) c;
		String text = b.getText();
		Icon icon = b.isEnabled() ? b.getIcon() : b.getDisabledIcon();

		if (icon == null && text == null)
			return;

		Graphics2D g2 = (Graphics2D) g.create();
		
		FontMetrics fm = g2.getFontMetrics();
		viewInsets = b.getInsets(viewInsets);

		viewRect.x = viewInsets.left;
		viewRect.y = viewInsets.top;

		// Use inverted Button height and width
		viewRect.height = b.getWidth() - (viewInsets.left + viewInsets.right);
		viewRect.width = b.getHeight() - (viewInsets.top + viewInsets.bottom);

		Rectangle2D strBounds = text != null ? fm.getStringBounds(text, g2) : new Rectangle2D.Double();

		iconRect.x = (int) Math.round((viewRect.width - icon.getIconWidth() - b.getIconTextGap() - strBounds.getWidth()) / 2.0f);
		iconRect.y = viewRect.y + (int) Math.round((viewRect.height - icon.getIconHeight()) / 2.0f);
		iconRect.width = icon.getIconWidth();
		iconRect.height = icon.getIconHeight();
		
		if (text != null) {
			textRect.x = iconRect.x + iconRect.width + b.getIconTextGap();
			textRect.y = viewRect.y + (int) Math.round((viewRect.height - strBounds.getHeight()) / 2.0f);
			textRect.width = (int) strBounds.getWidth();
			textRect.height = (int) strBounds.getHeight();
		}
		
		if (clockwise) {
			g2.rotate(Math.PI / 2);
			g2.translate(0, -b.getWidth());
		} else {
			g2.rotate(-Math.PI / 2);
			g2.translate(-b.getHeight(), 0);
		}
		
		if (icon != null)
			icon.paintIcon(b, g2, iconRect.x, iconRect.y);
		
		if (text != null)
			paintText(g2, b, textRect, text);

		g2.dispose();
	}
}
