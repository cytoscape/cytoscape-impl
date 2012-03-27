package org.cytoscape.task.internal.proxysettings;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Properties;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

public class ProxySettingsTaskFactoryTest {
	@Test
	public void testRun() throws Exception {

		StreamUtil streamUtil = mock(StreamUtil.class);

		Properties properties = new Properties();
		final CyProperty<Properties> proxyProperties = new SimpleCyProperty<Properties>("Test", properties, Properties.class, SavePolicy.DO_NOT_SAVE);
		ProxySettingsTaskFactoryImpl factory = new ProxySettingsTaskFactoryImpl(proxyProperties, streamUtil);
		
		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );				
	}	
}
