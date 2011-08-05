/**
 *
 */
package org.cytoscape.plugin.internal;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.plugin.internal.util.URLUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.net.URL;

import java.io.InputStream;

/**
 * @author skillcoy
 * 
 */
public class PluginFileReader {
 // private static CyLogger logger = CyLogger.getLogger(PluginFileReader.class);
	private static final Logger logger = LoggerFactory.getLogger(PluginFileReader.class);

	private Document document;

	private String downloadUrl;

	/**
	 * Creates a new PluginFileReader object.
	 * 
	 * @param Url
	 *            DOCUMENT ME!
	 * 
	 */
	protected PluginFileReader(String Url) throws java.io.IOException,
			JDOMException {
		downloadUrl = Url;

		InputStream is = null;

        try {
			URL dlUrl = new URL(downloadUrl);
			is = URLUtil.getInputStream(dlUrl);
            // would be nice to validate later
            SAXBuilder Builder = new SAXBuilder(false);
            document = Builder.build(is, dlUrl.toString());
        }
        finally {
            if (is != null) {
                is.close();
            }
        }

		// don't use this xsd it's no longer valid
		// InputStream is = URLUtil.getInputStream( new URL(downloadUrl) );
		// BufferedReader xsdReader = new BufferedReader( new
		// InputStreamReader(PluginFileReader.class.getResourceAsStream("plugins.xsd"))
		// );
		// String line = null;
		// String Xsd = "";
		// while ( (line = xsdReader.readLine()) != null)
		// Xsd += line;
		//		
		// // validate
		// SAXBuilder Builder = new
		// SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
		// Builder.setFeature("http://apache.org/xml/features/validation/schema",
		// true);
		// Builder.setProperty(
		// "http://apache.org/xml/properties/schema"
		// + "/external-noNamespaceSchemaLocation",
		// Xsd );
		// document = Builder.build(is);

	}

	/**
	 * @return The global project name given by the xml document. NOT CURRENTLY
	 *         USED
	 */
	protected String getProjectName() {
		return document.getRootElement().getChild(nameTag).getTextTrim();
	}

	/**
	 * @return The global project description given by the xml document. NOT
	 *         CURRENTLY USED
	 */
	protected String getProjectDescription() {
		return document.getRootElement().getChild(descTag).getTextTrim();
	}

	/**
	 * @return The global project url given by the xml document. NOT CURRENTLY
	 *         USED
	 */
	protected String getProjectUrl() {
		return document.getRootElement().getChild(urlTag).getTextTrim();
	}

	/**
	 * Retrieves the full list of all downloadable objects in the xml file.
	 * 
	 * @return
	 */
	protected List<DownloadableInfo> getDownloadables() {
		List<DownloadableInfo> downloadableObjs = new ArrayList<DownloadableInfo>();
		downloadableObjs.addAll(getPlugins());
		downloadableObjs.addAll(getThemes());
		return downloadableObjs;
	}

	/**
	 * Use getDownloadables() Gets the ThemeInfo objects as set up in the xml
	 * document.
	 * 
	 * @return
	 */
	protected List<ThemeInfo> getThemes() {
		List<ThemeInfo> Themes = new ArrayList<ThemeInfo>();

		Element ThemeList = document.getRootElement().getChild(
				PluginXml.THEME_LIST.getTag());

		if (ThemeList != null) {
		  //logger.debug("Theme list from xml: " + ThemeList.getChildren().size());
				Iterator<Element> themeI = ThemeList.getChildren(
					PluginXml.THEME.getTag()).iterator();

			while (themeI.hasNext()) {
				Element CurrentTheme = themeI.next();
				ThemeInfo Info = createThemeObject(CurrentTheme);
				if (Info == null)
					continue;
				Themes.add(Info);
			}
		}
		return Themes;
	}

	/**
	 * Use getDownloadables() This gets the PluginInfo objects as
	 *             set up in the xml document.
	 * 
	 * @return The list of PluginInfo objects specified by the xml document.
	 */
	protected List<PluginInfo> getPlugins() {
		List<PluginInfo> Plugins = new ArrayList<PluginInfo>();

		Element PluginList = document.getRootElement().getChild(pluginListTag);
		if (PluginList != null) {
			Iterator<Element> pluginI = PluginList.getChildren(pluginTag)
					.iterator();

			while (pluginI.hasNext()) {
				Element CurrentPlugin = pluginI.next();
				PluginInfo Info = createPluginObject(CurrentPlugin);
				if (Info == null)
					continue;
				Plugins.add(Info);
			}
		}
		return Plugins;
	}

