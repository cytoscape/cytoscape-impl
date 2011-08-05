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

package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.LongHistogramGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.SettingsGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.charts.JFreeChartConn;

/**
 * Handles the visualization of the {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.IntHistogram} complex
 * parameter type instances.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public class LongHistogramVisualizer extends ComplexParamVisualizer {

	/**
	 * Initializes a new instance of <code>LongHistogramVisualizer</code>.
	 * 
	 * @param aParam Instance of the integer histogram complex parameter type.
	 * @param aSettings Visual settings for the complex parameter.
	 */
	public LongHistogramVisualizer(LongHistogram aParam, LongHistogramGroup aSettings) {
		param = aParam;
		settings = (LongHistogramGroup) aSettings.clone();
		general = settings.general;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.ui.ComplexParamVisualizer#getComplexParam()
	 */
	@Override
	public ComplexParam getComplexParam() {
		return param;
	}

	/**
	 * Gets the settings for the visualized <code>LongHistogram</code>.
	 * 
	 * @return The <code>LongHistogramGroup</code> instance for the visualized complex parameter.
	 */
	@Override
	public SettingsGroup getSettings() {
		return settings;
	}

	/**
	 * Sets the complex parameter instance managed by this visualizer.
	 * <p>
	 * <b>Note:</b> It is the responsibility of the caller to recreate all the controls created by this
	 * visualizer by calling the {@link #createControl()} method for each of them.
	 * </p>
	 * 
	 * @param aParam Complex parameter instance to be managed by this visualizer.
	 * @throws ClassCastException If the specified complex parameter is not of type
	 *         {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram}.
	 * @throws NullPointerException If <code>aParam</code> is <code>null</code>.
	 */
	@Override
	public void setComplexParam(ComplexParam aParam) {
		if (aParam == null) {
			throw new NullPointerException();
		}
		param = (LongHistogram) aParam;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.ui.ComplexParamVisualizer#createControl()
	 */
	@Override
	public JFreeChart createControl() {
		return JFreeChartConn.createChart(param, settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.ui.ComplexParamVisualizer#updateControl(org.jfree.chart.JFreeChart)
	 */
	@Override
	public JFreeChart updateControl(JFreeChart aControl) {
		XYPlot plot = (XYPlot) aControl.getPlot();
		if (plot.getRenderer() instanceof XYBarRenderer) {
			// Current control is Histogram
			if (settings.useScatter()) {
				return JFreeChartConn.createScatter(param, settings);
			}
			JFreeChartConn.updateGeneral(aControl, general);
			JFreeChartConn.updateAxes(aControl, settings.axes, settings.grid);
			JFreeChartConn.updateBars(aControl, settings.bars);
			return aControl;
		}

		// else: plot.getRenderer() instanceof StandardXYItemRenderer
		// Current control is Scatter Plot
		if (settings.useScatter()) {
			JFreeChartConn.updateGeneral(aControl, general);
			JFreeChartConn.updateAxes(aControl, settings.axes, settings.grid);
			JFreeChartConn.updateScatter(aControl, settings.scatter);
			return aControl;
		}
		return JFreeChartConn.createHistogram(param, settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.ui.ComplexParamVisualizer#addSettingsPanels(javax.swing.JTabbedPane)
	 */
	@Override
	protected JComponent addSettingsPanels(JTabbedPane aPanel) {

		addTab(aPanel, Messages.DI_GENERAL, new SettingsPanel(general), Messages.TT_GENSETTINGS);
		addTab(aPanel, Messages.DI_AXES, new SettingsPanel(settings.axes), Messages.TT_AXESSETTINGS);
		addTab(aPanel, Messages.DI_GRID, new SettingsPanel(settings.grid), Messages.TT_GRIDSETTINGS);

		boolean useScatter = settings.useScatter();
		final Box histPanel = Box.createVerticalBox();

		final JComboBox choiceCombo = addChoice(histPanel, useScatter ? 1 : 0);

		final CardLayout innerLayout = new CardLayout();
		final JPanel innerPanel = new JPanel(innerLayout);
		histPanel.add(innerPanel);
		aPanel.addTab(Messages.DI_HISTOGRAM, null, histPanel, Messages.TT_HISTSETTINGS);

		ItemListener listener = new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					innerLayout.next(innerPanel);
				}
			}
		};
		choiceCombo.addItemListener(listener);

		innerPanel.add(new SettingsPanel(settings.bars), "0");
		innerPanel.add(new SettingsPanel(settings.scatter), "1");
		if (useScatter) {
			innerLayout.next(innerPanel);
		}

		return choiceCombo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.ui.ComplexParamVisualizer#saveDefault()
	 */
	@Override
	protected void saveDefault() throws IOException {
		SettingsSerializer.setDefault(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.ui.ComplexParamVisualizer#updateSettings(javax.swing.JComponent)
	 */
	@Override
	protected void updateSettings(JComponent aDataComp) {
		settings.useScatter.setValue(((JComboBox) aDataComp).getSelectedIndex() == 1);
	}

	/**
	 * Creates the combo box (drop-down list) to choose between visualization of the histogram as bar chart or
	 * as a scatter plot.
	 * 
	 * @param aContainer Container control, to which the combo box is to be added.
	 * @param aSelectedIndex Which choice is to be initially selected. This parameter must have one of the
	 *        values <code>{1; 2}</code>.
	 * @return The newly created combo box control.
	 */
	private static JComboBox addChoice(Box aContainer, int aSelectedIndex) {
		final String[] choices = new String[] { Messages.DI_SHOWHIST, Messages.DI_SHOWSCAT };
		JComboBox choiceCombo = new JComboBox(choices);
		choiceCombo.setSelectedIndex(aSelectedIndex);
		choiceCombo.setEditable(false);
		JPanel choicePanel = new JPanel();
		choicePanel.add(choiceCombo);
		aContainer.add(choicePanel);
		return choiceCombo;
	}

	/**
	 * Integer histogram instance to be visualized.
	 */
	private LongHistogram param;

	/**
	 * Visual settings for the long histogram instance.
	 */
	private LongHistogramGroup settings;
}
