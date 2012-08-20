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

package de.mpg.mpi_inf.bioinf.netanalyzer;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import javax.swing.JMenu;

import org.cytoscape.application.swing.CySwingApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;
import org.cytoscape.application.CyVersion;
import org.cytoscape.property.CyProperty;


/**
 * Wrapper class for the NetworkAnalyzer plugin.
 * 
 * @author Yassen Assenov
 */
public class Plugin {

	private static final Logger logger = LoggerFactory.getLogger(Plugin.class);

	/**
	 * Name of directory that contains Cytoscape plugins as a path relative to Cytoscape's root directory.
	 */
	public static final String cytoscapeDir = System.getProperty("user.home") + File.separator + CyProperty.DEFAULT_PROPS_CONFIG_DIR ;

	/**
	 * Name of XML settings file. Both the internal and external setting files have this name.
	 */
	public static final String settingsFileName = "NetworkAnalyzer.xml";

	/**
	 * Gets the decorator class given decorators name.
	 * 
	 * @param aName
	 *            Name of the decorator as defined in XML settings file.
	 * @return <code>Class</code> instance of the decorator.
	 * @throws ClassNotFoundException
	 *             If the given decorator does not exist.
	 */
	public static Class<?> getDecoratorClass(String aName) throws ClassNotFoundException {
		return Class.forName("de.mpg.mpi_inf.bioinf.netanalyzer.dec." + aName);
	}

	/**
	 * Gets the filter class corresponding to the given complex parameter type.
	 * 
	 * @param aParamType
	 *            Complex parameter type.
	 * @return <code>Class</code> instance of the filter for the specified complex parameter type.
	 * @throws ClassNotFoundException
	 *             If the given complex parameter type is invalid or it does not have a corresponding filter.
	 */
	public static Class<?> getFilterClass(String aParamType) throws ClassNotFoundException {
		return Class.forName("de.mpg.mpi_inf.bioinf.netanalyzer.data.filter." + aParamType + "Filter");
	}

	/**
	 * Gets the filter dialog class (extending
	 * {@link de.mpg.mpi_inf.bioinf.netanalyzer.ui.filter.ComplexParamFilterDialog}) corresponding to the
	 * given complex parameter type.
	 * 
	 * @param aParamClass
	 *            <code>Class</code> instance of a complex parameter type.
	 * @return <code>Class</code> instance of the filter dialog for the specified complex parameter.
	 * @throws ClassNotFoundException
	 *             If the given complex parameter type is invalid or it does not have a corresponding filter.
	 */
	public static Class<?> getFilterDialogClass(Class<?> aParamClass) throws ClassNotFoundException {
		return getFilterDialogClass(aParamClass.getSimpleName());
	}

	/**
	 * Gets the filter dialog class (extending
	 * {@link de.mpg.mpi_inf.bioinf.netanalyzer.ui.filter.ComplexParamFilterDialog}) corresponding to the
	 * given complex parameter type.
	 * 
	 * @param aParamType
	 *            Complex parameter type.
	 * @return <code>Class</code> instance of the filter dialog for the specified complex parameter.
	 * @throws ClassNotFoundException
	 *             If the given complex parameter type is invalid or it does not have a corresponding filter.
	 */
	public static Class<?> getFilterDialogClass(String aParamType) throws ClassNotFoundException {
		return Class.forName("de.mpg.mpi_inf.bioinf.netanalyzer.ui.filter." + aParamType + "FilterDialog");
	}

	/**
	 * Gets the settings group class corresponding to the given complex parameter type.
	 * 
	 * @param aParamType
	 *            Complex parameter type.
	 * @return <code>Class</code> instance of the settings group type that manages the visual settings for the
	 *         specified complex parameter.
	 * @throws ClassNotFoundException
	 *             If the given complex parameter type is invalid.
	 */
	public static Class<?> getSettingsGroupClass(String aParamType) throws ClassNotFoundException {
		return Class.forName("de.mpg.mpi_inf.bioinf.netanalyzer.data.settings." + aParamType + "Group");
	}

	/**
	 * Gets the name of the external XML settings file.
	 * 
	 * @return Name of the external XML settings file as a path relative to Cytoscape's root directory.
	 */
	public static String getSettingsFileName() {
		return cytoscapeDir + File.separator + settingsFileName;
	}

	/**
	 * Gets the visualizer class for the given complex parameter type.
	 * 
	 * @param aParamType
	 *            Complex parameter type.
	 * @return <code>Class</code> instance of a visualizer that displays data stored in instances of the
	 *         specified complex parameter.
	 * @throws ClassNotFoundException
	 *             If the given complex parameter type is invalid.
	 */
	public static Class<?> getVisualizerClass(String aParamType) throws ClassNotFoundException {
		return Class.forName("de.mpg.mpi_inf.bioinf.netanalyzer.ui." + aParamType + "Visualizer");
	}

	/**
	 * Checks if the specified complex parameter type has a filter.
	 * 
	 * @param aParamClass
	 *            <code>Class</code> instance of a complex parameter type to be checked for filter.
	 * @return <code>true</code> if <code>aParamType</code> is class a representing valid complex parameter
	 *         type and there is a filter class for this type; <code>false</code> otherwise.
	 */
	public static boolean hasFilter(Class<?> aParamClass) {
		return hasFilter(aParamClass.getSimpleName());
	}

	/**
	 * Checks if the specified complex parameter type has a filter.
	 * 
	 * @param aParamType
	 *            Complex parameter type to be checked for filter.
	 * @return <code>true</code> if <code>aParamType</code> is a valid complex parameter type and there is a
	 *         filter class for this type; <code>false</code> otherwise.
	 */
	public static boolean hasFilter(String aParamType) {
		try {
			getFilterClass(aParamType);
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

	/**
	 * Initializes a new instance of <code>Plugin</code>.
	 */
	public Plugin(CySwingApplication swingApp) {
		try {
			// Initiate default visual settings
			SettingsSerializer.initVisualSettings();
			// If initialization fails, the following lines are not executed:

		} catch (SecurityException ex) {
			Utils.showErrorBox(swingApp.getJFrame(),Messages.DT_SECERROR, Messages.SM_SECERROR1);
			System.err.println(Messages.SM_SECERROR1);
		} catch (InnerException ex) {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(os);
			ex.printStackTrace(ps);
			ps.flush();
			if (os.toString().contains("NoClassDefFoundError:")) {
				// Library is missing
				logger.error(Messages.SM_LOGERROR, ex);
			} else {
				// NetworkAnalyzer internal error
				logger.error(Messages.SM_LOGERROR, ex);
			}
		}
	}
}
