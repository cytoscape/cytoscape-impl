package org.cytoscape.view.manual.internal.common;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.view.model.CyNetworkView;

/**
 *
 */
public class SliderStateTracker implements  SetCurrentNetworkViewListener {

	long preFocusedViewId;
	Map<Long,Integer> layoutStateMap;
	PolymorphicSlider slider;

	public SliderStateTracker(PolymorphicSlider s) {
		layoutStateMap = new HashMap<Long,Integer>();
		preFocusedViewId = 0l;
		slider = s;
	}

	/** 
	 * Keep track of the state for each view focused.
	 */
	public void handleEvent(SetCurrentNetworkViewEvent e) {
			CyNetworkView curr = e.getNetworkView(); 

			if ( curr == null )
				return;

			long curFocusedViewId = curr.getSUID();

			// detect duplicate NETWORK_VIEW_FOCUSED event 
			if (preFocusedViewId == curFocusedViewId) {
				return;
			}

			//save layout state for the previous network view
			layoutStateMap.put(preFocusedViewId, slider.getSliderValue());

			//Restore layout state for the current network view, if any
			Integer stateValue = layoutStateMap.get(curFocusedViewId);

			if (stateValue == null) {
				slider.updateSlider(0);
			} else {
				slider.updateSlider(stateValue.intValue());
			}

			preFocusedViewId = curFocusedViewId;
	}
}
