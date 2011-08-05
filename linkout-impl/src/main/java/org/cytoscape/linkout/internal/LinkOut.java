package org.cytoscape.linkout.internal;


import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.cytoscape.model.CyNode;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;



/**
 * Generates links to external web pages specified in the cytoscape.props file.
 * Nodes can be linked to external web pages by specifying web resources in the linkout.properties file
 * The format for a weblink is in the form of a <key> = <value> pair where <key> is the name of the
 * website (e.g. NCBI, E!, PubMed, SGD,etc) and <value> is the URL. The key name must be preceded by the keyword "url." to distinguish
 * this property from other properties.
 * In the URL string the placeholder %ID% will be replaced with the node label that is visible on the node.
 * If no label is visible, the node identifier (far left of attribute browser) will be used.
 * It is the users responsibility
 * to ensure that the URL is correct and the node's name will match the required query value.
 * Examples:
 *    url.NCBI=http\://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd\=Search&db\=Protein&term\=%ID%&doptcmdl\=GenPept
 *    url.SGD=http\://db.yeastgenome.org/cgi-bin/locus.pl?locus\=%ID%
 *    url.E\!Ensamble=http\://www.ensembl.org/Homo_sapiens/textview?species\=all&idx\=All&q\=%ID%
 *    url.Pubmed=http\://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd\=Search&db\=PubMed&term\=%ID%
 *
 */

/**
 * AJK: 06/0  9/2009: porting to Cytoscape 3.0. Under 3.0, should take the form:
 * 
 * 
 * 
 * public class Linkout { public Linkout(Properties p, CyServiceRegistrar
 * registrar) { // loop over all linkout properties String link =
 * p.getProperty(whatever); String preferredMenu =
 * extractMenuTitleFromLink(link); Map<String,String> dict = new
 * HashMap<String,String>(); dict.put("preferredMenu",preferredMenu);
 * NodeViewTaskFactory nvtf = new LinkoutTaskFactory(link);
 * registrar.registerService( nvtf, dict ); }
 */
public class LinkOut {
	// keyword that marks properties that should be added to LinkOut
	private static final String nodeMarker = "nodelinkouturl.";
	private static final String edgeMarker = "edgelinkouturl.";
	private static final String linkMarker = "Linkout.externalLinkName";
	private static String externalLinksAttribute = "Linkout.ExternalLinks";

	private Properties props;
	private static final Font TITLE_FONT = new Font("sans-serif", Font.BOLD, 14);
	CyServiceRegistrar registrar;
	OpenBrowser browser;

	// null constractor
	/**
	 * Creates a new LinkOut object.
	 */
	public LinkOut() {
	}

	public LinkOut(CyProperty<Properties> props, CyServiceRegistrar registrar,
			OpenBrowser browser) {
		this.props = props.getProperties();
		this.registrar = registrar;
		this.browser = browser;
		readProperties();
		// now link through properties
		addLinksFromProperties();

		// TODO: how to deal with external properties that the user may have set
		// on a particular node/edge?

	}

	/**
	 * Read properties values from linkout.props file included in the
	 * linkout.jar file and apply those properties to the base Cytoscape
	 * properties. Only apply the properties from the jar file if NO linkout
	 * properties already exist. This allows linkout properties to be specified
	 * on the command line, editted in the preferences dialog, and to be saved
	 * with other properties.
	 */

	private void readProperties() {
		final String inputFileName =
			System.getProperty("user.home") + "/" + CyProperty.DEFAULT_CONFIG_DIR + "/linkout.props";

		try {
			final File inputFile = new File(inputFileName);
			if (inputFile.canRead())
				props.load(new FileInputStream(inputFile));
		} catch (Exception e) {
			System.err.println("Couldn't load linkout props from \""
					   + inputFileName + "\"!");
		}
	}

