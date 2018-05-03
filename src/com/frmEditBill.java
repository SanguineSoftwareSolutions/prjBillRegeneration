/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;

public class frmEditBill extends JDialog
{

    ResultSet rs, recordset;
    private String BillNo, sql, Bill_date, userCreated, SubTotal, voidBillDate, Settel_Mode, amount;
    BigDecimal totalAmt;
    String[] reason;
    private String reasoncode, dtPOSDate;
    private String finalAmount;
    private String TableNo, areaCode, operationTypeForTax;
    private double voidedItemAmount, voidedTotalAmount;
    java.util.Vector modVector, itemVector;
    private int selectedBillNoRow;
    public java.util.Vector vTax, vBillNo, vSubTotal;
    double discPer, discAmount, discOnBill;
    int reasoncount;
    double selectedVoidQty, discountAmt, discountPer;
    private ArrayList<ArrayList<Object>> arrListTaxCal;
    private ArrayList<ArrayList<Object>> arrListItemDtls;
    private SimpleDateFormat ddMMyyyHHMMSS;
    private String qBillTaxAmt;
    private int noOfRows;
    private ArrayList<String> updatedItemList;
    private ArrayList<String> deletedItemList;
    private HashMap<String, String> mapItemQty;
    private HashMap<String, String> mapItemAmount;
    private String gClientCode;
    private String billDate;

