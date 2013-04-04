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


import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListSingleSelection;


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

	@Tunable(description="Type")
	public ListSingleSelection<String> type = new ListSingleSelection<String>(PROXY_TYPES);

	@Tunable(description="Proxy Server",groups={"Options"},dependsOn="type!=direct",params="displayState=hidden")
	public String hostname="";

	@Tunable(description="Port",groups={"Options"},dependsOn="type!=direct",params="displayState=hidden")
	public int port=0;

	@Tunable(description="User Name", groups={"Options"},dependsOn="type!=direct", params="displayState=hidden")
	public String userName;
	
	@Tunable(description="Password", groups={"Options"},dependsOn="type!=direct", params="displayState=hidden")
	public String password;
	
	private final StreamUtil streamUtil;

	private final Map<String,String> oldSettings;
	private final Properties properties;


	public ProxySettingsTask2(CyProperty<Properties> proxyProperties, final StreamUtil streamUtil) {
		this.streamUtil = streamUtil;
		oldSettings = new HashMap<String,String>();
		properties = proxyProperties.getProperties();
		try {
            final String proxyType = properties.getProperty(PROXY_TYPE);
            if (PROXY_TYPES.contains(proxyType)) {
                type.setSelectedValue(proxyType);
            } else {
                type.setSelectedValue("direct");
            }
			hostname = properties.getProperty(PROXY_HOST);
			port = Integer.parseInt(properties.getProperty(PROXY_PORT));
			userName = properties.getProperty(PROXY_USERNAME);
			try {
				password = decode(properties.getProperty(PROXY_PASSWORD, null));
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
		if (text == null) {
			return null;
		}
		return DatatypeConverter.printBase64Binary(text.getBytes("UTF-8"));
	}
	
	private static String decode(String text) throws IOException {
		if (text == null) {
			return null;
		}
		return new String(DatatypeConverter.parseBase64Binary(text), "UTF-8");
	}
	
	public ValidationState getValidationState(final Appendable errMsg) {
	
		storeProxySettings();

		FutureTask<Exception> task = new FutureTask<Exception>(new TestProxySettings(streamUtil));
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

		revertProxySettings();

		if (result == null)
			return ValidationState.OK;

		try {
			errMsg.append("Cytoscape was unable to connect to the internet because:\n\n" + result.getMessage());
		} catch (final Exception e) {
			/* Intentionally ignored. */
		}
		
		return ValidationState.INVALID;
	}

	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setProgress(0.0);
		storeProxySettings();
		oldSettings.clear();
		
		taskMonitor.setProgress(1.0);
	}

	void storeProxySettings() {
		oldSettings.clear();
		for (String key : KEYS) {
			if (properties.getProperty(key) != null)
				oldSettings.put(key, properties.getProperty(key));
			properties.remove(key);
		}

		String proxyType = type.getSelectedValue(); 
		if ("direct".equals(proxyType)) {
			properties.setProperty(PROXY_TYPE, proxyType);
		} else if ("http".equals(proxyType) || "socks".equals(proxyType)) {
			properties.setProperty(PROXY_TYPE, proxyType);
			properties.setProperty(PROXY_HOST, hostname);
			properties.setProperty(PROXY_PORT, Integer.toString(port));
			if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
				properties.setProperty(PROXY_USERNAME, userName);
				try {
					properties.setProperty(PROXY_PASSWORD, encode(password));
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}
        
        assignSystemProperties();
	}

	void revertProxySettings() {
		for (String key : KEYS) {
			properties.remove(key);
			
			if (oldSettings.containsKey(key))
				properties.setProperty(key, oldSettings.get(key));
		}
		oldSettings.clear();
        
        assignSystemProperties();
	}

	void dumpSettings(String title) {
		System.out.println(title);
		for (String key : KEYS)
			System.out.println(String.format("%s: %s", key, properties.getProperty(key)));
	}
}


final class TestProxySettings implements Callable<Exception> {
	static final String TEST_URL = "http://www.google.com";
	final StreamUtil streamUtil;

	public TestProxySettings(final StreamUtil streamUtil) {
		this.streamUtil = streamUtil;
	}

	public Exception call() {
		try {
			final URL url = new URL(TEST_URL);
			streamUtil.getInputStream(url).close();
		} catch (ProtocolException e) {
			return new IOException("Unable to validate proxy settings.  Please ensure your user name and password are correct (if required).", e);
		} catch (final Exception ex) {
			return ex;
		}

		return null;
	}
}

