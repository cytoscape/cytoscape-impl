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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.io.internal.read.AbstractVizmapReader;
import org.cytoscape.io.internal.util.vizmap.VisualStyleSerializer;
import org.cytoscape.io.internal.util.vizmap.model.Vizmap;
import org.cytoscape.work.TaskMonitor;

public class VizmapXMLReader extends AbstractVizmapReader {

    private static final String VIZMAP_PACKAGE = Vizmap.class.getPackage().getName();

    public VizmapXMLReader(InputStream is, VisualStyleSerializer visualStyleSerializer) {
        super(is, visualStyleSerializer);
    }

    public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
        // No idea why, but ObjectFactory doesn't get picked up in the default
        // Thread.currentThread().getContextClassLoader() classloader, whereas 
        // that approach works fine for bookmarks.  Anyway, just force the issue
        // by getting this classloader.
        final JAXBContext jaxbContext = JAXBContext.newInstance(VIZMAP_PACKAGE, getClass().getClassLoader());
		tm.setProgress(0.1);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		tm.setProgress(0.2);
        Vizmap vizmap = (Vizmap) unmarshaller.unmarshal(inputStream);
		tm.setProgress(0.7);
        this.visualStyles = visualStyleSerializer.createVisualStyles(vizmap);
		tm.setProgress(1.0);
    }
}
