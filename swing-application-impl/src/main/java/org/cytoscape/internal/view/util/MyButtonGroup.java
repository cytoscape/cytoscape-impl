package org.cytoscape.internal.view.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

/*
 * @ 2003 Daniel C. Tofan
 * daniel@danieltofan.org
 * https://www.javaworld.com/article/2077509/java-tip-142--pushing-jbuttongroup.html
 */

/**
 * Extends <code>javax.swing.ButtonGroup</code> to provide methods that allow
 * working with button references instead of button models.
 * Also, you can't add a button to a group more than once, and provides some useful methods.
 * 
 * @author Daniel Tofan
 * @version 1.0 April 2003
 * @see ButtonGroup
 */
@SuppressWarnings("serial")
public class MyButtonGroup extends ButtonGroup {
	
	/**
	 * Stores a reference to the currently selected button in the group.
	 */
	private AbstractButton selectedButton;

	/**
	 * Creates an empty <code>MyButtonGroup</code>
	 */
	public MyButtonGroup() {
		super();
	}

	@Override
	public void add(AbstractButton button) {
		if (button == null || buttons.contains(button))
			return;
		
		super.add(button);
		
		if (getSelection() == button.getModel())
			selectedButton = button;
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
		if (button != null) {
			if (selectedButton == button)
				selectedButton = null;
			
			super.remove(button);
		}
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
		if (button != null && buttons.contains(button)) {
			setSelected(button.getModel(), selected);
			
			if (getSelection() == button.getModel())
				selectedButton = button;
		}
	}

	@Override
	public void setSelected(ButtonModel model, boolean selected) {
		AbstractButton button = getButton(model);
		
		if (buttons.contains(button))
			super.setSelected(model, selected);
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
	public AbstractButton getSelected() {
		return selectedButton;
	}

	/**
	 * Returns whether the button is selected.
	 * @param button an <code>AbstractButton</code> reference
	 * @return <code>true</code> if the button is selected, <code>false</code>otherwise
	 */
	public boolean isSelected(AbstractButton button) {
		return button == selectedButton;
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
