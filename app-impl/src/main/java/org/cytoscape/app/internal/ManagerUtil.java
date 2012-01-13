/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.app.internal;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

import org.cytoscape.app.internal.AppStatus;

/**
 * 
 */
public class ManagerUtil {
	// get the list sorted the way we want to display it, I'd like to do these
	// in one method somehow
	// where you just give it the AppInfo method to sort by. I'm sure there's
	// a way, I just don't know it yet
	/**
	 * DOCUMENT ME!
	 * 
	 * @param Apps
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static Map<String, List<DownloadableInfo>> sortByCategory(
			List<DownloadableInfo> Apps) {
		Map<String, List<DownloadableInfo>> Categories = new java.util.HashMap<String, List<DownloadableInfo>>();

		for (DownloadableInfo Current : Apps) {
			List<DownloadableInfo> categoryList = Categories.get(Current.getCategory());
			if (categoryList != null) {
				categoryList.add(Current);
			} else {
				List<DownloadableInfo> List = new java.util.ArrayList<DownloadableInfo>();
				List.add(Current);
				Collections.sort(List, NAME_ORDER);
				Categories.put(Current.getCategory(), List);
			}
		}

		return Categories;
	}

	// cheap and hacky I know....
	public static Map<String, List<AppInfo>> sortByClass(
			List<AppInfo> Apps) {
		Map<String, List<AppInfo>> Classes = new java.util.HashMap<String, List<AppInfo>>();

		for (AppInfo Current : Apps) {
			List<AppInfo> classList = Classes.get(Current.getAppClassName());
			if (classList != null) {
				classList.add(Current);
			} else {
				List<AppInfo> List = new java.util.ArrayList<AppInfo>();
				List.add(Current);
				Classes.put(Current.getAppClassName(), List);
			}
		}
		return Classes;
	}

	public static Map<String, List<AppInfo>> sortByID(
			List<AppInfo> Apps) {
		Map<String, List<AppInfo>> Ids = new java.util.HashMap<String, List<AppInfo>>();

		for (AppInfo Current : Apps) {
			List<AppInfo> idList = Ids.get(Current.getID());
			if (Ids.containsKey(Current.getID())) {
				idList.add(Current);
			} else {
				List<AppInfo> List = new java.util.ArrayList<AppInfo>();
				List.add(Current);
				Ids.put(Current.getID(), List);
			}
		}
		return Ids;
	}

	/**
	 * Returns a list of available apps minus any currently installed
	 * 
	 * @param Current
	 * @param Available
	 */
	public static List<DownloadableInfo> getUnique(
			List<DownloadableInfo> Current, List<DownloadableInfo> Available) {
			java.util.Set<DownloadableInfo> CurrentSet = new java.util.HashSet<DownloadableInfo>(Current);
		
		List<DownloadableInfo> UniqueAvail = new java.util.ArrayList<DownloadableInfo>(
				Available);

		if (Current == null) {
			return Available;
		}

		for (DownloadableInfo infoAvail : Available) {
			for (DownloadableInfo infoCur : Current) {
				if (!AppManager.getAppManager().usingWebstartManager()) {
					if (infoCur.getType().equals(infoAvail.getType()) && infoCur.equalsDifferentObjectVersion(infoAvail)) {
						UniqueAvail.remove(infoAvail);
					}
				} else { // in webstart
					if ( infoCur.equalsDifferentObjectVersion(infoAvail) ||
						infoCur.getName().equals(infoAvail.getName()) ) { 
						infoAvail.setDescription( infoCur.getDescription() + 
								"<p><font color='red'><i><b>Webstart Warning:</b><br>This app may be the same as a app loaded with the webstart bundle '" + 
								infoCur.toString() + "' </i></font>");
					}
							
				}

			}
		}
		return UniqueAvail;
	}

	/**
	 * Takes a Class object for a CytoscapeApp and returns the DownloadableInfo
	 * object associated
	 * 
	 * @param appClass
	 * @return DownloadableInfo object
	 */
	public static DownloadableInfo getInfoObject(Class appClass) {
		AppManager mgr = AppManager.getAppManager();
		
		List<DownloadableInfo> Downloadables = mgr.getDownloadables(AppStatus.CURRENT);

		for (DownloadableInfo Current : Downloadables) {
			
			if (Current.getType().equals(DownloadableType.THEME)) {
				ThemeInfo t = (ThemeInfo) Current;
				for (AppInfo p: t.getApps()) {
					if (p.getAppClassName().equals(appClass.getName()))
						return t; // return the theme that contains the app
				}
			} else {
				AppInfo p = (AppInfo) Current;
				if (p.getAppClassName().equals(appClass.getName()))
					return p;
			}
		}
		return null;
	}

	public static List sort(List toSort) {

		return null;
	}

	// this doesn't appear to work as I would expect
	private static final Comparator<DownloadableInfo> NAME_ORDER = new Comparator<DownloadableInfo>() {
		public int compare(DownloadableInfo p1, DownloadableInfo p2) {
			int nameCmp = p2.getName().toLowerCase().compareTo(
					p1.getName().toLowerCase());
			return nameCmp;
		}
	};

}
