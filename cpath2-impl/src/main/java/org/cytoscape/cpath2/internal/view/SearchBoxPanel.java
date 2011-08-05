package org.cytoscape.cpath2.internal.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.task.ExecutePhysicalEntitySearchTaskFactory;
import org.cytoscape.cpath2.internal.task.ExecutePhysicalEntitySearchTaskFactory.ResultHandler;
import org.cytoscape.cpath2.internal.view.model.Organism;
import org.cytoscape.cpath2.internal.web_service.CPathProperties;
import org.cytoscape.cpath2.internal.web_service.CPathWebService;

/**
 * Search Box Panel.
 *
 * @author Ethan Cerami.
 */
public class SearchBoxPanel extends JPanel {
    private JButton searchButton;
    private final CPathWebService webApi;
    private static final String ENTER_TEXT = "Enter Gene Name or ID";
    private PulsatingBorder pulsatingBorder;
    private JComboBox organismComboBox;
	private final CPath2Factory factory;
	
    /**
     * Constructor.
     *
     * @param webApi CPath Web Service Object.
     */
    public SearchBoxPanel(CPathWebService webApi, CPath2Factory factory) {
        this.webApi = webApi;
        this.factory = factory;
        
        GradientHeader header = new GradientHeader("Step 1:  Search");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(boxLayout);
        add (header);
        add (Box.createVerticalStrut(5));

        JPanel centerPanel = new JPanel();
        BoxLayout boxLayoutMain = new BoxLayout(centerPanel, BoxLayout.X_AXIS);
        centerPanel.setLayout(boxLayoutMain);
        centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        final JTextField searchField = createSearchField();

        pulsatingBorder = new PulsatingBorder (searchField);
        searchField.setBorder (BorderFactory.createCompoundBorder(searchField.getBorder(),
                pulsatingBorder));

        organismComboBox = createOrganismComboBox();
        searchButton = createSearchButton(searchField);

        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JEditorPane label = new JEditorPane ("text/html", "Examples:  <a href='TP53'>TP53</a>, " +
                "<a href='BRCA1'>BRCA1</a>, or <a href='SRY'>SRY</a>.");
        label.setEditable(false);
        label.setOpaque(false);
        label.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        label.addHyperlinkListener(new HyperlinkListener() {

            // Update search box with activated example.
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    searchField.setText(hyperlinkEvent.getDescription());
                }
            }
        });
        
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        Font font = label.getFont();
        Font newFont = new Font (font.getFamily(), font.getStyle(), font.getSize()-2);
        label.setFont(newFont);
        label.setBorder(new EmptyBorder(5,3,3,3));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerPanel.add(searchField);

        organismComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(organismComboBox);
        centerPanel.add(searchButton);

        add(centerPanel);
        add(label);
    }

    /**
     * Creates the Organism Combo Box.
     *
     * @return JComboBox Object.
     */
    private JComboBox createOrganismComboBox() {
        //  Organism List is currently hard-coded.
        Vector organismList = new Vector();
        organismList.add(new Organism("All Organisms", -1));
        CPathProperties props = CPathProperties.getInstance();
        organismList.addAll(props.getOrganismList());
        DefaultComboBoxModel organismComboBoxModel = new DefaultComboBoxModel(organismList);
        JComboBox organismComboBox = new JComboBox(organismComboBoxModel);
        organismComboBox.setToolTipText("Select Organism");
        organismComboBox.setMaximumSize(new Dimension(200, 9999));
        organismComboBox.setPrototypeDisplayValue("12345678901234567");
        return organismComboBox;
    }

    /**
     * Creates the Search Field and associated listener(s)
     *
     * @return JTextField Object.
     */
    private JTextField createSearchField() {
        final JTextField searchField = new JTextField(ENTER_TEXT.length());
        searchField.setText(ENTER_TEXT);
        searchField.setToolTipText(ENTER_TEXT);
        searchField.setMaximumSize(new Dimension(200, 9999));
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                if (searchField.getText() != null
                        && searchField.getText().startsWith("Enter")) {
                    searchField.setText("");
                }
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == 10) {
                    Organism organism = (Organism) organismComboBox.getSelectedItem();
                    executeSearch(searchField.getText(), organism.getNcbiTaxonomyId(),
                            organism.getSpeciesName());
                }
            }
        });
        return searchField;
    }

    /**
     * Creates the Search Button and associated action listener.
     *
     * @param searchField JTextField searchField
     * @return
     */
    private JButton createSearchButton(final JTextField searchField) {
        URL url = GradientHeader.class.getResource("resources/run_tool.gif");
        ImageIcon icon = new ImageIcon(url);
        //searchButton = new JButton(icon);
        searchButton = new JButton("Search");
        searchButton.setToolTipText("Execute Search");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Organism organism = (Organism) organismComboBox.getSelectedItem();
                executeSearch(searchField.getText(), organism.getNcbiTaxonomyId(),
                        organism.getSpeciesName());
            }
        });
        return searchButton;
    }

    private void executeSearch(final String keyword, int ncbiTaxonomyId, final String speciesName) {
        Window window = factory.getCySwingApplication().getJFrame();
        if (keyword == null || keyword.trim().length() == 0
                || keyword.startsWith(ENTER_TEXT)) {
            JOptionPane.showMessageDialog(window, "Please enter a Gene Name or ID.",
                    "Search Error", JOptionPane.ERROR_MESSAGE);
        } else {
        	ResultHandler handler = new ResultHandler() {
        		@Override
        		public void finished(int matchesFound) throws Exception {
                    if (matchesFound == 0) {
                        JOptionPane.showMessageDialog(factory.getCySwingApplication().getJFrame(),
                                "No matches found for:  " + keyword + " [" + speciesName + "]" +
                                "\nPlease try a different search term and/or organism filter.",
                                "No matches found.",
                                JOptionPane.WARNING_MESSAGE);
                    }
        		}
        	};
            ExecutePhysicalEntitySearchTaskFactory search = new ExecutePhysicalEntitySearchTaskFactory
                    (webApi, keyword.trim(), ncbiTaxonomyId, handler);
            factory.getTaskManager().execute(search);
        }
    }

    /**
     * Initializes Focus to the Search Button.
     */
    public void initFocus() {
        searchButton.requestFocusInWindow();
    }
}