	public void addLinksFromProperties() {

		// iterate through properties list
		try {

			for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
				String propKey = (String) e.nextElement();
				int p = propKey.lastIndexOf(nodeMarker);

				if (p == -1)
					continue;

				p = p + nodeMarker.length();

				// the URL
				String url = props.getProperty(propKey);

				if (url == null) {
					url = "<html><small><i>empty- no links<br> See documentation</i></small></html>"
							+ "http://www.cytoscape.org/";
				}

				// TODO: do a proper substitution of node/edge name into the
				// link values
				/*
				 * String fUrl = subsAttrs(url, na, nodeId, "ID", "");
				 * 
				 * //the link name String[] temp = ((String)
				 * propKey.substring(p)).split("\\."); ArrayList keys = new
				 * ArrayList(Arrays.asList(temp));
				 * 
				 * //Generate the menu path generateLinks(keys, top_menu, fUrl);
				 */

				// AJK: 06/11/09 code to set preferred menu
				Dictionary<String, String> dict = new Hashtable<String, String>();
				
				// AJK: 11/12/09 need to substitute "LinkOut" for nodeMarker in the property key
				
//				dict.put("preferredMenu", propKey);
				String menuKey = "LinkOut." + propKey.substring(p);
				dict.put("preferredMenu", menuKey);
				
				NodeViewTaskFactory nvtf = new LinkoutTaskFactory(url);
				registrar.registerService(nvtf, NodeViewTaskFactory.class, dict);
			}

			// Now, see if the user has specified their own URL to add to
			// linkout
			// TODO: generate external links later
			// generateExternalLinks(na, nodeId, top_menu);

			// if no links specified insert a default message
			// TODO: deal with no links specified
			/*
			 * if (top_menu.getMenuComponentCount() == 0) { String url =
			 * "<html><small><i>empty- no links<br> See documentation</i></small></html>"
			 * + "http://www.cytoscape.org/"; top_menu.add(new JMenuItem(url));
			 * }
			 */

			// For debugging
			// printMenu(top_menu);
		} catch (NullPointerException e) {
			String url = "<html><small><i>empty- no links<br> See documentation</i></small></html>"
					+ "http://www.cytoscape.org/";
			// TOTO: deal with error condition and set topt level menu item
			// top_menu.add(new JMenuItem(url));
			System.err.println("NullPointerException: " + e.getMessage());
		}

