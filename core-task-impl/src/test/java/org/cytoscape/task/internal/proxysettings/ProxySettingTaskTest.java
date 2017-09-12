package org.cytoscape.task.internal.proxysettings;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class ProxySettingTaskTest {

	@Mock private StreamUtil streamUtil;
	@Mock private CyEventHelper eventHelper;
	@Mock private TaskMonitor tm;
	@Mock CyServiceRegistrar serviceRegistrar;
	
	private Properties props = new Properties();
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		
		CyProperty<Properties> proxyProps =
				new SimpleCyProperty<>("Test", props, Properties.class, SavePolicy.DO_NOT_SAVE);
		
		when(serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")).thenReturn(proxyProps);
		when(serviceRegistrar.getService(StreamUtil.class)).thenReturn(streamUtil);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
	}

	@Test
	public void testRun() throws Exception {
		final ProxySettingsTask2 t = new ProxySettingsTask2(serviceRegistrar);

		final String type = "http";
		final String hostName = "dummy";
		final int portNumber = 12345;

		t.type.setSelectedValue(type);
		t.hostname = hostName;
		t.port = portNumber;

		t.run(tm);

		assertEquals(hostName, props.getProperty(ProxySettingsTask2.PROXY_HOST));
		assertEquals(Integer.toString(portNumber), props.getProperty(ProxySettingsTask2.PROXY_PORT));
	}
}
