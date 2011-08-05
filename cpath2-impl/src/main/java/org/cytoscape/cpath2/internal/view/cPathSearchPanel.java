package org.cytoscape.cpath2.internal.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.schemas.search_response.SearchResponseType;
import org.cytoscape.cpath2.internal.schemas.summary_response.SummaryResponseType;
import org.cytoscape.cpath2.internal.view.model.InteractionBundleModel;
import org.cytoscape.cpath2.internal.view.model.PathwayTableModel;
import org.cytoscape.cpath2.internal.web_service.CPathProperties;
import org.cytoscape.cpath2.internal.web_service.CPathWebService;
import org.cytoscape.cpath2.internal.web_service.CPathWebServiceListener;

/**
 * Main GUI Panel for Searching a cPath Instance.
 *
 * @author Ethan Cerami.
 */
public class cPathSearchPanel extends JPanel implements CPathWebServiceListener {
    protected InteractionBundleModel interactionBundleModel;
    protected PathwayTableModel pathwayTableModel;
    protected CPathWebService webApi;
    private JPanel searchBoxPanel;
    private JPanel searchHitsPanel = null;
    private JPanel cards;
	private final CPath2Factory factory;

    /**
     * Constructor.
     *
     * @param webApi CPathWebService API.
     */
    public cPathSearchPanel(CPathWebService webApi, CPath2Factory factory) {
    	this.factory = factory;
    	
        //  Store the web API model
        this.webApi = webApi;

        //  Create shared model classes
        interactionBundleModel = new InteractionBundleModel();
        pathwayTableModel = new PathwayTableModel();

        //  Create main Border Layout
        setLayout(new BorderLayout());

        //  Create North Panel:  Search Box
        searchBoxPanel = factory.createSearchBoxPanel(webApi);
        add(searchBoxPanel, BorderLayout.NORTH);

        cards = new JPanel(new CardLayout());
        searchHitsPanel = createSearchResultsPanel();

        JPanel aboutPanel = createAboutPanel();
        cards.add (aboutPanel, "ABOUT");
        cards.add(searchHitsPanel, "HITS");
        add(cards, BorderLayout.CENTER);
        webApi.addApiListener(this);
        this.setMinimumSize(new Dimension (300,40));
    }

    public void showAboutPanel() {
        CardLayout cl = (CardLayout)(cards.getLayout());
        cl.show(cards, "ABOUT");
    }

    private JPanel createAboutPanel() {
        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new BorderLayout());
        GradientHeader header = new GradientHeader("About");

        aboutPanel.add(header, BorderLayout.NORTH);
        JTextPane textPane = PhysicalEntityDetailsPanel.createHtmlTextPane(factory.getOpenBrowser());
        textPane.setText(CPathProperties.getInstance().getCPathBlurb());
        aboutPanel.add(textPane, BorderLayout.CENTER);
        return aboutPanel;
    }

    public void searchInitiatedForPhysicalEntities(String keyword, int ncbiTaxonomyId) {
    }

    public void searchCompletedForPhysicalEntities(SearchResponseType peSearchResponse) {
        if (peSearchResponse.getTotalNumHits() > 0) {
            if (!searchHitsPanel.isVisible()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        CardLayout cl = (CardLayout)(cards.getLayout());
                        cl.show(cards, "HITS");                }
                });
            }
        }
    }

    public void requestInitiatedForParentSummaries(long primaryId) {
        //  Currently no-op
    }

    public void requestCompletedForParentSummaries(long primaryId,
            SummaryResponseType summaryResponse) {
        //  Currently no-op
    }

    /**
     * Initialize the Focus.  Can only be called after component has been
     * packed and displayed.
     */
    public void initFocus() {
        searchBoxPanel.requestFocusInWindow();
    }

    /**
     * Creates the Search Results Split Pane.
     *
     * @return JSplitPane Object.
     */
    private JPanel createSearchResultsPanel() {
        JPanel hitListPanel = factory.createSearchHitsPanel(this.interactionBundleModel,
                this.pathwayTableModel, webApi);
        return hitListPanel;
    }
}