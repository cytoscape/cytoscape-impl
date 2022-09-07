package org.cytoscape.cg.internal.image;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.model.SVGCustomGraphics;
import org.cytoscape.cg.model.SVGLayer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

import com.google.common.hash.Hashing;

/**
 * This factory accepts SVG images from URLs, Data URLs (e.g. "data:image/svg+xml;utf8,&lt;svg ...&gt;...&lt;/svg&gt;")
 * or just raw SVG text.
 */
public class SVGCustomGraphicsFactory extends AbstractURLImageCustomGraphicsFactory<SVGLayer> {

	public static final String SUPPORTED_CLASS_ID =
			SVGCustomGraphics.TYPE_NAMESPACE + "." + SVGCustomGraphics.TYPE_NAME;
	
	public SVGCustomGraphicsFactory(CustomGraphicsManager manager, CyServiceRegistrar serviceRegistrar) {
		super(manager, serviceRegistrar);
	}

	@Override
	public boolean supportsMime(String mimeType) {
		return "image/svg+xml".equalsIgnoreCase(mimeType);
	}
	
	@Override
	public SVGCustomGraphics getInstance(String input) {
		try {
			URL url = null;
			String name = null;
			boolean isSVGText = false;
			
			if (isDataURL(input)) {
				var idx = input.indexOf(',');
				
				if (idx == -1 || idx >= input.length() - 1)
					return null;
				
				input = input.substring(idx + 1, input.length()).trim(); // This is now the SVG text only!
				isSVGText = true;
				
				// Create a name from this hash to try to reuse images that have already been parsed
				var sha = Hashing.sha256().hashString(input, StandardCharsets.UTF_8).toString();
				name = sha + ".svg";
				
				// Create a fake local URL so it can be stored in the manager
				var config = serviceRegistrar.getService(CyApplicationConfiguration.class);
				var dir = config.getConfigurationDirectoryLocation();
				var file = new File(dir, name);
				url = file.toURI().toURL();
			} else {
				name = ViewUtil.getShortName(input);
				url = new URL(input);
			}

			var cg = manager.getCustomGraphicsBySourceURL(url);

			if (cg instanceof SVGCustomGraphics == false) {
				var id = manager.getNextAvailableID();
				cg = isSVGText ? new SVGCustomGraphics(id, name, url, input) : new SVGCustomGraphics(id, input, url);

				manager.addCustomGraphics(cg, url);
			}

			return (SVGCustomGraphics) cg;
		} catch (IOException e) {
			return null;
		}
	}
	
	@Override
	protected SVGCustomGraphics createMissingImageCustomGraphics(String entryStr, long id, String sourceURL) {
		try {
			var cg = new MissingSVGCustomGraphics(entryStr, id, sourceURL, this);
			manager.addMissingImageCustomGraphics(cg);
			
			return cg;
		} catch (IOException e) {
			logger.error("Cannot create MissingSVGCustomGraphics object", e);
		}
		
		return null;
	}
	
	@Override
	public Class<? extends CyCustomGraphics<?>> getSupportedClass() {
		return SVGCustomGraphics.class;
	}
	
	@Override
	public String getSupportedClassId() {
		return SUPPORTED_CLASS_ID;
	}
	
	private boolean isDataURL(String s) {
		return s.startsWith("data:image/svg+xml");
	}
}
