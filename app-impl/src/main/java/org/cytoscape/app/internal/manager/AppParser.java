package org.cytoscape.app.internal.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.cytoscape.app.internal.exception.AppParsingException;
import org.cytoscape.app.internal.util.DebugHelper;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class represents an app parser that is capable of parsing given {@link File}
 * objects to {@link App} objects, as well as reporting problems found while attempting
 * to verify the app.
 */
public class AppParser {
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
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
		} catch (IOException e) {
			throw new AppParsingException("Error parsing given file as a jar file: " + e.getMessage());
		}
		
		boolean bundleApp = false;
		boolean xmlParseFailed = false;
		boolean osgiMetadataFound = false; // Treat the jar as an OSGi bundle if OSGi metadata is found
		
	
		// Look for features specified in an xml file
		List<KarafArchiveApp.KarafFeature> featuresList = Collections.emptyList();
        try {
            featuresList = getFeaturesXmlFromJar(jarFile);
        } catch (AppParsingException e) {
            xmlParseFailed = true;
        }
		
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
		
		String entryClassName = null;
		// Bundle apps are instantiated by OSGi using their activator classes
		if (!bundleApp) {
			// Obtain the fully-qualified name of the class to instantiate upon app installation
			entryClassName = manifest.getMainAttributes().getValue(SimpleAppOld.APP_CLASS_TAG);
			if (entryClassName == null || entryClassName.trim().length() == 0) {
				throw new AppParsingException("Jar is missing value for entry " + SimpleAppOld.APP_CLASS_TAG + " in its manifest file.");
			}
		}
		
		// Obtain the human-readable name of the app
		String readableName = null;
		if (!bundleApp) {
			readableName = manifest.getMainAttributes().getValue(SimpleAppOld.APP_READABLE_NAME_TAG);
			
			if (readableName == null || readableName.trim().length() == 0) {
				throw new AppParsingException("Jar is missing value for entry " + SimpleAppOld.APP_READABLE_NAME_TAG + " in its manifest file.");
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
		String appVersion = null;
		if (!bundleApp) {
			appVersion = manifest.getMainAttributes().getValue(SimpleAppOld.APP_VERSION_TAG);
			
			if (appVersion == null || appVersion.trim().length() == 0) {
				throw new AppParsingException("Jar is missing value for entry " + SimpleAppOld.APP_VERSION_TAG + " in its manifiest file.");
			} else if (!SimpleAppOld.APP_VERSION_TAG_REGEX.matcher(appVersion).matches()) {
				throw new AppParsingException("The app version specified in its manifest file under the key " + SimpleAppOld.APP_VERSION_TAG
						+ " was found to not match the format major.minor[.patch][-tag], eg. 2.1, 2.1-test, 3.0.0 or 3.0.0-SNAPSHOT");
			}
		} else {
			appVersion = manifest.getMainAttributes().getValue("Bundle-Version");
			
			if (appVersion == null || appVersion.trim().length() == 0) {
				
				// For now, while it hasn't been decided, accept values for Cytoscape-App-Version if Bundle-Version is not found
				appVersion = manifest.getMainAttributes().getValue(SimpleAppOld.APP_VERSION_TAG);
				
				if (appVersion == null || appVersion.trim().length() == 0) {
					throw new AppParsingException("Bundle jar manifest has no entry for Bundle-Version");
				}
			}
		}
		
		String compatibleVersions = manifest.getMainAttributes().getValue(SimpleAppOld.APP_COMPATIBLE_TAG);
		if (compatibleVersions == null || compatibleVersions.trim().length() == 0) {
            if (bundleApp) {
                logger.info("Bundle app " + file.getName() + " manifest does not contain the entry \"" + SimpleAppOld.APP_COMPATIBLE_TAG
                        + "\". Assuming default value 3.0.");
                compatibleVersions = "3.0";
            } else {
                throw new AppParsingException("Jar is missing value for entry " + SimpleAppOld.APP_COMPATIBLE_TAG + " in its manifest file.");
            }
		} else if (!compatibleVersions.matches(APP_COMPATIBLE_TAG_REGEX)) {
			throw new AppParsingException("The known compatible versions of Cytoscape specified in the manifest under the"
					+ " key " + SimpleAppOld.APP_COMPATIBLE_TAG + " does not match the form of a comma-delimited list of versions of the form"
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

    private static List<KarafArchiveApp.KarafFeature> getFeaturesXmlFromJar(final JarFile jarFile) throws AppParsingException
    {
        final List<KarafArchiveApp.KarafFeature> featuresList = new LinkedList<KarafArchiveApp.KarafFeature>();
		final Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry jarEntry = entries.nextElement();
			
			if (jarEntry.getName().endsWith("xml")) {

				try {
					SAXParserFactory spf = SAXParserFactory.newInstance();
				    spf.setNamespaceAware(true);
				    XMLReader xmlReader = spf.newSAXParser().getXMLReader();

				    xmlReader.setContentHandler(new ContentHandler() {
						
				    	private Stack<String> qNames = new Stack<String>();
				    	
						@Override
						public void startPrefixMapping(String arg0, String arg1) throws SAXException { }
						
						@Override
						public void startElement(String uri, String localName, String qName,
								Attributes atts) throws SAXException {
							
							qNames.push(qName);

							if (qNames.size() == 2
									&& qNames.get(0).equalsIgnoreCase("features")
									&& qNames.get(1).equalsIgnoreCase("feature")) {
								
								KarafArchiveApp.KarafFeature feature = new KarafArchiveApp.KarafFeature();
								
								// Obtain the feature name and version
								feature.featureName = atts.getValue("name");
								feature.featureVersion = atts.getValue("version");
								
								featuresList.add(feature);
							}
						}
						
						@Override
						public void startDocument() throws SAXException { }
						
						@Override
						public void skippedEntity(String arg0) throws SAXException { }
						
						@Override
						public void setDocumentLocator(Locator arg0) { }
						
						@Override
						public void processingInstruction(String arg0, String arg1) throws SAXException { }
						
						@Override
						public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException { }
						
						@Override
						public void endPrefixMapping(String arg0) throws SAXException { }
						
						@Override
						public void endElement(String arg0, String arg1, String arg2) throws SAXException {
							qNames.pop();
						}
						
						@Override
						public void endDocument() throws SAXException { }
						
						@Override
						public void characters(char[] arg0, int arg1, int arg2) throws SAXException { }
					});
				    
				    InputStream inputStream = null;
				    try {
				    	inputStream = jarFile.getInputStream(jarEntry);
				    	xmlReader.parse(new InputSource(inputStream));
				    	
				    } finally {
				    	if (inputStream != null) {
				    		inputStream.close();
				    	}
				    }
				    
				} catch (SAXException e) {
					throw new AppParsingException("Failed to read features.xml", e);
				} catch (IOException e) {
					throw new AppParsingException("Failed to read features.xml", e);
				} catch (ParserConfigurationException e) {
					throw new AppParsingException("Failed to read features.xml", e);
				}
			}
		}

        return featuresList;
    }
}
