package org.cytoscape.session;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;


@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class Cy283ComplexSessionLodingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session2x/", "v283Session1.cys");
		if(!sessionFile.exists())
			fail("Could not find the file! " + sessionFile.toString());
		
		checkBasicConfiguration();
	}

	@Test
	public void testLoadSession() throws Exception {
		final TaskIterator ti = openSessionTF.createTaskIterator(sessionFile);
		tm.execute(ti);
	}

	@After
	public void confirm() {
		// test overall status of current session.
		checkGlobalStatus();


	}
	
	private void checkGlobalStatus() {
		assertEquals(5, networkManager.getNetworkSet().size());
		assertEquals(4, viewManager.getNetworkViewSet().size());

		// Since this test runs in headless mode, this should be zero.
		assertEquals(0, renderingEngineManager.getAllRenderingEngines().size());

		// 6 tables per network
		assertEquals(30, tableManager.getAllTables(true).size());
		assertEquals(15, tableManager.getAllTables(false).size());

	}

}
