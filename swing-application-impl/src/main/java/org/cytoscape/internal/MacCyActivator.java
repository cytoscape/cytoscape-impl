package org.cytoscape.internal;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.CyVersion;
import org.cytoscape.internal.dialogs.AboutDialogFactoryImpl;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.osgi.framework.BundleContext;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

public class MacCyActivator extends AbstractCyActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		final CyShutdown shutdown = getService(context, CyShutdown.class);
		final SubmenuTaskManager taskManager = getService(context, SubmenuTaskManager.class);
		final CyVersion version = getService(context, CyVersion.class);
		final AboutDialogFactoryImpl aboutDialogFactory = new AboutDialogFactoryImpl(version);
		
		Application application = Application.getApplication();
		application.setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(QuitEvent event, QuitResponse response) {
				shutdown.exit(0);
			}
		});
		application.setAboutHandler(new AboutHandler() {
			@Override
			public void handleAbout(AboutEvent event) {
				taskManager.execute(aboutDialogFactory);
			}
		});
	}
}
