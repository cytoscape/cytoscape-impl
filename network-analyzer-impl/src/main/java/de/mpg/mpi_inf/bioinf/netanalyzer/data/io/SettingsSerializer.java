/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.data.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.DOMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.InnerException;
import de.mpg.mpi_inf.bioinf.netanalyzer.Plugin;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Version;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.PluginSettings;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.SettingsGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.XMLSerializable;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Controller class providing static methods for loading and saving settings.
 * <p>
 * This class also provides interface for getting and updating plugin's settings, as well as the default
 * visual settings for a specified complex parameter.
 * </p>
 * 
 * @author Yassen Assenov
 */
public abstract class SettingsSerializer {

	private static final Logger logger = LoggerFactory.getLogger(SettingsSerializer.class);

	/**
	 * Gets the default visual settings group for the specified complex parameter.
	 * 
	 * @param aParam
	 *            ID of complex parameter.
	 * @return Instance of the default visual settings group for the specified parameter; <code>null</code> if
	 *         the parameter ID is unknown.
	 */
	public static SettingsGroup getDefault(String aParam) {
		return visualSettings.get(aParam);
	}

	/**
	 * Gets the name of the expected type of the given complex parameter.
	 * 
	 * @param aParam
	 *            ID of complex parameter.
	 * @return <code>String</code> instance storing the simple name (not prefixed by package name) of the type
	 *         of the given complex parameter; <code>null</code> if the parameter ID is unknown.
	 */
	public static String getDefaultType(String aParam) {
		SettingsGroup settings = visualSettings.get(aParam);
		if (settings != null) {
			String settingsType = settings.getClass().getSimpleName();
			if (settingsType.endsWith("Group")) {
				return settingsType.substring(0, settingsType.length() - 5);
			}
		}
		return null;
	}

	/**
	 * Gets the current plugin's general settings.
	 * 
	 * @return General settings of the NetworkAnalyzer plugin, encapsulated in a <code>PluginSettings</code>
	 *         instance.
	 */
	public static PluginSettings getPluginSettings() {
		return pluginSettings;
	}

	/**
	 * Initializes the default visual settings by loading them from file(s). This method is called upon plugin
	 * initialization only.
	 * <p>
	 * If an external settings file exists, the settings are loaded from it. Otherwise, settings are loaded
	 * from the internal XML settings file (the file in the JAR archive).<br/>
	 * If the external settings file is from a previous version, it contains incomplete data. The missing data
	 * is taken from the internal file.
	 * </p>
	 * 
	 * @throws InnerException
	 *             If locating, opening or parsing the internal XML settings file has failed.
	 */
	public static void initVisualSettings() {
		visualSettings = new HashMap<String, SettingsGroup>();

		// Try to load settings from external XML file
		Document doc = getDocExternal();
		if (doc != null) {
			try {
				loadSettings(doc);
				return;
			} catch (Exception ex) {
				final String msg = Messages.SM_LOADSETTINGSFAIL1 + Plugin.getSettingsFileName()
						+ Messages.SM_LOADSETTINGSFAIL2;
				logger.warn(msg);
			}
		}

		// Loading from external XML file failed, load from internal XML file
		doc = getDocInternal();
		loadSettings(doc);

		// We are starting the plugin for the first time; save the settings
		try {
			save();
		} catch (Exception ex) {
			// FileNotFoundException; IOException; SecurityException
			logger.warn(Messages.DT_IOERROR + " " + Messages.SM_SAVESETERROR, ex);
			return;
		}
	}

