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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.mpi_inf.bioinf.netanalyzer.InnerException;
import de.mpg.mpi_inf.bioinf.netanalyzer.Plugin;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.ComplexParamFilter;
import de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.charts.JFreeChartConn;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.filter.ComplexParamFilterDialog;

/**
 * Panel used to display chart.
 * <p>
 * In addition to a chart control (<code>ChartPanel</code>), this panel also contains buttons for
 * enlarged view, saving and viewing/editing visual settings, and others.
 * </p>
 * 
 * @author Yassen Assenov
 */
public class ChartDisplayPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1662216221117859713L;
	private static final Logger logger = LoggerFactory.getLogger(ChartDisplayPanel.class);
	
	private static Dimension preferred;

	static {
		final JButton b = new JButton("");
		preferred = b.getPreferredSize();
		final String[] texts = new String[] { Messages.DI_VIEWENLARGED, Messages.DI_FILTERDATA,
				Messages.DI_EXPORTCHART, Messages.DI_EXPORTDATA, Messages.DI_CHARTSETTINGS,
				Messages.DI_HELP, Messages.DI_FITLINE, Messages.DI_REMOVELINE, Messages.DI_FITPL, Messages.DI_REMOVEPL };
		for (final String text : texts) {
			b.setText(text);
			Utils.ensureSize(preferred, b.getPreferredSize());
		}
	}

	/** Decorators registered for the complex parameter instance visualized. */
	private Decorator[] decorators;
	
	/** Original (unfiltered) complex parameter displayed in this panel. */
	private ComplexParam originalParam;
	
	/** Chart to be displayed. */
	private JFreeChart chart;

	/** Swing control that displays the chart. */
	private JPanel chartPanel;

	/** &quot;View Enlarged&quot; button. */
	private JButton btnEnlarged;

	/** &quot;Save Chart&quot; button. */
	private JButton btnSaveChart;

	/** &quot;Save Data&quot; button. */
	private JButton btnSaveData;

	/** &quot;Filter Data&quot; / &quot;Remove Filter&quot; button. */
	private JButton btnFilter;

	/** Buttons for the decorators. */
	private JButton[] btnsDec;

	/** ID of the visualized complex parameter. */
	private String id;
	
	/** Visualizer instance for the displayed parameter chart. Used for viewing/editing of visual settings. */
	protected ComplexParamVisualizer visualizer;

	/** Owner dialog of this panel. */
	protected final Window owner;

	/** &quot;Chart Settings&quot; button. */
	protected JButton btnChartSettings;


	/**
	 * Initializes a new instance of <code>ChartDisplayPanel</code>.
	 * 
	 * @param owner
	 *            Owner dialog.
	 * @param aID
	 *            ID of complex parameter to be displayed.
	 * @param aVisualizer
	 *            Visualizer of the complex parameter to be displayed.
	 */
	public ChartDisplayPanel(Window owner, String aID, ComplexParamVisualizer aVisualizer) {
		this(owner, aID, aVisualizer, null);
	}

	/**
	 * Initializes a new instance of <code>ChartDisplayPanel</code>.
	 * 
	 * @param owner
	 *            Owner dialog.
	 * @param aID
	 *            ID of complex parameter to be displayed.
	 * @param aVisualizer
	 *            Visualizer of the complex parameter to be displayed.
	 * @param aDecorators
	 *            Decorator instances for the complex parameter visualized.
	 */
	public ChartDisplayPanel(Window owner, String aID, ComplexParamVisualizer aVisualizer,
			Decorator[] aDecorators) {
		super();
		visualizer = aVisualizer;
		id = aID;
		decorators = aDecorators;
		originalParam = aVisualizer.getComplexParam();
		chart = aVisualizer.createControl();
		this.owner = owner;

		initControls();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		try {
			if (source == btnEnlarged) {
				displayEnlarged();
			} else if (source == btnSaveChart) {
				SaveChartDialog dialog = new SaveChartDialog(owner, chart);
				dialog.setVisible(true);
			} else if (source == btnSaveData) {
				saveChartData();
			} else if (source == btnFilter) {
				performFilter();
			} else if (source == btnChartSettings) {
				if (changeVisSettings()) {
					repaint();
				}
			} else if (btnsDec != null) {
				for (int i = 0; i < btnsDec.length; ++i) {
					if (source == btnsDec[i]) {
						Decorator dec = decorators[i];
						if (dec.isActive()) {
							dec.undecorate(chart);
						} else {
							dec.decorate(owner, chart, visualizer, true);
						}
						btnsDec[i].setText(dec.getButtonLabel());
						btnsDec[i].setToolTipText(dec.getButtonToolTip());
						break;
					}
				}
			}
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}

	/**
	 * Displays a dialog for changing the visual settings and aplies the changes if necessary.
	 * 
	 * @return <code>true</code> if the user has pressed one of &quot;Set Default&quot; or
	 *         &quot;OK&quot; buttons and changes were applied; <code>false</code> otherwise.
	 */
	protected boolean changeVisSettings() {
		// Display dialog to view/edit the settings
		if (visualizer.showSettingsDialog(owner) != SettingsDialog.STATUS_CANCEL) {
			// User has pressed SET DEFAULT or OK
			final JFreeChart newChart = visualizer.updateControl(chart);
			if (decorators != null) {
				for (int i = 0; i < decorators.length; ++i) {
					if (decorators[i].isActive()) {
						decorators[i].decorate(owner, newChart, visualizer, false);
					}
				}
			}
			JFreeChartConn.setChart(getChartPanel(), chart = newChart);
			return true;
		}
		return false;
	}

	/**
	 * Creates and lays out the controls inside this panel.
	 * <p> This method is called upon initialization only.</p>
	 */
	private void initControls() {
		btnChartSettings = Utils.createButton(Messages.DI_CHARTSETTINGS, Messages.TT_CHARTSETTINGS, this);
		btnEnlarged = Utils.createButton(Messages.DI_VIEWENLARGED, Messages.TT_VIEWENLARGED, this);
		btnSaveChart = Utils.createButton(Messages.DI_EXPORTCHART, Messages.TT_SAVECHART, this);
		btnSaveData = Utils.createButton(Messages.DI_EXPORTDATA, Messages.TT_SAVEDATA, this);
		
		if (Plugin.hasFilter(originalParam.getClass())) {
			btnFilter = Utils.createButton(Messages.DI_FILTERDATA, Messages.TT_FILTERDATA, this);
		} else {
			btnFilter = null;
		}
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		final ParallelGroup hBtnGroup;
		final SequentialGroup vBtnGroup;
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getChartPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(hBtnGroup = layout.createParallelGroup(Alignment.CENTER, true))
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getChartPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(vBtnGroup = layout.createSequentialGroup())
		);

		final List<JButton> buttons = new ArrayList<JButton>(8);
		buttons.add(btnChartSettings);
		buttons.add(btnEnlarged);
		if (btnFilter != null) buttons.add(btnFilter);
		buttons.add(btnSaveChart);
		buttons.add(btnSaveData);
		
		// Add buttons to panel
		hBtnGroup.addComponent(btnChartSettings, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
		vBtnGroup.addComponent(btnChartSettings, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		
		hBtnGroup.addComponent(btnEnlarged, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
		vBtnGroup.addComponent(btnEnlarged, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		
		if (btnFilter != null) {
			hBtnGroup.addComponent(btnFilter, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
			vBtnGroup.addComponent(btnFilter, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		}
		
		vBtnGroup.addGap(10, 20, Short.MAX_VALUE);
		
		if (decorators != null) {
			btnsDec = new JButton[decorators.length];
			
			for (int i = 0; i < decorators.length; ++i) {
				Decorator d = decorators[i];
				btnsDec[i] = Utils.createButton(d.getButtonLabel(), d.getButtonToolTip(), this);
				buttons.add(btnsDec[i]);
				
				hBtnGroup.addComponent(btnsDec[i], DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vBtnGroup.addComponent(btnsDec[i], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			}
		}
		
		vBtnGroup.addGap(10, 20, Short.MAX_VALUE);
		
		hBtnGroup.addComponent(btnSaveChart, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
		vBtnGroup.addComponent(btnSaveChart, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		
		hBtnGroup.addComponent(btnSaveData, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
		vBtnGroup.addComponent(btnSaveData, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		
		// Ensure buttons are large enough to fit all possible messages
		final JButton[] btnsArray = new JButton[buttons.size()];
		Utils.setSizes(buttons.toArray(btnsArray), preferred, preferred);
	}

	private JPanel getChartPanel() {
		if (chartPanel == null) {
			chartPanel = JFreeChartConn.createPanel(chart);
			chartPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
			
			final Dimension size = chartPanel.getPreferredSize();
			size.setSize(size.getWidth() / 3 * 2, size.getHeight() / 3 * 2);
			chartPanel.setPreferredSize(size);
		}
		
		return chartPanel;
	}

	/**
	 * Creates a dialog window which contains the chart in its preferred size.
	 */
	@SuppressWarnings("serial")
	private void displayEnlarged() {
		final JDialog dialog = new JDialog(owner, visualizer.getTitle());
		dialog.getContentPane().add(JFreeChartConn.createPanel(chart));
		dialog.pack();
		dialog.setModalityType(ModalityType.MODELESS);
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(
				dialog.getRootPane(),
				null,
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();
					}
				});
	}

	/**
	 * Handles the pressing of the {@link #btnFilter} button.
	 * <p>
	 * After asking the user for confirmation, this method adds or removes filter to the complex
	 * parameter displayed.
	 * </p>
	 */
	private void performFilter() {
		if (originalParam == visualizer.getComplexParam()) {
			// Create filter
			try {
				final Class<?> paramClass = originalParam.getClass();
				final Class<?> filterClass = Plugin.getFilterDialogClass(paramClass);
				final Constructor<?> constr = filterClass.getConstructors()[0];
				
				Object[] cParams = new Object[] { owner, Messages.DT_FILTERDATA,
						originalParam, visualizer.getSettings() };
				ComplexParamFilterDialog d = (ComplexParamFilterDialog) constr.newInstance(cParams);
				d.setMinimumSize(new Dimension(260, d.getMinimumSize().height));
				ComplexParamFilter filter = d.showDialog();
				
				if (filter != null) {
					btnFilter.setText(Messages.DI_REMOVEFILTER);
					btnFilter.setToolTipText(Messages.TT_REMOVEFILTER);
					visualizer.setComplexParam(filter.filter(originalParam));
					chart = visualizer.createControl();
					JFreeChartConn.setChart(getChartPanel(), chart);
				}
			} catch (InnerException ex) {
				// NetworkAnalyzer internal error
				logger.error(Messages.SM_LOGERROR, ex);
			} catch (SecurityException ex) {
				Utils.showErrorBox(this, Messages.DT_SECERROR, Messages.SM_SECERROR2);
			} catch (Exception ex) {
				// ClassCastException, ClassNotFoundException, IllegalAccessException
				// IllegalArgumentException, InstantiationException, InvocationTargetException
				// NetworkAnalyzer internal error
				logger.error(Messages.SM_LOGERROR, ex);
			}
		} else {
			// Remove filter
			int res = JOptionPane.showConfirmDialog(this, Messages.SM_REMOVEFILTER,
					Messages.DT_REMOVEFILTER, JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.YES_OPTION) {
				btnFilter.setText(Messages.DI_FILTERDATA);
				btnFilter.setToolTipText(Messages.TT_FILTERDATA);
				visualizer.setComplexParam(originalParam);
				chart = visualizer.createControl();
				JFreeChartConn.setChart(getChartPanel(), chart);
			}
		}
	}

	/**
	 * Displays a dialog for saving the complex parameter data into a text file.
	 * <p>
	 * If the user presses the &quot;Save&quot; button of the shown dialog, the complex parameter is
	 * saved to the chosen file.
	 * </p>
	 */
	private void saveChartData() {
		JFileChooser saveFileDialog = new JFileChooser();
		int saveIt = saveFileDialog.showSaveDialog(this);
		if (saveIt == JFileChooser.APPROVE_OPTION) {
			FileWriter writer = null;
			try {
				File file = saveFileDialog.getSelectedFile();
				if (Utils.canSave(file, this)) {
					writer = new FileWriter(file);
					visualizer.getComplexParam().save(writer, false);
				}
			} catch (IOException ex) {
				Utils.showErrorBox(this, Messages.DT_IOERROR, Messages.SM_OERROR);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException ex) {
						// Fall through
					}
				}
			}
		}
	}
}
