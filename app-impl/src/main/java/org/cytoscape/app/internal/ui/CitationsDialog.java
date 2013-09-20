package org.cytoscape.app.internal.ui;

import java.util.Set;
import java.util.TreeSet;
import java.util.Date;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Comparator;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import javax.swing.JDialog;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.Frame;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.Insets;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.File;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.util.swing.OpenBrowser;

public class CitationsDialog {
  final WebQuerier webQuerier;
  final AppManager appMgr;
  final TaskManager taskMgr;
  final Frame parent;
  final JDialog dialog;
  final JTextPane textPane;

  public CitationsDialog(WebQuerier webQuerier, AppManager appMgr, TaskManager taskMgr, Frame parent, final OpenBrowser openBrowser) {
    this.webQuerier = webQuerier;
    this.appMgr = appMgr;
    this.taskMgr = taskMgr;
    this.parent = parent;

    dialog = new JDialog(parent, "Citations", JDialog.ModalityType.MODELESS);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setPreferredSize(new Dimension(500, 400));

    textPane = new JTextPane();
    textPane.setEditable(false);
    textPane.setContentType("text/html");
    textPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (!e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
          return;
        openBrowser.openURL(e.getURL().toString());
      }
    });

    dialog.setLayout(new GridBagLayout());
    final GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;      c.gridy = 0;
    c.gridwidth = 1;  c.gridheight = 1;
    c.weightx = 1.0;  c.weighty = 1.0;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(0, 0, 0, 0);
    dialog.add(new JScrollPane(textPane), c);
  }

  public void show() {
    dialog.pack();
    dialog.setVisible(true);
    taskMgr.execute(new TaskIterator(new RetrieveTask(webQuerier, appMgr, textPane)));
  }
}

/**
 * Generic container class for PubMed articles.
 */
class Article {
  final String pmid;
  final String pubDate;
  final String source;
  final String title;
  final List<String> authors;
  final String volume;
  final String issue;
  final String pages;

  public Article (
      final String pmid,
      final String pubDate,
      final String source,
      final String title,
      final List<String> authors,
      final String volume,
      final String issue,
      final String pages) {
    this.pmid = pmid;
    this.pubDate = pubDate;
    this.source = source;
    this.title = title;
    this.authors = authors;
    this.volume = volume;
    this.issue = issue;
    this.pages = pages;
  }

  public String getPmid() {
    return pmid;
  }

  public String getPubDate() {
    return pubDate;
  }

  public String getSource() {
    return source;
  }

  public String getTitle() {
    return title;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public String getAuthorsAsString() {
    final StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < authors.size(); i++) {
      buffer.append(authors.get(i));
      if (i != (authors.size() - 1))
        buffer.append(", ");
    }
    return buffer.toString();
  }

  public String getVolume() {
    return volume;
  }

  public String getIssue() {
    return issue;
  }

  public String getPages() {
    return pages;
  }

  public String toString() {
    return toString("%s. <i>%s</i> %s, %s:%s (%s). %s. PubMed ID: %s.");
  }

  public String toString(final String fmtString) {
    return String.format(fmtString, getAuthorsAsString(), getTitle(), getSource(), getVolume(), getIssue(), getPages(), getPubDate(), getPmid());
  }
}

/**
 * Retrieves article summaries from PubMed and stores them in {@code Article} instances.
 */
class PubMedParser {
  static final String REQUEST_URL_BASE = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=";

  /**
   * Takes a sequence of PubMed IDs and returns a URL for requesting article summaries from PubMed.
   */
  protected static String makeRequestURL(final Iterable<String> pmids) {
    final StringBuffer buffer = new StringBuffer(REQUEST_URL_BASE);
    final Iterator<String> iterator = pmids.iterator();
    while (iterator.hasNext()) {
      buffer.append(iterator.next());
      if (iterator.hasNext())
        buffer.append(',');
    }
    return buffer.toString();
  }

