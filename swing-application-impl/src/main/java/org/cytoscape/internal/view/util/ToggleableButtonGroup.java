package org.cytoscape.internal.view.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

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
 * Modified ButtonGroup that allows a selected toggle button to be deselected when it's clicked again.<br><br>
 * It also provides methods that allow working with button references instead of button models, 
 * and you can't add a button to a group more than once, plus other useful methods
 * (from <a href="https://www.javaworld.com/article/2077509/java-tip-142--pushing-jbuttongroup.html">this class</a>,
 * by Daniel C. Tofan).<br><br>
 */
@SuppressWarnings("serial")
public class ToggleableButtonGroup extends ButtonGroup {
	
	/** Only used when toggleable is true. */
	private ButtonModel lastModel;
	private boolean toggleable;

	/**
	 * Creates an empty <code>ToggleableButtonGroup</code>
	 */
	public ToggleableButtonGroup() {
		this(false);
	}
	
	public ToggleableButtonGroup(boolean toggleable) {
		super();
		this.toggleable = toggleable;
	}

	@Override
	public void add(AbstractButton button) {
		if (button == null || buttons.contains(button))
			return;
		
		super.add(button);
		
		if (getSelection() == button.getModel() && toggleable)
			lastModel = button.getModel();
	}

	/**
	 * Adds an array of buttons to the group.
	 * @param buttons a collection of <code>AbstractButton</code>s
	 */
	public void add(Collection<? extends AbstractButton> buttons) {
		if (buttons == null)
			return;
		
		for (AbstractButton b : buttons)
			add(b);
	}

	@Override
	public void remove(AbstractButton button) {
		if (button == null || !buttons.contains(button))
			return;
		
		if (toggleable && lastModel == button.getModel())
			lastModel = null;

		super.remove(button);
	}

	/**
	 * Removes all the buttons in the array from the group.
	 * @param buttons a collection of <code>AbstractButton</code>s
	 */
	public void remove(Collection<? extends AbstractButton> buttons) {
		if (buttons == null)
			return;
		
		for (AbstractButton b : buttons)
			remove(b);
	}
	
	/**
	 * Sets the selected button in the group Only one button in the group can be selected.
	 * @param button an <code>AbstractButton</code> reference
	 * @param selected an <code>boolean</code> representing the selection state of the button
	 */
	public void setSelected(AbstractButton button, boolean selected) {
		if (button != null && buttons.contains(button))
			setSelected(button.getModel(), selected);
	}

	@Override
	public void setSelected(ButtonModel model, boolean selected) {
		AbstractButton button = getButton(model);
		
		if (button == null)
			return;
		
		if (toggleable) {
			if (model == lastModel)
				clearSelection();
			else if (buttons.contains(button) && selected != isSelected(model))
				super.setSelected(model, selected);
			
			lastModel = getSelection();
		} else if (buttons.contains(button) && selected != isSelected(model)) {
			super.setSelected(model, selected);
		}
	}

	/**
	 * Returns the <code>AbstractButton</code> whose <code>ButtonModel</code> is given.
	 * If the model does not belong to a button in the group, returns null.
	 * @param model a <code>ButtonModel</code> that should belong to a button in the group
	 * @return an <code>AbstractButton</code> reference whose model is
	 *         <code>model</code> if the button belongs to the group, <code>null</code>otherwise
	 */
	public AbstractButton getButton(ButtonModel model) {
		Iterator<AbstractButton> it = buttons.iterator();
		
		while (it.hasNext()) {
			AbstractButton ab = (AbstractButton) it.next();
			
			if (ab.getModel() == model)
				return ab;
		}
		
		return null;
	}

	/**
	 * Returns the selected button in the group.
	 * @return a reference to the currently selected button in the group or
	 *         <code>null</code> if no button is selected
	 */
	public AbstractButton getSelectedButton() {
		return getSelection() != null ? getButton(getSelection()) : null;
	}

	/**
	 * Returns whether the button is selected.
	 * @param button an <code>AbstractButton</code> reference
	 * @return <code>true</code> if the button is selected, <code>false</code>otherwise
	 */
	public boolean isSelected(AbstractButton button) {
		return button.getModel() == getSelection();
	}

	/**
	 * Returns the buttons in the group as a <code>List</code>.
	 * @return a <code>List</code> containing the buttons in the group, in the order
	 *         they were added to the group
	 */
	public List<AbstractButton> getButtons() {
		return Collections.unmodifiableList(buttons);
	}

	/**
	 * Checks whether the group contains the given button
	 * @return <code>true</code> if the button is contained in the group,
	 *         <code>false</code> otherwise
	 */
	public boolean contains(AbstractButton button) {
		return buttons.contains(button);
	}
}
