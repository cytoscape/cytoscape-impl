package org.cytoscape.app.internal.manager;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.cytoscape.app.internal.exception.AppParsingException;
import org.cytoscape.app.internal.util.DebugHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public static final String APP_CLASS_TAG = "Cytoscape-App";
	
	/**
	 * The name of the key in the app jar's manifest file indicating the human-readable
	 * name of the app
	 */
	public static final String APP_READABLE_NAME_TAG = "Cytoscape-App-Name";

	/**
	 * The name of the key in the app jar's manifest file indicating the version of the app
	 * in the format major.minor.patch[-tag], eg. 3.0.0-SNAPSHOT or 1.2.3
	 */
	public static final String APP_VERSION_TAG = "Cytoscape-App-Version";
	
	/**
	 * The name of the key in the app jar's manifest file indicating the major versions of
	 * Cytoscape that the app is known to be compatible with in comma-delimited form
	 */
	public static final String APP_COMPATIBLE_TAG = "Cytoscape-API-Compatibility";
	
	/**
	 * A regular expression representing valid app versions, which are in the format major.minor[.patch][-tag],
	 * eg. 3.0.0-SNAPSHOT, or 3.0.
	 */
	public static final Pattern APP_VERSION_TAG_REGEX = Pattern.compile("(0|([1-9]+\\d*))\\.(\\d)+(\\.(\\d)+)?(.*)?");

	
	/**
	 * A regular expression representing valid values for the entry containing the major versions of Cytoscape
	 * that the app is known to work with in, in comma-delimited form. Examples that work are "3.0, 3.1"  or "2, 3.0".
	 * Examples that do not match are "1.0b" and "v1, v2", as these contain non-digit characters.
	 */
	private static final String APP_COMPATIBLE_TAG_REGEX = "(\\d+(\\.\\d+)?\\s*)(,\\s*\\d+(\\.\\d+)?\\s*)*";
	
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	
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
		JarFile jarFile;
		try {
			jarFile = new JarFile(file);
		} catch (IOException e) {
			throw new AppParsingException("Error parsing given file as a jar file: " + e.getMessage());
		}
		
		boolean bundleApp = false;
		boolean xmlParseFailed = false;
		boolean osgiMetadataFound = false; // Treat the jar as an OSGi bundle if OSGi metadata is found
		
	
		// Look for features specified in an xml file
		/*
		List<KarafArchiveApp.KarafFeature> featuresList = Collections.emptyList();
        try {
            featuresList = getFeaturesXmlFromJar(jarFile);
        } catch (AppParsingException e) {
            xmlParseFailed = true;
        }
        */
		
		// Check if a manifest that contains OSGi metadata is present
		try {
			Manifest osgiManifest = jarFile.getManifest();
			
			if (osgiManifest != null) {
				if (osgiManifest.getMainAttributes().getValue("Bundle-SymbolicName") != null) {

					osgiMetadataFound = true;
				}
			}
			
		} catch (IOException e) {
		}
		
		// If an XML parsing error occurred, continue to attempt to parse the app as a simple app
		/*
		 * 
		 * Commented out, no longer uses Karaf. see if (osgiMetadataFound) block 
		 * below this commented block.
		
		if (featuresList.size() > 0 && !xmlParseFailed) {
			bundleApp = true;
			parsedApp = new KarafArchiveApp();
			
			for (KarafArchiveApp.KarafFeature feature: featuresList) {
				((KarafArchiveApp) parsedApp).getFeaturesList().put(feature.featureName, feature);
			}
		} else if (osgiMetadataFound) {
			bundleApp = true;
			parsedApp = new BundleApp();
		}
		*/
		
		if (osgiMetadataFound) {
			bundleApp = true;
			parsedApp = new BundleApp();
		}
		
		// Attempt to obtain manifest file from jar
		Manifest manifest = null;
		try {
			manifest = jarFile.getManifest();
		} catch (IOException e) {
			throw new AppParsingException("Error obtaining manifest from app jar", e);
		} finally {
			try {
				jarFile.close();
			} catch (IOException e) {
				throw new AppParsingException("Error closing file", e);
			}
		}
		
		// Make sure the getManifest() call didn't return null
		if (manifest == null) {
			throw new AppParsingException("No manifest was found in the jar file.");
		}
		
		// Bundle apps are instantiated by OSGi using their activator classes
		String entryClassName;
		if (!bundleApp) {
			// Obtain the fully-qualified name of the class to instantiate upon app installation
			entryClassName = manifest.getMainAttributes().getValue(APP_CLASS_TAG);
			if (entryClassName == null || entryClassName.trim().length() == 0) {
				throw new AppParsingException("Jar is missing value for entry " + APP_CLASS_TAG + " in its manifest file.");
			}
		}
		else {
			// Obtain the fully-qualified name of the class to instantiate upon app installation
			entryClassName = manifest.getMainAttributes().getValue("Bundle-Activator");
			if (entryClassName == null || entryClassName.trim().length() == 0) {
				throw new AppParsingException("Jar is missing value for entry Bundle-Activator");
			}
		}
		
		// Obtain the human-readable name of the app
		String readableName;
		if (!bundleApp) {
			readableName = manifest.getMainAttributes().getValue(APP_READABLE_NAME_TAG);
			
			if (readableName == null || readableName.trim().length() == 0) {
				throw new AppParsingException("Jar is missing value for entry " + APP_READABLE_NAME_TAG + " in its manifest file.");
			}
		} else {
			readableName = manifest.getMainAttributes().getValue("Bundle-Name");
			
			if (readableName == null || readableName.trim().length() == 0) {
				readableName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
			}
			
			if (readableName == null || readableName.trim().length() == 0) {
				throw new AppParsingException("Bundle jar manifest had no entry for Bundle-Name, and no entry for Bundle-SymbolicName");
			}
		}
		
		// Obtain the version of the app, in major.minor.patch[-tag] format, ie. 3.0.0-SNAPSHOT or 1.2.3
		String appVersion;
		if (!bundleApp) {
			appVersion = manifest.getMainAttributes().getValue(APP_VERSION_TAG);
			
			if (appVersion == null || appVersion.trim().length() == 0) {
				throw new AppParsingException("Jar is missing value for entry " + APP_VERSION_TAG + " in its manifiest file.");
			} else if (!APP_VERSION_TAG_REGEX.matcher(appVersion).matches()) {
				throw new AppParsingException("The app version specified in its manifest file under the key " + APP_VERSION_TAG
						+ " was found to not match the format major.minor[.patch][-tag], eg. 2.1, 2.1-test, 3.0.0 or 3.0.0-SNAPSHOT");
			}
		} else {
			appVersion = manifest.getMainAttributes().getValue("Bundle-Version");
			
			if (appVersion == null || appVersion.trim().length() == 0) {
				
				// For now, while it hasn't been decided, accept values for Cytoscape-App-Version if Bundle-Version is not found
				appVersion = manifest.getMainAttributes().getValue(APP_VERSION_TAG);
				
				if (appVersion == null || appVersion.trim().length() == 0) {
					throw new AppParsingException("Bundle jar manifest has no entry for Bundle-Version");
				}
			}
		}
		String compatibleVersions;
		if (bundleApp) {
			compatibleVersions = manifest.getMainAttributes().getValue("Import-Package");
			if (compatibleVersions == null || compatibleVersions.trim().length() == 0) {
				throw new AppParsingException("Jar is missing value for entry Import-Package in its manifest file.");
			}
		} else {
			compatibleVersions = manifest.getMainAttributes().getValue(APP_COMPATIBLE_TAG);

			if (compatibleVersions == null || compatibleVersions.trim().length() == 0) {
				// For now, accept the deprecated field Cytoscape-App-Works-With if the official field was not found
				compatibleVersions = manifest.getMainAttributes().getValue("Cytoscape-App-Works-With");
			}

			if (compatibleVersions == null || compatibleVersions.trim().length() == 0) {
				throw new AppParsingException("Jar is missing value for entry " + APP_COMPATIBLE_TAG + " in its manifest file.");
			}
			else if (!compatibleVersions.matches(APP_COMPATIBLE_TAG_REGEX)) {
				throw new AppParsingException("The known compatible versions of Cytoscape specified in the manifest under the"
						+ " key " + APP_COMPATIBLE_TAG + " does not match the form of a comma-delimited list of versions of the form"
						+ " major[.minor] (eg. 1 or 1.0) with variable whitespace around versions");
			}
		}

		List<App.Dependency> deps = null;
		final String depsStr = manifest.getMainAttributes().getValue("Cytoscape-App-Dependencies");
		if (depsStr != null && depsStr.trim().length() != 0) {
			deps = new ArrayList<App.Dependency>();
			final String[] depsPieces = splitSmart(',', depsStr);
			for (final String depPiece : depsPieces) {
				final String[] depPieces = splitSmart(';', depPiece);
				if (depPieces.length < 2)
					throw new AppParsingException("Each dependency must have a name and version");
				deps.add(new App.Dependency(depPieces[0], depPieces[1]));
			}
		}	
		
		String fileHash;
		try {
			fileHash = getChecksum(file);
			parsedApp.setSha512Checksum(fileHash);
		} catch (ChecksumException e) {
			parsedApp.setSha512Checksum(null);
		}
		
		parsedApp.setAppFile(file);
		parsedApp.setAppName(readableName);
		parsedApp.setEntryClassName(entryClassName);
		parsedApp.setVersion(appVersion);
		parsedApp.setCompatibleVersions(compatibleVersions);
		parsedApp.setAppValidated(true);
		parsedApp.setDependencies(deps);
		
		return parsedApp;
	}
    
    public class ChecksumException extends Exception {
		private static final long serialVersionUID = 7022699404764909882L;
		
		public ChecksumException(String text) {
			super(text);
		}
	}
	
	/**
	 * Obtain the SHA-512 checksum of a file, in the following format: sha512:3c1c..
	 * @param file The file to find the checksum
	 * @return The SHA-512 checksum, in format: sha512:e1..
	 * @throws ChecksumException If unable to obtain SHA-512 algorithm implementation,
	 * file does not exist, or IO error while reading
	 */
	public String getChecksum(File file) throws ChecksumException {
		MessageDigest messageDigest;
		 
		try {
			messageDigest = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			throw new ChecksumException("Unable to obtain SHA-512 algorithm implementation");
		}

		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new ChecksumException("File " + file.getAbsolutePath() + " does not exist.");
		}
		 
		try {
			inputStream = new DigestInputStream(inputStream, messageDigest);
			
			byte[] byteBuffer = new byte[128];
			
			while (inputStream.available() != 0) {
				inputStream.read(byteBuffer);
			}
		} catch (IOException e) {
			throw new ChecksumException("Error reading from file " + file + ", " + e.getMessage());
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// logger.warn("Failed to close input stream on file " + file.getAbsolutePath() + ", " + e.getMessage());
			}
		}
		
		byte[] digest = messageDigest.digest();
		
		String result = "";
		for (int i = 0; i < digest.length; i++) {
			// Convert each byte to a 2-digit hex number, adding 0x100 to obtain the 0 if the byte is 0E
			result += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
		}
		
		return "sha512:" + result;
	}

	private static int findChar(final char lookForThis, final String inThis, final int startingAt) {
		final int len = inThis.length();
		for (int i = startingAt; i < len; i++) {
			char c = inThis.charAt(i);
			if (c == '\"') {
				for (++i /* skip over first quote */; i < len; i++) {
					c = inThis.charAt(i);
          if (c == '\"') // break at next quote
            break;
        }
      } else if (lookForThis == c) {
      	return i;
      }
    }
    return len;
  }

  private static String[] splitSmart(final char splitBy, final String whatToSplit) {
  	final int len = whatToSplit.length();
  	int numPieces = 0;
  	for (int i = 0; i < len;) {
  		i = findChar(splitBy, whatToSplit, i) + 1;
  		numPieces++;
  	}

  	final String[] pieces = new String[numPieces];
  	int pi = 0;
  	for (int i = 0; i < len;) {
  		final int j = findChar(splitBy, whatToSplit, i);
  		String piece = whatToSplit.substring(i, j);
  		piece = piece.trim();
  		if (piece.charAt(0) == '"' && piece.charAt(piece.length() - 1) == '"') {
  			piece = piece.substring(1, piece.length() - 1);
  			piece = piece.trim();
  		}
  		pieces[pi++] = piece;
  		i = j + 1;
  	}
  	return pieces;
  }
}
