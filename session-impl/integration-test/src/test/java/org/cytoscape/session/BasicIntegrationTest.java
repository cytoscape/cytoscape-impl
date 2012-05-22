/*
 * Copyright (C) 2011 Toni Menzel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cytoscape.session;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.repository;

import java.io.File;

import javax.inject.Inject;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.BundleContext;

/**
 * Build minimum set of Cytoscape to test session loading/saving.
 *
 */
@RunWith(JUnit4TestRunner.class)
// Framework will be reset for each test
@ExamReactorStrategy( AllConfinedStagedReactorFactory.class )
public abstract class BasicIntegrationTest {

	protected static final String API_BUNDLE_VERSION = "3.0.0-beta2-SNAPSHOT";
	protected static final String IMPL_BUNDLE_VERSION = "3.0.0-alpha9-SNAPSHOT";

	
	///////// OSGi Bundle Context ////////////
	@Inject
	protected BundleContext bundleContext;

	///////// Manager objects ////////////////
	
	@Inject
	protected CyNetworkManager networkManager;
	
	@Inject
	protected CyTableManager tableManager;
	
	@Inject
	protected CyNetworkTableManager networkTableManager;
	
	@Inject
	protected CyNetworkViewManager viewManager;
	
	@Inject
	protected CyNetworkFactory networkFactory;
	
	@Inject
	protected CySessionManager sessionManager;
	
	@Inject
	protected VisualMappingManager vmm;
	
	@Inject
	protected RenderingEngineManager renderingEngineManager;
	
	@Inject @Filter("(id=ding)") // Use DING
	protected VisualLexicon lexicon;
	
	@Inject
	protected OpenSessionTaskFactory openSessionTF;
	
	@Inject
	protected SynchronousTaskManager<?> tm;
	
	@Inject
	protected CyApplicationManager applicationManager;
	
	
	// Target file name.  Assume we always have one test session file per test class.
	protected File sessionFile;

	/**
	 * Build minimal set of bundles.
	 */
	@Configuration
	public Option[] config() {
		return options(
				junitBundles(),
				felix(), // Use Felix as runtime
				repository("http://code.cytoscape.org/nexus/content/repositories/snapshots/"),

				// Misc. bundles required to run minimal Cytoscape
				mavenBundle().groupId("cytoscape-sun").artifactId("jhall").version("1.0"),
				mavenBundle().groupId("com.googlecode.guava-osgi").artifactId("guava-osgi").version("9.0.0"),
				mavenBundle().groupId("cytoscape-temp").artifactId("parallelcolt").version("0.9.4"),
				mavenBundle().groupId("cytoscape-temp").artifactId("opencsv").version("2.1"),
				mavenBundle().groupId("com.lowagie.text").artifactId("com.springsource.com.lowagie.text").version("2.0.8"),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-graphicsio").version("2.1.3"),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-graphicsio-svg").version("2.1.3"),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-graphicsio-ps").version("2.1.3"),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-graphics2d").version("2.1.3"),
				mavenBundle().groupId("cytoscape-temp").artifactId("l2fprod-common-shared").version("7.3"),
				mavenBundle().groupId("cytoscape-temp").artifactId("l2fprod-common-fontchooser").version("7.3"),
				mavenBundle().groupId("cytoscape-temp").artifactId("l2fprod-common-sheet").version("7.3"),
				mavenBundle().groupId("cytoscape-temp").artifactId("org.swinglabs.swingx").version("1.6.1"),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-export").version("2.1.1"),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-util").version("2.0.2"),
				
				// API bundles
				mavenBundle().groupId("org.cytoscape").artifactId("event-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("model-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("group-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("viewmodel-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("presentation-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("vizmap-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("session-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("io-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("property-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("work-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("core-task-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("application-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("layout-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("datasource-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("vizmap-gui-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("work-swing-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("swing-application-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("equations-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("swing-application-api").version(API_BUNDLE_VERSION),
				
				mavenBundle().groupId("org.cytoscape").artifactId("service-api").version(API_BUNDLE_VERSION),
				
				// Implementation bundles
				mavenBundle().groupId("org.cytoscape").artifactId("property-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("datasource-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("equations-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("event-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("swing-util-api").version(API_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("model-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("group-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("work-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("work-headless-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("layout-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("viewmodel-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("presentation-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("vizmap-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("application-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("session-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("ding-presentation-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("io-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("core-task-impl").version(IMPL_BUNDLE_VERSION),
				mavenBundle().groupId("org.cytoscape").artifactId("vizmap-gui-impl").version(IMPL_BUNDLE_VERSION)
		);
	}
	
	
	/**
	 * 
	 * Make Sure all required basic objects are available.
	 * 
	 * @throws Exception
	 */
	void checkBasicConfiguration() throws Exception {
		assertNotNull(sessionFile);

		assertNotNull(bundleContext);
		assertNotNull(networkManager);
		assertNotNull(networkTableManager);
		assertNotNull(tableManager);
		assertNotNull(networkFactory);
		assertNotNull(sessionManager);
		assertNotNull(renderingEngineManager);
		assertNotNull(tm);
		assertNotNull(openSessionTF);
		assertNotNull(applicationManager);
	}
}
