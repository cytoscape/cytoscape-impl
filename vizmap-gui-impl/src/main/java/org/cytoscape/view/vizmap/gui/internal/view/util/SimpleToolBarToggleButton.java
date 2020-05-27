package org.cytoscape.view.vizmap.gui.internal.view.util;

import javax.swing.Icon;
import javax.swing.JToggleButton;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class SimpleToolBarToggleButton extends JToggleButton {

	public SimpleToolBarToggleButton(String text) {
		this(text, false);
	}
	
	public SimpleToolBarToggleButton(Icon icon) {
		this(icon, false);
	}

	public SimpleToolBarToggleButton(String text, Boolean selected) {
		super(text, selected);
		addActionListener(evt -> update());
	}
	
	public SimpleToolBarToggleButton(Icon icon, Boolean selected) {
		super(icon, selected);
		addActionListener(evt -> update());
	}

	@Override
	public void setSelected(boolean b) {
		if (b != isSelected()) {
			super.setSelected(b);
			update();
		}
	}
	
	protected void update() {
		ViewUtil.updateToolBarStyle(this);
	}
}
