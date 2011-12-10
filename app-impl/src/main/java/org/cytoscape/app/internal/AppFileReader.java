/**
 *
 */
package org.cytoscape.app.internal;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.app.internal.util.URLUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.net.URL;

import java.io.InputStream;

/**
 * @author skillcoy
 * 
 */
public class AppFileReader {
 // private static CyLogger logger = CyLogger.getLogger(AppFileReader.class);
	private static final Logger logger = LoggerFactory.getLogger(AppFileReader.class);

	private Document document;

	private String downloadUrl;

	/**
	 * Creates a new AppFileReader object.
	 * 
	 * @param Url
	 *            DOCUMENT ME!
	 * 
	 */
	protected AppFileReader(String Url) throws java.io.IOException,
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
		// InputStreamReader(AppFileReader.class.getResourceAsStream("apps.xsd"))
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
		downloadableObjs.addAll(getApps());
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
				AppXml.THEME_LIST.getTag());

		if (ThemeList != null) {
		  //logger.debug("Theme list from xml: " + ThemeList.getChildren().size());
				Iterator<Element> themeI = ThemeList.getChildren(
					AppXml.THEME.getTag()).iterator();

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
	 * Use getDownloadables() This gets the AppInfo objects as
	 *             set up in the xml document.
	 * 
	 * @return The list of AppInfo objects specified by the xml document.
	 */
	protected List<AppInfo> getApps() {
		List<AppInfo> Apps = new ArrayList<AppInfo>();

		Element AppList = document.getRootElement().getChild(appListTag);
		if (AppList != null) {
			Iterator<Element> appI = AppList.getChildren(appTag)
					.iterator();

			while (appI.hasNext()) {
				Element CurrentApp = appI.next();
				AppInfo Info = createAppObject(CurrentApp);
				if (Info == null)
					continue;
				Apps.add(Info);
			}
		}
		return Apps;
	}

	protected ThemeInfo  createThemeObject(Element CurrentTheme) {
		ThemeInfo Info = (ThemeInfo) this.createBasicInfoObject(CurrentTheme,
				DownloadableType.THEME);
    logger.debug("Creating Theme: " + Info.getName() + " " + Info.getID());
		if (Info != null) {
			/*
			 * add apps this is apps from the current download location
			 * only (others not yet supported)
			 */
			java.util.Map<String, List<AppInfo>> Apps = ManagerUtil
					.sortByID(getApps());

			Iterator<Element> themeAppI = CurrentTheme.getChild(
					AppXml.APP_LIST.getTag()).getChildren(
					AppXml.APP.getTag()).iterator();

			// two ways to specify a app in a theme
			while (themeAppI.hasNext()) {
				Element ThemeApp = themeAppI.next();

				if (ThemeApp.getChildren().size() == 2) {
          logger.debug("Theme apps defined shorthand");
          for (AppInfo appInfo : Apps.get(ThemeApp.getChildTextTrim(AppXml.UNIQUE_ID.getTag()))) {
            String version = Double.valueOf(ThemeApp.getChildTextTrim(AppXml.APP_VERSION.getTag())).toString();
            if (appInfo.getObjectVersion().equals(version)) {
							appInfo.setParent(Info);
							Info.addApp(appInfo);
						}
					}
				} else {
					AppInfo appInfo = this.createAppObject(ThemeApp);
					appInfo.setParent(Info);
					Info.addApp(appInfo);
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
		case APP:
			Info = new AppInfo(Id);
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
				AppXml.CYTOSCAPE_VERSIONS.getTag()).getChildren(
				AppXml.VERSION.getTag()).iterator();
		while (versionI.hasNext()) {
			Element Version = versionI.next();
			Info.addCytoscapeVersion(Version.getTextTrim());
		}
		return Info;
	}

	/**
	 * Creates the AppInfo object from the xml <app> element. This could
	 * be useful to the AppTracker.
	 * 
	 * @param CurrentApp
	 *            Element
	 * @return AppInfo object
	 */
	protected AppInfo createAppObject(Element CurrentApp) {
		AppInfo Info = (AppInfo) createBasicInfoObject(CurrentApp,
				DownloadableType.APP);
		if (Info != null) {
			Info.setProjectUrl(CurrentApp.getChildTextTrim(projUrlTag));
			Info.setInstallLocation(CurrentApp.getChildTextTrim(installLocTag));

			// file type
			AppInfo.FileType Type = getType(CurrentApp);
			if (Type == null) { // unknown type error and move on
				logger.warn("Unknown app file type '" + Type
						+ " skipping app " + Info.getName());
				return null;
			} else {
				Info.setFiletype(Type);
			}
			// authors
			Info = addAuthors(Info, CurrentApp);
			// license
			Info = addLicense(Info, CurrentApp);
		}
		return Info;
	}

	protected DownloadableInfo addVersion(DownloadableInfo obj, Element e,
			DownloadableType Type) {
		String Version = null;
		switch (Type) {
		case APP:
			Version = e.getChildTextTrim(appVersTag);
			break;
		case THEME:
			Version = e.getChildTextTrim(AppXml.THEME_VERSION.getTag());
			break;
		}

		try {
			//obj.setObjectVersion(Double.valueOf(Version));
			obj.setObjectVersion(Version);
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
	protected static AppInfo addLicense(AppInfo obj, Element App) {
		Element License = App.getChild(licenseTag);

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
	private AppInfo addAuthors(AppInfo obj, Element App) {
		if (App.getChild(authorListTag) != null) {
			List<Element> Authors = App.getChild(authorListTag).getChildren(authorTag);
			//Iterator<Element> authI = App.getChild(authorListTag).getChildren(authorTag).iterator();
			for (Element CurrentAuthor: Authors) {
			//while (authI.hasNext()) {
				//Element CurrentAuthor = authI.next();
				obj.addAuthor(CurrentAuthor.getChildTextTrim(nameTag),
						CurrentAuthor.getChildTextTrim(instTag));
			}
		}
		return obj;
	}

	// get the type from the app element
	private AppInfo.FileType getType(Element App) {
		AppInfo.FileType Type = null;

		String GivenType = App.getChild(fileType).getTextTrim();

		if (GivenType.equalsIgnoreCase(AppInfo.FileType.JAR.toString())) {
			Type = AppInfo.FileType.JAR;
		} else if (GivenType.equalsIgnoreCase(AppInfo.FileType.ZIP
				.toString())) {
			Type = AppInfo.FileType.ZIP;
		}
		return Type;
	}

	// XML Tags AppTracker uses the same tags
	private static String nameTag = AppXml.NAME.getTag();

	private static String descTag = AppXml.DESCRIPTION.getTag();

	private static String classTag = AppXml.CLASS_NAME.getTag();

	private static String appVersTag = AppXml.APP_VERSION.getTag();

	private static String cytoVersTag = "cytoscapeVersion";

	private static String urlTag = AppXml.URL.getTag();

	private static String projUrlTag = AppXml.PROJECT_URL.getTag();

	private static String downloadUrlTag = AppXml.DOWNLOAD_URL.getTag();

	private static String categoryTag = AppXml.CATEGORY.getTag();

	private static String fileListTag = AppXml.FILE_LIST.getTag();

	private static String fileTag = AppXml.FILE.getTag();

	private static String appListTag = AppXml.APP_LIST.getTag();

	private static String appTag = AppXml.APP.getTag();

	private static String authorListTag = AppXml.AUTHOR_LIST.getTag();

	private static String authorTag = AppXml.AUTHOR.getTag();

	private static String instTag = AppXml.INSTITUTION.getTag();

	private static String fileType = AppXml.FILE_TYPE.getTag();

	private static String uniqueID = AppXml.UNIQUE_ID.getTag();

	private static String licenseTag = AppXml.LICENSE.getTag();

	private static String licenseText = AppXml.LICENSE_TEXT.getTag();

	private static String installLocTag = AppXml.INSTALL_LOCATION.getTag();
}
