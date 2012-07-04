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

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

import de.mpg.mpi_inf.bioinf.netanalyzer.CyNetworkUtils;
import de.mpg.mpi_inf.bioinf.netanalyzer.InnerException;
import de.mpg.mpi_inf.bioinf.netanalyzer.NetworkAnalyzer;
import de.mpg.mpi_inf.bioinf.netanalyzer.Plugin;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Decorators;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStats;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.StatsSerializer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.PluginSettings;
import de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator;

/**
 * Dialog presenting results of network analysis.
 * 
 * @author Yassen Assenov
 */
public class AnalysisDialog extends JDialog implements ActionListener, WindowListener {

	private final CyNetworkViewManager viewManager;
	private final VisualMappingManager vmm;
	private final VisualStyleBuilder vsBuilder;
	
	/**
	 * Dialog window for choosing a filename when saving and loading .netstats files.
	 */
	public static JFileChooser netstatsDialog = new JFileChooser();

	static {
		netstatsDialog.addChoosableFileFilter(SupportedExtensions.netStatsFilter);
	}

	private final Frame aOwner;
	
	/**
	 * Initializes a new instance of <code>AnalysisDialog</code>.
	 * <p>
	 * The dialog created is non-modal and has a title &quot;Network Analysis - [name]&quot;, where [name] is
	 * the name of the network, as saved in the <code>aStats</code> parameter. The constructor creates and
	 * lays out all the controls of the dialog. It also positions the window according to its parent, so no
	 * subsequent calls to <code>pack</code> or <code>setLocation(...)</code> are necessary.
	 * </p>
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aStats
	 *            Network statistics to be visualized.
	 * @param aAnalyzer
	 *            Analyzer class that performed the topological analysis. Set this to <code>null</code> if the
	 *            results were loaded from a file rather than just computed.
	 */
	public AnalysisDialog(Frame aOwner, NetworkStats aStats, NetworkAnalyzer aAnalyzer, final CyNetworkViewManager viewManager, final VisualStyleBuilder vsBuilder,
			final VisualMappingManager vmm) {
		super(aOwner, Messages.DT_ANALYSIS + aStats.getTitle(), false);
		this.aOwner = aOwner;
		this.viewManager = viewManager;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;

		stats = aStats;
		boolean paramMapping = false;
		if (aAnalyzer != null) {
			final PluginSettings s = SettingsSerializer.getPluginSettings();
			paramMapping = aAnalyzer.isGlobal() && (s.getUseNodeAttributes() || s.getUseEdgeAttributes());
			saved = false;
		} else {
			saved = true;
		}
		initControls(paramMapping);
		addWindowListener(this);

		pack();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(aOwner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if (src == saveButton) {
			saveNetstats();
		} else if (src == visualizeButton) {
			visualizeParameter();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
		// Event is not processed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent e) {
		// Event is not processed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
		// Event is not processed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		if (!saved) {
			int choice = JOptionPane.showConfirmDialog(this, Messages.SM_CLOSEWARN, Messages.DT_CLOSEWARN,
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.NO_OPTION) {
				return;
			}
		}
		setVisible(false);
		dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) {
		// Event is not processed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {
		// Event is not processed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent e) {
		// Event is not processed
	}

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	private void initControls(boolean enableParameterMapping) {
		final boolean useExpandable = SettingsSerializer.getPluginSettings().getExpandable();
		final Container contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		final JTabbedPane tabs = useExpandable ? null : new JTabbedPane();

		final JComponent simpleStatsPanel = new SimpleStatsPanel(stats);
		if (useExpandable) {
			simpleStatsPanel.setBorder(BorderFactory.createTitledBorder(Messages.DI_SIMPLEPARAMS));
			contentPane.add(simpleStatsPanel);
		} else {
			contentPane.add(tabs);
			tabs.addTab(Messages.DI_SIMPLEPARAMS, simpleStatsPanel);
		}

		final String[] complexNames = stats.getComputedComplex();
		for (int i = 0; i < complexNames.length; ++i) {
			final String id = complexNames[i];
			final ComplexParam cp = stats.getComplex(id);
			final String typeName = cp.getClass().getSimpleName();
			try {
				final Class<?> visClass = Plugin.getVisualizerClass(typeName);
				final Constructor<?> con = visClass.getConstructors()[0];
				final Object[] conParams = new Object[] { cp, SettingsSerializer.getDefault(id) };
				ComplexParamVisualizer v = (ComplexParamVisualizer) con.newInstance(conParams);
				final Decorator[] decs = Decorators.get(id);
				if (useExpandable) {
					contentPane.add(new ChartExpandablePanel(this, id, v, (i == 0), decs));
				} else {
					tabs.addTab(v.getTitle(), new ChartDisplayPanel(this, id, v, decs));
				}
			} catch (Exception ex) {
				throw new InnerException(ex);
			}
		}

		contentPane.add(Box.createVerticalStrut(Utils.BORDER_SIZE));
		saveButton = new JButton(Messages.DI_SAVESTATISTICS);
		saveButton.addActionListener(this);
		visualizeButton = new JButton(Messages.DI_VISUALIZEPARAMETER);
		visualizeButton.setEnabled(false);
		visualizeButton.addActionListener(this);
		visualizeButton.setEnabled(enableParameterMapping);
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, Utils.BORDER_SIZE, 0));
		Utils.equalizeSize(saveButton, visualizeButton);
		buttonPane.add(saveButton);
		buttonPane.add(Box.createHorizontalStrut(Utils.BORDER_SIZE * 2));
		buttonPane.add(visualizeButton);
		contentPane.add(buttonPane);
		contentPane.add(Box.createVerticalStrut(Utils.BORDER_SIZE));
	}

	/**
	 * Displays a dialog prompting to save the network analysis in a <code>.netstats</code> file.
	 */
	private void saveNetstats() {
		int saveIt = netstatsDialog.showSaveDialog(this);
		if (saveIt == JFileChooser.APPROVE_OPTION) {
			try {
				String fileName = netstatsDialog.getSelectedFile().getAbsolutePath();
				Utils.removeSelectedFile(netstatsDialog);
				if (!SupportedExtensions.netStatsFilter.hasExtension(fileName)) {
					fileName = SupportedExtensions.netStatsFilter.appendExtension(fileName);
				}
				if (Utils.canSave(new File(fileName), this)) {
					StatsSerializer.save(stats, fileName);
					saved = true;
				}
			} catch (IOException ex) {
				// Could not save file
				Utils.showErrorBox(this, Messages.DT_IOERROR, Messages.SM_OERROR);
			} catch (SecurityException ex) {
				// Could not save file - security manager has denied access
				Utils.showErrorBox(this, Messages.DT_SECERROR, Messages.SM_SECERROR2);
			}
		} else if (saveIt == JFileChooser.ERROR_OPTION) {
			Utils.showErrorBox(this, Messages.DT_GUIERROR, Messages.SM_GUIERROR);
		}
	}

	/**
	 * Opens the &quot;Map Parameters to Visual Styles&quot; dialog when the &quot;Map Parameters to Visual
	 * Styles&quot; button is activated.
	 */
	private void visualizeParameter() {
		CyNetwork network = stats.getNetwork();
		if (network != null) {
			final String[][] nodeAttr = CyNetworkUtils.getComputedNodeAttributes(network);
			final String[][] edgeAttr = CyNetworkUtils.getComputedEdgeAttributes(network);
			if ((nodeAttr[0].length > 0) || (nodeAttr[1].length > 0) || (edgeAttr[0].length > 0)
					|| (edgeAttr[1].length > 0)) {
				final MapParameterDialog d = new MapParameterDialog(aOwner, network, viewManager, vsBuilder, vmm,
						nodeAttr, edgeAttr);
				d.setVisible(true);
				return;
			}
		}
		// Could not locate network - display an error message to the user
		Utils.showErrorBox(this, Messages.DT_WRONGDATA, Messages.SM_VISUALIZEERROR);
		visualizeButton.setEnabled(false);
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 5759174742857183439L;

	/**
	 * Flag indicating if the network analysis results were successfully saved to a file.
	 */
	private boolean saved;

	/**
	 * &quot;Save Statistics&quot; button.
	 */
	private JButton saveButton;

	/**
	 * &quot;Visualize Parameters&quot; button.
	 */
	private JButton visualizeButton;

	/**
	 * Network parameters instance displayed in this dialog.
	 */
	private NetworkStats stats;

}
