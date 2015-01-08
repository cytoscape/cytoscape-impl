package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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

import java.awt.Dialog;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.jfree.chart.JFreeChart;

import de.mpg.mpi_inf.bioinf.netanalyzer.InnerException;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.GeneralVisSettings;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.SettingsGroup;

/**
 * Base class for all classes responsible for the visualization of a certain complex parameter type.
 * 
 * @author Yassen Assenov
 */
public abstract class ComplexParamVisualizer {

	/**
	 * Creates the chart control that displays the complex parameter.
	 * 
	 * @return Chart control as a <code>JFreeChart</code> instance.
	 */
	public abstract JFreeChart createControl();

	/**
	 * Gets the complex parameter instance managed by this visualizer.
	 * 
	 * @return Complex parameter instance managed by this visualizer.
	 */
	public abstract ComplexParam getComplexParam();

	/**
	 * Gets the settings for the visualized complex parameter instance.
	 * 
	 * @return The <code>SettingsGroup</code> instance for the visualized complex parameter.
	 */
	public abstract SettingsGroup getSettings();

	/**
	 * Sets the complex parameter instance managed by this visualizer.
	 * <p>
	 * <b>Note:</b> It is the responsibility of the caller to recreate all the controls created by this
	 * visualizer by calling the {@link #createControl()} method for each of them.
	 * </p>
	 * 
	 * @param aParam Complex parameter instance to be managed by this visualizer.
	 * @throws ClassCastException If the specified complex parameter is not of the expected type.
	 * @throws NullPointerException If <code>aParam</code> is <code>null</code>.
	 */
	public abstract void setComplexParam(ComplexParam aParam);

	/**
	 * Gets the title of the displayed complex parameter.
	 * 
	 * @return Description of the complex parameter in a human readable form.
	 */
	public String getTitle() {
		if (general != null) {
			return general.getTitle();
		}
		return null;
	}

	/**
	 * Pops up a dialog for viewing/editing complex parameter settings.
	 * <p>
	 * This method automatically updates the visual settings for the complex parameter if the user presses the
	 * &quot;OK&quot; or &quot;Save as Default&quot; button. It also attempts to save the settings if the user
	 * presses the &quot;Save as Default&quot; button and displays an error message in case the attempt was
	 * not successful.
	 * </p>
	 * <p>
	 * <b>Note:</b> It is the responsibility of the caller to update all the controls created by the
	 * visualizer by calling the {@link #updateControl(JFreeChart)} method for each of them.
	 * </p>
	 * 
	 * @param aOwner The <code>Dialog</code> from which the settings dialog is displayed.
	 * @return The status as returned by the settings dialog.
	 * @see SettingsDialog#STATUS_CANCEL
	 * @see SettingsDialog#STATUS_DEFAULT
	 * @see SettingsDialog#STATUS_OK
	 */
	public int showSettingsDialog(Dialog aOwner) {
		SettingsDialog d = new SettingsDialog(aOwner, Messages.DI_CHARTSETTINGS);
		JComponent dataComp = addSettingsPanels(d.getSettingsPane());

		d.pack();
		d.setLocationRelativeTo(aOwner);
		d.setVisible(true);

		int status = d.getStatus();
		if (status != SettingsDialog.STATUS_CANCEL) {
			try {
				d.update();
				updateSettings(dataComp);
				if (status == SettingsDialog.STATUS_DEFAULT) {
					try {
						saveDefault();
					} catch (SecurityException ex) {
						Utils.showErrorBox(aOwner, Messages.DT_SECERROR, Messages.SM_SECERROR2);
					} catch (Exception ex) {
						// FileNotFoundException
						// IOException
						Utils.showErrorBox(aOwner, Messages.DT_IOERROR, Messages.SM_DEFFAILED);
					}
				}
			} catch (InvocationTargetException ex) {
				throw new InnerException(ex);
			}
		}
		return status;
	}

	/**
	 * Updates the given chart control according to the visual settings for the complex parameter.
	 * 
	 * @param aControl Control to be updated. This must be an instance of <code>JFreeChart</code>, as
	 *        returned by {@link #createControl()}.
	 * @return The updated chart control, which may be the same instance as <code>aControl</code>, but it
	 *         may also be a newly created instance.
	 */
	public abstract JFreeChart updateControl(JFreeChart aControl);

	/**
	 * Adds a new tab to the given tabbed pane.
	 * 
	 * @param aPanel Tabbed pane to be used.
	 * @param aTitle Title of the new tab.
	 * @param aComp Component to be added.
	 * @param aToolTip Tool-tip of the new tab. Set this to <code>null</code> if the tab title does not a
	 *        have a tool-tip.
	 */
	protected static void addTab(JTabbedPane aPanel, String aTitle, JComponent aComp, String aToolTip) {
		if (LookAndFeelUtil.isAquaLAF())
			aComp.setOpaque(false);
		
		aPanel.addTab(aTitle, null, aComp, aToolTip);
	}

	/**
	 * Adds the tabs for the parameter-specific visual settings, e.g. axes-related settings, bar chart
	 * setting, and others.
	 * 
	 * @param aPanel Tabbed control to adds tabs to.
	 * @return Component to be passed as an argument of {@link #updateSettings(JComponent)};
	 *         <code>null</code> if the visualizer instance does not encode any visualization settings
	 *         information into controls other than {@link SettingsPanel}.
	 */
	protected abstract JComponent addSettingsPanels(JTabbedPane aPanel);

	/**
	 * Sets the stored settings for this complex parameter as default.
	 * <p>
	 * This method calls
	 * {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer#setDefault(SettingsGroup)} which
	 * results in saving the settings to the XML settings file of the plugin.
	 * </p>
	 * 
	 * @throws IOException If saving the XML settings file failed due to an I/O error.
	 */
	protected abstract void saveDefault() throws IOException;

	/**
	 * Updates the visual settings instances based on the state of some component in the settings dialog.
	 * <p>
	 * This method is called only in the scenario when the settings dialog is displayed and the user presses
	 * the &quot;OK&quot; or &quot;Save as Default&quot; button. In such a case, this method is invoked
	 * <b>after</b> invoking {@link SettingsDialog#update()}.
	 * </p>
	 * <p>
	 * An inheriting class should override this method when some properties are not automatically updated by
	 * the {@link SettingsDialog#update()}, that is, when some visual properties were added outside a
	 * {@link SettingsPanel}.
	 * </p>
	 * 
	 * @param aDataComp Component in the settings dialog that carries the information required to update the
	 *        visual settings instance(s).
	 */
	protected void updateSettings(JComponent aDataComp) {
		// Default implementation is empty. See the Javadoc of the method for details.
	}

	/**
	 * General visual settings for the complex parameter.
	 */
	protected GeneralVisSettings general;
}
