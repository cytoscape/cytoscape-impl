package org.cytoscape.app.internal.manager;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.cytoscape.app.internal.exception.AppParsingException;
import org.cytoscape.app.internal.util.DebugHelper;

/**
 * This class represents an app parser that is capable of parsing given {@link File}
 * objects to {@link App} objects, as well as reporting problems found while attempting
 * to verify the app.
 */
public class AppParser {
	/** 
	 * The name of the key in the app jar's manifest file that indicates the fully-qualified name 
	 * of the class to instantiate upon app installation. 
	 * */
	private static final String APP_CLASS_TAG = "Cytoscape-App";
	
	/**
	 * The name of the key in the app jar's manifest file indicating the human-readable
	 * name of the app
	 */
	private static final String APP_READABLE_NAME_TAG = "Cytoscape-App-Name";
	
	/**
	 * The name of the key in the app jar's manifest file indicating the version of the app
	 * in the format major.minor.patch[-tag], eg. 3.0.0-SNAPSHOT or 1.2.3
	 */
	private static final String APP_VERSION_TAG = "Cytoscape-App-Version";
	
	/**
	 * The name of the key in the app jar's manifest file indicating the major versions of
	 * Cytoscape that the app is known to be compatible with in comma-delimited form
	 */
	private static final String APP_COMPATIBLE_TAG = "Cytoscape-App-Works-With";
	
	/**
	 * A regular expression representing valid app versions, which are in the format major.minor[.patch][-tag],
	 * eg. 3.0.0-SNAPSHOT, or 3.0.
	 */
	private static final String APP_VERSION_TAG_REGEX = "(0|([1-9]+\\d*))\\.(\\d)+(\\.(\\d)+)?(-.*)?";
	
	/**
	 * A regular expression representing valid values for the entry containing the major versions of Cytoscape
	 * that the app is known to work with in, in comma-delimited form. Examples that work are "3.0, 3.1"  or "2, 3.0".
	 * Examples that do not match are "1.0b" and "v1, v2", as these contain non-digit characters.
	 */
	private static final String APP_COMPATIBLE_TAG_REGEX = "(\\d+(\\.\\d+)?\\s*)(,\\s*\\d+(\\.\\d+)?\\s*)*";
	
	/**
	 * Attempt to parse a given {@link File} object as an {@link App} object.
	 * @param file The file to use for parsing
	 * @return An {@link App} object representing the given file if parsing was successful
	 * @throws AppParsingException If there was an error during parsing, such as missing data from the manifest file
	 */
	public App parseApp(File file) throws AppParsingException {
		App parsedApp = new SimpleApp();
		
		DebugHelper.print("Parsing: " + file.getPath());
		
		if (!file.exists()) {
			throw new AppParsingException("No file with path: " + file.getAbsolutePath());
		}
		
		if (!file.isFile()) {
			throw new AppParsingException("The given file, " + file + ", is not a file.");
		}
		
		// Attempt to parse the file as a jar file
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
		} catch (IOException e) {
			throw new AppParsingException("Error parsing given file as a jar file: " + e.getMessage());
		}
		
		// Attempt to obtain manifest file from jar
		Manifest manifest = null;
		try {
			manifest = jarFile.getManifest();
		} catch (IOException e) {
			throw new AppParsingException("Error obtaining manifest from app jar: " + e.getMessage());
		}
		
		// Make sure the getManifest() call didn't return null
		if (manifest == null) {
			throw new AppParsingException("No manifest was found in the jar file.");
		}
		
		// Obtain the fully-qualified name of the class to instantiate upon app installation
		String entryClassName = manifest.getMainAttributes().getValue(APP_CLASS_TAG);
		if (entryClassName == null || entryClassName.trim().length() == 0) {
			throw new AppParsingException("Jar is missing value for entry " + APP_CLASS_TAG + " in its manifest file.");
		}
		
		// Obtain the human-readable name of the app
		String readableName = manifest.getMainAttributes().getValue(APP_READABLE_NAME_TAG);
		if (readableName == null || readableName.trim().length() == 0) {
			throw new AppParsingException("Jar is missing value for entry " + APP_READABLE_NAME_TAG + " in its manifest file.");
		}
		
		// Obtain the version of the app, in major.minor.patch[-tag] format, ie. 3.0.0-SNAPSHOT or 1.2.3
		String appVersion = manifest.getMainAttributes().getValue(APP_VERSION_TAG);
		if (appVersion == null || appVersion.trim().length() == 0) {
			throw new AppParsingException("Jar is missing value for entry " + APP_VERSION_TAG + " in its manifiest file.");
		} else if (!appVersion.matches(APP_VERSION_TAG_REGEX)) {
			throw new AppParsingException("The app version specified in its manifest file under the key " + APP_VERSION_TAG
					+ " was found to not match the format major.minor[.patch][-tag], eg. 2.1, 2.1-test, 3.0.0 or 3.0.0-SNAPSHOT");
		}
		
		String compatibleVersions = manifest.getMainAttributes().getValue(APP_COMPATIBLE_TAG);
		if (compatibleVersions == null || compatibleVersions.trim().length() == 0) {
			throw new AppParsingException("Jar is missing value for entry " + APP_COMPATIBLE_TAG + " in its manifest file.");
		} else if (!compatibleVersions.matches(APP_COMPATIBLE_TAG_REGEX)) {
			throw new AppParsingException("The known compatible versions of Cytoscape specified in the manifest under the"
					+ " key " + APP_COMPATIBLE_TAG + " does not match the form of a comma-delimited list of versions of the form"
					+ " major[.minor] (eg. 1 or 1.0) with variable whitespace around versions");
		}
		
		parsedApp.setAppFile(file);
		parsedApp.setEntryClassName(entryClassName);
		parsedApp.setAppName(readableName);
		parsedApp.setVersion(appVersion);
		parsedApp.setCompatibleVersions(compatibleVersions);
		parsedApp.setAppValidated(true);
		
		return parsedApp;
	}
}
