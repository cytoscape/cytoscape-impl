package org.cytoscape.task.internal.proxysettings;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
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
	@Mock private CyEventHelper eventHelper;
	@Mock private TaskMonitor tm;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testRun() throws Exception {
		Properties properties = new Properties();
		final CyProperty<Properties> proxyProperties = new SimpleCyProperty<Properties>("Test", properties, Properties.class, SavePolicy.DO_NOT_SAVE);
		final ProxySettingsTask2 t = new ProxySettingsTask2(proxyProperties, streamUtil, eventHelper);

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
