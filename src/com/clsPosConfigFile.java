package com;

import java.io.*;

public class clsPosConfigFile
{
    private File file;
    private BufferedReader br;
    public static String userId,password,ipAddress,databaseName,serverName,portNo;
    public static String[] configData,tempData;
    private String fileData;
    private int i;
    
    public clsPosConfigFile()
    {
        try
        {
            i=0;
            configData=new String[6];
            tempData=new String[6];
            file=new File(System.getProperty("user.dir")+"/DBConfigFile.txt");
            br=new BufferedReader(new FileReader(file));
            while((fileData=br.readLine())!=null)
            {
                String[] split=fileData.split("=");
                if(split.length>1)
                {
                    tempData[i]=split[0];
                    configData[i]=split[1];
                    i++;
                }
            }            
            serverName=configData[0].trim();
            databaseName=configData[1].trim();
            userId=configData[2].trim();
            password=configData[3].trim();
            ipAddress=configData[4].trim();
            portNo=configData[5].trim();
 
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
