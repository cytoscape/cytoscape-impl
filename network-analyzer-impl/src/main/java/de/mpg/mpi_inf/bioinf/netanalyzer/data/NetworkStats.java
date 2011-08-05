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

package de.mpg.mpi_inf.bioinf.netanalyzer.data;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetwork;

/**
 * Storage class for network parameters.
 *
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 * @author Nadezhda Doncheva
 */
public class NetworkStats {

	/**
	 * ID of parameter that stores the network title.
	 */
	public static final String titleParam = "networkTitle";

	/**
	 * List of IDs for parameters that are numbers or <code>String</code>s.
	 * <p>
	 * It is recommended that the parameters are displayed to the user in the order they appear in
	 * this list.
	 * </p>
	 */
	public static final String[] simpleParams = new String[] {
		"cc",
		"ncc",
		"diameter",
		"radius",
		"centralization",
		"connPairs",
		"avSpl",
		"avNeighbors",
		"nodeCount",
		"edgeCount",
		"density",
		"heterogeneity",
		"usn",
		"nsl",
		"mnp",
		"time"
	};

	/**
	 * List of IDs and types for parameters that are datasets.
	 * <p>
	 * It is recommended that the parameters are displayed to the user in the order they appear in
	 * this list.
	 * </p>
	 */
	public static final String[] complexParams = new String[] {
		"degreeDist",        // undirected
		"inDegreeDist",      // directed
		"outDegreeDist",     // directed
		"cksDist",           // undirected
		"topCoefs",          // undirected
		"splDist",           // undirected
		"commNeighbors",     // undirected
		"neighborConn",      // undirected
		"allNeighborConn",   // directed
		"inNeighborConn",    // directed	
		"outNeighborConn",   // directed
		"nodeBetween",		 // undirected
		"closenessCent",	 // undirected
		"stressDist" 		 // undirected
	};

	/**
	 * Initializes a new instance of <code>NetworkStats</code>.
	 */
	public NetworkStats() {
		params = new HashMap<String, Object>();
		network = null;
	}

	/**
	 * Initializes a new instance of <code>NetworkStats</code>.
	 *
	 * @param aNetwork Network, on which parameters will be stored.
	 */
	public NetworkStats(CyNetwork aNetwork, String aInterpretName) {
		params = new HashMap<String, Object>();
		set("networkTitle", aNetwork.getCyRow().get("name",String.class) + aInterpretName);
		set("nodeCount", new Integer(aNetwork.getNodeCount()));
		network = aNetwork;
	}

	/**
	 * Checks if the specified parameter is computed.
	 *
	 * @param aParam ID of parameter.
	 * @return <code>true</code> if the parameter with the specified ID is stored in this instance;
	 * <code>false</code> otherwise.
	 */
	public boolean contains(String aParam) {
		return params.containsKey(aParam);
	}

	/**
	 * Gets the value of the specified parameter.
	 *
	 * @param aParam ID of the parameter to get.
	 * @return Instance of parameter's value; <code>null</code> if such a parameter is not stored.
	 */
	public Object get(String aParam) {
		return params.get(aParam);
	}

	/**
	 * Gets the value of the specified complex parameter.
	 *
	 * @param aParam ID of the complex parameter to get.
	 * @return Parameter's value as a <code>ComplexParam</code>.
	 * @throws ClassCastException If the specified parameter is simple.
	 * @throws NullPointerException If the specified parameter does not exist.
	 */
	public ComplexParam getComplex(String aParam) {
		return (ComplexParam) params.get(aParam);
	}

	/**
	 * Gets the IDs of the computed simple parameters.
	 *
	 * @return Array of IDs of all the simple parameters stored in this instance.
	 */
	public String[] getComputedSimple() {
		return getComputed(simpleParams);
	}

	/**
	 * Gets the IDs of the computed complex parameters.
	 *
	 * @return Array of IDs of all the complex parameters stored in this instance.
	 */
	public String[] getComputedComplex() {
		return getComputed(complexParams);
	}

	/**
	 * Gets the integer value of the specified simple parameter.
	 *
	 * @param aParam ID of the simple parameter to get.
	 * @return Parameter's value as an integer.
	 * @throws ClassCastException If the specified parameter is not an integer.
	 * @throws NullPointerException If the specified parameter does not exist.
	 */
	public int getInt(String aParam) {
		return ((Integer) params.get(aParam)).intValue();
	}

	/**
	 * Gets the integer value of the specified simple parameter.
	 *
	 * @param aParam ID of the simple parameter to get.
	 * @return Parameter's value as a long integer.
	 * @throws ClassCastException If the specified parameter is not a long integer.
	 * @throws NullPointerException If the specified parameter does not exist.
	 */
	public long getLong(String aParam) {
		return ((Long) params.get(aParam)).longValue();
	}

	/**
	 * Gets the title of the analyzed network.
	 *
	 * @return Network title; <code>null</code> if the title is not stored in this instance.
	 */
	public String getTitle() {
		return (String) get(NetworkStats.titleParam);
	}

	/**
	 * Gets the analyzed network.
	 *
	 * @return Network; <code>null</code> if the network is not stored in this instance.
	 */
	public CyNetwork getNetwork() {
		return network;
	}

	/**
	 * Sets the value of the specified simple parameter.
	 * 
	 * @param aParam ID of the parameter to get.
	 * @param aValue Instance of parameter's value.
	 */
	public void set(String aParam, Object aValue) {
		params.put(aParam, aValue);
	}

	/**
	 * Sets the integer value of the specified simple parameter.
	 * 
	 * @param aParam ID of the parameter to get.
	 * @param aValue Parameter's value as an integer number.
	 */
	public void set(String aParam, int aValue) {
		set(aParam, new Integer(aValue));
	}

	/**
	 * Sets the integer value of the specified simple parameter.
	 * 
	 * @param aParam ID of the parameter to get.
	 * @param aValue Parameter's value as an integer number.
	 */
	public void set(String aParam, long aValue) {
		set(aParam, new Long(aValue));
	}

	/**
	 * Sets the real-number value of the specified simple parameter.
	 * 
	 * @param aParam ID of the parameter to get.
	 * @param aValue Parameter's value as a real number.
	 */
	public void set(String aParam, double aValue) {
		set(aParam, new Double(aValue));
	}

	/**
	 * Sets the title of the analyzed network.
	 *
	 * @param aTitle Network title.
	 */
	public void setTitle(String aTitle) {
		set(NetworkStats.titleParam, aTitle);
	}

	/**
	 * Gets the computed parameters among the given IDs.
	 *
	 * @param aIDs Array of IDs of network parameters.
	 * @return Array of those IDs among <code>aIDs</code> that are contained as keys in
	 * {@link #params}.
	 */
	private String[] getComputed(String[] aIDs) {
		int size = 0;
		for (int i = 0; i < aIDs.length; ++i) {
			if (params.containsKey(aIDs[i])) {
				size++;
			}
		}
		if (size == aIDs.length) {
			return aIDs;
		}
		String[] computed = new String[size];
		size = 0;
		for (int i = 0; i < aIDs.length; ++i) {
			if (params.containsKey(aIDs[i])) {
				computed[size++] = aIDs[i];
			}
		}
		return computed;
	}

	/**
	 * Network parameters in the form of a (ID, value) map.
	 */
	private Map<String, Object> params;
	
	/**
	 * Unique network id. Needed for parameter visualization.
	 */
	private CyNetwork network; 
}