	protected ThemeInfo  createThemeObject(Element CurrentTheme) {
		ThemeInfo Info = (ThemeInfo) this.createBasicInfoObject(CurrentTheme,
				DownloadableType.THEME);
    logger.debug("Creating Theme: " + Info.getName() + " " + Info.getID());
		if (Info != null) {
			/*
			 * add plugins this is plugins from the current download location
			 * only (others not yet supported)
			 */
			java.util.Map<String, List<PluginInfo>> Plugins = ManagerUtil
					.sortByID(getPlugins());

			Iterator<Element> themePluginI = CurrentTheme.getChild(
					PluginXml.PLUGIN_LIST.getTag()).getChildren(
					PluginXml.PLUGIN.getTag()).iterator();

			// two ways to specify a plugin in a theme
			while (themePluginI.hasNext()) {
				Element ThemePlugin = themePluginI.next();

				if (ThemePlugin.getChildren().size() == 2) {
          logger.debug("Theme plugins defined shorthand");
          for (PluginInfo pluginInfo : Plugins.get(ThemePlugin.getChildTextTrim(PluginXml.UNIQUE_ID.getTag()))) {
            String version = Double.valueOf(ThemePlugin.getChildTextTrim(PluginXml.PLUGIN_VERSION.getTag())).toString();
            if (pluginInfo.getObjectVersion().equals(version)) {
							pluginInfo.setParent(Info);
							Info.addPlugin(pluginInfo);
						}
					}
				} else {
					PluginInfo pluginInfo = this.createPluginObject(ThemePlugin);
					pluginInfo.setParent(Info);
					Info.addPlugin(pluginInfo);
				}
			}
		}
		return Info;
	}

	private DownloadableInfo createBasicInfoObject(Element E,
			DownloadableType Type) {
		
		DownloadableInfo Info = null;
		String Id = E.getChildTextTrim(uniqueID);
		switch (Type) { // TODO sort this type stuff out (no more switches)
		case PLUGIN:
			Info = new PluginInfo(Id);
			Info.setObjectUrl(E.getChildTextTrim(urlTag));
			break;
		case THEME:
			Info = new ThemeInfo(Id);
			break;
		}

		Info.setName(E.getChildTextTrim(nameTag));
		Info.setDescription(E.getChildTextTrim(descTag));
		Info.setDownloadableURL(downloadUrl);
		
		// category
		if (Info.getCategory().equals(Category.NONE.getCategoryText())) {
			if (E.getChild(categoryTag) != null) {
				Info.setCategory(E.getChildTextTrim(categoryTag));
			} else {
				Info.setCategory(Category.NONE);
			}
		}
		// object version
		Info = addVersion(Info, E, Type);

		// cytoscape version
		Iterator<Element> versionI = E.getChild(
				PluginXml.CYTOSCAPE_VERSIONS.getTag()).getChildren(
				PluginXml.VERSION.getTag()).iterator();
		while (versionI.hasNext()) {
			Element Version = versionI.next();
			Info.addCytoscapeVersion(Version.getTextTrim());
		}
		return Info;
	}

	/**
	 * Creates the PluginInfo object from the xml <plugin> element. This could
	 * be useful to the PluginTracker.
	 * 
	 * @param CurrentPlugin
	 *            Element
	 * @return PluginInfo object
	 */
	protected PluginInfo createPluginObject(Element CurrentPlugin) {
		PluginInfo Info = (PluginInfo) createBasicInfoObject(CurrentPlugin,
				DownloadableType.PLUGIN);
		if (Info != null) {
			Info.setProjectUrl(CurrentPlugin.getChildTextTrim(projUrlTag));
			Info.setInstallLocation(CurrentPlugin.getChildTextTrim(installLocTag));

			// file type
			PluginInfo.FileType Type = getType(CurrentPlugin);
			if (Type == null) { // unknown type error and move on
				logger.warn("Unknown plugin file type '" + Type
						+ " skipping plugin " + Info.getName());
				return null;
			} else {
				Info.setFiletype(Type);
			}
			// authors
			Info = addAuthors(Info, CurrentPlugin);
			// license
			Info = addLicense(Info, CurrentPlugin);
		}
		return Info;
	}

