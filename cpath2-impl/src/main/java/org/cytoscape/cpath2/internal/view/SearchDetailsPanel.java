package org.cytoscape.cpath2.internal.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.task.ExecuteGetRecordByCPathIdTaskFactory;
import org.cytoscape.cpath2.internal.util.NetworkMergeUtil;
import org.cytoscape.cpath2.internal.view.model.InteractionBundleModel;
import org.cytoscape.cpath2.internal.view.model.NetworkWrapper;
import org.cytoscape.cpath2.internal.view.model.PathwayTableModel;
import org.cytoscape.cpath2.internal.web_service.CPathProperties;
import org.cytoscape.cpath2.internal.web_service.CPathResponseFormat;
import org.cytoscape.cpath2.internal.web_service.CPathWebService;
import org.cytoscape.cpath2.internal.web_service.CPathWebServiceImpl;

/**
 * Search Details Panel.
 *
 * @author Ethan Cerami.
 */
public class SearchDetailsPanel extends JPanel {
	
    private CPath2Factory factory;

	/**
     * Constructor.
     *
     * @param interactionBundleModel InteractionBundleModel Object.
     * @param pathwayTableModel     PathwayTableModel Object.
     * @param application 
     * @param taskManager 
     */
    public SearchDetailsPanel(InteractionBundleModel interactionBundleModel,
            PathwayTableModel pathwayTableModel, CPath2Factory factory) {
    	this.factory = factory;
        GradientHeader header = new GradientHeader("Step 3:  Select Network(s)");
        setLayout(new BorderLayout());
        this.add(header, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel interactionPanel = factory.createInteractionBundlePanel(interactionBundleModel);
        JPanel pathwayPane = createPathwayPane(pathwayTableModel);
        Font font = tabbedPane.getFont();
        Font newFont = new Font (font.getFamily(), Font.PLAIN, font.getSize()-2);
        tabbedPane.setFont(newFont);

        tabbedPane.add("Pathways", pathwayPane);
        tabbedPane.add("Interaction Networks", interactionPanel);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPathwayPane(PathwayTableModel pathwayTableModel) {
        JPanel pathwayPane = new JPanel(new BorderLayout());
        JScrollPane pathwayTable = createPathwayTable(pathwayTableModel);
        pathwayPane.add(pathwayTable, BorderLayout.CENTER);
        JLabel label = new JLabel ("> Double-click pathway to retrieve.");
        label.setForeground(Color.BLUE);
        Font font = label.getFont();
        Font newFont = new Font(font.getFamily(), Font.PLAIN, font.getSize()-2);
        label.setFont(newFont);
        pathwayPane.add(label, BorderLayout.SOUTH);
        return pathwayPane;
    }
    /**
     * Creates the Pathway Table.
     *
     * @return JScrollPane Object.
     */
    private JScrollPane createPathwayTable(final PathwayTableModel pathwayTableModel) {
        final JTable pathwayTable = new JTable(pathwayTableModel);
        pathwayTable.setAutoCreateColumnsFromModel(true);
        pathwayTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        pathwayTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int rows[] = pathwayTable.getSelectedRows();
                    if (rows.length > 0) {
                        downloadPathway(rows, pathwayTableModel);
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(pathwayTable);
        return scrollPane;
    }

    /**
     * Downloads a single pathway in a new thread.
     * @param rows                  Selected row.
     * @param pathwayTableModel     Pathway Table Model.
     */
    private void downloadPathway(int[] rows, PathwayTableModel pathwayTableModel) {
        try {
            NetworkWrapper mergeNetwork = null;
            NetworkMergeUtil mergeUtil = factory.getNetworkMergeUtil();
            if (mergeUtil.mergeNetworksExist()) {
                mergeNetwork = mergeUtil.promptForNetworkToMerge();
                if (mergeNetwork == null) {
                    return;
                }
            }

            long internalId = pathwayTableModel.getInternalId(rows[0]);
            String title = pathwayTableModel.getValueAt(rows[0], 0)
                    + " (" + pathwayTableModel.getValueAt(rows[0], 1) + ")";
            long ids[] = new long[1];
            ids[0] = internalId;

            CPathWebService webApi = CPathWebServiceImpl.getInstance();
            ExecuteGetRecordByCPathIdTaskFactory taskFactory;

            CPathResponseFormat format;
            CPathProperties config = CPathProperties.getInstance();
            if (config.getDownloadMode() == CPathProperties.DOWNLOAD_FULL_BIOPAX) {
                format = CPathResponseFormat.BIOPAX;
            } else {
                format = CPathResponseFormat.BINARY_SIF;
            }

            if (mergeNetwork != null && mergeNetwork.getNetwork() != null) {
                taskFactory = factory.createExecuteGetRecordByCPathIdTaskFactory(webApi, ids, format,
                        title, mergeNetwork.getNetwork());
            } else {
                taskFactory = factory.createExecuteGetRecordByCPathIdTaskFactory(webApi, ids, format, title);
            }
            factory.getTaskManager().execute(taskFactory);
        } catch (IndexOutOfBoundsException e) {
            //  Ignore
        }
    }
}

