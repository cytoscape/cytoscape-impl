package org.cytoscape.cpath2.internal.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.schemas.summary_response.BasicRecordType;
import org.cytoscape.cpath2.internal.view.model.NetworkWrapper;
import org.cytoscape.cpath2.internal.web_service.CPathProperties;
import org.cytoscape.cpath2.internal.web_service.CPathResponseFormat;
import org.cytoscape.cpath2.internal.web_service.CPathWebService;
import org.cytoscape.cpath2.internal.web_service.CPathWebServiceImpl;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskManager;

/**
 * Download Details Frame.
 *
 * @author Ethan Cerami
 */
public class DownloadDetails extends JDialog {
    private MergePanel mergePanel;
    private long ids[];
    private String peName;
    private final CPath2Factory factory;
    
    /**
     * Constructor.
     * @param passedRecordList      List of Records that Passed over Filter.
     * @param peName                Name of Physical Entity.
     * @param bpContainer 
     */
    public DownloadDetails(List<BasicRecordType> passedRecordList, String peName, CPath2Factory factory) {    	
        super();
        this.factory = factory;
        
        this.peName = peName;
        this.setTitle("Retrieval Confirmation");
        this.setModal(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        GradientHeader header = new GradientHeader("Confirm Retrieval: "+ passedRecordList.size()
                + " records");
        contentPane.add(header, BorderLayout.NORTH);

        DefaultTableModel tableModel = new NonEditableTableModel();
        Vector headerList = new Vector();
        headerList.add("Name (if available)");
        headerList.add("Type");
        headerList.add("Data Source");
        tableModel.setColumnIdentifiers(headerList);
        tableModel.setRowCount(passedRecordList.size());
        JTable table = new JTable(tableModel);

        //  Adjust width / height of viewport;  fixes bug #1620.
        Dimension d = table.getPreferredSize();
        d.width = d.width * 2;
        if (d.height > 200) {
            d.height = 200;
        }
        table.setPreferredScrollableViewportSize(d);
        table.setAutoCreateColumnsFromModel(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ids = new long[passedRecordList.size()];
        int i = 0;
        for (BasicRecordType record : passedRecordList) {
            if (record.getName().equalsIgnoreCase("N/A")) {
                record.setName("---");
            }
            tableModel.setValueAt(record.getName(), i, 0);
            tableModel.setValueAt(record.getEntityType(), i, 1);
            if (record.getDataSource() != null) {
                tableModel.setValueAt(record.getDataSource().getName(), i, 2);
            } else {
                tableModel.setValueAt("---", i, 3);
            }
            ids[i++] = record.getPrimaryId();
        }
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel(this);
        mergePanel = factory.createMergePanel();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        if (mergePanel != null) {
            panel.add(mergePanel);
        }
        panel.add(buttonPanel);
        contentPane.add(panel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(factory.getCySwingApplication().getJFrame());
    }

    /**
     * Button Panel.
     */
    private JPanel createButtonPanel(final JDialog dialog) {
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
                dialog.dispose();
                downloadInteractions();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    /**
     * Downloads interaction bundles in a new thread.
     */
    private void downloadInteractions() {
        CyNetwork networkToMerge = null;
        JComboBox networkComboBox = mergePanel.getNetworkComboBox();
        if (networkComboBox != null) {
            NetworkWrapper netWrapper = (NetworkWrapper) networkComboBox.getSelectedItem();
            if (netWrapper != null) {
                networkToMerge = netWrapper.getNetwork();
            }
        }
        downloadInteractions(networkToMerge);
    }

    public void downloadInteractions(CyNetwork networkToMerge) {
        String networkTitle = peName + ":  Network";
        CPathWebService webApi = CPathWebServiceImpl.getInstance();

        CPathResponseFormat format;
        CPathProperties config = CPathProperties.getInstance();
        if (config.getDownloadMode() == CPathProperties.DOWNLOAD_FULL_BIOPAX) {
            format = CPathResponseFormat.BIOPAX;
        } else {
            format = CPathResponseFormat.BINARY_SIF;
        }

        TaskManager taskManager = factory.getTaskManager();
        taskManager.execute(factory.createExecuteGetRecordByCPathIdTaskFactory(webApi, ids, format, networkTitle, networkToMerge));
    }
}

class NonEditableTableModel extends DefaultTableModel {

    /**
     * Constructor.
     */
    public NonEditableTableModel() {
        super();
    }

    /**
     * Is the specified cell editable?  Never!
     *
     * @param row row index.
     * @param col col index.
     * @return false.
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}

