/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ajjim
 */
public class clsUtility
{

    public clsUtility()
    {
    }

    public int funUpdateBillDtlWithTaxValues(String billNo, String billType, String billDate) throws Exception
    {
        Map<String, clsBillItemTaxDtl> hmBillItemTaxDtl = new HashMap<String, clsBillItemTaxDtl>();
        Map<String, clsBillItemTaxDtl> hmBillTaxDtl = new HashMap<String, clsBillItemTaxDtl>();

        String billDtl = "tblbilldtl";
        String billTaxDtl = "tblbilltaxdtl";
        String billModifierDtl = "tblbillmodifierdtl";

        if (billType.equalsIgnoreCase("QFile"))
        {
            billDtl = "tblqbilldtl";
            billTaxDtl = "tblqbilltaxdtl";
            billModifierDtl = "tblqbillmodifierdtl";
        }

        String sql = "select a.strTaxCode,b.dblPercent,b.strTaxIndicator,b.strTaxCalculation,b.strTaxOnGD,b.strTaxOnTax "
                + " ,b.strTaxOnTaxCode,a.dblTaxAmount,a.dblTaxableAmount "
                + " from " + billTaxDtl + " a,tbltaxhd b "
                + " where a.strTaxCode=b.strTaxCode and a.strBillNo='" + billNo + "' and a.dblTaxAmount>0 "
                + " and a.dblTaxableAmount>0 "
                + "AND DATE(dteBillDate)='" + billDate + "' "
                + " order by b.strTaxOnTax,b.strTaxCode; ";
        ResultSet rsBillTaxDtl = clsGlobalClass.funExecuteResultSetQuery(sql);
        while (rsBillTaxDtl.next())
        {
            String taxCode = rsBillTaxDtl.getString(1);
            String taxIndicator = rsBillTaxDtl.getString(3);
            double taxPercentage = rsBillTaxDtl.getDouble(2);
            String taxCalculation = rsBillTaxDtl.getString(4);
            String taxOnGD = rsBillTaxDtl.getString(5);
            String taxOnTax = rsBillTaxDtl.getString(6);
            String taxOnTaxCode = rsBillTaxDtl.getString(7);
            double billTaxAmt = rsBillTaxDtl.getDouble(8);
            double billTaxableAmt = rsBillTaxDtl.getDouble(9);

            sql = "select a.strItemCode,a.dblAmount,b.strTaxIndicator,a.strKOTNo,a.dblDiscountAmt "
                    + " from " + billDtl + " a,tblitemmaster b "
                    + " where a.strItemCode=b.strItemCode and a.strBillNo='" + billNo + "' AND DATE(dteBillDate)='" + billDate + "' ";
            ResultSet rsBillDtl = clsGlobalClass.funExecuteResultSetQuery(sql);
            while (rsBillDtl.next())
            {
                String itemCode = rsBillDtl.getString(1);
                double itemAmt = rsBillDtl.getDouble(2);
                double itemDiscAmt = rsBillDtl.getDouble(5);
                String KOTNo = rsBillDtl.getString(4);
                double taxAmt = 0;
                if (taxOnGD.equalsIgnoreCase("Discount"))
                {
                    itemAmt -= itemDiscAmt;
                }

                sql = "select sum(dblAmount) "
                        + " from tblbillmodifierdtl "
                        + " where strBillNo='" + billNo + "' and left(strItemCode,7)='" + itemCode + "' AND DATE(dteBillDate)='" + billDate + "' "
                        + " group by left(strItemCode,7)";
                //System.out.println(sql);
                ResultSet rsModifierAmt = clsGlobalClass.funExecuteResultSetQuery(sql);
                if (rsModifierAmt.next())
                {
                    itemAmt += rsModifierAmt.getDouble(1);
                }
                rsModifierAmt.close();

                if (taxOnTax.equals("Yes"))
                {
                    String keyForTaxOnTax = itemCode + "," + KOTNo + "," + taxOnTaxCode;
                    if (hmBillTaxDtl.containsKey(keyForTaxOnTax))
                    {
                        clsBillItemTaxDtl objBillItemTaxDtl1 = hmBillTaxDtl.get(keyForTaxOnTax);
                        if (taxIndicator.isEmpty())
                        {
                            taxAmt = (billTaxAmt / billTaxableAmt) * (itemAmt + objBillItemTaxDtl1.getDblTaxAmt());
                        }
                        else
                        {
                            if (rsBillDtl.getString(3).equals(taxIndicator))
                            {
                                taxAmt = (billTaxAmt / billTaxableAmt) * (itemAmt + objBillItemTaxDtl1.getDblTaxAmt());
                            }
                        }
                    }
                    else
                    {
                        if (taxIndicator.isEmpty())
                        {
                            taxAmt = (billTaxAmt / billTaxableAmt) * (itemAmt);
                        }
                        else
                        {
                            if (rsBillDtl.getString(3).equals(taxIndicator))
                            {
                                taxAmt = (billTaxAmt / billTaxableAmt) * (itemAmt);
                            }
                        }
                    }

                    clsBillItemTaxDtl objItemTaxDtl = new clsBillItemTaxDtl();
                    objItemTaxDtl.setStrItemCode(itemCode);
                    objItemTaxDtl.setDblTaxAmt(taxAmt);
                    objItemTaxDtl.setStrKOTNo(KOTNo);
                    objItemTaxDtl.setStrBillNo(billNo);

                    String key2 = itemCode + "," + KOTNo + "," + taxCode;
                    String key1 = itemCode + "," + KOTNo;
                    hmBillTaxDtl.put(key2, objItemTaxDtl);

                    clsBillItemTaxDtl objBillItemTaxDtl = new clsBillItemTaxDtl();
                    objBillItemTaxDtl.setStrItemCode(itemCode);
                    objBillItemTaxDtl.setDblTaxAmt(taxAmt);
                    objBillItemTaxDtl.setStrKOTNo(KOTNo);
                    objBillItemTaxDtl.setStrBillNo(billNo);
                    if (hmBillItemTaxDtl.containsKey(key1))
                    {
                        objBillItemTaxDtl = hmBillItemTaxDtl.get(key1);
                        objBillItemTaxDtl.setDblTaxAmt(objBillItemTaxDtl.getDblTaxAmt() + taxAmt);
                    }
                    hmBillItemTaxDtl.put(key1, objBillItemTaxDtl);
                }
                else
                {
                    if (taxIndicator.isEmpty())
                    {
                        taxAmt = (billTaxAmt / billTaxableAmt) * itemAmt;
                    }
                    else
                    {
                        if (rsBillDtl.getString(3).equals(taxIndicator))
                        {
                            taxAmt = (billTaxAmt / billTaxableAmt) * itemAmt;
                        }
                    }
                    clsBillItemTaxDtl objItemTaxDtl = new clsBillItemTaxDtl();
                    objItemTaxDtl.setStrItemCode(itemCode);
                    objItemTaxDtl.setDblTaxAmt(taxAmt);
                    objItemTaxDtl.setStrKOTNo(KOTNo);
                    objItemTaxDtl.setStrBillNo(billNo);

                    String key2 = itemCode + "," + KOTNo + "," + taxCode;
                    String key1 = itemCode + "," + KOTNo;
                    hmBillTaxDtl.put(key2, objItemTaxDtl);

                    clsBillItemTaxDtl objBillItemTaxDtl = new clsBillItemTaxDtl();
                    objBillItemTaxDtl.setStrItemCode(itemCode);
                    objBillItemTaxDtl.setDblTaxAmt(taxAmt);
                    objBillItemTaxDtl.setStrKOTNo(KOTNo);
                    objBillItemTaxDtl.setStrBillNo(billNo);

                    if (hmBillItemTaxDtl.containsKey(key1))
                    {
                        objBillItemTaxDtl = hmBillItemTaxDtl.get(key1);
                        objBillItemTaxDtl.setDblTaxAmt(objBillItemTaxDtl.getDblTaxAmt() + taxAmt);
                    }
                    hmBillItemTaxDtl.put(key1, objBillItemTaxDtl);
                }
            }
            rsBillDtl.close();
        }
        rsBillTaxDtl.close();

        for (Map.Entry<String, clsBillItemTaxDtl> entry : hmBillItemTaxDtl.entrySet())
        {
            sql = "update " + billDtl + " set dblTaxAmount = " + entry.getValue().getDblTaxAmt() + " "
                    + " where strBillNo='" + billNo + "' and strItemCode='" + entry.getValue().getStrItemCode() + "' "
                    + " and strKOTNo='" + entry.getValue().getStrKOTNo() + "' AND DATE(dteBillDate)='" + billDate + "' ";
            clsGlobalClass.funExecuteUpdateQuery(sql);
            //System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue().getStrItemCode() + " " + entry.getValue().getDblTaxAmt());
        }
        return 1;
    }

