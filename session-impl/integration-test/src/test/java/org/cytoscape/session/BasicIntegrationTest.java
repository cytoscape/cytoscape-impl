/* %% Ignore-License */
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

import static org.cytoscape.model.CyNetwork.DEFAULT_ATTRS;
import static org.cytoscape.model.CyNetwork.HIDDEN_ATTRS;
import static org.cytoscape.model.CyNetwork.LOCAL_ATTRS;
import static org.cytoscape.model.CyNetwork.NAME;
import static org.cytoscape.model.CyNetwork.SELECTED;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_ATTRS;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_NAME;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_TITLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
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
	@Inject
	protected CySessionReaderManager sessionReaderManager;
	@Inject
	protected CyGroupManager groupManager;
	
	
	// Target file name.  Assume we always have one test session file per test class.
	protected File sessionFile;

	/**
	 * Build minimal set of bundles.
	 */
	@Configuration
	public Option[] config() {
		// These system properties are set in the surefire configuration in the pom.
		String apiBundleVersion = System.getProperty("cytoscape.api.version");
		String implBundleVersion = System.getProperty("cytoscape.impl.version");

		return options(
				systemProperty("org.osgi.framework.system.packages.extra").value("com.sun.xml.internal.bind"),
				junitBundles(),
				vmOption("-Xmx512M"),

				// Use Felix as runtime
				felix(), 

				// So that we actually start all of our bundles!
				frameworkStartLevel(50),

				// Specify all of our repositories
				repository("http://code.cytoscape.org/nexus/content/repositories/snapshots/"),
				repository("http://code.cytoscape.org/nexus/content/repositories/releases/"),
				repository("http://code.cytoscape.org/nexus/content/repositories/thirdparty/"),

				// Misc. bundles required to run minimal Cytoscape
				mavenBundle().groupId("cytoscape-sun").artifactId("jhall").version("1.0").startLevel(3),
				mavenBundle().groupId("com.googlecode.guava-osgi").artifactId("guava-osgi").version("9.0.0").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("parallelcolt").version("0.9.4").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("opencsv").version("2.1").startLevel(3),
				mavenBundle().groupId("com.lowagie.text").artifactId("com.springsource.com.lowagie.text").version("2.0.8").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-graphicsio").version("2.1.3").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-graphicsio-svg").version("2.1.3").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-graphicsio-ps").version("2.1.3").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-graphics2d").version("2.1.3").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("l2fprod-common-shared").version("7.3").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("l2fprod-common-fontchooser").version("7.3").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("l2fprod-common-sheet").version("7.3").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("org.swinglabs.swingx").version("1.6.1").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-export").version("2.1.1").startLevel(3),
				mavenBundle().groupId("cytoscape-temp").artifactId("freehep-util").version("2.0.2").startLevel(3),
				mavenBundle().groupId("org.apache.servicemix.specs").artifactId("org.apache.servicemix.specs.jaxb-api-2.1").version("1.2.0").startLevel(3),
				mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.jaxb-impl").version("2.1.6_1").startLevel(3),
				mavenBundle().groupId("javax.activation").artifactId("com.springsource.javax.activation").version("1.1.1").startLevel(3),
				mavenBundle().groupId("javax.xml.stream").artifactId("com.springsource.javax.xml.stream").version("1.0.1").startLevel(3),
				mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.eventadmin").version("1.2.14").startLevel(3),
				
				// API bundles
				mavenBundle().groupId("org.cytoscape").artifactId("event-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("model-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("group-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("viewmodel-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("presentation-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("vizmap-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("session-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("io-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("property-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("work-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("core-task-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("application-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("layout-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("datasource-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("vizmap-gui-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("work-swing-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("swing-application-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("equations-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("swing-application-api").version(apiBundleVersion).startLevel(5),
				mavenBundle().groupId("org.cytoscape").artifactId("service-api").version(apiBundleVersion).startLevel(5),
				
				// Implementation bundles
				mavenBundle().groupId("org.cytoscape").artifactId("property-impl").version(implBundleVersion).startLevel(7),

				mavenBundle().groupId("org.cytoscape").artifactId("swing-util-api").version(apiBundleVersion).startLevel(8),

				mavenBundle().groupId("org.cytoscape").artifactId("datasource-impl").version(implBundleVersion).startLevel(9),
				mavenBundle().groupId("org.cytoscape").artifactId("equations-impl").version(implBundleVersion).startLevel(9),
				mavenBundle().groupId("org.cytoscape").artifactId("event-impl").version(implBundleVersion).startLevel(9),

				mavenBundle().groupId("org.cytoscape").artifactId("model-impl").version(implBundleVersion).startLevel(11),
				mavenBundle().groupId("org.cytoscape").artifactId("group-impl").version(implBundleVersion).startLevel(11),
				mavenBundle().groupId("org.cytoscape").artifactId("work-impl").version(implBundleVersion).startLevel(11),

				mavenBundle().groupId("org.cytoscape").artifactId("work-headless-impl").version(implBundleVersion).startLevel(11),

				mavenBundle().groupId("org.cytoscape").artifactId("presentation-impl").version(implBundleVersion).startLevel(13),

				mavenBundle().groupId("org.cytoscape").artifactId("layout-impl").version(implBundleVersion).startLevel(15),
				mavenBundle().groupId("org.cytoscape").artifactId("viewmodel-impl").version(implBundleVersion).startLevel(15),
				mavenBundle().groupId("org.cytoscape").artifactId("vizmap-impl").version(implBundleVersion).startLevel(15),
				mavenBundle().groupId("org.cytoscape.distribution").artifactId("application-metadata-impl").version(implBundleVersion).startLevel(15).noStart(),

				mavenBundle().groupId("org.cytoscape").artifactId("application-impl").version(implBundleVersion).startLevel(17),

				mavenBundle().groupId("org.cytoscape").artifactId("session-impl").version(implBundleVersion).startLevel(19),

				mavenBundle().groupId("org.cytoscape").artifactId("vizmap-gui-core-impl").version(implBundleVersion).startLevel(20),
				mavenBundle().groupId("org.cytoscape").artifactId("ding-presentation-impl").version(implBundleVersion).startLevel(21),

				mavenBundle().groupId("org.cytoscape").artifactId("io-impl").version(implBundleVersion).startLevel(23),

				mavenBundle().groupId("org.cytoscape").artifactId("core-task-impl").version(implBundleVersion).startLevel(25)

				//mavenBundle().groupId("org.cytoscape").artifactId("vizmap-gui-impl").version(implBundleVersion).startLevel(27)
		);
	}
	
	/**
	 * Cy2 sessions only!
	 * @param net
	 */
	protected void assertCy2CustomColumnsAreMutable(CyNetwork net) {
		// User or non-default columns should be immutable
		List<CyTable> tables = new ArrayList<CyTable>();
		tables.add(net.getTable(CyNetwork.class, LOCAL_ATTRS));
		tables.add(net.getTable(CyNetwork.class, HIDDEN_ATTRS));
		tables.add(net.getTable(CyNetwork.class, DEFAULT_ATTRS));
		tables.add(net.getTable(CyNode.class, LOCAL_ATTRS));
		tables.add(net.getTable(CyNode.class, HIDDEN_ATTRS));
		tables.add(net.getTable(CyNode.class, DEFAULT_ATTRS));
		tables.add(net.getTable(CyEdge.class, LOCAL_ATTRS));
		tables.add(net.getTable(CyEdge.class, HIDDEN_ATTRS));
		tables.add(net.getTable(CyEdge.class, DEFAULT_ATTRS));
		
		if (net instanceof CyRootNetwork) {
			tables.add(net.getTable(CyNetwork.class, SHARED_ATTRS));
			tables.add(net.getTable(CyNode.class, SHARED_ATTRS));
			tables.add(net.getTable(CyEdge.class, SHARED_ATTRS));
		}
		
		for (CyTable t : tables) {
			for (CyColumn c : t.getColumns()) {
				String name = c.getName();
				if (!name.equals(CyNetwork.SUID)
						&& !name.equals(NAME)
						&& !name.equals(CyNetwork.SELECTED)
						&& !name.equals(CyEdge.INTERACTION)
						&& !name.equals(CyRootNetwork.SHARED_NAME)
						&& !name.equals(CyRootNetwork.SHARED_INTERACTION)) {
					assertFalse("Column " + c.getName() + " should NOT be immutable", c.isImmutable());
				}
			}
		}
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
		assertNotNull(sessionReaderManager);
		assertNotNull(groupManager);
	}
	
	protected void checkNodeEdgeCount(CyNetwork net, int nodeCount, int edgeCount, int selectedNodes, int selectedEdges) {
		assertEquals(net + ": incorrect selection", nodeCount, net.getNodeCount());
		assertEquals(net + ": incorrect selection", edgeCount, net.getEdgeCount());
		assertEquals(net + ": incorrect selection", selectedNodes, net.getTable(CyNode.class, LOCAL_ATTRS).getMatchingRows(SELECTED, true).size());
		assertEquals(net + ": incorrect selection", selectedEdges, net.getTable(CyEdge.class, LOCAL_ATTRS).getMatchingRows(SELECTED, true).size());
	}
	
	protected void checkSelection(CyNetwork net, CyIdentifiable element, boolean selected) {
		assertEquals(selected, net.getRow(element, LOCAL_ATTRS).get(SELECTED, Boolean.class));
	}
	
	protected void checkNetworkVisualProperties(final CyNetworkView view,
												final String title, final Color color,
												final double w, final double h,
												final double cx, final double cy,
												final double scale) {
	assertEquals(title, view.getVisualProperty(NETWORK_TITLE));
	assertEquals(color, view.getVisualProperty(NETWORK_BACKGROUND_PAINT));
	assertEquals(new Double(w), view.getVisualProperty(NETWORK_WIDTH));
	assertEquals(new Double(h), view.getVisualProperty(NETWORK_HEIGHT));
	assertEquals(new Double(cx), view.getVisualProperty(NETWORK_CENTER_X_LOCATION));
	assertEquals(new Double(cy), view.getVisualProperty(NETWORK_CENTER_Y_LOCATION));
	assertEquals(new Double(scale), view.getVisualProperty(NETWORK_SCALE_FACTOR));
	}
	
	protected CyNetwork getNetworkByName(String name) {
		for (CyNetwork net : networkManager.getNetworkSet()) {
			if (name.equals(net.getRow(net).get(NAME, String.class)))
				return net;
		}
		
		return null;
	}
	
	protected CyNode getNodeByName(CyNetwork net, String name) {
		for (CyNode n : net.getNodeList()) {
			CyRow row = net.getRow(n);
			
			if (name.equals(row.get(NAME, String.class)) || name.equals(row.get(SHARED_NAME, String.class)))
				return n;
		}
		
		return null;
	}
	
	protected CyEdge getEdgeByName(CyNetwork net, String name) {
		for (CyEdge e : net.getEdgeList()) {
			CyRow row = net.getRow(e);
			
			if (name.equals(row.get(NAME, String.class)) || name.equals(row.get(SHARED_NAME, String.class)))
				return e;
		}
		
		return null;
	}
}
