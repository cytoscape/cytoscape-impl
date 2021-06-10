package org.cytoscape.util.swing.internal;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.colorchooser.AbstractColorChooserPanel;

/*
 * #%L
 * Cytoscape Swing Utility Impl (swing-util-impl)
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
 * Created by andreas on 7/19/15.
 */
@SuppressWarnings("serial")
public abstract class ColorBlindAwareColorChooserPanel extends AbstractColorChooserPanel {

	boolean showColorBlindSafe;
	ColorBlindAwareColorChooserPanel chooserPanel;
	protected String selectedPalette;

	List<Container> currentButtons = new ArrayList<>();

	public boolean isShowColorBlindSafe() {
		return showColorBlindSafe;
	}

	public void setShowColorBlindSafe(boolean showColorBlindSafe) {
		if (this.showColorBlindSafe == showColorBlindSafe)
			return;
		
		this.showColorBlindSafe = showColorBlindSafe;
		chooserPanel = null;
		this.repaint();
	}

	abstract public void setSelectedPalette(String palette);

	@Override
	public void updateChooser() {
		if (currentButtons == null)
			currentButtons = new ArrayList<>();

		if (chooserPanel == null) {
			for (var c : currentButtons)
				remove(c);
			
			currentButtons.clear();

			buildChooser();
			chooserPanel = this;
		}
	}
}