    public List funCalculateTax(List<clsItemDtlForTax> arrListItemDtl, String POSCode, String dtPOSDate, String billAreaCode, String operationTypeForTax, double subTotal, double discountAmt, String transType, String settlementCode) throws Exception
    {
        return funCheckDateRangeForTax(arrListItemDtl, POSCode, dtPOSDate, billAreaCode, operationTypeForTax, subTotal, discountAmt, transType, settlementCode);
    }

    private List funCheckDateRangeForTax(List<clsItemDtlForTax> arrListItemDtl, String POSCode, String dtPOSDate, String billAreaCode, String operationTypeForTax, double subTotal, double discountAmt, String transType, String settlementCode) throws Exception
    {
        List<clsTaxCalculationDtls> arrListTaxDtl = new ArrayList<clsTaxCalculationDtls>();
        String taxCode = "", taxName = "", taxOnGD = "", taxCal = "", taxIndicator = "", taxType = "Percent";
        String opType = "", taxAreaCodes = "", taxOnTax = "No", taxOnTaxCode = "";
        double taxPercent = 0.00, taxFixedAmount = 0.00, taxableAmount = 0.00, taxCalAmt = 0.00;
        clsGlobalClass.funExecuteUpdateQuery("truncate table tbltaxtemp;");// Empty Tax Temp Table

        StringBuilder sbSql = new StringBuilder();
        sbSql.setLength(0);
        sbSql.append("select a.strTaxCode,a.strTaxDesc,a.strTaxOnSP,a.strTaxType,a.dblPercent"
                + ",a.dblAmount,a.strTaxOnGD,a.strTaxCalculation,a.strTaxIndicator,a.strAreaCode,a.strOperationType"
                + ",a.strItemType,a.strTaxOnTax,a.strTaxOnTaxCode "
                + "from tbltaxhd a,tbltaxposdtl b "
                + "where a.strTaxCode=b.strTaxCode and b.strPOSCode='" + POSCode + "' ");
        if (transType.equals("Tax Regen"))
        {
            sbSql.append(" and date(a.dteValidFrom) <='" + dtPOSDate + "' and date(a.dteValidTo)>='" + dtPOSDate + "' ");
        }
        else
        {
            sbSql.append(" and date(a.dteValidFrom) <='" + dtPOSDate + "' and date(a.dteValidTo)>='" + dtPOSDate + "' ");
        }
        sbSql.append(" and a.strTaxOnSP='Sales' "
                + "order by a.strTaxOnTax,a.strTaxDesc");

        ResultSet rsTax = clsGlobalClass.funExecuteResultSetQuery(sbSql.toString());
        while (rsTax.next())
        {
            taxCode = rsTax.getString(1);
            taxName = rsTax.getString(2);
            taxOnGD = rsTax.getString(7);
            taxCal = rsTax.getString(8);
            taxIndicator = rsTax.getString(9);
            taxOnTax = rsTax.getString(13);
            taxOnTaxCode = rsTax.getString(14);
            taxType = rsTax.getString(4);//taxType
            taxPercent = Double.parseDouble(rsTax.getString(5));//percent
            taxFixedAmount = Double.parseDouble(rsTax.getString(6));//fixes amount

            taxableAmount = 0.00;
            taxCalAmt = 0.00;

            String sqlTaxOn = "select strAreaCode,strOperationType,strItemType "
                    + "from tbltaxhd where strTaxCode='" + taxCode + "'";
            ResultSet rsTaxOn = clsGlobalClass.funExecuteResultSetQuery(sqlTaxOn);
            if (rsTaxOn.next())
            {
                taxAreaCodes = rsTaxOn.getString(1);
                opType = rsTaxOn.getString(2);
            }
            if (funCheckAreaCode(taxAreaCodes, billAreaCode))
            {
                if (funCheckOperationType(opType, operationTypeForTax))
                {
                    if (funFindSettlementForTax(taxCode, settlementCode))
                    {
                        boolean flgTaxOnGrpApplicable = false;
                        taxableAmount = 0;
                        clsTaxCalculationDtls objTaxDtls = new clsTaxCalculationDtls();

                        if (taxOnGD.equals("Gross"))
                        {
                            //to calculate tax on group of an item
                            for (int i = 0; i < arrListItemDtl.size(); i++)
                            {
                                clsItemDtlForTax objItemDtl = arrListItemDtl.get(i);

                                boolean isApplicable = isTaxApplicableOnItemGroup(taxCode, objItemDtl.getItemCode().substring(0, 7));
                                if (isApplicable)
                                {
                                    flgTaxOnGrpApplicable = true;
                                    taxableAmount = taxableAmount + objItemDtl.getAmount();

                                    if (taxOnTax.equalsIgnoreCase("Yes")) // For tax On Tax Calculation new logic only for same group item
                                    {
                                        taxableAmount = taxableAmount + funGetTaxableAmountForTaxOnTax(taxOnTaxCode, objItemDtl.getAmount(), arrListTaxDtl);
                                    }
                                }
                            }

//                            if (taxOnTax.equalsIgnoreCase("Yes")) // For tax On Tax Calculation
//                            {
//                                taxableAmount = taxableAmount + funGetTaxableAmountForTaxOnTax(taxOnTaxCode, arrListTaxDtl);
//                            }
                        }
                        else
                        {
                            subTotal = 0;
                            double discAmt = 0;
                            for (clsItemDtlForTax objItemDtl : arrListItemDtl)
                            {
                                boolean isApplicable = isTaxApplicableOnItemGroup(taxCode, objItemDtl.getItemCode().substring(0, 7));
                                if (isApplicable)
                                {
                                    flgTaxOnGrpApplicable = true;
                                    if (objItemDtl.getDiscAmt() > 0)
                                    {
                                        discAmt += objItemDtl.getDiscAmt();
                                    }
                                    taxableAmount = taxableAmount + objItemDtl.getAmount();

                                    if (taxOnTax.equalsIgnoreCase("Yes")) // For tax On Tax Calculation new logic only for same group item
                                    {
                                        taxableAmount = taxableAmount + funGetTaxableAmountForTaxOnTax(taxOnTaxCode, objItemDtl.getAmount() - objItemDtl.getDiscAmt(), arrListTaxDtl);
                                    }
                                }
                            }
                            if (taxableAmount > 0)
                            {
                                taxableAmount = taxableAmount - discAmt;
                            }
//                            if (taxOnTax.equalsIgnoreCase("Yes")) // For tax On Tax Calculation
//                            {
//                                taxableAmount += funGetTaxableAmountForTaxOnTax(taxOnTaxCode, arrListTaxDtl);
//                            }
                        }

                        if (flgTaxOnGrpApplicable)
                        {
                            if (taxCal.equals("Forward")) // Forward Tax Calculation
                            {
                                if (taxType.equalsIgnoreCase("Percent"))
                                {
                                    taxCalAmt = taxableAmount * (taxPercent / 100);
                                }
                                else
                                {
                                    taxCalAmt = taxFixedAmount;
                                }
                            }
                            else // Backward Tax Calculation
                            {
                                taxCalAmt = taxableAmount - (taxableAmount * 100 / (100 + taxPercent));
                            }

                            objTaxDtls.setTaxCode(taxCode);
                            objTaxDtls.setTaxName(taxName);
                            objTaxDtls.setTaxableAmount(taxableAmount);
                            objTaxDtls.setTaxAmount(taxCalAmt);
                            objTaxDtls.setTaxCalculationType(taxCal);
                            arrListTaxDtl.add(objTaxDtls);
                        }

                        /*
                         * if (taxIndicator.trim().length() > 0) // For
                         * Indicator Based Tax { double taxIndicatorTotal =
                         * funGetTaxIndicatorTotal(taxIndicator,
                         * arrListItemDtl); if (taxIndicatorTotal > 0) { double
                         * discAmt = 0, discPer = 0;// discAmt =
                         * funGetTaxIndicatorBasedDiscAmtTotal(taxIndicator,
                         * arrListItemDtl); if (taxIndicatorTotal > 0) { discPer
                         * = (discAmt / taxIndicatorTotal) * 100; }
                         *
                         * if (taxOnTax.equalsIgnoreCase("Yes")) // For tax On
                         * Tax Calculation { taxIndicatorTotal +=
                         * funGetTaxAmountForTaxOnTaxForIndicatorTax(taxOnTaxCode,
                         * taxIndicatorTotal, arrListTaxDtl); } if
                         * (taxOnGD.equals("Gross")) { taxableAmount =
                         * taxIndicatorTotal; } else { taxableAmount =
                         * taxIndicatorTotal - ((taxIndicatorTotal * discPer) /
                         * 100); }
                         *
                         * if (taxCal.equals("Forward")) // Forward Tax
                         * Calculation { taxCalAmt = taxableAmount * (taxPercent
                         * / 100); } else // Backward Tax Calculation {
                         * taxCalAmt = taxableAmount * 100 / (100 + taxPercent);
                         * taxCalAmt = taxableAmount - taxCalAmt; }
                         * objTaxDtls.setTaxCode(taxCode);
                         * objTaxDtls.setTaxName(taxName);
                         * objTaxDtls.setTaxableAmount(taxableAmount);
                         * objTaxDtls.setTaxAmount(taxCalAmt);
                         * objTaxDtls.setTaxCalculationType(taxCal);
                         * arrListTaxDtl.add(objTaxDtls); } } else // For Blank
                         * Indicator { if (taxOnTax.equalsIgnoreCase("Yes")) //
                         * For tax On Tax Calculation { if
                         * (taxOnGD.equals("Gross")) {
                         *
                         * //to calculate tax on group of an item for (int i =
                         * 0; i < arrListItemDtl.size(); i++) { clsItemDtlForTax
                         * objItemDtl = arrListItemDtl.get(i);
                         *
                         * boolean isApplicable =
                         * isTaxApplicableOnItemGroup(taxCode,
                         * arrListItemDtl.get(i)); if (isApplicable) {
                         *
                         * }
                         * else { subTotal = subTotal - objItemDtl.getAmount();
                         * } }
                         *
                         * taxableAmount = subTotal +
                         * funGetTaxableAmountForTaxOnTax(taxOnTaxCode,
                         * arrListTaxDtl); } else { subTotal = 0; double discAmt
                         * = 0; for (clsItemDtlForTax objItemDtl :
                         * arrListItemDtl) { boolean isApplicable =
                         * isTaxApplicableOnItemGroup(taxCode, objItemDtl); if
                         * (isApplicable) { if (objItemDtl.getDiscAmt() > 0) {
                         * discAmt += objItemDtl.getDiscAmt(); } subTotal +=
                         * objItemDtl.getAmount(); } else {
                         *
                         * }
                         * }
                         * taxableAmount = subTotal - discAmt; taxableAmount +=
                         * funGetTaxableAmountForTaxOnTax(taxOnTaxCode,
                         * arrListTaxDtl); }
                         *
                         * if (taxCal.equals("Forward")) // Forward Tax
                         * Calculation { taxCalAmt = taxableAmount * (taxPercent
                         * / 100); } else // Backward Tax Calculation {
                         * taxCalAmt = taxableAmount - (taxableAmount * 100 /
                         * (100 + taxPercent)); }
                         * objTaxDtls.setTaxCode(taxCode);
                         * objTaxDtls.setTaxName(taxName);
                         * objTaxDtls.setTaxableAmount(taxableAmount);
                         * objTaxDtls.setTaxAmount(taxCalAmt);
                         * objTaxDtls.setTaxCalculationType(taxCal);
                         * arrListTaxDtl.add(objTaxDtls); } else { if
                         * (taxOnGD.equals("Gross")) { //to calculate tax on
                         * group of an item for (int i = 0; i <
                         * arrListItemDtl.size(); i++) { clsItemDtlForTax
                         * objItemDtl = arrListItemDtl.get(i);
                         *
                         * boolean isApplicable =
                         * isTaxApplicableOnItemGroup(taxCode,
                         * arrListItemDtl.get(i)); if (isApplicable) {
                         *
                         * }
                         * else { subTotal = subTotal - objItemDtl.getAmount();
                         * } }
                         *
                         * taxableAmount = subTotal; } else { subTotal = 0;
                         * double discAmt = 0; for (int cn = 0; cn <
                         * arrListItemDtl.size(); cn++) { clsItemDtlForTax
                         * objItemDtl = arrListItemDtl.get(cn); // discAmt +=
                         * objItemDtl.getDiscAmt(); // subTotal +=
                         * objItemDtl.getAmount();
                         *
                         * boolean isApplicable =
                         * isTaxApplicableOnItemGroup(taxCode, objItemDtl); if
                         * (isApplicable) { if (objItemDtl.getDiscAmt() > 0) {
                         * discAmt += objItemDtl.getDiscAmt(); } subTotal +=
                         * objItemDtl.getAmount(); } else {
                         *
                         * }
                         *
                         * }
                         * taxableAmount = subTotal - discAmt; }
                         *
                         * if (taxCal.equals("Forward")) // Forward Tax
                         * Calculation { taxCalAmt = taxableAmount * (taxPercent
                         * / 100); } else // Backward Tax Calculation {
                         * taxCalAmt = taxableAmount - (taxableAmount * 100 /
                         * (100 + taxPercent)); }
                         * objTaxDtls.setTaxCode(taxCode);
                         * objTaxDtls.setTaxName(taxName);
                         * objTaxDtls.setTaxableAmount(taxableAmount);
                         * objTaxDtls.setTaxAmount(taxCalAmt);
                         * objTaxDtls.setTaxCalculationType(taxCal);
                         * arrListTaxDtl.add(objTaxDtls); } }
                         */
                    }
                }
            }
        }

        return arrListTaxDtl;
    }

