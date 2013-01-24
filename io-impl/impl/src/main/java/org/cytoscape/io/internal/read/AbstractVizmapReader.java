package org.cytoscape.io.internal.read;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.io.InputStream;
import java.util.Set;

import org.cytoscape.io.internal.util.vizmap.VisualStyleSerializer;
import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;


public abstract class AbstractVizmapReader extends AbstractTask implements VizmapReader {

    protected final InputStream inputStream;
    protected final VisualStyleSerializer visualStyleSerializer;
    protected Set<VisualStyle> visualStyles;
    
    public AbstractVizmapReader(InputStream inputStream, VisualStyleSerializer visualStyleSerializer) {
        if ( inputStream == null )
            throw new NullPointerException("InputStream is null");
        this.inputStream = inputStream;
        this.visualStyleSerializer = visualStyleSerializer;
    }

	@Override
	public Set<VisualStyle> getVisualStyles() {
		return visualStyles;
	}
}