		// return top_menu;
	}

	/**
	 * Perform variable substitution on a url using attribute values.
	 * 
	 * Given a url, look for attribute names within that url specified by
	 * %AttributeName% If our current node or edge (as specified by graphObjId)
	 * has an attribute name that matches the string in the URL, replace the
	 * string with the value of the attribute.
	 * 
	 * Special cases: 1. For backwards compatibility, if the attribute name
	 * given by idKeyword is found substitute the value of the attribute id as
	 * given by graphObjId. 2. When looking for attribute names, we can specify
	 * that they must have a prefix added to the beginning. This allows us to
	 * specify a "from" or "to" prefix in edge attributes so that
	 * from.AttributeName and to.AttributeName can select different properties.
	 * 
	 * @param url
	 *            the url to perform replacements on.
	 * @param attrs
	 *            a set of node or edge attributes.
	 * @param graphObjId
	 *            the id of the node or edge that is selected.
	 * @param idKeyword
	 *            a special attribute keyword, that if found, should be replaced
	 *            by graphObjId
	 * @param prefix
	 *            a prefix to prepend to attribute names.
	 * @return modified url after variable substitution
	 */

	/*
	 * private String substituteAttributes(String url, String graphObjId, String
	 * idKeyword, String prefix) {
	 * 
	 * // TODO: to start, just substitute ID string into the URL // then we can
	 * think about substituting in other attribute values Set<String> validAttrs
	 * = new HashSet<String>();
	 * 
	 * for (String attrName : attrs.getAttributeNames()) { if
	 * (attrs.hasAttribute(graphObjId, attrName)) { validAttrs.add(prefix +
	 * attrName); } }
	 * 
	 * // Replace %ATTRIBUTE.NAME% mark with the value of the attribute final
	 * String REGEX = "%.*%"; Pattern pat = Pattern.compile(REGEX); Matcher mat
	 * = pat.matcher(url);
	 * 
	 * while (mat.find()) { String attrName = url.substring(mat.start() + 1,
	 * mat.end() - 1);
	 * 
	 * // backwards compatibility, old keywords were %ID%, %ID1%, %ID2%. if
	 * (attrName.equals(idKeyword)) { String attrValue = graphObjId; url =
	 * url.replace("%" + idKeyword + "%", attrValue); mat = pat.matcher(url); }
	 * else if (validAttrs.contains(attrName)) { String attrValue =
	 * attrToString(attrs, graphObjId, attrName); url = url.replace("%" +
	 * attrName + "%", attrValue); mat = pat.matcher(url); } }
	 * 
	 * return url; }
	 */

	/**
	 * private classes
	 */

	// AJK: 06/11/09 task factory classes
	private class LinkoutTaskFactory extends AbstractNodeViewTaskFactory {
		private String link;
		public LinkoutTaskFactory(String link) {
			super();
			this.link = link;
		}
		public TaskIterator getTaskIterator() {
			return new TaskIterator(new LinkoutTask(link, nodeView));
		}
	}

	private class LinkoutTask extends AbstractTask {
		private String link;
		private View<CyNode> nodeView;

		public LinkoutTask(String link, View<CyNode> v) {
			this.link = link;
			this.nodeView = v;
		}

		@Override
		public void run(TaskMonitor tm) {
			System.out.println("LinkoutTask " + link);

			// String fUrl = substituteAttributes(link, nodeView, "ID", "");
			// TODO: get OpenBrowser working

			// TODO: for now, just substitute node ID into %ID% parameter
			CyNode node = nodeView.getModel();
			String identifier = node.getCyRow().get("name", String.class);

			link = link.replace("%" + "ID" + "%", identifier);
			browser.openURL(link);
		}

		@Override
		public void cancel() {
		}
	}

	/**
	 * everything from here on in is commented out
	 */

	/**
	 * Generates URL links with node name and places them in hierarchical JMenu
	 * list
	 * 
	 * @param node
	 *            the View<Node>.
	 * @return JMenuItem
	 */
	/*
	 * public JMenuItem addLinks(View<Node> node) { //
	 * System.out.println("linkout.addLinks called with node " // +
	 * ((View<Node>) node).getLabel().getText()); readProperties();
	 * 
	 * final JMenu top_menu = new JMenu("LinkOut"); final JMenuItem source = new
	 * JMenuItem("Database"); source.setBackground(Color.white);
	 * source.setFont(TITLE_FONT); source.setEnabled(false);
	 * top_menu.add(source);
	 * 
	 * //iterate through properties list try { // CyAttributes na =
	 * Cytoscape.getNodeAttributes(); na = node.attrs();
	 * 
	 * final View<Node> mynode = node;
	 * 
	 * // Get the set of attribute names for this node CyNode n =
	 * mynode.getNode(); String nodeId = n.getIdentifier();
	 * 
	 * for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
	 * String propKey = (String) e.nextElement(); int p =
	 * propKey.lastIndexOf(nodeMarker);
	 * 
	 * if (p == -1) continue;
	 * 
	 * p = p + nodeMarker.length();
	 * 
	 * //the URL String url = props.getProperty(propKey);
	 * 
	 * if (url == null) { url =
	 * "<html><small><i>empty- no links<br> See documentation</i></small></html>"
	 * + "http://www.cytoscape.org/"; }
	 * 
	 * String fUrl = subsAttrs(url, na, nodeId, "ID", "");
	 * 
	 * //the link name String[] temp = ((String)
	 * propKey.substring(p)).split("\\."); ArrayList keys = new
	 * ArrayList(Arrays.asList(temp));
	 * 
	 * //Generate the menu path generateLinks(keys, top_menu, fUrl); }
	 * 
	 * // Now, see if the user has specified their own URL to add to linkout
	 * generateExternalLinks(na, nodeId, top_menu);
	 * 
	 * //if no links specified insert a default message if
	 * (top_menu.getMenuComponentCount() == 0) { String url =
	 * "<html><small><i>empty- no links<br> See documentation</i></small></html>"
	 * + "http://www.cytoscape.org/"; top_menu.add(new JMenuItem(url)); }
	 * 
	 * // For debugging // printMenu(top_menu); } catch (NullPointerException e)
	 * { String url =
	 * "<html><small><i>empty- no links<br> See documentation</i></small></html>"
	 * + "http://www.cytoscape.org/"; top_menu.add(new JMenuItem(url));
	 * System.err.println("NullPointerException: " + e.getMessage()); }
	 * 
	 * return top_menu; }
	 */

	/**
	 * DOCUMENT ME!
	 * 
	 * @param attributes
	 *            DOCUMENT ME!
	 * @param id
	 *            DOCUMENT ME!
	 * @param attributeName
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	// TODO: convert attribute to String
	/*
	 * private String attrToString(CyAttributes attributes, String id, String
	 * attributeName) { Object value = null; byte attrType =
	 * attributes.getType(attributeName);
	 * 
	 * if (attrType == CyAttributes.TYPE_BOOLEAN) { value =
	 * attributes.getBooleanAttribute(id, attributeName); } else if (attrType ==
	 * CyAttributes.TYPE_FLOATING) { value = attributes.getDoubleAttribute(id,
	 * attributeName); } else if (attrType == CyAttributes.TYPE_INTEGER) { value
	 * = attributes.getIntegerAttribute(id, attributeName); } else if (attrType
	 * == CyAttributes.TYPE_STRING) { value = attributes.getStringAttribute(id,
	 * attributeName); }
	 * 
	 * if (value != null) return value.toString(); else
	 * 
	 * return "N/A"; }
	 */

	/**
	 * Generate URL links with edge property and places them in hierarchical
	 * JMenu list
	 * 
	 * @param edge
	 *            View<Edge> object
	 * @return JMenuItem
	 */
	// TODO: add Edge links later
	/*
	 * public JMenuItem addLinks(View<Edge> edge) { readProperties();
	 * 
	 * JMenu top_menu = new JMenu("LinkOut");
	 * 
	 * //iterate through properties list try { final View<Edge> myedge = edge;
	 * 
	 * // Replace edge attributes with values CyEdge ed = myedge.getEdge();
	 * 
	 * for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
	 * String propKey = (String) e.nextElement();
	 * 
	 * int p = propKey.lastIndexOf(edgeMarker);
	 * 
	 * if (p == -1) continue;
	 * 
	 * p = p + edgeMarker.length();
	 * 
	 * //the URL String url = props.getProperty(propKey);
	 * 
	 * if (url == null) { url =
	 * "<html><small><i>empty- no links<br> See documentation</i></small></html>"
	 * + "http://www.cytoscape.org/"; }
	 * 
	 * //add edge label to the URL String edgelabel;
	 * 
	 * CyAttributes attrs = Cytoscape.getNodeAttributes(); String sourceId =
	 * ed.getSource().getIdentifier(); String targetId =
	 * ed.getTarget().getIdentifier(); String fUrl = subsAttrs(url, attrs,
	 * sourceId, "ID%1", "source."); fUrl = subsAttrs(fUrl, attrs, targetId,
	 * "ID%2", "target.");
	 * 
	 * // System.out.println(fUrl);
	 * 
	 * //the link name String[] temp = ((String)
	 * propKey.substring(p)).split("\\."); ArrayList keys = new
	 * ArrayList(Arrays.asList(temp));
	 * 
	 * //Generate the menu path generateLinks(keys, top_menu, fUrl); }
	 * 
	 * CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
	 * 
	 * // Now, see if the user has specified their own URL to add to linkout
	 * generateExternalLinks(edgeAttributes, ed.getIdentifier(), top_menu);
	 * 
	 * //if no links specified insert a default message if
	 * (top_menu.getMenuComponentCount() == 0) { String url =
	 * "<html><small><i>empty- no links<br> See documentation</i></small></html>"
	 * + "http://www.cytoscape.org/"; top_menu.add(new JMenuItem(url)); }
	 * 
	 * // For debugging // printMenu(top_menu); } catch (NullPointerException e)
	 * { String url =
	 * "<html><small><i>empty- no links<br> See documentation</i></small></html>"
	 * + "http://www.cytoscape.org/"; top_menu.add(new JMenuItem(url));
	 * System.err.println("NullPointerException: " + e.getMessage()); }
	 * 
	 * return top_menu; }
	 */

	/**
	 * Recursive method that expands the current menu list The list of keys mark
	 * the current path of sub-menus
	 * 
	 * @param keys
	 *            ArrayList
	 * @param j
	 *            JMenu the curren JMenu object
	 * @param url
	 *            String the url to link the node
	 */
	/*
	 * TODO: generateLinks with cascading menus. For initial port, just use the
	 * full property name as name
	 * 
	 * 
	 * private void generateLinks(ArrayList keys, JMenu j, final String url) {
	 * //Get the sub-menu JMenuItem jmi = getMenuItem((String) keys.get(0), j);
	 * 
	 * //if its null and this is the last key generate a new JMenuItem if ((jmi
	 * == null) && (keys.size() == 1)) { final String s = (String) keys.get(0);
	 * JMenuItem new_jmi = new JMenuItem(new AbstractAction((String)
	 * keys.get(0)) { public void actionPerformed(ActionEvent e) {
	 * SwingUtilities.invokeLater(new Runnable() { public void run() { //
	 * System.out.println("Opening link: "+url); OpenBrowser.openURL(url); } });
	 * } }); //end of AbstractAction class
	 * 
	 * j.add(new_jmi);
	 * 
	 * return;
	 * 
	 * //if its a JMenuItem and this is the last key then there //is a duplicate
	 * of keys in the file. i.e two url with the exact same manu path } else if
	 * (jmi instanceof JMenuItem && (keys.size() == 1)) {
	 * System.out.println("Duplicate URL specified for " + (String)
	 * keys.get(0));
	 * 
	 * return;
	 * 
	 * //if not null create a new JMenu with current key // remove key from the
	 * keys ArrayList and call generateLinks } else if (jmi == null) { JMenu
	 * new_jm = new JMenu((String) keys.get(0));
	 * 
	 * keys.remove(0); generateLinks(keys, new_jm, url); j.add(new_jm);
	 * 
	 * return;
	 * 
	 * //Remove key from top of the list and call generateLinks with new JMenu }
	 * else { keys.remove(0);
	 * 
	 * generateLinks(keys, (JMenu) jmi, url); }
	 * 
	 * return; }
	 */

	/**
	 * Search for an existing JmenuItem that is nested within a higher level
	 * JMenu
	 * 
	 * @param name
	 *            String the name of the jmenu item
	 * @param menu
	 *            JMenu the parent JMenu to search in
	 * @return JMenuItem if found, null otherwise
	 * 
	 */
	// TODO: get rid of getMenuItem()?
	/*
	 * private JMenuItem getMenuItem(String name, JMenu menu) { int count =
	 * menu.getMenuComponentCount();
	 * 
	 * if (count == 0) { return null; }
	 * 
	 * //Skip over all JMenu components that are not JMenu or JMenuItem for (int
	 * i = 0; i < count; i++) { if
	 * (!menu.getItem(i).getClass().getName().equals("javax.swing.JMenu") &&
	 * !menu.getItem(i).getClass().getName().equals("javax.swing.JMenuItem")) {
	 * continue; }
	 * 
	 * JMenuItem jmi = menu.getItem(i);
	 * 
	 * if (jmi.getText().equalsIgnoreCase(name)) { return jmi; } }
	 * 
	 * return null; }
	 */

	/**
	 * Print menu items - for debugging
	 */
	/*
	 * private void printMenu(JMenu jm) { int count =
	 * jm.getMenuComponentCount();
	 * 
	 * for (int i = 0; i < count; i++) { if
	 * (jm.getItem(i).getClass().getName().equals("javax.swing.JMenuItem")) {
	 * System.out.println(jm.getItem(i).getText());
	 * 
	 * continue; } else { System.out.println(jm.getItem(i).getText() + "--");
	 * printMenu((JMenu) jm.getItem(i)); } } }
	 */

	/**
	 * If we have an ExternalLinks attribute, see if it's formatted as a
	 * LinkOut, and if so, add that to the menu. A LinkOut may be one of:
	 * String: name=URL List: [name1=URL1,name2=URL2,etc.] where the name will
	 * be used as the menu label and the URL will be what we actually hand off
	 * to the browser.
	 * 
	 * @param attributes
	 *            the attribute map we are currently using
	 * @param id
	 *            the ID of the object we are currently linking out from
	 * @param menu
	 *            the menu to add links to
	 */
	// TODO: add external links later
	/*
	 * private void generateExternalLinks(CyAttributes attributes, String id,
	 * JMenu menu) { if (attributes.hasAttribute(id, externalLinksAttribute)) {
	 * // Maybe..... byte attrType = attributes.getType(externalLinksAttribute);
	 * if (attrType == CyAttributes.TYPE_STRING) { // Single title=url pair
	 * String attr = attributes.getStringAttribute(id, externalLinksAttribute);
	 * addExternalLink(attr, menu); } else if (attrType ==
	 * CyAttributes.TYPE_SIMPLE_LIST) { // List of title=url pairs List attrList
	 * = attributes.getListAttribute(id, externalLinksAttribute); for (String
	 * attr: (List<String>)attrList) { addExternalLink(attr, menu); } } }
	 * return; }
	 * 
	 * private void addExternalLink(String attr, JMenu menu) { if (attr == null
	 * || attr.length() < 9) return; String[] pair = attr.split("=",2); if
	 * (pair.length != 2) return; if (!pair[1].startsWith("http://")) { return;
	 * } ArrayList<String>key = new ArrayList(); key.add("ExternalLinks");
	 * key.add(pair[0]); generateLinks(key, menu, pair[1]); }
	 */

}
