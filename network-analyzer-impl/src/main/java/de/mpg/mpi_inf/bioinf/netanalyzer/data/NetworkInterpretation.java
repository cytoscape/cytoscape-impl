package de.mpg.mpi_inf.bioinf.netanalyzer.data;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany.
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

import javax.swing.ImageIcon;

/**
 * Storage class for information on how to interpret a network.
 * 
 * @author Yassen Assenov
 */
public class NetworkInterpretation {

	/**
	 * Initializes a new instance of <code>NetworkInterpretation</code>.
	 * 
	 * @param aIcon
	 *            Image showing the state of the network resulting from the interpretation.
	 * @param aMessage
	 *            Message describing the network interpretation.
	 * @param aDirected
	 *            Flag indicating whether the network should be treated as directed.
	 */
	public NetworkInterpretation(ImageIcon aIcon, String aMessage, boolean aDirected) {
		directed = aDirected;
		ignoreUSL = false;
		paired = false;
		icon = aIcon;
		message = "<html>" + aMessage;
	}

	/**
	 * Initializes a new instance of <code>NetworkInterpretation</code>.
	 * 
	 * @param aIcon
	 *            Image showing the state of the network resulting from the interpretation.
	 * @param aDirected
	 *            Flag indicating whether the network should be treated as directed.
	 */
	public NetworkInterpretation(ImageIcon aIcon, boolean aDirected) {
		icon = aIcon;
		if (aDirected) {
			message = "<html>" + Messages.NI_TD;
		} else {
			message = "<html>" + Messages.NI_TU;
		}
		directed = aDirected;
		ignoreUSL = false;
		paired = false;
	}

	/**
	 * Initializes a new instance of <code>NetworkInterpretation</code>.
	 * 
	 * @param aIcon
	 *            Image showing the state of the network resulting from the interpretation.
	 * @param aDirected
	 *            Flag indicating whether the network should be treated as directed.
	 * @param aAdditional
	 *            Flag giving additional information to this interpretation. If <code>aDirected</code> is
	 *            <code>true</code>, this flag indicates whether undirected self-loops exist (and must be
	 *            ignored). If <code>aDirected</code> is <code>false</code>, this flag indicates whether
	 *            directed edges must be paired.
	 */
	public NetworkInterpretation(ImageIcon aIcon, boolean aDirected, boolean aAdditional) {
		icon = aIcon;
		directed = aDirected;
		if (aDirected) {
			ignoreUSL = aAdditional;
			paired = false;
			if (aAdditional) {
				message = "<html>" + Messages.NI_IGNOREUSL + "<br>" + Messages.NI_TD;
			} else {
				message = "<html>" + Messages.NI_TD;
			}
		} else {
			ignoreUSL = false;
			paired = aAdditional;
			if (aAdditional) {
				message = "<html>" + Messages.NI_COMBPAIRED + "<br>" + Messages.NI_TU;
			} else {
				message = "<html>" + Messages.NI_NOTCOMB + "<br>" + Messages.NI_TU;
			}
		}
	}

	/**
	 * Gets the icon of the interpretation results.
	 * 
	 * @return Image showing the state of the network resulting from the interpretation.
	 */
	public ImageIcon getIcon() {
		return icon;
	}

	/**
	 * Gets the interpretation suffix.
	 * 
	 * @return Interpretation suffix which states if the network is treated as directed or undirected.
	 */
	public String getInterpretSuffix() {
		return directed ? Messages.DT_DIRECTED : Messages.DT_UNDIRECTED;
	}

	/**
	 * Gets the message describing this network interpretation.
	 * <p>
	 * The message always starts with the <code>&lt;html&gt;</code> tag.
	 * </p>
	 * 
	 * @return Message describing this network interpretation in human-readable form.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Checks if the network must be considered directed.
	 * 
	 * @return <code>true</code> if the network must by interpreted as directed; <code>false</code> if the
	 *         network must be interpreted as undirected.
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * Checks if undirected self-loops must be ignored.
	 * <p>
	 * If the network is to be treated as undirected; this method always returns <code>false</code>.
	 * </p>
	 * 
	 * @return <code>true</code> if undirected self-loops must be ignored in the analysis; <code>false</code>
	 *         otherwise.
	 */
	public boolean isIgnoreUSL() {
		return ignoreUSL;
	}

	/**
	 * Checks if the directed edges are to be paired.
	 * 
	 * @return <code>true</code> if every pair of opposite directed edges must be interpreted as a single
	 *         undirected edge; <code>false</code> otherwise.
	 */
	public boolean isPaired() {
		return paired;
	}

	/**
	 * Flag indicating whether the network should be treated as directed.
	 */
	private boolean directed;

	/**
	 * Flag indicating whether undirected self-loops must be ignored in the analysis.
	 */
	private boolean ignoreUSL;

	/**
	 * Flag indicating whether directed edges should be paired.
	 * <p>
	 * Directed edges may be paired when a network containing directed edges must be treated as undirected. If
	 * this flag is <code>true</code>, every (directed) outgoing edge of a given node is combined with one
	 * opposite (incoming), and thus both edges are counted as one.
	 * </p>
	 */
	private boolean paired;

	/**
	 * Image showing the state of the network resulting from the interpretation.
	 */
	private ImageIcon icon;

	/**
	 * Message describing this network interpretation.
	 */
	private String message;
}