    private boolean funCheckAreaCode(String taxAreaCodes, String billAreaCode)
    {
        boolean flgTaxOn = false;
        String[] spAreaCode = taxAreaCodes.split(",");
        for (int cnt = 0; cnt < spAreaCode.length; cnt++)
        {
            if (spAreaCode[cnt].equals(billAreaCode))
            {
                flgTaxOn = true;
                break;
            }
        }

        return flgTaxOn;
    }

    private double funGetTaxIndicatorBasedDiscAmtTotal(String indicator, List<clsItemDtlForTax> arrListItemDtl) throws Exception
    {
        String sql_Query = "";
        double discAmt = 0.00;
        for (int cnt = 0; cnt < arrListItemDtl.size(); cnt++)
        {
            clsItemDtlForTax objItemDtl = arrListItemDtl.get(cnt);
            sql_Query = "select strTaxIndicator from tblitemmaster "
                    + "where strItemCode='" + objItemDtl.getItemCode().substring(0, 7) + "' "
                    + "and strTaxIndicator='" + indicator + "'";
            ResultSet rsTaxForDB = clsGlobalClass.funExecuteResultSetQuery(sql_Query);
            if (rsTaxForDB.next())
            {
                discAmt += objItemDtl.getDiscAmt();
            }
            rsTaxForDB.close();
        }
        return discAmt;
    }

