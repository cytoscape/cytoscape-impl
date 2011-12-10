/*
 Copyright (c) 2006, 2007, 2009, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.filter.internal.filters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.internal.ServicesUtil;
import org.cytoscape.filter.internal.filters.util.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FilterIO {
	private Logger logger = null;
	private final CyApplicationManager applicationManager;
	private final FilterPlugin filterPlugin;

	public FilterIO (CyApplicationManager applicationManager, FilterPlugin filterPlugin) {
		this.applicationManager = applicationManager;
		this.filterPlugin = filterPlugin;
		logger = LoggerFactory.getLogger(FilterIO.class);
	}

	/**
	 *  Construct the filter objects based on the string representation of each filter.
	 */
	public static Vector<CompositeFilter> getFilterVectFromPropFile(final InputStreamReader reader) {

		Vector<CompositeFilter> retFilterVect = new Vector<CompositeFilter>();
		
		try {
			BufferedReader in = new BufferedReader(reader);

			try {
				String oneLine = in.readLine();

				if (oneLine == null) {
					return null;
				}
				double filterVersion = 0.0;
				if (oneLine.trim().startsWith("FilterVersion")) {
					String versionStr = oneLine.trim().substring(14);
					filterVersion = Double.valueOf(versionStr);
				}

				// Ignore filters from the old version
				if (filterVersion <0.2) {
					return null;
				}
				
				while (oneLine != null) {
					// ignore comment, empty line or the version line
					if (oneLine.startsWith("#") || oneLine.trim().equals("")||oneLine.startsWith("FilterVersion")) {
						oneLine = in.readLine();
						continue;
					}

					if (oneLine.trim().startsWith("<Composite>")||oneLine.trim().startsWith("<TopologyFilter>")
					    ||oneLine.trim().startsWith("<InteractionFilter>")) {
						List<String> filterStrVect = new ArrayList<String>();
						filterStrVect.add(oneLine);
						while ((oneLine = in.readLine()) != null) {
							if (oneLine.trim().startsWith("</Composite>")||oneLine.trim().startsWith("</TopologyFilter>")
							    ||oneLine.trim().startsWith("</InteractionFilter>")) {
								filterStrVect.add(oneLine);
								break;
							}
							filterStrVect.add(oneLine);
						} // inner while loop

						CompositeFilter aFilter = getFilterFromStrVect(filterStrVect);
						if (aFilter != null && !FilterUtil.isFilterNameDuplicated(retFilterVect, aFilter.getName())) {
							retFilterVect.add(aFilter);
						}
					}

					oneLine = in.readLine();
				} // while loop
			}
			finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (Exception ex) {
			//logger.error("Filter Read error", ex);
			ex.printStackTrace();
		}
						
		return retFilterVect;
	}

	/**
	 *  Construct the filter objects based on the string representation of each filter.
	 */
	public static Vector<CompositeFilter> getFilterVectFromPropFile(final File input) {
		try {
			return getFilterVectFromPropFile(new FileReader(input));
		}
		catch (final FileNotFoundException e) {
			//logger.error("Filter Read error", e);
			final int[] retval = { 0, 0 };
			return null;
		}
	}

	private static AdvancedSetting getAdvancedSettingFromStrVect(List<String> pAdvSettingStrVect) {
		AdvancedSetting advSetting = new AdvancedSetting();
		String line = null;
		for (int i=0; i<pAdvSettingStrVect.size(); i++ ) {
			line = pAdvSettingStrVect.get(i);
			if (line.startsWith("scope.global=true")) {
				advSetting.setGlobal(true);
			}
			if (line.startsWith("scope.global=false")) {
				advSetting.setGlobal(false);
			}
			if (line.startsWith("scope.session=true")) {
				advSetting.setSession(true);
			}
			if (line.startsWith("scope.session=false")) {
				advSetting.setSession(false);
			}
			if (line.startsWith("selection.node=true")) {
				advSetting.setNode(true);
			}
			if (line.startsWith("selection.node=false")) {
				advSetting.setNode(false);
			}
			if (line.startsWith("selection.edge=true")) {
				advSetting.setEdge(true);
			}
			if (line.startsWith("selection.edge=false")) {
				advSetting.setEdge(false);
			}
			if (line.startsWith("edge.source=true")) {
				advSetting.setSource(true);
			}
			if (line.startsWith("edge.source=false")) {
				advSetting.setSource(false);
			}
			if (line.startsWith("edge.target=true")) {
				advSetting.setTarget(true);
			}
			if (line.startsWith("edge.target=false")) {
				advSetting.setTarget(false);
			}
			if (line.startsWith("Relation=AND")) {
				advSetting.setRelation(Relation.AND);
			}
			if (line.startsWith("Relation=OR")) {
				advSetting.setRelation(Relation.OR);
			}
		}

		return advSetting;
	}
	
	
	private static CompositeFilter getFilterFromStrVect(List<String> pFilterStrVect){
		
		boolean isTopologyFilter = false;
		boolean isInteractionFilter = false;
		
		if (pFilterStrVect.get(0).startsWith("<TopologyFilter>")) {
			isTopologyFilter = true;
		}
		if (pFilterStrVect.get(0).startsWith("<InteractionFilter>")) {
			isInteractionFilter = true;
		}
		
		List<String> advSettingStrVect = new ArrayList<String>();
		List<String> filterStrVect = new ArrayList<String>();

		String line = null;
		// Seperate AdvancedSetting from the rest
		int startIndex = -1, endIndex = -1;
		for (int i=0; i< pFilterStrVect.size(); i++) {
			line = pFilterStrVect.get(i);

			if (line.startsWith("<AdvancedSetting>")) {
				startIndex = i;
			}
			if (line.startsWith("</AdvancedSetting>")) {
				endIndex = i;
				break;
			}
		}
		advSettingStrVect.addAll(pFilterStrVect.subList(startIndex+1, endIndex));

		filterStrVect.addAll(pFilterStrVect.subList(1, startIndex));
		filterStrVect.addAll(pFilterStrVect.subList(endIndex+1, pFilterStrVect.size()));
				
		CompositeFilter retFilter = new CompositeFilter(ServicesUtil.cyApplicationManagerServiceRef);
		retFilter.setAdvancedSetting(getAdvancedSettingFromStrVect(advSettingStrVect));
		
		if (isTopologyFilter) {
			retFilter = new TopologyFilter(ServicesUtil.cyApplicationManagerServiceRef);
			retFilter.setAdvancedSetting(getAdvancedSettingFromStrVect(advSettingStrVect));
			getTopologyFilterFromStrVect((TopologyFilter)retFilter, filterStrVect);
			return retFilter;
		}
		
		if (isInteractionFilter) {
			AdvancedSetting advSetting = getAdvancedSettingFromStrVect(advSettingStrVect);
			if (advSetting.isNodeChecked()) {
				retFilter = new NodeInteractionFilter(ServicesUtil.cyApplicationManagerServiceRef);
			}
			else {//advSetting.isEdgeChecked() == true
				retFilter = new EdgeInteractionFilter(ServicesUtil.cyApplicationManagerServiceRef);
			}
			
			retFilter.setAdvancedSetting(advSetting);
			getInteractionFilterFromStrVect((InteractionFilter)retFilter, filterStrVect);
			return retFilter;
		}
		
		
		Vector<CompositeFilter> allFilterVect = new Vector<CompositeFilter>(); //ServicesUtil.filterReader.getProperties();
		
		for (int i=0; i<filterStrVect.size(); i++ ) {
			line = filterStrVect.get(i) ;

			if (line.startsWith("name=")) {
				String name =line.substring(5);
				retFilter.setName(name);
			}
			if (line.startsWith("Negation=true")) {
				retFilter.setNegation(true);
			}
			if (line.startsWith("Negation=false")) {
				retFilter.setNegation(false);
			}
			if (line.startsWith("StringFilter=")) {
				String _stringFilterValue = line.substring(13); 
				String[] _values = _stringFilterValue.split(":");
				//controllingAttribute+":" + negation+ ":"+searchStr+":"+index_type;
				StringFilter _strFilter = new StringFilter();
				_strFilter.setParent(retFilter);
				_strFilter.setControllingAttribute(_values[0]);
				_strFilter.setNegation((new Boolean(_values[1])).booleanValue());	
				
				// handle the case where ':' is part of the search string
				String _searchStr = _stringFilterValue.substring(_values[0].length()+_values[1].length()+2, _stringFilterValue.length()-_values[_values.length-1].length()-1);		
				_strFilter.setSearchStr(_searchStr);
				
				_strFilter.setIndexType((new Integer(_values[_values.length-1])).intValue());
				retFilter.addChild(_strFilter);
			}
			if (line.startsWith("NumericFilter=")) {
				String[] _values = line.substring(14).split(":");
				//controllingAttribute + ":" + negation+ ":"+lowBound+":" + highBound+ ":"+index_type;

				// Determine data type of the attribute
				String dataType = "int";
				if (_values[2].indexOf(".")>=0 ||_values[2].indexOf("E")>=0 ||
						_values[3].indexOf(".")>=0 ||_values[3].indexOf("E")>=0) {
					dataType = "double";
				}
				
				if (dataType.equalsIgnoreCase("double")) {
					NumericFilter<Double> _numFilter = new NumericFilter<Double>();
					_numFilter.setParent(retFilter);					
					_numFilter.setControllingAttribute(_values[0]);
					_numFilter.setNegation((new Boolean(_values[1])).booleanValue());
					_numFilter.setLowBound(Double.valueOf(_values[2]));
					_numFilter.setHighBound(Double.valueOf(_values[3]));
					_numFilter.setIndexType((new Integer(_values[4])).intValue());					
					retFilter.addChild(_numFilter);
				}
				else { // dataType = "int"
					NumericFilter<Integer> _numFilter = new NumericFilter<Integer>();
					_numFilter.setParent(retFilter);
					_numFilter.setControllingAttribute(_values[0]);
					_numFilter.setNegation((new Boolean(_values[1])).booleanValue());
					_numFilter.setLowBound(Integer.valueOf(_values[2]));
					_numFilter.setHighBound(Integer.valueOf(_values[3]));
					_numFilter.setIndexType((new Integer(_values[4])).intValue());
					retFilter.addChild(_numFilter);
				}
			}
			if (line.startsWith("CompositeFilter=")) {
				//e.g. CompositeFilter=AAA:true
				String[] _values = line.substring(16).split(":");

				String name = _values[0].trim();
				String notValue = _values[1].trim();

				// get the reference CompositeFilter
				CompositeFilter cmpFilter = null;
				
				for (int j=0; j< allFilterVect.size(); j++) {
					if (allFilterVect.elementAt(j).getName().equalsIgnoreCase(name)) {
						cmpFilter = allFilterVect.elementAt(j);
						break;
					}
				}
				if (cmpFilter !=null) {
					retFilter.addChild(cmpFilter, (new Boolean(notValue)).booleanValue());					
				}
			}
			if (line.startsWith("TopologyFilter=")) {
				//e.g. TopologyFilter=AAA:true
				String[] _values = line.substring(15).split(":");

				String name = _values[0].trim();
				String notValue = _values[1].trim();
				
				// get the reference TopologyFilter
				TopologyFilter topoFilter = null;
				for (int j=0; j< allFilterVect.size(); j++) {
					if (allFilterVect.elementAt(j).getName().equalsIgnoreCase(name)) {
						topoFilter = (TopologyFilter) allFilterVect.elementAt(j);
						break;
					}
				}
				if (topoFilter !=null) {
					retFilter.addChild(topoFilter, (new Boolean(notValue)).booleanValue());					
				}
			}
			if (line.startsWith("InteractionFilter=")) {
				//e.g. InteractionFilter=AAA:true
				String[] _values = line.substring(15).split(":");

				String name = _values[0].trim();
				String notValue = _values[1].trim();
				
				// get the reference InteractionFilter
				InteractionFilter interactionFilter = null;
				for (int j=0; j< allFilterVect.size(); j++) {
					if (allFilterVect.elementAt(j).getName().equalsIgnoreCase(name)) {
						interactionFilter = (InteractionFilter) allFilterVect.elementAt(j);
						break;
					}
				}
				if (interactionFilter !=null) {
					retFilter.addChild(interactionFilter, (new Boolean(notValue)).booleanValue());					
				}
			}
		}
		
		return retFilter;
	}
	
	
	private static void getTopologyFilterFromStrVect(TopologyFilter pFilter, List<String> pFilterStrVect){
		//logger.debug("\nFilterIO.getTopologyFilterFromStrVect() ...\n");

		Vector<CompositeFilter> allFilterVect = ServicesUtil.filterReader.getProperties();
		String line = null;
		for (int i=0; i<pFilterStrVect.size(); i++ ) {
			line = pFilterStrVect.get(i) ;

			if (line.startsWith("name=")) {
				String name =line.substring(5).trim();
				pFilter.setName(name);
			}
			if (line.startsWith("Negation=true")) {
				pFilter.setNegation(true);
			}
			if (line.startsWith("Negation=false")) {
				pFilter.setNegation(false);
			}
			
			if (line.startsWith("minNeighbors=")) {
				String minNeighbors = line.substring(13);
				int minN = new Integer(minNeighbors).intValue();
				pFilter.setMinNeighbors(minN);
			}
			if (line.startsWith("withinDistance=")) {
				String withinDistance = line.substring(15);
				int distance = new Integer(withinDistance).intValue();
				pFilter.setDistance(distance);
			}
			if (line.startsWith("passFilter=")) {
				String name = line.substring(11).trim();
				// get the reference CompositeFilter
				CompositeFilter cmpFilter = null;
				
				for (int j=0; j< allFilterVect.size(); j++) {
					if (allFilterVect.elementAt(j).getName().equalsIgnoreCase(name)) {
						cmpFilter = allFilterVect.elementAt(j);
						break;
					}
				}
				if (cmpFilter !=null) {
					pFilter.setPassFilter(cmpFilter);					
				}
			}			
		}	
		//logger.debug("\n\nLeaving FilterIO.getTopologyFilterFromStrVect() ...\n");
		//logger.debug("\nRecovered topo filter is :" + pFilter.toString()+ "\n\n");
	}
	
	
	private static void getInteractionFilterFromStrVect(InteractionFilter pFilter, List<String> pFilterStrVect){

		Vector<CompositeFilter> allFilterVect = ServicesUtil.filterReader.getProperties();
		String line = null;
		for (int i=0; i<pFilterStrVect.size(); i++ ) {
			line = pFilterStrVect.get(i) ;

			if (line.startsWith("name=")) {
				String name =line.substring(5).trim();
				pFilter.setName(name);
			}
			if (line.startsWith("Negation=true")) {
				pFilter.setNegation(true);
			}
			if (line.startsWith("Negation=false")) {
				pFilter.setNegation(false);
			}
			
			if (line.startsWith("nodeType=")) {
				String nodeTypeStr = line.substring(9);
				int nodeType = new Integer(nodeTypeStr).intValue();
				pFilter.setNodeType(nodeType);
			}
			if (line.startsWith("passFilter=")) {
				String name = line.substring(11).trim();
				// get the reference CompositeFilter
				CompositeFilter cmpFilter = null;
				
				for (int j=0; j< allFilterVect.size(); j++) {
					if (allFilterVect.elementAt(j).getName().equalsIgnoreCase(name)) {
						cmpFilter = allFilterVect.elementAt(j);
						break;
					}
				}
				if (cmpFilter !=null) {
					pFilter.setPassFilter(cmpFilter);					
				}
			}			
		}	
	}
	
	
	public void saveGlobalPropFile(File pPropFile) {
		
//		// Because one filter may depend on the other, CompositeFilters must 
//		// be sorted in the order of depthLevel before save
//		Vector<CompositeFilter> allFilterVect = filterPlugin.getAllFilterVect();
//		Object [] sortedFilters = getSortedCompositeFilter(allFilterVect);
//		Object[] globalFilters = getFiltersByScope(sortedFilters, "global");
//		
//		try {
//			BufferedWriter writer = new BufferedWriter(new FileWriter(pPropFile));
//
//            try {
//                // Need to allow writing of header only so that when the last
//                // global filter is deleted, the props file is updated to reflect this
//                writer.write("FilterVersion=0.2\n");
//
//                if (globalFilters != null) {
//                    for (int i = 0; i < globalFilters.length; i++) {
//                        CompositeFilter theFilter = (CompositeFilter) globalFilters[i];
//                        writer.write(theFilter.toSerializedForm());
//                        writer.newLine();
//                    }
//                }
//            }
//            finally {
//                if (writer != null) {
//                    writer.close();
//                }
//            }
//		} catch (Exception ex) {
//			logger.error("Global filter Write error",ex);
//		}
	}
	
	
	private Object[] getFiltersByScope(Object[] pFilters, String pScope) {
		if (pFilters == null || pFilters.length == 0) {
			return null;
		}
		ArrayList<CompositeFilter> retFilterList = new ArrayList<CompositeFilter>();
		for (int i = 0; i < pFilters.length; i++) {
			CompositeFilter theFilter = (CompositeFilter) pFilters[i];
			AdvancedSetting advSetting = theFilter.getAdvancedSetting();
			
			if (pScope.equalsIgnoreCase("global")) {
				if (advSetting.isGlobalChecked()) {
					retFilterList.add(theFilter);
				}
			}
			if (pScope.equalsIgnoreCase("session")) {
				if (advSetting.isSessionChecked()) {
					retFilterList.add(theFilter);
				}
			}
		}
		return retFilterList.toArray();
	}
	
	public void saveSessionStateFiles(List<File> pFileList){
				
//		// Because one filter may depend on the other, CompositeFilters must 
//		// be sorted in the order of depthLevel before save
//		Vector<CompositeFilter> allFilterVect = filterPlugin.getAllFilterVect();
//		Object [] sortedFilters = getSortedCompositeFilter(allFilterVect);
//		Object[] sessionFilters = getFiltersByScope(sortedFilters, "session");
//		
//		if (sessionFilters == null || sessionFilters.length == 0) {
//			return;
//		}
//		
//		// Create an empty file on system temp directory
//		String tmpDir = System.getProperty("java.io.tmpdir");
//		// logger.debug("java.io.tmpdir: [" + tmpDir + "]");
//
//		File session_filter_file = new File(tmpDir, "session_filters.props");
//
//		//
//		try {
//			BufferedWriter writer = new BufferedWriter(new FileWriter(session_filter_file));
//
//            try {
//                writer.write("FilterVersion=0.2\n");
//
//                for (int i = 0; i < sessionFilters.length; i++) {
//                    CompositeFilter theFilter = (CompositeFilter) sessionFilters[i];
//                    writer.write(theFilter.toSerializedForm());
//                    writer.newLine();
//                }
//            }
//            finally {
//                if ( writer != null) {
//                    writer.close();
//                }
//            }
//		} catch (Exception ex) {
//			logger.error("Session filter Write error",ex);
//		}
//
//        if ((session_filter_file != null) && (session_filter_file.exists())) {
//            pFileList.add(session_filter_file);
//        }
	}
	
	
	public void restoreSessionState(List<File> pStateFileList) {
		if ((pStateFileList == null) || (pStateFileList.size() == 0)) {
			logger.warn("\tNo previous filter state to restore.");
			return;
		}
		
		try {
			File session_filter_file = pStateFileList.get(0);

			//int[] loadCounts = 
			getFilterVectFromPropFile(session_filter_file);
			//logger.info("\tLoad " + loadCounts[1] + " session filters");
			//logger.info("\t\t" + (loadCounts[0]-loadCounts[1]) + " duplicated filters are not loaded");
		} catch (Throwable ee) {
			logger.error("Failed to restore Filters from session!");
		}
	}

	
	// Determine the nest level of the given CompositeFilter
	private int getTreeDepth(CompositeFilter pFilter, int pDepthLevel){
		List<CyFilter> childrenList = pFilter.getChildren();
		List<CompositeFilter> theList = new ArrayList<CompositeFilter>();
		
		// Find all the child compositeFilter
		for (int i=0; i<childrenList.size(); i++) {
			if (childrenList.get(i) instanceof CompositeFilter) {
				theList.add((CompositeFilter)childrenList.get(i));
			}
		}
		
		if (theList.size() == 0) {
			return pDepthLevel;
		}
		
		int [] depths = new int[theList.size()];
		for (int j=0; j<theList.size(); j++) {
			depths[j] = getTreeDepth(theList.get(j), pDepthLevel+1);
		}
		
		java.util.Arrays.sort(depths);
		
		return depths[depths.length-1];		
	}
	
	public Object [] getSortedCompositeFilter(List<?> pAllFilterVect) {
		if (pAllFilterVect == null || pAllFilterVect.size() == 0) {
			return null;
		}
 
		//Seperate TopologyFilter from other CompositeFilter
		List<TopologyFilter> topoFilterVect = new ArrayList<TopologyFilter>();
		List<CompositeFilter> otherFilterVect = new ArrayList<CompositeFilter>();
		for (int i=0; i<pAllFilterVect.size(); i++ ) {
			if (pAllFilterVect.get(i) instanceof TopologyFilter) {
				topoFilterVect.add((TopologyFilter)pAllFilterVect.get(i));
			}
			else {
				otherFilterVect.add((CompositeFilter)pAllFilterVect.get(i));
			}
		}
				
		//TopologyFilters depend on other compositeFilter, they should follow other compositeFilter
		CompositeFilter[] sortedFilters = otherFilterVect.toArray(new CompositeFilter[otherFilterVect.size()]);
		Arrays.sort(sortedFilters, (new CompositeFilterCmp<CompositeFilter>()));		
		
		List<CompositeFilter> sortedFilterVect = new ArrayList<CompositeFilter>();
		for (int i=0; i<sortedFilters.length; i++) {
			sortedFilterVect.add(sortedFilters[i]);
		}
		
		sortedFilterVect.addAll(topoFilterVect);
		
		return sortedFilterVect.toArray();
	}
	
	class CompositeFilterCmp<T> implements Comparator<CompositeFilter> {
		
		public int compare(CompositeFilter o1, CompositeFilter o2){
			int depth1 = getTreeDepth(o1, 0);
			int depth2 = getTreeDepth(o2, 0);

			if (depth1 > depth2) {
				return 1;
			}
			if (depth1 == depth2) {
				return 0;
			}
			return -1;//depth1 < depth2
		}
		
		public boolean equals(Object obj) {
			return false;
		}
		 
	}
}