	protected DownloadableInfo addVersion(DownloadableInfo obj, Element e,
			DownloadableType Type) {
		String Version = null;
		switch (Type) {
		case PLUGIN:
			Version = e.getChildTextTrim(pluginVersTag);
			break;
		case THEME:
			Version = e.getChildTextTrim(PluginXml.THEME_VERSION.getTag());
			break;
		}

		try {
			obj.setObjectVersion(Double.valueOf(Version));
			return obj;
		} catch (NumberFormatException ie) { // is there a better way to let
			// people know it's a bad
			// version? This will just skip
			// past bad version numbers
			// ie.printStackTrace();
			logger.warn("Version number format error: "+Version);
			return null;
		}
	}

	// get license text, add to info object
	protected static PluginInfo addLicense(PluginInfo obj, Element Plugin) {
		Element License = Plugin.getChild(licenseTag);

		if (License != null) {
			boolean RequireAlways = false;
			if (License.getChild("license_required") != null) {
				RequireAlways = true;
			}
			if (License.getChild(licenseText) != null) {
				obj.setLicense(License.getChildTextTrim(licenseText),
						RequireAlways);
			} else if (License.getChild(urlTag) != null) {
				try {
					String LicenseText = URLUtil.download(new URL(License
							.getChildTextTrim(urlTag)));
					obj.setLicense(LicenseText, RequireAlways);
				} catch (Exception E) {
					logger.warn("Unable to add license: "+E.toString(), E);
				}
			}
		}
		return obj;
	}

	// get the authors, add to info object
	private PluginInfo addAuthors(PluginInfo obj, Element Plugin) {
		if (Plugin.getChild(authorListTag) != null) {
			List<Element> Authors = Plugin.getChild(authorListTag).getChildren(authorTag);
			//Iterator<Element> authI = Plugin.getChild(authorListTag).getChildren(authorTag).iterator();
			for (Element CurrentAuthor: Authors) {
			//while (authI.hasNext()) {
				//Element CurrentAuthor = authI.next();
				obj.addAuthor(CurrentAuthor.getChildTextTrim(nameTag),
						CurrentAuthor.getChildTextTrim(instTag));
			}
		}
		return obj;
	}

	// get the type from the plugin element
	private PluginInfo.FileType getType(Element Plugin) {
		PluginInfo.FileType Type = null;

		String GivenType = Plugin.getChild(fileType).getTextTrim();

		if (GivenType.equalsIgnoreCase(PluginInfo.FileType.JAR.toString())) {
			Type = PluginInfo.FileType.JAR;
		} else if (GivenType.equalsIgnoreCase(PluginInfo.FileType.ZIP
				.toString())) {
			Type = PluginInfo.FileType.ZIP;
		}
		return Type;
	}

	// XML Tags PluginTracker uses the same tags
	private static String nameTag = PluginXml.NAME.getTag();

	private static String descTag = PluginXml.DESCRIPTION.getTag();

	private static String classTag = PluginXml.CLASS_NAME.getTag();

	private static String pluginVersTag = PluginXml.PLUGIN_VERSION.getTag();

	private static String cytoVersTag = "cytoscapeVersion";

	private static String urlTag = PluginXml.URL.getTag();

	private static String projUrlTag = PluginXml.PROJECT_URL.getTag();

	private static String downloadUrlTag = PluginXml.DOWNLOAD_URL.getTag();

	private static String categoryTag = PluginXml.CATEGORY.getTag();

	private static String fileListTag = PluginXml.FILE_LIST.getTag();

	private static String fileTag = PluginXml.FILE.getTag();

	private static String pluginListTag = PluginXml.PLUGIN_LIST.getTag();

	private static String pluginTag = PluginXml.PLUGIN.getTag();

	private static String authorListTag = PluginXml.AUTHOR_LIST.getTag();

	private static String authorTag = PluginXml.AUTHOR.getTag();

	private static String instTag = PluginXml.INSTITUTION.getTag();

	private static String fileType = PluginXml.FILE_TYPE.getTag();

	private static String uniqueID = PluginXml.UNIQUE_ID.getTag();

	private static String licenseTag = PluginXml.LICENSE.getTag();

	private static String licenseText = PluginXml.LICENSE_TEXT.getTag();

	private static String installLocTag = PluginXml.INSTALL_LOCATION.getTag();
}