    private boolean isTaxApplicableOnItemGroup(String taxCode, String itemCode)
    {
        boolean isApplicable = false;
        try
        {
            String sql = "select a.strItemCode,a.strItemName,b.strSubGroupCode,b.strSubGroupName,c.strGroupCode,c.strGroupName,d.strTaxCode,d.strApplicable "
                    + "from tblitemmaster a,tblsubgrouphd b,tblgrouphd c,tbltaxongroup d "
                    + "where a.strSubGroupCode=b.strSubGroupCode "
                    + "and b.strGroupCode=c.strGroupCode "
                    + "and c.strGroupCode=d.strGroupCode "
                    + "and a.strItemCode='" + itemCode + "' "
                    + "and d.strTaxCode='" + taxCode + "' "
                    + "and d.strApplicable='true' ";
            ResultSet rsTaxApplicable = clsGlobalClass.funExecuteResultSetQuery(sql);
            if (rsTaxApplicable.next())
            {
                isApplicable = true;
            }
            rsTaxApplicable.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            return isApplicable;
        }
    }

    private boolean funCheckOperationType(String taxOpTypes, String operationTypeForTax)
    {
        boolean flgTaxOn = false;
        String[] spOpType = taxOpTypes.split(",");
        for (int cnt = 0; cnt < spOpType.length; cnt++)
        {
            if (spOpType[cnt].equals("HomeDelivery") && operationTypeForTax.equalsIgnoreCase("HomeDelivery"))
            {
                flgTaxOn = true;
                break;
            }
            if (spOpType[cnt].equals("HomeDelivery") && operationTypeForTax.equalsIgnoreCase("Home Delivery"))
            {
                flgTaxOn = true;
                break;
            }
            if (spOpType[cnt].equals("DineIn") && operationTypeForTax.equalsIgnoreCase("DineIn"))
            {
                flgTaxOn = true;
                break;
            }
            if (spOpType[cnt].equals("DineIn") && operationTypeForTax.equalsIgnoreCase("Dine In"))
            {
                flgTaxOn = true;
                break;
            }
            if (spOpType[cnt].equals("TakeAway") && operationTypeForTax.equalsIgnoreCase("TakeAway"))
            {
                flgTaxOn = true;
                break;
            }
            if (spOpType[cnt].equals("TakeAway") && operationTypeForTax.equalsIgnoreCase("Take Away"))
            {
                flgTaxOn = true;
                break;
            }
        }
        return flgTaxOn;
    }

