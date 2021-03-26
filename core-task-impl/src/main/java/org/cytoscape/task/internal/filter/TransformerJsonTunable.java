package org.cytoscape.task.internal.filter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.ValidatableTransformer;
import org.cytoscape.filter.model.ValidationWarning;
import org.cytoscape.io.read.CyTransformerReader;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;

public class TransformerJsonTunable {

	@Tunable
	public String json;
	
	
	public NamedTransformer<CyNetwork,CyIdentifiable> getTransformer(String name, CyTransformerReader transformerReader) {
		String wrapper = "[ { \"name\" : \"%s\", \"transformers\" : [ %s ] } ]";
		String fullJson = String.format(wrapper, name, json);
		byte[] bytes = fullJson.getBytes();
		
		try(InputStream in = new ByteArrayInputStream(bytes)) {
			@SuppressWarnings("unchecked")
			NamedTransformer<CyNetwork,CyIdentifiable>[] transformers = 
				(NamedTransformer<CyNetwork,CyIdentifiable>[]) transformerReader.read(in);
			if(transformers == null || transformers.length == 0 || transformers[0] == null) {
				return null;
			}
			return transformers[0];
		} catch(Exception e) {
			return null;
		}
	}
	
	
	public static boolean validate(NamedTransformer<CyNetwork,CyIdentifiable> namedTransformer, TaskMonitor taskMonitor) {
		boolean valid = true;
		for(Transformer<CyNetwork,CyIdentifiable> transformer : namedTransformer.getTransformers()) {
			if(transformer instanceof ValidatableTransformer) {
				ValidatableTransformer<CyNetwork,CyIdentifiable> validatableTransformer = (ValidatableTransformer<CyNetwork,CyIdentifiable>) transformer;
				List<ValidationWarning> warnings = validatableTransformer.validateCreation();
				if(warnings != null && !warnings.isEmpty()) {
					valid = false;
					for(ValidationWarning warning : warnings) {
						taskMonitor.showMessage(Level.ERROR, warning.getWarning());
					}
				}
			} else {
				valid = false;
			}
		}
		return valid;
	}
	
}
