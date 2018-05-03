package com;

import com.bean.clsItemDtl;
import static com.clsGlobalClass.*;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author Sanguine
 */
public class frmBillRegeneration extends javax.swing.JFrame
{

    private HashMap<String, String> mapPOSName = null;
    private HashMap<String, String> mapSettlementType = null;
    private Connection connection = null;
    private ResultSet resultSet = null;
    private Statement statement = null;
    private StringBuilder sqlBuilder = null;
    private DefaultTableModel dtmSalesTableModel = null;
    private final DefaultTableModel dtmTotalSales;
    private JCheckBox chkBoxSelectAll = null;
    private SimpleDateFormat dateFormat;
    private String strFromDate = "";
    private String strToDate = "";
    private String lastBillNo = "";
    private StringBuilder deleteSql;
    private StringBuilder updateSql;
    private LinkedHashMap<String, String> mapPosCodeWiseFirstBillNos = null;
    private LinkedHashMap<String, Integer> mapCountPosWiseBillToBeDeleted = null;
    private String dbName = "";
    private String operationTypeForTax;
    private final SimpleDateFormat yyyyMMddDateFormat;
    private final SimpleDateFormat ddMMyyyyDateFormat;
    private int selectedQty = 1;

    private clsUtility objUtility;

    /**
     * This method is used to initialize frmPosMaster
     */
    public frmBillRegeneration()
    {
	initComponents();
	this.setLocationRelativeTo(null);
	sqlBuilder = new StringBuilder();
	deleteSql = new StringBuilder();
	updateSql = new StringBuilder();
	mapPOSName = new HashMap<String, String>();
	mapSettlementType = new HashMap<String, String>();
	yyyyMMddDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	ddMMyyyyDateFormat = new SimpleDateFormat("dd-MM-yyyy");

	try
	{
	    funDatabaseConnection();
	    funSetClientCode();
	    funLoadMapPOSName();
	    funLoadMapSettlementType();
	    funLoadMenuHeads();

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	funSetCmbPOSName();
	funSetCmbSettlementtype();

	funFillBillSeriesCombo();

	tblSalesTable = new javax.swing.JTable();
	dtmSalesTableModel = new javax.swing.table.DefaultTableModel(
		new Object[][]
		{

		},
		new String[]
		{
		    "Bill No", "Date", "Settlement Type", "Settlement Mode", "Discount", "Tax", "Grand Total", "select"
		}
	)
	{
	    Class[] types = new Class[]
	    {
		java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
	    };
	    boolean[] canEdit = new boolean[]
	    {
		false, false, false, false, false, false, false, true
	    };

	    public Class getColumnClass(int columnIndex)
	    {
		return types[columnIndex];
	    }

	    public boolean isCellEditable(int rowIndex, int columnIndex)
	    {
		return canEdit[columnIndex];
	    }
	};
	tblSalesTable.setModel(dtmSalesTableModel);
	tblSalesTable.setRowHeight(25);
	tblSalesTable.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mouseClicked(java.awt.event.MouseEvent evt)
	    {
		tblSalesTableMouseClicked(evt);
	    }
	});
	pnlSalesData.setViewportView(tblSalesTable);
	if (tblSalesTable.getColumnModel().getColumnCount() > 0)
	{
	    tblSalesTable.getColumnModel().getColumn(0).setHeaderValue("Bill No");
	    tblSalesTable.getColumnModel().getColumn(1).setHeaderValue("Date");
	    tblSalesTable.getColumnModel().getColumn(2).setHeaderValue("Settlement Type");
	    tblSalesTable.getColumnModel().getColumn(3).setHeaderValue("Settlement Mode");
	    tblSalesTable.getColumnModel().getColumn(4).setHeaderValue("Discount");
	    tblSalesTable.getColumnModel().getColumn(5).setHeaderValue("Tax");
	    tblSalesTable.getColumnModel().getColumn(6).setHeaderValue("Grand Total");
	    tblSalesTable.getColumnModel().getColumn(7).setHeaderValue("select");
	}
	tblSalesTable.setRowHeight(25);
	tblSalesTable.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mouseClicked(java.awt.event.MouseEvent evt)
	    {
		tblSalesTableMouseClicked(evt);
	    }
	});
	DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
	leftRenderer.setHorizontalAlignment(JLabel.LEFT);
	DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
	rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

	tblSalesTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
	tblSalesTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
	tblSalesTable.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
	tblSalesTable.getColumnModel().getColumn(3).setCellRenderer(leftRenderer);
	tblSalesTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
	tblSalesTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
	tblSalesTable.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

	tblSalesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	tblSalesTable.getColumnModel().getColumn(0).setPreferredWidth(110);
	tblSalesTable.getColumnModel().getColumn(1).setPreferredWidth(150);
	tblSalesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
	tblSalesTable.getColumnModel().getColumn(3).setPreferredWidth(110);
	tblSalesTable.getColumnModel().getColumn(4).setPreferredWidth(100);
	tblSalesTable.getColumnModel().getColumn(5).setPreferredWidth(120);
	tblSalesTable.getColumnModel().getColumn(6).setPreferredWidth(90);
	chkBoxSelectAll = new JCheckBox();
	chkBoxSelectAll.addActionListener(new ActionListener()
	{

	    @Override
	    public void actionPerformed(ActionEvent e)
	    {

		if (chkBoxSelectAll.isSelected())
		{
		    for (int i = 0; i < tblSalesTable.getRowCount(); i++)
		    {
			tblSalesTable.setValueAt(Boolean.parseBoolean("true"), i, 7);
		    }
		}
		else
		{
		    for (int i = 0; i < tblSalesTable.getRowCount(); i++)
		    {
			tblSalesTable.setValueAt(Boolean.parseBoolean("false"), i, 7);
		    }

		}
		//set labels value
		funSetLabelValues();
	    }
	});
	TableColumnModel columnModel = tblSalesTable.getColumnModel();
	JTableHeader header = tblSalesTable.getTableHeader();
	header.add(chkBoxSelectAll);
	header.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

	dtmTotalSales = new DefaultTableModel()
	{
	    @Override
	    public boolean isCellEditable(int row, int column)
	    {
		//all cells false
		return false;
	    }
	};
	dtmTotalSales.addColumn("");
	dtmTotalSales.addColumn("");
	dtmTotalSales.addColumn("");
	dtmTotalSales.addColumn("");
	dtmTotalSales.addColumn("Discount");
	dtmTotalSales.addColumn("Tax");
	dtmTotalSales.addColumn("Grand Total");
	dtmTotalSales.addColumn("");

	tblTotal.setModel(dtmTotalSales);

	tblTotal.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
	tblTotal.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
	tblTotal.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

	tblTotal.getColumnModel().getColumn(0).setPreferredWidth(110);
	tblTotal.getColumnModel().getColumn(1).setPreferredWidth(150);
	tblTotal.getColumnModel().getColumn(2).setPreferredWidth(100);
	tblTotal.getColumnModel().getColumn(3).setPreferredWidth(100);
	tblTotal.getColumnModel().getColumn(4).setPreferredWidth(110);
	tblTotal.getColumnModel().getColumn(5).setPreferredWidth(100);
	tblTotal.getColumnModel().getColumn(6).setPreferredWidth(120);
	tblTotal.getColumnModel().getColumn(7).setPreferredWidth(90);

	funResetButtonClicked();

	objUtility = new clsUtility();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        panelHeader = new javax.swing.JPanel();
        lblProductName = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        lblUserCode = new javax.swing.JLabel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblPosName = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        panelLayout = new JPanel() {  
            public void paintComponent(Graphics g) {  
                Image img = Toolkit.getDefaultToolkit().getImage(  
                    getClass().getResource("/com/imgBGJPOS.png"));  
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);  
            }  
        };
        panelbody = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        panelDeletion = new JPanel() {  
            public void paintComponent(Graphics g) {  
                Image img = Toolkit.getDefaultToolkit().getImage(  
                    getClass().getResource("/com/imgBGJPOS.png"));  
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);  
            }  
        };
        lblPOSName = new javax.swing.JLabel();
        cmbPosCode = new javax.swing.JComboBox();
        lblFromDate = new javax.swing.JLabel();
        dteFromDate = new com.toedter.calendar.JDateChooser();
        dteToDate = new com.toedter.calendar.JDateChooser();
        lblToDate = new javax.swing.JLabel();
        lblFromAmount = new javax.swing.JLabel();
        txtFromAmount = new javax.swing.JTextField();
        lblToAmount = new javax.swing.JLabel();
        txtToAmount = new javax.swing.JTextField();
        lblSettlementType = new javax.swing.JLabel();
        cmbSettlementType = new javax.swing.JComboBox();
        btnExecute = new javax.swing.JButton();
        pnlSalesData = new javax.swing.JScrollPane();
        tblSalesTable = new javax.swing.JTable();
        pnlsalesTotal = new javax.swing.JScrollPane();
        tblTotal = new javax.swing.JTable();
        btnItemDeletion = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        btnRefreshTable = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        lblSettlementMode = new javax.swing.JLabel();
        cmbSettlementMode = new javax.swing.JComboBox();
        cmbItemDeletionInsertionType = new javax.swing.JComboBox();
        btnItemInsertion = new javax.swing.JButton();
        lblSettlementType1 = new javax.swing.JLabel();
        lblBillSeries = new javax.swing.JLabel();
        cmbBillSeries = new javax.swing.JComboBox();
        panelItemSelection = new JPanel() {  
            public void paintComponent(Graphics g) {  
                Image img = Toolkit.getDefaultToolkit().getImage(  
                    getClass().getResource("/com/imgBGJPOS.png"));  
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);  
            }  
        };
        lblFromAmount1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listViewOfMenuHead = new javax.swing.JList();
        lblFromAmount2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listViewOfMenuHeadItems = new javax.swing.JList();
        lblFromAmount3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listViewOfSelectedItems = new javax.swing.JList();
        btbMoveSelectedItemsToRight = new javax.swing.JButton();
        btbMoveSelectedItemsToLeft = new javax.swing.JButton();
        btnClearMenuHeadWiseDeletion = new javax.swing.JButton();
        lblNoOfBillsSelectedName = new javax.swing.JLabel();
        lblNoOfBillsSelectedValue = new javax.swing.JLabel();
        lblSelectedSettlementModeName = new javax.swing.JLabel();
        lblSelectedSettlementModeValue = new javax.swing.JLabel();
        lblSelectedDiscAmtName = new javax.swing.JLabel();
        lblSelectedDiscAmtValue = new javax.swing.JLabel();
        lblSelectedTaxAmtName = new javax.swing.JLabel();
        lblSelectedTaxAmtValue = new javax.swing.JLabel();
        lblSelectedGTAmtName = new javax.swing.JLabel();
        lblSelectedGTAmtValue = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new java.awt.Dimension(800, 600));
        setUndecorated(true);

        panelHeader.setBackground(new java.awt.Color(69, 164, 238));
        panelHeader.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                panelHeaderMouseClicked(evt);
            }
        });
        panelHeader.setLayout(new javax.swing.BoxLayout(panelHeader, javax.swing.BoxLayout.LINE_AXIS));

        lblProductName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblProductName.setForeground(new java.awt.Color(255, 255, 255));
        lblProductName.setText("SPOS -Bill ReGeneration ");
        panelHeader.add(lblProductName);
        panelHeader.add(filler4);

        lblUserCode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));
        lblUserCode.setMaximumSize(new java.awt.Dimension(71, 30));
        lblUserCode.setMinimumSize(new java.awt.Dimension(71, 30));
        lblUserCode.setPreferredSize(new java.awt.Dimension(71, 30));
        panelHeader.add(lblUserCode);
        panelHeader.add(filler5);

        lblPosName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblPosName.setForeground(new java.awt.Color(255, 255, 255));
        lblPosName.setMaximumSize(new java.awt.Dimension(321, 30));
        lblPosName.setMinimumSize(new java.awt.Dimension(321, 30));
        lblPosName.setPreferredSize(new java.awt.Dimension(321, 30));
        panelHeader.add(lblPosName);
        panelHeader.add(filler6);

        getContentPane().add(panelHeader, java.awt.BorderLayout.PAGE_START);

        panelLayout.setLayout(new java.awt.GridBagLayout());

        panelbody.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        panelbody.setMinimumSize(new java.awt.Dimension(800, 570));
        panelbody.setOpaque(false);

        panelDeletion.setOpaque(false);

        lblPOSName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblPOSName.setText("POS Name :");

        cmbPosCode.setToolTipText("Select POS");

        lblFromDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromDate.setText("From Date :");

        lblToDate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToDate.setText("To :");

        lblFromAmount.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblFromAmount.setText("From Amount:");

        txtFromAmount.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                txtFromAmountActionPerformed(evt);
            }
        });

        lblToAmount.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblToAmount.setText("To Amount:");

        lblSettlementType.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSettlementType.setText("Settlement Type:");

        cmbSettlementType.setToolTipText("Select Settlement Mode");
        cmbSettlementType.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbSettlementTypeActionPerformed(evt);
            }
        });

        btnExecute.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExecute.setText("Execute");
        btnExecute.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnExecuteMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                btnExecuteMouseEntered(evt);
            }
        });

        tblSalesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Bill No", "Date", "Settlement Type", "Settlement Mode", "Discount", "Tax", "Grand Total", "select"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex)
            {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        tblSalesTable.setRowHeight(25);
        tblSalesTable.getTableHeader().setReorderingAllowed(false);
        tblSalesTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                tblSalesTableMouseClicked(evt);
            }
        });
        pnlSalesData.setViewportView(tblSalesTable);

        tblTotal.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        tblTotal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null, null, null, null, null}
            },
            new String []
            {
                "Title1", " Title2", " Title3", " Title4", " Title5", " Title6", " Title7", " Title8"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        tblTotal.setRowHeight(25);
        pnlsalesTotal.setViewportView(tblTotal);

        btnItemDeletion.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnItemDeletion.setText("Item Deletion");
        btnItemDeletion.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnItemDeletionMouseClicked(evt);
            }
        });

        btnReset.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnReset.setText("Reset");
        btnReset.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnResetMouseClicked(evt);
            }
        });

        btnRefreshTable.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnRefreshTable.setText("Refresh");
        btnRefreshTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnRefreshTableMouseClicked(evt);
            }
        });

        btnEdit.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setToolTipText("");
        btnEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEdit.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnEditMouseClicked(evt);
            }
        });

        btnDelete.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setToolTipText("");
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnDeleteMouseClicked(evt);
            }
        });

        btnExit.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExit.setText("Exit");
        btnExit.setToolTipText("");
        btnExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExit.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnExitMouseClicked(evt);
            }
        });

        lblSettlementMode.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSettlementMode.setText("Mode:");

        cmbSettlementMode.setToolTipText("Select Settlement Mode");

        cmbItemDeletionInsertionType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BILL DELETION", "ITEM DELETION", "ITEM INSERTION" }));
        cmbItemDeletionInsertionType.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbItemDeletionInsertionTypeActionPerformed(evt);
            }
        });

        btnItemInsertion.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnItemInsertion.setText("Item Insertion");
        btnItemInsertion.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnItemInsertionMouseClicked(evt);
            }
        });

        lblSettlementType1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblSettlementType1.setText("Operation Type:");

        lblBillSeries.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblBillSeries.setText("Bill Series      :");

        cmbBillSeries.setToolTipText("Select Settlement Mode");
        cmbBillSeries.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbBillSeriesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelDeletionLayout = new javax.swing.GroupLayout(panelDeletion);
        panelDeletion.setLayout(panelDeletionLayout);
        panelDeletionLayout.setHorizontalGroup(
            panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSalesData, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(pnlsalesTotal)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDeletionLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnItemInsertion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnItemDeletion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRefreshTable, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(panelDeletionLayout.createSequentialGroup()
                .addComponent(lblPOSName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbPosCode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFromDate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dteFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblToDate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dteToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSettlementType1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbItemDeletionInsertionType, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(panelDeletionLayout.createSequentialGroup()
                .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFromAmount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblBillSeries, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDeletionLayout.createSequentialGroup()
                        .addComponent(cmbBillSeries, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(panelDeletionLayout.createSequentialGroup()
                        .addComponent(txtFromAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblToAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtToAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSettlementType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSettlementType, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSettlementMode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSettlementMode, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExecute, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        panelDeletionLayout.setVerticalGroup(
            panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDeletionLayout.createSequentialGroup()
                .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dteToDate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblToDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelDeletionLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblFromDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(dteFromDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelDeletionLayout.createSequentialGroup()
                        .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblPOSName, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbPosCode, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbItemDeletionInsertionType, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblSettlementType1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblSettlementType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtToAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtFromAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblFromAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblToAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cmbSettlementType, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbSettlementMode, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblSettlementMode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBillSeries, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbBillSeries, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(pnlSalesData, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(pnlsalesTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDeletionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefreshTable, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnItemDeletion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnItemInsertion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        tabbedPane.addTab("Deletion", panelDeletion);

        panelItemSelection.setOpaque(false);

        lblFromAmount1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblFromAmount1.setText("Menu Head");

        listViewOfMenuHead.setModel(new DefaultListModel());
        listViewOfMenuHead.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listViewOfMenuHead.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                listViewOfMenuHeadMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(listViewOfMenuHead);

        lblFromAmount2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblFromAmount2.setText("Menu Items");

        listViewOfMenuHeadItems.setModel(new DefaultListModel());
        jScrollPane2.setViewportView(listViewOfMenuHeadItems);

        lblFromAmount3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblFromAmount3.setText("Selected Items");

        listViewOfSelectedItems.setModel(new DefaultListModel());
        listViewOfSelectedItems.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(listViewOfSelectedItems);

        btbMoveSelectedItemsToRight.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btbMoveSelectedItemsToRight.setText(">>");
        btbMoveSelectedItemsToRight.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btbMoveSelectedItemsToRightMouseClicked(evt);
            }
        });

        btbMoveSelectedItemsToLeft.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btbMoveSelectedItemsToLeft.setText("<<");
        btbMoveSelectedItemsToLeft.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btbMoveSelectedItemsToLeftMouseClicked(evt);
            }
        });

        btnClearMenuHeadWiseDeletion.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnClearMenuHeadWiseDeletion.setText("CLEAR");
        btnClearMenuHeadWiseDeletion.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnClearMenuHeadWiseDeletionMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelItemSelectionLayout = new javax.swing.GroupLayout(panelItemSelection);
        panelItemSelection.setLayout(panelItemSelectionLayout);
        panelItemSelectionLayout.setHorizontalGroup(
            panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelItemSelectionLayout.createSequentialGroup()
                .addGroup(panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelItemSelectionLayout.createSequentialGroup()
                        .addGroup(panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFromAmount1)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelItemSelectionLayout.createSequentialGroup()
                                .addComponent(lblFromAmount2)
                                .addGap(0, 184, Short.MAX_VALUE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btbMoveSelectedItemsToLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btbMoveSelectedItemsToRight, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFromAmount3)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelItemSelectionLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClearMenuHeadWiseDeletion, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelItemSelectionLayout.setVerticalGroup(
            panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelItemSelectionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFromAmount3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFromAmount1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblFromAmount2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelItemSelectionLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelItemSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                            .addComponent(jScrollPane1)
                            .addComponent(jScrollPane3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClearMenuHeadWiseDeletion, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(panelItemSelectionLayout.createSequentialGroup()
                        .addGap(187, 187, 187)
                        .addComponent(btbMoveSelectedItemsToRight, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btbMoveSelectedItemsToLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        tabbedPane.addTab("Item Selection", panelItemSelection);

        lblNoOfBillsSelectedName.setText("No Of Selected Bills :");

        lblNoOfBillsSelectedValue.setForeground(new java.awt.Color(255, 51, 0));
        lblNoOfBillsSelectedValue.setText("0");

        lblSelectedSettlementModeName.setText("Settlement Mode :");

        lblSelectedSettlementModeValue.setForeground(new java.awt.Color(255, 51, 0));

        lblSelectedDiscAmtName.setText("Discount Amt :");

        lblSelectedDiscAmtValue.setForeground(new java.awt.Color(255, 51, 0));

        lblSelectedTaxAmtName.setText("Tax Amt :");

        lblSelectedTaxAmtValue.setForeground(new java.awt.Color(255, 51, 0));

        lblSelectedGTAmtName.setText("Grand Total :");

        lblSelectedGTAmtValue.setForeground(new java.awt.Color(255, 51, 0));

        javax.swing.GroupLayout panelbodyLayout = new javax.swing.GroupLayout(panelbody);
        panelbody.setLayout(panelbodyLayout);
        panelbodyLayout.setHorizontalGroup(
            panelbodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelbodyLayout.createSequentialGroup()
                .addGroup(panelbodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane)
                    .addGroup(panelbodyLayout.createSequentialGroup()
                        .addComponent(lblNoOfBillsSelectedName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNoOfBillsSelectedValue, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblSelectedSettlementModeName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSelectedSettlementModeValue, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblSelectedDiscAmtName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSelectedDiscAmtValue, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSelectedTaxAmtName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSelectedTaxAmtValue, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblSelectedGTAmtName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSelectedGTAmtValue, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelbodyLayout.setVerticalGroup(
            panelbodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelbodyLayout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 588, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelbodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelbodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblNoOfBillsSelectedName, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblNoOfBillsSelectedValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSelectedSettlementModeName, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSelectedSettlementModeValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelbodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSelectedDiscAmtName, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSelectedDiscAmtValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelbodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSelectedTaxAmtName, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSelectedTaxAmtValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelbodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSelectedGTAmtName, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSelectedGTAmtValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        panelLayout.add(panelbody, new java.awt.GridBagConstraints());

        getContentPane().add(panelLayout, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dteFromDateHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_dteFromDateHierarchyChanged

    }//GEN-LAST:event_dteFromDateHierarchyChanged

    private void dteFromDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dteFromDatePropertyChange

    }//GEN-LAST:event_dteFromDatePropertyChange

    private void dteToDateHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_dteToDateHierarchyChanged

    }//GEN-LAST:event_dteToDateHierarchyChanged

    private void dteToDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dteToDatePropertyChange

    }//GEN-LAST:event_dteToDatePropertyChange

    private void txtFromAmountActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtFromAmountActionPerformed
    {//GEN-HEADEREND:event_txtFromAmountActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_txtFromAmountActionPerformed

    private void btnExecuteMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnExecuteMouseClicked
    {//GEN-HEADEREND:event_btnExecuteMouseClicked
	try
	{
	    if (btnExecute.isEnabled())
	    {
		dtmSalesTableModel.setRowCount(0);
		dtmTotalSales.setRowCount(0);

		Date fromDate = ddMMyyyyDateFormat.parse(ddMMyyyyDateFormat.format(dteFromDate.getDate()));
		Date toDate = ddMMyyyyDateFormat.parse(ddMMyyyyDateFormat.format(dteToDate.getDate()));

		if (cmbPosCode.getSelectedIndex() < 0)
		{
		    JOptionPane.showMessageDialog(null, "Please select POS Name.");
		    return;
		}
		if (cmbSettlementType.getSelectedIndex() < 0)
		{
		    JOptionPane.showMessageDialog(null, "Please select Settelement Type.");
		    return;
		}
		if (dteFromDate.getDate() == null)
		{
		    JOptionPane.showMessageDialog(null, "Please select From Date.");
		    return;
		}
		if (dteToDate.getDate() == null)
		{
		    JOptionPane.showMessageDialog(null, "Please select To Date.");
		    return;
		}
		if (fromDate.after(toDate))
		{
		    JOptionPane.showMessageDialog(null, "Please select Valid Date");
		    return;
		}
		if (cmbPosCode.getSelectedIndex() < 1)
		{
		    clsGlobalClass.gPOSCode = "All";
		}
		else
		{
		    clsGlobalClass.gPOSCode = mapPOSName.get(cmbPosCode.getSelectedItem().toString());
		}

		//set labels value
		funSetLabelValues();

		if (cmbItemDeletionInsertionType.getSelectedItem().toString().equalsIgnoreCase("ITEM DELETION"))
		{
		    if (((DefaultListModel) listViewOfSelectedItems.getModel()).size() <= 0)
		    {
			JOptionPane.showMessageDialog(null, "Please select Item.");
			return;
		    }
		    else
		    {
			funFillTableForItemDeletion();
		    }
		}
		else if (cmbItemDeletionInsertionType.getSelectedItem().toString().equalsIgnoreCase("ITEM INSERTION"))
		{
		    if (((DefaultListModel) listViewOfSelectedItems.getModel()).size() <= 0)
		    {
			JOptionPane.showMessageDialog(null, "Please select Item.");
			return;
		    }
		    else
		    {
			funFillTableForItemInsertion();
		    }
		}
		else//for default logic BILL DELETION
		{
		    funFillTableForBillDeletion();
		}
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }//GEN-LAST:event_btnExecuteMouseClicked

    private void btnExecuteMouseEntered(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnExecuteMouseEntered
    {//GEN-HEADEREND:event_btnExecuteMouseEntered
	// TODO add your handling code here:
    }//GEN-LAST:event_btnExecuteMouseEntered

    private void tblSalesTableMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_tblSalesTableMouseClicked
    {//GEN-HEADEREND:event_tblSalesTableMouseClicked

	//set labels value
	funSetLabelValues();
    }//GEN-LAST:event_tblSalesTableMouseClicked

    private void btnItemDeletionMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnItemDeletionMouseClicked
    {//GEN-HEADEREND:event_btnItemDeletionMouseClicked
	if (btnItemDeletion.isEnabled())
	{
	    if (tblSalesTable.getRowCount() > 0)
	    {
		funDeleteMenuHeadWiseItemsFromBill();
	    }
	    else
	    {
		JOptionPane.showMessageDialog(null, "Please select Bill No.");
		return;
	    }
	}
    }//GEN-LAST:event_btnItemDeletionMouseClicked

    private void btnResetMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnResetMouseClicked
    {//GEN-HEADEREND:event_btnResetMouseClicked
	funResetButtonClicked();
    }//GEN-LAST:event_btnResetMouseClicked

    private void btnRefreshTableMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnRefreshTableMouseClicked
    {//GEN-HEADEREND:event_btnRefreshTableMouseClicked

	if (btnRefreshTable.isEnabled())
	{
	    if (tblSalesTable.getRowCount() > 0)
	    {
		funFillTableForBillDeletion();
	    }
	    else
	    {
		JOptionPane.showMessageDialog(null, "Please Execute First.");
	    }
	}
    }//GEN-LAST:event_btnRefreshTableMouseClicked

    private void btnEditMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnEditMouseClicked
    {//GEN-HEADEREND:event_btnEditMouseClicked
	if (btnEdit.isEnabled())
	{
	    try
	    {

		int selectedRow = tblSalesTable.getSelectedRow();
		System.out.println("selected row=" + selectedRow);
		if (selectedRow > -1)
		{
		    clsGlobalClass.gFromDate = yyyyMMddDateFormat.format(dteFromDate.getDate());
		    clsGlobalClass.gToDate = yyyyMMddDateFormat.format(dteToDate.getDate());
		    String billNo = tblSalesTable.getValueAt(selectedRow, 0).toString();
		    String billDate = tblSalesTable.getValueAt(selectedRow, 1).toString();
		    clsGlobalClass.gPOSCode = billNo.substring(0, 3);

		    frmEditBill editBill = new frmEditBill();
		    editBill.funGetBillDetail(billNo, billDate);
		    editBill.setVisible(true);
		}
		else
		{
		    JOptionPane.showMessageDialog(null, "Please Select Bill.");
		    return;
		}

	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }//GEN-LAST:event_btnEditMouseClicked

    private void btnDeleteMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnDeleteMouseClicked
    {//GEN-HEADEREND:event_btnDeleteMouseClicked

	btnDelete.setEnabled(false);
	JOptionPane.showMessageDialog(null, "This option is disabled.");

	if (btnDelete.isEnabled())
	{
	    try
	    {
		String checkDayEndQuery = "select a.strBillNo from tblbillhd a";
		resultSet = statement.executeQuery(checkDayEndQuery);
		if (resultSet.next())
		{
		    JOptionPane.showMessageDialog(null, "Please Check Day End process.");
		    return;
		}

		funGeneratePosWiseFirstBillNos();

		ArrayList<String> billNos = new ArrayList<String>();
		billNos.clear();
		for (int i = 0; i < tblSalesTable.getRowCount(); i++)
		{
		    boolean isDelete = Boolean.parseBoolean(String.valueOf(tblSalesTable.getValueAt(i, 7)));
		    if (isDelete)
		    {
			billNos.add(String.valueOf(tblSalesTable.getValueAt(i, 0)));
		    }
		}
		if (billNos.size() > 0)
		{
		    funCountPosWiseBillToBeDeleted();

		    Iterator<String> it = billNos.iterator();
		    String bill = "";
		    while (it.hasNext())
		    {
			bill += ",'" + it.next() + "'";
		    }
		    bill = bill.substring(1, bill.length());
		    String bill1 = bill;
		    //  System.out.println("bill=="+bill);
		    //  tblqbillhd
		    deleteSql.setLength(0);
		    deleteSql.append("delete from tblqbillhd where strBillNo In (" + bill1 + ")");
		    statement.executeUpdate(deleteSql.toString());
		    //tblqbilldtl
		    deleteSql.setLength(0);
		    deleteSql.append("delete from tblqbilldtl where strBillNo In (" + bill1 + ")");
		    statement.executeUpdate(deleteSql.toString());
		    //tblqbillmodifierdtl
		    deleteSql.setLength(0);
		    deleteSql.append("delete from tblqbillmodifierdtl where strBillNo In (" + bill1 + ")");
		    statement.executeUpdate(deleteSql.toString());
		    //tblqbillpromotiondtl
		    deleteSql.setLength(0);
		    deleteSql.append("delete from tblqbillpromotiondtl where strBillNo In (" + bill1 + ")");
		    statement.executeUpdate(deleteSql.toString());
		    //tblqbillcomplementrydtl
		    deleteSql.setLength(0);
		    deleteSql.append("delete from tblqbillcomplementrydtl where strBillNo In (" + bill1 + ")");
		    statement.executeUpdate(deleteSql.toString());
		    //tblqbilltaxdtl
		    deleteSql.setLength(0);
		    deleteSql.append("delete from tblqbilltaxdtl where strBillNo In (" + bill1 + ")");
		    statement.executeUpdate(deleteSql.toString());
		    //tblqbillsettlementdtl
		    deleteSql.setLength(0);
		    deleteSql.append("delete from tblqbillsettlementdtl where strBillNo In (" + bill1 + ")");
		    statement.executeUpdate(deleteSql.toString());
		    //tblqbilldiscdtl
		    deleteSql.setLength(0);
		    deleteSql.append("delete from tblqbilldiscdtl where strBillNo In (" + bill1 + ")");
		    statement.executeUpdate(deleteSql.toString());

		    //  Opearations after Deleted........
		    funDropTable();
		    funCreateTable();
		    //    resultSet=funSelectQBillHdData();
		    //    funBillGeneration(resultSet);
		    resultSet = funSelectQBillHD2();
		    funBillGeneration2(resultSet);
		    funFillTableForBillDeletion();
		    // resultSet = funGetOldAndNewBillNo();
		    funUpdateBills(resultSet);
		    funUpdateLastBills();
		    funDropTable();                  //////////To Delete Temp Table

		    funUpdateDayEndField();
		}
		else
		{
		    JOptionPane.showMessageDialog(null, "Please select Bill.");
		}
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }//GEN-LAST:event_btnDeleteMouseClicked

    private void btnExitMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnExitMouseClicked
    {//GEN-HEADEREND:event_btnExitMouseClicked
	// TODO add your handling code here:
	dispose();
    }//GEN-LAST:event_btnExitMouseClicked

    private void listViewOfMenuHeadMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_listViewOfMenuHeadMouseClicked
    {//GEN-HEADEREND:event_listViewOfMenuHeadMouseClicked
	funListViewMenuHeadMouseClicked();
    }//GEN-LAST:event_listViewOfMenuHeadMouseClicked

    private void btbMoveSelectedItemsToRightMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btbMoveSelectedItemsToRightMouseClicked
    {//GEN-HEADEREND:event_btbMoveSelectedItemsToRightMouseClicked
	funBtbMoveSelectedItemsToRightMouseClicked();
    }//GEN-LAST:event_btbMoveSelectedItemsToRightMouseClicked

    private void btbMoveSelectedItemsToLeftMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btbMoveSelectedItemsToLeftMouseClicked
    {//GEN-HEADEREND:event_btbMoveSelectedItemsToLeftMouseClicked
	funBtbMoveSelectedItemsToLeftMouseClicked();
    }//GEN-LAST:event_btbMoveSelectedItemsToLeftMouseClicked

    private void btnClearMenuHeadWiseDeletionMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnClearMenuHeadWiseDeletionMouseClicked
    {//GEN-HEADEREND:event_btnClearMenuHeadWiseDeletionMouseClicked
	funBtnClearMenuHeadWiseDeletionMouseClicked();
    }//GEN-LAST:event_btnClearMenuHeadWiseDeletionMouseClicked

    private void cmbSettlementTypeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmbSettlementTypeActionPerformed
    {//GEN-HEADEREND:event_cmbSettlementTypeActionPerformed
	funLoadSettlementMode(cmbSettlementType.getSelectedItem().toString());
    }//GEN-LAST:event_cmbSettlementTypeActionPerformed

    private void cmbItemDeletionInsertionTypeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmbItemDeletionInsertionTypeActionPerformed
    {//GEN-HEADEREND:event_cmbItemDeletionInsertionTypeActionPerformed
	funCmbItemDeletionInsertionTypeAction();
    }//GEN-LAST:event_cmbItemDeletionInsertionTypeActionPerformed

    private void btnItemInsertionMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnItemInsertionMouseClicked
    {//GEN-HEADEREND:event_btnItemInsertionMouseClicked
	if (btnItemInsertion.isEnabled())
	{

	    boolean isBillSelected = false;
	    if (tblSalesTable.getRowCount() > 0)
	    {
		for (int i = 0; i < tblSalesTable.getRowCount(); i++)
		{
		    if (Boolean.parseBoolean(tblSalesTable.getValueAt(i, 7).toString()))
		    {
			isBillSelected = true;
			break;
		    }
		}
		if (isBillSelected)
		{
		    funInsertItemsIntoBill();
		}
		else
		{
		    JOptionPane.showMessageDialog(null, "Please select Bill No.");
		    return;
		}
	    }
	    else
	    {
		JOptionPane.showMessageDialog(null, "Please select Bill No.");
		return;
	    }
	}
    }//GEN-LAST:event_btnItemInsertionMouseClicked

    private void panelHeaderMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_panelHeaderMouseClicked
    {//GEN-HEADEREND:event_panelHeaderMouseClicked
	funHeaderMouseClicked();
    }//GEN-LAST:event_panelHeaderMouseClicked

    private void cmbBillSeriesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmbBillSeriesActionPerformed
    {//GEN-HEADEREND:event_cmbBillSeriesActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_cmbBillSeriesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btbMoveSelectedItemsToLeft;
    private javax.swing.JButton btbMoveSelectedItemsToRight;
    private javax.swing.JButton btnClearMenuHeadWiseDeletion;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnExecute;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnItemDeletion;
    private javax.swing.JButton btnItemInsertion;
    private javax.swing.JButton btnRefreshTable;
    private javax.swing.JButton btnReset;
    private javax.swing.JComboBox cmbBillSeries;
    private javax.swing.JComboBox cmbItemDeletionInsertionType;
    private javax.swing.JComboBox cmbPosCode;
    private javax.swing.JComboBox cmbSettlementMode;
    private javax.swing.JComboBox cmbSettlementType;
    private com.toedter.calendar.JDateChooser dteFromDate;
    private com.toedter.calendar.JDateChooser dteToDate;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblBillSeries;
    private javax.swing.JLabel lblFromAmount;
    private javax.swing.JLabel lblFromAmount1;
    private javax.swing.JLabel lblFromAmount2;
    private javax.swing.JLabel lblFromAmount3;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblNoOfBillsSelectedName;
    private javax.swing.JLabel lblNoOfBillsSelectedValue;
    private javax.swing.JLabel lblPOSName;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblSelectedDiscAmtName;
    private javax.swing.JLabel lblSelectedDiscAmtValue;
    private javax.swing.JLabel lblSelectedGTAmtName;
    private javax.swing.JLabel lblSelectedGTAmtValue;
    private javax.swing.JLabel lblSelectedSettlementModeName;
    private javax.swing.JLabel lblSelectedSettlementModeValue;
    private javax.swing.JLabel lblSelectedTaxAmtName;
    private javax.swing.JLabel lblSelectedTaxAmtValue;
    private javax.swing.JLabel lblSettlementMode;
    private javax.swing.JLabel lblSettlementType;
    private javax.swing.JLabel lblSettlementType1;
    private javax.swing.JLabel lblToAmount;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JList listViewOfMenuHead;
    private javax.swing.JList listViewOfMenuHeadItems;
    private javax.swing.JList listViewOfSelectedItems;
    private javax.swing.JPanel panelDeletion;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelItemSelection;
    private javax.swing.JPanel panelLayout;
    private javax.swing.JPanel panelbody;
    private javax.swing.JScrollPane pnlSalesData;
    private javax.swing.JScrollPane pnlsalesTotal;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTable tblSalesTable;
    private javax.swing.JTable tblTotal;
    private javax.swing.JTextField txtFromAmount;
    private javax.swing.JTextField txtToAmount;
    // End of variables declaration//GEN-END:variables
    private void funDatabaseConnection()
    {
	try
	{
	    Class.forName("com.mysql.jdbc.Driver");
	    connection = DriverManager.getConnection("jdbc:mysql://" + clsPosConfigFile.ipAddress + ":" + clsPosConfigFile.portNo + "/" + clsPosConfigFile.databaseName, clsPosConfigFile.userId, clsPosConfigFile.password);
	    statement = connection.createStatement();
	    //    System.out.println("Connection created successfully.......to:-"+clsPosConfigFile.databaseName);
	    clsGlobalClass.gConnection = connection;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funLoadMapPOSName() throws SQLException
    {
	try
	{
	    String posNameSql = "select a.strPosName,a.strPosCode from tblposmaster a";
	    statement = connection.createStatement();
	    resultSet = statement.executeQuery(posNameSql);
	    while (resultSet.next())
	    {
		mapPOSName.put(resultSet.getString("strPosName"), resultSet.getString("strPosCode"));
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
//            resultSet.close();
//            statement.close();
	}
    }

    private void funLoadMapSettlementType() throws SQLException
    {
	try
	{
	    String posNameSql = "select a.strSettelmentType,a.strSettelmentCode from tblsettelmenthd a";
	    statement = connection.createStatement();
	    resultSet = statement.executeQuery(posNameSql);
	    while (resultSet.next())
	    {
		mapSettlementType.put(resultSet.getString("strSettelmentType"), resultSet.getString("strSettelmentCode"));
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
//            resultSet.close();
//            statement.close();
	}
    }

    private void funSetCmbPOSName()
    {

	Iterator posNameIterator = mapPOSName.keySet().iterator();
	cmbPosCode.addItem("All");
	while (posNameIterator.hasNext())
	{
	    cmbPosCode.addItem(posNameIterator.next());
	}
    }

    private void funSetCmbSettlementtype()
    {
	Iterator settTypeIterator = mapSettlementType.keySet().iterator();
	//cmbSettlementType.addItem("All");
	while (settTypeIterator.hasNext())
	{
	    cmbSettlementType.addItem(settTypeIterator.next());
	}
	cmbSettlementType.setSelectedItem("Cash");
    }

    private void funFillTableForBillDeletion()
    {
	dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	strFromDate = dateFormat.format(dteFromDate.getDate());
	strToDate = dateFormat.format(dteToDate.getDate());
	dtmSalesTableModel.setRowCount(0);
	dtmTotalSales.setRowCount(0);
	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select a.strBillNo,date(a.dteBillDate),d.strSettelmentType,a.strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt,a.dblGrandTotal "
		    + ",e.strBillSeries,e.strHdBillNo ");
	    sqlBuilder.append("FROM tblqbillhd a  "
		    + "inner join tblqbilldtl b on a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "inner join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo AND DATE(a.dteBillDate)= DATE(c.dteBillDate)  "
		    + "inner join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode  "
		    + "left outer join tblbillseriesbilldtl e on a.strBillNo=e.strHdBillNo "
		    + "WHERE DATE(a.dteBillDate) between '" + strFromDate + "' and '" + strToDate + "' ");

	    if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and a.strPOSCode='" + mapPOSName.get(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }

	    if (!txtFromAmount.getText().isEmpty())
	    {
		if (!txtToAmount.getText().isEmpty())
		{
		    sqlBuilder.append(" and a.dblGrandTotal between '" + Double.parseDouble(txtFromAmount.getText()) + "' and '" + Double.parseDouble(txtToAmount.getText()) + "' ");
		}
	    }
	    if (!cmbSettlementType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and d.strSettelmentType ='" + cmbSettlementType.getSelectedItem().toString() + "' ");
	    }

	    if (!cmbSettlementMode.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and a.strSettelmentMode ='" + cmbSettlementMode.getSelectedItem().toString() + "' ");
	    }
	    if (!cmbBillSeries.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and e.strBillSeries='" + cmbBillSeries.getSelectedItem().toString() + "' ");
	    }

	    sqlBuilder.append(" group by a.strBillNo,a.dteBillDate ");
	    resultSet = statement.executeQuery(sqlBuilder.toString());

	    double totalDisc = 0, totalTaxAmt = 0, totalGrandTotalAmt = 0;
	    while (resultSet.next())
	    {
		Object tblRow[] =
		{
		    resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
		    resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), new Boolean(false)
		};
		dtmSalesTableModel.addRow(tblRow);

		totalDisc += resultSet.getDouble(5);
		totalTaxAmt += resultSet.getDouble(6);
		totalGrandTotalAmt += resultSet.getDouble(7);
	    }
	    resultSet.close();
	    tblSalesTable.setModel(dtmSalesTableModel);

	    //tblTotal
	    Object tblRow[] =
	    {
		"Total", "", "", "", String.valueOf(Math.rint(totalDisc)), String.valueOf(Math.rint(totalTaxAmt)), String.valueOf(Math.rint(totalGrandTotalAmt)), ""
	    };

	    dtmTotalSales.addRow(tblRow);
	    tblTotal.setModel(dtmTotalSales);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{

	}
    }

    private void funCreateTable()
    {

	try
	{
	    String createTableSql = "CREATE TABLE `tblbillgeneration` (\n"
		    + "	`strOldBillNo` VARCHAR(50) NOT NULL,\n"
		    + "	`strNewBillNo` VARCHAR(50) NOT NULL ,"
		    + "  PRIMARY KEY (`strOldBillNo`) "
		    + ") ";
	    int isCreated = statement.executeUpdate(createTableSql);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funDropTable()
    {
	try
	{
	    String dropTableSql = "drop table IF EXISTS tblbillgeneration ";
	    statement.executeUpdate(dropTableSql);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private ResultSet funSelectQBillHdData()
    {

	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select a.strBillNo,a.dteBillDate,c.strSettelmentType,a.dblDiscountPer,a.dblTaxAmt,a.dblGrandTotal from tblqbillhd a ,tblqbillsettlementdtl b,tblsettelmenthd c ");
	    if (cmbPosCode.getSelectedIndex() < 1)
	    {
		sqlBuilder.append(" where a.strPOSCode=a.strPOSCode ");
	    }
	    else
	    {
		sqlBuilder.append(" where a.strPOSCode='" + mapPOSName.get(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }

	    sqlBuilder.append(" and date(a.dteBillDate) between '" + strFromDate + "' and '" + strToDate + "' "
		    + " and a.strBillNo=b.strBillNo "
		    + " and b.strSettlementCode=c.strSettelmentCode ");
	    if (!txtFromAmount.getText().isEmpty())
	    {
		if (!txtToAmount.getText().isEmpty())
		{
		    sqlBuilder.append(" and a.dblGrandTotal between '" + Double.parseDouble(txtFromAmount.getText()) + "' and '" + Double.parseDouble(txtToAmount.getText()) + "' ");
		}
	    }
	    if (cmbSettlementType.getSelectedIndex() < 1)
	    {
		sqlBuilder.append(" and  c.strSettelmentType= c.strSettelmentType ");
	    }
	    else
	    {
		sqlBuilder.append(" and c.strSettelmentType ='" + cmbSettlementType.getSelectedItem().toString() + "' ");
	    }

	    //    System.out.println("tblqbillhd query to fill main table-->"+tblQuery);
	    sqlBuilder.append(" group by a.strBillNo ");
	    resultSet = statement.executeQuery(sqlBuilder.toString());

	    return resultSet;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	return null;
    }

    private ResultSet funSelectQBillHD2()
    {

	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select a.strBillNo,a.dteBillDate,a.strSettelmentMode,a.dblDiscountPer,a.dblTaxAmt,a.dblGrandTotal from tblqbillhd a");
	    if (cmbPosCode.getSelectedIndex() < 1)
	    {
		sqlBuilder.append(" where a.strPOSCode=a.strPOSCode ");
		Iterator<Map.Entry<String, String>> firstBillIterator = mapPosCodeWiseFirstBillNos.entrySet().iterator();
		while (firstBillIterator.hasNext())
		{
		    Map.Entry<String, String> entry = firstBillIterator.next();
		    sqlBuilder.append("and a.strBillNo>=if(a.strPosCode='" + entry.getKey() + "','" + entry.getKey() + entry.getValue() + "',a.strBillNo) ");
		}
	    }
	    else
	    {
		sqlBuilder.append(" where a.strPOSCode='" + mapPOSName.get(cmbPosCode.getSelectedItem().toString()) + "' ");
		sqlBuilder.append(" and a.strBillNo>='" + mapPOSName.get(cmbPosCode.getSelectedItem().toString()) + mapPosCodeWiseFirstBillNos.get(mapPOSName.get(cmbPosCode.getSelectedItem().toString())) + "' ");
	    }

	    //    System.out.println("Query tblqbillhd to billgeneration --> "+tblQuery);
	    resultSet = statement.executeQuery(sqlBuilder.toString());

	    return resultSet;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	return null;
    }

    private void funUpdateBills(ResultSet rsOldAndNewBills)
    {
	try
	{

//            String sql = "ALTER TABLE `tblqbillhd` "
//                    + "	DROP PRIMARY KEY;";
//            System.out.println("tblqbillhd drop key= " + statement.executeUpdate(sql));
	    String sql = "update tblqbillhd a join tblbillgeneration b\n"
		    + "on a.strBillNo = b.strOldBillNo \n"
		    + "set a.strBillNo = b.strNewBillNo ";
	    System.out.println("tblqbillhd Rows Affected= " + statement.executeUpdate(sql));

//            sql = "ALTER TABLE `tblqbillhd` "
//                    + "ADD PRIMARY KEY (`strBillNo`, `strClientCode`);";
//            System.out.println("tblqbillhd add key= " + statement.executeUpdate(sql));
	    sql = "update tblqbilldtl a join tblbillgeneration b\n"
		    + "on a.strBillNo = b.strOldBillNo \n"
		    + "set a.strBillNo = b.strNewBillNo";
	    System.out.println("tblqbilldtl Rows Affected= " + statement.executeUpdate(sql));

	    sql = "update tblqbillmodifierdtl a join tblbillgeneration b\n"
		    + "on a.strBillNo = b.strOldBillNo \n"
		    + "set a.strBillNo = b.strNewBillNo";
	    System.out.println("tblqbillmodifierdtl Rows Affected= " + statement.executeUpdate(sql));

	    sql = "update tblqbillpromotiondtl a join tblbillgeneration b\n"
		    + "on a.strBillNo = b.strOldBillNo \n"
		    + "set a.strBillNo = b.strNewBillNo";
	    System.out.println("tblqbillpromotiondtl Rows Affected= " + statement.executeUpdate(sql));

	    sql = "update tblqbillcomplementrydtl a join tblbillgeneration b\n"
		    + "on a.strBillNo = b.strOldBillNo \n"
		    + "set a.strBillNo = b.strNewBillNo";
	    System.out.println("tblqbillcomplementrydtl Rows Affected= " + statement.executeUpdate(sql));

//            sql = "ALTER TABLE `tblqbilltaxdtl` "
//                    + "	DROP PRIMARY KEY;";
//            System.out.println("tblqbilltaxdtl drop key= " + statement.executeUpdate(sql));
	    sql = "update tblqbilltaxdtl a join tblbillgeneration b\n"
		    + "on a.strBillNo = b.strOldBillNo \n"
		    + "set a.strBillNo = b.strNewBillNo";
	    System.out.println("tblqbilltaxdtl Rows Affected= " + statement.executeUpdate(sql));

//            sql = "ALTER TABLE `tblqbilltaxdtl` "
//                    + "ADD PRIMARY KEY (`strBillNo`,`strTaxCode`, `strClientCode`);";
//            System.out.println("tblqbilltaxdtl add key= " + statement.executeUpdate(sql));
//            sql = "ALTER TABLE `tblqbillsettlementdtl` "
//                    + "	DROP PRIMARY KEY;";
//            System.out.println("tblqbillsettlementdtl drop key= " + statement.executeUpdate(sql));
	    sql = "update tblqbillsettlementdtl a join tblbillgeneration b\n"
		    + "on a.strBillNo = b.strOldBillNo \n"
		    + "set a.strBillNo = b.strNewBillNo";
	    System.out.println("tblqbillsettlementdtl Rows Affected= " + statement.executeUpdate(sql));

//            sql = "ALTER TABLE `tblqbillsettlementdtl` "
//                    + "ADD PRIMARY KEY (`strBillNo`,`strSettlementCode`, `strClientCode`);";
//            System.out.println("tblqbillsettlementdtl add key= " + statement.executeUpdate(sql));
	    sql = "update tblqbilldiscdtl a join tblbillgeneration b\n"
		    + "on a.strBillNo = b.strOldBillNo \n"
		    + "set a.strBillNo = b.strNewBillNo";
	    System.out.println("tblqbilldiscdtl Rows Affected= " + statement.executeUpdate(sql));

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funBillGeneration(ResultSet resultSet)
    {
	try
	{
	    String insertIntoTempTable = "insert into tblbillgeneration(strOldBillNo,strNewBillNo) values ";
	    String valuesSql = "";
	    while (resultSet.next())
	    {
		String oldBillNo = resultSet.getString("strBillNo");
		String posCode = oldBillNo.substring(0, 3);
		String strFirstBillNo = mapPosCodeWiseFirstBillNos.get(posCode);
		long longFirstBillNo = Long.parseLong(strFirstBillNo);
		valuesSql += ",('" + oldBillNo + "','" + posCode + String.format("%05d", longFirstBillNo) + "') ";
		longFirstBillNo++;
		mapPosCodeWiseFirstBillNos.replace(posCode, String.valueOf(longFirstBillNo));
	    }
	    if (valuesSql.length() > 0)
	    {
		valuesSql = valuesSql.substring(1, valuesSql.length());
		insertIntoTempTable = insertIntoTempTable + valuesSql;

		//    System.out.println("insertIntoTblBillGenerationSql="+insertIntoTempTable);            
		int aff = statement.executeUpdate(insertIntoTempTable);
		//    System.out.println("affected rows="+aff);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private ResultSet funGetOldAndNewBillNo()
    {

	try
	{
	    String selectOldAndNewBillSql = "select strOldBillNo,strNewBillNo from tblbillgeneration ";
	    ResultSet resultSet = statement.executeQuery(selectOldAndNewBillSql);
	    return resultSet;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	return null;
    }

    private void funUpdateBillNo(String oldBillNo, String newBillNo)
    {
	try
	{
	    //tblqbillhd
	    updateSql.setLength(0);
	    updateSql.append("update tblqbillhd set strBillNo='" + newBillNo + "' where strBillNo='" + oldBillNo + "' ");
	    statement.executeUpdate(updateSql.toString());
	    //tblqbilldtl
	    updateSql.setLength(0);
	    updateSql.append("update tblqbilldtl set strBillNo='" + newBillNo + "' where strBillNo='" + oldBillNo + "' ");
	    statement.executeUpdate(updateSql.toString());
	    //tblqbillmodifierdtl
	    updateSql.setLength(0);
	    updateSql.append("update tblqbillmodifierdtl set strBillNo='" + newBillNo + "' where strBillNo='" + oldBillNo + "' ");
	    statement.executeUpdate(updateSql.toString());
	    //tblqbilltaxdtl
	    updateSql.setLength(0);
	    updateSql.append("update tblqbilltaxdtl set strBillNo='" + newBillNo + "' where strBillNo='" + oldBillNo + "' ");
	    statement.executeUpdate(updateSql.toString());
	    //tblqbillsettlementdtl
	    updateSql.setLength(0);
	    updateSql.append("update tblqbillsettlementdtl set strBillNo='" + newBillNo + "' where strBillNo='" + oldBillNo + "' ");
	    statement.executeUpdate(updateSql.toString());
	    //tblqbilldiscdtl
	    updateSql.setLength(0);
	    updateSql.append("update tblqbilldiscdtl set strBillNo='" + newBillNo + "' where strBillNo='" + oldBillNo + "' ");
	    statement.executeUpdate(updateSql.toString());
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funGeneratePosWiseFirstBillNos()
    {

	mapPosCodeWiseFirstBillNos = new LinkedHashMap<String, String>();
	resultSet = funSelectFirstBillFromDB();
	try
	{
	    while (resultSet.next())
	    {
		String billNo = resultSet.getString("firstBill");
		String posCode = billNo.substring(0, 3);
		mapPosCodeWiseFirstBillNos.put(posCode, billNo.substring(3, billNo.length()));
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	if (tblSalesTable.getRowCount() > 0)
	{
	    String firstBillNo = String.valueOf(tblSalesTable.getValueAt(0, 0));
	    String posCode = firstBillNo.substring(0, 3);
	    mapPosCodeWiseFirstBillNos.replace(posCode, firstBillNo.substring(3, firstBillNo.length()));
	    for (int row = 1; row < tblSalesTable.getRowCount(); row++)
	    {
		if (!String.valueOf(tblSalesTable.getValueAt(row, 0)).startsWith(posCode))
		{
		    firstBillNo = String.valueOf(tblSalesTable.getValueAt(row, 0));
		    posCode = firstBillNo.substring(0, 3);
		    mapPosCodeWiseFirstBillNos.replace(posCode, firstBillNo.substring(3, firstBillNo.length()));
		}
	    }
	}

	Iterator<Map.Entry<String, String>> firstBillIterator = mapPosCodeWiseFirstBillNos.entrySet().iterator();
	while (firstBillIterator.hasNext())
	{
	    Map.Entry<String, String> entry = firstBillIterator.next();
	    System.out.println("posCode=" + entry.getKey() + "\tFirstBillNo=" + entry.getValue());
	}
	System.out.println("            ...           ");
    }

    private void funCountPosWiseBillToBeDeleted()
    {

	mapCountPosWiseBillToBeDeleted = new LinkedHashMap<String, Integer>();
	if (tblSalesTable.getRowCount() > 0)
	{

	    for (int row = 0; row < tblSalesTable.getRowCount(); row++)
	    {
		boolean isDelete = Boolean.parseBoolean(String.valueOf(tblSalesTable.getValueAt(row, 6)));
		if (isDelete)
		{
		    int i = 0;
		    String billNo = String.valueOf(tblSalesTable.getValueAt(row, 0));
		    String posCode = billNo.substring(0, 3);
		    if (mapCountPosWiseBillToBeDeleted.containsKey(posCode))
		    {
			int billCounter = mapCountPosWiseBillToBeDeleted.get(posCode);
			billCounter = billCounter + 1;
			mapCountPosWiseBillToBeDeleted.replace(posCode, billCounter);
		    }
		    else
		    {
			mapCountPosWiseBillToBeDeleted.put(posCode, ++i);
		    }
		}
		else
		{
		    String billNo = String.valueOf(tblSalesTable.getValueAt(row, 0));
		    String posCode = billNo.substring(0, 3);
		    if (!mapCountPosWiseBillToBeDeleted.containsKey(posCode))
		    {
			mapCountPosWiseBillToBeDeleted.put(posCode, 0);
		    }
		}
	    }
	    Iterator<Map.Entry<String, String>> posCodeIterator = mapPOSName.entrySet().iterator();
	    while (posCodeIterator.hasNext())
	    {
		Map.Entry<String, String> entry = posCodeIterator.next();
		String posCode = entry.getValue();
		if (!mapCountPosWiseBillToBeDeleted.containsKey(posCode))
		{
		    mapCountPosWiseBillToBeDeleted.put(posCode, 0);
		}
	    }

	    Iterator<String> it = mapCountPosWiseBillToBeDeleted.keySet().iterator();
	    while (it.hasNext())
	    {
		String key = it.next();
		System.out.println("posCode=" + key + "\tBillToBeDeleted=" + mapCountPosWiseBillToBeDeleted.get(key));
	    }
	}
    }

    private void funUpdateLastBills()
    {
	try
	{
	    String lastBillSql = "select strPosCode,strBillNo from tblstorelastbill";
	    resultSet = statement.executeQuery(lastBillSql);

	    ArrayList<String> updateSql = new ArrayList<String>();
	    while (resultSet.next())
	    {
		String posCode = resultSet.getString("strPosCode");
		String strLastBillNo = resultSet.getString("strBillNo");
		long longLastBillNo = Long.parseLong(strLastBillNo);
		if (mapCountPosWiseBillToBeDeleted.containsKey(posCode))
		{
		    longLastBillNo = longLastBillNo - mapCountPosWiseBillToBeDeleted.get(posCode);
		    updateSql.add("update tblstorelastbill set strBillNo='" + longLastBillNo + "' where strPosCode='" + posCode + "' ");
		    // System.out.println("update last billNo-->"+updateSql.get(i)); 
		    //i++;
		}
	    }
	    for (int j = 0; j < updateSql.size(); j++)
	    {
		//System.out.println("update Sql legnth-->"+updateSql.get(j));
		statement.executeUpdate(updateSql.get(j));
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funBillGeneration2(ResultSet resultSet)
    {

	try
	{
	    String insertIntoTempTable = "insert into tblbillgeneration(strOldBillNo,strNewBillNo) values ";
	    String valuesSql = "";
	    while (resultSet.next())
	    {
		String oldBillNo = resultSet.getString("strBillNo");
		String posCode = oldBillNo.substring(0, 3);
		String strFirstBillNo = mapPosCodeWiseFirstBillNos.get(posCode);
		long longFirstBillNo = Long.parseLong(strFirstBillNo);
		valuesSql += ",('" + oldBillNo + "','" + posCode + String.format("%05d", longFirstBillNo) + "') ";
		longFirstBillNo++;
		mapPosCodeWiseFirstBillNos.replace(posCode, String.valueOf(longFirstBillNo));
	    }
	    if (valuesSql.length() > 0)
	    {
		valuesSql = valuesSql.substring(1, valuesSql.length());
		insertIntoTempTable = insertIntoTempTable + valuesSql;

		//    System.out.println("insertIntoTblBillGenerationSql="+insertIntoTempTable);            
		int aff = statement.executeUpdate(insertIntoTempTable);
		//    System.out.println("affected rows="+aff);
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private ResultSet funSelectFirstBillFromDB()
    {

	try
	{
	    String firstBillSql = "select min(strBillNo) as firstBill from tblqbillhd group by strPosCode";
	    return statement.executeQuery(firstBillSql);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	return null;
    }

    private void funSetClientCode()
    {

	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select strClientCode from tblsetup limit 1;");

	    ResultSet rs = statement.executeQuery(sqlBuilder.toString());
	    if (rs.next())
	    {
		clsGlobalClass.gClientCode = rs.getString(1);
	    }
	    rs.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funLoadItemsForBill(String billNo)
    {

    }

    private void funEnableMenuHeadWiseDeletion(boolean isMenuHeadWiseDeletion)
    {
	//enable menu head wise button
	btnItemDeletion.setEnabled(isMenuHeadWiseDeletion);//menu head wise deletion        
	//disable other non required controlls
//        dteFromDate.setEnabled(!isMenuHeadWiseDeletion);
//        dteToDate.setEnabled(!isMenuHeadWiseDeletion);
//        txtFromAmount.setEnabled(!isMenuHeadWiseDeletion);
//        txtToAmount.setEnabled(!isMenuHeadWiseDeletion);
//        cmbSettlementType.setEnabled(!isMenuHeadWiseDeletion);
//        btnExecute.setEnabled(!isMenuHeadWiseDeletion);
	btnDelete.setEnabled(!isMenuHeadWiseDeletion);
	btnEdit.setEnabled(!isMenuHeadWiseDeletion);
	btnRefreshTable.setEnabled(!isMenuHeadWiseDeletion);
	btnItemInsertion.setEnabled(!isMenuHeadWiseDeletion);

	//clear tables
	((DefaultTableModel) tblSalesTable.getModel()).setRowCount(0);
	((DefaultTableModel) tblTotal.getModel()).setRowCount(0);
    }

    private void funEnableMenuHeadWiseInsertion(boolean isMenuHeadWiseInertion)
    {
	//enable menu head wise button
	btnItemInsertion.setEnabled(isMenuHeadWiseInertion);//menu head wise deletion        
	//disable other non required controlls
//        dteFromDate.setEnabled(!isMenuHeadWiseDeletion);
//        dteToDate.setEnabled(!isMenuHeadWiseDeletion);
//        txtFromAmount.setEnabled(!isMenuHeadWiseDeletion);
//        txtToAmount.setEnabled(!isMenuHeadWiseDeletion);
//        cmbSettlementType.setEnabled(!isMenuHeadWiseDeletion);
//        btnExecute.setEnabled(!isMenuHeadWiseDeletion);
	btnDelete.setEnabled(!isMenuHeadWiseInertion);
	btnEdit.setEnabled(!isMenuHeadWiseInertion);
	btnRefreshTable.setEnabled(!isMenuHeadWiseInertion);
	btnItemDeletion.setEnabled(!isMenuHeadWiseInertion);

	//clear tables
	((DefaultTableModel) tblSalesTable.getModel()).setRowCount(0);
	((DefaultTableModel) tblTotal.getModel()).setRowCount(0);
    }

    private void funEnableButtonsForMenuHeadWiseDeletion(boolean isMenuHeadWiseDeletion)
    {

	dteFromDate.setEnabled(isMenuHeadWiseDeletion);
	dteToDate.setEnabled(isMenuHeadWiseDeletion);
	txtFromAmount.setEnabled(isMenuHeadWiseDeletion);
	txtToAmount.setEnabled(isMenuHeadWiseDeletion);
	cmbSettlementType.setEnabled(isMenuHeadWiseDeletion);
	btnExecute.setEnabled(isMenuHeadWiseDeletion);
	btnDelete.setEnabled(isMenuHeadWiseDeletion);
	btnEdit.setEnabled(!isMenuHeadWiseDeletion);
	btnRefreshTable.setEnabled(!isMenuHeadWiseDeletion);

	//clear tables
	((DefaultTableModel) tblSalesTable.getModel()).setRowCount(0);
	((DefaultTableModel) tblTotal.getModel()).setRowCount(0);
    }

    private void funResetButtonClicked()
    {
	cmbItemDeletionInsertionType.setSelectedIndex(0);//BILL DELETION
	//enable menu head wise button
	btnItemDeletion.setEnabled(false);//menu head wise deletion        
	btnItemInsertion.setEnabled(false);
	btnDelete.setEnabled(true);
	btnEdit.setEnabled(true);
	btnRefreshTable.setEnabled(true);

	//clear tables
	((DefaultTableModel) tblSalesTable.getModel()).setRowCount(0);
	((DefaultTableModel) tblTotal.getModel()).setRowCount(0);

	//set labels value
	funSetLabelValues();
    }

    private void funFillTableForItemDeletion()
    {
	dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	strFromDate = dateFormat.format(dteFromDate.getDate());
	strToDate = dateFormat.format(dteToDate.getDate());
	dtmSalesTableModel.setRowCount(0);
	dtmTotalSales.setRowCount(0);
	try
	{

	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select a.strBillNo,date(a.dteBillDate),d.strSettelmentType,a.strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt,a.dblGrandTotal "
		    + ",e.strBillSeries,e.strHdBillNo ");
	    sqlBuilder.append("FROM tblqbillhd a  "
		    + "inner join tblqbilldtl b on a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "inner join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo AND DATE(a.dteBillDate)= DATE(c.dteBillDate)  "
		    + "inner join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode  "
		    + "left outer join tblbillseriesbilldtl e on a.strBillNo=e.strHdBillNo "
		    + "WHERE DATE(a.dteBillDate) between '" + strFromDate + "' and '" + strToDate + "' ");

	    if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and a.strPOSCode='" + mapPOSName.get(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }

	    if (!txtFromAmount.getText().isEmpty())
	    {
		if (!txtToAmount.getText().isEmpty())
		{
		    sqlBuilder.append(" and a.dblGrandTotal between '" + Double.parseDouble(txtFromAmount.getText()) + "' and '" + Double.parseDouble(txtToAmount.getText()) + "' ");
		}
	    }
	    if (!cmbSettlementType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and d.strSettelmentType ='" + cmbSettlementType.getSelectedItem().toString() + "' ");
	    }
	    if (!cmbSettlementMode.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and a.strSettelmentMode ='" + cmbSettlementMode.getSelectedItem().toString() + "' ");
	    }
	    if (!cmbBillSeries.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and e.strBillSeries='" + cmbBillSeries.getSelectedItem().toString() + "' ");
	    }

	    sqlBuilder.append(" and b.strItemCode IN " + funGetSelectedItemList() + " ");

	    sqlBuilder.append(" group by a.strBillNo,a.dteBillDate ");
	    resultSet = statement.executeQuery(sqlBuilder.toString());

	    double totalDisc = 0, totalTaxAmt = 0, totalGrandTotalAmt = 0;
	    while (resultSet.next())
	    {
		String billNo = resultSet.getString(1);
		String billDate = resultSet.getString(2);

		int resultSetNotInRowCount = funGetRowCountForNonExistingItemCode(billNo, billDate);
		if (resultSetNotInRowCount > 0)
		{
		    Object tblRow[] =
		    {
			resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
			resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), new Boolean(false)
		    };
		    dtmSalesTableModel.addRow(tblRow);

		    totalDisc += resultSet.getDouble(5);
		    totalTaxAmt += resultSet.getDouble(6);
		    totalGrandTotalAmt += resultSet.getDouble(7);
		}
	    }
	    resultSet.close();
	    tblSalesTable.setModel(dtmSalesTableModel);

	    //tblTotal
	    Object tblRow[] =
	    {
		"Total", "", "", "", String.valueOf(Math.rint(totalDisc)), String.valueOf(Math.rint(totalTaxAmt)), String.valueOf(Math.rint(totalGrandTotalAmt)), ""
	    };

	    dtmTotalSales.addRow(tblRow);
	    tblTotal.setModel(dtmTotalSales);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{

	}
    }

    private void funLoadMenuHeads()
    {
	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select a.strMenuCode,a.strMenuName "
		    + "from tblmenuhd a where a.strOperational='Y' ");
	    ResultSet rsMenuHeads = clsGlobalClass.funExecuteResultSetQuery(sqlBuilder.toString());
	    listViewOfMenuHead.removeAll();
	    List<clsItemDtl> listOfMenuHead = new ArrayList<clsItemDtl>();
	    while (rsMenuHeads.next())
	    {
		clsItemDtl objItemDtl = new clsItemDtl();
		objItemDtl.setStrName(rsMenuHeads.getString(2));

		listOfMenuHead.add(objItemDtl);
	    }
	    rsMenuHeads.close();

	    final String[] arrayOfMenuHead = funGetArrayFromList(listOfMenuHead);
	    AbstractListModel listModel = new DefaultListModel()
	    {
		private String[] strings = arrayOfMenuHead;

		public int getSize()
		{
		    return strings.length;
		}

		public Object getElementAt(int i)
		{
		    return strings[i];
		}

	    };

	    listViewOfMenuHead.setModel(listModel);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private String[] funGetArrayFromList(List<clsItemDtl> listOfMenuHead)
    {
	String[] modelListArray = new String[listOfMenuHead.size()];
	try
	{
	    for (int i = 0; i < listOfMenuHead.size(); i++)
	    {
		modelListArray[i] = listOfMenuHead.get(i).getStrName();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return modelListArray;
	}
    }

    private void funListViewMenuHeadMouseClicked()
    {
	if (listViewOfMenuHead.getSelectedValue() != null)
	{
	    funLoadItemsForSelectedMenuHead(listViewOfMenuHead.getSelectedValue().toString());
	}
    }

    private void funLoadItemsForSelectedMenuHead(String selectedMenuHeadName)
    {
	try
	{
	    String menuHeadCode = funGetMenuHeadCode(selectedMenuHeadName);

	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select distinct(a.strItemCode),a.strItemName "
		    + "from tblmenuitempricingdtl a,tblmenuhd b "
		    + "where a.strMenuCode=b.strMenuCode "
		    + "and b.strMenuCode='" + menuHeadCode + "'; ");
	    ResultSet rsMenuHeads = clsGlobalClass.funExecuteResultSetQuery(sqlBuilder.toString());
	    listViewOfMenuHeadItems.removeAll();
	    List<clsItemDtl> listOfMenuHeadItems = new ArrayList<clsItemDtl>();
	    while (rsMenuHeads.next())
	    {
		clsItemDtl objItemDtl = new clsItemDtl();
		objItemDtl.setStrName(rsMenuHeads.getString(2));

		listOfMenuHeadItems.add(objItemDtl);
	    }
	    rsMenuHeads.close();

	    final String[] arrayOfMenuHeadItems = funGetArrayFromList(listOfMenuHeadItems);
	    AbstractListModel listModel = new DefaultListModel()
	    {
		private String[] strings = arrayOfMenuHeadItems;

		public int getSize()
		{
		    return strings.length;
		}

		public Object getElementAt(int i)
		{
		    return strings[i];
		}

	    };

	    listViewOfMenuHeadItems.setModel(listModel);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private String funGetMenuHeadCode(String menuHeadName)
    {
	String menuHeadCode = null;
	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select strMenuCode "
		    + "from tblmenuhd where strMenuName='" + menuHeadName + "' ");
	    ResultSet rsMenuHeads = clsGlobalClass.funExecuteResultSetQuery(sqlBuilder.toString());
	    if (rsMenuHeads.next())
	    {
		menuHeadCode = rsMenuHeads.getString(1);
	    }
	    rsMenuHeads.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return menuHeadCode;
	}
    }

    private void funBtbMoveSelectedItemsToRightMouseClicked()
    {
	if (listViewOfMenuHeadItems.getSelectedValues().length > 0)
	{
	    funAddSelectedItemsToListViewItems(listViewOfMenuHeadItems.getSelectedValuesList());
	}
	else
	{
	    JOptionPane.showMessageDialog(null, "Please select Items.");
	    return;
	}
    }

    private void funAddSelectedItemsToListViewItems(List selectedValuesList)
    {
	try
	{
	    DefaultListModel defaultListModel = (DefaultListModel) listViewOfSelectedItems.getModel();
	    for (int i = 0; i < selectedValuesList.size(); i++)
	    {
		if (defaultListModel.contains(selectedValuesList.get(i)))
		{

		}
		else
		{
		    defaultListModel.addElement(selectedValuesList.get(i));
		}
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funBtbMoveSelectedItemsToLeftMouseClicked()
    {
	try
	{
	    DefaultListModel defaultListModel = (DefaultListModel) listViewOfSelectedItems.getModel();
	    defaultListModel.removeElement(listViewOfSelectedItems.getSelectedValue());
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funBtnClearMenuHeadWiseDeletionMouseClicked()
    {
	DefaultListModel defaultListModel = (DefaultListModel) listViewOfSelectedItems.getModel();
	defaultListModel.clear();
    }

    private void funBtnSaveMenuHeadWiseDeletionMouseClicked()
    {
	DefaultListModel defaultListModel = (DefaultListModel) listViewOfSelectedItems.getModel();
	Object[] arrayItemsBeingDeleted = defaultListModel.toArray();
	///clsGlobalClass.listOfItemsBeingDeleted.clear();
	for (int i = 0; i < arrayItemsBeingDeleted.length; i++)
	{
	    clsItemDtl objDtl = new clsItemDtl();

	    String itemCode = funGetItemCode(arrayItemsBeingDeleted[i].toString());

	    objDtl.setStrItemCode(itemCode);
	    objDtl.setStrItemName(arrayItemsBeingDeleted[i].toString());

	    // clsGlobalClass.listOfItemsBeingDeleted.add(objDtl);
	    this.dispose();
	}
    }

    private String funGetItemCode(String itemName)
    {
	String itemCode = null;
	try
	{
	    StringBuilder itemCodeBuilder = new StringBuilder();
	    itemCodeBuilder.append("select strItemCode from tblitemmaster a where a.strItemName='" + itemName + "' ");
	    ResultSet rsItemCode = clsGlobalClass.funExecuteResultSetQuery(itemCodeBuilder.toString());
	    if (rsItemCode.next())
	    {
		itemCode = rsItemCode.getString(1);
	    }
	    rsItemCode.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return itemCode;
	}
    }

    private String funGetSelectedItemList()
    {
	DefaultListModel defaultListModel = (DefaultListModel) listViewOfSelectedItems.getModel();

	StringBuilder itemCodeBuilder = new StringBuilder();
	itemCodeBuilder.append("(");
	for (int i = 0; i < defaultListModel.size(); i++)
	{
	    if (i == 0)
	    {
		itemCodeBuilder.append("'" + funGetItemCode(defaultListModel.getElementAt(i).toString()) + "'");
	    }
	    else
	    {
		itemCodeBuilder.append(",'" + funGetItemCode(defaultListModel.getElementAt(i).toString()) + "'");
	    }
	}
	itemCodeBuilder.append(")");

	System.out.println("itemCode builder->" + itemCodeBuilder);
	return itemCodeBuilder.toString();
    }

    private void funDeleteMenuHeadWiseItemsFromBill()
    {
	try
	{

	    for (int row = 0; row < tblSalesTable.getRowCount(); row++)
	    {
		boolean isDelete = Boolean.parseBoolean(String.valueOf(tblSalesTable.getValueAt(row, 7)));
		if (isDelete)
		{
		    String billNo = tblSalesTable.getValueAt(row, 0).toString();
		    String billDate = tblSalesTable.getValueAt(row, 1).toString();

		    int resultSetRowCount = funGetRowCountForExistingItemCode(billNo);
		    if (resultSetRowCount > 0)
		    {
			int resultSetNotInRowCount = funGetRowCountForNonExistingItemCode(billNo, billDate);
			if (resultSetNotInRowCount > 0)
			{
			    funDeleteItemsFromBill(billNo, billDate);
			    funReCalculateTax(billNo, billDate);
			}
		    }
		}
	    }
	    funCmbItemDeletionInsertionTypeAction();//refresh
	    funUpdateDayEndField();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{

	}
    }

    private int funGetRowCountForExistingItemCode(String billNo)
    {
	int resultSetRowCount = 0;
	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select count(*),a.strItemCode "
		    + "from tblqbilldtl a "
		    + "where a.strBillNo='" + billNo + "' "
		    + "and a.strItemCode IN " + funGetSelectedItemList() + " ");
	    //sqlBuilder.append("group by a.strItemCode ");
	    ResultSet rsIsExistsItemInBill = clsGlobalClass.funExecuteResultSetQuery(sqlBuilder.toString());
	    if (rsIsExistsItemInBill.next())
	    {
		resultSetRowCount = rsIsExistsItemInBill.getInt(1);
	    }
	    rsIsExistsItemInBill.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return resultSetRowCount;
	}
    }

    private int funGetRowCountForNonExistingItemCode(String billNo, String billDate)
    {
	int resultSetRowCount = 0;
	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select count(*),a.strItemCode "
		    + "from tblqbilldtl a "
		    + "where a.strBillNo='" + billNo + "' "
		    + "and date(a.dtebillDate)='" + billDate + "'  "
		    + "and a.strItemCode NOT IN " + funGetSelectedItemList() + " ");
	    sqlBuilder.append("group by a.strItemCode ");
	    ResultSet rsIsExistsItemInBill = clsGlobalClass.funExecuteResultSetQuery(sqlBuilder.toString());
	    if (rsIsExistsItemInBill.next())
	    {
		resultSetRowCount = rsIsExistsItemInBill.getInt(1);
	    }
	    rsIsExistsItemInBill.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return resultSetRowCount;
	}
    }

    private void funDeleteItemsFromBill(String billNo, String billDate)
    {
	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("delete from tblqbilldtl  "
		    + "where strBillNo='" + billNo + "' "
		    + "AND DATE(dteBillDate)='" + billDate + "' "
		    + "and strItemCode IN " + funGetSelectedItemList() + " ");
	    int affectedRows = clsGlobalClass.funExecuteUpdateQuery(sqlBuilder.toString());
	    System.out.println("tblqbilldtl BillNo->" + billNo + "\t" + affectedRows);

	    sqlBuilder.setLength(0);
	    sqlBuilder.append("delete from tblqbillmodifierdtl  "
		    + "where strBillNo='" + billNo + "' "
		    + "AND DATE(dteBillDate)='" + billDate + "' "
		    + "and left(strItemCode,7) IN " + funGetSelectedItemList() + " ");
	    affectedRows = clsGlobalClass.funExecuteUpdateQuery(sqlBuilder.toString());
	    System.out.println("tblqbillmodifierdtl BillNo->" + billNo + "\t" + affectedRows);

	    sqlBuilder.setLength(0);
	    sqlBuilder.append("delete from tblqbillpromotiondtl  "
		    + "where strBillNo='" + billNo + "' "
		    + "AND DATE(dteBillDate)='" + billDate + "' "
		    + "and strItemCode IN " + funGetSelectedItemList() + " ");
	    affectedRows = clsGlobalClass.funExecuteUpdateQuery(sqlBuilder.toString());
	    System.out.println("tblqbillmodifierdtl BillNo->" + billNo + "\t" + affectedRows);

	    new clsUtility().funReCalculateDiscountForBill("MenuHeadWiseItemDeletion", "QFile", clsGlobalClass.gPOSCode, clsGlobalClass.gClientCode, billNo, billDate);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{

	}
    }

    private void funReCalculateTax(String billNo, String billDate) throws SQLException, Exception
    {
	List<clsItemDtlForTax> arrListItemDtls = new ArrayList<clsItemDtlForTax>();
	double subTotal = 0.00, totalDiscAmt = 0.00;
	String sql = "select  a.strItemCode,a.strItemName,a.dblAmount,a.dblDiscountAmt "
		+ "from tblqbilldtl a "
		+ "where strBillNo='" + billNo + "' "
		+ "AND DATE(dteBillDate)='" + billDate + "' ";
	ResultSet rsbillDtl = clsGlobalClass.funExecuteResultSetQuery(sql);
	while (rsbillDtl.next())
	{
	    subTotal = subTotal + rsbillDtl.getDouble(3);
	    totalDiscAmt = totalDiscAmt + rsbillDtl.getDouble(4);

	    clsItemDtlForTax objItemDtlForTax = new clsItemDtlForTax();

	    objItemDtlForTax.setItemCode(rsbillDtl.getString(1));
	    objItemDtlForTax.setItemName(rsbillDtl.getString(1));
	    objItemDtlForTax.setAmount(rsbillDtl.getDouble(3));
	    objItemDtlForTax.setDiscAmt(rsbillDtl.getDouble(4));

	    arrListItemDtls.add(objItemDtlForTax);
	}
	rsbillDtl.close();

	sql = "select  a.strItemCode,a.strModifierName,a.dblAmount,a.dblDiscAmt "
		+ "from tblqbillmodifierdtl a "
		+ "where strBillNo='" + billNo + "' "
		+ "AND DATE(dteBillDate)='" + billDate + "' ";
	rsbillDtl = clsGlobalClass.funExecuteResultSetQuery(sql);
	while (rsbillDtl.next())
	{
	    subTotal = subTotal + rsbillDtl.getDouble(3);
	    totalDiscAmt = totalDiscAmt + rsbillDtl.getDouble(4);

	    clsItemDtlForTax objItemDtlForTax = new clsItemDtlForTax();

	    objItemDtlForTax.setItemCode(rsbillDtl.getString(1));
	    objItemDtlForTax.setItemName(rsbillDtl.getString(1));
	    objItemDtlForTax.setAmount(rsbillDtl.getDouble(3));
	    objItemDtlForTax.setDiscAmt(rsbillDtl.getDouble(4));

	    arrListItemDtls.add(objItemDtlForTax);
	}
	rsbillDtl.close();

	sql = "select date(a.dteBillDate),a.strPOSCode,a.strOperationType,a.strAreaCode,a.strClientCode  "
		+ "from tblqbillhd a "
		+ "where strBillNo='" + billNo + "' "
		+ "AND DATE(dteBillDate)='" + billDate + "' ";
	rsbillDtl = clsGlobalClass.funExecuteResultSetQuery(sql);
	String posCode = "", opearationType = "", areaCode = "";
	if (rsbillDtl.next())
	{
	    billDate = rsbillDtl.getString(1);
	    posCode = rsbillDtl.getString(2);
	    opearationType = rsbillDtl.getString(3);
	    if (opearationType.equalsIgnoreCase("DirectBiller"))
	    {
		opearationType = "DineIn";
	    }
	    areaCode = rsbillDtl.getString(4);
	    clsGlobalClass.gClientCode = rsbillDtl.getString(5);
	}

	List<clsTaxCalculationDtls> arrListTaxCal = new clsUtility().funCalculateTax(arrListItemDtls, posCode, billDate, areaCode, opearationType, subTotal, totalDiscAmt, "", "S01");

	List<clsBillTaxDtl> listObjBillTaxBillDtls = new ArrayList<clsBillTaxDtl>();
	double totalTaxAmt = 0.00;
	for (clsTaxCalculationDtls objTaxCalculationDtls : arrListTaxCal)
	{
	    double dblTaxAmt = objTaxCalculationDtls.getTaxAmount();
	    totalTaxAmt = totalTaxAmt + dblTaxAmt;
	    clsBillTaxDtl objBillTaxDtl = new clsBillTaxDtl();
	    objBillTaxDtl.setStrBillNo(billNo);
	    objBillTaxDtl.setStrTaxCode(objTaxCalculationDtls.getTaxCode());
	    objBillTaxDtl.setDblTaxableAmount(objTaxCalculationDtls.getTaxableAmount());
	    objBillTaxDtl.setDblTaxAmount(dblTaxAmt);
	    objBillTaxDtl.setStrClientCode(clsGlobalClass.gClientCode);

	    listObjBillTaxBillDtls.add(objBillTaxDtl);
	}

	funInsertBillTaxDtlTable(listObjBillTaxBillDtls, billNo, billDate);

	clsUtility obj = new clsUtility();
	obj.funUpdateBillDtlWithTaxValues(billNo, "QFile", billDate);

	double dblGrandTotal = 0.00 + subTotal - totalDiscAmt + totalTaxAmt;

	//start code to calculate roundoff amount and round off by amt
	Map<String, Double> mapRoundOff = objUtility.funCalculateRoundOffAmount(dblGrandTotal);
	double grandTotal = mapRoundOff.get("roundOffAmt");
	double grandTotalRoundOffBy = mapRoundOff.get("roundOffByAmt");
	//end code to calculate roundoff amount and round off by amt

	sql = "update tblqbillhd "
		+ "set dblDiscountAmt='" + totalDiscAmt + "' "
		+ ",dblTaxAmt='" + totalTaxAmt + "' "
		+ ",dblSubTotal='" + subTotal + "' "
		+ ",dblGrandTotal='" + grandTotal + "' "
		+ ",dblRoundOff='" + grandTotalRoundOffBy + "' "
		+ "where strBillNo='" + billNo + "' "
		+ "AND DATE(dteBillDate)='" + billDate + "' ";
	clsGlobalClass.funExecuteUpdateQuery(sql);

	ResultSet rsSettlement = clsGlobalClass.funExecuteResultSetQuery("select  count(*) from tblqbillsettlementdtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ");
	int settleCount = 0;
	if (rsSettlement.next())
	{
	    settleCount = rsSettlement.getInt(1);
	}
	rsSettlement.close();
	double settleAmt = grandTotal / settleCount;
	sql = "update tblqbillsettlementdtl "
		+ "set dblSettlementAmt='" + settleAmt + "' "
		+ ",dblPaidAmt='" + settleAmt + "' "
		+ ",dblActualAmt='" + settleAmt + "' "
		+ "where strBillNo='" + billNo + "' "
		+ "AND DATE(dteBillDate)='" + billDate + "' ";
	clsGlobalClass.funExecuteUpdateQuery(sql);

    }

    private int funInsertBillTaxDtlTable(List<clsBillTaxDtl> listObjBillTaxDtl, String billNo, String billDate) throws Exception
    {
	int rows = 0;
	String sqlDelete = "delete from tblqbilltaxdtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ";
	clsGlobalClass.funExecuteUpdateQuery(sqlDelete);

	for (clsBillTaxDtl objBillTaxDtl : listObjBillTaxDtl)
	{
	    String sqlInsertTaxDtl = "insert into tblqbilltaxdtl "
		    + "(strBillNo,strTaxCode,dblTaxableAmount,dblTaxAmount,strClientCode,dteBillDate) "
		    + "values('" + objBillTaxDtl.getStrBillNo() + "','" + objBillTaxDtl.getStrTaxCode() + "'"
		    + "," + objBillTaxDtl.getDblTaxableAmount() + "," + objBillTaxDtl.getDblTaxAmount() + ""
		    + ",'" + clsGlobalClass.gClientCode + "','" + billDate + "')";
	    rows += clsGlobalClass.funExecuteUpdateQuery(sqlInsertTaxDtl);
	}
	return rows;
    }

    private void funLoadSettlementMode(String settlemntType)
    {
	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select a.strSettelmentDesc,a.strSettelmentType "
		    + "from tblsettelmenthd a "
		    + "where a.strSettelmentType='" + settlemntType + "' ");
	    ResultSet resultSet = clsGlobalClass.funExecuteResultSetQuery(sqlBuilder.toString());
	    cmbSettlementMode.removeAllItems();
	    cmbSettlementMode.addItem("All");
	    String cashSettlementMode = "Cash";
	    while (resultSet.next())
	    {
		cmbSettlementMode.addItem(resultSet.getString(1));
		if (resultSet.getString(1).equalsIgnoreCase("Cash"));
		{
		    cashSettlementMode = resultSet.getString(1);
		}
	    }
	    resultSet.close();
	    cmbSettlementMode.setSelectedItem(cashSettlementMode);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{

	}
    }

    private void funCmbItemDeletionInsertionTypeAction()
    {
//        BILL DELETION
//        ITEM DELETION
//        ITEM INSERTION        
	if (cmbItemDeletionInsertionType.getSelectedItem().toString().equalsIgnoreCase("BILL DELETION"))
	{
	    funResetButtonClicked();
	}
	else if (cmbItemDeletionInsertionType.getSelectedItem().toString().equalsIgnoreCase("ITEM DELETION"))
	{
	    funEnableMenuHeadWiseDeletion(true);
	}
	else if (cmbItemDeletionInsertionType.getSelectedItem().toString().equalsIgnoreCase("ITEM INSERTION"))
	{
	    funEnableMenuHeadWiseInsertion(true);
	}
    }

    private void funFillTableForItemInsertion()
    {

	dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	strFromDate = dateFormat.format(dteFromDate.getDate());
	strToDate = dateFormat.format(dteToDate.getDate());
	dtmSalesTableModel.setRowCount(0);
	dtmTotalSales.setRowCount(0);
	try
	{
	    sqlBuilder.setLength(0);
	    sqlBuilder.append("select a.strBillNo,date(a.dteBillDate),d.strSettelmentType,a.strSettelmentMode,a.dblDiscountAmt,a.dblTaxAmt,a.dblGrandTotal "
		    + ",e.strBillSeries,e.strHdBillNo ");
	    sqlBuilder.append("FROM tblqbillhd a  "
		    + "inner join tblqbilldtl b on a.strBillNo=b.strBillNo AND DATE(a.dteBillDate)= DATE(b.dteBillDate)  "
		    + "inner join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo AND DATE(a.dteBillDate)= DATE(c.dteBillDate)  "
		    + "inner join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode  "
		    + "left outer join tblbillseriesbilldtl e on a.strBillNo=e.strHdBillNo "
		    + "WHERE DATE(a.dteBillDate) between '" + strFromDate + "' and '" + strToDate + "' ");

	    if (!cmbPosCode.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and a.strPOSCode='" + mapPOSName.get(cmbPosCode.getSelectedItem().toString()) + "' ");
	    }

	    if (!txtFromAmount.getText().isEmpty())
	    {
		if (!txtToAmount.getText().isEmpty())
		{
		    sqlBuilder.append(" and a.dblGrandTotal between '" + Double.parseDouble(txtFromAmount.getText()) + "' and '" + Double.parseDouble(txtToAmount.getText()) + "' ");
		}
	    }
	    if (!cmbSettlementType.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and d.strSettelmentType ='" + cmbSettlementType.getSelectedItem().toString() + "' ");
	    }

	    if (!cmbSettlementMode.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and a.strSettelmentMode ='" + cmbSettlementMode.getSelectedItem().toString() + "' ");
	    }
	    if (!cmbBillSeries.getSelectedItem().toString().equalsIgnoreCase("All"))
	    {
		sqlBuilder.append(" and e.strBillSeries='" + cmbBillSeries.getSelectedItem().toString() + "' ");
	    }

	    sqlBuilder.append(" group by a.strBillNo,a.dteBillDate ");
	    resultSet = statement.executeQuery(sqlBuilder.toString());

	    double totalDisc = 0, totalTaxAmt = 0, totalGrandTotalAmt = 0;
	    while (resultSet.next())
	    {
		Object tblRow[] =
		{
		    resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
		    resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), new Boolean(false)
		};
		dtmSalesTableModel.addRow(tblRow);

		totalDisc += resultSet.getDouble(5);
		totalTaxAmt += resultSet.getDouble(6);
		totalGrandTotalAmt += resultSet.getDouble(7);
	    }
	    resultSet.close();
	    tblSalesTable.setModel(dtmSalesTableModel);

	    //tblTotal
	    Object tblRow[] =
	    {
		"Total", "", "", "", String.valueOf(Math.rint(totalDisc)), String.valueOf(Math.rint(totalTaxAmt)), String.valueOf(Math.rint(totalGrandTotalAmt)), ""
	    };

	    dtmTotalSales.addRow(tblRow);
	    tblTotal.setModel(dtmTotalSales);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{

	}
    }

    private void funInsertItemsIntoBill()
    {
	try
	{

	    frmNumberKeyPad num = new frmNumberKeyPad(this, true, "No. Of Items Insert");
	    num.setVisible(true);
	    //selectedQty = num.getResult();
	    if (null != clsGlobalClass.gNumerickeyboardValue)
	    {
		selectedQty = Integer.parseInt(clsGlobalClass.gNumerickeyboardValue);
		clsGlobalClass.gNumerickeyboardValue = null;
	    }

	    if (selectedQty > listViewOfSelectedItems.getModel().getSize())
	    {
		JOptionPane.showMessageDialog(null, "Please Select More Items.");
		selectedQty = 1;
		return;
	    }

	    int insertItemNo = 0;
	    for (int row = 0; row < tblSalesTable.getRowCount(); row++)
	    {
		boolean isDelete = Boolean.parseBoolean(String.valueOf(tblSalesTable.getValueAt(row, 7)));
		if (isDelete)
		{
		    String billNo = tblSalesTable.getValueAt(row, 0).toString();
		    String billDate = tblSalesTable.getValueAt(row, 1).toString();
		    String posCode = "";

		    insertItemNo = funClearAndUpdateBillNo(posCode, billNo, billDate, insertItemNo, selectedQty);

		}
	    }
	    funCmbItemDeletionInsertionTypeAction();//refresh

	    funUpdateDayEndField();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{

	}
    }

    private int funClearAndUpdateBillNo(String posCode, String billNo, String billdate, int insertItemNo, int offSet)
    {
	try
	{
	    ResultSet rsBillHd = clsGlobalClass.funExecuteResultSetQuery("select * from tblqbillhd a "
		    + "where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billdate + "' ");
//                    + "and strPOSCode='" + posCode + "' "                    
	    if (rsBillHd.next())
	    {
		//billhd info                
		String billDate = rsBillHd.getString(3);
		posCode = rsBillHd.getString(4);
		String clientCode = rsBillHd.getString(17);//client code
		String areaCode = rsBillHd.getString(32);//area code
		double billHdSubTotal = 0.00, billHdGrandTotal = 0.00;

		List<clsBillDtl> listOfBillDtl = new ArrayList<>();

		for (int i = 0; i < offSet; i++)
		{
		    double dblItemAmt = 0.00, itemPrice = 0;
		    int itemQty = 1;
		    String itemName = "", itemCode = "";
//                    do
//                    {
		    itemName = ((DefaultListModel) listViewOfSelectedItems.getModel()).get(insertItemNo).toString();
		    itemCode = funGetItemCode(itemName);

		    itemPrice = funGetItemPrice(posCode, areaCode, itemCode, itemName);
		    dblItemAmt = itemQty * itemPrice;

		    insertItemNo++;

//                    } while (dblItemAmt <= 0);
		    //Bill dtl                                       
		    ResultSet rsBillDtl = clsGlobalClass.funExecuteResultSetQuery("select * from tblqbilldtl "
			    + "where  strBillNo='" + billNo + "' "
			    + "AND DATE(dteBillDate)='" + billdate + "' "
			    + "and strClientCode='" + clientCode + "' "
			    + "order by strKOTNo desc "
			    + "limit 1 ");
		    if (rsBillDtl.next())
		    {
			clsBillDtl objBillDtl = new clsBillDtl();

			objBillDtl.setStrItemCode(itemCode);
			objBillDtl.setStrItemName(itemName);
			objBillDtl.setStrBillNo(billNo);
			objBillDtl.setStrAdvBookingNo(rsBillDtl.getString(4));

			objBillDtl.setDblRate(itemPrice);
			objBillDtl.setDblQuantity(itemQty);
			objBillDtl.setDblAmount(dblItemAmt);

			billHdSubTotal += dblItemAmt;
			billHdGrandTotal += dblItemAmt;

			objBillDtl.setDblTaxAmount(0.00);

			objBillDtl.setDteBillDate(rsBillDtl.getString(9));
			objBillDtl.setStrKOTNo(rsBillDtl.getString(10));
			objBillDtl.setStrClientCode(rsBillDtl.getString(11));
			objBillDtl.setStrCustomerCode(rsBillDtl.getString(12));
			objBillDtl.setTmeOrderProcessing(rsBillDtl.getString(13));
			objBillDtl.setStrDataPostFlag(rsBillDtl.getString(14));
			objBillDtl.setStrMMSDataPostFlag(rsBillDtl.getString(15));
			objBillDtl.setStrManualKOTNo(rsBillDtl.getString(16));
			objBillDtl.setTdhYN("N");
			objBillDtl.setStrPromoCode("");
			objBillDtl.setStrCounterCode(rsBillDtl.getString(19));
			objBillDtl.setStrWaiterNo(rsBillDtl.getString(20));
			objBillDtl.setDblDiscountAmt(0.00);
			objBillDtl.setDblDiscountPer(0.00);
			objBillDtl.setSequenceNo(String.valueOf((i + 1)));

			listOfBillDtl.add(objBillDtl);
		    }

		    //set next item no
		    if ((insertItemNo + 1) > ((DefaultListModel) listViewOfSelectedItems.getModel()).size())
		    {
			insertItemNo = 0;
		    }

		}

		//bill
		clsBillHd objBillHd = new clsBillHd();

		objBillHd.setStrBillNo(billNo);
		objBillHd.setStrAdvBookingNo(rsBillHd.getString(2));
		objBillHd.setDteBillDate(rsBillHd.getString(3));
		objBillHd.setStrPOSCode(posCode);
		objBillHd.setStrSettelmentMode("CASH");//settlement mode

		objBillHd.setDblDiscountAmt(0.00);
		objBillHd.setDblDiscountPer(0.00);
		objBillHd.setDblTaxAmt(0.00);
		objBillHd.setDblSubTotal(billHdSubTotal);
		objBillHd.setDblGrandTotal(billHdGrandTotal);

		objBillHd.setStrTakeAway(rsBillHd.getString(11));
		objBillHd.setStrOperationType(rsBillHd.getString(12));
		objBillHd.setStrUserCreated(rsBillHd.getString(13));
		objBillHd.setStrUserEdited(rsBillHd.getString(14));
		objBillHd.setDteDateCreated(rsBillHd.getString(15));
		objBillHd.setDteDateEdited(rsBillHd.getString(16));
		objBillHd.setStrClientCode(rsBillHd.getString(17));
		objBillHd.setStrTableNo(rsBillHd.getString(18));
		objBillHd.setStrWaiterNo(rsBillHd.getString(19));
		objBillHd.setStrCustomerCode(rsBillHd.getString(20));
		objBillHd.setStrManualBillNo(rsBillHd.getString(21));
		objBillHd.setIntShiftCode(rsBillHd.getInt(22));
		objBillHd.setIntPaxNo(rsBillHd.getInt(23));
		objBillHd.setStrDataPostFlag(rsBillHd.getString(24));
		objBillHd.setStrReasonCode(rsBillHd.getString(25));
		objBillHd.setStrRemarks(rsBillHd.getString(26));

		objBillHd.setDblTipAmount(0.00);

		objBillHd.setDteSettleDate(rsBillHd.getString(28));
		objBillHd.setStrCounterCode(rsBillHd.getString(29));

		objBillHd.setDblDeliveryCharges(0.00);

		objBillHd.setStrCouponCode(rsBillHd.getString(31));
		objBillHd.setStrAreaCode(rsBillHd.getString(32));
		objBillHd.setStrDiscountRemark("");
		objBillHd.setStrTakeAwayRemarks(rsBillHd.getString(34));
		objBillHd.setStrDiscountOn("");
		objBillHd.setStrCardNo(rsBillHd.getString(36));
		objBillHd.setStrTransactionType(rsBillHd.getString(37));
		//end of billhd

		//bill settlement dtl
		clsBillSettlementDtl objBillSettlementDtl = new clsBillSettlementDtl();

		String cashSettlementModeCode = funGetCashSettlementModeCode(posCode, clientCode);

		objBillSettlementDtl.setStrBillNo(billNo);
		objBillSettlementDtl.setStrSettlementCode(cashSettlementModeCode);
		objBillSettlementDtl.setDblSettlementAmt(objBillHd.getDblSubTotal());
		objBillSettlementDtl.setDblPaidAmt(objBillHd.getDblSubTotal());
		objBillSettlementDtl.setStrExpiryDate("");
		objBillSettlementDtl.setStrCardName("");
		objBillSettlementDtl.setStrRemark("");
		objBillSettlementDtl.setStrClientCode(clientCode);
		objBillSettlementDtl.setStrCustomerCode("");
		objBillSettlementDtl.setDblActualAmt(objBillHd.getDblSubTotal());
		objBillSettlementDtl.setDblRefundAmt(0.00);
		objBillSettlementDtl.setStrGiftVoucherCode("");
		objBillSettlementDtl.setStrDataPostFlag("N");

		if (listOfBillDtl.size() > 0)
		{
		    funInsertBillHdAndBillDtlTable(billNo, billdate, objBillHd, listOfBillDtl, objBillSettlementDtl);
		}

	    }
	    rsBillHd.close();

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	finally
	{
	    return insertItemNo;
	}
    }

    private double funGetItemPrice(String posCode, String areaCode, String itemCode, String itemName)
    {
	double itemPrice = 0.00;
	try
	{
	    String areaWisePricing = "N";
	    String sql = "select a.strAreaWisePricing from tblsetup a where (a.strPosCode='" + posCode + "' or a.strPosCode='All') ";
	    ResultSet rs = statement.executeQuery(sql);
	    if (rs.next())
	    {
		areaWisePricing = rs.getString(1);
	    }
	    rs.close();

	    if ("N".equalsIgnoreCase(areaWisePricing))
	    {
		sql = "select a.strItemCode,a.strItemName,a.strPosCode,a.strAreaCode,a.strPriceMonday "
			+ "from tblmenuitempricingdtl a "
			+ "where a.strItemCode='" + itemCode + "' "
			+ "and (a.strPosCode='" + posCode + "' or a.strPosCode='All') ";
	    }
	    else
	    {
		sql = "select a.strItemCode,a.strItemName,a.strPosCode,a.strAreaCode,a.strPriceMonday "
			+ "from tblmenuitempricingdtl a "
			+ "where a.strItemCode='" + itemCode + "' "
			+ "and a.strPosCode='" + posCode + "' "
			+ "and a.strAreaCode='" + areaCode + "' ";
	    }

	    ResultSet rsItemPrice = clsGlobalClass.funExecuteResultSetQuery(sql);
	    if (rsItemPrice.next())
	    {
		itemPrice = rsItemPrice.getDouble(5);//monday price
	    }
	    rsItemPrice.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return itemPrice;
	}
    }

    private void funInsertBillHdAndBillDtlTable(String billNo, String billDate, clsBillHd objBillHd, List<clsBillDtl> listOfBillDtl, clsBillSettlementDtl objBillSettlementDtl)
    {
	try
	{

	    DecimalFormat objDecFormat = new DecimalFormat("####0.00");
	    deleteSql.setLength(0);
	    deleteSql.append("delete from tblqbillhd where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ");
	    statement.executeUpdate(deleteSql.toString());
	    //tblqbilldtl
	    deleteSql.setLength(0);
	    deleteSql.append("delete from tblqbilldtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ");
	    statement.executeUpdate(deleteSql.toString());
	    //tblqbillmodifierdtl
	    deleteSql.setLength(0);
	    deleteSql.append("delete from tblqbillmodifierdtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ");
	    statement.executeUpdate(deleteSql.toString());
	    //tblqbillpromotiondtl
	    deleteSql.setLength(0);
	    deleteSql.append("delete from tblqbillpromotiondtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ");
	    statement.executeUpdate(deleteSql.toString());
	    //tblqbillcomplementrydtl
	    deleteSql.setLength(0);
	    deleteSql.append("delete from tblqbillcomplementrydtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ");
	    statement.executeUpdate(deleteSql.toString());
	    //tblqbilltaxdtl
	    deleteSql.setLength(0);
	    deleteSql.append("delete from tblqbilltaxdtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ");
	    statement.executeUpdate(deleteSql.toString());
	    //tblqbillsettlementdtl
	    deleteSql.setLength(0);
	    deleteSql.append("delete from tblqbillsettlementdtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ");
	    statement.executeUpdate(deleteSql.toString());
	    //tblqbilldiscdtl
	    deleteSql.setLength(0);
	    deleteSql.append("delete from tblqbilldiscdtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ");
	    statement.executeUpdate(deleteSql.toString());

	    //insert bill hd
	    String sqlInsertBillHd = "insert into tblqbillhd(strBillNo,strAdvBookingNo,dteBillDate,strPOSCode,strSettelmentMode,"
		    + "dblDiscountAmt,dblDiscountPer,dblTaxAmt,dblSubTotal,dblGrandTotal,strTakeAway,strOperationType"
		    + ",strUserCreated,strUserEdited,dteDateCreated,dteDateEdited,strClientCode"
		    + ",strTableNo,strWaiterNo,strCustomerCode,strManualBillNo,intShiftCode"
		    + ",intPaxNo,strDataPostFlag,strReasonCode,strRemarks,dblTipAmount,dteSettleDate"
		    + ",strCounterCode,dblDeliveryCharges,strAreaCode,strDiscountRemark,strTakeAwayRemarks"
		    + ",strDiscountOn,strCardNo,strTransactionType,dtBillDate ) "
		    + "values('" + objBillHd.getStrBillNo() + "','" + objBillHd.getStrAdvBookingNo() + "'"
		    + ",'" + objBillHd.getDteBillDate() + "','" + objBillHd.getStrPOSCode() + "'"
		    + ",'" + objBillHd.getStrSettelmentMode() + "','" + objDecFormat.format(objBillHd.getDblDiscountAmt()) + "'"
		    + ",'" + objDecFormat.format(objBillHd.getDblDiscountPer()) + "','" + objBillHd.getDblTaxAmt() + "'"
		    + ",'" + objBillHd.getDblSubTotal() + "','" + Math.rint(objBillHd.getDblGrandTotal()) + "'"
		    + ",'" + objBillHd.getStrTakeAway() + "','" + objBillHd.getStrOperationType() + "'"
		    + ",'" + objBillHd.getStrUserCreated() + "','" + objBillHd.getStrUserEdited() + "'"
		    + ",'" + objBillHd.getDteDateCreated() + "','" + objBillHd.getDteDateEdited() + "'"
		    + ",'" + objBillHd.getStrClientCode() + "','" + objBillHd.getStrTableNo() + "'"
		    + ",'" + objBillHd.getStrWaiterNo() + "','" + objBillHd.getStrCustomerCode() + "'"
		    + ",'" + objBillHd.getStrManualBillNo() + "'," + objBillHd.getIntShiftCode() + ""
		    + "," + objBillHd.getIntPaxNo() + ",'" + objBillHd.getStrDataPostFlag() + "','" + objBillHd.getStrReasonCode() + "'"
		    + ",'" + objBillHd.getStrRemarks() + "'," + objBillHd.getDblTipAmount() + ",'" + objBillHd.getDteSettleDate() + "'"
		    + ",'" + objBillHd.getStrCounterCode() + "'," + objBillHd.getDblDeliveryCharges() + ""
		    + ", '" + objBillHd.getStrAreaCode() + "','" + objBillHd.getStrDiscountRemark() + "'"
		    + ",'" + objBillHd.getStrTakeAwayRemarks() + "','" + objBillHd.getStrDiscountOn() + "'"
		    + ",'" + objBillHd.getStrCardNo() + "','" + objBillHd.getStrTransactionType() + "','" + billDate + "')";

	    clsGlobalClass.funExecuteUpdateQuery(sqlInsertBillHd);

	    //insert bill dtl            
	    for (clsBillDtl objBillDtl : listOfBillDtl)
	    {
		String sqlInsertBillDtl = "insert into tblqbilldtl "
			+ "(strItemCode,strItemName,strBillNo,strAdvBookingNo,dblRate"
			+ ",dblQuantity,dblAmount,dblTaxAmount,dteBillDate,strKOTNo"
			+ ",strClientCode,strCustomerCode,tmeOrderProcessing,strDataPostFlag"
			+ ",strMMSDataPostFlag,strManualKOTNo,tdhYN,strPromoCode,strCounterCode"
			+ ",strWaiterNo,dblDiscountAmt,dblDiscountPer,strSequenceNo,dtBillDate) "
			+ "values ('" + objBillDtl.getStrItemCode() + "','" + objBillDtl.getStrItemName() + "'"
			+ ",'" + objBillDtl.getStrBillNo() + "','" + objBillDtl.getStrAdvBookingNo() + "'," + objBillDtl.getDblRate() + ""
			+ ",'" + objBillDtl.getDblQuantity() + "','" + objBillDtl.getDblAmount() + "'"
			+ "," + objBillDtl.getDblTaxAmount() + ",'" + objBillDtl.getDteBillDate() + "'"
			+ ",'" + objBillDtl.getStrKOTNo() + "','" + objBillDtl.getStrClientCode() + "'"
			+ ",'" + objBillDtl.getStrCustomerCode() + "','" + objBillDtl.getTmeOrderProcessing() + "'"
			+ ",'" + objBillDtl.getStrDataPostFlag() + "','" + objBillDtl.getStrMMSDataPostFlag() + "'"
			+ ",'" + objBillDtl.getStrManualKOTNo() + "','" + objBillDtl.getTdhYN() + "'"
			+ ",'" + objBillDtl.getStrPromoCode() + "','" + objBillDtl.getStrCounterCode() + "'"
			+ ",'" + objBillDtl.getStrWaiterNo() + "','" + objBillDtl.getDblDiscountAmt() + "'"
			+ ",'" + objBillDtl.getDblDiscountPer() + "','" + objBillDtl.getSequenceNo() + "','" + billDate + "')";

		clsGlobalClass.funExecuteUpdateQuery(sqlInsertBillDtl);
	    }

	    //insert bill settlement dtl
	    String sqlInsertBillSettlementDtl = "insert into tblqbillsettlementdtl"
		    + "(strBillNo,strSettlementCode,dblSettlementAmt,dblPaidAmt,strExpiryDate"
		    + ",strCardName,strRemark,strClientCode,strCustomerCode,dblActualAmt"
		    + ",dblRefundAmt,strGiftVoucherCode,strDataPostFlag,dteBillDate) "
		    + "values ('" + objBillSettlementDtl.getStrBillNo() + "'"
		    + ",'" + objBillSettlementDtl.getStrSettlementCode() + "'," + objBillSettlementDtl.getDblSettlementAmt() + ""
		    + "," + objBillSettlementDtl.getDblPaidAmt() + ",'" + objBillSettlementDtl.getStrExpiryDate() + "'"
		    + ",'" + objBillSettlementDtl.getStrCardName() + "','" + objBillSettlementDtl.getStrRemark() + "'"
		    + ",'" + objBillSettlementDtl.getStrClientCode() + "','" + objBillSettlementDtl.getStrCustomerCode() + "'"
		    + "," + objBillSettlementDtl.getDblActualAmt() + "," + objBillSettlementDtl.getDblRefundAmt() + ""
		    + ",'" + objBillSettlementDtl.getStrGiftVoucherCode() + "','" + objBillSettlementDtl.getStrDataPostFlag() + "','" + billDate + "')";

	    clsGlobalClass.funExecuteUpdateQuery(sqlInsertBillSettlementDtl);

	    ///recalculate taxes
	    funReCalculateTax(billNo, billDate);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{

	}
    }

    private String funGetCashSettlementModeCode(String posCode, String clientCode)
    {
	String cashSettlementCode = "S01";
	try
	{
	    ResultSet rsCashSettlementCode = clsGlobalClass.funExecuteResultSetQuery("select a.strSettelmentCode,a.strSettelmentDesc,a.strSettelmentType "
		    + "from tblsettelmenthd a "
		    + "where a.strSettelmentType='cash' "
		    + "limit 1 ");
	    if (rsCashSettlementCode.next())
	    {
		cashSettlementCode = rsCashSettlementCode.getString(1);
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    return cashSettlementCode;
	}
    }

    private void funSetLabelValues()
    {
	try
	{
	    int noOfSelectedBills = 0;
	    double discAmt = 0.00, taxAmt = 0.00, grandTotalAmt = 0.00;
	    for (int row = 0; row < tblSalesTable.getRowCount(); row++)
	    {
		if (Boolean.parseBoolean(String.valueOf(tblSalesTable.getValueAt(row, 7))))
		{
		    noOfSelectedBills++;
		    discAmt += Double.parseDouble(tblSalesTable.getValueAt(row, 4).toString());
		    taxAmt += Double.parseDouble(tblSalesTable.getValueAt(row, 5).toString());
		    grandTotalAmt += Double.parseDouble(tblSalesTable.getValueAt(row, 6).toString());
		}
	    }
	    lblNoOfBillsSelectedValue.setText(String.valueOf(noOfSelectedBills));
	    lblSelectedDiscAmtValue.setText(String.valueOf(discAmt));
	    lblSelectedSettlementModeValue.setText(cmbSettlementMode.getSelectedItem().toString());
	    lblSelectedTaxAmtValue.setText(String.valueOf(taxAmt));
	    lblSelectedGTAmtValue.setText(String.valueOf(grandTotalAmt));
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void funUpdateDayEndField()
    {
	try
	{
	    dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    strFromDate = dateFormat.format(dteFromDate.getDate());
	    strToDate = dateFormat.format(dteToDate.getDate());

	    String queryDayEnd = "select a.strPOSCode,a.dtePOSDate,a.intShiftCode,a.strDayEnd,a.strShiftEnd "
		    + "from tbldayendprocess a "
		    + "where date(a.dtePOSDate) between '" + strFromDate + "' and '" + strToDate + "' "
		    + "and a.strPOSCode='" + mapPOSName.get(cmbPosCode.getSelectedItem().toString()) + "' ";

	    ResultSet rsDayEndData = clsGlobalClass.funExecuteResultSetQuery(queryDayEnd);
	    while (rsDayEndData.next())
	    {
		String dayEndPOSCode = rsDayEndData.getString(1);//posCode
		String posDayEndDate = rsDayEndData.getString(2);//posDayEndDate
		int posShiftEndNo = rsDayEndData.getInt(3);//posShiftEndCode
		String dayEndYN = rsDayEndData.getString(4);//strDayEnd
		String shiftEndYN = rsDayEndData.getString(5);//strShiftEnd
		//*****************************************************************//

		//  Calculate Total Cash Amt, Total Advance Amt, Total Receipts , Total Payments, Ttoal Discount Amt
		//  , No of Discounted Bills, No of Total bills.
		funCalculateDayEndCashForQFile(posDayEndDate, posShiftEndNo, dayEndPOSCode);
		// Update tbldayendprocess table fields     
		funUpdateDayEndFieldsForQFile(posDayEndDate, posShiftEndNo, dayEndYN, dayEndPOSCode);

	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    // Function to calculate total settlement amount and assigns global variables, which are shown on day end/shift end form.
// This function calculate settlement amount from live tables.    
    private int funCalculateDayEndCashForQFile(String posDate, int shiftCode, String posCode)
    {
	double sales = 0.00, totalDiscount = 0.00, totalSales = 0.00, noOfDiscountedBills = 0.00;
	double advCash = 0.00, cashIn = 0.00, cashOut = 0.00;
	try
	{
	    String sql = "SELECT c.strSettelmentDesc,sum(ifnull(b.dblSettlementAmt,0)),sum(a.dblDiscountAmt),c.strSettelmentType"
		    + " FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + " Where a.strBillNo = b.strBillNo and b.strSettlementCode = c.strSettelmentCode "
		    + " and date(a.dteBillDate ) ='" + posDate + "' and a.strPOSCode='" + posCode + "'"
		    + " and a.intShiftCode=" + shiftCode
		    + " GROUP BY c.strSettelmentDesc,a.strPosCode";
	    //System.out.println(sql);
	    ResultSet rsSettlementAmt = clsGlobalClass.funExecuteResultSetQuery(sql);

	    while (rsSettlementAmt.next())
	    {
		//records[1]=rsSettlementAmt.getString(2);
		if (rsSettlementAmt.getString(4).equalsIgnoreCase("Cash"))
		{
		    sales = sales + (Double.parseDouble(rsSettlementAmt.getString(2).toString()));
		}
		totalDiscount = totalDiscount + (Double.parseDouble(rsSettlementAmt.getString(3).toString()));
		totalSales = totalSales + (Double.parseDouble(rsSettlementAmt.getString(2).toString()));
	    }
	    gTotalDiscounts = totalDiscount;
	    gTotalCashSales = totalSales;
	    rsSettlementAmt.close();

	    sql = "SELECT count(strBillNo),sum(dblDiscountAmt) FROM tblqbillhd "
		    + "Where date(dteBillDate ) ='" + posDate + "' and strPOSCode='" + posCode + "' "
		    + "and dblDiscountAmt > 0.00 and intShiftCode=" + shiftCode
		    + " GROUP BY strPosCode";
	    ResultSet rsTotalDiscountBills = clsGlobalClass.funExecuteResultSetQuery(sql);
	    if (rsTotalDiscountBills.next())
	    {
		gNoOfDiscountedBills = rsTotalDiscountBills.getInt(1);
	    }
	    rsTotalDiscountBills.close();

	    sql = "select count(strBillNo) from tblqbillhd where date(dteBillDate ) ='" + posDate + "' and "
		    + "strPOSCode='" + posCode + "' and intShiftCode=" + shiftCode + " "
		    + " GROUP BY strPosCode";
	    ResultSet rsTotalBills = clsGlobalClass.funExecuteResultSetQuery(sql);

	    if (rsTotalBills.next())
	    {
		gTotalBills = rsTotalBills.getInt(1);
	    }
	    rsTotalBills.close();

	    gTotalCashSales = sales;
	    sql = "select count(dblAdvDeposite) from tblqadvancereceipthd "
		    + "where dtReceiptDate='" + posDate + "' and intShiftCode=" + shiftCode;
	    ResultSet rsTotalAdvance = clsGlobalClass.funExecuteResultSetQuery(sql);
	    rsTotalAdvance.next();
	    int cntAdvDeposite = rsTotalAdvance.getInt(1);
	    if (cntAdvDeposite > 0)
	    {
		//sql="select sum(dblAdvDeposite) from tbladvancereceipthd where dtReceiptDate='"+posDate+"'";
		sql = "select sum(b.dblAdvDepositesettleAmt) from tblqadvancereceipthd a,tblqadvancereceiptdtl b,tblsettelmenthd c "
			+ "where date(a.dtReceiptDate)='" + posDate + "' and a.strPOSCode='" + posCode + "' "
			+ "and c.strSettelmentCode=b.strSettlementCode and a.strReceiptNo=b.strReceiptNo "
			+ "and c.strSettelmentType='Cash' and a.intShiftCode=" + shiftCode;
		rsTotalAdvance = clsGlobalClass.funExecuteResultSetQuery(sql);
		rsTotalAdvance.next();
		advCash = Double.parseDouble(rsTotalAdvance.getString(1));
		gTotalAdvanceAmt = advCash;
	    }
	    rsTotalAdvance.close();

	    //sql="select strTransType,sum(dblAmount) from tblcashmanagement where dteTransDate='"+posDate+"'"
	    //    + " and strPOSCode='"+globalVarClass.gPOSCode+"' group by strTransType";
	    sql = "select strTransType,sum(dblAmount),strCurrencyType from tblcashmanagement "
		    + "where dteTransDate='" + posDate + "' and strPOSCode='" + posCode + "' "
		    + "and intShiftCode=" + shiftCode + " group by strTransType,strCurrencyType";
	    ResultSet rsCashTransaction = clsGlobalClass.funExecuteResultSetQuery(sql);

	    while (rsCashTransaction.next())
	    {
		if (rsCashTransaction.getString(1).equals("Float") || rsCashTransaction.getString(1).equals("Transfer In"))
		{
		    cashIn = cashIn + (Double.parseDouble(rsCashTransaction.getString(2).toString()));
		}
		if (rsCashTransaction.getString(1).equals("Withdrawl") || rsCashTransaction.getString(1).equals("Transfer Out") || rsCashTransaction.getString(1).equals("Payments"))
		{
		    cashOut = cashOut + (Double.parseDouble(rsCashTransaction.getString(2).toString()));
		}
	    }
	    cashIn = cashIn + advCash + sales;
	    gTotalReceipt = cashIn;
	    gTotalPayments = cashOut;
	    double inHandCash = (cashIn) - cashOut;
	    gTotalCashInHand = inHandCash;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	return 1;
    }

    // Function to update values in tbldayendprocess table.
// This function updates values from Q File tables.
    public int funUpdateDayEndFieldsForQFile(String posDate, int shiftNo, String dayEnd, String posCode)
    {
	try
	{
	    String sql = "update tbldayendprocess set dblTotalSale = IFNULL((select sum(b.dblSettlementAmt) "
		    + "TotalSale from tblqbillhd a,tblqbillsettlementdtl b "
		    + "where a.strBillNo=b.strBillNo and date(a.dteBillDate) = '" + posDate + "' and "
		    + "a.strPOSCode = '" + posCode + "' and a.intShiftCode=" + shiftNo + "),0)"
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode = '" + posCode + "'"
		    + " and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_1=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);
//            sql = "update tbldayendprocess set dteDayEndDateTime='" + clsGlobalVarClass.getCurrentDateTime() + "'"
//                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalVarClass.gPOSCode + "' and intShiftCode=" + shiftNo;
//            //System.out.println("UpdateDayEndQuery_2=="+sql);
//
//            clsGlobalClass.funExecuteUpdateQuery(sql);
//            sql = "update tbldayendprocess set strUserEdited='" + clsGlobalVarClass.gUserCode + "'"
//                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalVarClass.gPOSCode + "' and intShiftCode=" + shiftNo;
//            //System.out.println("UpdateDayEndQuery_3=="+sql);
//
//            clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblNoOfBill = IFNULL((select count(*) NoOfBills "
		    + "from tblqbillhd where Date(dteBillDate) = '" + posDate + "' and "
		    + "strPOSCode = '" + posCode + "' and intShiftCode=" + shiftNo + "),0)"
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_4=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblNoOfVoidedBill = IFNULL((select count(DISTINCT strBillNo) "
		    + "NoOfVoidBills from tblvoidbillhd where date(dteModifyVoidBill) = " + "'" + posDate + "'"
		    + " and strPOSCode = '" + posCode + "' and strTransType = 'VB'"
		    + " and intShiftCode=" + shiftNo + "),0)"
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_5=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblNoOfModifyBill = IFNULL((select count(DISTINCT b.strBillNo) "
		    + "NoOfModifiedBills from tblqbillhd a,tblvoidbillhd b where a.strBillNo=b.strBillNo"
		    + " and Date(b.dteModifyVoidBill) = '" + posDate + "' and b.strPOSCode='" + posCode + "'"
		    + " and b.strTransType = 'MB' and a.intShiftCode=" + shiftNo + "),0)"
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_6=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblHDAmt=IFNULL((select sum(a.dblGrandTotal) HD from tblqbillhd a,"
		    + "tblhomedelivery b where a.strBillNo=b.strBillNo and date(a.dteBillDate) = '" + posDate + "' and "
		    + "a.strPOSCode = '" + posCode + "' and a.intShiftCode=" + shiftNo + "), 0) "
		    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_7=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblDiningAmt=IFNULL(( select sum(dblGrandTotal) Dining"
		    + " from tblqbillhd where strTakeAway='No' and date(dteBillDate) = '" + posDate + "' and strPOSCode = '" + posCode + "'"
		    + "  and strBillNo NOT IN (select strBillNo from tblhomedelivery where strBillNo is not NULL) and intShiftCode=" + shiftNo + "),0)"
		    + "  where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    clsGlobalClass.funExecuteUpdateQuery(sql);
	    //System.out.println("UpdateDayEndQuery_8=="+sql);

	    sql = "update tbldayendprocess set dblTakeAway=IFNULL((select sum(dblGrandTotal) TakeAway from tblqbillhd"
		    + " where strTakeAway='Yes' and date(dteBillDate) = '" + posDate + "' and strPOSCode = '" + posCode + "'"
		    + " and intShiftCode=" + shiftNo + "),0)"
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;

	    //System.out.println("UpdateDayEndQuery_9=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblFloat=IFNULL((select sum(dblAmount) TotalFloats from tblcashmanagement "
		    + "where strTransType='Float' and date(dteTransDate) = '" + posDate + "' and strPOSCode = '" + posCode + "'"
		    + " and intShiftCode=" + shiftNo + ""
		    + " group by strTransType),0) "
		    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_10=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblTransferIn=IFNULL((select sum(dblAmount) TotalTransferIn from tblcashmanagement "
		    + "where strTransType='Transfer In' and dteTransDate = '" + posDate + "'"
		    + " and strPOSCode = '" + posCode + "' and intShiftCode=" + shiftNo
		    + " group by strTransType),0) "
		    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_11=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblTransferOut=IFNULL((select sum(dblAmount) TotalTransferOut from tblcashmanagement "
		    + "where strTransType='Transfer Out' and date(dteTransDate) = '" + posDate + "'"
		    + " and strPOSCode = '" + posCode + "' and intShiftCode=" + shiftNo + ""
		    + " group by strTransType),0) "
		    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_12=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblWithdrawal=IFNULL(( select sum(dblAmount) TotalWithdrawals from tblcashmanagement "
		    + "where strTransType='Withdrawal' and date(dteTransDate) = '" + posDate + "' "
		    + "and strPOSCode = '" + posCode + "' and intShiftCode=" + shiftNo + ""
		    + " group by strTransType),0) "
		    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_13=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblRefund=IFNULL(( select sum(dblAmount) TotalRefunds from tblcashmanagement "
		    + " where strTransType='Refund' and date(dteTransDate) = '" + posDate + "' and strPOSCode = '" + posCode + "'"
		    + " and intShiftCode=" + shiftNo + " group by strTransType),0)"
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_14=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblPayments=IFNULL(( select sum(dblAmount) TotalPayments from tblcashmanagement "
		    + "where strTransType='Payments' and date(dteTransDate) = '" + posDate + "'"
		    + " and strPOSCode = '" + posCode + "' and intShiftCode=" + shiftNo + ""
		    + " group by strTransType),0) "
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_15=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblAdvance=IFNULL((select sum(b.dblAdvDepositesettleAmt) "
		    + "from tbladvancereceipthd a,tbladvancereceiptdtl b,tblsettelmenthd c "
		    + "where date(a.dtReceiptDate)='" + posDate + "' and a.strPOSCode='" + posCode + "' "
		    + "and c.strSettelmentCode=b.strSettlementCode and a.strReceiptNo=b.strReceiptNo "
		    + "and c.strSettelmentType='Cash' and intShiftCode=" + shiftNo + "),0)"
		    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_16=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblTotalReceipt=" + gTotalReceipt
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_17=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblTotalPay=" + gTotalPayments
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_18=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblCashInHand=" + gTotalCashInHand
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_19=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblCash=" + gTotalCashSales
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println(sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblTotalDiscount=" + gTotalDiscounts
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_21=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set dblNoOfDiscountedBill=" + gNoOfDiscountedBills
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_22=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set intTotalPax=IFNULL((select sum(intPaxNo)"
		    + " from tblqbillhd where date(dteBillDate ) ='" + posDate + "' and intShiftCode=" + shiftNo + ""
		    + " and strPOSCode='" + posCode + "'),0)"
		    + " where date(dtePOSDate)='" + posDate + "' "
		    + "and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("UpdateDayEndQuery_23=="+sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set intNoOfTakeAway=(select count(strTakeAway)"
		    + "from tblqbillhd where date(dteBillDate )='" + posDate + "' and intShiftCode=" + shiftNo + ""
		    + " and strPOSCode='" + posCode + "' and strTakeAway='Yes')"
		    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("update int takeawy==" + sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);
	    sql = "update tbldayendprocess set intNoOfHomeDelivery=(select COUNT(strBillNo)from tblhomedelivery where date(dteDate)='" + posDate + "' and strPOSCode='" + posCode + "' )"
		    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
	    //System.out.println("update int homedelivry:==" + sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    // Update Day End Table with Used Card Balance    
	    double debitCardAmtUsed = 0;
	    sql = "select sum(b.dblSettlementAmt) "
		    + " from tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
		    + " where a.strBillNo=b.strBillNo and b.strSettlementCode=c.strSettelmentCode "
		    + " and date(a.dteBillDate)='" + posDate + "' and a.strPOSCode='" + posCode + "' "
		    + " and c.strSettelmentType='Debit Card' "
		    + " group by a.strPOSCode,date(a.dteBillDate),c.strSettelmentType;";
	    ResultSet rsUsedDCAmt = clsGlobalClass.funExecuteResultSetQuery(sql);
	    if (rsUsedDCAmt.next())
	    {
		debitCardAmtUsed = rsUsedDCAmt.getDouble(1);
	    }
	    rsUsedDCAmt.close();
	    sql = "update tbldayendprocess set dblUsedDebitCardBalance=" + debitCardAmtUsed + " "
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' "
		    + " and intShiftCode=" + shiftNo;
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    // Update Day End Table with UnUsed Card Balance    
	    double debitCardAmtUnUsed = 0;
	    sql = "select sum(dblCardAmt) from tbldebitcardrevenue "
		    + " where strPOSCode='" + posCode + "' and date(dtePOSDate)='" + posDate + "' "
		    + " group by strPOSCode,date(dtePOSDate);";
	    ResultSet rsUnUsedDCAmt = clsGlobalClass.funExecuteResultSetQuery(sql);
	    if (rsUnUsedDCAmt.next())
	    {
		debitCardAmtUnUsed = rsUnUsedDCAmt.getDouble(1);
	    }
	    rsUnUsedDCAmt.close();
	    sql = "update tbldayendprocess set dblUnusedDebitCardBalance=" + debitCardAmtUnUsed + " "
		    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' "
		    + " and intShiftCode=" + shiftNo;
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "UPDATE tbldayendprocess SET dblTipAmt= IFNULL(( "
		    + "SELECT SUM(dblTipAmount) "
		    + "FROM tblqbillhd "
		    + "WHERE DATE(dteBillDate) ='" + posDate + "' AND intShiftCode='" + shiftNo + "' AND strPOSCode='" + posCode + "'),0) "
		    + "WHERE DATE(dtePOSDate)='" + posDate + "' AND strPOSCode='" + posCode + "' AND intShiftCode='" + shiftNo + "' ";
	    clsGlobalClass.funExecuteUpdateQuery(sql);

	    sql = "update tbldayendprocess set intNoOfComplimentaryKOT=(select COUNT(a.strBillNo)"
		    + "from  tblqbillhd a,tblqbillcomplementrydtl b "
		    + "where a.strBillNo=b.strBillNo "
		    + "and date(b.dteBillDate)='" + posDate + "' and a.strPOSCode='" + posCode + "') "
		    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + posCode + "' and intShiftCode=" + shiftNo;
//            System.out.println("intNoOfComplimentaryKOT:==" + sql);
	    clsGlobalClass.funExecuteUpdateQuery(sql);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return 1;
    }

    private void funHeaderMouseClicked()
    {
	this.setState(Frame.ICONIFIED);
    }

    private void funFillBillSeriesCombo()
    {

	try
	{

	    String posName = cmbPosCode.getSelectedItem().toString();
	    String posCode = "All";
	    if (!posName.equalsIgnoreCase("All"))
	    {
		posCode = mapPOSName.get(posName);
	    }

	    String posNameSql = "select a.strPOSCode,a.strType,a.strBillSeries,a.intLastNo,a.strCodes,a.strNames "
		    + "from tblbillseries a ";
	    if (!posName.equalsIgnoreCase("All"))
	    {
		posNameSql = posNameSql + " where (a.strPOSCode='" + posCode + "' or a.strPOSCode='All') ";
	    }
	    statement = connection.createStatement();
	    ResultSet rsBillSeries = statement.executeQuery(posNameSql);
	    cmbBillSeries.addItem("All");
	    while (rsBillSeries.next())
	    {
		cmbBillSeries.addItem(rsBillSeries.getString(3));
	    }
	    rsBillSeries.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{

	}
    }
}