    private double funGetTaxIndicatorTotal(String indicator, List<clsItemDtlForTax> arrListItemDtl) throws Exception
    {
        String sql_Query = "";
        double indicatorAmount = 0.00;
        for (int cnt = 0; cnt < arrListItemDtl.size(); cnt++)
        {
            clsItemDtlForTax objItemDtl = arrListItemDtl.get(cnt);
            sql_Query = "select strTaxIndicator from tblitemmaster "
                    + "where strItemCode='" + objItemDtl.getItemCode().substring(0, 7) + "' "
                    + "and strTaxIndicator='" + indicator + "'";
            ResultSet rsTaxForDB = clsGlobalClass.funExecuteResultSetQuery(sql_Query);
            if (rsTaxForDB.next())
            {
                indicatorAmount += objItemDtl.getAmount();
            }
            rsTaxForDB.close();
        }
        return indicatorAmount;
    }

    private double funGetItemTypeTotal(String itemType, List<clsItemDtlForTax> arrListItemDtl) throws Exception
    {
        String sql_Query = "";
        double itemTypeAmount = 0.00;

        for (int cnt = 0; cnt < arrListItemDtl.size(); cnt++)
        {
            clsItemDtlForTax objItemDtl = arrListItemDtl.get(cnt);
            sql_Query = "select strTaxIndicator from tblitemmaster "
                    + "where strItemCode='" + objItemDtl.getItemCode() + "' "
                    + "and and strItemType='" + itemType + "'";
            ResultSet rsTaxForDB = clsGlobalClass.funExecuteResultSetQuery(sql_Query);
            if (rsTaxForDB.next())
            {
                itemTypeAmount += objItemDtl.getAmount();
            }
            rsTaxForDB.close();
        }
        return itemTypeAmount;
    }

