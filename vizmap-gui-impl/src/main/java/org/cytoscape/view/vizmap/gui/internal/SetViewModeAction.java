package org.cytoscape.view.vizmap.gui.internal;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.swing.AbstractCyAction;

public class SetViewModeAction extends AbstractCyAction {
	
	// Local property changed event.
	public static final String VIEW_MODE_CHANGED = "VIEW_MODE_CHANGED";

	private final static String BASIC = "Show All Visual Properties";
	private final static String ALL = "Hide Advanced Visual Proeprties";
	
	public SetViewModeAction() {
		super(BASIC);

		PropertySheetUtil.setMode(false);

		setPreferredMenu("View");
		setMenuGravity(25.0f);
	}

	/**
	 * Toggles the Show/Hide state.
	 * 
	 * @param ev
	 *            Triggering event - not used.
	 */
	public void actionPerformed(ActionEvent ev) {
		firePropertyChange(VIEW_MODE_CHANGED, null, null);
	}

	@Override
	public void menuSelected(MenuEvent me) {
		if (PropertySheetUtil.isAdvancedMode()) {
			putValue(Action.NAME, ALL);
			PropertySheetUtil.setMode(false);
		} else {
			putValue(Action.NAME, BASIC);
			PropertySheetUtil.setMode(true);
		}
	}
}
