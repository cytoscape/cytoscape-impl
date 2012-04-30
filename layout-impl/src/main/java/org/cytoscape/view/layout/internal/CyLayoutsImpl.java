/*
  File: CyLayoutsImpl.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.view.layout.internal;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.layout.internal.algorithms.GridNodeLayout;


/**
 * CyLayoutsImpl is a singleton class that is used to register all available
 * layout algorithms.  
 */
public class CyLayoutsImpl implements CyLayoutAlgorithmManager {

	private final Map<String, CyLayoutAlgorithm> layoutMap;
	private final CyProperty<Properties> cyProps;

	public CyLayoutsImpl(final CyProperty<Properties> p, CyLayoutAlgorithm defaultLayout) {
		this.cyProps = p;
		layoutMap = new HashMap<String,CyLayoutAlgorithm>();
		addLayout(defaultLayout, new HashMap());
	}

	/**
	 * Add a layout to the layout manager's list.  If menu is "null"
	 * it will be assigned to the "none" menu, which is not displayed.
	 * This can be used to register layouts that are to be used for
	 * specific algorithmic purposes, but not, in general, supposed
	 * to be for direct user use.
	 *
	 * @param layout The layout to be added
	 * @param menu The menu that this should appear under
	 */
	public void addLayout(CyLayoutAlgorithm layout, Map props) {
		if ( layout != null )
			layoutMap.put(layout.getName(),layout);
	}

	/**
	 * Remove a layout from the layout maanger's list.
	 *
	 * @param layout The layout to remove
	 */
	public void removeLayout(CyLayoutAlgorithm layout, Map props) {
		if ( layout != null )
			layoutMap.remove(layout.getName());
	}

	/**
	 * Get the layout named "name".  If "name" does
	 * not exist, this will return null
	 *
	 * @param name String representing the name of the layout
	 * @return the layout of that name or null if it is not reigstered
	 */
	@Override
	public CyLayoutAlgorithm getLayout(String name) {
		if (name != null)
			return layoutMap.get(name);
		return null;
	}

	/**
	 * Get all of the available layouts.
	 *
	 * @return a Collection of all the available layouts
	 */
	@Override
	public Collection<CyLayoutAlgorithm> getAllLayouts() {
		return layoutMap.values();
	}

	/**
	 * Get the default layout.  This is either the grid layout or a layout
	 * chosen by the user via the setting of the "layout.default" property.
	 *
	 * @return CyLayoutAlgorithm to use as the default layout algorithm
	 */
	@Override
	public CyLayoutAlgorithm getDefaultLayout() {
		// See if the user has set the layout.default property	
		String defaultLayout = cyProps.getProperties().getProperty(CyLayoutAlgorithmManager.DEFAULT_LAYOUT_PROPERTY_NAME);
		if (defaultLayout == null || layoutMap.containsKey(defaultLayout) == false)
			defaultLayout = CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME; 

		return layoutMap.get(defaultLayout);
	}
}