    private boolean funFindSettlementForTax(String taxCode, String settlementCode) throws Exception
    {
        boolean flgTaxSettlement = false;
        String sql_SettlementTax = "select strSettlementCode,strSettlementName "
                + "from tblsettlementtax where strTaxCode='" + taxCode + "' "
                + "and strApplicable='true' and strSettlementCode='" + settlementCode + "'";
        ResultSet rsTaxSettlement = clsGlobalClass.funExecuteResultSetQuery(sql_SettlementTax);
        if (rsTaxSettlement.next())
        {
            flgTaxSettlement = true;
        }
        rsTaxSettlement.close();
        return flgTaxSettlement;
    }

    private double funGetTaxableAmountForTaxOnTax(String taxOnTaxCode, List<clsTaxCalculationDtls> arrListTaxCal) throws Exception
    {
        double taxAmt = 0;
        String[] spTaxOnTaxCode = taxOnTaxCode.split(",");
        for (int cnt = 0; cnt < arrListTaxCal.size(); cnt++)
        {
            for (int t = 0; t < spTaxOnTaxCode.length; t++)
            {
                clsTaxCalculationDtls objTaxDtls = arrListTaxCal.get(cnt);
                if (objTaxDtls.getTaxCode().equals(spTaxOnTaxCode[t]))
                {
                    taxAmt += objTaxDtls.getTaxAmount();
                }
            }
        }
        return taxAmt;
    }

    //new logic for tax on tax
    private double funGetTaxableAmountForTaxOnTax(String taxOnTaxCode, double taxableAmt, List<clsTaxCalculationDtls> listTaxDtl) throws Exception
    {
        double taxAmt = 0;
        String[] spTaxOnTaxCode = taxOnTaxCode.split(",");
        for (clsTaxCalculationDtls objTaxCalDtl : listTaxDtl)
        {
            for (int t = 0; t < spTaxOnTaxCode.length; t++)
            {
                if (objTaxCalDtl.getTaxCode().equals(spTaxOnTaxCode[t]))
                {
                    taxAmt += funGetTaxOnTaxAmt(spTaxOnTaxCode[t], taxableAmt);
                }
            }
        }

        return taxAmt;
    }

