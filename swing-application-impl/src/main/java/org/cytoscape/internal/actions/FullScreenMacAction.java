package org.cytoscape.internal.actions;

import javax.swing.JFrame;

import org.cytoscape.application.swing.CySwingApplication;

import com.apple.eawt.Application;
import com.apple.eawt.FullScreenAdapter;
import com.apple.eawt.FullScreenListener;
import com.apple.eawt.FullScreenUtilities;
import com.apple.eawt.AppEvent.FullScreenEvent;

public class FullScreenMacAction extends FullScreenAction {

	private static final long serialVersionUID = 1886462527637755625L;
	private static final String MAC_MENU_NAME = "Full Screen Mode";

	private boolean macScreenState = false;

	public FullScreenMacAction(CySwingApplication desktop) {
		super(desktop, MAC_MENU_NAME);

		final FullScreenListener listener = new FullScreenAdapter() {

			@Override
			public void windowExitedFullScreen(FullScreenEvent arg0) {
				macScreenState = false;
			}

			@Override
			public void windowEnteredFullScreen(FullScreenEvent arg0) {
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
