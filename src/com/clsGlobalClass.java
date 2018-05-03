/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.bean.clsItemDtl;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class clsGlobalClass
{
    public static String gPOSCode=null;   
    public static Connection gConnection=null;   
    private static Statement statement=null;
    private static ResultSet resultset=null;
    public static String gFromDate=null;  
    public static String gToDate=null;  
    public static String gClientCode=null;  
    public static double gTotalDiscounts;
    public static double gTotalCashSales;
    public static int gNoOfDiscountedBills;
    public static int gTotalBills;
    public static double gTotalAdvanceAmt;
    public static double gTotalReceipt;
    public static double gTotalPayments;
    public static double gTotalCashInHand;
    public static String gShiftNo;
    public static String gUserCode;    
    public static String gNumerickeyboardValue;
    
    
    public static ResultSet funExecuteResultSetQuery(String resultSetQuery)
    {
        try
        {
            statement=gConnection.createStatement();
            resultset=statement.executeQuery(resultSetQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return resultset;
    }
    
    public static int funExecuteUpdateQuery(String updateQuery)
    {
        int i=-1;
        try
        {
            statement=gConnection.createStatement();
            i=statement.executeUpdate(updateQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return i;
    }

    static String getCurrentDateTime()
    {
        return null;
    }
}
