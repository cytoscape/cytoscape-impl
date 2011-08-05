package org.cytoscape.io.internal.read;


import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.io.read.VizmapReaderManager;


public class VizmapReaderManagerImpl extends GenericReaderManager<InputStreamTaskFactory, VizmapReader> 
    implements VizmapReaderManager {

    public VizmapReaderManagerImpl() {
        super(DataCategory.VIZMAP);
    }
}