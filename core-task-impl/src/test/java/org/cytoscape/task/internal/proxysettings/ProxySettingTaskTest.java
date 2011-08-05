package org.cytoscape.task.internal.proxysettings;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProxySettingTaskTest {

	@Mock private StreamUtil streamUtil;
	@Mock private TaskMonitor tm;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testRun() throws Exception {
		final ProxySettingsTask2 t = new ProxySettingsTask2(streamUtil);

		final String type = "http";
		final String hostName = "dummy";
		final int portNumber = 12345;
		
		t.type.setSelectedValue(type);
		t.hostname = hostName;
		t.port = portNumber;
		
		t.run(tm);
		
		final Properties newProps = System.getProperties();
		
		assertEquals(hostName, newProps.getProperty("http.proxyHost"));
		assertEquals(Integer.toString(portNumber), newProps.getProperty("http.proxyPort"));
	}

}
