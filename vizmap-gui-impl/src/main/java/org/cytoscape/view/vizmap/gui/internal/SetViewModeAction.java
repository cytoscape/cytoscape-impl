package org.cytoscape.view.vizmap.gui.internal;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;

public class SetViewModeAction extends AbstractCyAction {

	private final static String BASIC = "Show All Visual Properties";
	private final static String ALL = "Hide Advanced Visual Proeprties";
	
	private VizMapperMainPanel mainPanel;
	final SelectedVisualStyleManager manager;

	public SetViewModeAction(final CyApplicationManager applicationManager, VizMapperMainPanel mainPanel, final SelectedVisualStyleManager manager) {
		super(BASIC, applicationManager);
		this.mainPanel = mainPanel;
		this.manager = manager;
		
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
		// TODO: DO NOT CALL apply
		mainPanel.switchVS(manager.getCurrentVisualStyle(), true);
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
