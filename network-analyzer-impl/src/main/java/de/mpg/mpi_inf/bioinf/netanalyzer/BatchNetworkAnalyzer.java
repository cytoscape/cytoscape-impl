package de.mpg.mpi_inf.bioinf.netanalyzer;

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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.AnalysisError;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Interpretations;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkAnalysisReport;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInspection;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStats;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStatus;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.StatsSerializer;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.BatchAnalysisDialog;

/**
 * Class for batch analysis of networks.
 * 
 * @author Yassen Assenov
 * @author Nadezhda Doncheva
 */
public class BatchNetworkAnalyzer extends SwingWorker {

	private final CyNetworkManager netMgr;
	private final CyNetworkReaderManager cyNetworkViewReaderMgr;
	
	/**
	 * Initializes a new instance of <code>BatchNetworkAnalyzer</code>.
	 * 
	 * @param aOutputDir
	 *            Output directory as chosen by the user.
	 * @param aInputFiles
	 *            List of all input files for the analysis.
	 * @param aInterpr
	 *            Parameter specifying which interpretations to be applied to each network.
	 */
	public BatchNetworkAnalyzer(File aOutputDir, List<File> aInputFiles, Interpretations aInterpr, CyNetworkManager netMgr, CyNetworkReaderManager cyNetworkViewReaderMgr) {
		analyzer = null;
		cancelled = false;
		dialog = null;
		progress = 0;
		outputDir = aOutputDir;
		inputFiles = aInputFiles;
		interpretations = aInterpr;
		reports = new ArrayList<NetworkAnalysisReport>();
		scale = 0.0;
		analyzing = false;
		subProgress = 0;
		this.netMgr = netMgr;
		this.cyNetworkViewReaderMgr = cyNetworkViewReaderMgr;
	}