    private double funGetTaxOnTaxAmt(String taxCode, double taxableAmt) throws Exception
    {
        double taxAmt = 0;
        String sql = "select a.strTaxCode,a.strTaxType,a.dblPercent"
                + " ,a.dblAmount,a.strTaxOnGD,a.strTaxCalculation "
                + " from tbltaxhd a "
                + " where a.strTaxOnSP='Sales' and a.strTaxCode='" + taxCode + "'";
        ResultSet rsTax = clsGlobalClass.funExecuteResultSetQuery(sql);
        if (rsTax.next())
        {
            double taxPercent = rsTax.getDouble(3);
            if (rsTax.getString(6).equals("Forward")) // Forward Tax Calculation
            {
                taxAmt = taxableAmt * (taxPercent / 100);
            }
            else // Backward Tax Calculation
            {
                taxAmt = taxableAmt * 100 / (100 + taxPercent);
                taxAmt = taxableAmt - taxAmt;
            }
        }
        rsTax.close();
        return taxAmt;
    }

    void funReCalculateDiscountForBill(String transactionName, String QOrLiveFile, String posCode, String clientCode, String billNo, String billDate)
    {
        try
        {
            String tblBillHd = "tblbillhd";
            String tblBillDtl = "tblbilldtl";
            String tblBillModifierDtl = "tblbillmodifierdtl";
            String tblBillDiscDtl = "tblbilldiscdtl";
            if (QOrLiveFile.equalsIgnoreCase("QFile"))
            {
                tblBillHd = "tblqbillhd";
                tblBillDtl = "tblqbilldtl";
                tblBillModifierDtl = "tblqbillmodifierdtl";
                tblBillDiscDtl = "tblqbilldiscdtl";
            }

            StringBuilder sqlBuilder = new StringBuilder();
            StringBuilder sqlBillBuilder = new StringBuilder();
            StringBuilder sqlModifierBuilder = new StringBuilder();
            StringBuilder sqlFilter = new StringBuilder();

            sqlBuilder.append("select a.strBillNo,a.strPOSCode,a.dblDiscAmt,a.dblDiscPer,a.strDiscOnType,a.strDiscOnValue,a.strDiscReasonCode,a.strDiscRemarks "
                    + "from " + tblBillDiscDtl + " a "
                    + "where a.strBillNo='" + billNo + "' "
                    + "AND DATE(dteBillDate)='" + billDate + "' "
                    //+ "and a.strPOSCode='" + posCode + "' "
                    + "and  a.strClientCode='" + clientCode + "' ");
            ResultSet rsBillDiscDtl = clsGlobalClass.funExecuteResultSetQuery(sqlBuilder.toString());
            while (rsBillDiscDtl.next())
            {
                sqlBillBuilder.setLength(0);
                sqlModifierBuilder.setLength(0);
                sqlFilter.setLength(0);

                String discOnType = rsBillDiscDtl.getString(5);
                String discOnValue = rsBillDiscDtl.getString(6);
                double discPer = rsBillDiscDtl.getDouble(4);

                String groupCode = null;
                String subGroupCode = null;
                String itemCode = null;

                if (discOnType.equalsIgnoreCase("GroupWise"))
                {
                    groupCode = funGetGroupCode(discOnValue);
                    sqlFilter.append("and c.strGroupCode='" + groupCode + "' ");
                }
                else if (discOnType.equalsIgnoreCase("SubGroupWise"))
                {
                    subGroupCode = funGetSubGroupCode(discOnValue);
                    sqlFilter.append("and b.strSubGroupCode='" + subGroupCode + "' ");
                }
                else if (discOnType.equalsIgnoreCase("ItemWise"))
                {
                    itemCode = funGetItemCode(discOnValue);
                    sqlFilter.append("and d.strItemCode='" + itemCode + "' ");
                }
                else if (discOnType.equalsIgnoreCase("Total"))
                {
                    //total
                    sqlFilter.append("");
                }
                sqlFilter.append("group by d.strItemCode ");

                sqlBillBuilder.append("select d.strItemCode,d.strItemName,d.dblRate,sum(d.dblQuantity),sum(d.dblAmount),d.dblDiscountPer,sum(d.dblDiscountAmt) "
                        + "from tblitemmaster a,tblsubgrouphd b,tblgrouphd c," + tblBillDtl + " d "
                        + "where a.strSubGroupCode=b.strSubGroupCode "
                        + "and b.strGroupCode=c.strGroupCode "
                        + "and a.strItemCode=d.strItemCode "
                        + "and d.strBillNo='" + billNo + "' "
                        + "AND DATE(d.dteBillDate)='" + billDate + "' "
                        + "and d.strClientCode='" + clientCode + "' ");
                sqlBillBuilder.append(sqlFilter);

                sqlModifierBuilder.append("select d.strItemCode,d.strModifierName,d.dblRate,sum(d.dblQuantity),sum(d.dblAmount),d.dblDiscPer,sum(d.dblDiscAmt) "
                        + "from tblitemmaster a,tblsubgrouphd b,tblgrouphd c," + tblBillModifierDtl + " d "
                        + "where a.strSubGroupCode=b.strSubGroupCode "
                        + "and b.strGroupCode=c.strGroupCode "
                        + "and a.strItemCode=left(d.strItemCode,7) "
                        + "and d.strBillNo='" + billNo + "' "
                        + "AND DATE(d.dteBillDate)='" + billDate + "' "
                        + "and d.strClientCode='" + clientCode + "' ");
                sqlModifierBuilder.append(sqlFilter);

                double subTotal = 0.00, discAmt = 0.00;

                ResultSet rsBillDtl = clsGlobalClass.funExecuteResultSetQuery(sqlBillBuilder.toString());
                while (rsBillDtl.next())
                {
                    subTotal += rsBillDtl.getDouble(5);
                    discAmt += rsBillDtl.getDouble(7);
                }
                rsBillDtl.close();

                ResultSet rsBillModifierDtl = clsGlobalClass.funExecuteResultSetQuery(sqlModifierBuilder.toString());
                while (rsBillModifierDtl.next())
                {
                    subTotal += rsBillModifierDtl.getDouble(5);
                    discAmt += rsBillModifierDtl.getDouble(7);
                }
                rsBillModifierDtl.close();

                if (subTotal > 0)
                {
                    clsGlobalClass.funExecuteUpdateQuery("update " + tblBillDiscDtl + " "
                            + "set dblDiscAmt='" + discAmt + "',dblDiscOnAmt='" + subTotal + "' "
                            + "where strBillNo='" + billNo + "' "
                            + "AND DATE(dteBillDate)='" + billDate + "' "
                            + "and strDiscOnType='" + discOnType + "' "
                            + "and strDiscOnValue='" + discOnValue + "' ");
                }
                else
                {
                    clsGlobalClass.funExecuteUpdateQuery("delete  from " + tblBillDiscDtl + " "
                            + "where strBillNo='" + billNo + "' "
                            + "AND DATE(dteBillDate)='" + billDate + "' "
                            + "and strDiscOnType='" + discOnType + "' "
                            + "and strDiscOnValue='" + discOnValue + "' ");
                }

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String funGetGroupCode(String groupName)
    {
        String groupCode = null;
        try
        {
            ResultSet rsSubGroupCode = clsGlobalClass.funExecuteResultSetQuery("select a.strGroupCode from tblgrouphd a where a.strGroupName='" + groupName + "' ");
            if (rsSubGroupCode.next())
            {
                groupCode = rsSubGroupCode.getString(1);
            }
            rsSubGroupCode.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            return groupCode;
        }
    }

    private String funGetSubGroupCode(String subGroupName)
    {
        String subGroupCode = null;
        try
        {
            ResultSet rsSubGroupCode = clsGlobalClass.funExecuteResultSetQuery("select a.strSubGroupCode from tblsubgrouphd a where a.strSubGroupName='" + subGroupName + "' ");
            if (rsSubGroupCode.next())
            {
                subGroupCode = rsSubGroupCode.getString(1);
            }
            rsSubGroupCode.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            return subGroupCode;
        }
    }

    private String funGetItemCode(String itemName)
    {
        String itemCode = null;
        try
        {
            ResultSet rsSubGroupCode = clsGlobalClass.funExecuteResultSetQuery("select a.strItemCode from tblitemmaster a where a.strItemNAme='" + itemName + "' ");
            if (rsSubGroupCode.next())
            {
                itemCode = rsSubGroupCode.getString(1);
            }
            rsSubGroupCode.close();
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
    
      /*
     * this is a customise function to calculate roundoff amount to X amount
     *
     * hash map returns roundoff amount and roundoff by amount
     */
    public Map funCalculateRoundOffAmount(double settlementAmt)
    {
        Map<String, Double> hm = new HashMap<>();

        double roundOffTo = 1;

        if (roundOffTo == 0.00)
        {
            roundOffTo = 1.00;
        }

        double roundOffSettleAmt = settlementAmt;
        double remainderAmt = (settlementAmt % roundOffTo);
        double roundOffToBy2 = roundOffTo / 2;
        double x = 0.00;

        if (remainderAmt <= roundOffToBy2)
        {
            x = (-1) * remainderAmt;

            roundOffSettleAmt = (Math.floor(settlementAmt / roundOffTo) * roundOffTo);

            //System.out.println(settleAmt + " " + roundOffSettleAmt + " " + x);
        }
        else
        {
            x = roundOffTo - remainderAmt;

            roundOffSettleAmt = (Math.ceil(settlementAmt / roundOffTo) * roundOffTo);

            // System.out.println(settleAmt + " " + roundOffSettleAmt + " " + x);
        }

        hm.put("roundOffAmt", roundOffSettleAmt);
        hm.put("roundOffByAmt", x);

        System.out.println("Original Settl Amt=" + settlementAmt + " RoundOff Settle Amt=" + roundOffSettleAmt + " RoundOff To=" + roundOffTo + " RoundOff By=" + x);

        return hm;

    }

}
