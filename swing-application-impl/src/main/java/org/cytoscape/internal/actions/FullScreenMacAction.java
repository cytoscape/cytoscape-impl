package org.cytoscape.internal.actions;

import javax.swing.JFrame;

import org.cytoscape.application.swing.CySwingApplication;

import com.apple.eawt.AppEvent.FullScreenEvent;
import com.apple.eawt.Application;
import com.apple.eawt.FullScreenAdapter;
import com.apple.eawt.FullScreenListener;
import com.apple.eawt.FullScreenUtilities;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class FullScreenMacAction extends FullScreenAction {

	private static final long serialVersionUID = 1886462527637755625L;

	private boolean macScreenState;

	public FullScreenMacAction(CySwingApplication desktop) {
		super(desktop);

		final FullScreenListener listener = new FullScreenAdapter() {

			@Override
			public void windowExitedFullScreen(FullScreenEvent e) {
				macScreenState = false;
			}

			@Override
			public void windowEnteredFullScreen(FullScreenEvent e) {
				macScreenState = true;
			}
		};
		FullScreenUtilities.addFullScreenListenerTo(desktop.getJFrame(), listener);
	}

	@Override
	protected void toggle() {
		super.toggle();
		final JFrame window = desktop.getJFrame();
		
		try {
			// Full screen mode
			if ((macScreenState != inFullScreenMode)) {
				if (macScreenState == false && inFullScreenMode == true)
					Application.getApplication().requestToggleFullScreen(window);
			} else {
				Application.getApplication().requestToggleFullScreen(window);
			}
		} catch (Exception e) {
			// Usually, it will be ignored if it's not OS 10.7+
		}
	}
}