  /**
   * Constructs a new XML-to-DOM parser.
   */
  protected static DocumentBuilder newXmlParser() throws ParserConfigurationException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    final DocumentBuilder builder = factory.newDocumentBuilder();
    return builder;
  }

  protected DocumentBuilder xmlParser = null;

  /**
   * Issues a request to a given URL and returns its parsed XML content as a DOM.
   */
  protected Document xmlRequest(final String url) throws ParserConfigurationException, MalformedURLException, IOException, SAXException {
    if (xmlParser == null)
      xmlParser = newXmlParser();
    final InputStream inputStream = new URL(url).openConnection().getInputStream();
    final Document doc = xmlParser.parse(inputStream);
    inputStream.close();
    return doc;
  }

  /**
   * Takes a {@code DocSum} XML tag and returns an Article object containing all the
   * information in the {@code DocSum} tag.
   */
  protected static Article parseArticle(final Node docSum) {
    String pmid = null;
    String pubDate = null;
    String source = null;
    String title = null;
    List<String> authors = new ArrayList<String>();
    String volume = null;
    String issue = null;
    String pages = null;

    final NodeList items = docSum.getChildNodes();
    for (int i = 0; i < items.getLength(); i++) {
      final Node item = items.item(i);
      final String content = item.getTextContent();
      if ("Id".equals(item.getNodeName())) {
        pmid = content;
      } else if ("Item".equals(item.getNodeName())) {
        final Node nameNode = item.getAttributes().getNamedItem("Name");
        if (nameNode == null) continue;
        final String name = nameNode.getTextContent();
        if ("PubDate".equals(name)) {
          pubDate = content;
        } else if ("Source".equals(name)) {
          source = content;
        } else if ("AuthorList".equals(name)) {
          final NodeList authorList = item.getChildNodes();
          for (int j = 0; j < authorList.getLength(); j++) {
            final Node author = authorList.item(j);
            if (!"Item".equals(author.getNodeName())) continue;
            authors.add(author.getTextContent());
          }
        } else if ("Title".equals(name)) {
          title = content;
        } else if ("Volume".equals(name)) {
          volume = content;
        } else if ("Issue".equals(name)) {
          issue = content;
        } else if ("Pages".equals(name)) {
          pages = content;
        }
      }
    }
    return new Article(pmid, pubDate, source, title, authors, volume, issue, pages);
  }

  /**
   * Return the first key with the given value, or null if the map doesn't have the given value.
   */
  protected static <K,V> K keyForValue(final Map<K,V> map, final V value) {
    for (final Map.Entry<K,V> entry : map.entrySet()) {
      if (entry.getValue().equals(value))
        return entry.getKey();
    }
    return null;
  }

  /**
   * Takes a map of app names to their PubMed IDs, then returns
   * a map of the (potentially) same app names to {@code Article} objects.
   */
  public Map<String,Article> retrieveArticles(final Map<String,String> pmids) throws ParserConfigurationException, MalformedURLException, IOException, SAXException {
    final Map<String,Article> articles = new HashMap<String,Article>();
    final Document root = xmlRequest(makeRequestURL(pmids.values()));
    final NodeList results = root.getChildNodes();
    for (int i = 0; i < results.getLength(); i++) {
      final Node result = results.item(i); // <eSummaryResult>
      final NodeList docSums = result.getChildNodes();
      for (int j = 0; j < docSums.getLength(); j++) {
        final Node docSum = docSums.item(j); // <DocSum>
        if (!"DocSum".equals(docSum.getNodeName()))
          continue;

        final Article article = parseArticle(docSum);
        final String name = keyForValue(pmids, article.getPmid());
        articles.put(name, article);
      }
    }
    return articles;
  }
}

class RetrieveTask implements Task {
  /**
   * PubMed ID of Cytoscape article that will always be included in the citations dialog.
   */
  protected static final String CYTOSCAPE_PMID = "14597658";

  final WebQuerier webQuerier;
  final AppManager appMgr;
  final JTextPane textPane;

  public RetrieveTask(final WebQuerier webQuerier, final AppManager appMgr, final JTextPane textPane) {
    this.webQuerier = webQuerier;
    this.appMgr = appMgr;
    this.textPane = textPane;
  }

  /**
   * Return a map of app names to their PubMed IDs.
   */
  private Map<String,String> getPmidsOfApps() {
    final Map<String,String> pmids = new HashMap<String,String>();
    final Set<WebApp> allApps = webQuerier.getAllApps();
    webQuerier.checkWebAppInstallStatus(allApps, appMgr);
    for (final WebApp app : allApps) {
      if (app.getCorrespondingApp() == null
       || app.getCorrespondingApp().getStatus() != App.AppStatus.INSTALLED)
        continue;
      if (app.getCitation() != null)
        pmids.put(app.getFullName(), app.getCitation());
    }
    return pmids;
  }

  public void run(final TaskMonitor monitor) throws Exception {
    monitor.setTitle("Retrieve citations from PubMed");

    monitor.showMessage(TaskMonitor.Level.INFO, "Retrieve list of apps");
    final Map<String,String> pmids = getPmidsOfApps();
    pmids.put("Cytoscape", CYTOSCAPE_PMID);

    monitor.showMessage(TaskMonitor.Level.INFO, "Retrieve articles");
    final PubMedParser pubMedParser = new PubMedParser();
    final Map<String,Article> articles = pubMedParser.retrieveArticles(pmids);

    final StringBuffer buffer = new StringBuffer("<html>");
    formatArticleAsHtmlDefinition("Cytoscape", articles.get("Cytoscape"), buffer);
    buffer.append("<br><hr><br>");
    for (final String name : new TreeSet<String>(articles.keySet())) {
      if (name.equals("Cytoscape"))
        continue;
      final Article article = articles.get(name);
      formatArticleAsHtmlDefinition(name, article, buffer);
    }
    buffer.append("</html>");

    textPane.setText(buffer.toString());
  }

  private static void formatArticleAsHtmlDefinition(final String name, final Article article, final StringBuffer buffer) {
    buffer.append("<dl><dt><b>");
    buffer.append(name);
    buffer.append("</b></dt>");
    buffer.append("<dd>");
    buffer.append(article.toString());
    buffer.append("<br>");
    buffer.append("<font size=\"-1\"><a href=\"http://www.ncbi.nlm.nih.gov/pubmed/");
    buffer.append(article.getPmid());
    buffer.append("\">Open in PubMed &rarr;</a></font>");
    buffer.append("</dd></dl>");
  }

  public void cancel() {
  }
}