	/**
	 * Saves the settings to the XML settings file.
	 * 
	 * @throws FileNotFoundException
	 *             If the default XML settings file could not be created.
	 * @throws IOException
	 *             If an I/O error occurred while writing to the XML settings file.
	 * @throws SecurityException
	 *             If the program does not have write permission for the XML settings file.
	 * @see Plugin#getSettingsFileName()
	 */
	public static void save() throws IOException {
		Element settingsRoot = new Element(rootTag);
		settingsRoot.setAttribute(versionAttribute, Plugin.version);
		settingsRoot.addContent(pluginSettings.toXmlNode());

		for (String id : visualSettings.keySet()) {
			settingsRoot.addContent(visualSettings.get(id).toXmlNode());
		}
		Document settingsDoc = new Document(settingsRoot);
		final Format format = Format.getCompactFormat();
		format.setIndent("\t");
		XMLOutputter saver = new XMLOutputter(format);
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(Plugin.getSettingsFileName());
			saver.output(settingsDoc, stream);
			stream.close();
			stream = null;
		} catch (IOException ex) {
			if (stream != null) {
				stream.close();
			}
			throw ex;
		}
	}

	/**
	 * Sets the default visual settings group for the specified parameter.
	 * <p>
	 * This method also saves the parameters to the XML settings file.
	 * </p>
	 * 
	 * @param aValue
	 *            Instance of the default visual settings group for one of the complex parameter types.
	 * @throws FileNotFoundException
	 *             If the default XML settings file could not be created.
	 * @throws IOException
	 *             If an I/O error occurred while writing to the XML settings file.
	 * @throws SecurityException
	 *             If the program does not have write permission for the XML settings file.
	 * @see #save()
	 */
	public static void setDefault(SettingsGroup aValue) throws IOException {
		visualSettings.put(aValue.getParamID(), aValue);
		save();
	}

	/**
	 * Gets the XML document contained in the external XML settings file.
	 * 
	 * @return <code>Document</code> instance loaded from the external XML settings file; <code>null</code> if
	 *         one of the following is true:
	 *         <ul>
	 *         <li>the plugin does not have read access to the external file</li>
	 *         <li>the external file does not exist</li>
	 *         <li>the external file is not a regular file (e.g. it is a directory)</li>
	 *         <li>the external file is not a valid XML file</li>
	 *         <li>an I/O error has occurred while reading the contents of the external file.</li>
	 *         </ul>
	 */
	private static Document getDocExternal() {
		File settingsFile = new File(Plugin.getSettingsFileName());
		if (settingsFile.exists() && settingsFile.isFile()) {
			try {
				return new SAXBuilder().build(settingsFile);
			} catch (Exception ex) {
				// Opening XML settings file failed, fall through
			}
		}
		return null;
	}

	/**
	 * Gets the XML document contained in the internal XML settings file.
	 * 
	 * @return <code>Document</code> instance loaded from the internal XML settings file.
	 * @throws InnerException
	 *             If locating, opening or parsing the internal XML settings file has failed.
	 */
	private static Document getDocInternal() {
		final URL settingsURL = Plugin.class.getResource(Plugin.settingsFileName);
		try {
			return new SAXBuilder().build(settingsURL);
		} catch (Exception ex) {
			// JDOMException
			// IOException
			// NullPointerException
			throw new InnerException(ex);
		}
	}

	/**
	 * Loads the settings from a given XML document.
	 * 
	 * @param aDoc
	 *            Document that contains the settings.
	 */
	private static void loadSettings(Document aDoc) {
		try {
			Element root = aDoc.getRootElement();
			verifyVersion(root.getAttributeValue("ver"));
			pluginSettings = new PluginSettings(root.getChild(PluginSettings.tag));
			// TODO: [Cytoscape 2.8] Check if a new version of JDom is used
			final List<?> paramSettings = root.getChildren(SettingsGroup.tag);
			for (final Object el : paramSettings) {
				final Element complexParamEl = (Element) el;
				final String type = complexParamEl.getAttributeValue("type");
				final Class<?> sGroup = Plugin.getSettingsGroupClass(type);
				final Constructor<?> constr = sGroup.getConstructor(XMLSerializable.constructorParams);
				final Object[] params = new Object[] { complexParamEl };
				final SettingsGroup value = (SettingsGroup) constr.newInstance(params);
				visualSettings.put(value.getParamID(), value);
			}
		} catch (SecurityException ex) {
			throw ex;
		} catch (Exception ex) {
			// ClassCastException
			// ClassNotFoundException
			// DOMException
			// IllegalArgumentException
			// IllegalAccessException
			// InstantiationException
			// InvocationTargetException
			// NoSuchElementException
			// NoSuchMethodException
			// NullPointerException
			throw new InnerException(ex);
		}
	}

	/**
	 * Verifies the given version is compatible with the version of this plugin.
	 * <p>
	 * In case the versions are incompatible, this method throws an exception.
	 * </p>
	 * 
	 * @param aVersion
	 *            Version to be checked for compatibility.
	 * 
	 * @throws DOMException
	 *             If <code>aVersion</code> is incompatible with {@link Plugin#version}.
	 * @throws NullPointerException
	 *             If <code>aVersion</code> is <code>null</code>.
	 */
	private static void verifyVersion(String aVersion) {
		if (!(new Version(Plugin.version).equals(new Version(aVersion)))) {
			throw new DOMException((short) 9, "Version is not supported");
		}
	}

	/**
	 * Tag name used in the XML settings file to identify NetworkAnalyzer settings.
	 */
	private static final String rootTag = "networkanalyzersettings";

	/**
	 * Name of {@link #rootTag}'s attribute that specifies version of the XML settings file.
	 */
	private static final String versionAttribute = "ver";

	/**
	 * Map of the visual settings for the all complex parameters.
	 */
	private static Map<String, SettingsGroup> visualSettings;

	/**
	 * General settings of the plugin.
	 */
	private static PluginSettings pluginSettings;
}
