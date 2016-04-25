package org.cytoscape.filter.internal.read.filter;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.cytoscape.filter.internal.filters.model.AdvancedSetting;
import org.cytoscape.filter.internal.filters.model.CompositeFilter;
import org.cytoscape.filter.internal.filters.model.EdgeInteractionFilter;
import org.cytoscape.filter.internal.filters.model.InteractionFilter;
import org.cytoscape.filter.internal.filters.model.NodeInteractionFilter;
import org.cytoscape.filter.internal.filters.model.NumericFilter;
import org.cytoscape.filter.internal.filters.model.Relation;
import org.cytoscape.filter.internal.filters.model.StringFilter;
import org.cytoscape.filter.internal.filters.model.TopologyFilter;
import org.cytoscape.filter.internal.filters.util.FilterUtil;
import org.cytoscape.filter.internal.filters.util.ServicesUtil;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class FilterReader {
	private static final Logger logger = LoggerFactory.getLogger(FilterReader.class);

	private final QuickFind quickFind;
	
	public FilterReader(final QuickFind quickFind) {
		this.quickFind = quickFind;
	}
	
	public Collection<CompositeFilter> read(final File file) {
		Collection<CompositeFilter> filters = null;
		InputStream is = null;
		
		try {
			is = new FileInputStream(file);
			filters = read(is);
		} catch (FileNotFoundException fnfe) {
			logger.error("Filter Read error: file not found", fnfe);
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException ioe) {}
				is = null;
			}
		}
		
		return filters;
	}
	
	/**
	 *  Construct the filter objects based on the string representation of each filter.
	 */
	public Collection<CompositeFilter> read(final InputStream in) {
		final Collection<CompositeFilter> filters = new LinkedHashSet<CompositeFilter>();
		final InputStreamReader reader = new InputStreamReader(in);
		
		try {
			final BufferedReader br = new BufferedReader(reader);
			String oneLine = br.readLine();

			if (oneLine == null)
				return null;
			
			double filterVersion = 0.0;
			
			if (oneLine.trim().startsWith("FilterVersion")) {
				String versionStr = oneLine.trim().substring(14);
				filterVersion = Double.valueOf(versionStr);
			}

			// Ignore filters from the old version
			if (filterVersion < 0.2)
				return null;
			
			while (oneLine != null) {
				// ignore comment, empty line or the version line
				if (oneLine.startsWith("#") || oneLine.trim().equals("") || oneLine.startsWith("FilterVersion")) {
					oneLine = br.readLine();
					continue;
				}

				if (oneLine.trim().startsWith("<Composite>") || oneLine.trim().startsWith("<TopologyFilter>")
						|| oneLine.trim().startsWith("<InteractionFilter>")) {
					List<String> filterStrVect = new ArrayList<String>();
					filterStrVect.add(oneLine);

					while ((oneLine = br.readLine()) != null) {
						filterStrVect.add(oneLine);
						
						if (oneLine.trim().startsWith("</Composite>")
								|| oneLine.trim().startsWith("</TopologyFilter>")
								|| oneLine.trim().startsWith("</InteractionFilter>")) {
							break;
						}
					}

					CompositeFilter aFilter = getFilterFromStrList(filterStrVect, filters);

					if (aFilter != null && !FilterUtil.isFilterNameDuplicated(filters, aFilter.getName())) {
						filters.add(aFilter);
					}
				}

				oneLine = br.readLine();
			}
		} catch (Exception ex) {
			logger.error("Filter Read error", ex);
		}
						
		return filters;
	}
	
	private CompositeFilter getFilterFromStrList(List<String> strList, Collection<CompositeFilter> filters) {
		boolean isTopologyFilter = false;
		boolean isInteractionFilter = false;

		if (strList.get(0).startsWith("<TopologyFilter>")) {
			isTopologyFilter = true;
		}
		
		if (strList.get(0).startsWith("<InteractionFilter>")) {
			isInteractionFilter = true;
		}

		List<String> advSettingStrVect = new ArrayList<String>();
		List<String> filterStrVect = new ArrayList<String>();

		// Seperate AdvancedSetting from the rest
		int startIndex = -1, endIndex = -1;
		String line;

		for (int i = 0; i < strList.size(); i++) {
			line = strList.get(i);

			if (line.startsWith("<AdvancedSetting>")) {
				startIndex = i;
			}

			if (line.startsWith("</AdvancedSetting>")) {
				endIndex = i;
				break;
			}
		}

		advSettingStrVect.addAll(strList.subList(startIndex + 1, endIndex));

		filterStrVect.addAll(strList.subList(1, startIndex));
		filterStrVect.addAll(strList.subList(endIndex + 1, strList.size()));

		CompositeFilter retFilter = new CompositeFilter(ServicesUtil.cyApplicationManagerServiceRef);
		retFilter.setAdvancedSetting(getAdvancedSettingFromStr(advSettingStrVect));

		if (isTopologyFilter) {
			retFilter = new TopologyFilter(ServicesUtil.cyApplicationManagerServiceRef);
			retFilter.setAdvancedSetting(getAdvancedSettingFromStr(advSettingStrVect));
			getTopologyFilterFromStr((TopologyFilter) retFilter, filterStrVect, filters);
			return retFilter;
		}

		if (isInteractionFilter) {
			AdvancedSetting advSetting = getAdvancedSettingFromStr(advSettingStrVect);

			if (advSetting.isNodeChecked()) {
				retFilter = new NodeInteractionFilter(ServicesUtil.cyApplicationManagerServiceRef);
			} else {// advSetting.isEdgeChecked() == true
				retFilter = new EdgeInteractionFilter(ServicesUtil.cyApplicationManagerServiceRef);
			}

			retFilter.setAdvancedSetting(advSetting);
			getInteractionFilterFromStr((InteractionFilter) retFilter, filterStrVect, filters);
			
			return retFilter;
		}

		Collection<CompositeFilter> allFilters = new LinkedHashSet<CompositeFilter>();

		for (int i = 0; i < filterStrVect.size(); i++) {
			line = filterStrVect.get(i);

			if (line.startsWith("name=")) {
				String name = line.substring(5);
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
				// controllingAttribute+":" + negation+ ":"+searchStr+":"+index_type;
				StringFilter _strFilter = new StringFilter(quickFind);
				_strFilter.setParent(retFilter);
				_strFilter.setControllingAttribute(_values[0]);
				_strFilter.setNegation((new Boolean(_values[1])).booleanValue());

				// handle the case where ':' is part of the search string
				String _searchStr = _stringFilterValue.substring(_values[0].length() + _values[1].length() + 2,
						_stringFilterValue.length() - _values[_values.length - 1].length() - 1);
				_strFilter.setSearchStr(_searchStr);

				_strFilter.setIndexType((new Integer(_values[_values.length - 1])).intValue());
				retFilter.addChild(_strFilter);
			}
			if (line.startsWith("NumericFilter=")) {
				String[] _values = line.substring(14).split(":");
				// controllingAttribute + ":" + negation+ ":"+lowBound+":" +
				// highBound+ ":"+index_type;

				// Determine data type of the attribute
				String dataType = "int";

				if (_values[2].indexOf(".") >= 0 || _values[2].indexOf("E") >= 0 || _values[3].indexOf(".") >= 0
						|| _values[3].indexOf("E") >= 0) {
					dataType = "double";
				}

				if (dataType.equalsIgnoreCase("double")) {
					NumericFilter<Double> _numFilter = new NumericFilter<Double>(quickFind);
					_numFilter.setParent(retFilter);
					_numFilter.setControllingAttribute(_values[0]);
					_numFilter.setNegation((new Boolean(_values[1])).booleanValue());
					_numFilter.setLowBound(Double.valueOf(_values[2]));
					_numFilter.setHighBound(Double.valueOf(_values[3]));
					_numFilter.setIndexType((new Integer(_values[4])).intValue());
					retFilter.addChild(_numFilter);
				} else { // dataType = "int"
					NumericFilter<Integer> _numFilter = new NumericFilter<Integer>(quickFind);
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
				// e.g. CompositeFilter=AAA:true
				String[] _values = line.substring(16).split(":");

				String name = _values[0].trim();
				String notValue = _values[1].trim();

				// get the reference CompositeFilter
				CompositeFilter cmpFilter = null;

				for (CompositeFilter cf : allFilters) {
					if (cf.getName().equalsIgnoreCase(name)) {
						cmpFilter = cf;
						break;
					}
				}

				if (cmpFilter != null) {
					retFilter.addChild(cmpFilter, (new Boolean(notValue)).booleanValue());
				}
			}

			if (line.startsWith("TopologyFilter=")) {
				// e.g. TopologyFilter=AAA:true
				String[] _values = line.substring(15).split(":");

				String name = _values[0].trim();
				String notValue = _values[1].trim();

				// get the reference TopologyFilter
				TopologyFilter topoFilter = null;
				
				for (CompositeFilter cf : allFilters) {
					if (cf.getName().equalsIgnoreCase(name)) {
						topoFilter = (TopologyFilter) cf;
						break;
					}
				}
				
				if (topoFilter != null) {
					retFilter.addChild(topoFilter, (new Boolean(notValue)).booleanValue());
				}
			}

			if (line.startsWith("InteractionFilter=")) {
				// e.g. InteractionFilter=AAA:true
				String[] _values = line.substring(15).split(":");

				String name = _values[0].trim();
				String notValue = _values[1].trim();

				// get the reference InteractionFilter
				InteractionFilter interactionFilter = null;

				for (CompositeFilter cf : allFilters) {
					if (cf.getName().equalsIgnoreCase(name)) {
						interactionFilter = (InteractionFilter) cf;
						break;
					}
				}
				if (interactionFilter != null) {
					retFilter.addChild(interactionFilter, (new Boolean(notValue)).booleanValue());
				}
			}
		}

		return retFilter;
	}
	
	private AdvancedSetting getAdvancedSettingFromStr(List<String> pAdvSettingStrVect) {
		AdvancedSetting advSetting = new AdvancedSetting();
		String line;

		for (int i = 0; i < pAdvSettingStrVect.size(); i++) {
			line = pAdvSettingStrVect.get(i);

			if (line.startsWith("scope.global=true"))
				advSetting.setGlobal(true);
			if (line.startsWith("scope.global=false"))
				advSetting.setGlobal(false);
			if (line.startsWith("scope.session=true"))
				advSetting.setSession(true);
			if (line.startsWith("scope.session=false"))
				advSetting.setSession(false);
			if (line.startsWith("selection.node=true"))
				advSetting.setNode(true);
			if (line.startsWith("selection.node=false"))
				advSetting.setNode(false);
			if (line.startsWith("selection.edge=true"))
				advSetting.setEdge(true);
			if (line.startsWith("selection.edge=false"))
				advSetting.setEdge(false);
			if (line.startsWith("edge.source=true"))
				advSetting.setSource(true);
			if (line.startsWith("edge.source=false"))
				advSetting.setSource(false);
			if (line.startsWith("edge.target=true"))
				advSetting.setTarget(true);
			if (line.startsWith("edge.target=false"))
				advSetting.setTarget(false);
			if (line.startsWith("Relation=AND"))
				advSetting.setRelation(Relation.AND);
			if (line.startsWith("Relation=OR"))
				advSetting.setRelation(Relation.OR);
		}

		return advSetting;
	}
	
	private void getTopologyFilterFromStr(TopologyFilter pFilter, List<String> pFilterStrVect,
			Collection<CompositeFilter> filters) {
		String line;
		
		for (int i = 0; i < pFilterStrVect.size(); i++) {
			line = pFilterStrVect.get(i);

			if (line.startsWith("name=")) {
				String name = line.substring(5).trim();
				pFilter.setName(name);
			}
			
			if (line.startsWith("Negation=true"))
				pFilter.setNegation(true);
			if (line.startsWith("Negation=false"))
				pFilter.setNegation(false);

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

				for (CompositeFilter cf : filters) {
					if (cf.getName().equalsIgnoreCase(name)) {
						cmpFilter = cf;
						break;
					}
				}
				
				if (cmpFilter != null) {
					pFilter.setPassFilter(cmpFilter);
				}
			}
		}
	}
	
	private void getInteractionFilterFromStr(InteractionFilter pFilter, List<String> pFilterStrVect,
			Collection<CompositeFilter> filters) {
		String line;

		for (int i = 0; i < pFilterStrVect.size(); i++) {
			line = pFilterStrVect.get(i);

			if (line.startsWith("name=")) {
				String name = line.substring(5).trim();
				pFilter.setName(name);
			}
			
			if (line.startsWith("Negation=true"))
				pFilter.setNegation(true);
			if (line.startsWith("Negation=false"))
				pFilter.setNegation(false);

			if (line.startsWith("nodeType=")) {
				String nodeTypeStr = line.substring(9);
				int nodeType = new Integer(nodeTypeStr).intValue();
				pFilter.setNodeType(nodeType);
			}

			if (line.startsWith("passFilter=")) {
				String name = line.substring(11).trim();
				// get the reference CompositeFilter
				CompositeFilter cmpFilter = null;

				for (CompositeFilter cf : filters) {
					if (cf.getName().equalsIgnoreCase(name)) {
						cmpFilter = cf;
						break;
					}
				}

				if (cmpFilter != null) {
					pFilter.setPassFilter(cmpFilter);
				}
			}
		}
	}
}
