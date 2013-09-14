package org.cytoscape.app.internal.ui;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

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

import java.awt.Frame;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.Insets;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;

public class CitationsDialog {
  final WebQuerier webQuerier;
  final AppManager appMgr;
  final TaskManager taskMgr;
  final JDialog dialog;
  final JTextPane textPane;

  public CitationsDialog(WebQuerier webQuerier, AppManager appMgr, TaskManager taskMgr, Frame parent) {
    this.webQuerier = webQuerier;
    this.appMgr = appMgr;
    this.taskMgr = taskMgr;

    dialog = new JDialog(parent, "Citations", JDialog.ModalityType.MODELESS);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setPreferredSize(new Dimension(500, 400));

    textPane = new JTextPane();
    textPane.setEditable(false);
    textPane.setContentType("text/html");

    final JButton saveButton = new JButton("Save for EndNote...");

    dialog.setLayout(new GridBagLayout());
    final GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;      c.gridy = 0;
    c.gridwidth = 1;  c.gridheight = 1;
    c.weightx = 1.0;  c.weighty = 1.0;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(0, 0, 10, 0);
    dialog.add(new JScrollPane(textPane), c);

    final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonsPanel.add(saveButton);

    c.gridx = 0;      c.gridy = 1;
    c.gridwidth = 1;  c.gridheight = 1;
    c.weightx = 1.0;  c.weighty = 0.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 10, 10, 10);
    dialog.add(buttonsPanel, c);
  }

  public void show() {
    dialog.pack();
    dialog.setVisible(true);
    taskMgr.execute(new TaskIterator(new RetrieveTask(webQuerier, appMgr, textPane)));
  }
}

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

  public String getAuthorEtAl() {
    if (authors.size() == 0)
      return null;
    else if (authors.size() == 1)
      return authors.get(0);
    else
      return authors.get(0) + " et al";
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
}

class TextArticleFormatter {
  public static String format(final Article article) {
    return String.format("%s. %s %s, %s:%s (%s). %s",
      article.getAuthorEtAl(),
      article.getTitle(),
      article.getSource(),
      article.getVolume(),
      article.getIssue(),
      article.getPages(),
      article.getPubDate());
  }
}

class PubMedParser {
  static final String REQUEST_URL_BASE = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=";

  private static String makeRequestURL(final List<String> pmids) {
    final StringBuffer buffer = new StringBuffer(REQUEST_URL_BASE);
    for (int i = 0; i < pmids.size(); i++) {
      buffer.append(pmids.get(i));
      if (i != (pmids.size() - 1))
        buffer.append(',');
    }
    return buffer.toString();
  }

  protected static DocumentBuilder newXmlParser() throws ParserConfigurationException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    final DocumentBuilder builder = factory.newDocumentBuilder();
    return builder;
  }

  protected DocumentBuilder xmlParser = null;

  protected Document xmlRequest(final String url) throws ParserConfigurationException, MalformedURLException, IOException, SAXException {
    if (xmlParser == null)
      xmlParser = newXmlParser();
    return xmlParser.parse(new URL(url).openConnection().getInputStream());
  }

  public List<Article> retrieveArticles(final List<String> pmids) throws Exception {
    final List<Article> articles = new ArrayList<Article>();
    final Document root = xmlRequest(makeRequestURL(pmids));
    final NodeList results = root.getChildNodes(); 
    for (int i = 0; i < results.getLength(); i++) {
      final Node result = results.item(i); // <eSummaryResult>
      final NodeList docSums = result.getChildNodes();
      for (int j = 0; j < docSums.getLength(); j++) {
        final Node docSum = docSums.item(j); // <DocSum>
        if (!"DocSum".equals(docSum.getNodeName()))
          continue;
        articles.add(parseArticle(docSum));
      }
    }
    return articles;
  }

  private static Article parseArticle(final Node docSum) {
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
}

class RetrieveTask implements Task {
  final WebQuerier webQuerier;
  final AppManager appMgr;
  final JTextPane textPane;
  public RetrieveTask(final WebQuerier webQuerier, final AppManager appMgr, final JTextPane textPane) {
    this.webQuerier = webQuerier;
    this.appMgr = appMgr;
    this.textPane = textPane;
  }

  public void run(final TaskMonitor monitor) throws Exception {
    monitor.setTitle("Retrieve citations from PubMed");

    monitor.showMessage(TaskMonitor.Level.INFO, "Retrieve list of apps");
    final List<String> pmids = getPmids();

    monitor.showMessage(TaskMonitor.Level.INFO, "Retrieve articles");
    final PubMedParser pubMedParser = new PubMedParser();
    final List<Article> articles = pubMedParser.retrieveArticles(pmids);

    final StringBuffer buffer = new StringBuffer("<html><ul>");
    for (final Article article : articles) {
      buffer.append("<li>");
      buffer.append(TextArticleFormatter.format(article));
      buffer.append("</li>");
    }
    buffer.append("</ul></html>");

    textPane.setText(buffer.toString());
  }

  private List<String> getPmids() {
    final List<String> pmids = new ArrayList<String>();
    final Set<WebApp> allApps = webQuerier.getAllApps();
    webQuerier.checkWebAppInstallStatus(allApps, appMgr);
    for (final WebApp app : allApps) {
      if (app.getCorrespondingApp() == null
       || app.getCorrespondingApp().getStatus() != App.AppStatus.INSTALLED)
        continue;
      if (app.getCitation() != null)
        pmids.add(app.getCitation());
    }
    return pmids;
  }

  public void cancel() {
  }
}