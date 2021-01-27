package org.cytoscape.cg.internal.image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

import com.google.common.hash.Hashing;

/**
 * This factory accepts bitmap images from URLs or Base64 Data URLs (e.g. "data:image/jpeg;base64,LzlqLzRBQ...").
 */
public class BitmapCustomGraphicsFactory extends AbstractURLImageCustomGraphicsFactory<BitmapLayer> {

	public static final String SUPPORTED_CLASS_ID =
			BitmapCustomGraphics.TYPE_NAMESPACE + "." + BitmapCustomGraphics.TYPE_NAME;
	
	private final List<String> MIME_TYPES = List.of(
			"image/bmp",
			"image/x-windows-bmp",
			"image/gif",
			"image/jpeg",
			"image/png",
			"image/vnd.wap.wbmp"
	);
	
	public BitmapCustomGraphicsFactory(CustomGraphicsManager manager, CyServiceRegistrar serviceRegistrar) {
		super(manager, serviceRegistrar);
	}

	@Override
	public boolean supportsMime(String mimeType) {
		return MIME_TYPES.contains(mimeType);
	}
	
	@Override
	public BitmapCustomGraphics getInstance(String input) {
		URL url = null;
		final String name;
		boolean isBase64 = isBase64DataURL(input);

		try {
			if (isBase64) {
				// Create a fake local URL so it can be stored in the manager
				var config = serviceRegistrar.getService(CyApplicationConfiguration.class);
				var dir = config.getConfigurationDirectoryLocation();
				
				var parts = input.split(",");
				
				if (parts.length < 2 || parts[1].isBlank())
					return null;
				
				input = parts[1].trim(); // This is now the base64 content only!
				
				// Create a name from this hash to try to reuse images that have already been parsed
				var sha = Hashing.sha256().hashString(input, StandardCharsets.UTF_8).toString();
				var ext = getExtensionFromDataURL(input);
				name = sha + "." + ext;
				var file = new File(dir, name);
				
				url = file.toURI().toURL();
			} else {
				name = input;
				url = new URL(input);
			}

			var cg = manager.getCustomGraphicsBySourceURL(url);
			
			if (cg instanceof BitmapCustomGraphics == false) {
				var id = manager.getNextAvailableID();

				if (isBase64) {
					var decoder = Base64.getDecoder();
					var imgBytes = decoder.decode(input);
					var bis = new ByteArrayInputStream(imgBytes);
					var img = ImageIO.read(bis);
					bis.close();
					
					cg = new BitmapCustomGraphics(id, name, url, img);
				} else {
					cg = new BitmapCustomGraphics(id, name, url);
				}

				manager.addCustomGraphics(cg, url);
			}

			return (BitmapCustomGraphics) cg;
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	protected BitmapCustomGraphics createMissingImageCustomGraphics(String entryStr, long id, String sourceURL) {
		try {
			var cg = new MissingBitmapCustomGraphics(entryStr, id, sourceURL, this);
			manager.addMissingImageCustomGraphics(cg);
			
			return cg;
		} catch (IOException e) {
			logger.error("Cannot create MissingBitmapCustomGraphics object", e);
		}
		
		return null;
	}
	
	@Override
	public Class<? extends CyCustomGraphics<?>> getSupportedClass() {
		return BitmapCustomGraphics.class;
	}
	
	@Override
	public String getSupportedClassId() {
		return SUPPORTED_CLASS_ID;
	}
	
	private String getExtensionFromDataURL(String s) {
        var parts = s.split(",");
        
        switch (parts[0]) {
            case "data:image/gif;base64":
                return "gif";
            case "data:image/bmp;base64":
                return "bmp";
            case "data:image/png;base64":
            	return "png";
            case "data:image/jpeg;base64":
            default:
            	return "jpg";
        }
	}
	
	private boolean isBase64DataURL(String s) {
		return s.startsWith("data:") && s.contains(";base64,");
	}
}
