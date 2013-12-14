package org.cytoscape.task.internal.session;

import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadCancelledEvent;
import org.cytoscape.session.events.SessionLoadCancelledListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.session.events.SessionSaveCancelledEvent;
import org.cytoscape.session.events.SessionSaveCancelledListener;
import org.cytoscape.session.events.SessionSavedEvent;
import org.cytoscape.session.events.SessionSavedListener;
import org.cytoscape.work.AbstractTaskFactory;

public abstract class AbstractSessionTaskFactory extends AbstractTaskFactory
												 implements SessionAboutToBeLoadedListener, SessionLoadedListener,
												 			SessionAboutToBeSavedListener, SessionSavedListener,
												 			SessionLoadCancelledListener, SessionSaveCancelledListener {

	private volatile boolean loadingSession;
	private volatile boolean savingSession;
	
	@Override
	public boolean isReady() {
		return !loadingSession && !savingSession;
	}

	@Override
	public void handleEvent(SessionAboutToBeLoadedEvent e) {
		loadingSession = true;
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		loadingSession = false;
	}

	@Override
	public void handleEvent(SessionLoadCancelledEvent e) {
		loadingSession = false;
	}
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		savingSession = true;
	}
	
	@Override
	public void handleEvent(SessionSavedEvent e) {
		savingSession = false;
	}

	@Override
	public void handleEvent(SessionSaveCancelledEvent e) {
		savingSession = false;
	}
}
