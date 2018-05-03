/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.POSLicence.controller.clsClientDetails;
import com.POSLicence.controller.clsEncryptDecryptClientCode;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

public class clsMainClass
{

    public static String gAreaWisePricing;

    public static void main(String[] args)
    {
	try
	{

	    clsPosConfigFile configFile = new clsPosConfigFile();

	    frmBillRegeneration billRegeneration = new frmBillRegeneration();

	    String sql = "select strClientCode,strClientName from tblsetup;";
	    ResultSet rs = clsGlobalClass.funExecuteResultSetQuery(sql);
	    if (rs.next())
	    {
		clsClientDetails.funAddClientCodeAndName();

		String clientCodeFromDB = rs.getString(1);
		String clientNameFromDB = rs.getString(2);
		String encryptedClientCodeFromDB = clsEncryptDecryptClientCode.funEncryptClientCode(clientCodeFromDB);
		if (clsClientDetails.hmClientDtl.containsKey(encryptedClientCodeFromDB))
		{
		    clsGlobalClass.gClientCode = clientCodeFromDB;

		    //login Successfull
		    String decryptedClientNameFromHM = clsEncryptDecryptClientCode.funDecryptClientCode(clsClientDetails.hmClientDtl.get(encryptedClientCodeFromDB).Client_Name);
		    if (decryptedClientNameFromHM.equalsIgnoreCase(clientNameFromDB))
		    {
			String billDeletionLicense = clsEncryptDecryptClientCode.funDecryptClientCode(clsClientDetails.hmClientDtl.get(encryptedClientCodeFromDB).getStrBillDeletion());
			if (billDeletionLicense.equalsIgnoreCase("Bill Deletion"))
			{
			    billRegeneration.setVisible(true);
			}
			else
			{
			    JOptionPane.showMessageDialog(null, "Please purchase the license.", "No Bill Deletion License", 0);
			    System.exit(0);
			}
		    }
		    else
		    {
			JOptionPane.showMessageDialog(null, "Please purchase the license.", "No Bill Deletion License", 0);
			System.exit(0);
		    }
		}
		else
		{
		    JOptionPane.showMessageDialog(null, "Please purchase the license.", "No Bill Deletion License", 0);
		    System.exit(0);
		}
	    }
	    else
	    {
		JOptionPane.showMessageDialog(null, "Please purchase the license.", "No Bill Deletion License", 0);
		System.exit(0);
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }

}
