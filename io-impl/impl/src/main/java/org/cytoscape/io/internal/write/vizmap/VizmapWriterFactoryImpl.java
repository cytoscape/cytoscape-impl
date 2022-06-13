package org.cytoscape.io.internal.write.vizmap;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.io.OutputStream;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.vizmap.VisualStyleSerializer;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.view.vizmap.StyleAssociation;
import org.cytoscape.view.vizmap.VisualStyle;

public class VizmapWriterFactoryImpl extends AbstractCyWriterFactory implements VizmapWriterFactory {

	private final VisualStyleSerializer visualStyleSerializer;

    public VizmapWriterFactoryImpl(CyFileFilter fileFilter, VisualStyleSerializer visualStyleSerializer) {
        super(fileFilter);
        this.visualStyleSerializer = visualStyleSerializer;
    }

    @Override
    public CyWriter createWriter(OutputStream os, Set<VisualStyle> networkStyles) {
        return new VizmapWriterImpl(os, visualStyleSerializer, networkStyles, null, null);
    }
    
    @Override
    public CyWriter createWriter(OutputStream os, Set<VisualStyle> networkStyles, Set<VisualStyle> tableStyles) {
    	return new VizmapWriterImpl(os, visualStyleSerializer, networkStyles, tableStyles, null);
    }
    
    @Override
    public CyWriter createWriter(OutputStream os, Set<VisualStyle> networkStyles, Set<VisualStyle> tableStyles, Set<StyleAssociation> columnStyleAssociations) {
    	return new VizmapWriterImpl(os, visualStyleSerializer, networkStyles, tableStyles, columnStyleAssociations);
    }
}