	/**
	 * Cancels the process of network analysis.
	 * <p>
	 * Note that this method does not force the analyzer to cancel immediately; it takes an unspecified period of time
	 * until the analysis thread actually stops.
	 * </p>
	 */
	public void cancel() {
		cancelled = true;
		synchronized (this) {
			if (analyzer != null) {
				analyzer.cancel();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cytoscape.util.SwingWorker#construct()
	 */
	@Override
	public Object construct() {
		progress = 0;
		for (final File inputFile : inputFiles) {

			// Make a new network in cytoscape from a filename in
			// the network-directory
			CyNetwork network = null;
			try {
				write(Messages.SM_LOADING + inputFile.getName() + " ... ");
				if (!inputFile.isFile()) {
					throw new RuntimeException();
				}
				CyNetworkReader reader = cyNetworkViewReaderMgr.getReader(inputFile.toURI(), inputFile.getName());
				network = reader.getNetworks()[0];
			} catch (RuntimeException e) {
				writeLine(Messages.SM_READERROR);
				reports.add(new NetworkAnalysisReport(inputFile, null, AnalysisError.NETWORK_NOT_OPENED));
				progress += PROGRESS_PER_NET;
				continue;
			}

			// Get all possible interpretations for the network
			NetworkInspection inspection = null;
			try {
				inspection = CyNetworkUtils.inspectNetwork(network);
			} catch (IllegalArgumentException e) {
				writeLine(Messages.SM_DONE);
				reports.add(new NetworkAnalysisReport(inputFile, null, AnalysisError.NETWORK_EMPTY));
				unloadNetwork(inputFile, network);
				continue;
			} catch (NullPointerException e) {
				reports.add(new NetworkAnalysisReport(inputFile, null, AnalysisError.NETWORK_FILE_INVALID));
				progress += PROGRESS_PER_NET;
				continue;
			}

			final NetworkInterpretation[] interprs = filterInterpretations(getInterpretations(inspection));
			final int intCount = interprs.length;
			final int advance = PROGRESS_PER_NET / intCount;

			// Run NetworkAnalyzer on all accepted interpretations
			writeLine(Messages.SM_DONE);
			for (int j = 0; j < intCount; progress += advance, ++j) {
				if (cancelled) {
					writeLine(Messages.SM_ANALYSISC);
					return null;
				}

				// Run the analysis for an interpretation
				final NetworkInterpretation interpretation = interprs[j];
				try {
					if (interpretation.isDirected()) {
						analyzer = new DirNetworkAnalyzer(network, null, interpretation);
					} else {
						analyzer = new UndirNetworkAnalyzer(network, null, interpretation);
					}
					writeLine(Messages.DI_ANALYZINGINTERP1 + (j + 1) + Messages.DI_ANALYZINGINTERP2 + intCount);
					final int maxProgress = analyzer.getMaxProgress();
					scale = (double) advance / (double) maxProgress;
					analyzing = true;
					subProgress = 0;
					analyzer.computeAll();
					analyzing = false;
					if (cancelled) {
						writeLine(Messages.SM_ANALYSISC);
						return null;
					}
					final NetworkStats stats = analyzer.getStats();
					synchronized (this) {
						analyzer = null;
					}

					final String networkName = network.getRow(network).get("name",String.class);
					stats.setTitle(networkName + interpretation.getInterpretSuffix());
					final String extendedName = networkName + createID(interpretation);
					try {
						if (SettingsSerializer.getPluginSettings().getUseNodeAttributes()) {
							if (!saveNodeAttributes(network, interpretation.isDirected(), outputDir,
									extendedName)) {
								writeLine(Messages.SM_ATTRIBUTESNOTSAVED);
							}
						}
						File netstatFile = new File(outputDir, extendedName + ".netstats");
						StatsSerializer.save(stats, netstatFile);
						writeLine(Messages.SM_RESULTSSAVED);
						reports.add(new NetworkAnalysisReport(inputFile, interpretation, netstatFile));
					} catch (SecurityException ex) {
						writeError(Messages.SM_SAVEERROR);
						reports.add(new NetworkAnalysisReport(inputFile, interpretation,
								AnalysisError.OUTPUT_NOT_CREATED));
					} catch (FileNotFoundException ex) {
						writeError(Messages.SM_SAVEERROR);
						reports.add(new NetworkAnalysisReport(inputFile, interpretation,
								AnalysisError.OUTPUT_NOT_CREATED));
					} catch (IOException e) {
						writeError(Messages.SM_SAVEERROR);
						reports
								.add(new NetworkAnalysisReport(inputFile, interpretation, AnalysisError.OUTPUT_IO_ERROR));
					}

					if (cancelled) {
						writeLine(Messages.SM_ANALYSISC);
						return null;
					}
				} catch (Exception e) {
					reports.add(new NetworkAnalysisReport(inputFile, interpretation, AnalysisError.INTERNAL_ERROR));
				}
			}

			unloadNetwork(inputFile, network);
		}
		return null;
	}

	/**
	 * Filters the set of network interpretations based on the setting selected by the user.
	 * 
	 * @param interprs
	 *            All possible interpretations of the current network.
	 * @return Array of all acceptable interpretations of the current network.
	 * 
	 * @see #interpretations
	 */
	private NetworkInterpretation[] filterInterpretations(NetworkInterpretation[] interprs) {
		if (interpretations != Interpretations.ALL) {
			ArrayList<NetworkInterpretation> accepted = null;
			for (int i = 0; i < interprs.length; i++) {
				if ((interpretations == Interpretations.DIRECTED) != interprs[i].isDirected()) {
					if (accepted == null) {
						accepted = new ArrayList<NetworkInterpretation>(interprs.length - 1);
						for (int j = 0; j < i; j++) {
							accepted.add(interprs[j]);
						}
					}
				} else if (accepted != null) {
					accepted.add(interprs[i]);
				}
			}
			if (accepted != null) {
				NetworkInterpretation[] result = new NetworkInterpretation[accepted.size()];
				accepted.toArray(result);
				return result;
			}
		}
		return interprs;
	}

	/**
	 * Save node attributes computed by NetworkAnalyzer for this network into a tab-delimited file with extension
	 * "nattributes" (1st column corresponds to the node ids, each subsequent column contains the values of a node
	 * attribute).
	 * 
	 * @param aNetwork
	 *            Target network.
	 * @param aDir
	 *            Flag indicating if the network interpretation is directed.
	 * @param aOutputDir
	 *            Output directory for writing files as chosen by the user.
	 * @param aExtendedName
	 *            Name of the analyzed network including the current interpretation.
	 * @return <code>true</code> if any node attributes where present and have been saved, and <code>false</code>
	 *         otherwise.
	 */
	private boolean saveNodeAttributes(CyNetwork aNetwork, boolean aDir, File aOutputDir,
			String aExtendedName) {
		// get node attributes computed in the last analysis run
		Set<String> netAnayzerAttr = new HashSet<String>();
		if (aDir) {
			netAnayzerAttr = Messages.getDirNodeAttributes();
		} else {
			netAnayzerAttr = Messages.getUndirNodeAttributes();
		}
		if (netAnayzerAttr.size() == 0) {
			return false;

		}
		// save chosen node attributes in a file, 1st column corresponds to the node ids, each subsequent column
		// contains the values of a node attribute
		try {
			final FileWriter writer = new FileWriter(new File(outputDir, aExtendedName + ".nattributes"));
			writer.write("Node ID");
			for (final String attr : netAnayzerAttr) {
				writer.write("\t" + attr);
			}
			writer.write("\n");
			for ( CyNode n : aNetwork.getNodeList()) {
				final String id = aNetwork.getRow(n).get("name", String.class);
				writer.write(id);
				for (final String attr : netAnayzerAttr) {
					final Object attrValue = aNetwork.getRow(n).getRaw(attr);
					if (attrValue != null) {
						writer.write("\t" + attrValue.toString());
					}
				}
				writer.write("\n");
			}
			writer.close();
		} catch (IOException ex) {
			// attributes file could not be written
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cytoscape.util.SwingWorker#finished()
	 */
	@Override
	public void finished() {
		progress++;
		if (dialog != null) {
			dialog.analysisFinished();
		}
		interrupt();
	}


	/**
	 * Gets the current progress of the tester as a number of steps.
	 * 
	 * @return Number of steps completed in the analysis process.
	 */
	public int getCurrentProgress() {
		if (analyzing) {
			subProgress = +analyzer.getCurrentProgress();
			return (progress + (int) (subProgress * scale));
		}
		return progress;
	}

	/**
	 * Gets the maximum progress of the tester as a number of steps.
	 * 
	 * @return Total number of steps required for the tester to finish.
	 */
	public int getMaxProgress() {
		return inputFiles.size() * PROGRESS_PER_NET + 1;
	}

	/**
	 * Gets the number of input files, i.e. the number of networks to be analyzed.
	 * 
	 * @return Number of input files, i.e. networks to be analyzed.
	 */
	public int getInputFilesCount() {
		return inputFiles.size();
	}

	/**
	 * Gets the list with reports after the analysis is finished.
	 * 
	 * @return List of reports describing the success or failure of the analysis of each network.
	 */
	public List<NetworkAnalysisReport> getReports() {
		return reports;
	}

	/**
	 * Sets the corresponding batch analysis dialog.
	 * 
	 * @param aDialog
	 *            Dialog of the batch analysis.
	 */
	public void setDialog(BatchAnalysisDialog aDialog) {
		dialog = aDialog;
	}

	/**
	 * Creates an integer identifier of the network interpretation, where 1 means directed network and 0 - undirected.
	 * 
	 * @param aInterp
	 *            Network Interpretation.
	 * @return A String of the integer identifier of the network interpretation.
	 */
	private static String createID(NetworkInterpretation aInterp) {
		String newName = "";
		final boolean flag1 = aInterp.isDirected();
		final boolean flag2 = aInterp.isIgnoreUSL();
		final boolean flag3 = aInterp.isPaired();
		if (flag1) {
			newName += "-d";
			if (flag2) {
				newName += "-isl";
			}
		} else {
			newName += "-u";
			if (flag3) {
				newName += "-cpe";
			}
		}
		return newName;
	}

	/**
	 * Gets the network interpretations from the network inspection.
	 * 
	 * @param inspection
	 *            Network inspection.
	 * @return Array with different network interpretations.
	 */
	private static NetworkInterpretation[] getInterpretations(NetworkInspection inspection) {
		return NetworkStatus.getStatus(inspection).getInterpretations();
	}

	/**
	 * Fixed integer estimating the analysis time of a single network.
	 */
	private static final int PROGRESS_PER_NET = 12;

	/**
	 * Unloads the network from Cytoscape and writes a message in the batch analysis dialog.
	 * 
	 * @param inputFile
	 *            File from which the network was loaded.
	 * @param network
	 *            Network to be unloaded.
	 */
	private void unloadNetwork(final File inputFile, CyNetwork network) {
		// Unload the network
		write(Messages.SM_UNLOADING + inputFile.getName() + " ... ");
		try {
			netMgr.destroyNetwork(network);
		} catch (Exception ex) {
			// Network already removed (by another plugin); ignore
		}
		writeLine(Messages.SM_DONE + "\n");
	}

	/**
	 * Writes a message to the user in the batch analysis dialog.
	 * 
	 * @param aMessage
	 *            Message to be written (showed) to the user.
	 */
	private void write(String aMessage) {
		if (dialog != null) {
			dialog.write(aMessage);
		}
	}

	/**
	 * Writes a message, followed by a new line, to the user in the batch analysis dialog.
	 * 
	 * @param aMessage
	 *            Message to be written to the user.
	 */
	private void writeLine(String aMessage) {
		write(aMessage + "\n");
	}

	/**
	 * Writes an error message, followed by a new line, to the user in the batch analysis dialog.
	 * 
	 * @param aMessage
	 *            An error message to be written to the user.
	 */
	private void writeError(String aMessage) {
		if (dialog != null) {
			dialog.write(aMessage + "\n");
		}
	}

	/**
	 * Instance of <code>UndirNetowkrAnalyzer</code> or <code>DirNetowkrAnalyzer</code> responsible for the network
	 * parameters computation.
	 */
	private NetworkAnalyzer analyzer;

	/**
	 * Flag indicating if the analysis has been canceled.
	 */
	private boolean cancelled;

	/**
	 * Dialog showing the progress of the batch analysis.
	 */
	private BatchAnalysisDialog dialog;

	/**
	 * Progress of the batch analysis.
	 */
	private int progress;

	/**
	 * Array with the names of the input (inOutDir[0]) and output directory (inOutDir[1]).
	 */
	private File outputDir;

	/**
	 * List of input files, that can be loaded in Cytoscape and analyzed by NetworkAnalyzer.
	 */
	private List<File> inputFiles;

	/**
	 * Interpretations to applied for every loaded network.
	 */
	private Interpretations interpretations;

	/**
	 * List of reports describing the success or failure of the analysis of each network.
	 */
	private List<NetworkAnalysisReport> reports;

	/**
	 * Scaling factor needed for showing the analysis progress.
	 */
	private double scale;

	/**
	 * Flag indicating if parameters are computed by <code>NetworkAnalyzer</code>.
	 */
	private boolean analyzing;

	/**
	 * Progress of <code>NetworkAnalyzer</code> analysis for a single network interpretation.
	 */
	private double subProgress;

}
