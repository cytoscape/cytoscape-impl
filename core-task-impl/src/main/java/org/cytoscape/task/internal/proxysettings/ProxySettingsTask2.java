package org.cytoscape.task.internal.proxysettings;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.DatatypeConverter;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListSingleSelection;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
 * Dialog for assigning proxy settings.
 */
public class ProxySettingsTask2 extends AbstractTask implements TunableValidator {
	
	@ProvidesTitle
	public String getTitle() {
		return "Proxy Settings";
	}
	
	static final String PROXY_HOST = "proxy.server";
	static final String PROXY_PORT = "proxy.server.port";
	static final String PROXY_TYPE = "proxy.server.type";
	static final String PROXY_USERNAME = "proxy.server.userName";
	static final String PROXY_PASSWORD = "proxy.server.password";
	
	private static final List<String> KEYS = Arrays.asList(PROXY_HOST, PROXY_PORT, PROXY_TYPE, PROXY_USERNAME, PROXY_PASSWORD);

	private static final List<String> PROXY_TYPES = Arrays.asList("direct", "http", "socks");

	@Tunable(description = "Type:")
	public ListSingleSelection<String> type = new ListSingleSelection<>(PROXY_TYPES);

	@Tunable(description = "Proxy Server:", groups = { "Options" }, dependsOn = "type!=direct")
	public String hostname = "";

	@Tunable(description = "Port:", groups = { "Options" }, dependsOn = "type!=direct")
	public int port;

	@Tunable(description = "User Name:", groups = { "Options" }, dependsOn = "type!=direct")
	public String userName;

	@Tunable(description = "Password:", groups = { "Options" }, dependsOn = "type!=direct")
	public String password;
	
	private final Map<String, String> oldSettings;
	private final CyServiceRegistrar serviceRegistrar;

	public ProxySettingsTask2(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		oldSettings = new HashMap<>();
		
		try {
			final CyProperty<Properties> proxyProps = getProxyProperties();
			final Properties props = proxyProps.getProperties();
			final String proxyType = props.getProperty(PROXY_TYPE);
			
			if (PROXY_TYPES.contains(proxyType))
				type.setSelectedValue(proxyType);
			else
				type.setSelectedValue("direct");

			hostname = props.getProperty(PROXY_HOST);
			port = Integer.parseInt(props.getProperty(PROXY_PORT));
			userName = props.getProperty(PROXY_USERNAME);

			try {
				password = decode(props.getProperty(PROXY_PASSWORD, null));
			} catch (IOException e) {
				password = null;
			}
		} catch (IllegalArgumentException e) {
			type.setSelectedValue("direct");
			hostname = "";
			port = 0;
		}

		assignSystemProperties();
	}

    public void assignSystemProperties() {
        if ("direct".equals(type.getSelectedValue())) {
            System.setProperty("http.proxyHost", "");
            System.setProperty("http.proxyPort", "");
            System.setProperty("socksProxyHost", "");
            System.setProperty("socksProxyPort", "");
        } else if ("http".equals(type.getSelectedValue())) {
            System.setProperty("http.proxyHost", hostname);
            System.setProperty("http.proxyPort", Integer.toString(port));
            System.setProperty("socksProxyHost", "");
            System.setProperty("socksProxyPort", "");
        } else if ("socks".equals(type.getSelectedValue())) {
            System.setProperty("http.proxyHost", "");
            System.setProperty("http.proxyPort", "");
            System.setProperty("socksProxyHost", hostname);
            System.setProperty("socksProxyPort", Integer.toString(port));
        }
    }

	private static String encode(String text) throws IOException {
		if (text == null)
			return null;
		
		return DatatypeConverter.printBase64Binary(text.getBytes("UTF-8"));
	}
	
	private static String decode(String text) throws IOException {
		if (text == null)
			return null;
		
		return new String(DatatypeConverter.parseBase64Binary(text), "UTF-8");
	}
	
	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		final CyProperty<Properties> proxyProps = getProxyProperties();
		final Properties props = proxyProps.getProperties();
		storeProxySettings(props);
		
		final StreamUtil streamUtil = serviceRegistrar.getService(StreamUtil.class);
		final FutureTask<Exception> task = new FutureTask<>(new TestProxySettings(streamUtil));
		Exception result = null;
		
		try {
			new Thread(task).start();
			result = task.get(10, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			result = e;
		} catch (final ExecutionException e) {
			result = e;
		} catch (final TimeoutException e) {
			result = e;
		}

		revertProxySettings(props);

		if (result == null)
			return ValidationState.OK;

		try {
			errMsg.append("Cytoscape was unable to connect to the internet because these proxy settings are not correct:\n\n" + result.getMessage());
		} catch (final Exception e) {
			/* Intentionally ignored. */
		}
		
		return ValidationState.INVALID;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setProgress(0.0);
		
		final CyProperty<Properties> proxyProps = getProxyProperties();
		final Properties props = proxyProps.getProperties();
		storeProxySettings(props);
		
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		
		for (String key : oldSettings.keySet()) {
			if (!oldSettings.get(key).equals(props.get(key))) {
				eventHelper.fireEvent(new PropertyUpdatedEvent(proxyProps));
				break;
			}
		}
		
		oldSettings.clear();

		tm.setProgress(1.0);
	}

	private void storeProxySettings(Properties props) {
		oldSettings.clear();
		
		for (String key : KEYS) {
			if (props.getProperty(key) != null)
				oldSettings.put(key, props.getProperty(key));
			
			props.remove(key);
		}
		
		String proxyType = type.getSelectedValue(); 
		
		if ("direct".equals(proxyType)) {
			props.setProperty(PROXY_TYPE, proxyType);
		} else if ("http".equals(proxyType) || "socks".equals(proxyType)) {
			props.setProperty(PROXY_TYPE, proxyType);
			props.setProperty(PROXY_HOST, hostname);
			props.setProperty(PROXY_PORT, Integer.toString(port));
			
			if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
				props.setProperty(PROXY_USERNAME, userName);
				
				try {
					props.setProperty(PROXY_PASSWORD, encode(password));
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}
        
        assignSystemProperties();
	}

	private void revertProxySettings(Properties props) {
		for (String key : KEYS) {
			props.remove(key);
			
			if (oldSettings.containsKey(key))
				props.setProperty(key, oldSettings.get(key));
		}
		
		oldSettings.clear();
        assignSystemProperties();
	}
	
	@SuppressWarnings("unchecked")
	private CyProperty<Properties> getProxyProperties() {
		return serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
	}
}

final class TestProxySettings implements Callable<Exception> {

	static final String TEST_URL = "http://www.google.com/";
	final StreamUtil streamUtil;

	public TestProxySettings(final StreamUtil streamUtil) {
		this.streamUtil = streamUtil;
	}

	@Override
	public Exception call() {
		try {
			final URL url = new URL(TEST_URL);
			streamUtil.getInputStream(url).close();
		} catch (ProtocolException e) {
			return new IOException(
					"Unable to validate proxy settings.  Please ensure your user name and password are correct (if required).",
					e);
		} catch (final Exception ex) {
			return ex;
		}

		return null;
	}
}