    public frmEditBill()
    {

        try
        {

            initComponents();
            setModal(true);
            itemVector = new java.util.Vector();
            vSubTotal = new java.util.Vector();
            vTax = new java.util.Vector();
            vBillNo = new java.util.Vector();
            voidedItemAmount = 0;
            voidedTotalAmount = 0;
            totalAmt = new BigDecimal("0.00");
            ddMMyyyHHMMSS = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            updatedItemList = new ArrayList<String>();
            deletedItemList = new ArrayList<String>();
            mapItemQty = new HashMap<String, String>();
            mapItemAmount = new HashMap<String, String>();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void funFillItemGrid(String billNo, String billDate)
    {
        try
        {
            totalAmt = new BigDecimal("0.00");
            DefaultTableModel dm = new DefaultTableModel(
                    new Object[][]
                    {

                    },
                    new String[]
                    {
                        "Item Name", "Quantity", "Amount", "Item Code", "KOT NO"
                    }
            )
            {
                @Override
                public boolean isCellEditable(int row, int column)
                {
                    //all cells false
                    return false;
                }

            };

            lblBillNoValue.setText(billNo);
            arrListItemDtls = new ArrayList<ArrayList<Object>>();

            rs = clsGlobalClass.funExecuteResultSetQuery("select strItemName,strBillNo,dblQuantity,dblAmount,"
                    + "dteBillDate,strItemCode,strKOTNo from tblqbilldtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ");
            while (rs.next())
            {
                ArrayList<Object> arrListItemRow = new ArrayList<Object>();
                String Totalamount = rs.getString(4);
                String itemCode = rs.getString(6);
                Object[] rows =
                {
                    rs.getString(1), rs.getString(3), rs.getString(4), rs.getString(6), rs.getString(7)
                };
                BigDecimal tempAmt = new BigDecimal(Totalamount);
                totalAmt = totalAmt.add(tempAmt);
                dm.addRow(rows);
                arrListItemRow.add(rs.getString(6));
                arrListItemRow.add(rs.getString(4));
                arrListItemDtls.add(arrListItemRow);
                sql = "select strModifierName,dblQuantity,dblAmount,strItemCode,strModifierCode from tblqbillmodifierdtl "
                        + "where strItemCode='" + itemCode + "' and strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ;";
                ResultSet rsModifier = clsGlobalClass.funExecuteResultSetQuery(sql);
                while (rsModifier.next())
                {
                    String modItemCode = rsModifier.getString(5) + rsModifier.getString(4);
                    Object[] modifier =
                    {
                        rsModifier.getString("strModifierName"), rsModifier.getString("dblQuantity"), rsModifier.getString("dblAmount"), modItemCode, ""
                    };
                    dm.addRow(modifier);
                }
                rsModifier.close();
            }
            rs.close();
            discountAmt = 0;
            discountPer = 0;
            tblItemTable.setModel(dm);

            String sql_BillHd = "select dblTaxAmt,dblSubTotal,dblGrandTotal,strUserCreated"
                    + ",dblDiscountAmt,dblDiscountPer,dteBillDate "
                    + "from tblqbillhd where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ";

            rs = clsGlobalClass.funExecuteResultSetQuery(sql_BillHd);
            while (rs.next())
            {
                qBillTaxAmt = rs.getString(1);
                SubTotal = rs.getString(2);
                amount = rs.getString(3);
                userCreated = rs.getString(4);
                discountAmt = Double.parseDouble(rs.getString(5));
                discountPer = Double.parseDouble(rs.getString(6));
                //Date billDate = ddMMyyyHHMMSS.parse(rs.getString(7));
                lblBillDateTimeValue.setText(ddMMyyyHHMMSS.format(billDate));
            }
            finalAmount = SubTotal.toString();
            funCalculateTax();
            double dblTotalTaxAmt = 0;
            for (int cnt = 0; cnt < arrListTaxCal.size(); cnt++)
            {
                ArrayList<Object> list = arrListTaxCal.get(cnt);
                double dblTaxAmt = Double.parseDouble(list.get(3).toString());
                dblTotalTaxAmt = dblTotalTaxAmt + dblTaxAmt;
            }

            lblUserNameValue.setText(userCreated);
            lblSubTotalValue.setText(SubTotal);

            double taxAmount = Double.parseDouble(qBillTaxAmt);
            taxAmount = Math.rint(taxAmount);
            lblTaxValue.setText(String.valueOf(taxAmount));

            lblTotalAmt.setText(amount.toString());

            if (tblItemTable.getColumnModel().getColumnCount() > 0)
            {
                tblItemTable.getColumnModel().getColumn(0).setResizable(false);
                tblItemTable.getColumnModel().getColumn(1).setResizable(false);
                tblItemTable.getColumnModel().getColumn(2).setResizable(false);
                tblItemTable.getColumnModel().getColumn(3).setResizable(false);
                tblItemTable.getColumnModel().getColumn(4).setResizable(false);
            }
            tblItemTable.setShowHorizontalLines(true);
            tblItemTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            tblItemTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
            tblItemTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
            tblItemTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            tblItemTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

            tblItemTable.getColumnModel().getColumn(0).setPreferredWidth(300);
            tblItemTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            tblItemTable.getColumnModel().getColumn(2).setPreferredWidth(100);
            tblItemTable.getColumnModel().getColumn(3).setPreferredWidth(150);
            tblItemTable.getColumnModel().getColumn(4).setPreferredWidth(150);

            int rcnt = tblItemTable.getRowCount();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private int funCalculateTax()
    {
        try
        {
            funCheckDateRangeForTax();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return 1;
    }

    private void funCheckDateRangeForTax() throws Exception
    {
        String taxCode = "", taxName = "", taxOnSP = "", taxType = "", taxOnGD = "", taxCal = "", taxIndicator = "";
        String itemType = "", opType = "", area = "", taxOnTax = "No", taxOnTaxCode = "";
        double taxPercent = 0.00, taxAmount = 0.00, taxableAmount = 0.00, taxCalAmt = 0.00;
        ArrayList<Object> listTax = new ArrayList<Object>();
        arrListTaxCal = new ArrayList<ArrayList<Object>>();
        clsGlobalClass.funExecuteUpdateQuery("truncate table tbltaxtemp;");// Empty Tax Temp Table

        String sql_ChkTaxDate = "select a.strTaxCode,a.strTaxDesc,a.strTaxOnSP,a.strTaxType,a.dblPercent"
                + ",a.dblAmount,a.strTaxOnGD,a.strTaxCalculation,a.strTaxIndicator,a.strAreaCode,a.strOperationType"
                + ",a.strItemType,a.strTaxOnTax,a.strTaxOnTaxCode "
                + "from tbltaxhd a,tbltaxposdtl b ";
        if (clsGlobalClass.gPOSCode.equalsIgnoreCase("All"))
        {
            sql_ChkTaxDate = sql_ChkTaxDate + " where a.strTaxCode=b.strTaxCode and b.strPOSCode=b.strPOSCode ";
        }
        else
        {
            sql_ChkTaxDate = sql_ChkTaxDate + " where a.strTaxCode=b.strTaxCode and b.strPOSCode='" + clsGlobalClass.gPOSCode + "' ";
        }
        sql_ChkTaxDate = sql_ChkTaxDate + "  and a.strTaxOnSP='Sales' "
                + "order by a.strTaxOnTax,a.strTaxDesc";

        System.out.println("TAX SQLL=" + sql_ChkTaxDate);

        ResultSet rsTax = clsGlobalClass.funExecuteResultSetQuery(sql_ChkTaxDate);
        while (rsTax.next())
        {
            taxCode = rsTax.getString(1);
            taxName = rsTax.getString(2);
            taxOnSP = rsTax.getString(3);
            taxType = rsTax.getString(4);
            taxOnGD = rsTax.getString(7);
            taxCal = rsTax.getString(8);
            taxIndicator = rsTax.getString(9);
            taxOnTax = rsTax.getString(13);
            taxOnTaxCode = rsTax.getString(14);

            taxPercent = Double.parseDouble(rsTax.getString(5));
            taxAmount = Double.parseDouble(rsTax.getString(6));
            taxableAmount = 0.00;
            taxCalAmt = 0.00;

            String sql_TaxOn = "select strAreaCode,strOperationType,strItemType "
                    + "from tbltaxhd where strTaxCode='" + taxCode + "'";
            ResultSet rsTaxOn = clsGlobalClass.funExecuteResultSetQuery(sql_TaxOn);
            if (rsTaxOn.next())
            {
                area = rsTaxOn.getString(1);
                opType = rsTaxOn.getString(2);
                itemType = rsTaxOn.getString(3);
            }
            if (funCheckAreaCode(taxCode, area))
            {
                if (funCheckOperationType(taxCode, opType))
                {
                    if (funFindSettlementForTax(taxCode, "Cash"))
                    {
                        listTax = new ArrayList<Object>();
                        if (taxIndicator.trim().length() > 0) // For Indicator Based Tax
                        {
                            double taxIndicatorTotal = funGetTaxIndicatorTotal(taxIndicator);
                            if (taxIndicatorTotal > 0)
                            {
                                taxableAmount = taxIndicatorTotal;
                                if (taxCal.equals("Forward")) // Forward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * (taxPercent / 100);
                                }
                                else // Backward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * 100 / (100 + taxPercent);
                                    taxCalAmt = taxableAmount - taxCalAmt;
                                }
                                listTax.add(taxCode);
                                listTax.add(taxName);
                                listTax.add(taxableAmount);
                                listTax.add(taxCalAmt);
                                listTax.add(taxCal);
                                arrListTaxCal.add(listTax);
                                funInsertTaxTemp(taxCode, taxName, taxableAmount, taxCalAmt, taxCal);
                            }
                        }
                        else // For Blank Indicator
                        {
                            if (taxOnTax.equalsIgnoreCase("Yes")) // For tax On Tax Calculation
                            {
                                taxableAmount = funGetTaxableAmountForTaxOnTax(taxOnTaxCode);
                                if (taxCal.equals("Forward")) // Forward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * (taxPercent / 100);
                                }
                                else // Backward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * 100 / (100 + taxPercent);
                                }
                                listTax.add(taxCode);
                                listTax.add(taxName);
                                listTax.add(taxableAmount);
                                listTax.add(taxCalAmt);
                                listTax.add(taxCal);
                                arrListTaxCal.add(listTax);
                                funInsertTaxTemp(taxCode, taxName, taxableAmount, taxCalAmt, taxCal);
                            }
                            else
                            {
                                if (taxOnGD.equals("Gross"))
                                {
                                    taxableAmount = Double.parseDouble(finalAmount);
                                }
                                else
                                {
                                    System.out.println(Double.parseDouble(finalAmount));
                                    double tempDisc = (Double.parseDouble(finalAmount) * (discountPer / 100));
                                    System.out.println(tempDisc);
                                    taxableAmount = Double.parseDouble(finalAmount) - (tempDisc);
                                }

                                if (taxCal.equals("Forward")) // Forward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * (taxPercent / 100);
                                }
                                else // Backward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * 100 / (100 + taxPercent);
                                }
                                listTax.add(taxCode);
                                listTax.add(taxName);
                                listTax.add(taxableAmount);
                                listTax.add(taxCalAmt);
                                listTax.add(taxCal);
                                arrListTaxCal.add(listTax);
                                funInsertTaxTemp(taxCode, taxName, taxableAmount, taxCalAmt, taxCal);
                            }
                        }
                    }
                }
            }
        }
    }

    private int funInsertTaxTemp(String tempTaxCode, String tempTaxName, double tempTaxableAmt, double tempTaxAmt, String tempTaxCal) throws Exception
    {
        int retRows = 0;
        String sql_TaxTempInsert = "insert into tbltaxtemp (strTaxCode,strTaxName,dblTaxableAmt"
                + ",dblTaxAmt,strTaxCal,strItemName) values('" + tempTaxCode + "','" + tempTaxName + "',"
                + "" + tempTaxableAmt + "," + tempTaxAmt + ",'" + tempTaxCal + "','')";
        retRows = clsGlobalClass.funExecuteUpdateQuery(sql_TaxTempInsert);
        return retRows;
    }

    private boolean funCheckAreaCode(String taxCode, String area)
    {
        boolean flgTaxOn = false;
        String[] spAreaCode = area.split(",");
        for (int cnt = 0; cnt < spAreaCode.length; cnt++)
        {
            if (spAreaCode[cnt].equals(areaCode))
            {
                flgTaxOn = true;
                break;
            }
        }

        return flgTaxOn;
    }

    private boolean funCheckOperationType(String taxCode, String opType)
    {
        boolean flgTaxOn = false;
        String[] spOpType = opType.split(",");
        for (int cnt = 0; cnt < spOpType.length; cnt++)
        {
            if (spOpType[cnt].equals("HomeDelivery") && operationTypeForTax.equalsIgnoreCase("HomeDelivery"))
            {
                flgTaxOn = true;
                break;
            }
            if (spOpType[cnt].equals("DineIn") && operationTypeForTax.equalsIgnoreCase("DineIn"))
            {
                flgTaxOn = true;
                break;
            }
            if (spOpType[cnt].equals("TakeAway") && operationTypeForTax.equalsIgnoreCase("TakeAway"))
            {
                flgTaxOn = true;
                break;
            }
        }
        return flgTaxOn;
    }

    private double funGetTaxIndicatorTotal(String indicator) throws Exception
    {
        String sql_Query = "";
        double indicatorAmount = 0.00;
        for (int cnt = 0; cnt < arrListItemDtls.size(); cnt++)
        {
            ArrayList<Object> tempArrList = arrListItemDtls.get(cnt);
            sql_Query = "select strTaxIndicator from tblitemmaster "
                    + "where strItemCode='" + tempArrList.get(0).toString() + "' "
                    + "and strTaxIndicator='" + indicator + "'";
            ResultSet rsTaxForDB = clsGlobalClass.funExecuteResultSetQuery(sql_Query);
            if (rsTaxForDB.next())
            {
                indicatorAmount += Double.parseDouble(tempArrList.get(1).toString());
            }
            rsTaxForDB.close();
        }
        return indicatorAmount;
    }

    private double funGetTaxableAmountForTaxOnTax(String taxOnTaxCode) throws Exception
    {
        double taxableAmt = 0;
        String[] spTaxOnTaxCode = taxOnTaxCode.split(",");
        for (int cnt = 0; cnt < arrListTaxCal.size(); cnt++)
        {
            for (int t = 0; t < spTaxOnTaxCode.length; t++)
            {
                ArrayList arrListTax = arrListTaxCal.get(cnt);
                if (arrListTax.get(0).toString().equals(spTaxOnTaxCode[t]))
                {
                    taxableAmt += Double.parseDouble(arrListTax.get(2).toString()) + Double.parseDouble(arrListTax.get(3).toString());
                }
            }
        }
        return taxableAmt;
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
        lblModuleName = new javax.swing.JLabel();
        lblformName = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblPosName = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblUserCode = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblHOSign = new javax.swing.JLabel();
        panelMainForm = new JPanel() {  
            public void paintComponent(Graphics g) {  
                Image img = Toolkit.getDefaultToolkit().getImage(  
                    getClass().getResource("/com/imgBGJPOS.png"));  
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);  
            }  
        };
        panelFormBody = new javax.swing.JPanel();
        OrderPanel = new javax.swing.JPanel();
        scrItemDtlGrid = new javax.swing.JScrollPane();
        tblItemTable = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        lblPaxNo = new javax.swing.JLabel();
        btnUp = new javax.swing.JButton();
        btnDown = new javax.swing.JButton();
        lblBillDateTimeValue = new javax.swing.JLabel();
        lblBillDateTime = new javax.swing.JLabel();
        lblBillNo = new javax.swing.JLabel();
        lblBillNoValue = new javax.swing.JLabel();
        lblSubTotalTitle = new javax.swing.JLabel();
        lblSubTotalValue = new javax.swing.JLabel();
        lblTaxTitle = new javax.swing.JLabel();
        lblTaxValue = new javax.swing.JLabel();
        lblUserNameValue = new javax.swing.JLabel();
        lblTotalAmt = new javax.swing.JLabel();
        lblUserName = new javax.swing.JLabel();
        btnItemVoid = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBounds(new java.awt.Rectangle(220, 220, 830, 620));
        setMinimumSize(new java.awt.Dimension(800, 600));

        panelHeader.setBackground(new java.awt.Color(69, 164, 238));
        panelHeader.setLayout(new javax.swing.BoxLayout(panelHeader, javax.swing.BoxLayout.LINE_AXIS));

        lblProductName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblProductName.setForeground(new java.awt.Color(255, 255, 255));
        lblProductName.setText("SPOS -");
        panelHeader.add(lblProductName);

        lblModuleName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblModuleName.setForeground(new java.awt.Color(255, 255, 255));
        panelHeader.add(lblModuleName);

        lblformName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblformName.setForeground(new java.awt.Color(255, 255, 255));
        lblformName.setText("- Edit Bill");
        panelHeader.add(lblformName);
        panelHeader.add(filler4);
        panelHeader.add(filler5);

        lblPosName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblPosName.setForeground(new java.awt.Color(255, 255, 255));
        lblPosName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPosName.setMaximumSize(new java.awt.Dimension(321, 30));
        lblPosName.setMinimumSize(new java.awt.Dimension(321, 30));
        lblPosName.setPreferredSize(new java.awt.Dimension(321, 30));
        panelHeader.add(lblPosName);
        panelHeader.add(filler6);

        lblUserCode.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));
        lblUserCode.setMaximumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setMinimumSize(new java.awt.Dimension(90, 30));
        lblUserCode.setPreferredSize(new java.awt.Dimension(90, 30));
        panelHeader.add(lblUserCode);

        lblDate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblDate.setForeground(new java.awt.Color(255, 255, 255));
        lblDate.setMaximumSize(new java.awt.Dimension(192, 30));
        lblDate.setMinimumSize(new java.awt.Dimension(192, 30));
        lblDate.setPreferredSize(new java.awt.Dimension(192, 30));
        panelHeader.add(lblDate);

        lblHOSign.setMaximumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setMinimumSize(new java.awt.Dimension(34, 30));
        lblHOSign.setPreferredSize(new java.awt.Dimension(34, 30));
        panelHeader.add(lblHOSign);

        getContentPane().add(panelHeader, java.awt.BorderLayout.PAGE_START);

        panelMainForm.setLayout(new java.awt.GridBagLayout());

        panelFormBody.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        panelFormBody.setMinimumSize(new java.awt.Dimension(800, 570));
        panelFormBody.setOpaque(false);

        OrderPanel.setBackground(new java.awt.Color(255, 255, 255));
        OrderPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        OrderPanel.setForeground(new java.awt.Color(254, 184, 80));
        OrderPanel.setOpaque(false);
        OrderPanel.setPreferredSize(new java.awt.Dimension(260, 600));
        OrderPanel.setLayout(null);

        tblItemTable.setBackground(new java.awt.Color(51, 102, 255));
        tblItemTable.setForeground(new java.awt.Color(255, 255, 255));
        tblItemTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Description", "Qty", "Amount", "ModCode", "KOT NO"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        tblItemTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tblItemTable.setRowHeight(30);
        tblItemTable.setShowVerticalLines(false);
        tblItemTable.getTableHeader().setReorderingAllowed(false);
        tblItemTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                tblItemTableMouseClicked(evt);
            }
        });
        scrItemDtlGrid.setViewportView(tblItemTable);
        if (tblItemTable.getColumnModel().getColumnCount() > 0)
        {
            tblItemTable.getColumnModel().getColumn(0).setResizable(false);
            tblItemTable.getColumnModel().getColumn(1).setResizable(false);
            tblItemTable.getColumnModel().getColumn(2).setResizable(false);
            tblItemTable.getColumnModel().getColumn(3).setResizable(false);
            tblItemTable.getColumnModel().getColumn(4).setResizable(false);
        }

        OrderPanel.add(scrItemDtlGrid);
        scrItemDtlGrid.setBounds(10, 70, 800, 300);

        lblTotal.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        lblTotal.setText("TOTAL     :");
        OrderPanel.add(lblTotal);
        lblTotal.setBounds(530, 430, 110, 40);
        OrderPanel.add(lblPaxNo);
        lblPaxNo.setBounds(290, 20, 0, 0);

        btnUp.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnUp.setText("UP");
        btnUp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUp.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnUpMouseClicked(evt);
            }
        });
        btnUp.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnUpActionPerformed(evt);
            }
        });
        OrderPanel.add(btnUp);
        btnUp.setBounds(10, 370, 90, 40);

        btnDown.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnDown.setText("DOWN");
        btnDown.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDown.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnDownMouseClicked(evt);
            }
        });
        OrderPanel.add(btnDown);
        btnDown.setBounds(120, 370, 90, 40);
        OrderPanel.add(lblBillDateTimeValue);
        lblBillDateTimeValue.setBounds(380, 20, 140, 30);

        lblBillDateTime.setText("Date & Time :");
        OrderPanel.add(lblBillDateTime);
        lblBillDateTime.setBounds(290, 20, 90, 30);

        lblBillNo.setText("Bill No. :");
        OrderPanel.add(lblBillNo);
        lblBillNo.setBounds(10, 20, 50, 30);
        OrderPanel.add(lblBillNoValue);
        lblBillNoValue.setBounds(70, 20, 130, 30);

        lblSubTotalTitle.setText("SUB TOTAL   :");
        OrderPanel.add(lblSubTotalTitle);
        lblSubTotalTitle.setBounds(530, 370, 110, 30);

        lblSubTotalValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        OrderPanel.add(lblSubTotalValue);
        lblSubTotalValue.setBounds(640, 370, 100, 30);

        lblTaxTitle.setText("TAX               :");
        OrderPanel.add(lblTaxTitle);
        lblTaxTitle.setBounds(530, 400, 110, 30);

        lblTaxValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        OrderPanel.add(lblTaxValue);
        lblTaxValue.setBounds(640, 400, 100, 30);
        OrderPanel.add(lblUserNameValue);
        lblUserNameValue.setBounds(650, 20, 110, 30);

        lblTotalAmt.setBackground(new java.awt.Color(255, 255, 255));
        lblTotalAmt.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        lblTotalAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        OrderPanel.add(lblTotalAmt);
        lblTotalAmt.setBounds(640, 430, 100, 40);

        lblUserName.setText("User Created :");
        OrderPanel.add(lblUserName);
        lblUserName.setBounds(550, 20, 90, 30);

        btnItemVoid.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnItemVoid.setText("VOID ITEM");
        btnItemVoid.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnItemVoid.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnItemVoidMouseClicked(evt);
            }
        });
        OrderPanel.add(btnItemVoid);
        btnItemVoid.setBounds(230, 370, 100, 40);

        btnSave.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnSave.setText("SAVE");
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnSaveMouseClicked(evt);
            }
        });
        OrderPanel.add(btnSave);
        btnSave.setBounds(10, 430, 90, 40);

        btnCancel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnCancel.setText("CANCEL");
        btnCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnCancelMouseClicked(evt);
            }
        });
        OrderPanel.add(btnCancel);
        btnCancel.setBounds(120, 430, 90, 40);

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton1.setText("EXIT");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jButton1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelFormBodyLayout = new javax.swing.GroupLayout(panelFormBody);
        panelFormBody.setLayout(panelFormBodyLayout);
        panelFormBodyLayout.setHorizontalGroup(
            panelFormBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFormBodyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFormBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(OrderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelFormBodyLayout.createSequentialGroup()
                        .addGap(0, 730, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelFormBodyLayout.setVerticalGroup(
            panelFormBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormBodyLayout.createSequentialGroup()
                .addComponent(OrderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 476, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panelMainForm.add(panelFormBody, new java.awt.GridBagConstraints());

        getContentPane().add(panelMainForm, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tblItemTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblItemTableMouseClicked
        selectedVoidQty = Double.parseDouble(tblItemTable.getValueAt(tblItemTable.getSelectedRow(), 1).toString());
    }//GEN-LAST:event_tblItemTableMouseClicked

    private void btnUpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUpMouseClicked
        // TODO add your handling code here:

        if (tblItemTable.getModel().getRowCount() > 0)
        {
            int r = tblItemTable.getSelectedRow();
            tblItemTable.changeSelection(r - 1, 0, false, false);
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Please Select Item.");
            return;
        }
    }//GEN-LAST:event_btnUpMouseClicked

    private void btnUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnUpActionPerformed

    private void btnDownMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDownMouseClicked
        // TODO add your handling code here:

        if (tblItemTable.getModel().getRowCount() > 0)
        {
            int r = tblItemTable.getSelectedRow();
            int rowcount = tblItemTable.getRowCount();
            if (r < rowcount)
            {
                tblItemTable.changeSelection(r + 1, 0, false, false);
            }
            else if (r == rowcount)
            {
                r = 0;
                tblItemTable.changeSelection(r, 0, false, false);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Please Select Item.");
            return;
        }
    }//GEN-LAST:event_btnDownMouseClicked

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jButton1MouseClicked
    {//GEN-HEADEREND:event_jButton1MouseClicked
        dispose();
    }//GEN-LAST:event_jButton1MouseClicked

    private void btnSaveMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnSaveMouseClicked
    {//GEN-HEADEREND:event_btnSaveMouseClicked
        System.out.println("global gPOSCode=" + clsGlobalClass.gPOSCode);
        if (updatedItemList.size() > 0 || deletedItemList.size() > 0)
        {
            try
            {
                funSaveChanges();
                JOptionPane.showMessageDialog(null, "Updated Successfully.");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            JOptionPane.showMessageDialog(null, "No Changes Were Made.");
            return;
        }

        updatedItemList.clear();
        deletedItemList.clear();
        mapItemQty.clear();
        mapItemAmount.clear();
        funGetBillDetail(lblBillNoValue.getText(), this.billDate);

    }//GEN-LAST:event_btnSaveMouseClicked

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnCancelMouseClicked
    {//GEN-HEADEREND:event_btnCancelMouseClicked
        updatedItemList.clear();
        deletedItemList.clear();
        mapItemQty.clear();
        mapItemAmount.clear();
        funGetBillDetail(lblBillNoValue.getText(), this.billDate);
    }//GEN-LAST:event_btnCancelMouseClicked

    private void btnItemVoidMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btnItemVoidMouseClicked
    {//GEN-HEADEREND:event_btnItemVoidMouseClicked
        noOfRows = tblItemTable.getRowCount();
        if (noOfRows > 1)
        {
            int selectedRow = tblItemTable.getSelectedRow();
            if (selectedRow < 0)
            {
                JOptionPane.showMessageDialog(null, "Please Select Item.");
                return;
            }
            else
            {
                funEditSelectedRow(selectedRow);
            }
        }
    }//GEN-LAST:event_btnItemVoidMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(frmEditBill.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(frmEditBill.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(frmEditBill.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(frmEditBill.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new frmEditBill().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OrderPanel;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDown;
    private javax.swing.JButton btnItemVoid;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUp;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel lblBillDateTime;
    private javax.swing.JLabel lblBillDateTimeValue;
    private javax.swing.JLabel lblBillNo;
    public javax.swing.JLabel lblBillNoValue;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblHOSign;
    private javax.swing.JLabel lblModuleName;
    private javax.swing.JLabel lblPaxNo;
    private javax.swing.JLabel lblPosName;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblSubTotalTitle;
    private javax.swing.JLabel lblSubTotalValue;
    private javax.swing.JLabel lblTaxTitle;
    private javax.swing.JLabel lblTaxValue;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalAmt;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblUserName;
    private javax.swing.JLabel lblUserNameValue;
    private javax.swing.JLabel lblformName;
    private javax.swing.JPanel panelFormBody;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelMainForm;
    private javax.swing.JScrollPane scrItemDtlGrid;
    private javax.swing.JTable tblItemTable;
    // End of variables declaration//GEN-END:variables

    void funGetBillDetail(String selectedBillNo, String billDate)
    {
        try
        {
            lblBillNoValue.setText(selectedBillNo);
            String sql = "select a.strOperationType,a.strTableNo,ifnull(b.strAreaCode,'') "
                    + "from tblqbillhd a left outer join tbltablemaster b on a.strTableNo=b.strTableNo "
                    + "where strBillNo='" + selectedBillNo + "' "
                    + "AND DATE(dteBillDate)='" + billDate + "' ";
            ResultSet rsBillInfo = clsGlobalClass.funExecuteResultSetQuery(sql);
            if (rsBillInfo.next())
            {
                operationTypeForTax = "DineIn";
                if (rsBillInfo.getString(1).equalsIgnoreCase("HomeDelivery"))
                {
                    operationTypeForTax = "HomeDelivery";
                }
                if (rsBillInfo.getString(1).equalsIgnoreCase("TakeAway"))
                {
                    operationTypeForTax = "TakeAway";
                }
                if (rsBillInfo.getString(2).trim().length() > 0)
                {
                    areaCode = rsBillInfo.getString(3);
                }

            }
            rsBillInfo.close();

            this.billDate = billDate;
            funFillItemGrid(selectedBillNo, billDate);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void funEditSelectedRow(int selectedRow)
    {
        double qty = Double.parseDouble(tblItemTable.getValueAt(selectedRow, 1).toString());
        if (qty > 1)
        {
            int result = JOptionPane.showConfirmDialog(null, "Do You Want To Change Quantity ?", "Change Quantity!!!", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == 0)//yes
            {
                JLabel lblChangeQty = new JLabel("Please Enter Quatiyy.");
                String strQty = JOptionPane.showInputDialog(null, lblChangeQty);
                double dblNewQty = 0.00;
                try
                {
                    dblNewQty = Double.parseDouble(strQty);
                }
                catch (NumberFormatException e)
                {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Please Enter Valid Quantity.");
                    return;
                }

                double dblOldQty = Double.parseDouble(tblItemTable.getValueAt(selectedRow, 1).toString());
                if (dblNewQty == 0)
                {
                    JOptionPane.showMessageDialog(null, "Please Enter Valid Quantity.");
                    return;
                }
                else
                {
                    if (dblNewQty < dblOldQty)
                    {
                        double oldAmount = Double.parseDouble(tblItemTable.getValueAt(selectedRow, 2).toString());
                        double rate = oldAmount / dblOldQty;
                        System.out.println("rate=" + rate);
                        funChangeQuantity(selectedRow, dblNewQty, rate);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "New Quantity Should Not Be Greater Than Old Quantity.");
                        return;
                    }
                }
            }
            else if (result == 1)//no
            {
                funRemoveSelectedRow(selectedRow);
            }
            else if (result == 2)//cancel
            {
                return;
            }
        }
        else
        {
            funRemoveSelectedRow(selectedRow);
        }
    }

    private void funChangeQuantity(int selectedRow, double dblNewQty, double itemRate)
    {
        String itemCode = tblItemTable.getValueAt(selectedRow, 3).toString();
        double newAmount = dblNewQty * itemRate;

        if (!updatedItemList.contains(itemCode))
        {
            updatedItemList.add(itemCode);
        }

        if (mapItemQty.containsKey(itemCode))
        {
            mapItemQty.remove(itemCode);
        }
        if (mapItemAmount.containsKey(itemCode))
        {
            mapItemAmount.remove(itemCode);
        }
        mapItemQty.put(itemCode, String.valueOf(dblNewQty));
        mapItemAmount.put(itemCode, String.valueOf(newAmount));

        tblItemTable.setValueAt(dblNewQty, selectedRow, 1);
        tblItemTable.setValueAt(newAmount, selectedRow, 2);
    }

    private void funRemoveSelectedRow(int selectedRow)
    {
        String itemCode = tblItemTable.getValueAt(selectedRow, 3).toString();
        deletedItemList.add(itemCode);
        ((DefaultTableModel) tblItemTable.getModel()).removeRow(selectedRow);
    }

    private void funSaveChanges() throws Exception
    {
        try
        {

            funUpdateBillItems();
            funDeleteBillItems();
            funReCalculateTax();

            //funRegenerateTax(clsGlobalClass.gPOSCode, clsGlobalClass.gFromDate, clsGlobalClass.gToDate);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void funUpdateBillItems()
    {
        try
        {
            funUpdateQBillDtl();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void funDeleteBillItems()
    {
        try
        {
            funDeleteQBillDtl();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void funUpdateQBillDtl()
    {
        String billNo = lblBillNoValue.getText();
        String sql = "";
        for (int i = 0; i < updatedItemList.size(); i++)
        {
            String itemCode = updatedItemList.get(i);
            String quantity = mapItemQty.get(itemCode);
            String amount = mapItemAmount.get(itemCode);

            sql = " update tblqbilldtl "
                    + "set dblQuantity='" + quantity + "'"
                    + ",dblAmount='" + amount + "'"
                    + ",dblDiscountAmt=FORMAT(((dblDiscountPer/100)*dblAmount),2) "
                    + " where strItemCode='" + itemCode + "' and strBillNo='" + billNo + "' ";

            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = " update tblqbillmodifierdtl "
                    + "set dblQuantity='" + quantity + "'"
                    + ",dblAmount='" + amount + "' "
                    + ",dblDiscAmt=FORMAT(((dblDiscPer/100)*dblAmount),2) "
                    + " where strItemCode='" + itemCode + "' and strBillNo='" + billNo + "' ";

            clsGlobalClass.funExecuteUpdateQuery(sql);
        }
    }

    private void funDeleteQBillDtl()
    {
        String billNo = lblBillNoValue.getText();
        String sql = "";
        for (int i = 0; i < deletedItemList.size(); i++)
        {
            String itemCode = deletedItemList.get(i);

            sql = " delete from tblqbilldtl "
                    + " where strItemCode='" + itemCode + "' and strBillNo='" + billNo + "' ";
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = " delete from tblqbillmodifierdtl "
                    + " where strItemCode='" + itemCode + "' and strBillNo='" + billNo + "' ";

            clsGlobalClass.funExecuteUpdateQuery(sql);
        }
    }

    private int funRegenerateTax(String posCode, String fromDate, String toDate)
    {
        try
        {
            String sql = "";

            String sql_Bills = "select a.strBillNo,ifnull(a.strAreaCode,''),a.strOperationType,ifnull(d.strSettelmentType,'Cash') "
                    + " ,a.dblSubTotal,a.dblDiscountPer "
                    + " from tblqbillhd a left outer join tbltablemaster b on a.strTableNo=b.strTableNo "
                    + " left outer join tblqbillsettlementdtl c on a.strBillNo=c.strBillNo "
                    + " left outer join tblsettelmenthd d on c.strSettlementCode=d.strSettelmentCode "
                    + " where date(a.dteBillDate) between '" + fromDate + "' and '" + toDate + "' and a.strPOSCode='" + posCode + "' "
                    + " and a.strBillNo='" + lblBillNoValue.getText() + "' ";
            ResultSet rsBills = clsGlobalClass.funExecuteResultSetQuery(sql_Bills);
            while (rsBills.next())
            {
                String billNo = rsBills.getString(1);
                String area = rsBills.getString(2);
                if (area.trim().length() == 0)
                {
//                    area=clsGlobalClass.gDirectAreaCode;
                }
                ArrayList<ArrayList<Object>> list = funCalculateTax(billNo, area, rsBills.getString(3), rsBills.getString(4), rsBills.getString(5), rsBills.getString(6), posCode);
                //System.out.println(list);

                String sql_BillTaxDtl = "insert into tblqbilltaxdtl (strBillNo,strTaxCode,dblTaxableAmount,dblTaxAmount,strClientCode,strDataPostFlag) "
                        + " values ";
                String billTaxDtlData = "";
                boolean flgData = false;
                for (int cnt = 0; cnt < list.size(); cnt++)
                {
                    ArrayList<Object> arrListTax = list.get(cnt);
                    billTaxDtlData += "('" + billNo + "','" + arrListTax.get(0) + "','" + arrListTax.get(2) + "','" + arrListTax.get(3) + "'"
                            + ",'" + clsGlobalClass.gClientCode + "','N'),";
                    flgData = true;
                }

                if (flgData)
                {
                    System.out.println(billNo);
                    sql_BillTaxDtl += " " + billTaxDtlData;
                    sql_BillTaxDtl = sql_BillTaxDtl.substring(0, (sql_BillTaxDtl.length() - 1));
                    //System.out.println(sql_BillTaxDtl);

                    sql = "delete from tblqbilltaxdtl where strBillNo='" + billNo + "'";
                    clsGlobalClass.funExecuteUpdateQuery(sql);
                    clsGlobalClass.funExecuteUpdateQuery(sql_BillTaxDtl);

                    //sql="update tblqbilldtl set dblAmount=dblRate*dblQuantity where strBillNo='"+billNo+"'";
                    //clsGlobalClass.funExecuteUpdateQuery(sql);
                    String sql_UpdateBillHdSubTotal = "update tblqbillhd "
                            + " set dblSubTotal=(select ifnull(sum(a.dblAmount),0) from tblqbilldtl a where strBillNo='" + billNo + "' group by strBillNo) "
                            + " where strBillNo='" + billNo + "'";
                    clsGlobalClass.funExecuteUpdateQuery(sql_UpdateBillHdSubTotal);

                    String sql_UpdateBillHdTaxAmt = "update tblqbillhd "
                            + " set dblTaxAmt=(select ifnull(sum(a.dblTaxAmount),0) from tblqbilltaxdtl a where strBillNo='" + billNo + "' group by strBillNo) "
                            + " where strBillNo='" + billNo + "'";
                    clsGlobalClass.funExecuteUpdateQuery(sql_UpdateBillHdTaxAmt);
                    //Change By Pavan Date 20-03-2015.
                    String sql_UpdateBillHdGrandTotal = "update tblqbillhd set dblGrandTotal=round((dblSubTotal-dblDiscountAmt)+dblTaxAmt,0) "
                            + " where strBillNo='" + billNo + "'";
                    clsGlobalClass.funExecuteUpdateQuery(sql_UpdateBillHdGrandTotal);

                    /*String sql_BillSettlementAmt="update tblqbillsettlementdtl  set dblSettlementAmt=  (select a.dblGrandTotal "
                     + " from tblqbillhd a,tblqbillsettlementdtl b where a.strbillno=b.strbillNo "
                     + " and strBillNo='"+billNo+"' and (b.dblSettlementAmt-a.dblGrandTotal) not between -1 and 1 "
                     + " and a.strSettelmentMode<> 'MultiSettle')";
                     */
                    String sql_BillSettlementAmt = "update tblqbillsettlementdtl c "
                            + " join (select a.dblGrandTotal as GrandTotal,a.strBillNo as BillNo "
                            + " from tblqbillhd a,tblqbillsettlementdtl b "
                            + " where a.strbillno=b.strbillNo and a.strBillNo='" + billNo + "' "
                            + " and (b.dblSettlementAmt-a.dblGrandTotal) not between -0.01 and 0.01 "
                            + " and a.strSettelmentMode<> 'MultiSettle') d "
                            + " on c.strbillno=d.BillNo "
                            + " set c.dblSettlementAmt = d.GrandTotal where c.strBillNo='" + billNo + "'";
                    clsGlobalClass.funExecuteUpdateQuery(sql_BillSettlementAmt);

                    /*
                     String multiSettleBill="update tblqbillhd a, tblqbillsettlementdtl b "
                     + "set a.dblgrandtotal = sum(b.dblSettlementAmt) "
                     + "where a.strBillNo=b.strBillNo and a.strSettelmentMode='MultiSettle' "
                     + "group by b.strBillNo ";
                     System.out.println(multiSettleBill);
                     clsGlobalClass.funExecuteUpdateQuery(multiSettleBill);
                     */
                }
            }
            sql = "update tblqbillhd set dblGrandTotal=0 "
                    + " where strSettelmentMode='complimentory' "
                    + " and date(dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                    + " and strPOSCode='" + posCode + "' "
                    + "  and strBillNo='" + lblBillNoValue.getText() + "' ";
            System.out.println(sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tblqbillhd set dblsubtotal=0,dblTaxAmt=0,dblGrandTotal=0 "
                    + " where date(dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                    + " and strPOSCode='" + posCode + "' "
                    + " and strBillNo Not In (select strBillNo from tblqbilldtl) "
                    + "  and strBillNo='" + lblBillNoValue.getText() + "' ";
            System.out.println(sql);
            System.out.println(clsGlobalClass.funExecuteUpdateQuery(sql));

            /* 
             tblqbilldtl DisAmt update as tblqbillHd where  DisAmt=0.00 in tblqbillHd          
             */
            sql = "   update tblqbilldtl a join "
                    + " ( select b.dblDiscountAmt as DisAmt,b.strBillNo as BillNo ,"
                    + " b.strPOSCode as POSCode from tblqbillhd b  "
                    + "where date(b.dteBillDate) between '" + fromDate + "' and '" + toDate + "'  "
                    + "and b.dblDiscountAmt=0.00 ) c "
                    + " on a.strbillno=c.BillNo  "
                    + " set a.dblDiscountAmt=c.DisAmt   "
                    + " where date(dteBillDate) between '" + fromDate + "' and '" + toDate + "' "
                    + " and c.POSCode='" + posCode + "' "
                    + "  and c.BillNo='" + lblBillNoValue.getText() + "' ";
            System.out.println(sql);
            System.out.println(clsGlobalClass.funExecuteUpdateQuery(sql));

            funCalculateDayEndCashForQFile(fromDate, 1);
            //funUpdateDayEndFieldsForQFile(fromDate,1,"Y");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return 1;
    }

    private ArrayList<ArrayList<Object>> funCalculateTax(String billNo, String areaCode, String opTypeFromDB, String settleMode, String subTotal, String discPer, String posCode)
    {
        ArrayList<ArrayList<Object>> list = null;
        try
        {
            list = funCheckDateRangeForTax(billNo, areaCode, opTypeFromDB, settleMode, subTotal, discPer, posCode);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return list;
    }

    private ArrayList<ArrayList<Object>> funCheckDateRangeForTax(String billNo, String areaCode, String opTypeFromDB, String settleMode, String subTotal, String discPer, String posCode) throws Exception
    {
        String taxCode = "", taxName = "", taxOnSP = "", taxType = "", taxOnGD = "", taxCal = "", taxIndicator = "";
        String itemType = "", opType = "", area = "", taxOnTax = "No", taxOnTaxCode = "";
        double taxPercent = 0.00, taxAmount = 0.00, taxableAmount = 0.00, taxCalAmt = 0.00;
        ArrayList<Object> listTax = new ArrayList<Object>();
        ArrayList<ArrayList<Object>> arrListTaxCal = new ArrayList<ArrayList<Object>>();
        clsGlobalClass.funExecuteUpdateQuery("truncate table tbltaxtemp;");// Empty Tax Temp Table

        String sql_ChkTaxDate = "select a.strTaxCode,a.strTaxDesc,a.strTaxOnSP,a.strTaxType,a.dblPercent"
                + ",a.dblAmount,a.strTaxOnGD,a.strTaxCalculation,a.strTaxIndicator,a.strAreaCode,a.strOperationType"
                + ",a.strItemType,a.strTaxOnTax,a.strTaxOnTaxCode "
                + "from tbltaxhd a,tbltaxposdtl b "
                + "where a.strTaxCode=b.strTaxCode and b.strPOSCode='" + posCode + "' "
                + "and a.strTaxOnSP='Sales' "
                + "order by a.strTaxOnTax,a.strTaxDesc";

        ResultSet rsTax = clsGlobalClass.funExecuteResultSetQuery(sql_ChkTaxDate);
        while (rsTax.next())
        {
            taxCode = rsTax.getString(1);
            taxName = rsTax.getString(2);
            taxOnSP = rsTax.getString(3);
            taxType = rsTax.getString(4);
            taxOnGD = rsTax.getString(7);
            taxCal = rsTax.getString(8);
            taxIndicator = rsTax.getString(9);
            taxOnTax = rsTax.getString(13);
            taxOnTaxCode = rsTax.getString(14);
            taxPercent = Double.parseDouble(rsTax.getString(5));
            taxAmount = Double.parseDouble(rsTax.getString(6));
            taxableAmount = 0.00;
            taxCalAmt = 0.00;

            String sql_TaxOn = "select strAreaCode,strOperationType,strItemType "
                    + "from tbltaxhd where strTaxCode='" + taxCode + "'";
            ResultSet rsTaxOn = clsGlobalClass.funExecuteResultSetQuery(sql_TaxOn);
            if (rsTaxOn.next())
            {
                area = rsTaxOn.getString(1);
                opType = rsTaxOn.getString(2);
                itemType = rsTaxOn.getString(3);
            }
            if (funCheckAreaCode(taxCode, area, areaCode))
            {
                if (funCheckOperationType(taxCode, opType, opTypeFromDB))
                {
                    if (funFindSettlementForTax(taxCode, settleMode))
                    {
                        listTax = new ArrayList<Object>();
                        if (taxIndicator.trim().length() > 0) // For Indicator Based Tax
                        {
                            double taxIndicatorTotal = funGetTaxIndicatorTotal(taxIndicator, billNo);
                            if (taxIndicatorTotal > 0)
                            {
                                taxableAmount = taxIndicatorTotal;
                                if (taxCal.equals("Forward")) // Forward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * (taxPercent / 100);
                                }
                                else // Backward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * 100 / (100 + taxPercent);
                                    taxCalAmt = taxableAmount - taxCalAmt;
                                }
                                listTax.add(taxCode);
                                listTax.add(taxName);
                                listTax.add(taxableAmount);
                                listTax.add(taxCalAmt);
                                listTax.add(taxCal);
                                arrListTaxCal.add(listTax);
                                //funInsertTaxTemp(taxCode,taxName,taxableAmount,taxCalAmt,taxCal);
                            }
                        }
                        else // For Blank Indicator
                        {
                            if (taxOnTax.equalsIgnoreCase("Yes")) // For tax On Tax Calculation
                            {
                                taxableAmount = funGetTaxableAmountForTaxOnTax(taxOnTaxCode, arrListTaxCal);
                                if (taxCal.equals("Forward")) // Forward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * (taxPercent / 100);
                                }
                                else // Backward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * 100 / (100 + taxPercent);
                                }
                                listTax.add(taxCode);
                                listTax.add(taxName);
                                listTax.add(taxableAmount);
                                listTax.add(taxCalAmt);
                                listTax.add(taxCal);
                                arrListTaxCal.add(listTax);
                            }
                            else
                            {
                                if (taxOnGD.equals("Gross"))
                                {
                                    taxableAmount = Double.parseDouble(subTotal);
                                }
                                else
                                {
                                    taxableAmount = (Double.parseDouble(subTotal)) - (Double.parseDouble(subTotal) * (Double.parseDouble(discPer) / 100));
                                }

                                if (taxCal.equals("Forward")) // Forward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * (taxPercent / 100);
                                }
                                else // Backward Tax Calculation
                                {
                                    taxCalAmt = taxableAmount * 100 / (100 + taxPercent);
                                }
                                listTax.add(taxCode);
                                listTax.add(taxName);
                                listTax.add(taxableAmount);
                                listTax.add(taxCalAmt);
                                listTax.add(taxCal);
                                arrListTaxCal.add(listTax);
                                //funInsertTaxTemp(taxCode,taxName,taxableAmount,taxCalAmt,taxCal);
                            }
                        }
                    }
                }
            }
        }

        return arrListTaxCal;
    }

    private boolean funCheckAreaCode(String taxCode, String area, String areaCode)
    {
        boolean flgTaxOn = false;
        String[] spAreaCode = area.split(",");
        for (int cnt = 0; cnt < spAreaCode.length; cnt++)
        {
            if (spAreaCode[cnt].equals(areaCode))
            {
                flgTaxOn = true;
                break;
            }
        }

        return flgTaxOn;
    }

    private boolean funCheckOperationType(String taxCode, String opType, String operationTypeForTax)
    {
        boolean flgTaxOn = false;
        String[] spOpType = opType.split(",");
        for (int cnt = 0; cnt < spOpType.length; cnt++)
        {
            if (spOpType[cnt].equals("HomeDelivery") && operationTypeForTax.equalsIgnoreCase("HomeDelivery"))
            {
                flgTaxOn = true;
                break;
            }
            if (spOpType[cnt].equals("DineIn") && operationTypeForTax.equalsIgnoreCase("Dine In"))
            {
                flgTaxOn = true;
                break;
            }
            if (spOpType[cnt].equals("DineIn") && operationTypeForTax.equalsIgnoreCase("Direct Biller"))
            {
                flgTaxOn = true;
                break;
            }
            if (spOpType[cnt].equals("TakeAway") && operationTypeForTax.equalsIgnoreCase("TakeAway"))
            {
                flgTaxOn = true;
                break;
            }
        }
        return flgTaxOn;
    }

    private double funGetTaxIndicatorTotal(String indicator, String billNo) throws Exception
    {
        String sql_Query = "";
        double indicatorAmount = 0.00;

        sql_Query = "select a.strItemCode,b.strTaxIndicator,sum(a.dblAmount) "
                + "from tblqbilldtl a,tblitemmaster b "
                + "where a.strItemCode=b.strItemCode and a.strBillNo='" + billNo + "' and b.strTaxIndicator='" + indicator + "' "
                + " group by b.strTaxIndicator";
        //System.out.println(sql_Query);
        ResultSet rsTaxIndicator = clsGlobalClass.funExecuteResultSetQuery(sql_Query);
        if (rsTaxIndicator.next())
        {
            indicatorAmount += Double.parseDouble(rsTaxIndicator.getString(3));
        }
        rsTaxIndicator.close();
        return indicatorAmount;
    }

    private double funGetItemTypeTotal(String itemType, String billNo) throws Exception
    {
        String sql_Query = "";
        double itemTypeAmount = 0.00;
        sql_Query = "select a.strItemCode,b.strTaxIndicator,sum(a.dblAmount) "
                + "from tblqbilldtl a,tblitemmaster b "
                + "where left(a.strItemCode,7)=b.strItemCode and b.strItemType='" + itemType + "' "
                + "and a.strBillNo='" + billNo + "' "
                + " group by b.strItemType";
        //System.out.println(sql_Query);
        ResultSet raItemType = clsGlobalClass.funExecuteResultSetQuery(sql_Query);
        if (raItemType.next())
        {
            itemTypeAmount += Double.parseDouble(raItemType.getString(3));
        }
        raItemType.close();
        return itemTypeAmount;
    }

    private boolean funFindSettlementForTax(String taxCode, String settlementMode) throws Exception
    {
        boolean flgTaxSettlement = false;
        String sql_SettlementTax = "select strSettlementCode,strSettlementName "
                + "from tblsettlementtax where strTaxCode='" + taxCode + "' "
                + "and strApplicable='true' and strSettlementName='" + settlementMode + "'";
        ResultSet rsTaxSettlement = clsGlobalClass.funExecuteResultSetQuery(sql_SettlementTax);
        if (rsTaxSettlement.next())
        {
            flgTaxSettlement = true;
        }
        rsTaxSettlement.close();
        return flgTaxSettlement;
    }

    private double funGetTaxableAmountForTaxOnTax(String taxOnTaxCode, ArrayList<ArrayList<Object>> arrListTaxCal) throws Exception
    {
        double taxableAmt = 0;
        String[] spTaxOnTaxCode = taxOnTaxCode.split(",");
        for (int cnt = 0; cnt < arrListTaxCal.size(); cnt++)
        {
            for (int t = 0; t < spTaxOnTaxCode.length; t++)
            {
                ArrayList arrListTax = arrListTaxCal.get(cnt);
                if (arrListTax.get(0).toString().equals(spTaxOnTaxCode[t]))
                {
                    taxableAmt += Double.parseDouble(arrListTax.get(2).toString()) + Double.parseDouble(arrListTax.get(3).toString());
                }
            }
        }
        return taxableAmt;
    }

    // Function to calculate total settlement amount and assigns global variables, which are shown on day end/shift end form.
// This function calculate settlement amount from Q File tables.
    public static int funCalculateDayEndCashForQFile(String posDate, int shiftCode)
    {
        double sales = 0.00, totalDiscount = 0.00, totalSales = 0.00, noOfDiscountedBills = 0.00;
        double advCash = 0.00, cashIn = 0.00, cashOut = 0.00;
        try
        {
            String sql = "SELECT c.strSettelmentDesc,sum(b.dblSettlementAmt),sum(a.dblDiscountAmt)"
                    + " FROM tblqbillhd a,tblqbillsettlementdtl b,tblsettelmenthd c "
                    + "Where a.strBillNo = b.strBillNo and b.strSettlementCode = c.strSettelmentCode "
                    + " and date(a.dteBillDate ) ='" + posDate + "' and a.strPOSCode='" + clsGlobalClass.gPOSCode + "'"
                    + " and a.intShiftCode=" + shiftCode
                    + " GROUP BY c.strSettelmentDesc,a.strPosCode ";
            //System.out.println(sql);
            ResultSet rsSettlementAmt = clsGlobalClass.funExecuteResultSetQuery(sql);

            while (rsSettlementAmt.next())
            {
                //records[1]=rsSettlementAmt.getString(2);
                if (rsSettlementAmt.getString(1).equals("Cash"))
                {
                    sales = sales + (Double.parseDouble(rsSettlementAmt.getString(2).toString()));
                }
                totalDiscount = totalDiscount + (Double.parseDouble(rsSettlementAmt.getString(3).toString()));
                totalSales = totalSales + (Double.parseDouble(rsSettlementAmt.getString(2).toString()));
            }
            clsGlobalClass.gTotalDiscounts = totalDiscount;
            clsGlobalClass.gTotalCashSales = totalSales;
            rsSettlementAmt.close();

            sql = "SELECT count(strBillNo),sum(dblDiscountAmt) FROM tblqbillhd "
                    + "Where date(dteBillDate ) ='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' "
                    + "and dblDiscountAmt > 0.00 and intShiftCode=" + shiftCode
                    + " GROUP BY strPosCode";
            ResultSet rsTotalDiscountBills = clsGlobalClass.funExecuteResultSetQuery(sql);
            if (rsTotalDiscountBills.next())
            {
                clsGlobalClass.gNoOfDiscountedBills = rsTotalDiscountBills.getInt(1);
            }
            rsTotalDiscountBills.close();

            sql = "select count(strBillNo) from tblqbillhd where date(dteBillDate ) ='" + posDate + "' and "
                    + "strPOSCode='" + clsGlobalClass.gPOSCode + " and intShiftCode=" + shiftCode + "' "
                    + "GROUP BY strPosCode";
            ResultSet rsTotalBills = clsGlobalClass.funExecuteResultSetQuery(sql);

            if (rsTotalBills.next())
            {
                clsGlobalClass.gTotalBills = rsTotalBills.getInt(1);
            }
            rsTotalBills.close();

            clsGlobalClass.gTotalCashSales = sales;
            sql = "select count(dblAdvDeposite) from tbladvancereceipthd "
                    + "where dtReceiptDate='" + posDate + "' and intShiftCode=" + shiftCode;
            ResultSet rsTotalAdvance = clsGlobalClass.funExecuteResultSetQuery(sql);
            rsTotalAdvance.next();
            int cntAdvDeposite = rsTotalAdvance.getInt(1);
            if (cntAdvDeposite > 0)
            {
                //sql="select sum(dblAdvDeposite) from tbladvancereceipthd where dtReceiptDate='"+posDate+"'";
                sql = "select sum(b.dblAdvDepositesettleAmt) "
                        + "from tbladvancereceipthd a,tbladvancereceiptdtl b,tblsettelmenthd c "
                        + "where date(a.dtReceiptDate)='" + posDate + "' and a.strPOSCode='" + clsGlobalClass.gPOSCode + "' "
                        + "and c.strSettelmentCode=b.strSettlementCode and a.strReceiptNo=b.strReceiptNo "
                        + "and c.strSettelmentType='Cash' and a.intShiftCode=" + shiftCode;
                rsTotalAdvance = clsGlobalClass.funExecuteResultSetQuery(sql);
                rsTotalAdvance.next();
                advCash = Double.parseDouble(rsTotalAdvance.getString(1));
                clsGlobalClass.gTotalAdvanceAmt = advCash;
            }
            rsTotalAdvance.close();

            //sql="select strTransType,sum(dblAmount) from tblcashmanagement where dteTransDate='"+posDate+"'"
            //    + " and strPOSCode='"+globalVarClass.gPOSCode+"' group by strTransType";
            sql = "select strTransType,sum(dblAmount),strCurrencyType from tblcashmanagement "
                    + "where dteTransDate='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' "
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
            clsGlobalClass.gTotalReceipt = cashIn;
            clsGlobalClass.gTotalPayments = cashOut;
            double inHandCash = (cashIn) - cashOut;
            clsGlobalClass.gTotalCashInHand = inHandCash;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return 1;
    }

    // Function to update values in tbldayendprocess table.
// This function updates values from Q File tables.
    public static int funUpdateDayEndFieldsForQFile(String posDate, int shiftNo, String dayEnd)
    {
        try
        {
            String sql = "update tbldayendprocess set dblTotalSale = IFNULL((select sum(b.dblSettlementAmt) "
                    + "TotalSale from tblqbillhd a,tblqbillsettlementdtl b "
                    + "where a.strBillNo=b.strBillNo and date(a.dteBillDate) = '" + posDate + "' and "
                    + "a.strPOSCode = '" + clsGlobalClass.gPOSCode + "' and a.intShiftCode=" + shiftNo + "),0)"
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode = '" + clsGlobalClass.gPOSCode + "'"
                    + " and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_1=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);
            sql = "update tbldayendprocess set dteDayEndDateTime='" + clsGlobalClass.getCurrentDateTime() + "'"
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_2=="+sql);

            clsGlobalClass.funExecuteUpdateQuery(sql);
            sql = "update tbldayendprocess set strUserEdited='" + clsGlobalClass.gUserCode + "'"
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_3=="+sql);

            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblNoOfBill = IFNULL((select count(*) NoOfBills "
                    + "from tblqbillhd where Date(dteBillDate) = '" + posDate + "' and "
                    + "strPOSCode = '" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo + "),0)"
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_4=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblNoOfVoidedBill = IFNULL((select count(DISTINCT strBillNo) "
                    + "NoOfVoidBills from tblvoidbillhd where date(dteModifyVoidBill) = " + "'" + posDate + "'"
                    + " and strPOSCode = '" + clsGlobalClass.gPOSCode + "' and strTransType = 'VB'"
                    + " and intShiftCode=" + shiftNo + "),0)"
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_5=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblNoOfModifyBill = IFNULL((select count(DISTINCT b.strBillNo) "
                    + "NoOfModifiedBills from tblqbillhd a,tblvoidbillhd b where a.strBillNo=b.strBillNo"
                    + " and Date(b.dteModifyVoidBill) = '" + posDate + "' and b.strPOSCode='" + clsGlobalClass.gPOSCode + "'"
                    + " and b.strTransType = 'MB' and a.intShiftCode=" + shiftNo + "),0)"
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_6=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblHDAmt=IFNULL((select sum(a.dblGrandTotal) HD from tblqbillhd a,"
                    + "tblhomedelivery b where a.strBillNo=b.strBillNo and date(a.dteBillDate) = '" + posDate + "' and "
                    + "a.strPOSCode = '" + clsGlobalClass.gPOSCode + "' and a.intShiftCode=" + shiftNo + "), 0) "
                    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_7=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblDiningAmt=IFNULL(( select sum(dblGrandTotal) Dining"
                    + " from tblqbillhd where strTakeAway='No' and date(dteBillDate) = '" + posDate + "' and strPOSCode = '" + clsGlobalClass.gPOSCode + "'"
                    + "  and strBillNo NOT IN (select strBillNo from tblhomedelivery where strBillNo is not NULL) and intShiftCode=" + clsGlobalClass.gShiftNo + "),0)"
                    + "  where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            clsGlobalClass.funExecuteUpdateQuery(sql);
            //System.out.println("UpdateDayEndQuery_8=="+sql);

            sql = "update tbldayendprocess set dblTakeAway=IFNULL((select sum(dblGrandTotal) TakeAway from tblqbillhd"
                    + " where strTakeAway='Yes' and date(dteBillDate) = '" + posDate + "' and strPOSCode = '" + clsGlobalClass.gPOSCode + "'"
                    + " and intShiftCode=" + shiftNo + "),0)"
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;

            //System.out.println("UpdateDayEndQuery_9=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblFloat=IFNULL((select sum(dblAmount) TotalFloats from tblcashmanagement "
                    + "where strTransType='Float' and date(dteTransDate) = '" + posDate + "' and strPOSCode = '" + clsGlobalClass.gPOSCode + "'"
                    + " and intShiftCode=" + shiftNo + ""
                    + " group by strTransType),0) "
                    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_10=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblTransferIn=IFNULL((select sum(dblAmount) TotalTransferIn from tblcashmanagement "
                    + "where strTransType='Transfer In' and dteTransDate = '" + posDate + "'"
                    + " and strPOSCode = '" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo
                    + " group by strTransType),0) "
                    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_11=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblTransferOut=IFNULL((select sum(dblAmount) TotalTransferOut from tblcashmanagement "
                    + "where strTransType='Transfer Out' and date(dteTransDate) = '" + posDate + "'"
                    + " and strPOSCode = '" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo + ""
                    + " group by strTransType),0) "
                    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_12=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblWithdrawal=IFNULL(( select sum(dblAmount) TotalWithdrawals from tblcashmanagement "
                    + "where strTransType='Withdrawal' and date(dteTransDate) = '" + posDate + "' "
                    + "and strPOSCode = '" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo + ""
                    + " group by strTransType),0) "
                    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_13=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblRefund=IFNULL(( select sum(dblAmount) TotalRefunds from tblcashmanagement "
                    + " where strTransType='Refund' and date(dteTransDate) = '" + posDate + "' and strPOSCode = '" + clsGlobalClass.gPOSCode + "'"
                    + " and intShiftCode=" + shiftNo + " group by strTransType),0)"
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_14=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblPayments=IFNULL(( select sum(dblAmount) TotalPayments from tblcashmanagement "
                    + "where strTransType='Payments' and date(dteTransDate) = '" + posDate + "'"
                    + " and strPOSCode = '" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo + ""
                    + " group by strTransType),0) "
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_15=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblAdvance=IFNULL((select sum(b.dblAdvDepositesettleAmt) "
                    + "from tbladvancereceipthd a,tbladvancereceiptdtl b,tblsettelmenthd c "
                    + "where date(a.dtReceiptDate)='" + posDate + "' and a.strPOSCode='" + clsGlobalClass.gPOSCode + "' "
                    + "and c.strSettelmentCode=b.strSettlementCode and a.strReceiptNo=b.strReceiptNo "
                    + "and c.strSettelmentType='Cash' and intShiftCode=" + shiftNo + "),0)"
                    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_16=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblTotalReceipt=" + clsGlobalClass.gTotalReceipt
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_17=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblTotalPay=" + clsGlobalClass.gTotalPayments
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_18=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblCashInHand=" + clsGlobalClass.gTotalCashInHand
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_19=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblCash=" + clsGlobalClass.gTotalCashSales
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println(sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblTotalDiscount=" + clsGlobalClass.gTotalDiscounts
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_21=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set dblNoOfDiscountedBill=" + clsGlobalClass.gNoOfDiscountedBills
                    + " where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_22=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set intTotalPax=IFNULL((select sum(intPaxNo)"
                    + " from tblqbillhd where date(dteBillDate ) ='" + posDate + "' and intShiftCode=" + shiftNo + ""
                    + " and strPOSCode='" + clsGlobalClass.gPOSCode + "'),0)"
                    + " where date(dtePOSDate)='" + posDate + "' "
                    + "and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("UpdateDayEndQuery_23=="+sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);

            sql = "update tbldayendprocess set intNoOfTakeAway=(select count(strTakeAway)"
                    + "from tblqbillhd where date(dteBillDate )='" + posDate + "' and intShiftCode=" + shiftNo + ""
                    + " and strPOSCode='" + clsGlobalClass.gPOSCode + "' and strTakeAway='Yes')"
                    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("update int takeawy==" + sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);
            sql = "update tbldayendprocess set intNoOfHomeDelivery=(select COUNT(strBillNo)from tblhomedelivery where date(dteDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' )"
                    + "where date(dtePOSDate)='" + posDate + "' and strPOSCode='" + clsGlobalClass.gPOSCode + "' and intShiftCode=" + shiftNo;
            //System.out.println("update int homedelivry:==" + sql);
            clsGlobalClass.funExecuteUpdateQuery(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return 1;
    }

    private void funReCalculateTax() throws SQLException, Exception
    {
        String billNo = lblBillNoValue.getText();

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
        String posCode = "", billDate = "", opearationType = "", areaCode = "";
        if (rsbillDtl.next())
        {
            billDate = rsbillDtl.getString(1);
            posCode = rsbillDtl.getString(2);
            opearationType = rsbillDtl.getString(3);
            areaCode = rsbillDtl.getString(4);
            gClientCode = rsbillDtl.getString(5);
        }

        List<clsTaxCalculationDtls> arrListTaxCal = new clsUtility().funCalculateTax(arrListItemDtls, posCode, billDate, areaCode, opearationType, subTotal, totalDiscAmt, "", "Cash");

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
            objBillTaxDtl.setStrClientCode(gClientCode);

            listObjBillTaxBillDtls.add(objBillTaxDtl);
        }

        funInsertBillTaxDtlTable(listObjBillTaxBillDtls);

        clsUtility obj = new clsUtility();
        obj.funUpdateBillDtlWithTaxValues(billNo, "QFile", billDate);

        double dblGrandTotal = 0.00 + subTotal - totalDiscAmt + totalTaxAmt;
        sql = "update tblqbillhd "
                + "set dblDiscountAmt='" + totalDiscAmt + "' "
                + ",dblTaxAmt='" + totalTaxAmt + "' "
                + ",dblSubTotal='" + subTotal + "' "
                + ",dblGrandTotal='" + dblGrandTotal + "' "
                + "where strBillNo='" + billNo + "' "
                + "AND DATE(dteBillDate)='" + billDate + "' ";
        clsGlobalClass.funExecuteUpdateQuery(sql);

        ResultSet rsSettlement = clsGlobalClass.funExecuteResultSetQuery("select  count(*) from tblqbillsettlementdtl where strBillNo='" + billNo + "' ");
        int settleCount = 0;
        if (rsSettlement.next())
        {
            settleCount = rsSettlement.getInt(1);
        }
        rsSettlement.close();
        double settleAmt = dblGrandTotal / settleCount;
        sql = "update tblqbillsettlementdtl "
                + "set dblSettlementAmt='" + settleAmt + "' "
                + ",dblPaidAmt='" + settleAmt + "' "
                + ",dblActualAmt='" + settleAmt + "' "
                + "where strBillNo='" + billNo + "' "
                + "AND DATE(dteBillDate)='" + billDate + "' ";
        clsGlobalClass.funExecuteUpdateQuery(sql);

    }

    private int funInsertBillTaxDtlTable(List<clsBillTaxDtl> listObjBillTaxDtl) throws Exception
    {
        String billNo = lblBillNoValue.getText();
        int rows = 0;
        String sqlDelete = "delete from tblqbilltaxdtl where strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ";
        clsGlobalClass.funExecuteUpdateQuery(sqlDelete);

        for (clsBillTaxDtl objBillTaxDtl : listObjBillTaxDtl)
        {
            String sqlInsertTaxDtl = "insert into tblqbilltaxdtl "
                    + "(strBillNo,strTaxCode,dblTaxableAmount,dblTaxAmount,strClientCode,dteBillDate) "
                    + "values('" + objBillTaxDtl.getStrBillNo() + "','" + objBillTaxDtl.getStrTaxCode() + "'"
                    + "," + objBillTaxDtl.getDblTaxableAmount() + "," + objBillTaxDtl.getDblTaxAmount() + ""
                    + ",'" + gClientCode + "','" + billDate + "')";
            rows += clsGlobalClass.funExecuteUpdateQuery(sqlInsertTaxDtl);
        }
        return rows;
    }
}
