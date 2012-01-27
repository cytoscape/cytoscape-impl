package org.cytoscape.task.internal.proxysettings;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
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
		Properties properties = new Properties();
		final CyProperty<Properties> proxyProperties = new SimpleCyProperty<Properties>("Test", properties, Properties.class, SavePolicy.DO_NOT_SAVE);
		final ProxySettingsTask2 t = new ProxySettingsTask2(proxyProperties, streamUtil);

		final String type = "http";
		final String hostName = "dummy";
		final int portNumber = 12345;
		
		t.type.setSelectedValue(type);
		t.hostname = hostName;
		t.port = portNumber;
		
		t.run(tm);
		
		assertEquals(hostName, properties.getProperty(ProxySettingsTask2.PROXY_HOST));
		assertEquals(Integer.toString(portNumber), properties.getProperty(ProxySettingsTask2.PROXY_PORT));
	}

}
