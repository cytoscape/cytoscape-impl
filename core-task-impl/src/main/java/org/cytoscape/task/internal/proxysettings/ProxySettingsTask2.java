package org.cytoscape.task.internal.proxysettings;


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
	
	private static final List<String> KEYS = Arrays.asList(PROXY_HOST, PROXY_PORT, PROXY_TYPE);

	@Tunable(description="Type")
	public ListSingleSelection<String> type = new ListSingleSelection<String>("direct", "http", "socks");

	@Tunable(description="Proxy Server",groups={"param"},dependsOn="type!=direct",params="alignments=horizontal;displayState=hidden")
	public String hostname="";

	@Tunable(description="Port",groups={"param"},dependsOn="type!=direct",params="alignments=horizontal;displayState=hidden")
	public int port=0;

	private final StreamUtil streamUtil;

	private final Map<String,String> oldSettings;
	private final Properties properties;

	public ProxySettingsTask2(CyProperty<Properties> proxyProperties, final StreamUtil streamUtil) {
		this.streamUtil = streamUtil;
		oldSettings = new HashMap<String,String>();
		properties = proxyProperties.getProperties();
		try {
			type.setSelectedValue(properties.getProperty(PROXY_TYPE));
			hostname = properties.getProperty(PROXY_HOST);
			port = Integer.parseInt(properties.getProperty(PROXY_PORT));
		} catch (IllegalArgumentException e) {
			type.setSelectedValue("direct");
			hostname = "";
			port = 0;
		}
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
			/* Intentionally ignored! */
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
		}
	}

	void revertProxySettings() {
		for (String key : KEYS) {
			properties.remove(key);
			
			if (oldSettings.containsKey(key))
				properties.setProperty(key, oldSettings.get(key));
		}
		oldSettings.clear();
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
		} catch (final Exception ex) {
			return ex;
		}

		return null;
	}
}

