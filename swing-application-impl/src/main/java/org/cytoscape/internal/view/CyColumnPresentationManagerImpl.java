package org.cytoscape.internal.view;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.swing.CyColumnPresentation;
import org.cytoscape.application.swing.CyColumnPresentationManager;

public class CyColumnPresentationManagerImpl implements CyColumnPresentationManager {

	private final Map<String,CyColumnPresentation> presentations = new HashMap<>();
	

	public void addPresentation(CyColumnPresentation presentation, Map<String,String> props) {
		String namespace = props.get(CyColumnPresentation.NAMESPACE);
		if(namespace != null) {
			presentations.put(namespace.toLowerCase(), presentation);
		}
	}
	
	public void removePresentation(CyColumnPresentation presentation, Map<String,String> props) {
		presentations.values().remove(presentation);
	}
	
	@Override
	public CyColumnPresentation getColumnPresentation(String namespace) {
		return presentations.get(namespace == null ? null : namespace.toLowerCase());
	}

}
