package org.cytoscape.io.internal.read;

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
