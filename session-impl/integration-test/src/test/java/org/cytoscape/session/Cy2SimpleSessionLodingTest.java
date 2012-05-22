package org.cytoscape.session;

import static org.junit.Assert.*;

import java.io.File;

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
public class Cy2SimpleSessionLodingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
//		sessionFile = new File("./src/test/resources/testData/session3x/", "smallSession.cys");
//		checkBasicConfiguration();
	}

	@Test
	public void testLoadSession() throws Exception {
//		final TaskIterator ti = openSessionTF.createTaskIterator(sessionFile);
//		tm.execute(ti);
	}

	@After
	public void confirm() {
		
		// test overall status of current session.
		checkGlobalStatus();
		
	}
	
	private void checkGlobalStatus() {
		
	}

}
