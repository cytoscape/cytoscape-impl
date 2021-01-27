package org.cytoscape.cg.internal.image;

import java.net.URL;

import org.cytoscape.cg.model.AbstractDCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

public abstract class AbstractURLImageCustomGraphics<T extends CustomGraphicLayer> extends AbstractDCustomGraphics<T> {
	
	static final float DEF_FIT_RATIO = 1.0f;
	
	protected URL sourceUrl;
	
	protected AbstractURLImageCustomGraphics(Long id, String displayName) {
		super(id, displayName);
	}
	
	protected AbstractURLImageCustomGraphics(Long id, String displayName, URL url) {
		this(id, displayName);
		sourceUrl = url;
		fitRatio = DEF_FIT_RATIO;
		
		// Special case.  We include a number of images as part of our bundles.  The
		// resulting URL's are not really helpful, so we need to massage the displayName here.
		if (displayName.startsWith("bundle:")) {
			int index = displayName.lastIndexOf("/");
			displayName = displayName.substring(index+1);
		}
	}
	
	public URL getSourceURL() {
		return sourceUrl;
	}

	@Override
	public String toSerializableString() {
		if (sourceUrl != null)
			return makeSerializableString(sourceUrl.toString());

		return makeSerializableString(displayName);
	}

	@Override
	public String toString() {
		if (sourceUrl == null && displayName == null)
			return "Empty image";
		
		if (sourceUrl != null && !sourceUrl.toString().startsWith("bundle"))
			return sourceUrl.toString();

		return displayName;
	}
	
	/** Used to create the serializable key. */
	public abstract String getTypeNamespace();
	
	/** The short name of the implementation. Used to create the serializable key. */
	public abstract String getTypeName();
	
	/** Used to create the serializable key. Includes the namespace given by {@link #getTypeNamespace()} */
	@Override
	public String getTypeFullName() {
		return getTypeNamespace() + "." + getTypeName();
	}
}
