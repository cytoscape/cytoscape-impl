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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.charts.JFreeChartConn;

/**
 * Dialog for saving a chart to a file.
 * <p>
 * This dialog prompts the user to specify chart dimensions and then a filename.
 * </p>
 * 
 * @author Yassen Assenov
 */
public class SaveChartDialog extends JDialog {

	private static final long serialVersionUID = -6256113953155955101L;
	
	/**
	 * Initializes a new instance of <code>SaveChartDialog</code>. The constructor creates and
	 * lays out all the controls of the dialog. It also positions the window according to its
	 * parent, so no subsequent calls to <code>pack</code> or <code>setLocation(...)</code> are
	 * necessary.
	 * 
	 * @param owner The <code>Dialog</code> from which this dialog is displayed.
	 * @param aChart Chart instance to be saved to a file.
	 */
	public SaveChartDialog(final Window owner, JFreeChart aChart) {
		super(owner, Messages.DT_SAVECHART);

		chart = aChart;

		initControls();
		pack();
		setModal(true);
		setResizable(false);
		setLocationRelativeTo(owner);
	}

	/**
	 * &qout;Save file&qout; dialog, reused by the instances of this class.
	 */
	private static final JFileChooser saveFileDialog = new JFileChooser();

	static {
		saveFileDialog.addChoosableFileFilter(SupportedExtensions.jpegFilesFilter);
		saveFileDialog.addChoosableFileFilter(SupportedExtensions.pngFilesFilter);
		saveFileDialog.addChoosableFileFilter(SupportedExtensions.svgFilesFilter);
	}

	/**
	 * Gets the user choice for height of the image.
	 * 
	 * @return Desired height, in pixels, of the image.
	 */
	private int getChosenHeight() {
		return Utils.getSpinnerInt(heightSpinner);
	}

	/**
	 * Gets the user choice for width of the image.
	 * 
	 * @return Desired width, in pixels, of the image.
	 */
	private int getChosenWidth() {
		return Utils.getSpinnerInt(widthSpinner);
	}

	/**
	 * Creates and lays out the controls inside this dialog's content pane.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	private void initControls() {
		final JPanel sizePanel = new JPanel();
		sizePanel.setBorder(LookAndFeelUtil.createTitledBorder(Messages.DI_IMAGESIZE));

		final JLabel widthLbl = new JLabel(Messages.DI_WIDTH);
		final JLabel heightLbl = new JLabel(Messages.DI_HEIGHT);
		final JLabel px1Lbl = new JLabel(Messages.DI_PIXELS);
		final JLabel px2Lbl = new JLabel(Messages.DI_PIXELS);
		
		// Add a spinner for choosing width
		int width = ChartPanel.DEFAULT_WIDTH;
		int minWidth = ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH;
		int maxWidth = ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH;
		SpinnerModel widthSettings = new SpinnerNumberModel(width, minWidth, maxWidth, 1);
		widthSpinner = new JSpinner(widthSettings);

		// Add a spinner for choosing height
		int height = ChartPanel.DEFAULT_HEIGHT;
		int minHeight = ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT;
		int maxHeight = ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT;
		SpinnerModel heightSettings = new SpinnerNumberModel(height, minHeight, maxHeight, 1);
		heightSpinner = new JSpinner(heightSettings);

		{
			final GroupLayout layout = new GroupLayout(sizePanel);
			sizePanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(widthLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(heightLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(widthSpinner, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(heightSpinner, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
							.addComponent(px1Lbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(px2Lbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
							.addComponent(widthLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(widthSpinner, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(px1Lbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
							.addComponent(heightLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(heightSpinner, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(px2Lbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
		}
		
		// Add Save and Cancel buttons
		btnSave = Utils.createButton(new AbstractAction(Messages.DI_SAVE) {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		}, null);
		btnCancel = Utils.createButton(new AbstractAction(Messages.DI_CANCEL) {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		}, null);
		
		Utils.equalizeSize(btnSave, btnCancel);
		final JPanel buttons = LookAndFeelUtil.createOkCancelPanel(btnSave, btnCancel);
		
		{
			final GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(sizePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(buttons, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(sizePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(buttons, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), btnSave.getAction(), btnCancel.getAction());
		getRootPane().setDefaultButton(btnSave);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	private void save() {
		// Save button pressed -> choose file name and save the chart
		int saveIt = saveFileDialog.showSaveDialog(this);
		if (saveIt == JFileChooser.APPROVE_OPTION) {

			// Choose file name
			File file = saveFileDialog.getSelectedFile();
			int width = getChosenWidth();
			int height = getChosenHeight();
			ExtensionFileFilter filter = null;
			try {
				filter = (ExtensionFileFilter) saveFileDialog.getFileFilter();
				if (!filter.hasExtension(file)) {
					file = filter.appendExtension(file);
				}
			} catch (ClassCastException ex) {
				// Try to infer the type of file by its extension
				FileFilter[] filters = saveFileDialog.getChoosableFileFilters();
				for (int i = 0; i < filters.length; ++i) {
					if (filters[i] instanceof ExtensionFileFilter) {
						filter = (ExtensionFileFilter) filters[i];
						if (filter.hasExtension(file)) {
							break;
						}
						filter = null;
					}
				}

				if (filter == null) {
					// Could not infer the type
					Utils.showErrorBox(this, Messages.DT_IOERROR, Messages.SM_AMBIGUOUSFTYPE);
					return;
				}
			}

			// Save the chart to the specified file name
			try {
				if (Utils.canSave(file, this)) {
					String ext = filter.getExtension();
					if (ext.equals("jpeg")) {
						JFreeChartConn.saveAsJpeg(file, chart, width, height);
					} else if (ext.equals("png")) {
						JFreeChartConn.saveAsPng(file, chart, width, height);
					} else { // ext.equals("svg")
						JFreeChartConn.saveAsSvg(file, chart, width, height);
					}
				}
			} catch (IOException ex) {
				Utils.showErrorBox(this, Messages.DT_IOERROR, Messages.SM_OERROR);
				return;
			}
			this.setVisible(false);
			this.dispose();
		} else if (saveIt == JFileChooser.ERROR_OPTION) {
			Utils.showErrorBox(this, Messages.DT_GUIERROR, Messages.SM_GUIERROR);
		}
	}

	/**
	 * Chart to be saved to a file.
	 */
	private JFreeChart chart;

	/**
	 * &quot;Save&quot; button.
	 */
	private JButton btnSave;

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;

	/**
	 * Spinner control for choosing height of the image.
	 */
	private JSpinner heightSpinner;

	/**
	 * Spinner control for choosing width of the image.
	 */
	private JSpinner widthSpinner;
}
