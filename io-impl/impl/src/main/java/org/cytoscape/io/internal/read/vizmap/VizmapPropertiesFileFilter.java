package org.cytoscape.io.internal.read.vizmap;

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

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.read.properties.PropertiesFileFilter;
import org.cytoscape.io.util.StreamUtil;

public class VizmapPropertiesFileFilter extends PropertiesFileFilter {


    public VizmapPropertiesFileFilter(Set<String> extensions, Set<String> contentTypes,
            String description, DataCategory category, StreamUtil streamUtil) {
        super(extensions, contentTypes, description, category, streamUtil);
    }

    public VizmapPropertiesFileFilter(String[] extensions, String[] contentTypes,
            String description, DataCategory category, StreamUtil streamUtil) {
        super(extensions, contentTypes, description, category, streamUtil);
    }

    @Override
    public boolean accepts(InputStream stream, DataCategory category) {

        // Check data category
        if (category != this.category)
            return false;
        
        final String header = this.getHeader(stream, 20);

        if (header.contains("<vizmap"))
            return false;
        
        return true;
    }
}
