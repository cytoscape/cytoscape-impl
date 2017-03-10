package org.cytoscape.filter.internal.work;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.filter.internal.work.ValidationViewListener.ValidationEvent;
import org.cytoscape.filter.model.TransformerListener;
import org.cytoscape.filter.model.ValidatableTransformer;
import org.cytoscape.filter.model.ValidationWarning;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class ValidationManager implements SetCurrentNetworkListener{

	private CyNetwork network;
	
	private Map<ValidatableTransformer<CyNetwork,CyIdentifiable>, ValidationViewListener> listeners = new HashMap<>();
	
	
	private class TransformerValidationListener implements TransformerListener {
		private ValidatableTransformer<CyNetwork,CyIdentifiable> transformer;
		
		public TransformerValidationListener(ValidatableTransformer<CyNetwork,CyIdentifiable> transformer) {
			this.transformer = transformer;
		}

		@Override
		public void handleSettingsChanged() {
			runValidation(transformer);
		}

		@Override
		public int hashCode() {
			return transformer.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof TransformerValidationListener) {
				return ((TransformerValidationListener)obj).transformer.equals(transformer);
			}
			return false;
		}
	}
	
	
	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		this.network = e.getNetwork();
		runValidation();
	}
	
	public void register(ValidatableTransformer<CyNetwork,CyIdentifiable> transformer, ValidationViewListener listener) {
		Object prev = listeners.put(transformer, listener);
		if(prev == null) {
			transformer.addListener(new TransformerValidationListener(transformer));
		}
	}
	
	public void unregister(ValidatableTransformer<CyNetwork,CyIdentifiable> transformer) {
		Object prev = listeners.remove(transformer);
		if(prev != null) {
			transformer.removeListener(new TransformerValidationListener(transformer)); // this works because of equals() method in TransformerValidationListener
		}
	}
	
	public void runValidation() {
		listeners.keySet().forEach(this::runValidation);
	}
	
	private void runValidation(ValidatableTransformer<CyNetwork,CyIdentifiable> transformer) {
		ValidationViewListener listener = listeners.get(transformer);
		List<ValidationWarning> warnings = transformer.validate(network);
		if(listener != null) {
			listener.handleValidated(new ValidationEvent(warnings));
		}
	}
	
	@Override
	public String toString() {
		String prefix = "ValidationManager (" + listeners.size() + ") {";
		return listeners.keySet().stream().map(t -> t.getClass().getSimpleName()).collect(Collectors.joining(",", prefix, "}"));
	}
	
}
