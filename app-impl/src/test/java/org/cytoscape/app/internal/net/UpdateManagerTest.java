package org.cytoscape.app.internal.net;

import static org.cytoscape.property.CyProperty.SavePolicy.DO_NOT_SAVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2026 The Cytoscape Consortium
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
 * Regression coverage for the startup update check that drives the App Updates
 * notification "bell".
 *
 * <p>The bug: {@link UpdateManager#handleEvent} only fetched app catalogs for the
 * custom download sites in {@link DownloadSitesManager}, never the default Cytoscape
 * App Store. {@link WebQuerier#checkForUpdates} only reads the already-cached catalog,
 * so with no custom sites it compared against an empty catalog and found nothing,
 * leaving the bell dark. The fix fetches the default App Store catalog before checking.
 */
public class UpdateManagerTest {

	private AppManager appManager;
	private WebQuerier webQuerier;
	private DownloadSitesManager downloadSitesManager; // real, with zero custom sites
	private UpdateManager updateManager;

	@Before
	public void setUp() {
		appManager = mock(AppManager.class);
		webQuerier = mock(WebQuerier.class);

		// A real manager backed by empty properties => getDownloadSites() is empty,
		// exactly the common case (no user-added download sites) that used to break the bell.
		Properties properties = new Properties();
		CyProperty<Properties> cyProperty = new SimpleCyProperty<Properties>(
				"TestProperties", properties, properties.getClass(), DO_NOT_SAVE);
		downloadSitesManager = new DownloadSitesManager(cyProperty);

		// Build mock-backed values BEFORE the outer when(...) calls: the helpers use their own
		// when(...) stubbing internally, and nesting that inside when(x).thenReturn(...) trips
		// Mockito's UnfinishedStubbingException.
		Set<App> installed = installedApps();

		when(appManager.getWebQuerier()).thenReturn(webQuerier);
		when(appManager.getInstalledApps()).thenReturn(installed);
		// getAllApps() is called for its .size(); return non-null so the fetch step is a no-op catalog.
		when(webQuerier.getAllApps()).thenReturn(Collections.<WebApp>emptySet());

		updateManager = new UpdateManager(appManager, downloadSitesManager);
	}

	/** Precondition sanity: the scenario really has no custom download sites. */
	@Test
	public void testNoCustomDownloadSites() {
		assertEquals(0, downloadSitesManager.getDownloadSites().size());
	}

	/**
	 * The fix: even with zero custom download sites, the startup check must fetch the
	 * DEFAULT App Store catalog. Before the fix this call never happened, so no updates
	 * were ever found at startup.
	 */
	@Test
	public void testHandleEventFetchesDefaultAppStore() throws Exception {
		Set<Update> found = oneUpdate();
		when(webQuerier.checkForUpdates(any(), any())).thenReturn(found);

		CountDownLatch done = awaitCheckComplete();
		updateManager.handleEvent(null); // handleEvent does not use the event object
		assertTrue("startup update check did not complete in time", done.await(5, TimeUnit.SECONDS));

		verify(webQuerier).setCurrentAppStoreUrl(WebQuerier.DEFAULT_APP_STORE_URL);
		verify(webQuerier, atLeastOnce()).getAllApps();
	}

	/** The default store must be fetched BEFORE checkForUpdates reads the cache. */
	@Test
	public void testDefaultStoreFetchedBeforeCheckingForUpdates() throws Exception {
		Set<Update> found = oneUpdate();
		when(webQuerier.checkForUpdates(any(), any())).thenReturn(found);

		CountDownLatch done = awaitCheckComplete();
		updateManager.handleEvent(null);
		assertTrue("startup update check did not complete in time", done.await(5, TimeUnit.SECONDS));

		InOrder inOrder = inOrder(webQuerier);
		inOrder.verify(webQuerier).setCurrentAppStoreUrl(WebQuerier.DEFAULT_APP_STORE_URL);
		inOrder.verify(webQuerier).getAllApps();
		inOrder.verify(webQuerier).checkForUpdates(any(), any());
	}

	/** End result: an available update surfaces in the count the bell reads. */
	@Test
	public void testUpdateCountReflectsFoundUpdate() throws Exception {
		Set<Update> found = oneUpdate();
		when(webQuerier.checkForUpdates(any(), any())).thenReturn(found);

		CountDownLatch done = awaitCheckComplete();
		updateManager.handleEvent(null);
		assertTrue("startup update check did not complete in time", done.await(5, TimeUnit.SECONDS));

		assertEquals(1, updateManager.getUpdateCount());
	}

	// ---- helpers ----

	/** Registers a listener that trips a latch when the update set has been (re)computed. */
	private CountDownLatch awaitCheckComplete() {
		CountDownLatch latch = new CountDownLatch(1);
		updateManager.addUpdatesChangedListener(evt -> latch.countDown());
		return latch;
	}

	private Set<App> installedApps() {
		App app = mock(App.class);
		when(app.getVersion()).thenReturn("3.7.0");
		when(app.getAppName()).thenReturn("CyNDEx-2");
		Set<App> apps = new HashSet<>();
		apps.add(app);
		return apps;
	}

	private Set<Update> oneUpdate() {
		App app = mock(App.class);
		when(app.getVersion()).thenReturn("3.7.0");
		when(app.getAppName()).thenReturn("CyNDEx-2");
		Update update = mock(Update.class);
		when(update.getApp()).thenReturn(app);
		when(update.getUpdateVersion()).thenReturn("3.7.3");
		Set<Update> updates = new HashSet<>();
		updates.add(update);
		return updates;
	}
}
