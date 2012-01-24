/*
 Copyright (c) 2006,2010 The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.filter.internal.write.filter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.cytoscape.filter.internal.filters.model.AdvancedSetting;
import org.cytoscape.filter.internal.filters.model.CompositeFilter;
import org.cytoscape.filter.internal.filters.model.CyFilter;
import org.cytoscape.filter.internal.filters.model.TopologyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterWriter {

	private static final Logger logger = LoggerFactory.getLogger(FilterWriter.class);

	public void saveGlobalPropFile(final Set<CompositeFilter> filters, final File file) {
		// Because one filter may depend on the other, CompositeFilters must
		// be sorted in the order of depthLevel before save
		Set<CompositeFilter> allFilterVect = filters; // filterPlugin.getAllFilterVect();
		CompositeFilter[] sortedFilters = getSortedCompositeFilter(allFilterVect);
		CompositeFilter[] globalFilters = getFiltersByScope(sortedFilters, "global");

		try {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));

			try {
				// Need to allow writing of header only so that when the last
				// global filter is deleted, the props file is updated to reflect this
				writer.write("FilterVersion=0.2\n");

				if (globalFilters != null) {
					for (int i = 0; i < globalFilters.length; i++) {
						CompositeFilter theFilter = (CompositeFilter) globalFilters[i];
						writer.write(theFilter.toSerializedForm());
						writer.write("\n");
					}
				}
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		} catch (Exception ex) {
			logger.error("Global filter Write error", ex);
		}
	}

	private static CompositeFilter[] getFiltersByScope(Object[] pFilters, String pScope) {
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

		return retFilterList.toArray(new CompositeFilter[retFilterList.size()]);
	}

	private static CompositeFilter[] getSortedCompositeFilter(Collection<?> pAllFilterVect) {
		if (pAllFilterVect == null || pAllFilterVect.size() == 0) {
			return null;
		}

		// Separate TopologyFilter from other CompositeFilter
		List<TopologyFilter> topoFilterVect = new ArrayList<TopologyFilter>();
		List<CompositeFilter> otherFilterVect = new ArrayList<CompositeFilter>();

		for (Object obj : pAllFilterVect) {
			if (obj instanceof TopologyFilter) {
				topoFilterVect.add((TopologyFilter) obj);
			} else {
				otherFilterVect.add((CompositeFilter) obj);
			}
		}

		// TopologyFilters depend on other compositeFilter, they should follow
		// other compositeFilter
		CompositeFilter[] sortedFilters = otherFilterVect.toArray(new CompositeFilter[otherFilterVect.size()]);
		Arrays.sort(sortedFilters, (new CompositeFilterCmp<CompositeFilter>()));
		List<CompositeFilter> sortedFilterList = new ArrayList<CompositeFilter>();

		for (int i = 0; i < sortedFilters.length; i++) {
			sortedFilterList.add(sortedFilters[i]);
		}

		sortedFilterList.addAll(topoFilterVect);

		return sortedFilterList.toArray(new CompositeFilter[sortedFilterList.size()]);
	}

	private static class CompositeFilterCmp<T> implements Comparator<CompositeFilter> {

		public int compare(CompositeFilter o1, CompositeFilter o2) {
			int depth1 = getTreeDepth(o1, 0);
			int depth2 = getTreeDepth(o2, 0);

			if (depth1 > depth2)
				return 1;
			if (depth1 == depth2)
				return 0;

			return -1; // depth1 < depth2
		}

		public boolean equals(Object obj) {
			return false;
		}
		
		private static int getTreeDepth(CompositeFilter pFilter, int pDepthLevel) {
			List<CyFilter> childrenList = pFilter.getChildren();
			List<CompositeFilter> theList = new ArrayList<CompositeFilter>();

			// Find all the child compositeFilter
			for (int i = 0; i < childrenList.size(); i++) {
				if (childrenList.get(i) instanceof CompositeFilter) {
					theList.add((CompositeFilter) childrenList.get(i));
				}
			}

			if (theList.size() == 0) {
				return pDepthLevel;
			}

			int[] depths = new int[theList.size()];
			for (int j = 0; j < theList.size(); j++) {
				depths[j] = getTreeDepth(theList.get(j), pDepthLevel + 1);
			}

			Arrays.sort(depths);

			return depths[depths.length - 1];
		}
	}

	public void write(Collection<CompositeFilter> filters, File file) {
		// Because one filter may depend on the other, CompositeFilters must
		// be sorted in the order of depthLevel before save
		CompositeFilter[] sortedFilters = getSortedCompositeFilter(filters);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			try {
				writer.write("FilterVersion=0.2\n");

				for (CompositeFilter cf : sortedFilters) {
					writer.write(cf.toSerializedForm());
					writer.newLine();
				}
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		} catch (Exception ex) {
			logger.error("Error writing filters file", ex);
		}
	}
}
